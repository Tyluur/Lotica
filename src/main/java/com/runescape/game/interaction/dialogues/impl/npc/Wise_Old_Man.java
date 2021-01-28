package com.runescape.game.interaction.dialogues.impl.npc;

import com.runescape.game.event.interaction.button.SkillSelectionInteractionEvent;
import com.runescape.game.interaction.dialogues.Dialogue;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/2/2015
 */
public class Wise_Old_Man extends Dialogue {

	@Override
	public void start() {
		int npcId = getParam(0);
		sendNPCDialogue(npcId, CALM, "Would you like to purchase a skillcape?");
	}

	@Override
	public void run(int interfaceId, int option) {
		switch(stage) {
			case -1:
				sendOptionsDialogue("Purchase a skillcape?", "Yes", "No");
				stage = 0;
				break;
			case 0:
				if (option == FIRST) {
					SkillSelectionInteractionEvent.display(player);
					player.putAttribute("skill_selection_type", "CAPES");
					sendDialogue("Select the skill in which you wish to buy a cape!");
					stage = -2;
				} else {
					end();
				}
				break;
		}
	}

	@Override
	public void finish() {

	}
}
