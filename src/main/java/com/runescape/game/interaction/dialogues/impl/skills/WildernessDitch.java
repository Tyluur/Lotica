package com.runescape.game.interaction.dialogues.impl.skills;

import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.ForceMovement;
import com.runescape.game.world.entity.player.Player;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

public class WildernessDitch extends Dialogue {

	private WorldObject ditch;

	@Override
	public void start() {
		ditch = (WorldObject) parameters[0];
		player.getInterfaceManager().sendInterface(382);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (interfaceId == 382 && componentId == 19) {
			hopDitch(player, ditch);
		} else {
			player.closeInterfaces();
		}
		end();
	}

	@Override
	public void finish() {

	}

	public static void hopDitch(Player player, WorldObject ditch) {
		player.stopAll();
		player.getLockManagement().lockAll();
		player.setNextAnimation(new Animation(6132));
		final WorldTile toTile = new WorldTile(ditch.getRotation() == 3 || ditch.getRotation() == 1 ? ditch.getX() - 1 : player.getX(), ditch.getRotation() == 0 || ditch.getRotation() == 2 ? ditch.getY() + 2 : player.getY(), ditch.getPlane());
		final WorldTile fromTile = new WorldTile(player);
		player.setNextForceMovement(new ForceMovement(new WorldTile(player), 1, toTile, 2, ditch.getRotation() == 0 || ditch.getRotation() == 2 ? ForceMovement.NORTH : ForceMovement.WEST));
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				player.setNextWorldTile(toTile);
				player.setNextFaceWorldTile(fromTile);
				player.getControllerManager().startController("Wilderness");
				player.resetReceivedDamage();
				player.getLockManagement().unlockAll();
			}
		}, 1);
	}


}
