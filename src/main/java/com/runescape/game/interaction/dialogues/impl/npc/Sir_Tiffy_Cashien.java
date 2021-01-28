package com.runescape.game.interaction.dialogues.impl.npc;

import com.runescape.game.content.PotionOperations;
import com.runescape.game.event.InputEvent;
import com.runescape.game.event.InputEvent.InputEventType;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleMessage;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.utility.Utils;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Jun 17, 2015
 */
public class Sir_Tiffy_Cashien extends Dialogue {

	private static final int ROW_RECHARGE_COST = 750_000;

	private static final int DFS_COST = 7_500_000;

	@Override
	public void start() {
		npcId = getParam(0);
		sendOptionsDialogue(DEFAULT_OPTIONS, "Decant potions", "Recharge ring of wealth", "Set security question", "Reset combat skills", "Make me a dragonfire-shield");
	}

	@Override
	public void run(int interfaceId, int option) {
		switch (stage) {
			case -1:
				switch (option) {
					case FIRST:
						PotionOperations.decantInventory(player, 4);
						sendNPCDialogue(npcId, CALM, "All your potions have been decanted.");
						stage = -2;
						break;
					case SECOND:
						sendOptionsDialogue("Pay " + Utils.numberToCashDigit(ROW_RECHARGE_COST) + " to recharge?", "Yes", "No");
						stage = 4;
						break;
					case THIRD:
						end();
						player.getPackets().requestClientInput(new InputEvent("Enter your security question:", InputEventType.LONG_TEXT) {
							@Override
							public void handleInput() {
								Object input = getInput();
								if (input instanceof String) {
									player.getSecurityDetails().setSecurityQuestion((String) input);
									player.getPackets().requestClientInput(new InputEvent("Enter the answer to your security question:", InputEventType.NAME) {
										@Override
										public void handleInput() {
											Object input = getInput();
											if (input instanceof String) {
												player.getSecurityDetails().setComputerAddressSetWith(player.getMacAddress());
												player.getSecurityDetails().setSecurityAnswer((String) input);
												player.getDialogueManager().startDialogue(SimpleMessage.class, "Your security question has been saved<br>your account is now more secure.");
											} else {
												player.getSecurityDetails().setSecurityQuestion("none");
												player.getSecurityDetails().setSecurityAnswer("none");
												player.getDialogueManager().startDialogue(SimpleMessage.class, "You must enter a valid answer.", "Your security question was not saved.");
											}
										}
									});
								} else {
									player.getDialogueManager().startDialogue(SimpleMessage.class, "You must enter a valid question.");
								}
							}
						});
						break;
					case FOURTH:
						sendOptionsDialogue("Select a skill", "Attack", "Strength", "Defence", "Magic", "Next");
						stage = 7;
						break;
					case FIFTH:
						sendOptionsDialogue("Are you sure you want a DFS?", "Yes", " No");
						stage = 9;
						break;
				}
				break;
			case 0:
				switch (option) {
					case FIRST:
						resetSkill(Skills.ATTACK);
						break;
					case SECOND:
						resetSkill(Skills.STRENGTH);
						break;
					case THIRD:
						resetSkill(Skills.DEFENCE);
						break;
					case FOURTH:
						resetSkill(Skills.RANGE);
						break;
					case FIFTH:
						sendOptionsDialogue("Prayer", "Magic", "Hitpoints", "Summoning", "Back");
						stage = 1;
						break;
				}
				break;
			case 1:
				switch (option) {
					case FIRST:
						resetSkill(Skills.PRAYER);
						break;
					case SECOND:
						resetSkill(Skills.HITPOINTS);
						break;
					case THIRD:
						resetSkill(Skills.SUMMONING);
						break;
					case FOURTH:
						sendOptionsDialogue(DEFAULT_OPTIONS, "Attack", "Strength", "Defence", "Range", "Next");
						stage = 0;
						break;
				}
				break;
			case 2:
				switch (option) {
					case FIRST:
						setSkill(Skills.ATTACK);
						break;
					case SECOND:
						setSkill(Skills.STRENGTH);
						break;
					case THIRD:
						setSkill(Skills.DEFENCE);
						break;
					case FOURTH:
						setSkill(Skills.RANGE);
						break;
					case FIFTH:
						sendOptionsDialogue("Prayer", "Magic", "Hitpoints", "Summoning", "Back");
						stage = 3;
						break;
				}
				break;
			case 3:
				switch (option) {
					case FIRST:
						setSkill(Skills.PRAYER);
						break;
					case SECOND:
						setSkill(Skills.HITPOINTS);
						break;
					case THIRD:
						setSkill(Skills.SUMMONING);
						break;
					case FOURTH:
						sendOptionsDialogue(DEFAULT_OPTIONS, "Attack", "Strength", "Defence", "Range", "Next");
						stage = 2;
						break;
				}
				break;
			case 4:
				if (option == FIRST) {
					if (player.takeMoney(ROW_RECHARGE_COST)) {
						player.getFacade().setRowCharges(100);
						sendPlayerDialogue(HAPPY, "Thanks a lot!");
					} else {
						sendPlayerDialogue(CALM, "I don't have " + Utils.numberToCashDigit(ROW_RECHARGE_COST) + " to pay for this.");
					}
					stage = -2;
				} else {
					end();
				}
				break;
			case 5:
				switch (option) {
					case FIRST:
						end();
						PotionOperations.decantInventory(player, 4);
						break;
					case SECOND:
						int conversionCost = PotionOperations.getFlaskConversionCost(player);
						if (conversionCost > 0) {
							sendOptionsDialogue("Pay " + Utils.format(conversionCost) + " gp to make " + PotionOperations.getTotalFlasksCreateable(player) + " flasks?", "Yes", "No");
							stage = 6;
						} else {
							sendNPCDialogue(npcId, CALM, "You don't have any potions I can convert into flasks.");
							stage = -2;
						}
						break;
				}
				break;
			case 6:
				if (option == FIRST) {
					if (player.takeMoney(PotionOperations.getFlaskConversionCost(player))) {
						sendPlayerDialogue(HAPPY, "Thanks!");
						PotionOperations.convertAllFlasks(player);
					} else {
						sendPlayerDialogue(CALM, "Sorry, I don't have that much money.");
					}
					stage = -2;
				} else {
					end();
				}
				break;
			case 7:
				//sendOptionsDialogue("Select a skill", "Attack", "Strength", "Defence", "Magic", "Next");
				switch (option) {
					case FIRST:
						resetSkill(Skills.ATTACK);
						break;
					case SECOND:
						resetSkill(Skills.STRENGTH);
						break;
					case THIRD:
						resetSkill(Skills.DEFENCE);
						break;
					case FOURTH:
						resetSkill(Skills.MAGIC);
						break;
					case FIFTH:
						sendOptionsDialogue("Select a skill", "Range", "Prayer", "Hitpoints", "Back");
						stage = 8;
						return;
				}
				break;
			case 8:
				switch (option) {
					case FIRST:
						resetSkill(Skills.RANGE);
						break;
					case SECOND:
						resetSkill(Skills.PRAYER);
						break;
					case THIRD:
						resetSkill(Skills.HITPOINTS);
						break;
					case FOURTH:
						sendOptionsDialogue("Select a skill", "Attack", "Strength", "Defence", "Magic", "Next");
						stage = 7;
						break;
				}
				break;
			case 9:
				if (option == FIRST) {
					sendDialogue("Tiffy can do this but it will cost you " + Utils.format(DFS_COST) + " coins.", "Are you sure you wish to continue?");
					stage = 10;
				} else {
					end();
				}
				break;
			case 10:
				sendOptionsDialogue(DEFAULT_OPTIONS, "Yes", "No");
				stage = 11;
				break;
			case 11:
				if (option == FIRST) {
					if (!player.getInventory().containsItems(new int[] { 11286, 1540 }, new int[] { 1, 1 })) {
						sendNPCDialogue(npcId, CALM, "You need to have a visage, and an anti-", "dragon fire shield in your inventory to do this.");
						stage = -2;
						return;
					}
					if (player.takeMoney(DFS_COST)) {
						sendItemDialogue(11283, 1, "Even for an expert armourer it is not an easy task,", "but eventually it is ready. Tiffy has crafted the", "draconic visage and anti-dragonbreath shield into a", "dragonfire shield");
						player.getInventory().deleteItem(11286, 1);
						player.getInventory().deleteItem(1540, 1);
						player.getInventory().addItem(11283, 1);
					} else {
						sendPlayerDialogue(CALM, "I'll be back when I have enough money.");
					}
					stage = -2;
				} else {
					end();
				}
				break;
			case 20:
				if (option == FIRST) {
					int skillId = player.getAttribute("reset_skill_id", -1);
					if (skillId == -1) {
						sendDialogue("Error...");
						stage = -2;
						return;
					}
					if (player.getEquipment().wearingArmour()) {
						sendDialogue("You must take off your armour first.");
						stage = -2;
						return;
					}
					if (!player.takeMoney(RESET_SKILL_COST)) {
						sendDialogue("You do not have " + Utils.numberToCashDigit(RESET_SKILL_COST) + " coins to pay for this.");
						stage = -2;
						return;
					}
					int level = skillId == Skills.HITPOINTS ? 10 : 1;
					player.getSkills().setLevel(skillId, level);
					player.getSkills().setXp(skillId, Skills.getXPForLevel(level));
					if (skillId == Skills.PRAYER) {
						player.getPrayer().restorePrayer(990);
					}
					sendNPCDialogue(npcId, HAPPY, "Your " + Skills.SKILL_NAME[skillId].toLowerCase() + " level has just been reset!");
					stage = -2;
				} else {
					end();
				}
				break;
		}
	}

