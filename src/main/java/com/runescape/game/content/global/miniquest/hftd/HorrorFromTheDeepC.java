package com.runescape.game.content.global.miniquest.hftd;

import com.runescape.game.content.global.miniquest.MiniquestController;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleMessage;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.player.quests.impl.HorrorFromTheDeep;
import com.runescape.game.world.region.RegionBuilder;
import com.runescape.workers.game.core.CoresManager;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since May 16, 2015
 */
public class HorrorFromTheDeepC extends MiniquestController {

	@Override
	public void start() {
		createRegion();
	}

	@Override
	public void createRegion() {
		player.getLockManagement().lockAll(3000); // locks player
		CoresManager.execute(() -> {
			try {
				boundChunks = RegionBuilder.findEmptyChunkBound(8, 8);
				RegionBuilder.copyAllPlanesMap(388, 490, boundChunks[0], boundChunks[1], 64);
				RegionBuilder.copyAllPlanesMap(385, 489, boundChunks[0], boundChunks[1], 64);
				spawnBoss();
				player.setNextWorldTile(getWorldTile(25, 21));
				player.setForceMultiArea(true);
				player.getLockManagement().unlockAll();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public WorldTile getLeaveTile() {
		return HorrorFromTheDeep.END_TILE;
	}

	@Override
	public boolean processObjectTeleport(WorldTile toTile) {
		player.getDialogueManager().startDialogue(SimpleMessage.class, "You can't do that here!");
		return false;
	}

	@Override
	public boolean processObjectClick1(WorldObject object) {
		player.getDialogueManager().startDialogue(SimpleMessage.class, "You can't do that here!");
		return false;
	}

	@Override
	public boolean processMagicTeleport(WorldTile toTile) {
		player.getDialogueManager().startDialogue(SimpleMessage.class, "You can't do that here!");
		return false;
	}

	@Override
	public boolean processItemTeleport(WorldTile toTile) {
		player.getDialogueManager().startDialogue(SimpleMessage.class, "You can't do that here!");
		return false;
	}

	/**
	 * Spawns the dagganoth mother boss
	 */
	private void spawnBoss() {
		Dagannoth_Mother mother = new Dagannoth_Mother(getWorldTile(25, 15));
		mother.setTarget(player);
		mother.getCombat().process();
		player.getHintIconsManager().addHintIcon(mother, 1, -1, false);
	}

}
