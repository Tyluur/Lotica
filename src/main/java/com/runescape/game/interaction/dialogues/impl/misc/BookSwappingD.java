package com.runescape.game.interaction.dialogues.impl.misc;

import com.runescape.game.interaction.dialogues.Dialogue;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 19, 2015
 */
public class BookSwappingD extends Dialogue {

	@Override
	public void start() {
		sendOptionsDialogue(DEFAULT_OPTIONS, "Switch Magic Book", "Switch Prayer Book");
	}

	@Override
	public void run(int interfaceId, int option) {
		switch(stage) {
		case -1:
			if (option == FIRST) {
				sendOptionsDialogue(DEFAULT_OPTIONS, "Modern Spellbook", "Ancient Spellbook", "Lunar Spellbook");
				stage = 0;
			} else {
				/*if (!player.getPrayer().isAncientCurses() && !player.getQuestManager().isFinished(TempleAtSenntisten.class)) {
					sendDialogue("You have to finish the Temple at Senntisten quest to use ancient curses.");
					return;
				}*/
				player.getPrayer().setPrayerBook(!player.getPrayer().isAncientCurses());
				end();
			}
			break;
		case 0:
			player.getCombatDefinitions().setSpellBook(option == FIRST ? 0 : option == SECOND ? 1 : 2);
			end();
			break;
		}
	}

	@Override
	public void finish() {
	}

}
