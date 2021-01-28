package com.runescape.network.stream.incoming.impl;

import com.runescape.game.world.entity.player.Player;
import com.runescape.network.stream.InputStream;
import com.runescape.network.stream.incoming.IncomingStreamDecoder;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/27/2016
 */
public class FrameInteractionStreamDecoder extends IncomingStreamDecoder {

	private final static int WINDOW_SWITCH_PACKET = 93;

	@Override
	public int[] getKeys() {
		return new int[] { 84, 29, 68, 75, 93 };
	}

	@Override
	public void decode(Player player, InputStream stream, int packetId, int length) {
		// click = 84, move mouse = 29, type = 68
		if (packetId == WINDOW_SWITCH_PACKET) {
			int active = stream.readByte();
			player.getInterfaceManager().setClientActive(active == 1);
		}
	}
}
