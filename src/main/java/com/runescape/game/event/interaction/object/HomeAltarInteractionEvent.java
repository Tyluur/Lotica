package com.runescape.game.event.interaction.object;

import com.runescape.game.event.interaction.type.ObjectInteractionEvent;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.world.ClickOption;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 1/30/2016
 */
public class HomeAltarInteractionEvent extends ObjectInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] { 13648 };
	}

	@Override
	public boolean handleObjectInteraction(Player player, WorldObject object, ClickOption option) {
		player.getDialogueManager().startDialogue(new Dialogue() {
			@Override
			public void start() {
				sendOptionsDialogue(DEFAULT_OPTIONS, "Switch prayer book", "Switch magic book");
			}

			@Override
			public void run(int interfaceId, int option) {
				switch(stage) {
					case -1:
						switch(option) {
							case FIRST:
								player.getPrayer().setPrayerBook(!player.getPrayer().isAncientCurses());
								if (player.getPrayer().isAncientCurses()) {
									sendDialogue("The altar fills your head with dark thoughts, purging the", "prayers from your memory and leaving only curses in", "their place.");
								} else {
									sendDialogue("The altar eases its grip on your mid. The curses slip from", "your memory and you recall the prayers you used to know.");
								}
								stage = -2;
								break;
							case SECOND:
								sendOptionsDialogue(DEFAULT_OPTIONS, "Modern Spellbook", "Ancient Spellbook", "Lunar Spellbook");
								stage = 0;
								break;
						}
						break;
					case 0:
						player.getCombatDefinitions().setSpellBook(option == 2 ? 0 : option == 3 ? 1 : 2);
						end();
						break;
				}
			}

			@Override
			public void finish() {

			}
		});
		return true;
	}
}
