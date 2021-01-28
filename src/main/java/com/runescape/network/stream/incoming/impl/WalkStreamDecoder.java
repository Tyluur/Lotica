package com.runescape.network.stream.incoming.impl;

import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.player.LockManagement.LockType;
import com.runescape.game.world.entity.player.Player;
import com.runescape.network.stream.InputStream;
import com.runescape.network.stream.incoming.IncomingStreamDecoder;
import com.runescape.utility.Utils;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/27/2016
 */
public class WalkStreamDecoder extends IncomingStreamDecoder {

	@Override
	public int[] getKeys() {
		return new int[] { 12, 83 };
	}

	@Override
	public void decode(Player player, InputStream stream, int packetId, int length) {
		if (!player.hasStarted() || !player.clientHasLoadedMapRegion() || player.isDead() || player.getLockManagement().isLocked(LockType.WALKING)) {
			return;
		}
		if (player.getFreezeDelay() >= Utils.currentTimeMillis()) {
			Entity frozenBy = player.getAttribute("frozen_by");
			if (frozenBy == null || frozenBy.withinDistance(player, 16)) {
				player.getPackets().sendGameMessage("A magical force prevents you from moving.");
				return;
			} else {
				player.setFreezeDelay(0);
			}
		}
		if (packetId == 83) { length -= 13; }
		int baseX = stream.readUnsignedShortLE128();
		int baseY = stream.readUnsignedShortLE128();
		stream.readByte();
		player.stopAll();
		if (player.isResting()) { return; }
		int steps = (length - 5) / 2;
		if (steps > 25) { steps = 25; }
		player.stopAll();
		if (player.isResting()) { return; }
		for (int step = 0; step < steps; step++) {
			int destX = baseX + stream.readUnsignedByte();
			int destY = baseY + stream.readUnsignedByte();
			if (!player.addWalkSteps(destX, destY, -1, !player.isAtDynamicRegion())) {
				break;
			}
		}
	}
}
