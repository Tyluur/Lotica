package com.runescape.game.content.global.miniquest.mm;

import com.runescape.game.interaction.controllers.Controller;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleNPCMessage;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.region.RegionBuilder;
import com.runescape.workers.game.core.CoresManager;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

import java.util.concurrent.TimeUnit;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 7, 2015
 */
public class MonkeyMadnessController extends Controller {

	@Override
	public void start() {
		createRegionAndBoss();
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

	@Override
	public void magicTeleported(int type) {
		leave(false);
	}

	/**
	 * Creates the region for fighting and starts the fight.
	 */
	private void createRegionAndBoss() {
		player.getLockManagement().lockAll(3000); // locks player
		CoresManager.execute(() -> {
			try {
				boundChunks = RegionBuilder.findEmptyChunkBound(8, 8);
				RegionBuilder.copyAllPlanesMap(331, 571, boundChunks[0], boundChunks[1], 64);
				RegionBuilder.copyAllPlanesMap(330, 567, boundChunks[0], boundChunks[1], 64);
				RegionBuilder.copyAllPlanesMap(329, 565, boundChunks[0], boundChunks[1], 64);
				spawnBoss();
				player.setNextWorldTile(getWorldTile(12, 60));
				player.setForceMultiArea(true);
				player.getLockManagement().unlockAll();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	/**
	 * Handles what to do when the player leaves the game.
	 *
	 * @param logout
	 * 		If the player left by logout
	 */
	public void leave(boolean logout) {
		if (logout) {
			player.setLocation(LEAVE_TILE);
		} else {
			player.setNextWorldTile(LEAVE_TILE);
		}
		removeRegion();
		removeController();
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

	/**
	 * Spawns the boss and starts the fight officially
	 */
	protected void spawnBoss() {
		MonkeyMadnessBoss boss = new MonkeyMadnessBoss(this, getWorldTile(25, 60));
		player.getDialogueManager().startDialogue(SimpleNPCMessage.class, 1411, "I wish you luck, speak to me after the fight if all goes well.");
		player.getHintIconsManager().addHintIcon(boss, 1, -1, true);
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
		return new WorldTile(boundChunks[0] * 8 + mapX, boundChunks[1] * 8 + mapY, 1);
	}

	/**
	 * Destroys the region from the memory
	 */
	private void removeRegion() {
		CoresManager.schedule(() -> {
			try {
				RegionBuilder.destroyMap(boundChunks[0], boundChunks[1], 8, 8);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}, 1200, TimeUnit.MILLISECONDS);
	}

	/**
	 * The chunks for the region
	 */
	private int[] boundChunks;

	/**
	 * The tile the player is moved to when they leave
	 */
	private static final WorldTile LEAVE_TILE = new WorldTile(3098, 3512, 0);
}
