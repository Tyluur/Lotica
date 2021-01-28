package com.runescape.network.codec.decoders.handlers;

import com.runescape.cache.loaders.ObjectDefinitions;
import com.runescape.game.GameConstants;
import com.runescape.game.content.ArmourSets.Sets;
import com.runescape.game.content.ItemOnTypeHandler;
import com.runescape.game.content.PartyRoom;
import com.runescape.game.content.economy.exchange.ExchangeManagement;
import com.runescape.game.content.economy.treasure.TreasureTrailHandler;
import com.runescape.game.content.global.minigames.CastleWars;
import com.runescape.game.content.global.minigames.Crucible;
import com.runescape.game.content.global.minigames.FightPits;
import com.runescape.game.content.global.minigames.pest.Lander;
import com.runescape.game.content.global.minigames.warriors.WarriorsGuild;
import com.runescape.game.content.global.wilderness.WildernessObelisk;
import com.runescape.game.content.skills.Woodcutting;
import com.runescape.game.content.skills.Woodcutting.TreeDefinitions;
import com.runescape.game.content.skills.agility.*;
import com.runescape.game.content.skills.cooking.Cooking;
import com.runescape.game.content.skills.cooking.Cooking.Cookables;
import com.runescape.game.content.skills.crafting.JewllerySmithing;
import com.runescape.game.content.skills.hunter.TrapAction;
import com.runescape.game.content.skills.magic.Magic;
import com.runescape.game.content.skills.mining.EssenceMining;
import com.runescape.game.content.skills.mining.EssenceMining.EssenceDefinitions;
import com.runescape.game.content.skills.mining.Mining;
import com.runescape.game.content.skills.mining.Mining.RockDefinitions;
import com.runescape.game.content.skills.runecrafting.Runecrafting;
import com.runescape.game.content.skills.runecrafting.SiphonActionNodes;
import com.runescape.game.content.skills.smithing.Smithing.ForgingBar;
import com.runescape.game.content.skills.smithing.Smithing.ForgingInterface;
import com.runescape.game.content.skills.thieving.Thieving;
import com.runescape.game.event.interaction.InteractionEventManager;
import com.runescape.game.interaction.controllers.impl.FightCaves;
import com.runescape.game.interaction.controllers.impl.FightKiln;
import com.runescape.game.interaction.controllers.impl.Wilderness;
import com.runescape.game.interaction.dialogues.impl.npc.MiningGuildDwarf;
import com.runescape.game.interaction.dialogues.impl.object.ClimbNoEmoteStairs;
import com.runescape.game.interaction.dialogues.impl.skills.WildernessDitch;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.ForceMovement;
import com.runescape.game.world.entity.masks.NewForceMovement;
import com.runescape.game.world.entity.npc.others.PolyporeCreature;
import com.runescape.game.world.entity.player.LockManagement.LockType;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.QuestManager;
import com.runescape.game.world.entity.player.RouteEvent;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.game.world.entity.player.actions.ConstructionAltarAction;
import com.runescape.game.world.entity.player.actions.CowMilkingAction;
import com.runescape.game.world.entity.player.actions.PlayerCombat;
import com.runescape.game.world.item.Item;
import com.runescape.network.stream.InputStream;
import com.runescape.utility.Utils;
import com.runescape.utility.world.ClickOption;
import com.runescape.utility.world.object.ObjectRemoval;
import com.runescape.utility.world.player.PkRank;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;

public final class ObjectHandler {

	private ObjectHandler() {

	}

	public static void handleObjectInteraction(Player player, int option, InputStream stream) {
		if (!player.hasStarted() || !player.clientHasLoadedMapRegion() || player.isDead()) {
			return;
		}

		if (player.getLockManagement().isLocked(LockType.OBJECT_INTERACTION) || player.isFrozen() || player.getEmotesManager().isDoingEmote()) {
			return;
		}

		final boolean forceRun = stream.readUnsignedByte128() == 1;
		final int x = stream.readUnsignedShort128();
		final int id = stream.readInt();
		int y = stream.readUnsignedShortLE();
		if (id == 43526 && (x == 2552 || x == 2551) && y == 3553) {
			y = 3550;
		}

		final WorldTile tile = new WorldTile(x, y, player.getPlane());
		final int regionId = tile.getRegionId();
		if (!player.getMapRegionsIds().contains(regionId)) {
			return;
		}

		if (forceRun) { player.setRun(true); }

		WorldObject mapObject = World.getObjectWithId(tile, id);
		if (mapObject == null || mapObject.getId() != id) {
			return;
		}

		switch (option) {
			case 1:
				handleOption1(player, mapObject);
				break;
			case 2:
				handleOption2(player, mapObject);
				break;
			case 3:
				handleOption3(player, mapObject);
				break;
			case 4:
				handleOptionExamine(player, mapObject);
				break;
			case 5:
				handleOption5(player, mapObject);
				break;
			default:
				System.err.println("Unhandled object option: " + option);
				break;
		}
	}

