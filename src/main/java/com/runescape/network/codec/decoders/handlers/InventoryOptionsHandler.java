package com.runescape.network.codec.decoders.handlers;

import com.runescape.game.GameConstants;
import com.runescape.game.content.*;
import com.runescape.game.content.ArmourSets.Sets;
import com.runescape.game.content.economy.treasure.TreasureTrailHandler;
import com.runescape.game.content.global.misc.ItemColouring;
import com.runescape.game.content.skills.AncientEffigies;
import com.runescape.game.content.skills.SkillCapeCustomizer;
import com.runescape.game.content.skills.Woodcutting.Nest;
import com.runescape.game.content.skills.crafting.GemCutting;
import com.runescape.game.content.skills.crafting.GemCutting.Gem;
import com.runescape.game.content.skills.crafting.JewllerySmithing;
import com.runescape.game.content.skills.firemaking.Firemaking;
import com.runescape.game.content.skills.fletching.Fletching;
import com.runescape.game.content.skills.fletching.Fletching.Fletch;
import com.runescape.game.content.skills.herblore.HerbCleaning;
import com.runescape.game.content.skills.herblore.Herblore;
import com.runescape.game.content.skills.hunter.FlyingEntityHunter;
import com.runescape.game.content.skills.hunter.FlyingEntityHunter.FlyingEntities;
import com.runescape.game.content.skills.hunter.TrapAction;
import com.runescape.game.content.skills.magic.Magic;
import com.runescape.game.content.skills.prayer.Burying.Bone;
import com.runescape.game.content.skills.runecrafting.Runecrafting;
import com.runescape.game.content.skills.summoning.Summoning;
import com.runescape.game.content.skills.summoning.Summoning.Pouches;
import com.runescape.game.event.interaction.InteractionEventManager;
import com.runescape.game.interaction.controllers.impl.Barrows;
import com.runescape.game.interaction.controllers.impl.FightKiln;
import com.runescape.game.interaction.controllers.impl.Wilderness;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.interaction.dialogues.impl.item.DestroyItemOption;
import com.runescape.game.interaction.dialogues.impl.item.LentItemDeleteD;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleMessage;
import com.runescape.game.interaction.dialogues.impl.skills.AmuletAttaching;
import com.runescape.game.interaction.dialogues.impl.skills.LeatherCraftingD;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.ForceTalk;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.familiar.impl.Familiar;
import com.runescape.game.world.entity.npc.familiar.impl.Familiar.SpecialAttack;
import com.runescape.game.world.entity.npc.pet.Pet;
import com.runescape.game.world.entity.player.*;
import com.runescape.game.world.entity.player.LockManagement.LockType;
import com.runescape.game.world.entity.player.actions.CowMilkingAction;
import com.runescape.game.world.entity.player.quests.impl.GertrudesCat;
import com.runescape.game.world.item.Item;
import com.runescape.game.world.item.ItemConstants;
import com.runescape.network.stream.InputStream;
import com.runescape.utility.Utils;
import com.runescape.utility.world.ClickOption;
import com.runescape.workers.game.core.CoresManager;
import com.runescape.workers.game.log.GameLog;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

import java.util.concurrent.TimeUnit;

public class InventoryOptionsHandler {

