package com.runescape.game.world.entity.player;

import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.game.content.skills.Woodcutting;
import com.runescape.game.content.skills.Woodcutting.HatchetDefinitions;
import com.runescape.game.content.skills.Woodcutting.TreeDefinitions;
import com.runescape.game.content.skills.farming.FarmingConstants;
import com.runescape.game.content.skills.farming.FarmingSpot;
import com.runescape.game.content.skills.farming.ProductInfo;
import com.runescape.game.content.skills.farming.SpotInfo;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.player.actions.Action;
import com.runescape.game.world.entity.player.actions.WaterFilling;
import com.runescape.game.world.item.Item;
import com.runescape.utility.Utils;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class FarmingManager implements FarmingConstants {

	private List<FarmingSpot> spots;

	private transient Player player;
	
	public FarmingManager() {
		spots = new CopyOnWriteArrayList<>();
	}
	
	public void setPlayer(Player player) {
		this.player = player;
	}
	
	public void init() {
		for (FarmingSpot spot : spots) {
			spot.setPlayer(player);
			spot.refresh();
		}
	}

	public void process() {
		Iterator<FarmingSpot> it = spots.iterator();
		while (it.hasNext()) {
			FarmingSpot spot = it.next();
			spot.setPlayer(player);
			if (spot.process()) {
				it.remove();
			}
		}
	}

	public boolean isFarming(int objectId, Item item, int optionId) {
		SpotInfo info = SpotInfo.getInfo(objectId);
		if (info != null) {
			handleFarming(info, item, optionId);
			return true;
		}
		return false;
	}
	
	public void handleFarming(SpotInfo info, Item item, int optionId) {
		FarmingSpot spot = getSpot(info);
		if (spot == null) { spot = new FarmingSpot(player, info); }
		if (!spot.isCleared()) {
			if (item != null) {
				if (info.getType() == COMPOST) { fillCompostBin(spot, item); }
			} else {
				switch (optionId) {
					case 1: // rake
						if (info.getType() == COMPOST) {
							if (spot.getHarvestAmount() == 15) {
								spot.setCleared(true);
								spot.setActive(ProductInfo.Compost_Bin);
								spot.setHarvestAmount(15);
								spot.refresh();
								player.getPackets().sendGameMessage("You close the compost bin.");
								player.getPackets().sendGameMessage("The vegetation begins to decompose.");
							}
						} else {
							startRakeAction(spot); // creates usable spot
						}
						break;
					case 2: // inspect
						sendNeedsWeeding(spot.isCleared());
						break;
					case 3: // guide
						openGuide();
						break;
				}
			}
		} else {
			if (item != null) {
				String itemName = item.getName().toLowerCase();
				if (itemName.startsWith("watering can (")) {
					startWateringAction(spot, item);
				} else if (itemName.contains("compost")) {
					startCompostAction(spot, item, itemName.equals("supercompost"));
				} else if (item.getId() == 6036) {
					startCureAction(spot, item);
				} else if (WaterFilling.isFilling(player, item.getId(), true) && player.getInventory().contains(5325)) {
					return;
				} else { startFarmingCycle(spot, item); }
			} else if (spot.getProductInfo() != null) {
				switch (optionId) {
					case 1:
						if (info.getType() == TREES) {
							if (spot.reachedMaxStage() && !spot.hasChecked()) {
								checkHealth(spot);
							} else if (spot.reachedMaxStage() && !spot.isEmpty()) {
								collectTreeProducts(spot, TreeDefinitions.valueOf(spot.getProductInfo().name().toUpperCase()));
							} else if (spot.reachedMaxStage() && spot.isEmpty()) {
								startHarvestingAction(spot);
							} else if (spot.isDead()) { clearFarmingPatch(spot); } else if (spot.isDiseased()) {
								startCureAction(spot, null);
							}
						} else if (info.getType() == FRUIT_TREES) {
							if (spot.reachedMaxStage() && !spot.hasChecked()) {
								checkHealth(spot);
							} else if (spot.reachedMaxStage() && !spot.hasEmptyHarvestAmount()) {
								startPickingAction(spot);
							} else if (spot.reachedMaxStage() && !spot.isEmpty()) {
								collectTreeProducts(spot, TreeDefinitions.FRUIT_TREES);
							} else if (spot.reachedMaxStage() && spot.isEmpty() || spot.isDead()) {
								clearFarmingPatch(spot);
							} else if (spot.isDiseased()) { startCureAction(spot, null); }
						} else if (info.getType() == BUSHES) {
							if (spot.reachedMaxStage() && !spot.hasChecked()) {
								checkHealth(spot);
							} else if (spot.reachedMaxStage() && !spot.hasEmptyHarvestAmount()) {
								startPickingAction(spot);
							} else if (spot.isDead()) { clearFarmingPatch(spot); } else if (spot.isDiseased()) {
								startCureAction(spot, null);
							}
						} else if (info.getType() == COMPOST) {
							if (spot.reachedMaxStage() && !spot.hasChecked()) {
								spot.setChecked(true);
								spot.refresh();
								player.getPackets().sendGameMessage("You open the compost bin.");
							} else if (!spot.reachedMaxStage()) {
								player.getDialogueManager().startDialogue("SimpleMessage", "The vegetation hasn't finished rotting yet.");
							} else { clearCompostAction(spot); }
						} else {
							if (spot.isDead()) { clearFarmingPatch(spot); } else if (spot.reachedMaxStage()) {
								startHarvestingAction(spot);
							}
						}
						break;
					case 2:// inspect... usless tbh
						break;
					case 3:// clear & guide
						if (spot.isDiseased() || spot.reachedMaxStage()) {
							if (info.getType() == TREES) {
								if (spot.isEmpty()) // stump
								{ startHarvestingAction(spot); } else {
									player.getPackets().sendGameMessage("You need to chop the tree down before removing it.");
									return;
								}
							} else { clearFarmingPatch(spot); }
						} else if (spot.getProductInfo().getType() == FRUIT_TREES) {
							if (spot.reachedMaxStage()) { return; }
							clearFarmingPatch(spot);
						} else { openGuide(); }
						break;
				}
			}
		}
	}
	
	public FarmingSpot getSpot(SpotInfo info) {
		for (FarmingSpot spot : spots) { if (spot.getSpotInfo().equals(info)) { return spot; } }
		return null;
	}
	
	private void fillCompostBin(final FarmingSpot spot, final Item item) {
		final boolean[] attributes = isOrganicItem(item.getId());
		player.getActionManager().setAction(new Action() {

			@Override
			public boolean start(Player player) {
				if (!player.getInventory().containsItem(item.getId(), 1) || spot.isCleared()) {
					return false;
				} else if (!attributes[0]) {
					player.getPackets().sendGameMessage("You cannot use this item to make compost.");
					return false;
				}
				return true;
			}

			@Override
			public boolean process(Player player) {
				return spot.getHarvestAmount() != 15 && player.getInventory().containsItem(item.getId(), 1);
			}

			@Override
			public int processWithDelay(Player player) {
				player.setNextAnimation(FILL_COMPOST_ANIMATION);
				player.getInventory().deleteItem(item);
				spot.setHarvestAmount(spot.getHarvestAmount() + 1);
				spot.refresh();
				return 2;
			}

			@Override
			public void stop(Player player) {
				setActionDelay(player, 3);
			}
		});
	}
	
	private void startRakeAction(final FarmingSpot spot) {
		player.getActionManager().setAction(new Action() {

			@Override
			public boolean start(Player player) {
				if (!player.getInventory().contains(RAKE)) {
					player.getPackets().sendGameMessage("You'll need a rake to get rid of the weeds.");
					return false;
				}
				return true;
			}

			@Override
			public boolean process(Player player) {
				return spot.getStage() != 3;
			}

			@Override
			public int processWithDelay(Player player) {
				player.setNextAnimation(RAKING_ANIMATION);
				if (Utils.random(3) == 0) {
					spot.increaseStage();
					if (spot.getStage() == 3) { spot.setCleared(true); }
					player.getInventory().addItemDrop(6055, 1);
					player.getSkills().addXp(Skills.FARMING, 8);
				}
				return 2;
			}

			@Override
			public void stop(Player player) {
				setActionDelay(player, 3);
			}
		});
	}
	
	private void sendNeedsWeeding(boolean cleared) {
		player.getPackets().sendGameMessage(cleared ? "The patch is ready for planting." : "The patch needs weeding.");
	}
	
	private void openGuide() {
		player.putAttribute("skillMenu", 21);
	}
	
	public boolean startWateringAction(final FarmingSpot spot, Item item) {
		if (spot == null || spot.getProductInfo() == null) { return false; }
		if (item.getName().toLowerCase().startsWith("watering can(")) {
			player.getPackets().sendGameMessage("Your watering can is empty and cannot water the plants.");
			return true;
		} else if (spot.isWatered()) {
			player.getPackets().sendGameMessage("This patch is already watered.");
			return true;
		} else if (spot.reachedMaxStage() || spot.getProductInfo().getType() == HERBS || spot.getProductInfo().getType() == COMPOST || spot.getProductInfo().getType() == TREES || spot.getProductInfo().getType() == FRUIT_TREES || spot.getProductInfo() == ProductInfo.White_lily || spot.getProductInfo().getType() == BUSHES) {
			player.getPackets().sendGameMessage("This patch doesn't need watering.");
			return true;
		} else if (spot.isDiseased()) {
			player.getPackets().sendGameMessage("This crop is diseased and needs cure, not water!");
			return true;
		} else if (spot.isDead()) {
			player.getPackets().sendGameMessage("This crop is dead and needs to be removed, not watered!");
			return true;
		}
		player.getPackets().sendGameMessage("You begin to tip the can over...");
		player.setNextAnimation(WATERING_ANIMATION);
		spot.setWatered(true);
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				player.getPackets().sendGameMessage("... and the patch becomes moist with water.");
				spot.refresh();
			}
		}, 2);
		return true;
	}
	
	public boolean startCompostAction(final FarmingSpot spot, final Item item, boolean superCompost) {
		if (spot == null || spot.getProductInfo() == null || spot.getProductInfo().getType() == -1 || spot.getProductInfo().getType() == COMPOST) { return false; }
		if (spot.hasCompost()) {
			player.getPackets().sendGameMessage("This patch is already saturated with a compost.");
			return true;
		} else if (!spot.isCleared()) {
			player.getPackets().sendGameMessage("The patch needs to be cleared in order to saturate it with compost.");
			return true;
		}
		player.getPackets().sendGameMessage("You dump a bucket of " + (superCompost ? "supercompost" : "compost") + "...");
		player.setNextAnimation(COMPOST_ANIMATION);
		if (superCompost) { spot.setSuperCompost(true); } else { spot.setCompost(true); }
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				player.getInventory().deleteItem(item);
				player.getInventory().addItem(EMPTY_BUCKET, 1);
				player.getSkills().addXp(Skills.FARMING, 8);
				player.getPackets().sendGameMessage("... and the patch becomes saturated with nutrients.");
				spot.refresh();
			}
		}, 2);
		return true;
	}
	
	public boolean startCureAction(final FarmingSpot spot, final Item item) {
		if (spot == null || spot.getProductInfo() == null || spot.getProductInfo().getType() == COMPOST) {
			return false;
		}
		final boolean isTree = spot.getProductInfo().getType() == TREES || spot.getProductInfo().getType() == FRUIT_TREES;
		final boolean isBush = spot.getProductInfo().getType() == BUSHES;
		if (!spot.isDiseased()) {
			player.getPackets().sendGameMessage("This patch doesn't need to be cured.");
			return true;
		} else if (isTree || isBush) {
			if (!(player.getInventory().contains(5329) || player.getInventory().contains(7409))) {
				player.getPackets().sendGameMessage("You need a pair of secatures to prune the tree.");
			}
		}
		player.getPackets().sendGameMessage(isTree ? "You prune the " + spot.getProductInfo().name().toLowerCase() + " tree's diseased branches." : isBush ? "You prune the " + spot.getProductInfo().name().toLowerCase() + " bush's diseased leaves." : "You treat the " + getPatchName(spot.getSpotInfo().getType()) + " patch with the plant cure.");
		player.setNextAnimation((isTree || isBush) ? PRUNING_ANIMATION : CURE_PLANT_ANIMATION);
		spot.setDiseased(false);
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				if (!isTree && !isBush) {
					player.getInventory().deleteItem(item);
					player.getInventory().addItem(new Item(229, 1));
				} else { player.setNextAnimation(new Animation(-1)); }
				player.getPackets().sendGameMessage("It is restored to health.");
				spot.refresh();
			}
		}, 2);
		return true;
	}
	
	public boolean startFarmingCycle(FarmingSpot spot, Item item) { // check if
		// weeded
		ProductInfo productInfo = ProductInfo.getProduct(item.getId());
		if (spot == null || productInfo == null || spot.getSpotInfo().getType() != productInfo.getType() || !spot.isCleared() || spot.getProductInfo() != null || spot.getSpotInfo().getType() == COMPOST) {
			return false;
		}
		String patchName = getPatchName(productInfo.getType());
		String itemName = item.getDefinitions().getName().toLowerCase();
		int requiredAmount = (productInfo.getType() == ALLOTMENT || productInfo.getType() == HOPS) ? 3 : 1;
		boolean isTree = productInfo.getType() == TREES || productInfo.getType() == FRUIT_TREES;
		int level = productInfo.getLevel();
		if (!player.getInventory().contains(isTree ? 952 : 5343)) {
			player.getPackets().sendGameMessage(isTree ? "You need a spade to plant the sappling into the dirt." : "You need a seed dipper to plant the seed in the dirt.");
			return true;
		} else if (!player.getInventory().containsItem(item.getId(), requiredAmount)) {
			player.getPackets().sendGameMessage("You don't have enough " + item.getDefinitions().getName().toLowerCase() + " to plant " + (patchName.startsWith("(?i)[^aeiou]") ? "an" : "a") + " " + patchName + " patch.");
			return true;
		} else if (player.getSkills().getLevel(Skills.FARMING) < level) {
			player.getPackets().sendGameMessage("You need a farming level of " + level + " to plant this " + (isTree ? "sapling" : "seed") + ".");
			return true;
		}
		player.getPackets().sendGameMessage("You plant the " + itemName + " in the " + patchName + " patch.");
		player.setNextAnimation(isTree ? SPADE_ANIMATION : SEED_DIPPING_ANIMATION);
		player.getSkills().addXp(Skills.FARMING, isTree ? productInfo.getExperience() : productInfo.getPlantingExperience());
		player.getInventory().deleteItem(new Item(item.getId(), requiredAmount));
		spot.setActive(productInfo);
		return true;
	}
	
	private void checkHealth(final FarmingSpot spot) {
		player.getPackets().sendGameMessage("You examine the " + ((spot.getProductInfo().getType() == TREES || spot.getProductInfo().getType() == FRUIT_TREES) ? "tree" : "bush") + " for signs of disease and find that it is in perfect health.");
		player.getSkills().addXp(Skills.FARMING, spot.getProductInfo().getPlantingExperience());
		player.setNextAnimation(CHECK_TREE_ANIMATION);
		spot.setChecked(true);
		spot.refresh();
	}
	
	private void collectTreeProducts(final FarmingSpot spot, final TreeDefinitions definitions) {
		player.getActionManager().setAction(new Action() {

			private HatchetDefinitions hatchet;

			private boolean usedDeplateAurora;

			@Override
			public boolean start(Player player) {
				if (!checkAll(player)) { return false; }
				player.getPackets().sendGameMessage("You swing your hatchet at the tree...", true);
				setActionDelay(player, getWoodcuttingDelay(player));
				return true;
			}

			private int getWoodcuttingDelay(Player player) {
				int summoningBonus = player.getFamiliar() != null ? (player.getFamiliar().getId() == 6808 || player.getFamiliar().getId() == 6807) ? 10 : 0 : 0;
				int wcTimer = definitions.getLogBaseTime() - (player.getSkills().getLevel(8) + summoningBonus) - Utils.random(hatchet.getAxeTime());
				if (wcTimer < 1 + definitions.getLogRandomTime()) {
					wcTimer = 1 + Utils.random(definitions.getLogRandomTime());
				}
				wcTimer /= player.getAuraManager().getWoodcuttingAccurayMultiplier();
				return wcTimer;
			}

			private boolean checkAll(Player player) {
				for (HatchetDefinitions def : HatchetDefinitions.values()) {
					if (player.getInventory().contains(def.getItemId()) || player.getEquipment().getWeaponId() == def.getItemId()) {
						hatchet = def;
						if (player.getSkills().getLevel(Skills.WOODCUTTING) < hatchet.getLevelRequried()) {
							hatchet = null;
							break;
						}
					}
				}
				if (hatchet == null) {
					player.getPackets().sendGameMessage("You dont have the required level to use that axe or you don't have a hatchet.");
					return false;
				}
				if (!hasWoodcuttingLevel(player)) { return false; }
				if (!player.getInventory().hasFreeSlots()) {
					player.getPackets().sendGameMessage("Not enough space in your inventory.");
					return false;
				}
				return true;
			}

			private boolean hasWoodcuttingLevel(Player player) {
				if (definitions.getLevel() > player.getSkills().getLevel(8)) {
					player.getPackets().sendGameMessage("You need a woodcutting level of " + definitions.getLevel() + " to chop down this tree.");
					return false;
				}
				return true;
			}

			@Override
			public boolean process(Player player) {
				player.setNextAnimation(new Animation(hatchet.getEmoteId()));
				return checkTree(player);
			}

			@Override
			public int processWithDelay(Player player) {
				Woodcutting.addLog(definitions, null, player);
				if (!usedDeplateAurora && (1 + Math.random()) < player.getAuraManager().getChanceNotDepleteMN_WC()) {
					usedDeplateAurora = true;
				} else if (Utils.random(definitions.getRandomLifeProbability()) == 0) {
					int time = definitions.getRespawnDelay();
					spot.setEmpty(true);
					spot.refresh();
					spot.setCycleTime(true, time * 1000); // time in seconds
					player.setNextAnimation(new Animation(-1));
					return -1;
				}
				if (!player.getInventory().hasFreeSlots()) {
					player.setNextAnimation(new Animation(-1));
					player.getPackets().sendGameMessage("Not enough space in your inventory.");
					return -1;
				}
				return getWoodcuttingDelay(player);
			}

			private boolean checkTree(Player player) {
				return spot != null && !spot.isEmpty();
			}

			@Override
			public void stop(Player player) {
				setActionDelay(player, 3);
			}
		});
	}
	
	public void startHarvestingAction(final FarmingSpot spot) {
		final String patchName = getPatchName(spot.getProductInfo().getType());
		player.getActionManager().setAction(new Action() {

			@Override
			public boolean start(Player player) {
				if (!player.getInventory().contains(952)) {
					player.getPackets().sendGameMessage("You need a spade to harvest your crops.");
					return false;
				}
				if (spot.hasEmptyHarvestAmount() && !spot.hasGivenAmount()) {
					spot.setHarvestAmount(getRandomHarvestAmount(spot.getProductInfo().getType()));
					spot.setHasGivenAmount(true);
				} else if (spot.getHarvestAmount() <= 0) {
					player.getPackets().sendGameMessage("You have successfully harvested this patch for new crops.");
					player.setNextAnimation(new Animation(-1));
					spot.setIdle();
					return false;
				}
				player.getPackets().sendGameMessage("You begin to harvest the " + patchName + " patch.");
				setActionDelay(player, 1);
				return true;
			}

			@Override
			public boolean process(Player player) {
				if (spot.getHarvestAmount() > 0) { return true; } else {
					player.getPackets().sendGameMessage("You have successfully harvested this patch for new crops.");
					player.setNextAnimation(new Animation(-1));
					spot.setIdle();
					return false;
				}
			}

			@Override
			public int processWithDelay(Player player) {
				spot.setHarvestAmount(spot.getHarvestAmount() - 1);
				player.setNextAnimation(getHarvestAnimation(spot.getProductInfo().getType()));
				player.getSkills().addXp(Skills.FARMING, spot.getProductInfo().getExperience());
				player.getInventory().addItemDrop(spot.getProductInfo().getProductId(), 1);
				return 2;
			}

			@Override
			public void stop(Player player) {
				setActionDelay(player, 3);
			}
		});
	}
	
	public void clearFarmingPatch(final FarmingSpot spot) {
		final String patchName = getPatchName(spot.getProductInfo().getType());
		player.getActionManager().setAction(new Action() {

			private int stage;

			@Override
			public boolean start(Player player) {
				if (!player.getInventory().contains(952)) {
					player.getPackets().sendGameMessage("You need a spade to clear this farming patch.");
					return false;
				}
				player.getPackets().sendGameMessage("You start digging the " + patchName + " patch...");
				return true;
			}

			@Override
			public boolean process(Player player) {
				if (stage != 2) { return true; } else {
					player.getPackets().sendGameMessage("You have successfully cleared this patch for new crops.");
					player.setNextAnimation(new Animation(-1));
					spot.setIdle();
					return false;
				}
			}

			@Override
			public int processWithDelay(Player player) {
				player.setNextAnimation(SPADE_ANIMATION);
				if (Utils.random(3) == 0) { stage++; }
				return 2;
			}

			@Override
			public void stop(Player player) {
				setActionDelay(player, 3);
			}
		});
	}
	
	private void startPickingAction(final FarmingSpot spot) {
		player.getActionManager().setAction(new Action() {

			@Override
			public boolean start(Player player) {
				return true;
			}

			@Override
			public boolean process(Player player) {
				if (spot.getHarvestAmount() > 0) { return true; } else {
					player.getPackets().sendGameMessage("You pick all of the " + (spot.getProductInfo().getType() == FRUIT_TREES ? "fruits" : "berries") + " from the " + getPatchName(spot.getProductInfo().getType()) + " patch.");
					player.setNextAnimation(new Animation(-1));
					return false;
				}
			}

			@Override
			public int processWithDelay(Player player) {
				player.getPackets().sendGameMessage("You pick a " + ItemDefinitions.getItemDefinitions(spot.getProductInfo().getProductId()).getName().toLowerCase() + ".");
				player.setNextAnimation(getHarvestAnimation(spot.getProductInfo().getType()));
				player.getSkills().addXp(Skills.FARMING, spot.getProductInfo().getExperience());
				player.getInventory().addItemDrop(spot.getProductInfo().getProductId(), 1);
				spot.setHarvestAmount(spot.getHarvestAmount() - 1);
				spot.refresh();
				if (spot.getCycleTime() < Utils.currentTimeMillis()) { spot.setCycleTime(REGENERATION_CONSTANT); }
				return 2;
			}

			@Override
			public void stop(Player player) {
				setActionDelay(player, 1);
			}
		});
	}
	
	public void clearCompostAction(final FarmingSpot spot) {
		player.getActionManager().setAction(new Action() {

			@Override
			public boolean start(Player player) {
				if (spot == null) { return false; } else if (!player.getInventory().containsItem(EMPTY_BUCKET, 1)) {
					player.getPackets().sendGameMessage("You'll need an empty bucket.");
					return false;
				}
				return true;
			}

			@Override
			public boolean process(Player player) {
				if (!player.getInventory().containsItem(EMPTY_BUCKET, 1)) {
					player.getPackets().sendGameMessage("You'll need an empty bucket.");
					return false;
				} else if (spot.getHarvestAmount() > 0) { return true; } else {
					spot.setCleared(false);
					spot.refresh();
					spot.setProductInfo(null);
					spot.remove();
					player.setNextAnimation(new Animation(-1));
					return false;
				}
			}

			@Override
			public int processWithDelay(Player player) {
				player.setNextAnimation(FILL_COMPOST_ANIMATION);
				player.getSkills().addXp(Skills.FARMING, 5);
				player.getInventory().addItemDrop(spot.getCompost() ? 6032 : 6034, 1);
				player.getInventory().deleteItem(EMPTY_BUCKET, 1);
				spot.setHarvestAmount(spot.getHarvestAmount() - 1);
				spot.refresh();
				return 2;
			}

			@Override
			public void stop(Player player) {
				setActionDelay(player, 1);
			}
		});
	}
	
	private boolean[] isOrganicItem(int itemId) {
		boolean[] bools = new boolean[2];
		for (int organicId : COMPOST_ORGANIC) {
			if (itemId == organicId) {
				bools[0] = true;
				bools[1] = false;
			}
		}
		for (int organicId : SUPER_COMPOST_ORGANIC) {
			if (itemId == organicId) {
				bools[0] = true;
				bools[1] = false;
			}
		}
		return bools;
	}
	
	private String getPatchName(int type) {
		return PATCH_NAMES[type];
	}
	
	private int getRandomHarvestAmount(int type) {
		int maximumAmount = 0, baseAmount = 0, totalAmount = 0;
		baseAmount = HARVEST_AMOUNTS[type][0];
		maximumAmount = HARVEST_AMOUNTS[type][1];
		totalAmount = Utils.random(baseAmount, maximumAmount);
		if (player.getEquipment().getWeaponId() == 7409) { totalAmount *= 1.1; }
		return totalAmount;
	}
	
	private Animation getHarvestAnimation(int type) {
		if (type == ALLOTMENT || type == HOPS || type == TREES || type == MUSHROOMS || type == BELLADONNA) {
			return SPADE_ANIMATION;
		} else if (type == HERBS || type == FLOWERS) {
			if (player.getEquipment().getWeaponId() == 7409) { return MAGIC_PICKING_ANIMATION; }
			return type == HERBS ? HERB_PICKING_ANIMATION : FLOWER_PICKING_ANIMATION;
		} else if (type == FRUIT_TREES) {
			return FRUIT_PICKING_ANIMATION;
		} else if (type == BUSHES) {
			return BUSH_PICKING_ANIMATION;
		}
		return SPADE_ANIMATION;
	}
	
	public void resetSpots() {
		spots.clear();
	}
	
	public void resetTreeTrunks() {
		for (FarmingSpot spot : spots) {
			if (spot.getSpotInfo().getType() == TREES || spot.getSpotInfo().getType() == FRUIT_TREES) {
				if (spot.isEmpty()) {
					spot.setEmpty(false);
					spot.refresh();
				}
			}
		}
	}

    public List<FarmingSpot> getSpots() {
        return this.spots;
    }
}
