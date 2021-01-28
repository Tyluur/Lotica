package com.runescape.game.content.skills.agility;

import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.ForceMovement;
import com.runescape.game.world.entity.player.Player;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/31/2016
 */
public class Shortcuts {

	public static void handleEdgevilleUnderwallTunnel(Player player, WorldObject object) {
		final boolean toGe = object.getId() == 9311;
		player.getLockManagement().lockAll();
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				switch(ticksPassed) {
					case 1:
						player.setNextAnimation(new Animation(2589));
						player.setNextForceMovement(new ForceMovement(toGe ? new WorldTile(3139, 3516, 0) : new WorldTile(3143, 3514, 0), 1, toGe ? ForceMovement.EAST : ForceMovement.WEST));
						break;
					case 2:
						player.setNextAnimation(new Animation(2590));
						player.setNextWorldTile(new WorldTile(3141, 3515, 0));
						break;
					case 3:
						player.setNextAnimation(new Animation(2591));
						player.setNextWorldTile(toGe ? new WorldTile(3143, 3514, 0) : new WorldTile(3139, 3516, 0));
						player.setNextForceMovement(new ForceMovement(toGe ? new WorldTile(3144, 3514, 0) : new WorldTile(3138, 3516, 0), 1, toGe ? ForceMovement.EAST : ForceMovement.WEST));
						break;
					case 4:
						player.setNextWorldTile(toGe ? new WorldTile(3144, 3514, 0) : new WorldTile(3138, 3516, 0));
						player.getLockManagement().unlockAll();
						stop();
						break;
				}
			}
		}, 1, 1);
	}

}
