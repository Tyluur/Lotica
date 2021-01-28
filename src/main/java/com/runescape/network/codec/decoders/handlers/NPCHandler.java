package com.runescape.network.codec.decoders.handlers;

import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.game.GameConstants;
import com.runescape.game.content.PlayerLook;
import com.runescape.game.content.economy.exchange.ExchangeManagement;
import com.runescape.game.content.economy.treasure.TreasureTrailHandler;
import com.runescape.game.content.skills.fishing.Fishing;
import com.runescape.game.content.skills.fishing.Fishing.FishingSpots;
import com.runescape.game.content.skills.hunter.FlyingEntityHunter;
import com.runescape.game.content.skills.mining.LivingMineralMining;
import com.runescape.game.content.skills.mining.MiningBase;
import com.runescape.game.content.skills.runecrafting.SiphonActionCreatures;
import com.runescape.game.content.skills.thieving.PickPocketAction;
import com.runescape.game.content.skills.thieving.PickPocketableNPC;
import com.runescape.game.event.interaction.InteractionEventManager;
import com.runescape.game.event.interaction.button.Scrollable;
import com.runescape.game.interaction.dialogues.DialogueHandler;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.ForceTalk;
import com.runescape.game.world.entity.npc.Drop;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.familiar.impl.Familiar;
import com.runescape.game.world.entity.npc.others.FireSpirit;
import com.runescape.game.world.entity.npc.others.LivingRock;
import com.runescape.game.world.entity.npc.pet.Pet;
import com.runescape.game.world.entity.npc.slayer.Strykewyrm;
import com.runescape.game.world.entity.player.LockManagement.LockType;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.QuestManager;
import com.runescape.game.world.entity.player.RouteEvent;
import com.runescape.network.stream.InputStream;
import com.runescape.utility.ChatColors;
import com.runescape.utility.Utils;
import com.runescape.utility.external.gson.loaders.ExchangePriceLoader;
import com.runescape.utility.external.gson.loaders.NPCDataLoader;
import com.runescape.utility.external.gson.loaders.NPCSpawnLoader;
import com.runescape.utility.external.gson.resource.NPCData;
import com.runescape.utility.world.ClickOption;
import com.runescape.workers.game.core.CoresManager;

import java.util.*;
import java.util.stream.Collectors;

public class NPCHandler {

	public static void handleNPCInteraction(Player player, int option, InputStream stream) {
		if (!player.hasStarted() || !player.clientHasLoadedMapRegion() || player.isDead()) {
			return;
		}
		if (player.getLockManagement().isLocked(LockType.NPC_INTERACTION) || player.isFrozen() || player.getEmotesManager().isDoingEmote()) {
			return;
		}
		boolean forceRun = stream.readByte128() == 1;
		int npcIndex = stream.readUnsignedShort128();
		final NPC npc = World.getNPCs().get(npcIndex);
		if (npc == null || npc.isCantInteract() || npc.isDead() || npc.hasFinished() || !player.getMapRegionsIds().contains(npc.getRegionId())) {
			return;
		}
		if (forceRun) {
			player.setRun(true);
		}
		switch (option) {
			case 1:
				handleOption1(player, npc);
				break;
			case 2:
				handleOption2(player, npc);
				break;
			case 3:
				handleOption3(player, npc);
				break;
			case 4:
				handleOption4(player, npc);
				break;
			case 5:
				handleExamine(player, npc);
				break;
			default:
				System.err.println("Unhandled object option: " + option);
				break;
		}
	}

