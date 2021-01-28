package com.runescape.network.codec.decoders;

import com.runescape.game.GameConstants;
import com.runescape.game.content.bot.BotInitializer;
import com.runescape.game.world.entity.player.Player;
import com.runescape.network.Session;
import com.runescape.network.codec.Decoder;
import com.runescape.network.stream.InputStream;
import com.runescape.utility.cache.IsaacKeyPair;

public final class ClientPacketsDecoder extends Decoder {

	public ClientPacketsDecoder(Session connection) {
		super(connection);
	}

	@Override
	public int decode(InputStream stream) {
		session.setDecoder(-1);
		int packetId = stream.readUnsignedByte();
		switch (packetId) {
			case 14:
				decodeLogin(stream);
				break;
			case 15:
				decodeGrab(stream);
				break;
			case 36:
				decodeBot(stream);
				break;
			default:
				if (GameConstants.DEBUG) { System.out.println("PacketId " + packetId); }
				session.getChannel().close();
				break;
		}
		return stream.getOffset();
	}

	private void decodeBot(InputStream stream) {
		session.setEncoder(1);
		Player bot = new Player().constructPlayer();
		String username = stream.readString();
		String identifier = stream.readString();
		int[] isaacKeys = new int[4];
		for (int i = 0; i < isaacKeys.length; i++) {
			isaacKeys[i] = stream.readInt();
		}

		bot.init(session, username, "", "BOT MAC ADDRESS", 0, 0, 0, new IsaacKeyPair(isaacKeys));
		bot.putAttribute("fake_bot_identifier", identifier);
		session.getLoginPackets().sendLoginDetails(bot);
		session.setDecoder(3, bot);
		session.setEncoder(2, bot);
		bot.getControllerManager().forceSetLastController("");
		bot.start();

		BotInitializer.onLogin(bot);
	}

	private void decodeGrab(InputStream stream) {
		int remaining = stream.getRemaining();
		if (remaining != 8) {
			session.getChannel().close();
			System.err.println("Remaining was " + remaining);
			return;
		}
		session.setEncoder(0);
		int build = stream.readInt();
		int sub = stream.readInt();
		if (build != GameConstants.CLIENT_BUILD || sub != GameConstants.CUSTOM_CLIENT_BUILD) {
			session.setDecoder(-1);
			session.getGrabPackets().sendOutdatedClientPacket();
			System.err.println("Received [build=" + build + ", sub=" + sub + "] - closed connection.");
			return;
		}
		session.setDecoder(1);
		session.getGrabPackets().sendStartUpPacket();
	}

	private void decodeLogin(InputStream stream) {
		if (stream.getRemaining() != 0) {
			session.getChannel().close();
			return;
		}
		session.setDecoder(2);
		session.setEncoder(1);
		session.getLoginPackets().sendStartUpPacket();
	}
}