	/**
	 * Handles the resetting of a skill
	 *
	 * @param skillId
	 * 		The id of the skill
	 */
	private void resetSkill(int skillId) {
		sendOptionsDialogue("Confirm resetting " + Skills.SKILL_NAME[skillId].toLowerCase(), "Yes, I'm sure.", "No, never mind.");
		player.putAttribute("reset_skill_id", skillId);
		stage = 20;
	}

	/**
	 * Sets a skill to a certain level
	 *
	 * @param skillId
	 * 		The id of the skill
	 */
	private void setSkill(int skillId) {
		if (player.getEquipment().wearingArmour()) {
			sendDialogue("You must take off your armour first.");
			return;
		}
		player.getPackets().requestClientInput(new InputEvent("Enter Level", InputEventType.INTEGER) {

			@Override
			public void handleInput() {
				int level = getInput();
				if (level > player.getSkills().getLevelForXp(skillId)) {
					sendNPCDialogue(npcId, HAPPY, "You can only set skills to a level less than your current one.", "So if you have 80 attack, you can only set your attack to 80 or less.");
				} else {
					if (!player.takeMoney(RESET_SKILL_COST)) {
						sendDialogue("You do not have " + Utils.numberToCashDigit(RESET_SKILL_COST) + " coins to pay for this.");
					} else {
						player.getSkills().setLevel(skillId, level);
						player.getSkills().setXp(skillId, Skills.getXPForLevel(level));
						sendNPCDialogue(npcId, HAPPY, "Your " + Skills.SKILL_NAME[skillId].toLowerCase() + " level has been set to " + level + ".", "Enjoy!");
					}
				}
				stage = -2;
			}
		});
	}

	@Override
	public void finish() {
	}

	/**
	 * The id of the npc we're interacting with
	 */
	private int npcId;

	/**
	 * The cost to reset a combat skill
	 */
	private static final int RESET_SKILL_COST = 2_500_000;

	/**
	 * The cost to set a combat skill
	 */
	private static final int SET_SKILL_COST = 3_500_000;

}
