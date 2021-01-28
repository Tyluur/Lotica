package com.runescape.game.content.skills.magic;

import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.game.GameConstants;
import com.runescape.game.content.global.minigames.clanwars.FfaZone;
import com.runescape.game.content.global.minigames.clanwars.RequestController;
import com.runescape.game.event.interaction.button.PlankCreationInteractionEvent.Planks;
import com.runescape.game.interaction.controllers.impl.Wilderness;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleNPCMessage;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.player.CombatDefinitions;
import com.runescape.game.world.entity.player.Equipment;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.game.world.entity.player.actions.Action;
import com.runescape.game.world.entity.player.quests.impl.DesertTreasure;
import com.runescape.game.world.item.Item;
import com.runescape.game.world.item.ItemConstants;
import com.runescape.utility.Utils;
import com.runescape.utility.world.Coordinates;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

import java.util.concurrent.TimeUnit;

/*
 * content package used for static stuff
 */
public class Magic {

	public static final int MAGIC_TELEPORT = 0, ITEM_TELEPORT = 1, OBJECT_TELEPORT = 2;

	@SuppressWarnings("unused")
	public static final int AIR_RUNE = 556, WATER_RUNE = 555, EARTH_RUNE = 557, FIRE_RUNE = 554, MIND_RUNE = 558, NATURE_RUNE = 561, CHAOS_RUNE = 562, DEATH_RUNE = 560, BLOOD_RUNE = 565, SOUL_RUNE = 566, ASTRAL_RUNE = 9075, LAW_RUNE = 563, STEAM_RUNE = 4694, MIST_RUNE = 4695, DUST_RUNE = 4696, SMOKE_RUNE = 4697, MUD_RUNE = 4698, LAVA_RUNE = 4699, ARMADYL_RUNE = 21773;

	private final static WorldTile[] TABS = { new WorldTile(3217, 3426, 0), new WorldTile(3222, 3218, 0), new WorldTile(2965, 3379, 0), new WorldTile(2758, 3478, 0), new WorldTile(2660, 3306, 0), Coordinates.WATCHTOWER, GameConstants.START_PLAYER_LOCATION };

	private Magic() {

	}

