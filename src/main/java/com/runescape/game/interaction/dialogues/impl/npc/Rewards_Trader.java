package com.runescape.game.interaction.dialogues.impl.npc;

import com.runescape.game.event.interaction.button.LoyaltyInterfaceInteractionEvent;
import com.runescape.game.interaction.dialogues.Dialogue;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since Apr 30, 2015
 */
public class Rewards_Trader extends Dialogue {

	@Override
	public void start() {
		sendOptionsDialogue(DEFAULT_OPTIONS, "Gold Points Store", "Vote Points Store", "Loyalty Rewards", "Cancel");
	}

	@Override
	public void run(int interfaceId, int option) {
		switch (stage) {
			case -1:
				switch (option) {
					case FIRST:
						sendOptionsDialogue(DEFAULT_OPTIONS, "Featured", "Armory", "Weapons", "Miscellaneous");
						stage = 0;
						break;
					case SECOND:
						end();
						openStore("Vote Store");
						break;
					case THIRD:
						end();
						LoyaltyInterfaceInteractionEvent.displayInterface(player);
						break;
					case FOURTH:
						end();
						break;
				}
				break;
			case 0:
				String shopName = null;
				switch (option) {
					case FIRST:
						shopName = "Gold - Featured";
						break;
					case SECOND:
						shopName = "Gold - Armory";
						break;
					case THIRD:
						shopName = "Gold - Weaponry";
						break;
					case FOURTH:
						sendOptionsDialogue(DEFAULT_OPTIONS, "Miscellaneous 1", "Miscellaneous 2");
						stage = 1;
						return;
				}
				if (shopName == null) {
					end();
					return;
				}
				String setName = shopName;
				end();
				openStore(setName);
				break;
			case 1:
				shopName = null;
				switch(option) {
					case FIRST:
						shopName = "Gold - Miscellaneous";
						break;
					case SECOND:
						shopName = "Gold - Miscellaneous 2";
						break;
				}
				if (shopName == null) {
					end();
					return;
				}
				setName = shopName;
				end();
				openStore(setName);
				break;
		}
	}

	@Override
	public void finish() {
	}

}