	private static void handleOption1(final Player player, final WorldObject object) {
		final ObjectDefinitions objectDef = object.getDefinitions();
		final int id = object.getId();
		final int x = object.getX();
		final int y = object.getY();
		if (SiphonActionNodes.siphion(player, object)) {
			return;
		}
		if (predefinedPrimaryInteraction(player, object, id)) { return; }
		Object target = object;
		boolean customized = false;
		if (object.getId() == 2283 && object.matches(3005, 3952, 0)) {
			target = new WorldTile(3005, 3953, 0);
			customized = true;
		} else if (object.getId() == 43529 && object.getY() == 3425) {
			target = new WorldTile(2485, 3421, 3);
			customized = true;
		}/* else if (object.getId() == 43526 && (object.matches(2552, 3550, 0)) || object.matches(2551, 3550, 0)) {
			target = new WorldTile(2552, 3554, 0);
			customized = true;
		}
		if (customized) {
			WorldTile dest = (WorldTile) target;
//			player.addWalkSteps(dest.getX(), dest.getY());
			PlayerFollow.followPathTo(player, (WorldTile) target);
		}
*/
		player.setRouteEvent(new RouteEvent(target, () -> {
			player.stopAll();
			player.faceObject(object);
			if (!player.getControllerManager().processObjectClick1(object)) {
				return;
			}
			if (InteractionEventManager.handleObjectInteraction(player, object, ClickOption.FIRST)) {
				return;
			}
			if (QuestManager.handleObjectInteract(player, object, ClickOption.FIRST)) {
				return;
			}
			if (CastleWars.handleObjects(player, id)) {
				return;
			}
			if (TreasureTrailHandler.handleObject(player, object)) {
				return;
			}
//			if (player.getFarmingManager().isFarming(id, null, 1)) { return; }
			if (TrapAction.isTrap(player, object, id) || TrapAction.isTrap(player, object)) {
				return;
			} else if (id == 2350 && (object.getX() == 3352 && object.getY() == 3417 && object.getPlane() == 0)) {
				player.useStairs(832, new WorldTile(3177, 5731, 0), 1, 2);
			} else if (id == 2353 && (object.getX() == 3177 && object.getY() == 5730 && object.getPlane() == 0)) {
				player.useStairs(828, new WorldTile(3353, 3416, 0), 1, 2);
			} else if (id == 4495 && (object.getX() == 3413 && object.getY() == 3540 && object.getPlane() == 1)) {
				player.useStairs(-1, new WorldTile(3417, 3541, 2), 1, 2);
			} else if (id == 25339) {
				player.useStairs(828, new WorldTile(1778, 5346, 1), 1, 2);
			} else if (id == 25340) {
				player.useStairs(828, new WorldTile(1778, 5346, 0), 1, 2);
			} else if (id == 25336) {
				player.useStairs(828, new WorldTile(1768, 5366, 1), 1, 2);
			} else if (id == 25338) {
				player.useStairs(828, new WorldTile(1772, 5366, 0), 1, 2);
			} else if (id == 4496 && (object.getX() == 3415 && object.getY() == 3540 && object.getPlane() == 2)) {
				player.useStairs(-1, new WorldTile(3412, 3541, 1), 1, 2);
			} else if (id == 11554 || id == 11552) {
				player.getPackets().sendGameMessage("That rock is currently unavailable.");
			} else if (id == 38279) {
				player.getDialogueManager().startDialogue("RunespanPortalD");
			} else if (id >= 14826 && id <= 14831) {
				WildernessObelisk.activateObelisk(id, player);
			} else if (id == 2491) {
				player.getActionManager().setAction(new EssenceMining(object, player.getSkills().getLevel(Skills.MINING) < 30 ? EssenceDefinitions.Rune_Essence : EssenceDefinitions.Pure_Essence));
			} else if (id == 2478) {
				Runecrafting.craftEssence(player, 556, 1, 5, false, 11, 2, 22, 3, 34, 4, 44, 5, 55, 6, 66, 7, 77, 88, 9, 99, 10);
			} else if (id == 2479) {
				Runecrafting.craftEssence(player, 558, 2, 5.5, false, 14, 2, 28, 3, 42, 4, 56, 5, 70, 6, 84, 7, 98, 8);
			} else if (id == 2480) {
				Runecrafting.craftEssence(player, 555, 5, 6, false, 19, 2, 38, 3, 57, 4, 76, 5, 95, 6);
			} else if (id == 2481) {
				Runecrafting.craftEssence(player, 557, 9, 6.5, false, 26, 2, 52, 3, 78, 4);
			} else if (id == 2482) {
				Runecrafting.craftEssence(player, 554, 14, 7, false, 35, 2, 70, 3);
			} else if (id == 2483) {
				Runecrafting.craftEssence(player, 559, 20, 7.5, false, 46, 2, 92, 3);
			} else if (id == 2484) {
				Runecrafting.craftEssence(player, 564, 27, 8, true, 59, 2);
			} else if (id == 2487) {
				Runecrafting.craftEssence(player, 562, 35, 8.5, true, 74, 2);
			} else if (id == 17010) {
				Runecrafting.craftEssence(player, 9075, 40, 8.7, true, 82, 2);
			} else if (id == 2486) {
				Runecrafting.craftEssence(player, 561, 45, 9, true, 91, 2);
			} else if (id == 2485) {
				Runecrafting.craftEssence(player, 563, 50, 9.5, true);
			} else if (id == 2488) {
				Runecrafting.craftEssence(player, 560, 65, 10, true);
			} else if (id == 30624) {
				Runecrafting.craftEssence(player, 565, 77, 10.5, true);
			} else if (id == 2452) {
				int hatId = player.getEquipment().getHatId();
				if (hatId == Runecrafting.AIR_TIARA || hatId == Runecrafting.OMNI_TIARA || player.getInventory().containsItem(1438, 1)) {
					Runecrafting.enterAirAltar(player);
				}
			} else if (id == 2455) {
				int hatId = player.getEquipment().getHatId();
				if (hatId == Runecrafting.EARTH_TIARA || hatId == Runecrafting.OMNI_TIARA || player.getInventory().containsItem(1440, 1)) {
					Runecrafting.enterEarthAltar(player);
				}
			} else if (id == 2456) {
				int hatId = player.getEquipment().getHatId();
				if (hatId == Runecrafting.FIRE_TIARA || hatId == Runecrafting.OMNI_TIARA || player.getInventory().containsItem(1442, 1)) {
					Runecrafting.enterFireAltar(player);
				}
			} else if (id == 2454) {
				int hatId = player.getEquipment().getHatId();
				if (hatId == Runecrafting.WATER_TIARA || hatId == Runecrafting.OMNI_TIARA || player.getInventory().containsItem(1444, 1)) {
					Runecrafting.enterWaterAltar(player);
				}
			} else if (id == 2457) {
				int hatId = player.getEquipment().getHatId();
				if (hatId == Runecrafting.BODY_TIARA || hatId == Runecrafting.OMNI_TIARA || player.getInventory().containsItem(1446, 1)) {
					Runecrafting.enterBodyAltar(player);
				}
			} else if (id == 2453) {
				int hatId = player.getEquipment().getHatId();
				if (hatId == Runecrafting.MIND_TIARA || hatId == Runecrafting.OMNI_TIARA || player.getInventory().containsItem(1448, 1)) {
					Runecrafting.enterMindAltar(player);
				}
			} else if (id == 47120) { // zaros altar
				// recharge if needed
				if (player.getPrayer().getPrayerpoints() < player.getSkills().getLevelForXp(Skills.PRAYER) * 10) {
					player.getLockManagement().lockAll(1200);
					player.setNextAnimation(new Animation(12563));
					player.getPrayer().setPrayerpoints((int) ((player.getSkills().getLevelForXp(Skills.PRAYER) * 10) * 1.15));
					player.getPrayer().refreshPrayerPoints();
				}
				player.getDialogueManager().startDialogue("ZarosAltar");
			} else if (id == 36786) {
				player.getDialogueManager().startDialogue("Banker", 4907);
			} else if (id == 42377 || id == 42378) {
//				player.getDialogueManager().startDialogue("Banker", 2759);
				player.getBank().openBank();
			} else if (id == 42217 || id == 782 || id == 34752) {
				player.getDialogueManager().startDialogue("Banker", 553);
			} else if (id == 57437) {
				player.getBank().openBank();
			} else if (id == 42425 && object.getX() == 3220 && object.getY() == 3222) { // zaros
				// portal
				player.useStairs(10256, new WorldTile(3353, 3416, 0), 4, 5, "And you find yourself into a digsite.");
				player.addWalkSteps(3222, 3223, -1, false);
				player.getPackets().sendGameMessage("You examine portal and it aborves you...");
			} else if (id == 9356) {
				FightCaves.enterFightCaves(player, true);
			} else if (id == 68107) {
				FightKiln.enterFightKiln(player, false);
			} else if (id == 68223) {
				FightPits.enterLobby(player, false);
			} else if (id == 15653) {
				if (World.isSpawnedObject(object) || !WarriorsGuild.canEnter(player)) {
					return;
				}
				player.getLockManagement().lockAll(2000);
				WorldObject opened = new WorldObject(object.getId(), object.getType(), object.getRotation() - 1, object.getX(), object.getY(), object.getPlane());
				World.spawnObjectTemporary(opened, 600);
				player.addWalkSteps(2876, 3542, 2, false);
			} else if (id == 46500 && object.getX() == 3351 && object.getY() == 3415) { // zaros
				// portal
				player.useStairs(-1, new WorldTile(GameConstants.RESPAWN_PLAYER_LOCATION.getX(), GameConstants.RESPAWN_PLAYER_LOCATION.getY(), GameConstants.RESPAWN_PLAYER_LOCATION.getPlane()), 2, 3, "You found your way back to home.");
				player.addWalkSteps(3351, 3415, -1, false);
			} else if (id == 9293) {
				if (player.getSkills().getLevel(Skills.AGILITY) < 70) {
					player.getPackets().sendGameMessage("You need an agility level of 70 to use this obstacle.", true);
					return;
				}
				player.setNextAnimation(new Animation(10580));
				int x1 = player.getX() == 2886 ? 2892 : 2886;
				WorldTasksManager.schedule(new WorldTask() {
					int count = 0;

					@Override
					public void run() {
						player.setNextAnimation(new Animation(844));
						if (count++ == 1) {
							stop();
						}
					}

				}, 0, 0);
				player.setNextForceMovement(new ForceMovement(new WorldTile(x1, 9799, 0), 3, player.getX() == 2886 ? 1 : 3));
				player.useStairs(-1, new WorldTile(x1, 9799, 0), 3, 4);
			} else if (id == 29370 && (object.getX() == 3150 || object.getX() == 3153) && object.getY() == 9906) { // edgeville
				// dungeon
				// cut
				if (player.getSkills().getLevel(Skills.AGILITY) < 53) {
					player.getPackets().sendGameMessage("You need an agility level of 53 to use this obstacle.");
					return;
				}
				final boolean running = player.getRun();
				player.setRunHidden(false);
				player.getLockManagement().lockAll();
				player.addWalkSteps(x == 3150 ? 3155 : 3149, 9906, -1, false);
				player.getPackets().sendGameMessage("You pulled yourself through the pipes.", true);
				WorldTasksManager.schedule(new WorldTask() {
					boolean secondloop;

					@Override
					public void run() {
						if (!secondloop) {
							secondloop = true;
							player.getAppearence().setRenderEmote(295);
						} else {
							player.getAppearence().setRenderEmote(-1);
							player.setRunHidden(running);
							player.getSkills().addXp(Skills.AGILITY, 7);
							player.getLockManagement().unlockAll();
							stop();
						}
					}
				}, 0, 5);
			}
			// start forinthry dungeon
			else if (id == 18341 && object.getX() == 3036 && object.getY() == 10172) {
				player.useStairs(-1, new WorldTile(3039, 3765, 0), 0, 1);
			} else if (id == 20599 && object.getX() == 3038 && object.getY() == 3761) {
				player.useStairs(-1, new WorldTile(3037, 10171, 0), 0, 1);
			} else if (id == 18342 && object.getX() == 3075 && object.getY() == 10057) {
				player.useStairs(-1, new WorldTile(3071, 3649, 0), 0, 1);
			} else if (id == 20600 && object.getX() == 3072 && object.getY() == 3648) {
				player.useStairs(-1, new WorldTile(3077, 10058, 0), 0, 1);
			} else if (id == 8689) {
				player.getActionManager().setAction(new CowMilkingAction());
			} else if (id == 42220) {
				player.useStairs(-1, new WorldTile(3082, 3475, 0), 0, 1);
			}
			// start falador mininig
			else if (id == 30942 && object.getX() == 3019 && object.getY() == 3450) {
				player.useStairs(828, new WorldTile(3020, 9850, 0), 1, 2);
			} else if (id == 6226 && object.getX() == 3019 && object.getY() == 9850) {
				player.useStairs(828, new WorldTile(2996, 9845, 0), 1, 2);
			} else if (id == 30943 && object.getX() == 3059 && object.getY() == 9776) {
				player.useStairs(-1, new WorldTile(3061, 3376, 0), 0, 1);
			} else if (id == 30944 && object.getX() == 3059 && object.getY() == 3376) {
				player.useStairs(-1, new WorldTile(3058, 9776, 0), 0, 1);
			} else if (id == 2112 && object.getX() == 3046 && object.getY() == 9756) {
				if (player.getSkills().getLevelForXp(Skills.MINING) < 60) {
					player.getDialogueManager().startDialogue("SimpleNPCMessage", MiningGuildDwarf.getClosestDwarfID(player), "Sorry, but you need level 60 Mining to go in there.");
					return;
				}
				WorldObject openedDoor = new WorldObject(object.getId(), object.getType(), object.getRotation() - 1, object.getX(), object.getY() + 1, object.getPlane());
				if (World.removeTemporaryObject(object, 1200)) {
					World.spawnTemporaryObject(openedDoor, 1200);
					player.getLockManagement().lockAll(2000);
					player.stopAll();
					player.addWalkSteps(3046, player.getY() > object.getY() ? object.getY() : object.getY() + 1, -1, false);
				}
			} else if (id == 2113) {
				if (player.getSkills().getLevelForXp(Skills.MINING) < 60) {
					player.getDialogueManager().startDialogue("SimpleNPCMessage", MiningGuildDwarf.getClosestDwarfID(player), "Sorry, but you need level 60 Mining to go in there.");
					return;
				}
				player.useStairs(-1, new WorldTile(3021, 9739, 0), 0, 1);
			} else if (id == 6226 && object.getX() == 3019 && object.getY() == 9740) {
				player.useStairs(828, new WorldTile(3019, 3341, 0), 1, 2);
			} else if (id == 6226 && object.getX() == 3019 && object.getY() == 9738) {
				player.useStairs(828, new WorldTile(3019, 3337, 0), 1, 2);
			} else if (id == 6226 && object.getX() == 3018 && object.getY() == 9739) {
				player.useStairs(828, new WorldTile(3017, 3339, 0), 1, 2);
			} else if (id == 6226 && object.getX() == 3020 && object.getY() == 9739) {
				player.useStairs(828, new WorldTile(3021, 3339, 0), 1, 2);
			} else if (id == 30963) {
				player.getBank().openBank();
			} else if (id == 6045) {
				player.getPackets().sendGameMessage("You search the cart but find nothing.");
			} else if (id == 5906) {
				if (player.getSkills().getLevel(Skills.AGILITY) < 42) {
					player.getPackets().sendGameMessage("You need an agility level of 42 to use this obstacle.");
					return;
				}
				player.getLockManagement().lockAll();
				WorldTasksManager.schedule(new WorldTask() {
					int count = 0;

					@Override
					public void run() {
						if (count == 0) {
							player.setNextAnimation(new Animation(2594));
							WorldTile tile = new WorldTile(object.getX() + (object.getRotation() == 2 ? -2 : +2), object.getY(), 0);
							player.setNextForceMovement(new ForceMovement(tile, 4, Utils.getMoveDirection(tile.getX() - player.getX(), tile.getY() - player.getY())));
						} else if (count == 2) {
							WorldTile tile = new WorldTile(object.getX() + (object.getRotation() == 2 ? -2 : +2), object.getY(), 0);
							player.setNextWorldTile(tile);
						} else if (count == 5) {
							player.setNextAnimation(new Animation(2590));
							WorldTile tile = new WorldTile(object.getX() + (object.getRotation() == 2 ? -5 : +5), object.getY(), 0);
							player.setNextForceMovement(new ForceMovement(tile, 4, Utils.getMoveDirection(tile.getX() - player.getX(), tile.getY() - player.getY())));
						} else if (count == 7) {
							WorldTile tile = new WorldTile(object.getX() + (object.getRotation() == 2 ? -5 : +5), object.getY(), 0);
							player.setNextWorldTile(tile);
						} else if (count == 10) {
							player.setNextAnimation(new Animation(2595));
							WorldTile tile = new WorldTile(object.getX() + (object.getRotation() == 2 ? -6 : +6), object.getY(), 0);
							player.setNextForceMovement(new ForceMovement(tile, 4, Utils.getMoveDirection(tile.getX() - player.getX(), tile.getY() - player.getY())));
						} else if (count == 12) {
							WorldTile tile = new WorldTile(object.getX() + (object.getRotation() == 2 ? -6 : +6), object.getY(), 0);
							player.setNextWorldTile(tile);
						} else if (count == 14) {
							stop();
							player.getLockManagement().unlockAll();
						}
						count++;
					}

				}, 0, 0);
				// BarbarianOutpostAgility start
			} else if (id == 20210) {
				BarbarianOutpostAgility.enterObstaclePipe(player, object);
			} else if (id == 43526) {
				BarbarianOutpostAgility.swingOnRopeSwing(player, object);
			} else if (id == 43595 && x == 2550 && y == 3546) {
				BarbarianOutpostAgility.walkAcrossLogBalance(player, object);
			} else if (id == 20211 && x == 2538 && y == 3545) {
				BarbarianOutpostAgility.climbObstacleNet(player, object);
			} else if (id == 2302 && x == 2535 && y == 3547) {
				BarbarianOutpostAgility.walkAcrossBalancingLedge(player, object);
			} else if (id == 1948) {
				BarbarianOutpostAgility.climbOverCrumblingWall(player, object);
			} else if (id == 43533) {
				BarbarianOutpostAgility.runUpWall(player, object);
			} else if (id == 43597) {
				BarbarianOutpostAgility.climbUpWall(player, object);
			} else if (id == 43587) {
				BarbarianOutpostAgility.fireSpringDevice(player, object);
			} else if (id == 43527) {
				BarbarianOutpostAgility.crossBalanceBeam(player, object);
			} else if (id == 43531) {
				BarbarianOutpostAgility.jumpOverGap(player, object);
			} else if (id == 43532) {
				BarbarianOutpostAgility.slideDownRoof(player, object);
				// Wilderness course start
			} else if (id == 2297) {
				WildernessAgility.walkAcrossLogBalance(player, object);
			} else if (id == 37704) {
				WildernessAgility.jumpSteppingStones(player, object);
			} else if (id == 2288) {
				WildernessAgility.enterWildernessPipe(player, object.getX(), object.getY());
			} else if (id == 2328) {
				WildernessAgility.climbUpWall(player, object);
			} else if (id == 2283) {
				WildernessAgility.swingOnRopeSwing(player, object);
			} else if (id == 2309) {
				WildernessAgility.enterWildernessCourse(player);
			} else if (id == 2307 || id == 2308) {
				WildernessAgility.exitWildernessCourse(player);
			} else if (id == 9311 || id == 9312) {
				Shortcuts.handleEdgevilleUnderwallTunnel(player, object);
			}
			// rock living caverns
			else if (id == 45077) {
				player.getLockManagement().lockAll();
				if (player.getX() != object.getX() || player.getY() != object.getY()) {
					player.addWalkSteps(object.getX(), object.getY(), -1, false);
				}
				WorldTasksManager.schedule(new WorldTask() {

					private int count;

					@Override
					public void run() {
						if (count == 0) {
							player.setNextFaceWorldTile(new WorldTile(object.getX() - 1, object.getY(), 0));
							player.setNextAnimation(new Animation(12216));
							player.getLockManagement().unlockAll();
						} else if (count == 2) {
							player.setNextWorldTile(new WorldTile(3651, 5122, 0));
							player.setNextFaceWorldTile(new WorldTile(3651, 5121, 0));
							player.setNextAnimation(new Animation(12217));
						} else if (count == 5) {
							player.getLockManagement().unlockAll();
							stop();
						}
						count++;
					}

				}, 1, 0);
			} else if (id == 45076) {
				player.getActionManager().setAction(new Mining(object, RockDefinitions.LRC_Gold_Ore));
			} else if (id == 5999) {
				player.getActionManager().setAction(new Mining(object, RockDefinitions.LRC_Coal_Ore));
			} else if (id == 45078) {
				player.useStairs(2413, new WorldTile(3012, 9832, 0), 2, 2);
			} else if (id == 45079) {
				player.getBank().openDepositBox();
			}
			// champion guild
			else if (id == 24357 && object.getX() == 3188 && object.getY() == 3355) {
				player.useStairs(-1, new WorldTile(3189, 3354, 1), 0, 1);
			} else if (id == 24359 && object.getX() == 3188 && object.getY() == 3355) {
				player.useStairs(-1, new WorldTile(3189, 3358, 0), 0, 1);
			} else if (id == 1805 && object.getX() == 3191 && object.getY() == 3363) {
				WorldObject openedDoor = new WorldObject(object.getId(), object.getType(), object.getRotation() - 1, object.getX(), object.getY(), object.getPlane());
				if (World.removeTemporaryObject(object, 1200)) {
					World.spawnTemporaryObject(openedDoor, 1200);
					player.getLockManagement().lockAll(2000);
					player.stopAll();
					player.addWalkSteps(3191, player.getY() >= object.getY() ? object.getY() - 1 : object.getY(), -1, false);
					if (player.getY() >= object.getY()) {
						player.getDialogueManager().startDialogue("SimpleNPCMessage", 198, "Greetings bolt adventurer. Welcome to the guild of", "Champions.");
					}
				}
			}
			// start of varrock dungeon
			else if (id == 29355 && object.getX() == 3230 && object.getY() == 9904) // varrock
			// dungeon
			// climb
			// to
			// bear
			{
				player.useStairs(828, new WorldTile(3229, 3503, 0), 1, 2);
			} else if (id == 24264) {
				player.useStairs(833, new WorldTile(3229, 9904, 0), 1, 2);
			} else if (id == 24366) {
				player.useStairs(828, new WorldTile(3237, 3459, 0), 1, 2);
			} else if (id == 882 && object.getX() == 3237 && object.getY() == 3458) {
				player.useStairs(833, new WorldTile(3237, 9858, 0), 1, 2);
			} else if (id == 29355 && object.getX() == 3097 && object.getY() == 9867) // edge
			// dungeon
			// climb
			{
				player.useStairs(828, new WorldTile(3096, 3468, 0), 1, 2);
			} else if (id == 26934) {
				player.useStairs(833, new WorldTile(3097, 9868, 0), 1, 2);
			} else if (id == 29355 && object.getX() == 3088 && object.getY() == 9971) {
				player.useStairs(828, new WorldTile(3087, 3571, 0), 1, 2);
			} else if (id == 65453) {
				player.useStairs(833, new WorldTile(3089, 9971, 0), 1, 2);
			} else if (id == 12389 && object.getX() == 3116 && object.getY() == 3452) {
				player.useStairs(833, new WorldTile(3117, 9852, 0), 1, 2);
			} else if (id == 29355 && object.getX() == 3116 && object.getY() == 9852) {
				player.useStairs(833, new WorldTile(3115, 3452, 0), 1, 2);
			} else if (id == 2295) {
				GnomeAgility.walkGnomeLog(player);
			} else if (id == 2285) {
				GnomeAgility.climbGnomeObstacleNet(player);
			} else if (id == 35970) {
				GnomeAgility.climbUpGnomeTreeBranch(player);
			} else if (id == 2312) {
				GnomeAgility.walkGnomeRope(player);
			} else if (id == 4059) {
				GnomeAgility.walkBackGnomeRope(player);
			} else if (id == 2314) {
				GnomeAgility.climbDownGnomeTreeBranch(player);
			} else if (id == 2286) {
				GnomeAgility.climbGnomeObstacleNet2(player);
			} else if (id == 43544 || id == 43543) {
				GnomeAgility.enterGnomePipe(player, object.getX(), object.getY());
			} else if (id == 43528) {
				GnomeAgility.climbUpTree(player);
			} else if (id == 43529) {
				GnomeAgility.preSwing(player, object);
			} else if (id == 43539) {
				GnomeAgility.jumpDown(player, object);
			} else if (id == 64125) {
				int value = player.getVarsManager().getBitValue(10232);
				if (value == 7) {
					return;
				}
				player.getLockManagement().lockAll(2000);
				player.setNextAnimation(new Animation(15460));
				player.getInventory().addItem(22445, 1);
				if (value == 0) {
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							int value = player.getVarsManager().getBitValue(10232);
							player.getVarsManager().sendVarBit(10232, value - 1);
							if (value == 1) {
								stop();
							}
						}
					}, 9, 9);
				}
				player.getVarsManager().sendVarBit(10232, value + 1);
			} else if (id == 64360 && x == 4629 && y == 5453) {
				PolyporeCreature.useStairs(player, new WorldTile(4629, 5451, 2), true);
			} else if (id == 64361 && x == 4629 && y == 5452) {
				PolyporeCreature.useStairs(player, new WorldTile(4629, 5454, 3), false);
			} else if (id == 64359 && x == 4632 && y == 5443) {
				PolyporeCreature.useStairs(player, new WorldTile(4632, 5443, 1), true);
			} else if (id == 64361 && x == 4632 && y == 5442) {
				PolyporeCreature.useStairs(player, new WorldTile(4632, 5444, 2), false);
			} else if (id == 64359 && x == 4632 && y == 5409) {
				PolyporeCreature.useStairs(player, new WorldTile(4632, 5409, 2), true);
			} else if (id == 64361 && x == 4633 && y == 5409) {
				PolyporeCreature.useStairs(player, new WorldTile(4631, 5409, 3), false);
			} else if (id == 64359 && x == 4642 && y == 5389) {
				PolyporeCreature.useStairs(player, new WorldTile(4642, 5389, 1), true);
			} else if (id == 64361 && x == 4643 && y == 5389) {
				PolyporeCreature.useStairs(player, new WorldTile(4641, 5389, 2), false);
			} else if (id == 64359 && x == 4652 && y == 5388) {
				PolyporeCreature.useStairs(player, new WorldTile(4652, 5388, 0), true);
			} else if (id == 64362 && x == 4652 && y == 5387) {
				PolyporeCreature.useStairs(player, new WorldTile(4652, 5389, 1), false);
			} else if (id == 64359 && x == 4691 && y == 5469) {
				PolyporeCreature.useStairs(player, new WorldTile(4691, 5469, 2), true);
			} else if (id == 64361 && x == 4691 && y == 5468) {
				PolyporeCreature.useStairs(player, new WorldTile(4691, 5470, 3), false);
			} else if (id == 64359 && x == 4689 && y == 5479) {
				PolyporeCreature.useStairs(player, new WorldTile(4689, 5479, 1), true);
			} else if (id == 64361 && x == 4689 && y == 5480) {
				PolyporeCreature.useStairs(player, new WorldTile(4689, 5478, 2), false);
			} else if (id == 64359 && x == 4698 && y == 5459) {
				PolyporeCreature.useStairs(player, new WorldTile(4698, 5459, 2), true);
			} else if (id == 64361 && x == 4699 && y == 5459) {
				PolyporeCreature.useStairs(player, new WorldTile(4697, 5459, 3), false);
			} else if (id == 64359 && x == 4705 && y == 5460) {
				PolyporeCreature.useStairs(player, new WorldTile(4704, 5461, 1), true);
			} else if (id == 64361 && x == 4705 && y == 5461) {
				PolyporeCreature.useStairs(player, new WorldTile(4705, 5459, 2), false);
			} else if (id == 64359 && x == 4718 && y == 5467) {
				PolyporeCreature.useStairs(player, new WorldTile(4718, 5467, 0), true);
			} else if (id == 64361 && x == 4718 && y == 5466) {
				PolyporeCreature.useStairs(player, new WorldTile(4718, 5468, 1), false);
			} else if (Wilderness.isDitch(id)) {// wild ditch
				WildernessDitch.hopDitch(player, object);
			} else if (id == 42611) {// Magic Portal
				player.getDialogueManager().startDialogue("MagicPortal");
			} else if (id == 27254) {// Edgeville portal
				player.getPackets().sendGameMessage("You enter the portal...");
				player.useStairs(10584, new WorldTile(3087, 3488, 0), 2, 3, "..and are transported to Edgeville.");
				player.addWalkSteps(1598, 4506, -1, false);
			} else if (id == 12202) {// mole entrance
				if (!player.getInventory().containsItem(952, 1)) {
					player.getPackets().sendGameMessage("You need a spade to dig this.");
					return;
				}
				if (player.getX() != object.getX() || player.getY() != object.getY()) {
					player.getLockManagement().lockAll();
					player.addWalkSteps(object.getX(), object.getY());
					WorldTasksManager.schedule(new WorldTask() {
						@Override
						public void run() {
							InventoryOptionsHandler.dig(player);
						}

					}, 1);
				} else {
					InventoryOptionsHandler.dig(player);
				}
			} else if (id == 12230 && object.getX() == 1752 && object.getY() == 5136) {// mole
				// exit
				player.setNextWorldTile(new WorldTile(2986, 3316, 0));
			} else if (id == 15522) {// portal sign
				if (player.withinDistance(new WorldTile(1598, 4504, 0), 1)) {// PORTAL
					// 1
					player.getInterfaceManager().sendInterface(327);
					player.getPackets().sendIComponentText(327, 13, "Edgeville");
					player.getPackets().sendIComponentText(327, 14, "This portal will take you to edgeville. There " + "you can multi pk once past the wilderness ditch.");
				}
				if (player.withinDistance(new WorldTile(1598, 4508, 0), 1)) {// PORTAL
					// 2
					player.getInterfaceManager().sendInterface(327);
					player.getPackets().sendIComponentText(327, 13, "Mage Bank");
					player.getPackets().sendIComponentText(327, 14, "This portal will take you to the mage bank. " + "The mage bank is a 1v1 deep wilderness area.");
				}
				if (player.withinDistance(new WorldTile(1598, 4513, 0), 1)) {// PORTAL
					// 3
					player.getInterfaceManager().sendInterface(327);
					player.getPackets().sendIComponentText(327, 13, "Magic's Portal");
					player.getPackets().sendIComponentText(327, 14, "This portal will allow you to teleport to areas that " + "will allow you to change your magic spell book.");
				}
			} else if (id == 38811 || id == 37929) {// corp beast
				if (object.getX() == 2971 && object.getY() == 4382) {
					player.getInterfaceManager().sendInterface(650);
				} else if (object.getX() == 2918 && object.getY() == 4382) {
					player.stopAll();
					player.setNextWorldTile(new WorldTile(player.getX() == 2921 ? 2917 : 2921, player.getY(), player.getPlane()));
				}
			} else if (id == 37928 && object.getX() == 2883 && object.getY() == 4370) {
				player.stopAll();
				player.setNextWorldTile(new WorldTile(3214, 3782, 0));
				player.getControllerManager().startController("Wilderness");
			} else if (id == 38815 && object.getX() == 3209 && object.getY() == 3780 && object.getPlane() == 0) {
				if (player.getSkills().getLevelForXp(Skills.WOODCUTTING) < 37 || player.getSkills().getLevelForXp(Skills.MINING) < 45 || player.getSkills().getLevelForXp(Skills.SUMMONING) < 23 || player.getSkills().getLevelForXp(Skills.FIREMAKING) < 47 || player.getSkills().getLevelForXp(Skills.PRAYER) < 55) {
					player.getPackets().sendGameMessage("You need 23 Summoning, 37 Woodcutting, 45 Mining, 47 Firemaking and 55 Prayer to enter this dungeon.");
					return;
				}
				player.stopAll();
				player.setNextWorldTile(new WorldTile(2885, 4372, 2));
				player.getControllerManager().forceStop();
			} else if (id == 48803 && player.isKalphiteLairSetted()) {
				player.setNextWorldTile(new WorldTile(3508, 9494, 0));
			} else if (id == 48802 && player.isKalphiteLairEntranceSetted()) {
				player.setNextWorldTile(new WorldTile(3483, 9510, 2));
			} else if (id == 3829) {
				if (object.getX() == 3483 && object.getY() == 9510) {
					player.useStairs(828, new WorldTile(3226, 3108, 0), 1, 2);
				}
			} else if (id == 3832) {
				if (object.getX() == 3508 && object.getY() == 9494) {
					player.useStairs(828, new WorldTile(3509, 9496, 2), 1, 2);
				}
			} else if (id == 9369) {
				player.getControllerManager().startController("FightPits");
			} else if (id == 1817 && object.getX() == 2273 && object.getY() == 4680) { // kbd
				// lever
				Magic.pushLeverTeleport(player, new WorldTile(3067, 10254, 0));
			} else if (id == 1816 && object.getX() == 3067 && object.getY() == 10252) { // kbd
				// out
				// lever
				Magic.pushLeverTeleport(player, new WorldTile(2273, 4681, 0));
			} else if (id == 32015 && object.getX() == 3069 && object.getY() == 10256) { // kbd
				// stairs
				player.useStairs(828, new WorldTile(3017, 3848, 0), 1, 2);
				player.getControllerManager().startController("Wilderness");
			} else if (id == 1765 && object.getX() == 3017 && object.getY() == 3849) { // kbd
				// out
				// stairs
				player.stopAll();
				player.setNextWorldTile(new WorldTile(3069, 10255, 0));
				player.getControllerManager().forceStop();
			} else if (id == 14315) {
				if (Lander.canEnter(player, 0)) { return; }
			} else if (id == 25631) {
				if (Lander.canEnter(player, 1)) { return; }
			} else if (id == 25632) {
				if (Lander.canEnter(player, 2)) { return; }
			} else if (id == 5959) {
				Magic.pushLeverTeleport(player, new WorldTile(2539, 4712, 0));
			} else if (id == 5960) {
				Magic.pushLeverTeleport(player, new WorldTile(3090, 3956, 0));
			} else if (id == 1815) {
				Magic.pushLeverTeleport(player, new WorldTile(2561, 3311, 0));
			} else if (id == 9706) {
				Magic.pushLeverTeleport(player, new WorldTile(3105, 3951, 0));
			} else if (id == 9707) {
				Magic.pushLeverTeleport(player, new WorldTile(3105, 3956, 0));
			} else if (id == 62675) {
				player.getCutscenesManager().play("DTPreview");
			} else if (id == 62681) {
				player.getDominionTower().viewScoreBoard();
			} else if (id == 62678 || id == 62679) {
				player.getDominionTower().openModes();
			} else if (id == 62688) {
				player.getDialogueManager().startDialogue("DTClaimRewards");
			} else if (id == 62677) {
				player.getDominionTower().talkToFace();
			} else if (id == 62680) {
				player.getDominionTower().openBankChest();
			} else if (id == 48797) {
				player.useStairs(-1, new WorldTile(3877, 5526, 1), 0, 1);
			} else if (id == 48798) {
				player.useStairs(-1, new WorldTile(3246, 3198, 0), 0, 1);
			} else if (id == 48678 && x == 3858 && y == 5533) {
				player.useStairs(-1, new WorldTile(3861, 5533, 0), 0, 1);
			} else if (id == 48678 && x == 3858 && y == 5543) {
				player.useStairs(-1, new WorldTile(3861, 5543, 0), 0, 1);
			} else if (id == 48677 && x == 3858 && y == 5543) {
				player.useStairs(-1, new WorldTile(3856, 5543, 1), 0, 1);
			} else if (id == 48677 && x == 3858 && y == 5533) {
				player.useStairs(-1, new WorldTile(3856, 5533, 1), 0, 1);
			} else if (id == 48679) {
				player.useStairs(-1, new WorldTile(3875, 5527, 1), 0, 1);
			} else if (id == 48688) {
				player.useStairs(-1, new WorldTile(3972, 5565, 0), 0, 1);
			} else if (id == 48683) {
				player.useStairs(-1, new WorldTile(3868, 5524, 0), 0, 1);
			} else if (id == 48682) {
				player.useStairs(-1, new WorldTile(3869, 5524, 0), 0, 1);
			} else if (id == 62676) { // dominion exit
				player.useStairs(-1, new WorldTile(3374, 3093, 0), 0, 1);
			} else if (id == 62674) { // dominion entrance
				player.useStairs(-1, new WorldTile(3744, 6405, 0), 0, 1);
			} else if (id == 3192 || id == 54019 || id == 54020) {
				PkRank.showRanks(player);
			} else if (id == 65349) {
				player.useStairs(-1, new WorldTile(3044, 10325, 0), 0, 1);
			} else if (id == 32048 && object.getX() == 3043 && object.getY() == 10328) {
				player.useStairs(-1, new WorldTile(3045, 3927, 0), 0, 1);
			} else if (id == 61190 || id == 61191 || id == 61192 || id == 61193) {
				if (objectDef.containsOption(0, "Chop down")) {
					player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.NORMAL));
				}
			} else if (id == 20573) {
				player.getControllerManager().startController("RefugeOfFear");
			}
			// crucible
			else if (id == 67050) {
				player.useStairs(-1, new WorldTile(3359, 6110, 0), 0, 1);
			} else if (id == 67053) {
				player.useStairs(-1, new WorldTile(3120, 3519, 0), 0, 1);
			} else if (id == 67051) {
				player.getDialogueManager().startDialogue("Marv", false);
			} else if (id == 67052) {
				Crucible.enterCrucibleEntrance(player);
			} else if (Mining.getRockDefinitions(id) != null) {
				player.getActionManager().setAction(new Mining(object, Mining.getRockDefinitions(id)));
			} else {
				switch (id) {
					case 2092:
						player.getActionManager().setAction(new Mining(object, RockDefinitions.Iron_Ore));
						break;
					case 2094:
						player.getActionManager().setAction(new Mining(object, RockDefinitions.Tin_Ore));
						break;
					case 2090:
						player.getActionManager().setAction(new Mining(object, RockDefinitions.Copper_Ore));
						break;
					case 2100:
						player.getActionManager().setAction(new Mining(object, RockDefinitions.Silver_Ore));
						break;
					case 2098:
						player.getActionManager().setAction(new Mining(object, RockDefinitions.Gold_Ore));
						break;
					case 2102:
						player.getActionManager().setAction(new Mining(object, RockDefinitions.Mithril_Ore));
						break;
					case 2104:
						player.getActionManager().setAction(new Mining(object, RockDefinitions.Adamant_Ore));
						break;
				}
				switch (objectDef.name.toLowerCase()) {
					case "trapdoor":
					case "manhole":
						if (objectDef.containsOption(0, "Open")) {
							WorldObject openedHole = new WorldObject(object.getId() + 1, object.getType(), object.getRotation(), object.getX(), object.getY(), object.getPlane());
							// if (World.removeTemporaryObject(object, 60000,
							// true)) {
							player.faceObject(openedHole);
							World.spawnTemporaryObject(openedHole, 60000);
							// }
						}
						break;
					case "closed chest":
						if (objectDef.containsOption(0, "Open")) {
							player.setNextAnimation(new Animation(536));
							player.getLockManagement().lockAll(2000);
							WorldObject openedChest = new WorldObject(object.getId() + 1, object.getType(), object.getRotation(), object.getX(), object.getY(), object.getPlane());
							// if (World.removeTemporaryObject(object, 60000,
							// true)) {
							player.faceObject(openedChest);
							World.spawnTemporaryObject(openedChest, 60000);
							// }
						}
						break;
					case "open chest":
						if (objectDef.containsOption(0, "Search")) {
							player.getPackets().sendGameMessage("You search the chest but find nothing.");
						}
						break;
					case "spiderweb":
						if (object.getRotation() == 2) {
							player.getLockManagement().lockAll(2000);
							if (Utils.getRandom(1) == 0) {
								player.addWalkSteps(player.getX(), player.getY() < y ? object.getY() + 2 : object.getY() - 1, -1, false);
								player.getPackets().sendGameMessage("You squeeze though the web.");
							} else {
								player.getPackets().sendGameMessage("You fail to squeeze though the web; perhaps you should try again.");
							}
						}
						break;
					case "web":
						if (objectDef.containsOption(0, "Slash") && PlayerCombat.canSlashWeb(player)) {
							player.setNextAnimation(new Animation(PlayerCombat.getWeaponAttackEmote(player.getEquipment().getWeaponId(), player.getCombatDefinitions().getAttackStyle())));
							slashWeb(player, object);
						}
						break;
					case "anvil":
						if (objectDef.containsOption(0, "Smith")) {
							ForgingBar bar = ForgingBar.getBar(player);
							if (bar != null) {
								ForgingInterface.sendSmithingInterface(player, bar);
							} else {
								player.getPackets().sendGameMessage("You have no bars which you have smithing level to use.");
							}
						}
						break;
					case "spinning wheel":
						player.getDialogueManager().startDialogue("SpinningD");
						break;
					case "potter's wheel":
						player.getDialogueManager().startDialogue("PotteryWheel");
						break;
					case "pottery oven":
						player.getDialogueManager().startDialogue("PotteryFurnace");
						break;
					case "bank deposit box":
						if (objectDef.containsOption(0, "Deposit")) {
							player.getBank().openDepositBox();
						}
						break;
					case "bank":
					case "bank chest":
					case "bank table":
					case "bank booth":
					case "counter":
						if (objectDef.containsOption(0, "Bank") || objectDef.containsOption(0, "Use")) {
							player.getBank().openBank();
						}
						break;
					// Woodcutting start
					case "tree":
						if (objectDef.containsOption(0, "Chop down")) {
							player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.NORMAL));
						}
						break;
					case "evergreen":
						if (objectDef.containsOption(0, "Chop down")) {
							player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.EVERGREEN));
						}
						break;
					case "dead tree":
						if (objectDef.containsOption(0, "Chop down")) {
							player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.DEAD));
						}
						break;
					case "oak":
						if (objectDef.containsOption(0, "Chop down")) {
							player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.OAK));
						}
						break;
					case "willow":
						if (objectDef.containsOption(0, "Chop down")) {
							player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.WILLOW));
						}
						break;
					case "mahogany":
						if (objectDef.containsOption(0, "Chop down")) {
							player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.MAHOGANY));
						}
						break;
					case "teak":
						if (objectDef.containsOption(0, "Chop down")) {
							player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.TEAK));
						}
						break;
					case "maple tree":
						if (objectDef.containsOption(0, "Chop down")) {
							player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.MAPLE));
						}
						break;
					case "ivy":
						if (objectDef.containsOption(0, "Chop")) {
							player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.IVY));
						}
						break;
					case "yew":
						if (objectDef.containsOption(0, "Chop down")) {
							player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.YEW));
						}
						break;
					case "magic tree":
						if (objectDef.containsOption(0, "Chop down")) {
							player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.MAGIC));
						}
						break;
					case "cursed magic tree":
						if (objectDef.containsOption(0, "Chop down")) {
							player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.CURSED_MAGIC));
						}
						break;
					// Woodcutting end
					case "gate":
					case "large door":
					case "metal door":
						if (object.getType() == 0 && objectDef.containsOption(0, "Open")) {
							if (!handleGate(player, object)) {
								handleDoor(player, object);
							}
						}
						break;
					case "door":
						if (object.getType() == 0 && (objectDef.containsOption(0, "Open") || objectDef.containsOption(0, "Unlock"))) {
							handleDoor(player, object);
						}
						break;
					case "ladder":
						handleLadder(player, object, 1);
						break;
					case "staircase":
						handleStaircases(player, object, 1);
						break;
					case "small obelisk":
						if (objectDef.containsOption(0, "Renew-points")) {
							int summonLevel = player.getSkills().getLevelForXp(Skills.SUMMONING);
							if (player.getSkills().getLevel(Skills.SUMMONING) < summonLevel) {
								player.getLockManagement().lockAll(3000);
								player.setNextAnimation(new Animation(8502));
								player.getSkills().setLevel(Skills.SUMMONING, summonLevel);
								player.getPackets().sendGameMessage("You have recharged your Summoning points.", true);
							} else {
								player.getPackets().sendGameMessage("You already have full Summoning points.");
							}
						}
						break;
					case "altar":
					case "gorilla statue":
						if (objectDef.containsOption(0, "Pray") || objectDef.containsOption(0, "Pray-at")) {
							final int maxPrayer = player.getSkills().getLevelForXp(Skills.PRAYER) * 10;
							if (player.getPrayer().getPrayerpoints() < maxPrayer) {
								player.getLockManagement().lockAll();
								player.getPrayer().restorePrayer(maxPrayer);
								player.getPackets().sendGameMessage("You pray to the gods...", true);
								player.setNextAnimation(new Animation(645));
								player.getPrayer().restorePrayer(maxPrayer);
								WorldTasksManager.schedule(new WorldTask() {
									@Override
									public void run() {
										player.getPackets().sendGameMessage("...and recharged your prayer.", true);
										player.getLockManagement().unlockAll();
									}
								}, 2);
							} else {
								player.getPackets().sendGameMessage("You already have full prayer.");
							}
							if (id == 6552) {
								player.getDialogueManager().startDialogue("AncientAltar");
							}
						}
						break;
					default:
						player.getPackets().sendGameMessage("Nothing interesting happens.");
						break;
				}
			}
			if (GameConstants.DEBUG) {
				System.out.println("clicked 1 at object id : " + id + ", " + object.getX() + ", " + object.getY() + ", " + object.getPlane() + ", " + object.getType() + ", " + object.getRotation() + ", " + object.getDefinitions().name);
			}
		}, true));
	}

	private static boolean predefinedPrimaryInteraction(Player player, WorldObject object, int id) {
		if (id == 43581 || (id >= 4550 && id <= 4559) || id == 11231) {
			if (id == 11231 && object.withinDistance(player, 1)) {
				player.stopAll();
				player.faceObject(object);
				InteractionEventManager.handleObjectInteraction(player, object, ClickOption.FIRST);
			} else {
				player.setRouteEvent(new RouteEvent(object, () -> {
					player.stopAll();
					// unreachable agility objects exception
					player.faceObject(object);
					if (id == 43581) {
						GnomeAgility.runGnomeBoard(player, object);
					} else if (id >= 4550 && id <= 4559) {
						if (!Agility.hasLevel(player, 35)) {
							return;
						}
						if (object.withinDistance(player, 2)) {
							if (!Agility.hasLevel(player, 35)) {
								return;
							}
							player.setNextForceMovement(new NewForceMovement(player, 1, object, 2, Utils.getFaceDirection(object.getX() - player.getX(), object.getY() - player.getY())));
							player.useStairs(-1, object, 1, 2);
							player.setNextAnimation(new Animation(769));
							player.getSkills().addXp(Skills.AGILITY, 2);
						}
					} else if (id == 11231) {
						InteractionEventManager.handleObjectInteraction(player, object, ClickOption.FIRST);
					}
				}, true));
			}
			return true;
		}
		return false;
	}

	private static void handleOption2(final Player player, final WorldObject object) {
		final ObjectDefinitions objectDef = object.getDefinitions();
		final int id = object.getId();
		player.setRouteEvent(new RouteEvent(object, () -> {
			player.stopAll();
			player.faceObject(object);
			if (!player.getControllerManager().processObjectClick2(object)) {
				return;
			}
			if (InteractionEventManager.handleObjectInteraction(player, object, ClickOption.SECOND)) {
				return;
			}
//			if (player.getFarmingManager().isFarming(id, null, 2)) { return; }
			if (QuestManager.handleObjectInteract(player, object, ClickOption.SECOND)) {
				return;
			} else if (object.getDefinitions().name.equalsIgnoreCase("furnace")) {
				player.getDialogueManager().startDialogue("SmeltingD", object);
			} else if (id == 17010) {
				player.getDialogueManager().startDialogue("LunarAltar");
			} else if (id == 62677) {
				player.getDominionTower().openRewards();
			} else if (id == 62688) {
				player.getDialogueManager().startDialogue("SimpleMessage", "You have a Dominion Factor of " + player.getDominionTower().getDominionFactor() + ".");
			} else if (id == 68107) {
				FightKiln.enterFightKiln(player, true);
			} else if (id == 34384 || id == 34383 || id == 14011 || id == 7053 || id == 34387 || id == 34386 || id == 34385) {
				Thieving.handleStalls(player, object);
			} else if (id == 2418) {
				PartyRoom.openPartyChest(player);
			} else if (id == 2646) {
				World.removeTemporaryObject(object, 50000);
				player.getInventory().addItem(1779, 1);
				// crucible
			} else if (id == 67051) {
				player.getDialogueManager().startDialogue("Marv", true);
			} else {
				switch (objectDef.name.toLowerCase()) {
					case "cabbage":
						if (objectDef.containsOption(1, "Pick") && player.getInventory().addItem(1965, 1)) {
							player.setNextAnimation(new Animation(827));
							player.getLockManagement().lockAll(2000);
							World.removeTemporaryObject(object, 60000);
						}
						break;
					case "bank":
					case "bank chest":
					case "bank booth":
					case "bank table":
					case "counter":
						if (objectDef.containsOption(1, "Use-quickly") || objectDef.containsOption(1, "Bank")) {
							player.getBank().openBank();
						}
						break;
					case "gates":
					case "gate":
					case "metal door":
						if (object.getType() == 0 && objectDef.containsOption(1, "Open")) {
							handleGate(player, object);
						}
						break;
					case "door":
						if (object.getType() == 0 && objectDef.containsOption(1, "Open")) {
							handleDoor(player, object);
						}
						break;
					case "ladder":
						handleLadder(player, object, 2);
						break;
					case "staircase":
						handleStaircases(player, object, 2);
						break;
					case "spinning wheel":
						player.getDialogueManager().startDialogue("SpinningD");
						break;
					default:
						player.getPackets().sendGameMessage("Nothing interesting happens.");
						break;
				}
			}
			if (GameConstants.DEBUG) {
				System.out.println("clicked 2 at object id : " + id + ", " + object.getX() + ", " + object.getY() + ", " + object.getPlane());
			}
		}, true));
	}

	private static void handleOption3(final Player player, final WorldObject object) {
		final ObjectDefinitions objectDef = object.getDefinitions();
		final int id = object.getId();
		player.setRouteEvent(new RouteEvent(object, () -> {
			player.stopAll();
			player.faceObject(object);
			if (!player.getControllerManager().processObjectClick3(object)) {
				return;
			}
			if (InteractionEventManager.handleObjectInteraction(player, object, ClickOption.THIRD)) {
				return;
			}
//			if (player.getFarmingManager().isFarming(id, null, 3)) { return; }
			if (QuestManager.handleObjectInteract(player, object, ClickOption.THIRD)) {
				return;
			}
			switch (objectDef.name.toLowerCase()) {
				case "gate":
				case "metal door":
					if (object.getType() == 0 && objectDef.containsOption(2, "Open")) {
						handleGate(player, object);
					}
					break;
				case "door":
					if (object.getType() == 0 && objectDef.containsOption(2, "Open")) {
						handleDoor(player, object);
					}
					break;
				case "ladder":
					handleLadder(player, object, 3);
					break;
				case "staircase":
					handleStaircases(player, object, 3);
					break;
				case "counter":
					if (objectDef.containsOption(2, "Collect")) {
						ExchangeManagement.openCollectionBox(player);
					}
					break;
				default:
					player.getPackets().sendGameMessage("Nothing interesting happens.");
					break;
			}
			if (GameConstants.DEBUG) {
				System.out.println("cliked 3 at object id : " + id + ", " + object.getX() + ", " + object.getY() + ", " + object.getPlane() + ", ");
			}
		}, true));
	}

	private static void handleOption5(final Player player, final WorldObject object) {
		final ObjectDefinitions objectDef = object.getDefinitions();
		final int id = object.getId();
		player.setRouteEvent(new RouteEvent(object, () -> {
			player.stopAll();
			player.faceObject(object);
			if (!player.getControllerManager().processObjectClick5(object)) {
				return;
			}
			if (InteractionEventManager.handleObjectInteraction(player, object, ClickOption.FIFTH)) {
				return;
			}
			if (QuestManager.handleObjectInteract(player, object, ClickOption.FIFTH)) {
				return;
			}
			switch (objectDef.name.toLowerCase()) {
				default:
					player.getPackets().sendGameMessage("Nothing interesting happens.");
					break;
			}
			if (GameConstants.DEBUG) {
				System.out.println("cliked 5 at object id : " + id + ", " + object.getX() + ", " + object.getY() + ", " + object.getPlane() + ", ");
			}
		}, true));
	}

	private static void handleOptionExamine(final Player player, final WorldObject object) {
		player.getPackets().sendGameMessage("It's an " + object.getDefinitions().name + ".");
		if (player.getAttribute("removing_objects", false)) {
			try {
				PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(ObjectRemoval.NONSPAWNING_OBJECTS_FILE, true)));
				out.println(object.getId() + " " + object.getX() + " " + object.getY() + " " + object.getPlane());
				out.close();
				World.removeObject(object);
				System.out.println("Added " + object.getId() + " to be removed from spawns.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (GameConstants.DEBUG) {
			System.out.println(object);
			player.getPackets().sendGameMessage(object.toString());
		}
	}

	private static boolean handleGate(Player player, WorldObject object) {
		if (World.isSpawnedObject(object)) {
			return false;
		}
		if (object.getRotation() == 0) {

			boolean south = true;
			WorldObject otherDoor = World.getObjectWithType(new WorldTile(object.getX(), object.getY() + 1, object.getPlane()), object.getType());
			if (otherDoor == null || otherDoor.getRotation() != object.getRotation() || otherDoor.getType() != object.getType() || !otherDoor.getDefinitions().name.equalsIgnoreCase(object.getDefinitions().name)) {
				otherDoor = World.getObjectWithType(new WorldTile(object.getX(), object.getY() - 1, object.getPlane()), object.getType());
				if (otherDoor == null || otherDoor.getRotation() != object.getRotation() || otherDoor.getType() != object.getType() || !otherDoor.getDefinitions().name.equalsIgnoreCase(object.getDefinitions().name)) {
					return false;
				}
				south = false;
			}
			WorldObject openedDoor1 = new WorldObject(object.getId(), object.getType(), object.getRotation() + 1, object.getX(), object.getY(), object.getPlane());
			WorldObject openedDoor2 = new WorldObject(otherDoor.getId(), otherDoor.getType(), otherDoor.getRotation() + 1, otherDoor.getX(), otherDoor.getY(), otherDoor.getPlane());
			if (south) {
				openedDoor1.moveLocation(-1, 0, 0);
				openedDoor1.setRotation(3);
				openedDoor2.moveLocation(-1, 0, 0);
			} else {
				openedDoor1.moveLocation(-1, 0, 0);
				openedDoor2.moveLocation(-1, 0, 0);
				openedDoor2.setRotation(3);
			}

			if (World.removeTemporaryObject(object, 60000) && World.removeTemporaryObject(otherDoor, 60000)) {
				player.faceObject(openedDoor1);
				World.spawnTemporaryObject(openedDoor1, 60000);
				World.spawnTemporaryObject(openedDoor2, 60000);
				return true;
			}
		} else if (object.getRotation() == 2) {

			boolean south = true;
			WorldObject otherDoor = World.getObjectWithType(new WorldTile(object.getX(), object.getY() + 1, object.getPlane()), object.getType());
			if (otherDoor == null || otherDoor.getRotation() != object.getRotation() || otherDoor.getType() != object.getType() || !otherDoor.getDefinitions().name.equalsIgnoreCase(object.getDefinitions().name)) {
				otherDoor = World.getObjectWithType(new WorldTile(object.getX(), object.getY() - 1, object.getPlane()), object.getType());
				if (otherDoor == null || otherDoor.getRotation() != object.getRotation() || otherDoor.getType() != object.getType() || !otherDoor.getDefinitions().name.equalsIgnoreCase(object.getDefinitions().name)) {
					return false;
				}
				south = false;
			}
			WorldObject openedDoor1 = new WorldObject(object.getId(), object.getType(), object.getRotation() + 1, object.getX(), object.getY(), object.getPlane());
			WorldObject openedDoor2 = new WorldObject(otherDoor.getId(), otherDoor.getType(), otherDoor.getRotation() + 1, otherDoor.getX(), otherDoor.getY(), otherDoor.getPlane());
			if (south) {
				openedDoor1.moveLocation(1, 0, 0);
				openedDoor2.setRotation(1);
				openedDoor2.moveLocation(1, 0, 0);
			} else {
				openedDoor1.moveLocation(1, 0, 0);
				openedDoor1.setRotation(1);
				openedDoor2.moveLocation(1, 0, 0);
			}
			if (World.removeTemporaryObject(object, 60000) && World.removeTemporaryObject(otherDoor, 60000)) {
				player.faceObject(openedDoor1);
				World.spawnTemporaryObject(openedDoor1, 60000);
				World.spawnTemporaryObject(openedDoor2, 60000);
				return true;
			}
		} else if (object.getRotation() == 3) {

			boolean right = true;
			WorldObject otherDoor = World.getObjectWithType(new WorldTile(object.getX() - 1, object.getY(), object.getPlane()), object.getType());
			if (otherDoor == null || otherDoor.getRotation() != object.getRotation() || otherDoor.getType() != object.getType() || !otherDoor.getDefinitions().name.equalsIgnoreCase(object.getDefinitions().name)) {
				otherDoor = World.getObjectWithType(new WorldTile(object.getX() + 1, object.getY(), object.getPlane()), object.getType());
				if (otherDoor == null || otherDoor.getRotation() != object.getRotation() || otherDoor.getType() != object.getType() || !otherDoor.getDefinitions().name.equalsIgnoreCase(object.getDefinitions().name)) {
					return false;
				}
				right = false;
			}
			WorldObject openedDoor1 = new WorldObject(object.getId(), object.getType(), object.getRotation() + 1, object.getX(), object.getY(), object.getPlane());
			WorldObject openedDoor2 = new WorldObject(otherDoor.getId(), otherDoor.getType(), otherDoor.getRotation() + 1, otherDoor.getX(), otherDoor.getY(), otherDoor.getPlane());
			if (right) {
				openedDoor1.moveLocation(0, -1, 0);
				openedDoor2.setRotation(0);
				openedDoor1.setRotation(2);
				openedDoor2.moveLocation(0, -1, 0);
			} else {
				openedDoor1.moveLocation(0, -1, 0);
				openedDoor1.setRotation(0);
				openedDoor2.setRotation(2);
				openedDoor2.moveLocation(0, -1, 0);
			}
			if (World.removeTemporaryObject(object, 60000) && World.removeTemporaryObject(otherDoor, 60000)) {
				player.faceObject(openedDoor1);
				World.spawnTemporaryObject(openedDoor1, 60000);
				World.spawnTemporaryObject(openedDoor2, 60000);
				return true;
			}
		} else if (object.getRotation() == 1) {

			boolean right = true;
			WorldObject otherDoor = World.getObjectWithType(new WorldTile(object.getX() - 1, object.getY(), object.getPlane()), object.getType());
			if (otherDoor == null || otherDoor.getRotation() != object.getRotation() || otherDoor.getType() != object.getType() || !otherDoor.getDefinitions().name.equalsIgnoreCase(object.getDefinitions().name)) {
				otherDoor = World.getObjectWithType(new WorldTile(object.getX() + 1, object.getY(), object.getPlane()), object.getType());
				if (otherDoor == null || otherDoor.getRotation() != object.getRotation() || otherDoor.getType() != object.getType() || !otherDoor.getDefinitions().name.equalsIgnoreCase(object.getDefinitions().name)) {
					return false;
				}
				right = false;
			}
			WorldObject openedDoor1 = new WorldObject(object.getId(), object.getType(), object.getRotation() + 1, object.getX(), object.getY(), object.getPlane());
			WorldObject openedDoor2 = new WorldObject(otherDoor.getId(), otherDoor.getType(), otherDoor.getRotation() + 1, otherDoor.getX(), otherDoor.getY(), otherDoor.getPlane());
			if (right) {
				openedDoor1.moveLocation(0, 1, 0);
				openedDoor1.setRotation(0);
				openedDoor2.moveLocation(0, 1, 0);
			} else {
				openedDoor1.moveLocation(0, 1, 0);
				openedDoor2.setRotation(0);
				openedDoor2.moveLocation(0, 1, 0);
			}
			if (World.removeTemporaryObject(object, 60000) && World.removeTemporaryObject(otherDoor, 60000)) {
				player.faceObject(openedDoor1);
				World.spawnTemporaryObject(openedDoor1, 60000);
				World.spawnTemporaryObject(openedDoor2, 60000);
				return true;
			}
		}
		return false;
	}

	public static boolean handleDoor(Player player, WorldObject object, long timer) {
		if (World.isSpawnedObject(object)) {
			return false;
		}
		WorldObject openedDoor = new WorldObject(object.getId(), object.getType(), object.getRotation() + 1, object.getX(), object.getY(), object.getPlane());
		if (object.getRotation() == 0) {
			openedDoor.moveLocation(-1, 0, 0);
		} else if (object.getRotation() == 1) {
			openedDoor.moveLocation(0, 1, 0);
		} else if (object.getRotation() == 2) {
			openedDoor.moveLocation(1, 0, 0);
		} else if (object.getRotation() == 3) {
			openedDoor.moveLocation(0, -1, 0);
		}
		if (World.removeTemporaryObject(object, timer)) {
			player.faceObject(openedDoor);
			World.spawnTemporaryObject(openedDoor, timer);
			return true;
		}
		return false;
	}

	private static boolean handleDoor(Player player, WorldObject object) {
		return handleDoor(player, object, 60000);
	}

	private static boolean handleStaircases(Player player, WorldObject object, int optionId) {
		String option = object.getDefinitions().getOption(optionId);
		if (option.equalsIgnoreCase("Climb-up")) {
			climbUpStairs(player, object);
		} else if (option.equalsIgnoreCase("Climb-down")) {
			climbDownStairs(player, object);
		} else if (option.equalsIgnoreCase("Climb")) {
			if (player.getPlane() == 3 || player.getPlane() == 0) {
				return false;
			}
			player.getDialogueManager().startDialogue(ClimbNoEmoteStairs.class, object);
		} else {
			return false;
		}
		return false;
	}

	public static void climbUpStairs(Player player, WorldObject object) {
		if (player.getPlane() == 3) {
			return;
		}
		if (object.getId() == 66796 && object.getX() == 2839 && object.getPlane() == 1) {
			player.useStairs(828, new WorldTile(2841, 3534, 2), 0, 1);
		} else if (object.getId() == 1738 && object.getWorldTile().matches(2839, 3533, 0)) {
			player.useStairs(828, new WorldTile(2841, 3534, 1), 0, 1);
		} else if (object.getId() == 38012 && object.getWorldTile().matches(2839, 3533, 1)) {
			player.useStairs(828, new WorldTile(2840, 3535, 2), 0, 1);
		} else if (object.getId() == 4493 && object.getWorldTile().matches(3434, 3537, 0)) {
			player.useStairs(-1, new WorldTile(3433, 3538, 1), 0, 1);
		} else {
			player.sendMessage("This path leads nowhere important...");
		}
//		player.useStairs(-1, new WorldTile(player.getX(), player.getY(), player.getPlane() + 1), 0, 1);
	}

	public static void climbDownStairs(Player player, WorldObject object) {
		if (player.getPlane() == 0) {
			return;
		}
		if (object.getId() == 15638 && object.getWorldTile().matches(2840, 3534, 2)) {
			player.useStairs(828, new WorldTile(2841, 3534, 1), 0, 1);
		} else if (object.getId() == 38012 && object.getWorldTile().matches(2839, 3533, 1)) {
			player.useStairs(828, new WorldTile(2841, 3534, 0), 0, 1);
		} else if (object.getId() == 4494 && object.getWorldTile().matches(3434, 3537, 1)) {
			player.useStairs(-1, new WorldTile(3438, 3537, 0), 0, 1);
		} else {
			player.sendMessage("This path leads nowhere important...");
		}
//		player.useStairs(-1, new WorldTile(player.getX(), player.getY(), player.getPlane() - 1), 0, 1);
	}

	private static boolean handleLadder(Player player, WorldObject object, int optionId) {
		String option = object.getDefinitions().getOption(optionId);
		if (object.matches(2884, 9797, 0)) {
			return false;
		}
		if (option.equalsIgnoreCase("Climb-up")) {
			if (player.getPlane() == 3) {
				return false;
			}
			player.useStairs(828, new WorldTile(player.getX(), player.getY(), player.getPlane() + 1), 1, 2);
		} else if (option.equalsIgnoreCase("Climb-down")) {
			if (player.getPlane() == 0) {
				return false;
			}
			player.useStairs(828, new WorldTile(player.getX(), player.getY(), player.getPlane() - 1), 1, 2);
		} else if (option.equalsIgnoreCase("Climb")) {
			if (player.getPlane() == 3 || player.getPlane() == 0) {
				return false;
			}
			player.getDialogueManager().startDialogue("ClimbEmoteStairs", new WorldTile(player.getX(), player.getY(), player.getPlane() + 1), new WorldTile(player.getX(), player.getY(), player.getPlane() - 1), "Climb up the ladder.", "Climb down the ladder.", 828);
		} else {
			return false;
		}
		return true;
	}

	public static void handleItemOnObject(final Player player, InputStream stream) {
		if (!player.hasStarted() || !player.clientHasLoadedMapRegion() || player.isDead()) {
			return;
		}
		long currentTime = Utils.currentTimeMillis();
		if (player.getLockManagement().isLocked(LockType.ITEM_INTERACTION) || player.getEmotesManager().getNextEmoteEnd() >= currentTime) {
			return;
		}
		stream.readUnsignedByteC();
		final int y = stream.readUnsignedShortLE();
		final int itemSlot = stream.readUnsignedShortLE();
		final int interfaceHash = stream.readIntLE();
		final int interfaceId = interfaceHash >> 16;
		final int itemId = stream.readUnsignedShortLE128();
		final int x = stream.readUnsignedShortLE();
		final int id = stream.readInt();
		final WorldTile tile = new WorldTile(x, y, player.getPlane());
		int regionId = tile.getRegionId();
		if (!player.getMapRegionsIds().contains(regionId)) {
			return;
		}
		WorldObject mapObject = World.getObjectWithId(tile, id);
		if (mapObject == null || mapObject.getId() != id) {
			return;
		}
		final WorldObject object = !player.isAtDynamicRegion() ? mapObject : new WorldObject(id, mapObject.getType(), mapObject.getRotation(), x, y, player.getPlane());
		final Item item = player.getInventory().getItem(itemSlot);
		if (player.isDead() || Utils.getInterfaceDefinitionsSize() <= interfaceId) {
			return;
		}
		if (!player.getInterfaceManager().containsInterface(interfaceId)) {
			return;
		}
		if (item == null || item.getId() != itemId) {
			return;
		}
		player.stopAll(false); // false
		final ObjectDefinitions objectDef = object.getDefinitions();
		player.setRouteEvent(new RouteEvent(object, () -> {
			player.faceObject(object);
			if (!player.getControllerManager().handleItemOnObject(object, item)) {
				return;
			}
			if (ItemOnTypeHandler.handleItemOnObject(player, object, item)) {
				return;
			}
			if (ConstructionAltarAction.handleBoneOnAltar(player, object, item)) {
				return;
			}
			if (object.getId() == 26969) {
				Sets set = Sets.forId(itemId);
				if (set == null) {
					return;
				}
				player.getInventory().deleteItem(itemId, 1);
				for (int setItem : set.getItems()) {
					player.getInventory().addItemDrop(setItem, 1);
				}
			} else if (object.getDefinitions().name.toLowerCase().contains("furnace") && item.getId() == 2353) {
				player.getDialogueManager().startDialogue("CannonBallD");
				return;
			} else if (itemId == 1438 && object.getId() == 2452) {
				Runecrafting.enterAirAltar(player);
			} else if (itemId == 1440 && object.getId() == 2455) {
				Runecrafting.enterEarthAltar(player);
			} else if (itemId == 1442 && object.getId() == 2456) {
				Runecrafting.enterFireAltar(player);
			} else if (itemId == 1444 && object.getId() == 2454) {
				Runecrafting.enterWaterAltar(player);
			} else if (itemId == 1446 && object.getId() == 2457) {
				Runecrafting.enterBodyAltar(player);
			} else if (itemId == 1448 && object.getId() == 2453) {
				Runecrafting.enterMindAltar(player);
			}/* else if (player.getFarmingManager().isFarming(object.getId(), item, 0)) {
				return;
			} */ else if (object.getId() == 733 || object.getId() == 64729) {
				if (PlayerCombat.canSlashWeb(player)) {
					player.setNextAnimation(new Animation(PlayerCombat.getWeaponAttackEmote(-1, 0)));
					slashWeb(player, object);
				}
			} else if (object.getId() == 48803 && itemId == 954) {
				if (player.isKalphiteLairSetted()) {
					return;
				}
				player.getInventory().deleteItem(954, 1);
				player.setKalphiteLair();
			} else if (object.getId() == 48802 && itemId == 954) {
				if (player.isKalphiteLairEntranceSetted()) {
					return;
				}
				player.getInventory().deleteItem(954, 1);
				player.setKalphiteLairEntrance();
			} else {
				switch (objectDef.name.toLowerCase()) {
					case "anvil":
						ForgingBar bar = ForgingBar.forId(itemId);
						if (bar != null) {
							ForgingInterface.sendSmithingInterface(player, bar);
						}
						break;
					case "fire":
					case "range":
					case "cooking range":
					case "stove":
						Cookables cook = Cooking.isCookingSkill(item);
						if (cook != null) {
							player.getDialogueManager().startDialogue("CookingD", cook, object);
							return;
						}
						player.getDialogueManager().startDialogue("SimpleMessage", "You can't cook that on a " + (objectDef.name.equals("Fire") ? "fire" : "range") + ".");
						break;
					case "furnace":
					case "lava furnace":
						if (item.getId() == 2357) {
							JewllerySmithing.openInterface(player);
						}
						break;
					default:
						player.getPackets().sendGameMessage("Nothing interesting happens.");
						break;
				}
				if (GameConstants.DEBUG) {
					System.out.println("Item on object: " + object.getId());
				}
			}
			if (GameConstants.DEBUG) { System.out.println("Item on object: " + object.getId()); }
		}, true));
	}

	private static void slashWeb(Player player, WorldObject object) {
		if (Utils.getRandom(1) == 0) {
			World.spawnTemporaryObject(new WorldObject(object.getId() + 1, object.getType(), object.getRotation(), object.getX(), object.getY(), object.getPlane()), 60000);
			player.getPackets().sendGameMessage("You slash through the web!");
		} else {
			player.getPackets().sendGameMessage("You fail to cut through the web.");
		}
	}
}
