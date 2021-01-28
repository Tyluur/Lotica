package com.runescape.game.event.interaction.button;

import com.runescape.game.event.InputEvent;
import com.runescape.game.event.InputEvent.InputEventType;
import com.runescape.game.event.interaction.type.InterfaceInteractionEvent;
import com.runescape.game.interaction.dialogues.impl.skills.LevelUp;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.network.codec.decoders.WorldPacketsDecoder;
import com.runescape.utility.Utils;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Jun 17, 2015
 */
public class SkillTabInteractionEvent extends InterfaceInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] { 320 };
	}

	@Override
	public boolean handleInterfaceInteraction(Player player, int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
		if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
			player.stopAll();
			int lvlupSkill = -1;
			int skillMenu = -1;
			switch (componentId) {
				case 200: // Attack
					skillMenu = 1;
					if (player.getAttributes().remove("leveledUp[0]") != Boolean.TRUE) {
						player.getPackets().sendConfig(965, 1);
					} else {
						lvlupSkill = 0;
						player.getPackets().sendConfig(1230, 10);
					}
					break;
				case 11: // Strength
					skillMenu = 2;
					if (player.getAttributes().remove("leveledUp[2]") != Boolean.TRUE) {
						player.getPackets().sendConfig(965, 2);
					} else {
						lvlupSkill = 2;
						player.getPackets().sendConfig(1230, 20);
					}
					break;
				case 28: // Defence
					skillMenu = 5;
					if (player.getAttributes().remove("leveledUp[1]") != Boolean.TRUE) {
						player.getPackets().sendConfig(965, 5);
					} else {
						lvlupSkill = 1;
						player.getPackets().sendConfig(1230, 40);
					}
					break;
				case 52: // Ranged
					skillMenu = 3;
					if (player.getAttributes().remove("leveledUp[4]") != Boolean.TRUE) {
						player.getPackets().sendConfig(965, 3);
					} else {
						lvlupSkill = 4;
						player.getPackets().sendConfig(1230, 30);
					}
					break;
				case 76: // Prayer
					if (player.getAttributes().remove("leveledUp[5]") != Boolean.TRUE) {
						skillMenu = 7;
						player.getPackets().sendConfig(965, 7);
					} else {
						lvlupSkill = 5;
						player.getPackets().sendConfig(1230, 60);
					}
					break;
				case 93: // Magic
					if (player.getAttributes().remove("leveledUp[6]") != Boolean.TRUE) {
						skillMenu = 4;
						player.getPackets().sendConfig(965, 4);
					} else {
						lvlupSkill = 6;
						player.getPackets().sendConfig(1230, 33);
					}
					break;
				case 110: // Runecrafting
					if (player.getAttributes().remove("leveledUp[20]") != Boolean.TRUE) {
						skillMenu = 12;
						player.getPackets().sendConfig(965, 12);
					} else {
						lvlupSkill = 20;
						player.getPackets().sendConfig(1230, 100);
					}
					break;
				case 134: // Construction
					skillMenu = 22;
					if (player.getAttributes().remove("leveledUp[21]") != Boolean.TRUE) {
						player.getPackets().sendConfig(965, 22);
					} else {
						lvlupSkill = 21;
						player.getPackets().sendConfig(1230, 698);
					}
					break;
				case 193: // Hitpoints
					skillMenu = 6;
					if (player.getAttributes().remove("leveledUp[3]") != Boolean.TRUE) {
						player.getPackets().sendConfig(965, 6);
					} else {
						lvlupSkill = 3;
						player.getPackets().sendConfig(1230, 50);
					}
					break;
				case 19: // Agility
					skillMenu = 8;
					if (player.getAttributes().remove("leveledUp[16]") != Boolean.TRUE) {
						player.getPackets().sendConfig(965, 8);
					} else {
						lvlupSkill = 16;
						player.getPackets().sendConfig(1230, 65);
					}
					break;
				case 36: // Herblore
					skillMenu = 9;
					if (player.getAttributes().remove("leveledUp[15]") != Boolean.TRUE) {
						player.getPackets().sendConfig(965, 9);
					} else {
						lvlupSkill = 15;
						player.getPackets().sendConfig(1230, 75);
					}
					break;
				case 60: // Thieving
					skillMenu = 10;
					if (player.getAttributes().remove("leveledUp[17]") != Boolean.TRUE) {
						player.getPackets().sendConfig(965, 10);
					} else {
						lvlupSkill = 17;
						player.getPackets().sendConfig(1230, 80);
					}
					break;
				case 84: // Crafting
					skillMenu = 11;
					if (player.getAttributes().remove("leveledUp[12]") != Boolean.TRUE) {
						player.getPackets().sendConfig(965, 11);
					} else {
						lvlupSkill = 12;
						player.getPackets().sendConfig(1230, 90);
					}
					break;
				case 101: // Fletching
					skillMenu = 19;
					if (player.getAttributes().remove("leveledUp[9]") != Boolean.TRUE) {
						player.getPackets().sendConfig(965, 19);
					} else {
						lvlupSkill = 9;
						player.getPackets().sendConfig(1230, 665);
					}
					break;
				case 118: // Slayer
					skillMenu = 20;
					if (player.getAttributes().remove("leveledUp[18]") != Boolean.TRUE) {
						player.getPackets().sendConfig(965, 20);
					} else {
						lvlupSkill = 18;
						player.getPackets().sendConfig(1230, 673);
					}
					break;
				case 142: // Hunter
					skillMenu = 23;
					if (player.getAttributes().remove("leveledUp[22]") != Boolean.TRUE) {
						player.getPackets().sendConfig(965, 23);
					} else {
						lvlupSkill = 22;
						player.getPackets().sendConfig(1230, 689);
					}
					break;
				case 186: // Mining
					skillMenu = 13;
					if (player.getAttributes().remove("leveledUp[14]") != Boolean.TRUE) {
						player.getPackets().sendConfig(965, 13);
					} else {
						lvlupSkill = 14;
						player.getPackets().sendConfig(1230, 110);
					}
					break;
				case 179: // Smithing
					skillMenu = 14;
					if (player.getAttributes().remove("leveledUp[13]") != Boolean.TRUE) {
						player.getPackets().sendConfig(965, 14);
					} else {
						lvlupSkill = 13;
						player.getPackets().sendConfig(1230, 115);
					}
					break;
				case 44: // Fishing
					skillMenu = 15;
					if (player.getAttributes().remove("leveledUp[10]") != Boolean.TRUE) {
						player.getPackets().sendConfig(965, 15);
					} else {
						lvlupSkill = 10;
						player.getPackets().sendConfig(1230, 120);
					}
					break;
				case 68: // Cooking
					skillMenu = 16;
					if (player.getAttributes().remove("leveledUp[7]") != Boolean.TRUE) {
						player.getPackets().sendConfig(965, 16);
					} else {
						lvlupSkill = 7;
						player.getPackets().sendConfig(1230, 641);
					}
					break;
				case 172: // Firemaking
					skillMenu = 17;
					if (player.getAttributes().remove("leveledUp[11]") != Boolean.TRUE) {
						player.getPackets().sendConfig(965, 17);
					} else {
						lvlupSkill = 11;
						player.getPackets().sendConfig(1230, 649);
					}
					break;
				case 165: // Woodcutting
					skillMenu = 18;
					if (player.getAttributes().remove("leveledUp[8]") != Boolean.TRUE) {
						player.getPackets().sendConfig(965, 18);
					} else {
						lvlupSkill = 8;
						player.getPackets().sendConfig(1230, 660);
					}
					break;
				case 126: // Farming
					skillMenu = 21;
					if (player.getAttributes().remove("leveledUp[19]") != Boolean.TRUE) {
						player.getPackets().sendConfig(965, 21);
					} else {
						lvlupSkill = 19;
						player.getPackets().sendConfig(1230, 681);
					}
					break;
				case 150: // Summoning
					skillMenu = 24;
					if (player.getAttributes().remove("leveledUp[23]") != Boolean.TRUE) {
						player.getPackets().sendConfig(965, 24);
					} else {
						lvlupSkill = 23;
						player.getPackets().sendConfig(1230, 705);
					}
					break;
				case 158: // Dung
					skillMenu = 25;
					if (player.getAttributes().remove("leveledUp[24]") != Boolean.TRUE) {
						player.getPackets().sendConfig(965, 25);
					} else {
						lvlupSkill = 24;
						player.getPackets().sendConfig(1230, 705);
					}
					break;
			}
			player.getInterfaceManager().sendInterface(lvlupSkill != -1 ? 741 : 499);
			if (lvlupSkill != -1) {
				LevelUp.switchFlash(player, lvlupSkill, false);
			}
			if (skillMenu != -1) {
				player.getAttributes().put("skillMenu", skillMenu);
			}
		} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET || packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET) { // set level target,
			// set xp target
			final int skillId = player.getSkills().getTargetIdByComponentId(componentId);
			final boolean usingLevel = packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET;
			player.getPackets().requestClientInput(new InputEvent("Please enter target " + (usingLevel ? "level" : "xp") + " you want to set: ", InputEventType.INTEGER) {
				
				@Override
				public void handleInput() {
					if (!usingLevel) {
						int xpTarget = getInput();
						if (xpTarget < player.getSkills().getXp(player.getSkills().getSkillIdByTargetId(skillId)) || player.getSkills().getXp(player.getSkills().getSkillIdByTargetId(skillId)) >= 200000000) {
							return;
						}
						if (xpTarget > 200000000) {
							xpTarget = 200000000;
						}
						player.getSkills().setSkillTarget(false, skillId, xpTarget);
					} else {
						int levelTarget = getInput();
						int curLevel = player.getSkills().getLevel(player.getSkills().getSkillIdByTargetId(skillId));
						if (curLevel >= (skillId == 24 ? 120 : 99)) {
							return;
						}
						if (levelTarget > (skillId == 24 ? 120 : 99)) {
							levelTarget = skillId == 24 ? 120 : 99;
						}
						if (levelTarget < player.getSkills().getLevel(player.getSkills().getSkillIdByTargetId(skillId))) {
							return;
						}
						player.getSkills().setSkillTarget(true, skillId, levelTarget);
					}
				}
			});
		} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET) { // clear target
			int skillId = player.getSkills().getTargetIdByComponentId(componentId);
			player.getSkills().setSkillTargetEnabled(skillId, false);
			player.getSkills().setSkillTargetValue(skillId, 0);
			player.getSkills().setSkillTargetUsingLevelMode(skillId, false);
		}
		return true;
	}

	/**
	 * If the preconditions to set your stats have been failed
	 *
	 * @param player
	 * 		The player
	 */
	public boolean preconditionsFailed(Player player) {
		if (!player.isAvailable()) {
			player.sendMessage("You cannot set your stats at this location.");
			return true;
		}
		if (player.getEquipment().wearingArmour()) {
			player.sendMessage("You cannot set your stats while wearing armour.");
			return true;
		}
		if (player.getAttackedByDelay() + 10000 > Utils.currentTimeMillis()) {
			player.sendMessage("You cannot set your stats while in combat.");
			return true;
		}
		return false;
	}

	private static final int getSkillId(int componentId) {
		switch (componentId) {
			case 150:
				return Skills.ATTACK;
			case 9:
				return Skills.STRENGTH;
			case 22:
				return Skills.DEFENCE;
			case 40:
				return Skills.RANGE;
			case 58:
				return Skills.PRAYER;
			case 71:
				return Skills.MAGIC;
			case 145:
				return Skills.HITPOINTS;
			default:
				return -1;
		}
	}

}
