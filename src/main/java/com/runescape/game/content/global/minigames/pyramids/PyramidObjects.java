package com.runescape.game.content.global.minigames.pyramids;

import com.runescape.game.interaction.dialogues.impl.misc.SimpleMessage;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.entity.masks.ForceTalk;
import com.runescape.game.world.entity.masks.Hit;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;

/**
 * This class handles all pyramid object interactions and rewards
 *
 * @author Tyluur<itstyluur@gmail.com>
 * @since February 15, 2015
 */
public class PyramidObjects implements PyramidHunterConstants {

	public PyramidObjects(PyramidFloor pyramidFloor) {
		this.pyramidFloor = pyramidFloor;
	}

	/**
	 * This method checks the doorway by removing it for 1 1/2 seconds and then respawning it. Players are able to walk
	 * over it when it is removed.
	 *
	 * @param player
	 * 		The player
	 * @param object
	 * 		The doorway
	 */
	public boolean checkDoorway(Player player, WorldObject object) {
		if (pyramidFloor.getFacade().getFloorStage() == PyramidFloorFacade.PyramidFloorStage.PREPARING_ENTRANCE) {
			player.applyHit(new Hit(player, Utils.random(30, 120)));
			player.setNextForceTalk(new ForceTalk("I don't think I should be doing this yet..."));
			player.getDialogueManager().startDialogue(SimpleMessage.class, "Complete the cooking goal first before starting this.");
			return false;
		}
		if (Utils.percentageChance(25)) {
			player.applyHit(new Hit(player, Utils.random(30, 120)));
			player.setNextForceTalk(new ForceTalk("Dammit, I cut myself!"));
			return true;
		}
		if (object.getId() == 6553 || object.getId() == 6555) {
			World.removeObjectTemporary(object, 1500);
			return true;
		}
		return false;
	}

	/**
	 * The floor the game is on
	 */
	private PyramidFloor pyramidFloor;

}
