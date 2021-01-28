package com.runescape.game.content.global.miniquest.dt;

import com.runescape.game.interaction.controllers.Controller;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleNPCMessage;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.player.quests.impl.DesertTreasure;
import com.runescape.utility.Utils;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 7, 2015
 */
public class DesertTreasureController extends Controller {

	private DesertTreasureNPC npc;
	private WorldTile startTile;

	@Override
	public void start() {
		int npcId = (Integer) getArguments()[0];
		WorldTile[] targetTiles = null;
		switch (npcId) {
		case 1914:
			targetTiles = SPAWN_TILES[0];
			break;
		case 1913:
			targetTiles = SPAWN_TILES[1];
			break;
		case 1977:
			targetTiles = SPAWN_TILES[2];
			break;
		case 1974:
			targetTiles = SPAWN_TILES[3];
			break;
		}
		if (targetTiles == null) {
			throw new IllegalStateException();
		}
		if (DesertTreasure.killedBoss(player, npcId)) {
			player.getDialogueManager().startDialogue(SimpleNPCMessage.class, npcId, "You have already defeated me! What more do you want???", "Fight somebody that you haven't killed!");
			forceClose();
		} else {
			npc = new DesertTreasureNPC(player, npcId, targetTiles[0]);
			player.setNextWorldTile(startTile = targetTiles[1]);
			player.getHintIconsManager().addHintIcon(npc, 1, -1, false);
		}
	}
	
	@Override
	public boolean checkWalkStep(int lastX, int lastY, int nextX, int nextY) {
		if (startTile != null && Utils.getDistance(startTile, player) >= 30) {
			forceClose();
		}
		return true;
	}

	@Override
	public void forceClose() {
		removeController();
		if (npc != null) {
			npc.finish(); // target also calls removing hint icon at remove
		}
	}

	@Override
	public void magicTeleported(int type) {
		player.setNextWorldTile(DesertTreasure.FINISH_TILE);
	}

	/**
	 * A 2 dimensional array of the tiles that will be used when teleporting to
	 * the boss. The first index is the tile the boss is spawned at, the second
	 * index is the tile the player is teleported to.
	 */
	private static final WorldTile[][] SPAWN_TILES = new WorldTile[][] {
			// dessous
			new WorldTile[] { new WorldTile(3570, 3404, 0), new WorldTile(3570, 3411, 0) },
			// kamil
			new WorldTile[] { new WorldTile(2831, 3809, 2), new WorldTile(2824, 3809, 2) },
			// fareed
			new WorldTile[] { new WorldTile(3316, 9377, 0), new WorldTile(3305, 9376, 0) },
			// damis
			new WorldTile[] { new WorldTile(2740, 5091, 0), new WorldTile(2739, 5105, 0) },
	};

}
