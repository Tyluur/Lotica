package com.runescape.game.event.interaction.npc;

import com.runescape.game.GameConstants;
import com.runescape.game.content.PlayerLook;
import com.runescape.game.event.interaction.type.NPCInteractionEvent;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;
import com.runescape.utility.world.ClickOption;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/2/2015
 */
public class MakeoverMageInteractionEvent extends NPCInteractionEvent {
	
	@Override
	public int[] getKeys() {
		return new int[] { 599 };
	}

	@Override
	public boolean handleNPCInteraction(Player player, NPC npc, ClickOption option) {
		player.getDialogueManager().startDialogue(mageDialogue, npc.getId());
		return true;
	}

	/**
	 * The type of apperances we can change
	 */
	public enum AppearanceTypes {
		GENDER,
		CLOTHING,
		FACIAL,
		SHOES
	}

	/**
	 * The dialogue insetance
	 */
	private final Dialogue mageDialogue = new Dialogue() {
		int npcId;

		int cost = 0;

		AppearanceTypes type;

		@Override
		public void start() {
			npcId = getParam(0);
			sendNPCDialogue(npcId, HAPPY, "Hello adventurer, I am the makeover mage here at " + GameConstants.SERVER_NAME, "and I can change any part of your appearance for you", "for a small cost. Now what would you like today?");
			stage = -1;
		}

		@Override
		public void run(int interfaceId, int option) {
			switch (stage) {
				case -1:
					sendOptionsDialogue(DEFAULT_OPTIONS, "Change Gender/Skin", "Change Clothing", "Change Hair/Beard", "Change Shoes");
					stage = 0;
					break;
				case 0:
					if (player.getEquipment().wearingArmour()) {
						sendNPCDialogue(npcId, CALM, "You must take off your armour first.");
						stage = -2;
						return;
					}
					switch (option) {
						case FIRST:
							cost = 50_000;
							type = AppearanceTypes.GENDER;
							break;
						case SECOND:
							cost = 100_000;
							type = AppearanceTypes.CLOTHING;
							break;
						case THIRD:
							cost = 50_000;
							type = AppearanceTypes.FACIAL;
							break;
						case FOURTH:
							cost = 25_000;
							type = AppearanceTypes.SHOES;
							break;
					}
					sendNPCDialogue(npcId, CALM, "This will cost you " + Utils.format(cost) + " coins, are you sure?");
					stage = 1;
					break;
				case 1:
					sendOptionsDialogue("Pay " + Utils.format(cost) + " coins?", "Yes", "No");
					stage = 2;
					break;
				case 2:
					if (option == FIRST) {
						if (player.takeMoney(cost)) {
							end();
							switch (type) {
								case GENDER:
									PlayerLook.openGenderSelection(player);
									break;
								case CLOTHING:
									PlayerLook.openClothingSelection(player);
									break;
								case FACIAL:
									PlayerLook.openHairSelection(player);
									break;
								case SHOES:
									PlayerLook.openShoeSelection(player);
									break;
							}
						} else {
							sendPlayerDialogue(SAD, "I don't have that much money...");
							stage = -2;
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
	};
}