	public static void handleOption1(final Player player, NPC npc) {
		player.stopAll(false);
		if (SiphonActionCreatures.siphon(player, npc)) {
			return;
		}
		Object target = npc;
		if (npc.getName().equalsIgnoreCase("banker")) {
			Optional<WorldObject> booth = getClosestBankBooth(npc);
			if (booth.isPresent()) { target = booth.get(); }
		}
		player.setRouteEvent(new RouteEvent(target, () -> {
			npc.resetWalkSteps();
			player.faceEntity(npc);
			FishingSpots spot = FishingSpots.forId(npc.getId() | 1 << 24);
			if (spot != null) {
				player.getActionManager().setAction(new Fishing(spot, npc));
				return; // its a spot, they wont face us
			} else if (npc.getId() >= 8837 && npc.getId() <= 8839) {
				player.getActionManager().setAction(new LivingMineralMining((LivingRock) npc));
				return;
			}
			npc.faceEntity(player);
			if (!player.getControllerManager().processNPCClick1(npc)) {
				return;
			}
			if (InteractionEventManager.handleNPCInteraction(player, npc, ClickOption.FIRST)) {
				return;
			}
			if (QuestManager.handleNPCInteract(player, npc, ClickOption.FIRST)) {
				return;
			}
			if (TreasureTrailHandler.handleNPC(player, npc)) {
				return;
			}
			String dialogueName = npc.getName().replaceAll(" ", "_");
			if (DialogueHandler.getDialogue(dialogueName) != null) {
				player.getDialogueManager().startDialogue(dialogueName, npc.getId());
				return;
			}
			if (npc.getId() == 2619 || npc.getId() == 6362) {
				player.getDialogueManager().startDialogue("Banker", npc.getId());
			} else if (npc.getId() == 5532) {
				player.getDialogueManager().startDialogue("SorceressGardenNPCs", npc);
			} else if (npc.getId() == 5563) {
				player.getDialogueManager().startDialogue("SorceressGardenNPCs", npc);
			} else if (npc.getId() == 2824 || npc.getId() == 1041) {
				player.getDialogueManager().startDialogue("TanningD", npc.getId());
			} else if (npc.getId() == 15451 && npc instanceof FireSpirit) {
				FireSpirit spirit = (FireSpirit) npc;
				spirit.giveReward(player);
			} else if (npc.getId() == 949) {
				player.getDialogueManager().startDialogue("QuestGuide", npc.getId(), null);
			} else if (npc.getId() == 5157 || npc.getId() == 1765 || npc.getId() == 43 || npc.getId() == 5160 || npc.getId() == 5161 || npc.getId() == 5156) {
				final int npcId = npc.getId();
				if (Utils.getRandom(2) == 0) {
					npc.setNextForceTalk(new ForceTalk("Baa!"));
					npc.playSound(756, 1);
					npc.addWalkSteps(npcId, npcId, 4, true);
					npc.setRun(true);
					player.getPackets().sendGameMessage("The sheep runs away from you.");
				} else if (player.getInventory().containsItem(1735, 1)) {
					player.playSound(761, 1);
					player.getInventory().addItem(1737, 1);
					player.getPackets().sendGameMessage("You shear the sheep of it's fleece.");
					player.setNextAnimation(new Animation(893));
					npc.transformIntoNPC(5149);
					CoresManager.FAST_EXECUTOR.schedule(new TimerTask() {
						@Override
						public void run() {
							npc.transformIntoNPC(npcId);
						}
					}, 10000);
				} else {
					player.getPackets().sendGameMessage("You need a pair of shears to shear the sheep.");
				}
			} else if (npc.getId() >= 1 && npc.getId() <= 6 || npc.getId() >= 7875 && npc.getId() <= 7884) {
				player.getDialogueManager().startDialogue("Man", npc.getId());
			} else if (npc.getId() == 198) {
				player.getDialogueManager().startDialogue("Guildmaster", npc.getId());
			} else if (npc.getId() == 9462) {
				Strykewyrm.handleStomping(player, npc);
			} else if (npc.getId() == 9707) {
				player.getDialogueManager().startDialogue("FremennikShipmaster", npc.getId(), true);
			} else if (npc.getId() == 9708) {
				player.getDialogueManager().startDialogue("FremennikShipmaster", npc.getId(), false);
			} else if (npc.getId() == 6537) {
				player.getDialogueManager().startDialogue("SetSkills", npc.getId());
			} else if (npc.getId() == 2676) {
				player.getDialogueManager().startDialogue("MakeOverMage", npc.getId(), 0);
			} else if (npc.getId() == 598) {
				player.getDialogueManager().startDialogue("Hairdresser", npc.getId());
			} else if (npc.getId() == 548) {
				player.getDialogueManager().startDialogue("Thessalia", npc.getId());
			} else if (npc.getId() == 659) {
				player.getDialogueManager().startDialogue("PartyPete");
			} else if (npc.getId() == 579) {
				player.getDialogueManager().startDialogue("DrogoDwarf", npc.getId());
			} else if (npc.getId() == 582) // dwarves general store
			{
				player.getDialogueManager().startDialogue("GeneralStore", npc.getId(), 31);
			} else if (npc.getId() == 528 || npc.getId() == 529) // edge
			{
				player.getDialogueManager().startDialogue("GeneralStore", npc.getId(), 1);
			} else if (npc.getId() == 522 || npc.getId() == 523) // varrock
			{
				player.getDialogueManager().startDialogue("GeneralStore", npc.getId(), 8);
			} else if (npc.getId() == 520 || npc.getId() == 521) // lumbridge
			{
				player.getDialogueManager().startDialogue("GeneralStore", npc.getId(), 4);
			} else if (npc.getName().toLowerCase().contains("impling")) {
				FlyingEntityHunter.captureFlyingEntity(player, npc);
			} else if (npc.getId() == 594) {
				player.getDialogueManager().startDialogue("Nurmof", npc.getId());
			} else if (npc.getId() == 665) {
				player.getDialogueManager().startDialogue("BootDwarf", npc.getId());
			} else if (npc.getId() == 382 || npc.getId() == 3294 || npc.getId() == 4316) {
				player.getDialogueManager().startDialogue("MiningGuildDwarf", npc.getId(), false);
			} else if (npc.getId() == 3295) {
				player.getDialogueManager().startDialogue("MiningGuildDwarf", npc.getId(), true);
			} else if (npc.getId() == 537) {
				player.getDialogueManager().startDialogue("Scavvo", npc.getId());
			} else if (npc.getId() == 536) {
				player.getDialogueManager().startDialogue("Valaine", npc.getId());
			} else if (npc.getId() == 4563) // Crossbow Shop
			{
				player.getDialogueManager().startDialogue("Hura", npc.getId());
			} else if (npc.getId() == 2617) {
				player.getDialogueManager().startDialogue("TzHaarMejJal", npc.getId());
			} else if (npc.getId() == 2618) {
				player.getDialogueManager().startDialogue("TzHaarMejKah", npc.getId());
			} else if (npc.getId() == 15149) {
				player.getDialogueManager().startDialogue("MasterOfFear", 0);
			} else if (npc instanceof Pet) {
				Pet pet = (Pet) npc;
				if (pet != player.getPet()) {
					player.getPackets().sendGameMessage("This isn't your pet.");
					return;
				}
				player.setNextAnimation(new Animation(827));
				pet.pickup();
			} else {
				player.getPackets().sendGameMessage("Nothing interesting happens.");
				if (GameConstants.DEBUG) {
					System.out.println("cliked 1 at npc id : " + npc.getId() + ", " + npc.getX() + ", " + npc.getY() + ", " + npc.getPlane());
				}
			}
		}, true));
	}

