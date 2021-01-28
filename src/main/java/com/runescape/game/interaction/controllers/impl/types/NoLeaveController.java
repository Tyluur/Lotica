package com.runescape.game.interaction.controllers.impl.types;

import com.runescape.game.interaction.controllers.Controller;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.player.Player;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 1/8/2016
 */
public abstract class NoLeaveController extends Controller {

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
					player.getPackets().sendGameMessage("Oh dear, you are dead.");
				} else if (loop == 3) {
					player.reset();
					player.setNextAnimation(new Animation(-1));
					onDeath(player);
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
	public boolean processItemTeleport(WorldTile toTile) {
		player.sendMessage(getLeaveAttemptMessage());
		return false;
	}

	@Override
	public boolean processMagicTeleport(WorldTile toTile) {
		player.sendMessage(getLeaveAttemptMessage());
		return false;
	}

	@Override
	public boolean processObjectTeleport(WorldTile toTile) {
		player.sendMessage(getLeaveAttemptMessage());
		return false;
	}

	/**
	 * The message players see when they attempt to leave
	 *
	 * @return A {@code String} {@code Object}
	 */
	public abstract String getLeaveAttemptMessage();

	/**
	 * What happens when the player has died
	 *
	 * @param player
	 * 		The player who has died
	 */
	public abstract void onDeath(Player player);
}
