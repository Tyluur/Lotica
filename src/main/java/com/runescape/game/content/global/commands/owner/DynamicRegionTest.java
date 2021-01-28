package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.region.RegionBuilder;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 16, 2015
 */
public class DynamicRegionTest extends CommandSkeleton<String> {

	@Override
	public String getIdentifiers() {
		return "drt";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		int[] boundChunks = RegionBuilder.findEmptyChunkBound(8, 8);
		RegionBuilder.copyAllPlanesMap(281, 584, boundChunks[0], boundChunks[1], 64);
		player.setNextWorldTile(new WorldTile(getWorldTile(boundChunks, Integer.parseInt(cmd[1]), Integer.parseInt(cmd[2]))));
	}

	/**
	 * Retrieves a new {@code WorldTile} using the boundChunks of the dynamic
	 * region.
	 * 
	 * @param mapX
	 *            The 'x' coordinate value.
	 * @param mapY
	 *            The 'y' coordinate value.
	 * @return a new {@code WorldTile}
	 */
	public WorldTile getWorldTile(int[] boundChunks, int mapX, int mapY) {
		return new WorldTile(boundChunks[0] * 8 + mapX, boundChunks[1] * 8 + mapY, 0);
	}

}
