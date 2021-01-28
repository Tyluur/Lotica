package com.runescape.network.stream.incoming;

import com.runescape.game.world.entity.player.Player;
import com.runescape.network.stream.InputStream;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/27/2016
 */
public abstract class IncomingStreamDecoder {

	/**
	 * Gets the keys that identify this incoming stream
	 */
	public abstract int[] getKeys();

	/**
	 * Decodes an incoming stream
	 *
	 * @param player
	 * 		The player
	 * @param stream
	 * 		The stream
	 * @param length
	 * 		The length of the stream
	 * @param packetId
	 * 		The id of the incoming packet
	 */
	public abstract void decode(Player player, InputStream stream, int packetId, int length);
}