	public static boolean checkCombatSpell(Player player, int spellId, int set, boolean delete) {
		if (spellId == 65535) {
			return true;
		}
		switch (player.getCombatDefinitions().getSpellBook()) {
			case 193:
				switch (spellId) {
					case 28:
						if (!checkSpellRequirements(player, 50, delete, CHAOS_RUNE, 2, DEATH_RUNE, 2, FIRE_RUNE, 1, AIR_RUNE, 1)) {
							return false;
						}
						break;
					case 32:
						if (!checkSpellRequirements(player, 52, delete, CHAOS_RUNE, 2, DEATH_RUNE, 2, AIR_RUNE, 1, SOUL_RUNE, 1)) {
							return false;
						}
						break;
					case 24:
						if (!checkSpellRequirements(player, 56, delete, CHAOS_RUNE, 2, DEATH_RUNE, 2, BLOOD_RUNE, 1)) {
							return false;
						}
						break;
					case 20:
						if (!checkSpellRequirements(player, 58, delete, CHAOS_RUNE, 2, DEATH_RUNE, 2, WATER_RUNE, 2)) {
							return false;
						}
						break;
					case 30:
						if (!checkSpellRequirements(player, 62, delete, CHAOS_RUNE, 4, DEATH_RUNE, 2, FIRE_RUNE, 2, AIR_RUNE, 2)) {
							return false;
						}
						break;
					case 34:
						if (!checkSpellRequirements(player, 64, delete, CHAOS_RUNE, 4, DEATH_RUNE, 2, AIR_RUNE, 1, SOUL_RUNE, 2)) {
							return false;
						}
						break;
					case 26:
						if (!checkSpellRequirements(player, 68, delete, CHAOS_RUNE, 4, DEATH_RUNE, 2, BLOOD_RUNE, 2)) {
							return false;
						}
						break;
					case 22:
						if (!checkSpellRequirements(player, 70, delete, CHAOS_RUNE, 4, DEATH_RUNE, 2, WATER_RUNE, 4)) {
							return false;
						}
						break;
					case 29:
						if (!checkSpellRequirements(player, 74, delete, DEATH_RUNE, 2, BLOOD_RUNE, 2, FIRE_RUNE, 2, AIR_RUNE, 2)) {
							return false;
						}
						break;
					case 33:
						if (!checkSpellRequirements(player, 76, delete, DEATH_RUNE, 2, BLOOD_RUNE, 2, AIR_RUNE, 2, SOUL_RUNE, 2)) {
							return false;
						}
						break;
					case 25:
						if (!checkSpellRequirements(player, 80, delete, DEATH_RUNE, 2, BLOOD_RUNE, 4)) {
							return false;
						}
						break;
					case 21:
						if (!checkSpellRequirements(player, 82, delete, DEATH_RUNE, 2, BLOOD_RUNE, 2, WATER_RUNE, 3)) {
							return false;
						}
						break;
					case 31:
						if (!checkSpellRequirements(player, 86, delete, DEATH_RUNE, 4, BLOOD_RUNE, 2, FIRE_RUNE, 4, AIR_RUNE, 4)) {
							return false;
						}
						break;
					case 35:
						if (!checkSpellRequirements(player, 88, delete, DEATH_RUNE, 4, BLOOD_RUNE, 2, AIR_RUNE, 4, SOUL_RUNE, 3)) {
							return false;
						}
						break;
					case 27:
						if (!checkSpellRequirements(player, 92, delete, DEATH_RUNE, 4, BLOOD_RUNE, 4, SOUL_RUNE, 1)) {
							return false;
						}
						break;
					case 23:
						if (!checkSpellRequirements(player, 94, delete, DEATH_RUNE, 4, BLOOD_RUNE, 2, WATER_RUNE, 6)) {
							return false;
						}
						break;
					case 36: // Miasmic rush.
						if (!checkSpellRequirements(player, 61, delete, CHAOS_RUNE, 2, EARTH_RUNE, 1, SOUL_RUNE, 1)) {
							return false;
						}
						int weaponId = player.getEquipment().getWeaponId();
						if (weaponId != 13867 && weaponId != 13869 && weaponId != 13941 && weaponId != 13943) {
							player.getPackets().sendGameMessage("You need a Zuriel's staff to cast this spell.");
							return false;
						}
						break;
					case 38: // Miasmic burst.
						if (!checkSpellRequirements(player, 73, delete, CHAOS_RUNE, 4, EARTH_RUNE, 2, SOUL_RUNE, 2)) {
							return false;
						}
						weaponId = player.getEquipment().getWeaponId();
						if (weaponId != 13867 && weaponId != 13869 && weaponId != 13941 && weaponId != 13943) {
							player.getPackets().sendGameMessage("You need a Zuriel's staff to cast this spell.");
							return false;
						}
						break;
					case 37: // Miasmic blitz.
						if (!checkSpellRequirements(player, 85, delete, BLOOD_RUNE, 2, EARTH_RUNE, 3, SOUL_RUNE, 3)) {
							return false;
						}
						weaponId = player.getEquipment().getWeaponId();
						if (weaponId != 13867 && weaponId != 13869 && weaponId != 13941 && weaponId != 13943) {
							player.getPackets().sendGameMessage("You need a Zuriel's staff to cast this spell.");
							return false;
						}
						break;
					case 39: // Miasmic barrage.
						if (!checkSpellRequirements(player, 97, delete, BLOOD_RUNE, 4, EARTH_RUNE, 4, SOUL_RUNE, 4)) {
							return false;
						}
						weaponId = player.getEquipment().getWeaponId();
						if (weaponId != 13867 && weaponId != 13869 && weaponId != 13941 && weaponId != 13943) {
							player.getPackets().sendGameMessage("You need a Zuriel's staff to cast this spell.");
							return false;
						}
						break;
					default:
						return false;
				}
				break;
			case 192:
				switch (spellId) {
					case 25:
						if (!checkSpellRequirements(player, 1, delete, AIR_RUNE, 1, MIND_RUNE, 1)) {
							return false;
						}
						break;
					case 28:
						if (!checkSpellRequirements(player, 5, delete, WATER_RUNE, 1, AIR_RUNE, 1, MIND_RUNE, 1)) {
							return false;
						}
						break;
					case 30:
						if (!checkSpellRequirements(player, 9, delete, EARTH_RUNE, 2, AIR_RUNE, 1, MIND_RUNE, 1)) {
							return false;
						}
						break;
					case 32:
						if (!checkSpellRequirements(player, 13, delete, FIRE_RUNE, 3, AIR_RUNE, 2, MIND_RUNE, 1)) {
							return false;
						}
						break;
					case 34: // air bolt
						if (!checkSpellRequirements(player, 17, delete, AIR_RUNE, 2, CHAOS_RUNE, 1)) {
							return false;
						}
						break;
					case 36:// bind
						if (!checkSpellRequirements(player, 20, delete, EARTH_RUNE, 3, WATER_RUNE, 3, NATURE_RUNE, 2)) {
							return false;
						}
						break;
					case 55: // snare
						if (!checkSpellRequirements(player, 50, delete, EARTH_RUNE, 4, WATER_RUNE, 4, NATURE_RUNE, 3)) {
							return false;
						}
						break;
					case 81:// entangle
						if (!checkSpellRequirements(player, 79, delete, EARTH_RUNE, 5, WATER_RUNE, 5, NATURE_RUNE, 4)) {
							return false;
						}
						break;
					case 39: // water bolt
						if (!checkSpellRequirements(player, 23, delete, WATER_RUNE, 2, AIR_RUNE, 2, CHAOS_RUNE, 1)) {
							return false;
						}
						break;
					case 42: // earth bolt
						if (!checkSpellRequirements(player, 29, delete, EARTH_RUNE, 3, AIR_RUNE, 2, CHAOS_RUNE, 1)) {
							return false;
						}
						break;
					case 45: // fire bolt
						if (!checkSpellRequirements(player, 35, delete, FIRE_RUNE, 4, AIR_RUNE, 3, CHAOS_RUNE, 1)) {
							return false;
						}
						break;
					case 49: // air blast
						if (!checkSpellRequirements(player, 41, delete, AIR_RUNE, 3, DEATH_RUNE, 1)) {
							return false;
						}
						break;
					case 52: // water blast
						if (!checkSpellRequirements(player, 47, delete, WATER_RUNE, 3, AIR_RUNE, 3, DEATH_RUNE, 1)) {
							return false;
						}
						break;
					case 58: // earth blast
						if (!checkSpellRequirements(player, 53, delete, EARTH_RUNE, 4, AIR_RUNE, 3, DEATH_RUNE, 1)) {
							return false;
						}
						break;
					case 63: // fire blast
						if (!checkSpellRequirements(player, 59, delete, FIRE_RUNE, 5, AIR_RUNE, 4, DEATH_RUNE, 1)) {
							return false;
						}
						break;
					case 70: // air wave
						if (!checkSpellRequirements(player, 62, delete, AIR_RUNE, 5, BLOOD_RUNE, 1)) {
							return false;
						}
						break;
					case 73: // water wave
						if (!checkSpellRequirements(player, 65, delete, WATER_RUNE, 7, AIR_RUNE, 5, BLOOD_RUNE, 1)) {
							return false;
						}
						break;
					case 77: // earth wave
						if (!checkSpellRequirements(player, 70, delete, EARTH_RUNE, 7, AIR_RUNE, 5, BLOOD_RUNE, 1)) {
							return false;
						}
						break;
					case 80: // fire wave
						if (!checkSpellRequirements(player, 75, delete, FIRE_RUNE, 7, AIR_RUNE, 5, BLOOD_RUNE, 1)) {
							return false;
						}
						break;
					case 84:
						if (!checkSpellRequirements(player, 81, delete, AIR_RUNE, 7, DEATH_RUNE, 1, BLOOD_RUNE, 1)) {
							return false;
						}
						break;
					case 87:
						if (!checkSpellRequirements(player, 85, delete, WATER_RUNE, 10, AIR_RUNE, 7, DEATH_RUNE, 1, BLOOD_RUNE, 1)) {
							return false;
						}
						break;
					case 89:
						if (!checkSpellRequirements(player, 85, delete, EARTH_RUNE, 10, AIR_RUNE, 7, DEATH_RUNE, 1, BLOOD_RUNE, 1)) {
							return false;
						}
						break;
					case 66: // Sara Strike
						if (player.getEquipment().getWeaponId() != 2415) {
							player.getPackets().sendGameMessage("You need to be equipping a Saradomin staff to cast this spell.", true);
							return false;
						}
						if (!checkSpellRequirements(player, 60, delete, AIR_RUNE, 4, FIRE_RUNE, 1, BLOOD_RUNE, 2)) {
							return false;
						}
						break;
					case 67: // Guthix Claws
						if (player.getEquipment().getWeaponId() != 2416) {
							player.getPackets().sendGameMessage("You need to be equipping a Guthix Staff or Void Mace to cast this spell.", true);
							return false;
						}
						if (!checkSpellRequirements(player, 60, delete, AIR_RUNE, 4, FIRE_RUNE, 1, BLOOD_RUNE, 2)) {
							return false;
						}
						break;
					case 68: // Flame of Zammy
						if (player.getEquipment().getWeaponId() != 2417) {
							player.getPackets().sendGameMessage("You need to be equipping a Zamorak Staff to cast this spell.", true);
							return false;
						}
						if (!checkSpellRequirements(player, 60, delete, AIR_RUNE, 4, FIRE_RUNE, 4, BLOOD_RUNE, 2)) {
							return false;
						}
						break;
					case 91:
						if (!checkSpellRequirements(player, 85, delete, FIRE_RUNE, 10, AIR_RUNE, 7, DEATH_RUNE, 1, BLOOD_RUNE, 1)) {
							return false;
						}
						break;
					case 86: // teleblock
						if (!checkSpellRequirements(player, 85, delete, CHAOS_RUNE, 1, LAW_RUNE, 1, DEATH_RUNE, 1)) {
							return false;
						}
						break;
					case 99: // Storm of Armadyl
						if (!checkSpellRequirements(player, 77, delete, ARMADYL_RUNE, 1)) {
							return false;
						}
						break;
					default:
						return false;
				}
				break;
			default:
				return false;
		}
		if (set >= 0) {
			if (set == 0) {
				player.getCombatDefinitions().setAutoCastSpell(spellId);
			} else {
				player.getAttributes().put("tempCastSpell", spellId);
			}
		}
		return true;
	}

