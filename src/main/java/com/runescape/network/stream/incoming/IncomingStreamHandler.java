package com.runescape.network.stream.incoming;

import com.runescape.game.GameConstants;
import com.runescape.game.world.entity.player.Player;
import com.runescape.network.stream.InputStream;
import com.runescape.utility.Utils;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/27/2016
 */
public final class IncomingStreamHandler {

	/**
	 * The map of streams
	 */
	private static final ConcurrentHashMap<Integer, IncomingStreamDecoder> STREAM_DECODER_MAP = new ConcurrentHashMap<>();

	/**
	 * Loads all incoming streams
	 */
	public static void loadAll() {
		Utils.getClassesInDirectory(IncomingStreamHandler.class.getPackage().getName() + ".impl").forEach((clazz) -> {
			if (!(clazz instanceof IncomingStreamDecoder)) {
				throw new IllegalStateException(clazz + " did not belong in this package");
			}
			IncomingStreamDecoder decoder = (IncomingStreamDecoder) clazz;
			Arrays.stream(decoder.getKeys()).forEach(key -> STREAM_DECODER_MAP.put(key, decoder));
		});
		System.out.println("Loaded " + STREAM_DECODER_MAP.size() + " decoded packets.");
	}

	/**
	 * Decodes an incoming stream
	 */
	public static void decodeStream(Player player, InputStream stream, int packetId, int length) {
		IncomingStreamDecoder decoder = STREAM_DECODER_MAP.get(packetId);
		if (decoder == null) {
			if (GameConstants.DEBUG) {
				System.out.println("No packet found for id " + packetId);
			}
			return;
		}
		decoder.decode(player, stream, packetId, length);
		if (isActiveStream(decoder)) {
			player.putAttribute("last_logic_packet_time", System.currentTimeMillis());
			player.getLoyaltyManager().incrementActivityPoints();
		}
//		System.out.println(decoder.getClass().getSimpleName() + " decoder " + packetId);
	}

	/**
	 * Checks if the decoder should increase the player's playtime
	 *
	 * @param decoder
	 * 		The decoder
	 */
	private static boolean isActiveStream(IncomingStreamDecoder decoder) {
		switch (decoder.getClass().getSimpleName()) {
			case "ChatStreamDecoder":
			case "ButtonStreamDecoder":
			case "InputEventStreamDecoder":
			case "WalkStreamDecoder":
			case "PlayerInteractionStreamDecoder":
			case "NPCInteractionStreamDecoder":
			case "ObjectInteractionStreamDecoder":
				return true;
			default:
				return false;
		}
	}

	/**
	 * Checks if a stream should be stopped by the security management system
	 *
	 * @param opcode
	 * 		The packet id
	 */
	public static boolean isSecurityStream(int opcode) {
		IncomingStreamDecoder decoder = STREAM_DECODER_MAP.get(opcode);
		if (decoder == null) { return false; }
		switch (decoder.getClass().getSimpleName()) {
			case "ChatStreamDecoder":
			case "ButtonStreamDecoder":
			case "CommandStreamDecoder":
			case "WalkStreamDecoder":
			case "FriendChatStreamDecoder":
			case "ItemInteractionStreamDecoder":
			case "InterfaceStreamDecoder":
			case "PlayerInteractionStreamDecoder":
			case "NPCInteractionStreamDecoder":
			case "ObjectInteractionStreamDecoder":
				return true;

			default:
				return false;
		}
	}
}
