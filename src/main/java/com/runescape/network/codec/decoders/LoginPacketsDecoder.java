package com.runescape.network.codec.decoders;

import com.runescape.cache.Cache;
import com.runescape.game.GameConstants;
import com.runescape.game.content.GlobalPlayers;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Player;
import com.runescape.network.Session;
import com.runescape.network.codec.Decoder;
import com.runescape.network.stream.InputStream;
import com.runescape.utility.AntiFlood;
import com.runescape.utility.BCrypt;
import com.runescape.utility.Utils;
import com.runescape.utility.cache.IsaacKeyPair;
import com.runescape.utility.external.gson.GsonStartup;
import com.runescape.utility.external.gson.loaders.PunishmentLoader;
import com.runescape.utility.external.gson.resource.Punishment.PunishmentType;
import com.runescape.utility.world.player.PlayerSaving;
import com.runescape.workers.db.mysql.impl.DatabaseFunctions;
import com.runescape.workers.db.mysql.impl.DatabaseFunctions.ForumLoginResults;
import com.runescape.workers.game.login.LoginResponses;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class LoginPacketsDecoder extends Decoder {

	private static final ExecutorService executor = Executors.newSingleThreadExecutor();

	private static final Object LOCK = new Object();

	@Override
	public int decode(InputStream stream) {
		session.setDecoder(-1);
		int packetId = stream.readUnsignedByte();
		if (packetId == 16) {
			decodeWorldLogin(stream);
		} else {
			session.getChannel().close();
		}
		return stream.getOffset();
	}

	public void decodeWorldLogin(InputStream stream) {
		executor.execute(() -> {
			try {
				if (World.exiting_start != 0) {
					session.getLoginPackets().sendClientPacket(14);
					return;
				}
				int packetSize = stream.readUnsignedShort();
				if (packetSize != stream.getRemaining()) {
					session.getChannel().close();
					return;
				}
				if (stream.readInt() != GameConstants.CLIENT_BUILD || stream.readInt() != GameConstants.CUSTOM_CLIENT_BUILD) {
					session.getLoginPackets().sendClientPacket(6);
					return;
				}
				stream.readUnsignedByte();
				if (stream.readUnsignedByte() != 10) { // rsa block check
					session.getLoginPackets().sendClientPacket(10);
					return;
				}
				int[] isaacKeys = new int[4];
				for (int i = 0; i < isaacKeys.length; i++) {
					isaacKeys[i] = stream.readInt();
				}
				if (stream.readLong() != 0L) { // rsa block check, pass part
					session.getLoginPackets().sendClientPacket(10);
					return;
				}
				String password = stream.readString();
				Utils.longToString(stream.readLong());
				stream.readLong(); // random value
				stream.decodeXTEA(isaacKeys, stream.getOffset(), stream.getLength());
				String username = Utils.formatPlayerNameForProtocol(stream.readString());
				String macAddress = stream.readString();
				stream.readUnsignedByte(); // unknown
				int displayMode = stream.readUnsignedByte();
				int screenWidth = stream.readUnsignedShort();
				int screenHeight = stream.readUnsignedShort();
				stream.readUnsignedByte();
				stream.skip(24); // 24bytes directly from a file, no idea whats there
				stream.readString();
				stream.readInt();
				stream.skip(stream.readUnsignedByte()); // useless settings
				if (stream.readUnsignedByte() != 5) {
					session.getLoginPackets().sendClientPacket(10);
					return;
				}
				stream.readUnsignedByte();
				stream.readUnsignedByte();
				stream.readUnsignedByte();
				stream.readUnsignedByte();
				stream.readUnsignedByte();
				stream.readUnsignedByte();
				stream.readUnsignedByte();
				stream.readUnsignedByte();
				stream.readUnsignedShort();
				stream.readUnsignedByte();
				stream.read24BitInt();
				stream.readUnsignedShort();
				stream.readUnsignedByte();
				stream.readUnsignedByte();
				stream.readUnsignedByte();
				stream.readJagString();
				stream.readJagString();
				stream.readJagString();
				stream.readJagString();
				stream.readUnsignedByte();
				stream.readUnsignedShort();
				stream.readInt();
				stream.readLong();
				boolean hasAditionalInformation = stream.readUnsignedByte() == 1;
				if (hasAditionalInformation) {
					stream.readString(); // aditionalInformation
				}
				stream.readUnsignedByte();
				stream.readUnsignedByte();
				stream.readUnsignedByte();
				for (int index = 0; index < Cache.STORE.getIndexes().length; index++) {
					int crc = Cache.STORE.getIndexes()[index] == null ? 0 : Cache.STORE.getIndexes()[index].getCRC();
					int receivedCRC = stream.readInt();
					if (crc != receivedCRC && index < 32) {
						System.out.println("Invalid CRC at index: " + index + ", " + receivedCRC + ", " + crc);
						session.getLoginPackets().sendClientPacket(6);
						return;
					}
				}
				synchronized (LOCK) {
					long start = System.currentTimeMillis();
					if (Utils.invalidAccountName(username) || password.length() > 30) {
						session.getLoginPackets().sendClientPacket(LoginResponses.INVALID_USERNAME);
						return;
					}
					if (World.getPlayers().size() >= GameConstants.PLAYERS_LIMIT - 10) {
						session.getLoginPackets().sendClientPacket(7);
						return;
					}
					if (World.containsPlayer(username)) {
						session.getLoginPackets().sendClientPacket(5);
						return;
					}
					if (GameConstants.HOSTED && AntiFlood.getSessionsIP(session.getIP()) > 2) {
						session.getLoginPackets().sendClientPacket(9);
						return;
					}
					if (username.toLowerCase().contains("dragonkk") || username.toLowerCase().contains("apache")) {
						session.getLoginPackets().sendClientPacket(3);
						return;
					}
					if (GsonStartup.getClass(PunishmentLoader.class).isPunished(new Object[][] { new Object[] { username, PunishmentType.BAN }, new Object[] { macAddress, PunishmentType.MACBAN } }) != null) {
						session.getLoginPackets().sendClientPacket(4);
						return;
					}
					Player player;
					if (!PlayerSaving.playerExists(username)) {
						player = new Player().constructPlayer();
					} else {
						player = PlayerSaving.fromFile(username);
						if (player == null) {
							session.getLoginPackets().sendClientPacket(20);
							return;
						}
						if (!player.getPassword().equals(password)) {
							session.getLoginPackets().sendClientPacket(3);
							return;
						}
					}
					// we only modify the player password variables if sql is enabled
					if (GameConstants.SQL_ENABLED) {
						boolean shouldCheckDatabase = checkStoredPassword(player, password);
						boolean passwordMatch = player.getPasswordHash() != null && BCrypt.checkpw(password, player.getPasswordHash());
						if (!passwordMatch) {
							shouldCheckDatabase = true;
						}
						ForumLoginResults result = DatabaseFunctions.correctCredentials(username, password, session);
				        System.out.println("[shouldCheckDatabase=" + shouldCheckDatabase + ", result=" + result + ", passwordMatch=" + passwordMatch + ", username=" + username + "]");
						// we will only stop this block with a return statement if the login should not happen.
						if (result == null) {
							session.getLoginPackets().sendClientPacket(LoginResponses.DATABASE_CONNECTION_ERROR);
							return;
						}
						switch (result) {
							case WRONG_CREDENTIALS:
								session.getLoginPackets().sendClientPacket(LoginResponses.INVALID_CREDENTIALS);
								return;
							case SQL_ERROR:
								session.getLoginPackets().sendClientPacket(LoginResponses.DATABASE_CONNECTION_ERROR);
								return;
							case NON_EXISTANT_USERNAME:
								// login still happens when the username doesnt exist, they just get a new account made.
								if (!DatabaseFunctions.registerUser(username, password)) {
									session.getLoginPackets().sendClientPacket(LoginResponses.DATABASE_CONNECTION_ERROR);
									return;
								}
								GlobalPlayers.addRegistrationToFile(username);

								session.getLoginPackets().sendClientPacket(LoginResponses.REGISTERED_NOTIFICATION);
								return;
							default:
								player.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));
								break;
						}
					}
					try {
						player.init(session, username, password, macAddress, displayMode, screenWidth, screenHeight, new IsaacKeyPair(isaacKeys));
						session.getLoginPackets().sendLoginDetails(player);
						session.setDecoder(3, player);
						session.setEncoder(2, player);
						player.start();
					} catch (Throwable e) {
						e.printStackTrace();
					}
					long duration = System.currentTimeMillis() - start;
					if (duration > 100) {
						System.out.println("Login Took " + duration + " ms for " + username + " on " + Thread.currentThread().getName());
					}
				}
				//	LoginRequestProcessor.getSingleton().submit(new LoginRequest(session, username, password, macAddress, displayMode, screenWidth, screenHeight, isaacKeys));
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	/**
	 * This method checks if the player has a password existing or not. If they do not have one, we set their password
	 * to a new encrypted one.
	 *
	 * @param player
	 * 		The player
	 * @return {@code True} if we had to set their password
	 */
	private boolean checkStoredPassword(Player player, String password) {
		if (player.getPasswordHash() == null) {
			player.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));
			return true;
		}
		return false;
	}

	public LoginPacketsDecoder(Session session) {
		super(session);
	}
}