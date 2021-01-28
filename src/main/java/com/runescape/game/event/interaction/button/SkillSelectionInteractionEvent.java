package com.runescape.game.event.interaction.button;

import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.game.event.interaction.type.InterfaceInteractionEvent;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.interaction.dialogues.impl.npc.Max;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.game.world.item.Item;
import com.runescape.utility.Utils;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/2/2015
 */
public class SkillSelectionInteractionEvent extends InterfaceInteractionEvent {
	
	@Override
	public int[] getKeys() {
		return new int[] { 1214 };
	}

	/**
	 * Displays the skill selection interface
	 *
	 * @param player
	 * 		The player to display the interface to
	 */
	public static void display(Player player) {
		int interfaceId = 1214;
		player.getPackets().sendHideIComponent(interfaceId, 20, true);
		player.getPackets().sendHideIComponent(interfaceId, 22, true);
		player.getPackets().sendHideIComponent(interfaceId, 24, true);
		player.getPackets().sendHideIComponent(interfaceId, 27, true);

		player.getPackets().sendHideIComponent(interfaceId, 58, true);
		player.getPackets().sendHideIComponent(interfaceId, 56, true);
		player.getPackets().sendHideIComponent(interfaceId, 61, true);

		player.setCloseInterfacesEvent(() -> player.removeAttribute("skill_selection_type"));

		player.getPackets().sendIComponentText(interfaceId, 23, "Select a Skill");
		player.getInterfaceManager().sendInterface(interfaceId);
	}

