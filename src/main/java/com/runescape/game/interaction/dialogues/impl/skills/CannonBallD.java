package com.runescape.game.interaction.dialogues.impl.skills;

import com.runescape.game.content.skills.SkillsDialogue;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.world.entity.player.actions.CannonBallCreation;

/**
 *
 * @author Tyluur <itstyluur@gmail.com>
 * @since Jan 15, 2014
 */
public class CannonBallD extends Dialogue {

	@Override
	public void start() {
		SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.SELECT, "How many bars would you like to use?", player.getInventory().getItems().getNumberOf(2353), new int[] { 2 }, null, true);
	}

	@Override
	public void run(int interfaceId, int option) {
		end();
		player.getActionManager().setAction(new CannonBallCreation(SkillsDialogue.getQuantity(player)));

	}

	@Override
	public void finish() {

	}

}
