package com.runescape.game.interaction.controllers.impl;

import com.runescape.game.GameConstants;
import com.runescape.game.interaction.controllers.Controller;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.utility.Utils;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/6/2015
 */
public class JailController extends Controller {

	/** The array of possible jail coordinates */
	private static final WorldTile[] JAIL_COORDS = new WorldTile[] { new WorldTile(3013, 3193, 0),
			                                                               new WorldTile(3013, 3195, 0),
			                                                               new WorldTile(3013, 3189, 0),
			                                                               new WorldTile(3018, 3188, 0),
			                                                               new WorldTile(3018, 3181, 0),
			                                                               new WorldTile(3014, 3181, 0), };
	
	@Override
	public void start() {
		sendToRandomJail();
		player.sendMessage("You have been jailed.");
	}

	@Override
	public void process() {
		if (player.getFacade().getJailedUntil() <= System.currentTimeMillis()) {
			player.getControllerManager().getController().removeController();
			player.getPackets().sendGameMessage("Your account has been unjailed.", true);
			player.setNextWorldTile(GameConstants.RESPAWN_PLAYER_LOCATION);
		}
	}

	@Override
	public boolean login() {
		return false;
	}

	@Override
	public boolean logout() {
		return false;
	}

	@Override
	public boolean processMagicTeleport(WorldTile toTile) {
		return false;
	}

	@Override
	public boolean processItemTeleport(WorldTile toTile) {
		return false;
	}

	@Override
	public boolean processObjectClick1(WorldObject object) {
		return false;
	}

	@Override
	public boolean sendDeath() {
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				player.stopAll();
				if (loop == 0) {
					player.setNextAnimation(new Animation(836));
				} else if (loop == 1) {
					player.getPackets().sendGameMessage("Oh dear, you have died.");
				} else if (loop == 3) {
					player.setNextAnimation(new Animation(-1));
					player.reset();
					player.setCanPvp(false);
					sendToRandomJail();
					player.getLockManagement().unlockAll();
				}
				loop++;
			}
		}, 0, 1);
		return false;
	}

	public void sendToRandomJail() {
		WorldTile jail = Utils.randomArraySlot(JAIL_COORDS);
		player.setNextWorldTile(jail);
	}


}