	public static void setCombatSpell(Player player, int spellId) {
		if (player.getCombatDefinitions().getAutoCastSpell() == spellId) {
			player.getCombatDefinitions().resetSpells(true);
		} else {
			checkCombatSpell(player, spellId, 0, false);
		}
	}

	public static void processLunarSpell(Player player, int spellId, int packetId) {
		player.stopAll(false);
		/*if (spellId != 39 && !player.getQuestManager().isFinished(LunarDiplomacy.class)) {
			player.getDialogueManager().startDialogue(SimpleNPCMessage.class, 33, "You must complete 'Lunar Diplomacy' to use this spellbook.", "Check your quest diary to see how to start this quest.");
			return;
		}*/
		switch (spellId) {
			case 33:
				player.getInterfaceManager().openGameTab(7);
				final Item target = player.getInventory().getItem(packetId);
				if (target == null) {
					return;
				}
				if (!checkSpellRequirements(player, 86, true, ASTRAL_RUNE, 2, EARTH_RUNE, 15, NATURE_RUNE, 1)) {
					return;
				}
				Planks plank = Planks.forId(target.getId());
				if (plank == null) {
					player.getPackets().sendGameMessage("You can only cast this spell on a log.");
					return;
				}
				int cost = (int) (plank.getCost() * 0.75);
				if (!player.takeMoney(cost)) {
					player.sendMessage("You need " + Utils.format(cost) + " coins to cast this spell.");
					return;
				}
				player.setNextAnimation(new Animation(6298));
				player.setNextGraphics(new Graphics(1063, 0, 50));
				player.getInventory().deleteItem(plank.getLogId(), 1);
				player.getInventory().addItem(plank.getPlankId(), 1);
				player.getSkills().addXp(Skills.MAGIC, 90);
				player.getLockManagement().lockAll(3000);
				break;
			case 37:
				if (player.getSkills().getLevel(Skills.MAGIC) < 94) {
					player.getPackets().sendGameMessage("Your Magic level is not high enough for this spell.");
					return;
				} else if (player.getSkills().getLevel(Skills.DEFENCE) < 40) {
					player.getPackets().sendGameMessage("You need a Defence level of 40 for this spell");
					return;
				} else if (player.getAttribute("cast_veng", false)) {
					player.sendMessage("You already have vengeance cast.");
					return;
				}
				Long lastVeng = player.getAttribute("LAST_VENG");
				if (lastVeng != null && lastVeng + 30000 > Utils.currentTimeMillis()) {
					player.getPackets().sendGameMessage("You must wait " + (TimeUnit.MILLISECONDS.toSeconds((lastVeng + 30000) - Utils.currentTimeMillis())) + " more seconds to cast vengeance.");
					return;
				}
				if (!checkRunes(player, true, ASTRAL_RUNE, 4, DEATH_RUNE, 2, EARTH_RUNE, 10)) {
					return;
				}
				player.setNextGraphics(new Graphics(726, 0, 100));
				player.setNextAnimation(new Animation(4410));
				player.putAttribute("cast_veng", true);
				player.getAttributes().put("LAST_VENG", Utils.currentTimeMillis());
//				player.getPackets().sendGameMessage("You cast a vengeance.");
				break;
			case 39:
				useHomeTele(player);
				break;
		}
	}