	@Override
	public boolean handleInterfaceInteraction(Player player, int interfaceId, int buttonId, int slotId, int slotId2, int packetId) {
		if (player.getAttribute("skill_selection_type") != null) {
			if (buttonId == 18) {
				player.closeInterfaces();
				return true;
			}
			if (buttonId == 23 || buttonId == 57) {
				return true;
			}
			String type = player.getAttribute("skill_selection_type");
			final int skill = Skills.XP_COUNTER_STAT_ORDER[buttonId - 31];
			switch (type) {
				case "MASTERS":
					String masterCapeName = Skills.SKILL_NAME[skill] + " master cape";
					ItemDefinitions def = ItemDefinitions.forName(masterCapeName);
					if (def == null) {
						throw new IllegalStateException("Couldn't get master cape for " + masterCapeName);
					}
					int capeId = def.getId();
					player.getDialogueManager().startDialogue(new Dialogue() {
						@Override
						public void start() {
							sendOptionsDialogue("Purchase " + masterCapeName + "<br> for " + Utils.numberToCashDigit(Max.MASTER_CAPE_COST) + "?", "Yes", "No");
						}

						@Override
						public void run(int interfaceId, int option) {
							if (option == FIRST) {
								if (player.takeMoney(Max.MASTER_CAPE_COST)) {
									player.getInventory().addItemDrop(capeId, 1);
									sendItemDialogue(capeId, 1, "You receive a " + masterCapeName + ".");
								} else {
									sendPlayerDialogue(CALM, "I don't have that much money.");
								}
								stage = -2;
							} else {
								end();
							}
						}

						@Override
						public void finish() {
							player.getInterfaceManager().closeScreenInterface();
						}
					});
					break;
				case "CAPES":
					final Item[] capes = player.getSkills().getSkillCape(skill);
					if (capes == null) {
						return true;
					}
					player.getDialogueManager().startDialogue(new Dialogue() {

						@Override
						public void start() {
							sendOptionsDialogue("Purchase " + Skills.SKILL_NAME[skill].toLowerCase() + "<br> cape for 99K?", "Yes", "No");
						}

						@Override
						public void run(int interfaceId, int option) {
							switch (stage) {
								case -1:
									if (option == FIRST) {
										if (player.takeMoney(99_000)) {
											for (Item item : capes) {
												if (item == null) {
													System.err.println("Nulled cape with skill: " + skill);
													continue;
												}
												player.getInventory().addItemDrop(item.getId(), item.getAmount());
											}
											sendPlayerDialogue(CALM, "Thank you!");
										} else {
											sendPlayerDialogue(CALM, "I don't have 99K.");
										}
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
					}, capes);
					break;
			}
			return true;
		}
		return false;
	}

	public static final Item[][] TRIMMED_CAPES = {
			// { new Item(20771), new Item(20772) }, //COMPLETIONIST
			{ new Item(9748), new Item(9749) }, // ATTACK
			{ new Item(9751), new Item(9752) }, // STRENGTH
			{ new Item(9754), new Item(9755) }, // DEFENCE
			{ new Item(9757), new Item(9758) }, // RANGED
			{ new Item(9760), new Item(9761) }, // PRAYER
			{ new Item(9763), new Item(9764) }, // MAGIC
			{ new Item(9766), new Item(9767) }, // RUNECRAFTING
			{ new Item(9790), new Item(10654) }, // CONSTRUCTION
			{ new Item(18509), new Item(18510) }, // DUNGEONEERING
			{ new Item(9769), new Item(9770) }, // HITPOINTS
			{ new Item(9772), new Item(9773) }, // AGILITY
			{ new Item(9775), new Item(9776) }, // HERBLORE
			{ new Item(9778), new Item(9779) }, // THEIVING
			{ new Item(9781), new Item(9782) }, // CRAFTING
			{ new Item(9784), new Item(9785) }, // FLETCHING
			{ new Item(9787), new Item(9788) }, // SLAYER
			{ new Item(9949), new Item(9950) }, // HUNTER
			{ new Item(9793), new Item(9794) }, // MINING
			{ new Item(9796), new Item(9797) }, // SMITHING
			{ new Item(9799), new Item(9800) }, // FISHING
			{ new Item(9802), new Item(9803) }, // COOKING
			{ new Item(9805), new Item(9806) }, // FIREMAKING
			{ new Item(9808), new Item(9809) }, // WOODCUTTING
			{ new Item(9811), new Item(9812) }, // FARMING
			{ new Item(12170), new Item(12171) } }; // SUMMONING

	/**
	 * The two-dimensional array of untrimmed capes.
	 */
	public static final Item[][] UNTRIMMED_CAPES = {
			// { new Item(20771), new Item(20772) }, //COMPLETIONIST
			{ new Item(9747), new Item(9749) }, // ATTACK
			{ new Item(9750), new Item(9752) }, // STRENGTH
			{ new Item(9753), new Item(9755) }, // DEFENCE
			{ new Item(9756), new Item(9758) }, // RANGED
			{ new Item(9759), new Item(9761) }, // PRAYER
			{ new Item(9762), new Item(9764) }, // MAGIC
			{ new Item(9765), new Item(9767) }, // RUNECRAFTING
			{ new Item(9789), new Item(10654) }, // CONSTRUCTION
			{ new Item(18508), new Item(18510) }, // DUNGEONEERING
			{ new Item(9768), new Item(9770) }, // HITPOINTS
			{ new Item(9771), new Item(9773) }, // AGILITY
			{ new Item(9774), new Item(9776) }, // HERBLORE
			{ new Item(9777), new Item(9779) }, // THEIVING
			{ new Item(9780), new Item(9782) }, // CRAFTING
			{ new Item(9783), new Item(9785) }, // FLETCHING
			{ new Item(9786), new Item(9788) }, // SLAYER
			{ new Item(9948), new Item(9950) }, // HUNTER
			{ new Item(9792), new Item(9794) }, // MINING
			{ new Item(9795), new Item(9797) }, // SMITHING
			{ new Item(9798), new Item(9800) }, // FISHING
			{ new Item(9801), new Item(9803) }, // COOKING
			{ new Item(9804), new Item(9806) }, // FIREMAKING
			{ new Item(9807), new Item(9809) }, // WOODCUTTING
			{ new Item(9810), new Item(9812) }, // FARMING
			{ new Item(12169), new Item(12171) } }; // SUMMONING
}
