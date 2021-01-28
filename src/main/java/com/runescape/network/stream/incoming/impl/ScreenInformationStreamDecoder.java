package com.runescape.network.stream.incoming.impl;

import com.runescape.game.world.entity.player.Player;
import com.runescape.network.stream.InputStream;
import com.runescape.network.stream.incoming.IncomingStreamDecoder;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/27/2016
 */
public class ScreenInformationStreamDecoder extends IncomingStreamDecoder {

	@Override
	public int[] getKeys() {
		return new int[] { 87 };
	}

	@Override
	public void decode(Player player, InputStream stream, int packetId, int length) {
		int displayMode = stream.readUnsignedByte();
		player.setScreenWidth(stream.readUnsignedShort());
		player.setScreenHeight(stream.readUnsignedShort());

		stream.readUnsignedByte();
		if (!player.hasStarted() || player.hasFinished() || displayMode == player.getDisplayMode() || !player.getInterfaceManager().containsInterface(742)) {
			return;
		}
		player.setDisplayMode(displayMode);
		player.getInterfaceManager().removeAll();
		player.getInterfaceManager().sendInterfaces();
		player.getInterfaceManager().sendInterface(742);
	}
}