	public static void handleOption2(final Player player, NPC npc) {
		if (player.getLockManagement().isLocked(LockType.NPC_INTERACTION)) {
			return;
		}
		player.stopAll(false);
		Object target = npc;
		if (npc.getName().equalsIgnoreCase("banker")) {
			Optional<WorldObject> booth = getClosestBankBooth(npc);
			if (booth.isPresent()) { target = booth.get(); }
		}
		player.setRouteEvent(new RouteEvent(target, () -> {
			npc.resetWalkSteps();
			player.faceEntity(npc);
			FishingSpots spot = FishingSpots.forId(npc.getId() | (2 << 24));
			if (spot != null) {
				player.getActionManager().setAction(new Fishing(spot, npc));
				return;
			}
			PickPocketableNPC pocket = PickPocketableNPC.get(npc.getId());
			if (pocket != null) {
				player.getActionManager().setAction(new PickPocketAction(npc, pocket));
				return;
			}
			if (npc instanceof Familiar) {
				if (npc.getDefinitions().hasOption("store")) {
					if (player.getFamiliar() != npc) {
						player.getPackets().sendGameMessage("That isn't your familiar.");
						return;
					}
					player.getFamiliar().store();
				} else if (npc.getDefinitions().hasOption("cure")) {
					if (player.getFamiliar() != npc) {
						player.getPackets().sendGameMessage("That isn't your familiar.");
						return;
					}
					if (!player.getPoison().isPoisoned()) {
						player.getPackets().sendGameMessage("Your arent poisoned or diseased.");
						return;
					} else {
						player.getFamiliar().drainSpecial(2);
						player.addPoisonImmune(120);
					}
				}
				return;
			}
			npc.faceEntity(player);
			if (!player.getControllerManager().processNPCClick2(npc)) {
				return;
			}
			if (InteractionEventManager.handleNPCInteraction(player, npc, ClickOption.SECOND)) {
				return;
			}
			if (QuestManager.handleNPCInteract(player, npc, ClickOption.SECOND)) {
				return;
			}
			if (npc.getName().toLowerCase().contains("banker") || npc.getId() == 6362 || npc.getId() == 2619 || npc.getId() == 13455 || npc.getId() == 2617 || npc.getId() == 2618 || npc.getId() == 15194) {
				player.getBank().openBank();
			} else if (npc.getId() == 15149) {
				player.getDialogueManager().startDialogue("MasterOfFear", 3);
			} else if (npc.getId() == 2676) {
				PlayerLook.openGenderSelection(player);
			} else if (npc.getId() == 598) {
				PlayerLook.openHairSelection(player);
			} else if (npc instanceof Pet) {
				if (npc != player.getPet()) {
					player.getPackets().sendGameMessage("This isn't your pet!");
					return;
				}
				Pet pet = player.getPet();
				player.getPackets().sendMessage(99, "Pet [id=" + pet.getId() + ", hunger=" + pet.getDetails().getHunger() + ", growth=" + pet.getDetails().getGrowth() + ", stage=" + pet.getDetails().getStage() + "].", player);
			} else {
				player.getPackets().sendGameMessage("Nothing interesting happens.");
				if (GameConstants.DEBUG) {
					System.out.println("cliked 2 at npc id : " + npc.getId() + ", " + npc.getX() + ", " + npc.getY() + ", " + npc.getPlane());
				}
			}
		}, true));
	}

