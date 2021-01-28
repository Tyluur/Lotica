package com.runescape.game.interaction.dialogues.impl.skills;

import com.runescape.game.content.skills.SkillsDialogue;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.entity.player.actions.ConstructionAltarAction;

/**
 *
 * @author Tyluur <itstyluur@gmail.com>
 * @since Jan 15, 2014
 */
public class AltarBoneD extends Dialogue {
	
	int bone;
	WorldObject object;

	@Override
	public void start() {
		bone = (int) parameters[0];
		object = (WorldObject) parameters[1];
		SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.SELECT, "How many bones would you like to use?", player.getInventory().getItems().getNumberOf(bone), new int[] { bone }, null);
	}

	@Override
	public void run(int interfaceId, int option) {
		end();
		player.getActionManager().setAction(new ConstructionAltarAction(object, bone, SkillsDialogue.getQuantity(player)));
	}

	@Override
	public void finish() {

	}

}
