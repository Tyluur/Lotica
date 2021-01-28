package com.runescape.game.content.global.miniquest.ld;

import com.runescape.game.content.global.miniquest.MiniquestController;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.region.RegionBuilder;
import com.runescape.workers.game.core.CoresManager;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 17, 2015
 */
public class LunarDiplomacyC extends MiniquestController {

	@Override
	public void start() {
		createRegion();
	}

	@Override
	public void createRegion() {
		player.getLockManagement().lockAll(3000); // locks player
		CoresManager.execute(() -> {
			try {
				boundChunks = RegionBuilder.findEmptyChunkBound(20, 20);
				RegionBuilder.copyAllPlanesMap(227, 643, boundChunks[0], boundChunks[1], 60);
				RegionBuilder.copyAllPlanesMap(226, 642, boundChunks[0], boundChunks[1], 60);
				player.getLockManagement().unlockAll();
				player.setNextWorldTile(new WorldTile(getWorldTile(16, 28)));
				player.setNextAnimation(new Animation(-1));
				player.setNextGraphics(new Graphics(-1));
				spawnMe();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	protected void spawnMe() {
		LunarDiplomacyNPC npc = new LunarDiplomacyNPC(player, 4509, getWorldTile(16, 15));
		player.getHintIconsManager().addHintIcon(npc, 1, -1, false);
	}

	@Override
	public WorldTile getLeaveTile() {
		return new WorldTile(3097, 3512, 0);
	}
	
	@Override
	protected WorldTile getWorldTile(int mapX, int mapY) {
		return new WorldTile(boundChunks[0] * 8 + mapX, boundChunks[1] * 8 + mapY, 2);
	}

}