	public static void processAncientSpell(Player player, int spellId) {
		player.stopAll(false);
		boolean miasmic = CombatDefinitions.isMiasmicSpell(spellId);
		if (miasmic && !player.getQuestManager().isFinished(DesertTreasure.class)) {
			player.getDialogueManager().startDialogue(SimpleNPCMessage.class, 1918, "You must complete 'Desert Treasure' to use this spell.", "Check your quest diary to see how to start this quest.");
			return;
		}
		switch (spellId) {
			case 28:
			case 32:
			case 24:
			case 20:
			case 30:
			case 34:
			case 26:
			case 22:
			case 29:
			case 33:
			case 25:
			case 21:
			case 31:
			case 35:
			case 27:
			case 23:
			case 36:
			case 37:
			case 38:
			case 39:
				setCombatSpell(player, spellId);
				break;
			case 40:
				sendAncientTeleportSpell(player, 54, 64, new WorldTile(3099, 9882, 0), LAW_RUNE, 2, FIRE_RUNE, 1, AIR_RUNE, 1);
				break;
			case 41:
				sendAncientTeleportSpell(player, 60, 70, new WorldTile(3360, 3387, 0), LAW_RUNE, 2, SOUL_RUNE, 1);
				break;
			case 42:
				sendAncientTeleportSpell(player, 66, 76, new WorldTile(3492, 3471, 0), LAW_RUNE, 2, BLOOD_RUNE, 1);

				break;
			case 43:
				sendAncientTeleportSpell(player, 72, 82, new WorldTile(3006, 3471, 0), LAW_RUNE, 2, WATER_RUNE, 4);
				break;
			case 44:
				sendAncientTeleportSpell(player, 78, 88, new WorldTile(2990, 3696, 0), LAW_RUNE, 2, FIRE_RUNE, 3, AIR_RUNE, 2);
				break;
			case 45:
				sendAncientTeleportSpell(player, 84, 94, new WorldTile(3217, 3677, 0), LAW_RUNE, 2, SOUL_RUNE, 2);
				break;
			case 46:
				sendAncientTeleportSpell(player, 90, 100, new WorldTile(3288, 3886, 0), LAW_RUNE, 2, BLOOD_RUNE, 2);
				break;
			case 47:
				sendAncientTeleportSpell(player, 96, 106, new WorldTile(2977, 3873, 0), LAW_RUNE, 2, WATER_RUNE, 8);
				break;
			case 48:
				useHomeTele(player);
				break;
		}
	}

