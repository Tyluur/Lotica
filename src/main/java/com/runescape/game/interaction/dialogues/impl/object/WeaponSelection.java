package com.runescape.game.interaction.dialogues.impl.object;

import com.runescape.game.content.skills.construction.HouseConstants;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.world.WorldObject;

/**
 * 
 * @author Jonathan
 * 
 */
public class WeaponSelection extends Dialogue {

	private WorldObject object;

	public static int BLUE_BOXING_GLOVES = 7673, RED_BOXING_GLOVES = 7671;
	public static int WOODEN_SWORD = 7675, WOODEN_SHIELD = 7676;
	public static int PUGEL_STICK = 7679;
	public static final int[] items = new int[] { RED_BOXING_GLOVES, BLUE_BOXING_GLOVES, WOODEN_SWORD, WOODEN_SHIELD, PUGEL_STICK };

	@Override
	public void start() {
		object = (WorldObject) parameters[0];
		if (HouseConstants.HObject.GLOVE_RACK.getId() == object.getId()) {
			sendOptionsDialogue("What do you want to take?", "Red boxing gloves", "Blue boxing gloves");
		} else if (HouseConstants.HObject.WEAPON_RACK.getId() == object.getId()) {
			sendOptionsDialogue("What do you want to take?", "Red boxing gloves", "Blue boxing gloves", "Wooden sword", "Wooden shield");
		} else if (HouseConstants.HObject.EXTRA_WEAPON_RACK.getId() == object.getId()) {
			sendOptionsDialogue("What do you want to take?", "Red boxing gloves", "Blue boxing gloves", "Wooden sword", "Wooden shield", "Pugel stick");
		}
	}

	@Override
	public void run(int interfaceId, int option) {
		switch (stage) {
		case -1:
			if (option > items.length) {
				end();
				return;
			}
			player.getInventory().addItemDrop(items[option - 1], 1);
			end();
			break;
		}
	}

	@Override
	public void finish() {
	}

}