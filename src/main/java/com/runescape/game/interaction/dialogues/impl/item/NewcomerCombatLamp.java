package com.runescape.game.interaction.dialogues.impl.item;

import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.utility.Utils;

import static com.runescape.game.world.entity.player.Skills.*;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 8/23/2016
 */
public class NewcomerCombatLamp extends Dialogue {
	
	/**
	 * The combat experience received
	 */
	private static final int COMBAT_EXP_RECEIVED = 740_000;
	
	/**
	 * The formatted amount of combat exp received
	 */
	private static final String FORMATTED_COMBAT_EXP_RECEIVED = Utils.numberToCashDigit(COMBAT_EXP_RECEIVED);
	
	private int skillSelected;
	
	@Override
	public void start() {
		sendOptionsDialogue("Select a skill to receive " + FORMATTED_COMBAT_EXP_RECEIVED + " exp<br>(This is enough for level " + getLevelByExperience(COMBAT_EXP_RECEIVED, ATTACK) + ")", "Attack", "Strength", "Defence", "Ranged", "Magic");
	}
	
	@Override
	public void run(int interfaceId, int option) {
		switch (stage) {
			case -1:
				skillSelected = option == FIRST ? ATTACK : option == SECOND ? STRENGTH : option == THIRD ? DEFENCE : option == FOURTH ? RANGE : MAGIC;
				sendDialogue("Please confirm you wish to receive " + FORMATTED_COMBAT_EXP_RECEIVED + " experience", "in " + SKILL_NAME[skillSelected].toLowerCase() + ".");
				stage = 0;
				break;
			case 0:
				sendOptionsDialogue("Add " + FORMATTED_COMBAT_EXP_RECEIVED + " " + SKILL_NAME[skillSelected].toLowerCase() + " experience?", "Yes", "No");
				stage = 1;
				break;
			case 1:
				if (option == FIRST) {
					if (player.getInventory().containsItem(11137, 1)) {
						player.getInventory().deleteItem(11137, 1);
						player.getSkills().addXpNoModifier(skillSelected, COMBAT_EXP_RECEIVED);
						sendItemDialogue(11137, 1, "You receive " + FORMATTED_COMBAT_EXP_RECEIVED + " " + SKILL_NAME[skillSelected].toLowerCase() + " experience!");
					}
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
