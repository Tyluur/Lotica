package com.runescape.network.stream.incoming.impl;

import com.runescape.game.world.entity.player.Player;
import com.runescape.network.codec.decoders.handlers.ObjectHandler;
import com.runescape.network.stream.InputStream;
import com.runescape.network.stream.incoming.IncomingStreamDecoder;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/27/2016
 */
public class ObjectInteractionStreamDecoder extends IncomingStreamDecoder {

	private final static int OBJECT_CLICK1_PACKET = 11;

	private final static int OBJECT_CLICK2_PACKET = 2;

	private final static int OBJECT_CLICK3_PACKET = 76;

	private final static int OBJECT_CLICK5_PACKET = 69;

	private final static int OBJECT_EXAMINE_PACKET = 47;

	@Override
	public int[] getKeys() {
		return new int[] { 11, 2, 76, 69, 47 };
	}

	@Override
	public void decode(Player player, InputStream stream, int packetId, int length) {
		switch (packetId) {
			case OBJECT_CLICK1_PACKET:
				ObjectHandler.handleObjectInteraction(player, 1, stream);
				break;
			case OBJECT_CLICK2_PACKET:
				ObjectHandler.handleObjectInteraction(player, 2, stream);
				break;
			case OBJECT_CLICK3_PACKET:
				ObjectHandler.handleObjectInteraction(player, 3, stream);
				break;
			case OBJECT_EXAMINE_PACKET:
				ObjectHandler.handleObjectInteraction(player, 4, stream);
				break;
			case OBJECT_CLICK5_PACKET:
				ObjectHandler.handleObjectInteraction(player, 5, stream);
				break;
		}
	}
}
