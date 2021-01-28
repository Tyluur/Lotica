package com.runescape.network.stream.incoming.impl;

import com.runescape.game.world.entity.player.Player;
import com.runescape.network.stream.InputStream;
import com.runescape.network.stream.incoming.IncomingStreamDecoder;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/27/2016
 */
public class WorldMapStreamDecoder extends IncomingStreamDecoder {

	@Override
	public int[] getKeys() {
		return new int[] { 89 };
	}

	@Override
	public void decode(Player player, InputStream stream, int packetId, int length) {
		int coordinateHash = stream.readInt();
		int x = coordinateHash >> 14;
		int y = coordinateHash & 0x3fff;
		int plane = coordinateHash >> 28;
		Integer hash = player.getAttribute("worldHash");
		if (hash == null || coordinateHash != hash) {
			player.putAttribute("worldHash", coordinateHash);
		} else {
			player.removeAttribute("worldHash");
			player.getHintIconsManager().addHintIcon(x, y, plane, 20, 0, 2, -1, true);
			player.getVarsManager().sendVar(1159, coordinateHash);
		}
	}
}