	public static void handleOption3(final Player player, NPC npc) {
		player.stopAll(false);
		player.setRouteEvent(new RouteEvent(npc, () -> {
			npc.resetWalkSteps();
			player.faceEntity(npc);
			if (npc.getId() >= 8837 && npc.getId() <= 8839) {
				MiningBase.propect(player, "You examine the remains...", "The remains contain traces of living minerals.");
				return;
			}
			npc.faceEntity(player);
			if (!player.getControllerManager().processNPCClick3(npc)) {
				return;
			}
			if (InteractionEventManager.handleNPCInteraction(player, npc, ClickOption.THIRD)) {
				return;
			}
			if (QuestManager.handleNPCInteract(player, npc, ClickOption.THIRD)) {
				return;
			}
			if (npc.getId() == 548) {
				PlayerLook.openClothingSelection(player);
			} else if (npc.getId() == 5532) {
				npc.setNextForceTalk(new ForceTalk("Senventior Disthinte Molesko!"));
				player.getControllerManager().startController("SorceressGarden");
			} else {
				player.getPackets().sendGameMessage("Nothing interesting happens.");
			}
		}, true));
		if (GameConstants.DEBUG) {
			System.out.println("cliked 3 at npc id : " + npc.getId() + ", " + npc.getX() + ", " + npc.getY() + ", " + npc.getPlane());
		}
	}