	public static final void processNormalSpell(Player player, int spellId, int packetId) {
		player.stopAll(false);
		switch (spellId) {
			case 25: // air strike
			case 28: // water strike
			case 30: // earth strike
			case 32: // fire strike
			case 34: // air bolt
			case 39: // water bolt
			case 42: // earth bolt
			case 45: // fire bolt
			case 49: // air blast
			case 52: // water blast
			case 58: // earth blast
			case 63: // fire blast
			case 70: // air wave
			case 73: // water wave
			case 77: // earth wave
			case 80: // fire wave
			case 99:
			case 84:
			case 87:
			case 89:
			case 91:
			case 36:
			case 55:
			case 81:
			case 66:
			case 67:
			case 68:
				setCombatSpell(player, spellId);
				break;
			case 27: // crossbow bolt enchant
				if (player.getSkills().getLevel(Skills.MAGIC) < 4) {
					player.getPackets().sendGameMessage("Your Magic level is not high enough for this spell.");
					return;
				}
				player.stopAll();
				player.getInterfaceManager().sendInterface(432);
				break;
			case 24:
				useHomeTele(player);
				break;
			case 37: // mobi
				sendNormalTeleportSpell(player, 10, 19, new WorldTile(2413, 2848, 0), LAW_RUNE, 1, WATER_RUNE, 1, AIR_RUNE, 1);
				break;
			case 40: // varrock
				sendNormalTeleportSpell(player, 25, 19, new WorldTile(3212, 3424, 0), FIRE_RUNE, 1, AIR_RUNE, 3, LAW_RUNE, 1);
				break;
			case 43: // lumby
				sendNormalTeleportSpell(player, 31, 41, new WorldTile(3222, 3218, 0), EARTH_RUNE, 1, AIR_RUNE, 3, LAW_RUNE, 1);
				break;
			case 46: // fally
				sendNormalTeleportSpell(player, 37, 48, new WorldTile(2964, 3379, 0), WATER_RUNE, 1, AIR_RUNE, 3, LAW_RUNE, 1);
				break;
			case 51: // camelot
				sendNormalTeleportSpell(player, 45, 55.5, new WorldTile(2757, 3478, 0), AIR_RUNE, 5, LAW_RUNE, 1);
				break;
			case 57: // ardy
				sendNormalTeleportSpell(player, 51, 61, new WorldTile(2664, 3305, 0), WATER_RUNE, 2, LAW_RUNE, 2);
				break;
			case 62: // watch
				sendNormalTeleportSpell(player, 58, 68, new WorldTile(2547, 3113, 2), EARTH_RUNE, 2, LAW_RUNE, 2);
				break;
			case 69: // troll
				sendNormalTeleportSpell(player, 61, 68, new WorldTile(2888, 3674, 0), FIRE_RUNE, 2, LAW_RUNE, 2);
				break;
			case 72: // ape
				sendNormalTeleportSpell(player, 64, 76, new WorldTile(2776, 9103, 0), FIRE_RUNE, 2, WATER_RUNE, 2, LAW_RUNE, 2, 1963, 1);
				break;
		}
	}

