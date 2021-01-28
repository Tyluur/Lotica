package com.runescape.game.event.interaction.npc;

import com.runescape.game.event.interaction.type.NPCInteractionEvent;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;
import com.runescape.utility.external.gson.GsonStartup;
import com.runescape.utility.external.gson.loaders.StoreLoader;
import com.runescape.utility.world.ClickOption;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 6/19/2015
 */
public class ShopNPCInteractionEvent extends NPCInteractionEvent {

	@Override
	public int[] getKeys() {
		return Utils.toPrimitive(getIds());
	}

	@Override
	public boolean handleNPCInteraction(Player player, NPC npc, ClickOption option) {
		String name = getNameById(npc.getId());
		if (name == null) {
			return false;
		}
		if (name.contains("DIALOGUE")) {
			switch (name) {
				case "DIALOGUE_BOB":
					player.getDialogueManager().startDialogue(new BobDialogue());
					return true;
				case "DIALOGUE_STIX":
					player.getDialogueManager().startDialogue(new Dialogue() {

						@Override
						public void start() {
							sendOptionsDialogue(DEFAULT_OPTIONS, "Novice Supplies", "Elite Supplies");
						}

						@Override
						public void run(int interfaceId, int option) {
							if (option == FIRST) {
								openStore("Basic Summoning Store");
							} else {
								openStore("Advanced Summoning Store");
							}
						}

						@Override
						public void finish() {

						}
					});
					return true;
			}
		}
		openStore(player, name);
		return true;
	}

	/**
	 * Opens a specific store
	 *
	 * @param player
	 * 		The player
	 * @param name
	 * 		The name of the store
	 */
	private void openStore(Player player, String name) {
		GsonStartup.getOptional(StoreLoader.class).ifPresent(c -> c.openStore(player, name));
	}

	/**
	 * This method converts
	 */
	private Integer[] getIds() {
		List<Integer> idList = new ArrayList<>();
		for (Object[] SHOP_DETAIL : SHOP_DETAILS) {
			idList.add((Integer) SHOP_DETAIL[0]);
		}
		return idList.toArray(new Integer[idList.size()]);
	}

	/**
	 * This method gets the name of a shop by its id
	 *
	 * @param id
	 * 		The id of the shop
	 */
	private String getNameById(int id) {
		for (Object[] SHOP_DETAIL : SHOP_DETAILS) {
			if ((int) SHOP_DETAIL[0] == id) {
				return (String) SHOP_DETAIL[1];
			}
		}
		return null;
	}

	/**
	 * This 2D array consists of npc ids and the name of their shop
	 */
	private static final Object[][] SHOP_DETAILS = {
			                                               //
			                                               { 1699, "PK Supplies" },
			                                               //
			                                               { 1658, "Magic Supplies & Armour" },
			                                               //
			                                               { 550, "Ranging Store" },
			                                               //
			                                               { 549, "Melee Store" },
			                                               //
			                                               { 554, "Fancy Dress Store" },
			                                               //
			                                               { 5112, "Hunting Store" },
			                                               //
			                                               { 278, "Combat Supplies" },
			                                               //
			                                               { 6970, "DIALOGUE_STIX" },
														   //
			                                               { 7938, "Ironman Shop" },
														   //
														   { 520, "Edgeville General Store"},
														   //
			                                               { 2620, "TzHaar-Hur-Tel's Equipment Store"},
			                                               //
			                                               { 519, "DIALOGUE_BOB" },

	};

	private class BobDialogue extends Dialogue {

		@Override
		public void start() {
			sendOptionsDialogue(DEFAULT_OPTIONS, "Skilling Tools", "Farming", "Herblore", "Crafting Tools", "Construction Tools");
		}

		@Override
		public void run(int interfaceId, int option) {
			switch (stage) {
				case -1:
					switch (option) {
						case FIRST:
							openStore("Skilling Tools");
							break;
						case SECOND:
							openStore("Farming Store");
							break;
						case THIRD:
							openStore("Herblore Utilities");
							break;
						case FOURTH:
							openStore("Crafting Tools");
							break;
						case FIFTH:
							sendOptionsDialogue(DEFAULT_OPTIONS, "Construction Shop 1", "Construction Shop 2");
							stage = 0;
							break;
					}
					break;
				case 0:
					switch (option) {
						case FIRST:
							openStore("Construction Shop 1");
							break;
						case SECOND:
							openStore("Construction Shop 2");
							break;
					}
					break;
			}
		}

		@Override
		public void finish() {

		}
	}
}
