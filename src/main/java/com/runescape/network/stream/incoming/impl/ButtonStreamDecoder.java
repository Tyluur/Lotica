package com.runescape.network.stream.incoming.impl;

import com.runescape.game.world.entity.player.Player;
import com.runescape.network.codec.decoders.handlers.ButtonHandler;
import com.runescape.network.stream.InputStream;
import com.runescape.network.stream.incoming.IncomingStreamDecoder;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/27/2016
 */
public class ButtonStreamDecoder extends IncomingStreamDecoder {

	@Override
	public int[] getKeys() {
		return new int[] { 61, 64, 4, 52, 81, 18, 10, 25, 91, 20 };
	}

	@Override
	public void decode(Player player, InputStream stream, int packetId, int length) {
		ButtonHandler.handleButtons(player, stream, packetId);
	}
}
