package com.runescape.network.stream.incoming.impl;

import com.runescape.game.world.entity.player.Player;
import com.runescape.network.stream.InputStream;
import com.runescape.network.stream.incoming.IncomingStreamDecoder;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/27/2016
 */
public class CloseInterfaceStreamDecoder extends IncomingStreamDecoder {

	@Override
	public int[] getKeys() {
		return new int[] { 56 };
	}

	@Override
	public void decode(Player player, InputStream stream, int packetId, int length) {
		if (!player.isRunning()) {
			player.run();
			return;
		}
		player.stopAll();
	}
}
