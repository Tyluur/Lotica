package com.runescape.workers.game.login;

import com.runescape.game.GameConstants;
import com.runescape.game.content.GlobalPlayers;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Player;
import com.runescape.network.Session;
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

/**
 * The login request class. This class is constructed when passing a LoginService.
 *
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 17th, 2015
 */
public class LoginRequest {

	/**
	 * The session of the login request
	 */
	private final Session session;

	/**
	 * The username of the login request
	 */
	private final String username;

	/**
	 * The password of the login request
	 */
	private final String password;

	/**
	 * The mac address of the client
	 */
	private final String macAddress;

	/**
	 * The display mode of the client
	 */
	private final int displayMode;

	/**
	 * The width of the client
	 */
	private final int screenWidth;

	/**
	 * The height of the client
	 */
	private final int screenHeight;

	/**
	 * The isaac keys sent from the client
	 */
	private final int[] isaacKeys;

	@Override
	public String toString() {
		return "username=" + username + "";
	}

	public LoginRequest(Session session, String username, String password, String macAddress, int displayMode, int screenWidth, int screenHeight, int[] isaacKeys) {
		this.session = session;
		this.username = username;
		this.password = password;
		this.macAddress = macAddress;
		this.displayMode = displayMode;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.isaacKeys = isaacKeys;
	}

	/**
	 * Executing the login service. This will handle all aspects of login such as password verification and player
	 * initialization post login.
	 */
	public void execute() {
		try {
			System.out.println("executing the login request");
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
			}
			// we only modify the player password variables if sql is enabled
			if (GameConstants.SQL_ENABLED) {
				boolean shouldCheckDatabase = checkStoredPassword(player);
				boolean passwordMatch = player.getPasswordHash() != null && BCrypt.checkpw(password, player.getPasswordHash());
				if (!passwordMatch) {
					shouldCheckDatabase = true;
				}
				ForumLoginResults result = DatabaseFunctions.correctCredentials(username, password, session);
//				System.out.println("[shouldCheckDatabase=" + shouldCheckDatabase + ", result=" + result + ", passwordMatch=" + passwordMatch + ", username=" + username + "]");
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
			//if (duration > 100) {
				System.out.println("Login Took " + duration + " ms for " + username);
			//}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method checks if the player has a password existing or not. If they do not have one, we set their password
	 * to a new encrypted one.
	 *
	 * @param player
	 * 		The player
	 * @return {@code True} if we had to set their password
	 */
	private boolean checkStoredPassword(Player player) {
		if (player.getPasswordHash() == null) {
			player.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));
			return true;
		}
		return false;
	}
}