	public static void handleItemOption2(final Player player, final int slotId, final int itemId, Item item) {
		if (InteractionEventManager.handleItemInteraction(player, item, ClickOption.SECOND)) {
			return;
		}
		if (Firemaking.isFiremaking(player, itemId)) {
			return;
		}
		if (itemId >= 5509 && itemId <= 5514) {
			int pouch = -1;
			if (itemId == 5509) {
				pouch = 0;
			}
			if (itemId == 5510) {
				pouch = 1;
			}
			if (itemId == 5512) {
				pouch = 2;
			}
			if (itemId == 5514) {
				pouch = 3;
			}
			Runecrafting.emptyPouch(player, pouch);
			player.stopAll(false);
		} else if (itemId >= 15086 && itemId <= 15100) {
			Dicing.handleRoll(player, itemId, true);
		} else if (itemId == 6583 || itemId == 7927) {
			JewllerySmithing.ringTransformation(player, itemId);
		} else {
			if (!player.hasInstantSpecial(itemId)) {
				player.stopAll(false, false);
			}
			player.putAttribute("switching_happening", true);
			CoresManager.schedule(() -> WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					player.storeSwitch(slotId);
				}
			}), 100, TimeUnit.MILLISECONDS);
		}
	}

	public static void dig(final Player player) {
		player.resetWalkSteps();
		player.setNextAnimation(new Animation(830));
		player.getLockManagement().lockAll();
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				player.getLockManagement().unlockAll();
				if (Barrows.digIntoGrave(player)) {
					return;
				}
				if (TreasureTrailHandler.handleDig(player)) {
					return;
				}
				if (player.getX() == 3005 && player.getY() == 3376 || player.getX() == 2999 && player.getY() == 3375 || player.getX() == 2996 && player.getY() == 3377 || player.getX() == 2989 && player.getY() == 3378 || player.getX() == 2987 && player.getY() == 3387 || player.getX() == 2984 && player.getY() == 3387) {
					// mole
					player.setNextWorldTile(new WorldTile(1752, 5137, 0));
					player.getPackets().sendGameMessage("You seem to have dropped down into a network of mole tunnels.");
					return;
				}
				player.getPackets().sendGameMessage("Nothing interesting happens.");
			}

		});
	}

	public static void handleItemOption1(Player player, final int slotId, final int itemId, Item item) {
		long time = Utils.currentTimeMillis();
		if (player.getLockManagement().isLocked(LockType.ITEM_INTERACTION) || player.getEmotesManager().getNextEmoteEnd() >= time) {
			return;
		}
		player.stopAll(false);
		int leatherIndex = LeatherCraftingD.getIndex(item.getId());
		if (leatherIndex != -1) {
			player.getDialogueManager().startDialogue("LeatherCraftingD", leatherIndex);
			return;
		}
		if (InteractionEventManager.handleItemInteraction(player, item, ClickOption.FIRST)) {
			return;
		}
		if (Foods.eat(player, item, slotId)) {
			return;
		}
		if (Nest.isNest(itemId)) {
			Nest.searchNest(player, slotId);
			return;
		}
		if (itemId >= 15086 && itemId <= 15100) {
			Dicing.handleRoll(player, itemId, false);
			return;
		}
		if (TreasureTrailHandler.read(player, itemId) || TreasureTrailHandler.handleTrailCasket(player, itemId)) {
			return;
		}
		if (Pots.pot(player, item, slotId)) {
			return;
		}
		if (itemId == 299) {
			if (!GameConstants.GAMBLING_ENABLED) {
				player.sendMessage("Gambling has been disabled.");
				return;
			}
			if (!player.getFacade().isCanGamble()) {
				player.getDialogueManager().startDialogue(SimpleMessage.class, "You cannot dice! You must claim a \"Gambling claimer\" first before having access to gambling facilities like this.", "Buy one at the vote point shop or the gold point shop.");
				return;
			}
			if (player.isCanPvp()) {
				player.getPackets().sendGameMessage("You cant plant a seed while doing this action.");
				return;
			} else if (!World.canMoveNPC(player.getPlane(), player.getX(), player.getY(), 1) || World.getRegion(player.getRegionId()).getSpawnedObject(player) != null) {
				player.getPackets().sendGameMessage("You can't plant a flower here.");
				return;
			}
			if (!player.getAttribute("agreed_flower_planting_rules", false)) {
				player.getDialogueManager().startDialogue(new Dialogue() {
					@Override
					public void start() {
						sendDialogue("By flower planting, you are aware & agree that there are no refunds from scammers.", "But punishments such as permanent bans will be established for scammers.");
					}

					@Override
					public void run(int interfaceId, int option) {
						switch (stage) {
							case -1:
								sendOptionsDialogue("Do you agree?", "Yes", "No");
								stage = 0;
								break;
							case 0:
								if (option == FIRST) {
									player.putAttribute("agreed_flower_planting_rules", true);
									handleItemOption1(player, slotId, itemId, item);
								}
								end();
								break;
						}
					}

					@Override
					public void finish() {

					}
				});
				return;
			}
			if (!player.takeMoney(10_000)) {
				player.getDialogueManager().startDialogue(SimpleMessage.class, "It costs 10,000 coins to plant a flower.");
				return;
			}
			player.setNextAnimation(new Animation(827));
			World.spawnObjectTemporary(new WorldObject(2980 + Utils.random(8), 10, 0, player.getX(), player.getY(), player.getPlane()), 25000);
			player.getInventory().deleteItem(299, 1);
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					if (!player.addWalkSteps(player.getX() - 1, player.getY(), 1)) {
						if (!player.addWalkSteps(player.getX() + 1, player.getY(), 1)) {
							if (!player.addWalkSteps(player.getX(), player.getY() + 1, 1)) {
								player.addWalkSteps(player.getX(), player.getY() - 1, 1);
							}
						}
					}
				}
			}, 2);
		}
		if (itemId >= 5509 && itemId <= 5514) {
			int pouch = -1;
			if (itemId == 5509) {
				pouch = 0;
			}
			if (itemId == 5510) {
				pouch = 1;
			}
			if (itemId == 5512) {
				pouch = 2;
			}
			if (itemId == 5514) {
				pouch = 3;
			}
			Runecrafting.fillPouch(player, pouch);
			return;
		}
		if (itemId == 22370) {
			Summoning.openDreadnipInterface(player);
		}
		if (itemId == 952) {// spade
			dig(player);
			return;
		}
		if (HerbCleaning.clean(player, item, slotId)) {
			return;
		} else if (TrapAction.isTrap(player, new WorldTile(player), itemId)) { return; }
		Bone bone = Bone.forId(itemId);
		if (bone != null) {
			Bone.bury(player, slotId);
			return;
		}
		if (Magic.useTabTeleport(player, itemId)) {
			return;
		}
		if (itemId == AncientEffigies.SATED_ANCIENT_EFFIGY || itemId == AncientEffigies.GORGED_ANCIENT_EFFIGY || itemId == AncientEffigies.NOURISHED_ANCIENT_EFFIGY || itemId == AncientEffigies.STARVED_ANCIENT_EFFIGY) {
			player.getDialogueManager().startDialogue("AncientEffigiesD", itemId);
		} else if (itemId == 4155) {
			player.getDialogueManager().startDialogue("EnchantedGemDialouge");
		} else if (itemId >= 23653 && itemId <= 23658) {
			FightKiln.useCrystal(player, itemId);
		} else if (item.getDefinitions().getName().startsWith("Burnt")) {
			player.getDialogueManager().startDialogue("SimplePlayerMessage", "Ugh, this is inedible.");
		} else if (Sets.forId(itemId) != null) {
			ArmourSets.exchangeSets(player, Sets.forId(itemId));
		}

		if (GameConstants.DEBUG) {
			System.out.println("Item Select:" + itemId + ", Slot Id:" + slotId);
		}
	}

	/*
	 * returns the other
	 */
	public static Item contains(int id1, Item item1, Item item2) {
		if (item1.getId() == id1) {
			return item2;
		}
		if (item2.getId() == id1) {
			return item1;
		}
		return null;
	}

	public static boolean contains(int id1, int id2, Item... items) {
		boolean containsId1 = false;
		boolean containsId2 = false;
		for (Item item : items) {
			if (item.getId() == id1) {
				containsId1 = true;
			} else if (item.getId() == id2) {
				containsId2 = true;
			}
		}
		return containsId1 && containsId2;
	}

	public static void handleItemOnItem(final Player player, InputStream stream) {
		int hash1 = stream.readIntV1();
		int interfaceId = hash1 >> 16;
		int itemUsedId = stream.readShort128();
		int fromSlot = stream.readShortLE128();
		int hash2 = stream.readIntV2();
		int interfaceId2 = hash2 >> 16;
		int itemUsedWithId = stream.readShort128();
		int toSlot = stream.readShortLE();
		int component = hash2 & 0xFFFF;

		if ((interfaceId2 == 747 || interfaceId2 == 662) && interfaceId == Inventory.INVENTORY_INTERFACE) {
			if (player.getFamiliar() != null) {
				if (!player.getControllerManager().canUseFamiliarSpecial()) {
					return;
				}
				player.getFamiliar().setSpecial(true);
				if (player.getFamiliar().getSpecialAttack() == SpecialAttack.ITEM) {
					if (player.getFamiliar().hasSpecialOn()) {
						player.getFamiliar().submitSpecial(toSlot);
					}
				}
			}
			return;
		}
		if (interfaceId2 == 192 && interfaceId == 679) {
			Item slotItem = player.getInventory().getItem(toSlot);
			if (slotItem == null) {
				return;
			}
			Magic.handleMagicOnItemSpell(player, slotItem, component);
			return;
		}
		if (interfaceId == Inventory.INVENTORY_INTERFACE && interfaceId == interfaceId2 && !player.getInterfaceManager().containsInventoryInter()) {
			if (toSlot >= 28 || fromSlot >= 28) {
				return;
			}
			Item usedWith = player.getInventory().getItem(toSlot);
			Item itemUsed = player.getInventory().getItem(fromSlot);
			if (itemUsed == null || usedWith == null || itemUsed.getId() != itemUsedId || usedWith.getId() != itemUsedWithId) {
				return;
			}
			if (toSlot == fromSlot) {
				return;
			}
			player.stopAll();
			if (!player.getControllerManager().canUseItemOnItem(itemUsed, usedWith)) {
				return;
			}
			Fletch fletch = Fletching.isFletching(usedWith, itemUsed);
			if (fletch != null) {
				player.getDialogueManager().startDialogue("FletchingD", fletch);
				return;
			}
			int herblore = Herblore.isHerbloreSkill(itemUsed, usedWith);
			if (herblore > -1) {
				player.getDialogueManager().startDialogue("HerbloreD", herblore, itemUsed, usedWith);
				return;
			}
			int leatherIndex = LeatherCraftingD.getIndex(itemUsedId) == -1 ? LeatherCraftingD.getIndex(itemUsedWithId) : LeatherCraftingD.getIndex(itemUsedId);
			if (leatherIndex != -1 && ((itemUsedId == 1733 || itemUsedWithId == 1733) || LeatherCraftingD.isExtraItem(itemUsedWithId) || LeatherCraftingD.isExtraItem(itemUsedId))) {
				player.getDialogueManager().startDialogue("LeatherCraftingD", leatherIndex);
				return;
			}
			Sets set = ArmourSets.getArmourSet(itemUsedId, itemUsedWithId);
			if (set != null) {
				ArmourSets.exchangeSets(player, set);
				return;
			}
			if (WeaponPoison.poison(player, itemUsed, usedWith)) {
				return;
			} else if (contains(22498, 554, itemUsed, usedWith) || contains(22498, 22448, itemUsed, usedWith)) {
				if (player.getSkills().getLevel(Skills.FARMING) < 80) {
					player.getPackets().sendGameMessage("You need a Farming level of 80 in order to make a polypore staff.");
					return;
				} else if (!player.getInventory().containsItem(22448, 3000)) {
					player.getPackets().sendGameMessage("You need 3,000 polypore spores in order to make a polypore staff.");
					return;
				} else if (!player.getInventory().containsItem(554, 15000)) {
					player.getPackets().sendGameMessage("You need 15,000 fire runes in order to make a polypore staff.");
					return;
				}
				player.setNextAnimation(new Animation(15434));
				player.getLockManagement().lockAll(2000);
				player.getInventory().deleteItem(554, 15000);
				player.getInventory().deleteItem(22448, 3000);
				player.getInventory().deleteItem(22498, 1);
				player.getInventory().addItem(22494, 1);
				player.getPackets().sendGameMessage("You attach the polypore spores and infuse the fire runes to the stick in order to create a staff.");
				return;
			}
			if (PotionOperations.handleDecanting(player, fromSlot, toSlot)) {
				return;
			}
			if (ItemOnTypeHandler.handleItemOnItem(player, itemUsed, usedWith)) {
				return;
			} else if (ItemColouring.useDyeOnItem(player, itemUsed, usedWith)) {
				return;
			} else if (Firemaking.isFiremaking(player, itemUsed, usedWith)) {
				return;
			} else if (AmuletAttaching.isAttaching(itemUsedId, itemUsedWithId)) {
				player.getDialogueManager().startDialogue("AmuletAttaching");
			} else if (contains(1755, Gem.OPAL.getUncut(), itemUsed, usedWith)) {
				GemCutting.cut(player, Gem.OPAL);
			} else if (contains(1755, Gem.JADE.getUncut(), itemUsed, usedWith)) {
				GemCutting.cut(player, Gem.JADE);
			} else if (contains(1755, Gem.RED_TOPAZ.getUncut(), itemUsed, usedWith)) {
				GemCutting.cut(player, Gem.RED_TOPAZ);
			} else if (contains(1755, Gem.SAPPHIRE.getUncut(), itemUsed, usedWith)) {
				GemCutting.cut(player, Gem.SAPPHIRE);
			} else if (contains(1755, Gem.EMERALD.getUncut(), itemUsed, usedWith)) {
				GemCutting.cut(player, Gem.EMERALD);
			} else if (contains(1755, Gem.RUBY.getUncut(), itemUsed, usedWith)) {
				GemCutting.cut(player, Gem.RUBY);
			} else if (contains(1755, Gem.DIAMOND.getUncut(), itemUsed, usedWith)) {
				GemCutting.cut(player, Gem.DIAMOND);
			} else if (contains(1755, Gem.DRAGONSTONE.getUncut(), itemUsed, usedWith)) {
				GemCutting.cut(player, Gem.DRAGONSTONE);
			} else if (contains(1755, Gem.ONYX.getUncut(), itemUsed, usedWith)) {
				GemCutting.cut(player, Gem.ONYX);
			} else {
				player.getPackets().sendGameMessage("Nothing interesting happens.");
			}
			if (GameConstants.DEBUG) {
				System.out.println("Used:" + itemUsed.getId() + ", With:" + usedWith.getId());
			}
		}
	}

	public static void handleItemOption3(Player player, int slotId, int itemId, Item item) {
		long time = Utils.currentTimeMillis();
		if (player.getLockManagement().isLocked(LockType.ITEM_INTERACTION) || player.getEmotesManager().getNextEmoteEnd() >= time) {
			return;
		}
		FlyingEntities impJar = FlyingEntities.forItem((short) itemId);
		if (impJar != null) { FlyingEntityHunter.openJar(player, impJar, slotId); }
		if (InteractionEventManager.handleItemInteraction(player, item, ClickOption.THIRD)) {
			return;
		}
		player.stopAll(false);
		if (itemId == 20767 || itemId == 20769 || itemId == 20771) {
			SkillCapeCustomizer.startCustomizing(player, itemId);
		} else if (itemId >= 15084 && itemId <= 15100) {
			player.getDialogueManager().startDialogue("DiceBag", itemId);
		} else if (itemId == 24437 || itemId == 24439 || itemId == 24440 || itemId == 24441) {
			player.getDialogueManager().startDialogue("FlamingSkull", item, slotId);
		} else if (Equipment.getItemSlot(itemId) == Equipment.SLOT_AURA) {
			player.getAuraManager().sendTimeRemaining(itemId);
		} else if (item.getDefinitions().containsOption(2, "Check-charges")) {
			player.getDegradeManager().sendTimeLeft(item);
		}
	}

	public static void handleItemOption4(Player player, int slotId, int itemId, Item item) {
		System.out.println("Option 4");
	}

	public static void handleItemOption5(Player player, int slotId, int itemId, Item item) {
		System.out.println("Option 5");
	}

	public static void handleItemOption6(Player player, int slotId, int itemId, Item item) {
		if (InteractionEventManager.handleItemInteraction(player, item, ClickOption.SIXTH)) {
			return;
		}
		if (itemId == 995) {
			MoneyPouchManagement.addCoins(player, item.getAmount());
			return;
		}
		long time = Utils.currentTimeMillis();
		if (player.getLockManagement().isLocked(LockType.ITEM_INTERACTION) || player.getEmotesManager().getNextEmoteEnd() >= time) {
			return;
		}
		player.stopAll(false);
		Pouches pouches = Pouches.forId(itemId);
		if (pouches != null) {
			Summoning.spawnFamiliar(player, pouches);
		} else if (itemId == 1438) {
			Runecrafting.locate(player, 3127, 3405);
		} else if (itemId == 1440) {
			Runecrafting.locate(player, 3306, 3474);
		} else if (itemId == 1442) {
			Runecrafting.locate(player, 3313, 3255);
		} else if (itemId == 1444) {
			Runecrafting.locate(player, 3185, 3165);
		} else if (itemId == 1446) {
			Runecrafting.locate(player, 3053, 3445);
		} else if (itemId == 1448) {
			Runecrafting.locate(player, 2982, 3514);
		} else if (itemId <= 1712 && itemId >= 1706 || itemId >= 10354 && itemId <= 10362) {
			player.getDialogueManager().startDialogue("Transportation", "Edgeville", new WorldTile(3087, 3496, 0), "Karamja", new WorldTile(2918, 3176, 0), "Draynor Village", new WorldTile(3105, 3251, 0), "Al Kharid", new WorldTile(3293, 3163, 0), itemId);
		} else if (itemId == 1704 || itemId == 10352) {
			player.getPackets().sendGameMessage("The amulet has ran out of charges. You need to recharge it if you wish it use it once more.");
		} else if (itemId >= 3853 && itemId <= 3867) {
			player.getDialogueManager().startDialogue("Transportation", "Burthrope Games Room", new WorldTile(2880, 3559, 0), "Barbarian Outpost", new WorldTile(2519, 3571, 0), "Gamers' Grotto", new WorldTile(2970, 9679, 0), "Corporeal Beast", new WorldTile(2886, 4377, 0), itemId);
		}
	}

	public static void handleItemOption7(Player player, int slotId, int itemId, Item item) {
		long time = Utils.currentTimeMillis();
		if (player.getLockManagement().isLocked(LockType.ITEM_INTERACTION) || player.getEmotesManager().getNextEmoteEnd() >= time) {
			return;
		}
		if (!player.getControllerManager().canDropItem(item)) {
			return;
		}
		player.stopAll(false);
		if (item.getDefinitions().isOverSized()) {
			player.getPackets().sendGameMessage("The item appears to be oversized.");
			player.getInventory().deleteItem(item);
			return;
		}
		if (item.getDefinitions().isLended()) {
			player.getDialogueManager().startDialogue(LentItemDeleteD.class, item);
			return;
		}
		if (!ItemConstants.isTradeable(item) || item.getDefinitions().isDestroyItem()) {
			player.getDialogueManager().startDialogue(DestroyItemOption.class, slotId, item);
			return;
		}
		if (player.getPetManager().spawnPet(itemId, true)) {
			return;
		}
		player.getPackets().sendResetMinimapFlag();
		player.getInventory().deleteItem(slotId, item);
		CoresManager.LOG_PROCESSOR.appendLog(new GameLog("item_interaction", player.getUsername(), "Dropped:\t" + item));
		World.addGroundItem(item, new WorldTile(player), player, !player.getControllerManager().verifyControlerForOperation(Wilderness.class).isPresent(), 90);
		player.getPackets().sendSound(2739, 0, 1);
		player.stopAll();
	}

	public static void handleItemOption8(Player player, int slotId, int itemId, Item item) {
		player.getInventory().sendExamine(slotId);
	}

	public static void handleItemOnNPC(final Player player, final NPC npc, final Item item) {
		if (item == null) {
			return;
		}
		player.setRouteEvent(new RouteEvent(npc, () -> {
			if (!player.getInventory().containsItem(item.getId(), item.getAmount())) {
				return;
			}
			player.faceEntity(npc);
			npc.faceEntity(player);
			if (npc.getId() == 7742) {
				// fluffs
				if (item.getId() == 1927 && player.getQuestManager().getStage(GertrudesCat.class) == GertrudesCat.Stages.FINDING_CAT) {
					player.getInventory().deleteItem(item);
					player.getInventory().addItem(CowMilkingAction.EMPTY_BUCKET, 1);
					player.setNextAnimation(Bone.BURY_ANIMATION);
					player.getQuestManager().setStage(GertrudesCat.class, GertrudesCat.Stages.GAVE_FLUFFS_MILK);
					npc.setNextForceTalk(new ForceTalk("Mew!"));
				} else if (item.getId() == 1552 && player.getQuestManager().getStage(GertrudesCat.class) == GertrudesCat.Stages.GAVE_FLUFFS_MILK) {
					player.getInventory().deleteItem(item);
					player.setNextAnimation(Bone.BURY_ANIMATION);
					player.getQuestManager().setStage(GertrudesCat.class, GertrudesCat.Stages.GAVE_FLUFFS_SARDINE);
					npc.setNextForceTalk(new ForceTalk("Mew!"));
				} else if (item.getId() == 13236 && player.getQuestManager().getStage(GertrudesCat.class) == GertrudesCat.Stages.FOUND_KITTENS) {
					player.getLockManagement().lockAll();
					player.getInventory().deleteItem(item.getId(), Integer.MAX_VALUE);
					npc.addWalkSteps(3310, 3508);

					WorldTasksManager.schedule(new WorldTask() {
						@Override
						public void run() {
							stop();
							player.getQuestManager().setStage(GertrudesCat.class, GertrudesCat.Stages.GAVE_FLUFF_KITTENS);
							player.getDialogueManager().startDialogue(SimpleMessage.class, "Fluffs has run off home with her offspring.");
							player.getLockManagement().unlockAll();
							player.getLocalNPCUpdate().getLocalNPCs().clear();
							player.getPackets().sendLocalNPCsUpdate();
						}
					}, 5);
				}
			} else if (npc instanceof Pet) {
				player.faceEntity(npc);
				player.getPetManager().eat(item.getId(), (Pet) npc);
			} else if (npc instanceof Familiar) {
				Familiar familiar = (Familiar) npc;
				if (familiar.getBOBSize() == 0) {
					return;
				}
				int slot = player.getInventory().getItems().lookupSlot(item);
				if (slot == -1) {
					return;
				}
				familiar.getBob().addItem(slot, item.getAmount());
			} else {
				player.sendMessage("Nothing interesting happens...");
			}
		}, true));
	}
}