	private static void useHomeTele(Player player) {
		sendNormalTeleportSpell(player, 0, 0, GameConstants.START_PLAYER_LOCATION);
	}

	public static final void sendAncientTeleportSpell(Player player, int level, double xp, WorldTile tile, int... runes) {
		sendTeleportSpell(player, 1979, -1, 1681, -1, level, xp, tile, 5, true, MAGIC_TELEPORT, runes);
	}

	public static final void sendNormalTeleportSpell(Player player, int level, double xp, WorldTile tile, int... runes) {
		sendTeleportSpell(player, 8939, 8941, 1576, 1577, level, xp, tile, 3, true, MAGIC_TELEPORT, runes);
	}

	public static final boolean sendItemTeleportSpell(Player player, boolean randomize, int upEmoteId, int upGraphicId, int delay, WorldTile tile) {
		return sendTeleportSpell(player, upEmoteId, -2, upGraphicId, -1, 0, 0, tile, delay, randomize, ITEM_TELEPORT);
	}

	public static final boolean sendTeleportSpell(final Player player, int upEmoteId, final int downEmoteId, int upGraphicId, final int downGraphicId, int level, final double xp, final WorldTile tile, int delay, final boolean randomize, final int teleType, int... runes) {
		if (player.getLockManagement().isAnyLocked()) {
			return false;
		}
		if (player.getSkills().getLevel(Skills.MAGIC) < level) {
			player.getPackets().sendGameMessage("Your Magic level is not high enough for this spell.");
			return false;
		}
		if (!checkRunes(player, false, runes)) {
			return false;
		}
		if (teleType == MAGIC_TELEPORT) {
			if (!player.getControllerManager().processMagicTeleport(tile)) {
				return false;
			}
		} else if (teleType == ITEM_TELEPORT) {
			if (!player.getControllerManager().processItemTeleport(tile)) {
				return false;
			}
		} else if (teleType == OBJECT_TELEPORT) {
			if (!player.getControllerManager().processObjectTeleport(tile)) {
				return false;
			}
		}
		checkRunes(player, true, runes);
		player.putAttribute("teleporting", true);
		player.resetReceivedDamage();
		player.stopAll();
		if (upEmoteId != -1) {
			player.setNextAnimation(new Animation(upEmoteId));
		}
		if (upGraphicId != -1) {
			player.setNextGraphics(new Graphics(upGraphicId));
		}
		if (teleType == MAGIC_TELEPORT) {
			player.getPackets().sendSound(5527, 0, 2);
		}
		player.getLockManagement().lockAll((3 + delay) * 1000);
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				if (ticksPassed == (delay - 1)) {
					WorldTile teleTile = tile;
					if (randomize) {
						// attemps to randomize tile by 4x4 area
						for (int trycount = 0; trycount < 10; trycount++) {
							teleTile = new WorldTile(tile, 2);
							if (World.canMoveNPC(tile.getPlane(), teleTile.getX(), teleTile.getY(), player.getSize())) {
								break;
							}
							teleTile = tile;
						}
					}
					player.setNextWorldTile(teleTile);
					player.getControllerManager().magicTeleported(teleType);
					if (player.getControllerManager().getController() == null) {
						teleControlersCheck(player, teleTile);
					}
					if (xp != 0) {
						player.getSkills().addXp(Skills.MAGIC, xp);
					}
					if (downEmoteId != -1) {
						player.setNextAnimation(new Animation(downEmoteId == -2 ? -1 : downEmoteId));
					}
					if (downGraphicId != -1) {
						player.setNextGraphics(new Graphics(downGraphicId));
					}
					if (teleType == MAGIC_TELEPORT) {
						player.getPackets().sendSound(5524, 0, 2);
						player.setNextFaceWorldTile(new WorldTile(teleTile.getX(), teleTile.getY() - 1, teleTile.getPlane()));
						player.setDirection(6);
					}
					player.getLockManagement().unlockAll();
					player.stopAll();
				}

				if (ticksPassed >= (delay + 1)) {
					player.removeAttribute("teleporting");
					stop();
				}

			}
		}, 1, 0);
		return true;
	}

	public static boolean checkRunes(Player player, boolean delete, int... runes) {
		int weaponId = player.getEquipment().getWeaponId();
		int shieldId = player.getEquipment().getShieldId();
		int runesCount = 0;
		while (runesCount < runes.length) {
			int runeId = runes[runesCount++];
			int ammount = runes[runesCount++];
			if (hasInfiniteRunes(runeId, weaponId, shieldId)) {
				continue;
			}
			if (hasStaffOfLight(weaponId) && Utils.getRandom(8) == 0 && runeId != 21773) {
				continue;
			}
			if (!player.getInventory().containsItem(runeId, ammount)) {
				player.getPackets().sendGameMessage("You do not have enough " + ItemDefinitions.forId(runeId).getName().replace("rune", "Rune") + "s to cast this spell.");
				return false;
			}
		}
		if (delete) {
			runesCount = 0;
			while (runesCount < runes.length) {
				int runeId = runes[runesCount++];
				int ammount = runes[runesCount++];
				if (hasInfiniteRunes(runeId, weaponId, shieldId)) {
					continue;
				}
				player.getInventory().deleteItem(runeId, ammount);
			}
		}
		return true;
	}

	public static void teleControlersCheck(Player player, WorldTile teleTile) {
		if (Wilderness.isAtWild(player)) {
			player.getControllerManager().startController("Wilderness");
		} else if (RequestController.inWarRequest(player)) {
			player.getControllerManager().startController("clan_wars_request");
		} else if (FfaZone.inArea(player)) {
			player.getControllerManager().startController("clan_wars_ffa");
		}
	}

	public static final boolean hasInfiniteRunes(int runeId, int weaponId, int shieldId) {
		if (runeId == AIR_RUNE) {
			if (weaponId == 1381 || weaponId == 21777) // air staff
			{
				return true;
			}
		} else if (runeId == WATER_RUNE) {
			if (weaponId == 1383 || shieldId == 18346) // water staff
			{
				return true;
			}
		} else if (runeId == EARTH_RUNE) {
			if (weaponId == 1385) // earth staff
			{
				return true;
			}
		} else if (runeId == FIRE_RUNE) {
			if (weaponId == 1387) // fire staff
			{
				return true;
			}
		}
		return false;
	}

	public static boolean hasStaffOfLight(int weaponId) {
		return weaponId == 15486 || weaponId == 22207 || weaponId == 22209 || weaponId == 22211 || weaponId == 22213;
	}

	public static void pushLeverTeleport(final Player player, final WorldTile tile) {
		if (!player.getControllerManager().processObjectTeleport(tile)) {
			return;
		}
		player.setNextAnimation(new Animation(2140));
		player.getLockManagement().lockAll();
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				player.getLockManagement().unlockAll();
				Magic.sendObjectTeleportSpell(player, false, tile);
			}
		}, 1);
	}

	public static final void sendObjectTeleportSpell(Player player, boolean randomize, WorldTile tile) {
		sendTeleportSpell(player, 8939, 8941, 1576, 1577, 0, 0, tile, 3, randomize, OBJECT_TELEPORT);
	}

	public static final void sendDelayedObjectTeleportSpell(Player player, int delay, boolean randomize, WorldTile tile) {
		sendTeleportSpell(player, 8939, 8941, 1576, 1577, 0, 0, tile, delay, randomize, OBJECT_TELEPORT);
	}

	public static boolean useTabTeleport(final Player player, final int itemId) {
		if (itemId < 8007 || itemId > 8007 + TABS.length - 1) {
			return false;
		}
		if (useTeleTab(player, TABS[itemId - 8007])) {
			player.getInventory().deleteItem(itemId, 1);
		}
		return true;
	}

	public static boolean useTeleTab(final Player player, final WorldTile tile) {
		if (!player.getControllerManager().processItemTeleport(tile)) {
			return false;
		}
		player.getLockManagement().lockAll();
		player.setNextAnimation(new Animation(9597));
		player.setNextGraphics(new Graphics(1680));
		WorldTasksManager.schedule(new WorldTask() {
			int stage;

			@Override
			public void run() {
				if (stage == 0) {
					player.setNextAnimation(new Animation(4731));
					stage = 1;
				} else if (stage == 1) {
					WorldTile teleTile = tile;
					// attemps to randomize tile by 4x4 area
					for (int trycount = 0; trycount < 10; trycount++) {
						teleTile = new WorldTile(tile, 2);
						if (World.canMoveNPC(tile.getPlane(), teleTile.getX(), teleTile.getY(), player.getSize())) {
							break;
						}
						teleTile = tile;
					}
					player.setNextWorldTile(teleTile);
					player.getControllerManager().magicTeleported(ITEM_TELEPORT);
					if (player.getControllerManager().getController() == null) {
						teleControlersCheck(player, teleTile);
					}
					player.setNextFaceWorldTile(new WorldTile(teleTile.getX(), teleTile.getY() - 1, teleTile.getPlane()));
					player.setDirection(6);
					player.setNextAnimation(new Animation(9013));
					stage = 2;
				} else if (stage == 2) {
					player.resetReceivedDamage();
					player.getLockManagement().unlockAll();
					stop();
				}

			}
		}, 2, 1);
		return true;
	}

	/**
	 * Handles casting a magic spell on an item
	 *
	 * @param player
	 * 		The player
	 * @param item
	 * 		The item it is cast on
	 * @param spellId
	 * 		The id of the spell
	 */
	public static void handleMagicOnItemSpell(Player player, Item item, int spellId) {
		if (player.getLockManagement().isAnyLocked()) {
			return;
		}
		switch (spellId) {
			case 29:
			case 41:
			case 53:
			case 61:
			case 76:
			case 88:
				Enchanting.processMagicEnchantSpell(player, player.getInventory().getItems().getThisItemSlot(item), Enchanting.getJewleryIndex(spellId));
				break;
			case 59: // highalch
			case 38: // lowalch
				player.getActionManager().setAction(new Action() {
					@Override
					public boolean start(Player player) {
						return true;
					}

					@Override
					public boolean process(Player player) {
						return true;
					}

					@Override
					public int processWithDelay(Player player) {
						boolean highAlch = spellId == 59;
						if (!ItemConstants.isTradeable(item) || item.getId() == 995) {
							player.sendMessage("You cannot convert this item into gold!");
							return 1;
						}
						if (!checkSpellRequirements(player, highAlch ? 55 : 21, true, FIRE_RUNE, highAlch ? 5 : 3, NATURE_RUNE, 1)) {
							return 1;
						}
						boolean fireStaff = (player.getEquipment().getItem(Equipment.SLOT_WEAPON) != null && player.getEquipment().getItem(Equipment.SLOT_WEAPON).getName().toLowerCase().contains("fire"));
						player.getLockManagement().lockAll(2000);
						player.getInventory().deleteItem(item.getId(), 1);
						player.setNextAnimation(new Animation(fireStaff ? 9633 : 713));
						player.setNextGraphics(new Graphics(113));

						if (item.getDefinitions().isNoted() && item.getDefinitions().getCertId() != -1) {
							item.setId(item.getDefinitions().getCertId());
						}

						player.getInventory().addItem(995, highAlch ? item.getDefinitions().getHighAlchPrice() : item.getDefinitions().getLowAlchPrice());
						player.getSkills().addXp(Skills.MAGIC, highAlch ? 65 : 31);

						WorldTasksManager.schedule(new WorldTask() {

							@Override
							public void run() {
								player.getInterfaceManager().openGameTab(7);
								stop();
							}
						}, 1);
						return -1;
					}

					@Override
					public void stop(Player player) {

					}
				});
				break;
		}
	}

	public static boolean checkSpellRequirements(Player player, int level, boolean delete, int... runes) {
		if (player.getSkills().getLevel(Skills.MAGIC) < level) {
			player.getPackets().sendGameMessage("Your Magic level is not high enough for this spell.");
			return false;
		}
		return checkRunes(player, delete, runes);
	}

	public static boolean checkSpellLevel(Player player, int level) {
		if (player.getSkills().getLevel(Skills.MAGIC) < level) {
			player.getPackets().sendGameMessage("Your Magic level is not high enough for this spell.");
			return false;
		}
		return true;
	}
}