	/**
	 * @param player
	 * 		The player
	 * @param npc
	 * 		The npc
	 */
	public static void handleOption4(Player player, NPC npc) {
		player.stopAll(false);
		Object target = npc;
		if (npc.getName().equalsIgnoreCase("banker")) {
			Optional<WorldObject> booth = getClosestBankBooth(npc);
			if (booth.isPresent()) { target = booth.get(); }
		}
//		System.out.println(target);
		player.setRouteEvent(new RouteEvent(target, () -> {
			player.faceEntity(npc);
			npc.faceEntity(player);
			if (InteractionEventManager.handleNPCInteraction(player, npc, ClickOption.FOURTH)) {
				return;
			}
			if (npc.getName().toLowerCase().contains("banker") && npc.getDefinitions().hasOption("Collect")) {
				ExchangeManagement.openCollectionBox(player);
			}
		}, true));
	}

	public static void handleExamine(final Player player, NPC npc) {
		if (player.getAttribute("removing_npcs", false)) {
			NPCSpawnLoader.removeSpawn(npc);
			npc.finish();
			return;
		}
		if (npc.getDefinitions().hasAttackOption()) {
			player.sendMessage(npc.getName() + " has " + npc.getHitpoints() + "/" + npc.getMaxHitpoints() + " health left.", true);
			NPCData npcData = NPCDataLoader.getData(npc.getName());
			if (npcData != null) {
				List<Drop> drops = npcData.getDrops();
				Collections.sort(drops, (o1, o2) -> Double.compare(o1.getRate(), o2.getRate()));
				if (drops.size() > 0) {
					List<String> text = new ArrayList<>();
					drops.forEach(drop -> {
						String colour = ChatColors.BLACK;
						int price = ExchangePriceLoader.getEconomicalPrice(drop.getItemId());
						if (price > 100_000 && price < 1_000_000) {
							colour = ChatColors.MILD_BLUE;
						} else if (price >= 1_000_000) {
							colour = ChatColors.RED;
						}
						String amountDetail = "";
						if (drop.getMinAmount() == drop.getMaxAmount()) {
							amountDetail = drop.getMinAmount() + "x";
						} else {
							amountDetail = "[" + drop.getMinAmount() + " - " + drop.getMaxAmount() + "]";
						}
						text.add("<col=" + colour + ">" + amountDetail + " " + ItemDefinitions.getItemDefinitions(drop.getItemId()).getName().toLowerCase() + " drop with a chance of " + (int) drop.getRate() + "%");
					});

					Scrollable.sendQuestScroll(player, npc.getName() + " Drop Table", text.toArray(new String[text.size()]));
				}
			}
		} else {
			player.sendMessage("It's a " + npc.getDefinitions().getName() + ".");
		}
		if (GameConstants.DEBUG) {
			System.out.println("examined npc: index=" + npc.getIndex() + ", id=" + npc.getId() + "[" + npc.getName() + "]");
		}
	}

	/**
	 * Gets the closest bank booth to the npc, so npc interaction with banker's are reachable
	 *
	 * @param npc
	 * 		The npc
	 */
	private static Optional<WorldObject> getClosestBankBooth(NPC npc) {
		List<WorldObject> objectList = npc.getRegion().getObjects();
		objectList = objectList.stream().filter(object -> object.getDefinitions().containsOption("Use") || object.getDefinitions().containsOption("Use-quickly")).collect(Collectors.toList());
		Collections.sort(objectList, (o1, o2) -> Integer.compare(Utils.getDistance(npc, o1), Utils.getDistance(npc, o2)));
		return objectList.stream().findFirst();
	}
}
