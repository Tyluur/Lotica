package com.runescape.game.content.global.miniquest;

import com.runescape.game.interaction.controllers.Controller;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.region.RegionBuilder;
import com.runescape.workers.game.core.CoresManager;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

import java.util.concurrent.TimeUnit;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 17, 2015
 */
public abstract class MiniquestController extends Controller {

	@Override
	public void magicTeleported(int type) {
		leave(false);
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
	 * Handling the removing of the dynamic region
	 */
	public void removeRegion() {
		CoresManager.schedule(() -> {
			try {
				RegionBuilder.destroyMap(boundChunks[0], boundChunks[1], 8, 8);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}, 1200, TimeUnit.MILLISECONDS);
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
	 * What's done when the player leaves
	 *
	 * @param logout
	 * 		If it is on logout
	 */
	public void leave(boolean logout) {
		if (logout) {
			player.setLocation(getLeaveTile());
		} else {
			player.setNextWorldTile(getLeaveTile());
		}
		removeRegion();
		removeController();
	}

	/**
	 * Handling the creation of the dynamic region
	 */
	public abstract void createRegion();

	/**
	 * The tile that players are teleported to when they leave the miniquest
	 */
	public abstract WorldTile getLeaveTile();

	/**
	 * Gets the world tile inside the {@link #boundChunks}
	 *
	 * @param mapX
	 * 		The x of the map
	 * @param mapY
	 * 		The y of the map
	 */
	protected WorldTile getWorldTile(int mapX, int mapY) {
		return new WorldTile(boundChunks[0] * 8 + mapX, boundChunks[1] * 8 + mapY, 0);
	}

	/**
	 * The chunks for the region
	 */
	protected int[] boundChunks;
}