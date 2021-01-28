package com.runescape.game.content.global.miniquest.rfd;

import com.runescape.game.interaction.controllers.Controller;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.player.quests.impl.RecipeForDisaster;
import com.runescape.game.world.region.RegionBuilder;
import com.runescape.network.codec.decoders.WorldPacketsDecoder;
import com.runescape.workers.game.core.CoresManager;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

import java.util.concurrent.TimeUnit;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 25, 2015
 */
public class RecipeForDisasterController extends Controller {

	/**
	 * The tile the player is set to when they leave
	 */
	private static final WorldTile LEAVE_TILE = new WorldTile(3094, 3512, 0);

	/**
	 * The ids of the bosses we can fight
	 */
	private static final int[] BOSS_IDS = new int[] { 3493, 3494, 3495, 3496, 3491 };

	/**
	 * The current boss we are to kill
	 */
	private RecipeForDisasterBoss boss;

	/**
	 * The chunks for the region
	 */
	private int[] boundChunks;

	@Override
	public void start() {
		createRegionAndBoss();
	}

	@Override
	public void magicTeleported(int type) {
		leave(false);
	}

	@Override
	public boolean processButtonClick(int interfaceId, int componentId, int slotId, int packetId) {
		if (interfaceId == 271 && packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET || (interfaceId == 749 && componentId == 4)) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You are not allowed to use prayers in this quest.");
			return false;
		}
		return true;
	}

	@Override
	public boolean sendDeath() {
		player.getLockManagement().lockAll(7000);
		player.stopAll();
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					player.setNextAnimation(new Animation(836));
				} else if (loop == 1) {
					player.getPackets().sendGameMessage("You have been defeated!");
				} else if (loop == 3) {
					player.reset();
					forceClose();
					player.setNextAnimation(new Animation(-1));
				} else if (loop == 4) {
					player.getPackets().sendMusicEffect(90);
					stop();
				}
				loop++;
			}
		}, 0, 1);
		return false;
	}

	@Override
	public boolean login() {
		leave(true);
		return true;
	}

	@Override
	public boolean logout() {
		leave(true);
		return true;
	}

	@Override
	public void forceClose() {
		leave(false);
	}

	/**
	 * Creates the region for fighting and starts the fight.
	 */
	private void createRegionAndBoss() {
		player.getLockManagement().lockAll(3000); // locks player
		CoresManager.execute(() -> {
			boundChunks = RegionBuilder.findEmptyChunkBound(8, 8);
			RegionBuilder.copyAllPlanesMap(237, 669, boundChunks[0], boundChunks[1], 64);
			calculateNextBoss();
			player.setNextWorldTile(getWorldTile(7, 14));
			player.getLockManagement().unlockAll();
		});
	}

	/**
	 * Calculates the next boss we should fight
	 */
	public void calculateNextBoss() {
		try {
			Double killedBosses = player.getQuestManager().getAttribute(RecipeForDisaster.class, RecipeForDisaster.KILLED_KEY, 0D);
			if (killedBosses == 5) {
				player.getQuestManager().finishQuest(RecipeForDisaster.class);
				leave(false);
			} else {
				boss = new RecipeForDisasterBoss(BOSS_IDS[killedBosses.intValue()], getWorldTile(7, 7), this);
				player.getHintIconsManager().addHintIcon(boss, 1, -1, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the world tile inside the dynamic region
	 *
	 * @param mapX
	 * 		The x in the map
	 * @param mapY
	 * 		The y in the map
	 */
	public WorldTile getWorldTile(int mapX, int mapY) {
		return new WorldTile(boundChunks[0] * 8 + mapX, boundChunks[1] * 8 + mapY, 2);
	}

	/**
	 * Handles what to do when the player leaves the game.
	 *
	 * @param logout
	 * 		If the player left by logout
	 */
	private void leave(boolean logout) {
		if (logout) {
			player.setLocation(LEAVE_TILE);
		} else {
			player.setNextWorldTile(LEAVE_TILE);
		}
		removeRegion();
		removeController();
	}

	/**
	 * Destroys the region from the memory
	 */
	private void removeRegion() {
		CoresManager.schedule(() -> RegionBuilder.destroyMap(boundChunks[0], boundChunks[1], 8, 8), 1200, TimeUnit.MILLISECONDS);
	}

}
