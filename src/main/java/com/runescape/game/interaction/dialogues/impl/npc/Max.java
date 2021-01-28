package com.runescape.game.interaction.dialogues.impl.npc;

import com.runescape.game.event.interaction.button.SkillSelectionInteractionEvent;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.utility.Utils;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/13/2015
 */
public class Max extends Dialogue {

	/** The cost of max capes */
	public static final int MAX_CAPE_COST = 10_000_000;
	
	/** The cost of completionist capes */
	public static final int COMPLETIONIST_CAPE_COST = 25_000_000;

	/**
	 * The cost for a master cape
	 */
	public static final int MASTER_CAPE_COST = 15_000_000;

	int npcId;

	@Override
	public void start() {
		sendNPCDialogue(npcId = getParam(0), CALM, "Hello, I am max - the toughest guy around.", "What can I help you with?");
	}

	@Override
	public void run(int interfaceId, int option) {
		switch (stage) {
			case -1:
				sendOptionsDialogue(DEFAULT_OPTIONS, "Purchase a max cape (" + Utils.numberToCashDigit(MAX_CAPE_COST) + ")", "Purchase a completionist cape (" + Utils.numberToCashDigit(COMPLETIONIST_CAPE_COST) + ")", "Purchase a 200m cape (" + Utils.numberToCashDigit(MASTER_CAPE_COST) + ")");
				stage = 0;
				break;
			case 0:
				switch (option) {
					case 2:
						purchaseCape(false);
						break;
					case 3:
						purchaseCape(true);
						break;
					case 4:
						end();
						SkillSelectionInteractionEvent.display(player);
						player.putAttribute("skill_selection_type", "MASTERS");
						break;
				}
				break;
		}
	}

	@Override
	public void finish() {

	}

	/**
	 * Handles the purchasing of a cape
	 *
	 * @param completionist
	 * 		If the cape is the completionist cape.
	 */
	private void purchaseCape(boolean completionist) {
		int[] capeItems = new int[] { completionist ? 20771 : 20767, completionist ? 20772 : 20768 };
		if (player.takeMoney(completionist ? COMPLETIONIST_CAPE_COST : MAX_CAPE_COST)) {
			for (int itemId : capeItems) {
				player.getInventory().addItemDrop(itemId, 1);
			}
			sendNPCDialogue(npcId, HAPPY, "Congratulations on your purchase.");
		} else {
			sendPlayerDialogue(CALM, "Dammit! I'm too poor to afford that right now...");
		}
		stage = -2;
	}
}
