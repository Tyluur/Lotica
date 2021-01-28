package com.runescape.game.content.global.minigames.duel;

import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.game.content.Foods.Food;
import com.runescape.game.content.Pots.Pot;
import com.runescape.game.event.InputEvent;
import com.runescape.game.event.InputEvent.InputEventType;
import com.runescape.game.interaction.controllers.Controller;
import com.runescape.game.interaction.dialogues.impl.object.ForfeitDialouge;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.ForceTalk;
import com.runescape.game.world.entity.player.Equipment;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.actions.PlayerCombat;
import com.runescape.game.world.item.Item;
import com.runescape.game.world.item.ItemConstants;
import com.runescape.game.world.item.ItemsContainer;
import com.runescape.network.codec.decoders.WorldPacketsDecoder;
import com.runescape.network.codec.decoders.handlers.ButtonHandler;
import com.runescape.utility.Utils;
import com.runescape.workers.game.core.CoresManager;
import com.runescape.workers.game.log.GameLog;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

public class DuelArena extends Controller {

	private transient Player target;

	private transient boolean ifFriendly, isDueling;

	private static final WorldTile[] LOBBY_TELEPORTS = { new WorldTile(3367, 3275, 0), new WorldTile(3360, 3275, 0), new WorldTile(3358, 3270, 0), new WorldTile(3363, 3268, 0), new WorldTile(3370, 3268, 0), new WorldTile(3367, 3267, 0), new WorldTile(3376, 3275, 0), new WorldTile(3377, 3271, 0), new WorldTile(3375, 3269, 0), new WorldTile(3381, 3277, 0) };

	@Override
	public void start() {
		this.target = (Player) getArguments()[0];
		ifFriendly = (boolean) getArguments()[1];
		openDuelScreen(target, ifFriendly);
	}

	private void openDuelScreen(Player target, boolean ifFriendly) {
		if (!ifFriendly) {
			if (player.isAnyIronman() || target.isAnyIronman()) {
				player.sendMessage("Ironmen cannot duel staked duels.");
				openDuelScreen(target, this.ifFriendly = true);
				return;
			}
			sendOptions(player);
			player.getLastDuelRules().getStake().clear();
		}
		player.getAttributes().put("acceptedDuel", false);
		player.getPackets().sendItems(134, false, player.getLastDuelRules().getStake());
		player.getPackets().sendItems(134, true, player.getLastDuelRules().getStake());
		player.getPackets().sendGlobalString(274, target.getDisplayName());
		player.getPackets().sendIComponentText(ifFriendly ? 637 : 631, ifFriendly ? 18 : 40, "" + (target.getSkills().getCombatLevel()));
		player.getPackets().sendConfig(286, 0);
		player.getAttributes().put("firstScreen", true);
		player.getInterfaceManager().sendInterface(ifFriendly ? 637 : 631);
		refreshScreenMessage(true, ifFriendly);
		player.setCloseInterfacesEvent(() -> closeDuelInteraction(true, DuelStage.DECLINED));
	}

	private void accept(boolean firstStage) {
		if (!hasTarget()) {
			return;
		}
		DuelRules rules = player.getLastDuelRules();
		if (!rules.canAccept(player.getLastDuelRules().getStake())) {
			return;
		}
		player.getAttributes().put("acceptedDuel", true);

		boolean accepted = (Boolean) player.getAttributes().get("acceptedDuel");
		boolean targetAccepted = (Boolean) target.getAttributes().get("acceptedDuel");
		if (accepted && targetAccepted) {
			if (firstStage) {
				if (nextStage()) {
					((DuelArena) target.getControllerManager().getController()).nextStage();
				}
			} else {
				player.setCloseInterfacesEvent(null);
				player.closeInterfaces();
				closeDuelInteraction(true, DuelStage.DONE);
			}
			return;
		}
		refreshScreenMessages(firstStage, ifFriendly);
	}

	protected void closeDuelInteraction(boolean started, DuelStage duelStage) {
		Player oldTarget = target;
		if (duelStage != DuelStage.DONE) {
			target = null;
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					player.getControllerManager().startController("DuelControler");
				}
			});
			player.getInventory().getItems().addAll(player.getLastDuelRules().getStake());
			player.getInventory().init();
			player.getLastDuelRules().getStake().clear();
		} else {
			removeEquipment();
			beginBattle(started);
		}
		Controller controler = oldTarget.getControllerManager().getController();
		if (controler == null) {
			return;
		}
		DuelArena targetConfiguration = (DuelArena) controler;
		if (controler instanceof DuelArena) {
			if (targetConfiguration.hasTarget()) {
				oldTarget.setCloseInterfacesEvent(null);
				oldTarget.closeInterfaces();
				if (duelStage != DuelStage.DONE) {
					player.getControllerManager().removeController();
				}
				if (started) {
					targetConfiguration.closeDuelInteraction(false, duelStage);
				}
				if (duelStage == DuelStage.DONE) {
					player.getPackets().sendGameMessage("Your battle will begin shortly.");
				} else if (duelStage == DuelStage.SECOND) {
					player.getPackets().sendGameMessage("<col=ff0000>Please check if these settings are correct.");
				} else if (duelStage == DuelStage.DECLINED) {
					oldTarget.getPackets().sendGameMessage("<col=ff0000>Other player declined the duel!");
				} else if (duelStage == DuelStage.DECLINED) {
					oldTarget.getPackets().sendGameMessage("You do not have enough space to continue!");
					oldTarget.getPackets().sendGameMessage("Other player does not have enough space to continue!");
				}
			}
		}
	}

	public void addItem(int slot, int amount) {
		if (!hasTarget()) {
			return;
		}
		Item item = player.getInventory().getItem(slot);
		if (item == null) {
			return;
		}
		if (!ItemConstants.isTradeable(item)) {
			player.getPackets().sendGameMessage("That item cannot be staked!");
			return;
		}
		Item[] itemsBefore = player.getLastDuelRules().getStake().getItemsCopy();
		int maxAmount = player.getInventory().getItems().getNumberOf(item);
		if (amount < maxAmount) {
			item = new Item(item.getId(), amount);
		} else {
			item = new Item(item.getId(), maxAmount);
		}
		player.getLastDuelRules().getStake().add(item);
		player.getInventory().deleteItem(slot, item);
		refreshItems(itemsBefore);
		cancelAccepted();
	}

	public void removeItem(final int slot, int amount) {
		if (!hasTarget()) {
			return;
		}
		Item item = player.getLastDuelRules().getStake().get(slot);
		if (item == null) {
			return;
		}
		if (player.getInterfaceManager().containsInterface(ifFriendly ? 639 : 626)) {
			return;
		}
		Item[] itemsBefore = player.getLastDuelRules().getStake().getItemsCopy();
		int maxAmount = player.getLastDuelRules().getStake().getNumberOf(item);
		if (amount < maxAmount) {
			item = new Item(item.getId(), amount);
		} else {
			item = new Item(item.getId(), maxAmount);
		}
		player.getLastDuelRules().getStake().remove(slot, item);
		player.getInventory().addItem(item);
		refreshItems(itemsBefore);
		cancelAccepted();
	}

	private void refreshItems(Item[] itemsBefore) {
		int[] changedSlots = new int[itemsBefore.length];
		int count = 0;
		for (int index = 0; index < itemsBefore.length; index++) {
			Item item = player.getLastDuelRules().getStake().getItems()[index];
			if (itemsBefore[index] != item) {
				if (itemsBefore[index] != null && (item == null || item.getId() != itemsBefore[index].getId() || item.getAmount() < itemsBefore[index].getAmount())) {
					sendFlash(index);
				}
				changedSlots[count++] = index;
			}
		}
		int[] finalChangedSlots = new int[count];
		System.arraycopy(changedSlots, 0, finalChangedSlots, 0, count);
		refresh(finalChangedSlots);
	}

	private void sendFlash(int slot) {
		player.getPackets().sendInterFlashScript(631, 47, 4, 7, slot);
		target.getPackets().sendInterFlashScript(631, 50, 4, 7, slot);
	}

	private void refresh(int... slots) {
		player.getPackets().sendUpdateItems(134, player.getLastDuelRules().getStake(), slots);
		target.getPackets().sendUpdateItems(134, true, player.getLastDuelRules().getStake().getItems(), slots);
	}

	public void cancelAccepted() {
		boolean canceled = false;
		if (player.getAttribute("acceptedDuel", false)) {
			player.getAttributes().put("acceptedDuel", false);
			canceled = true;
		}
		if (target.getAttribute("acceptedDuel", false)) {
			target.getAttributes().put("acceptedDuel", false);
			canceled = true;
		}
		if (canceled) {
			refreshScreenMessages(true, ifFriendly);
		}
	}

	private void openConfirmationScreen(boolean ifFriendly) {
		player.getInterfaceManager().sendInterface(ifFriendly ? 639 : 626);
		if (!ifFriendly) {
			player.getPackets().sendIComponentText(626, 26, "");
			player.getPackets().sendIComponentText(626, 25, "");
		}
		refreshScreenMessage(false, ifFriendly);
	}

	private void refreshScreenMessages(boolean firstStage, boolean ifFriendly) {
		refreshScreenMessage(firstStage, ifFriendly);
		((DuelArena) target.getControllerManager().getController()).refreshScreenMessage(firstStage, ifFriendly);
	}

	private void refreshScreenMessage(boolean firstStage, boolean ifFriendly) {
		player.getPackets().sendIComponentText(firstStage ? (ifFriendly ? 637 : 631) : (ifFriendly ? 639 : 626), firstStage ? (ifFriendly ? 20 : 41) : (ifFriendly ? 23 : 35), "<col=ff0000>" + getAcceptMessage(firstStage));
	}

	private String getAcceptMessage(boolean firstStage) {
		if (target.getAttributes().get("acceptedDuel") == Boolean.TRUE) {
			return "Other player has accepted.";
		} else if (player.getAttributes().get("acceptedDuel") == Boolean.TRUE) {
			return "Waiting for other player...";
		}
		return firstStage ? "" : "Please look over the agreements to the duel.";
	}

	public boolean nextStage() {
		if (!hasTarget()) {
			return false;
		}
		if (player.getInventory().getItems().getUsedSlots() + target.getLastDuelRules().getStake().getUsedSlots() > 28) {
			player.setCloseInterfacesEvent(null);
			player.closeInterfaces();
			closeDuelInteraction(true, DuelStage.NO_SPACE);
			return false;
		}
		player.getAttributes().put("acceptedDuel", false);
		player.getInterfaceManager().closeInventoryInterface();
		cancelAccepted();
		openConfirmationScreen(false);
		return true;
	}

	private void sendOptions(Player player) {
		player.getInterfaceManager().sendInventoryInterface(628);
		player.getPackets().sendUnlockIComponentOptionSlots(628, 0, 0, 27, 0, 1, 2, 3, 4, 5);
		player.getPackets().sendInterSetItemsOptionsScript(628, 0, 93, 4, 7, "Stake 1", "Stake 5", "Stake 10", "Stake All", "Stake X");
		player.getPackets().sendUnlockIComponentOptionSlots(631, 47, 0, 27, 0, 1, 2, 3, 4, 5);
		player.getPackets().sendInterSetItemsOptionsScript(631, 0, 120, 4, 7, "Remove 1", "Remove 5", "Remove 10", "Remove X", "Examine");
	}

	public void endDuel(Player victor, Player loser) {
		endDuel(victor, loser, true);
	}

	private boolean shouldDraw(Player possibleVictor, Player possibleLoser) {
		return (possibleVictor.getHitpoints() <= 0 && possibleLoser.getHitpoints() <= 0);
	}

	public void endDuel(final Player victor, final Player loser, boolean removeLoserControler) {
		boolean drawed = shouldDraw(victor, loser);
		if (drawed) {
			refundItems(victor);
			refundItems(loser);
		} else {
			if (victor.getLastDuelRules().isReceivedWins() || loser.getLastDuelRules().isReceivedWins()) {
				System.err.println("Stopped from receiving double wins: " + victor + ", " + loser);
				return;
			}
			for (Item item : victor.getLastDuelRules().getStake().getItems()) {
				if (item == null) {
					continue;
				}
				victor.getInventory().addItem(item);
				CoresManager.LOG_PROCESSOR.appendLog(new GameLog("duel_arena", victor.getUsername(), "Won a duel vs " + loser.getUsername() + " and received " + item));
			}
			for (Item item : loser.getLastDuelRules().getStake().getItems()) {
				if (item == null) {
					continue;
				}
				victor.getInventory().addItem(item);
				CoresManager.LOG_PROCESSOR.appendLog(new GameLog("duel_arena", victor.getUsername(), "Won a duel vs " + loser.getUsername() + " and received " + item));
			}
		}
		if (loser.getControllerManager().getController() != null && removeLoserControler) {
			loser.getControllerManager().removeController();
		}
		loser.setCanPvp(false);
		loser.getHintIconsManager().removeUnsavedHintIcon();
		loser.reset();
		loser.closeInterfaces();
		if (victor.getControllerManager().getController() != null) {
			victor.getControllerManager().removeController();
		}
		victor.setCanPvp(false);
		victor.getHintIconsManager().removeUnsavedHintIcon();
		victor.reset();
		victor.closeInterfaces();
		startEndingTeleport(victor);
		startEndingTeleport(loser);
		if (!drawed) {
			loser.getPackets().sendGameMessage("Oh dear, it seems you have lost to " + victor.getDisplayName() + ".");
			victor.getPackets().sendGameMessage("Congratulations! You easily defeated " + loser.getDisplayName() + ".");
		}
		victor.getLastDuelRules().setReceivedWins(true);
		sendSpoilsInterface(victor, loser);
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				victor.getControllerManager().startController("DuelControler");
				loser.getControllerManager().startController("DuelControler");
			}
		}, 2);
	}

	private void sendSpoilsInterface(Player victor, Player loser) {
		victor.getInterfaceManager().sendInterface(634);
		victor.getPackets().sendIComponentSettings(634, 28, 0, 28, 1026);
		victor.getPackets().sendRunScript(149, "", "", "", "", "", -1, 0, 6, 6, 136, 634 << 16 | 28);
		final ItemsContainer<Item> spoils = new ItemsContainer<>(100, false);
		spoils.addAll(loser.getLastDuelRules().getStake().getItems());
		spoils.addAll(victor.getLastDuelRules().getStake().getItems());
		victor.getPackets().sendItems(136, false, spoils);
		victor.getPackets().sendGlobalString(274, " " + Utils.formatPlayerNameForDisplay(loser.getUsername()));
		victor.getPackets().sendIComponentText(634, 32, "" + loser.getSkills().getCombatLevelWithSummoning());
	}

	private void refundItems(Player player) {
		for (Item item : player.getLastDuelRules().getStake().getItems()) {
			if (item == null) {
				continue;
			}
			player.getInventory().addItem(item);
			CoresManager.LOG_PROCESSOR.appendLog(new GameLog("duel_arena", player.getUsername(), "Drawed a duel and received their items back: " + item));
		}
		player.sendMessage("The duel was a draw so you received your items back.");
	}

	private void startEndingTeleport(Player player) {
		WorldTile tile = LOBBY_TELEPORTS[Utils.random(LOBBY_TELEPORTS.length)];
		WorldTile teleTile = tile;
		for (int trycount = 0; trycount < 10; trycount++) {
			teleTile = new WorldTile(tile, 2);
			if (World.canMoveNPC(tile.getPlane(), teleTile.getX(), teleTile.getY(), player.getSize())) {
				break;
			}
			teleTile = tile;
		}
		player.setNextWorldTile(teleTile);
	}

	public void removeEquipment() {
		int slot = 0;
		for (int i = 10; i < 23; i++) {
			if (i == 14) {
				if (player.getEquipment().hasTwoHandedWeapon()) {
					ButtonHandler.sendRemove(player, Equipment.SLOT_WEAPON);
					ButtonHandler.sendRemove(player, Equipment.SLOT_SHIELD);
				}
			}
			if (player.getLastDuelRules().getRule(i)) {
				slot = i - 10;
				ButtonHandler.sendRemove(player, slot);
			}
		}
	}

	private void beginBattle(boolean started) {
		if (started) {
			WorldTile[] teleports = getPossibleWorldTiles();
			int random = Utils.getRandom(1);
			player.setNextWorldTile(random == 0 ? teleports[0] : teleports[1]);
			target.setNextWorldTile(random == 0 ? teleports[1] : teleports[0]);
		}
		player.stopAll();
		player.getLockManagement().lockAll(2000); // fixes mass click steps
		player.reset();
		isDueling = true;
		player.getAttributes().put("startedDuel", true);
		player.getAttributes().put("canFight", false);
		player.setCanPvp(true);
		player.getHintIconsManager().addHintIcon(target, 1, -1, false);
		WorldTasksManager.schedule(new WorldTask() {
			int count = 3;

			@Override
			public void run() {
				if (count > 0) {
					player.setNextForceTalk(new ForceTalk("" + count));
				}
				if (count == 0) {
					player.getAttributes().put("canFight", true);
					player.setNextForceTalk(new ForceTalk("FIGHT!"));
					this.stop();
				}
				count--;
			}
		}, 0, 1);
	}

	@Override
	public boolean canEat(Food food) {
		if (player.getLastDuelRules().getRule(4) && isDueling) {
			player.getPackets().sendGameMessage("You cannot eat during this duel.", true);
			return false;
		}
		return true;
	}

	@Override
	public boolean canPot(Pot pot) {
		if (player.getLastDuelRules().getRule(3) && isDueling) {
			player.getPackets().sendGameMessage("You cannot drink during this duel.", true);
			return false;
		}
		return true;
	}

	@Override
	public boolean canMove(int dir) {
		if (player.getLastDuelRules().getRule(25) && isDueling) {
			player.getPackets().sendGameMessage("You cannot move during this duel!", true);
			return false;
		}
		return true;
	}

	@Override
	public boolean canSummonFamiliar() {
		if (!player.getLastDuelRules().getRule(24)) {
			if (!isDueling) {
				player.getPackets().sendGameMessage("Summoning has been disabled during this duel!");
			}
			return false;
		}
		return true;
	}

	@Override
	public boolean canUseFamiliarSpecial() {
		if (!player.getLastDuelRules().getRule(24)) {
			player.getPackets().sendGameMessage("Summoning has been disabled during this duel!");
			return false;
		}
		return true;
	}

	@Override
	public boolean processMagicTeleport(WorldTile toTile) {
		player.getDialogueManager().startDialogue("SimpleMessage", "A magical force prevents you from teleporting from the arena.");
		return false;
	}

	@Override
	public boolean processItemTeleport(WorldTile toTile) {
		player.getDialogueManager().startDialogue("SimpleMessage", "A magical force prevents you from teleporting from the arena.");
		return false;
	}

	@Override
	public void magicTeleported(int type) {
		if (type != -1) {
			return;
		}
	}

	@Override
	public boolean processObjectClick1(WorldObject object) {
		player.getDialogueManager().startDialogue(ForfeitDialouge.class);
		return true;
	}

	@Override
	public boolean sendDeath() {
		player.getLockManagement().lockAll(7000);
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				player.stopAll();
				if (loop == 0) {
					player.setNextAnimation(new Animation(836));
				} else if (loop == 1) {
					player.getPackets().sendGameMessage("Oh dear, you have died.");
				} else if (loop == 3) {
					player.setNextAnimation(new Animation(-1));
					endDuel(target, player);
					this.stop();
				}
				loop++;
			}
		}, 0, 1);
		return false;
	}

	@Override
	public boolean login() {
		startEndingTeleport(player);
		removeController();
		return true;
	}

	@Override
	public boolean logout() {
		if (isDueling) {
			endDuel(target, player, false);
		} else {
			closeDuelInteraction(true, DuelStage.DECLINED);
		}
		return !isDueling;
	}

	@Override
	public boolean keepCombating(Entity victim) {
		DuelRules rules = player.getLastDuelRules();
		boolean isRanging = PlayerCombat.isRanging(player) != 0;
		if (player.getAttributes().get("canFight") == Boolean.FALSE) {
			player.getPackets().sendGameMessage("The duel hasn't started yet.", true);
			return false;
		}
		if (target != victim) {
			return false;
		}
		if (player.getCombatDefinitions().getSpellId() > 0 && rules.getRule(2) && isDueling) {
			player.getPackets().sendGameMessage("You cannot use Magic in this duel!", true);
			return false;
		} else if (isRanging && rules.getRule(0) && isDueling) {
			player.getPackets().sendGameMessage("You cannot use Range in this duel!", true);
			return false;
		} else if (!isRanging && rules.getRule(1) && player.getCombatDefinitions().getSpellId() <= 0 && isDueling) {
			player.getPackets().sendGameMessage("You cannot use Melee in this duel!", true);
			return false;
		}
		return true;
	}

	@Override
	public boolean canEquip(int slotId, int itemId) {
		DuelRules rules = player.getLastDuelRules();
	/*	for (int i = 0; i < rules.getDuelRuels().length; i++) {
			if (rules.getRule(i)) {
				System.out.println(i);
			}
		}*/
		if (isDueling) {
			if (rules.getRule(10 + slotId)) {
				player.getPackets().sendGameMessage("You can't equip " + ItemDefinitions.forId(itemId).getName().toLowerCase() + " during this duel.");
				return false;
			}
			if (slotId == 3 && player.getEquipment().hasTwoHandedWeapon() && rules.getRule(15)) {
				player.getPackets().sendGameMessage("You can't equip " + ItemDefinitions.forId(itemId).getName().toLowerCase() + " during this duel.");
				return false;
			}
			if (rules.getRule(8)) { // fun weapons
				player.sendMessage("You can't switch weapons in this duel.");
				return false;
			}
		}
		return true;
	}

	private WorldTile[] getPossibleWorldTiles() {
		final int arenaChoice = Utils.getRandom(2);
		WorldTile[] locations = new WorldTile[2];
		int[] arenaBoundariesX = { 3337, 3367, 3336 };
		int[] arenaBoundariesY = { 3246, 3227, 3208 };
		int[] maxOffsetX = { 14, 14, 16 };
		int[] maxOffsetY = { 10, 10, 10 };
		int finalX = arenaBoundariesX[arenaChoice] + Utils.getRandom(maxOffsetX[arenaChoice]);
		int finalY = arenaBoundariesY[arenaChoice] + Utils.getRandom(maxOffsetY[arenaChoice]);
		locations[0] = (new WorldTile(finalX, finalY, 0));
		if (player.getLastDuelRules().getRule(25)) {
			int direction = Utils.getRandom(1);
			if (direction == 0) {
				finalX--;
			} else {
				finalY++;
			}
		} else {
			finalX = arenaBoundariesX[arenaChoice] + Utils.getRandom(maxOffsetX[arenaChoice]);
			finalY = arenaBoundariesY[arenaChoice] + Utils.getRandom(maxOffsetY[arenaChoice]);
		}
		locations[1] = (new WorldTile(finalX, finalY, 0));
		return locations;
	}

	@Override
	public boolean processButtonClick(int interfaceId, int componentId, int slotId, int packetId) {
		synchronized (this) {
			synchronized (target.getControllerManager().getController()) {
				DuelRules rules = player.getLastDuelRules();
				switch (interfaceId) {
					case 749:
						if (componentId == 4 && rules.getRule(5) && isDueling) {
							player.getPackets().sendGameMessage("You can't use prayers in this duel.");
							return false;
						}
						return true;
					case 271:
						if (rules.getRule(5) && isDueling) {
							player.getPackets().sendGameMessage("You can't use prayers in this duel.");
							return false;
						}
						return true;
					case 193:
					case 430:
					case 192:
						return !(rules.getRule(2) && isDueling);
					case 884:
						if (componentId == 4) {
							if (rules.getRule(9) && isDueling) {
								player.getPackets().sendGameMessage("You can't use special attacks in this duel.");
								return false;
							}
						}
						return true;
					case 631:
						switch (componentId) {
							case 55: // no range
								rules.setRules(0);
								return false;
							case 56: // no melee
								rules.setRules(1);
								return false;
							case 57: // no magic
								rules.setRules(2);
								return false;
							case 58: // fun wep
								rules.setRules(8);
								return false;
							case 59: // no forfiet
								rules.setRules(7);
								return false;
							case 60: // no drinks
								rules.setRules(3);
								return false;
							case 61: // no food
								rules.setRules(4);
								return false;
							case 62: // no prayer
								rules.setRules(5);
								return false;
							case 63: // no movement
								rules.setRules(25);
								if (rules.getRule(6)) {
									rules.setRule(6, false);
									rules.setConfigs();
									player.getPackets().sendGameMessage("You can't have movement without obstacles.");
								}
								return false;
							case 64: // obstacles
								rules.setRules(6);
								if (rules.getRule(25)) {
									rules.setRule(25, false);
									rules.setConfigs();
									player.getPackets().sendGameMessage("You can't have obstacles without movement.");
								}
								return false;
							case 65: // enable summoning
								rules.setRules(24);
								return false;
							case 66:// no spec
								rules.setRules(9);
								return false;
							case 21:// no helm
								rules.setRules(10);
								return false;
							case 22:// no cape
								rules.setRules(11);
								return false;
							case 23:// no ammy
								rules.setRules(12);
								return false;
							case 31:// arrows
								rules.setRules(23);
								return false;
							case 24:// weapon
								rules.setRules(13);
								return false;
							case 25:// body
								rules.setRules(14);
								return false;
							case 26:// shield
								rules.setRules(15);
								return false;
							case 27:// legs
								rules.setRules(17);
								return false;
							case 28:// ring
								rules.setRules(19);
								return false;
							case 29: // bots
								rules.setRules(20);
								return false;
							case 30: // gloves
								rules.setRules(22);
								return false;
							case 107:
								closeDuelInteraction(true, DuelStage.DECLINED);
								return false;
							case 46:
								accept(true);
								return false;
							case 47:
								switch (packetId) {
									case WorldPacketsDecoder.ACTION_BUTTON1_PACKET:
										removeItem(slotId, 1);
										return false;
									case WorldPacketsDecoder.ACTION_BUTTON2_PACKET:
										removeItem(slotId, 5);
										return false;
									case WorldPacketsDecoder.ACTION_BUTTON3_PACKET:
										removeItem(slotId, 10);
										return false;
									case WorldPacketsDecoder.ACTION_BUTTON4_PACKET:
										removeItem(slotId, Integer.MAX_VALUE);
										return false;
									case WorldPacketsDecoder.ACTION_BUTTON5_PACKET:
										player.getPackets().requestClientInput(new InputEvent("Enter Amount:", InputEventType.INTEGER) {
											@Override
											public void handleInput() {
												removeItem(slotId, getInput());
											}
										});
										return false;
									case WorldPacketsDecoder.ACTION_BUTTON6_PACKET:
										player.getInventory().sendExamine(slotId);
										return false;
								}
								return false;
						}
					case 628:
						switch (packetId) {
							case WorldPacketsDecoder.ACTION_BUTTON1_PACKET:
								addItem(slotId, 1);
								return false;
							case WorldPacketsDecoder.ACTION_BUTTON2_PACKET:
								addItem(slotId, 5);
								return false;
							case WorldPacketsDecoder.ACTION_BUTTON3_PACKET:
								addItem(slotId, 10);
								return false;
							case WorldPacketsDecoder.ACTION_BUTTON4_PACKET:
								addItem(slotId, Integer.MAX_VALUE);
								return false;
							case WorldPacketsDecoder.ACTION_BUTTON5_PACKET:
								player.getPackets().requestClientInput(new InputEvent("Enter Amount:", InputEventType.INTEGER) {
									@Override
									public void handleInput() {
										Item item = player.getInventory().getItems().get(slotId);
										if (item == null) {
											return;
										}
										addItem(slotId, getInput());
									}
								});
								return false;
						}
					case 626:
						switch (componentId) {
							case 43:
								accept(false);
								return false;
						}
					case 637: // friendly
						switch (componentId) {
							case 25: // no range
								rules.setRules(0);
								return false;
							case 26: // no melee
								rules.setRules(1);
								return false;
							case 27: // no magic
								rules.setRules(2);
								return false;
							case 28: // fun wep
								rules.setRules(8);
								return false;
							case 29: // no forfiet
								rules.setRules(7);
								return false;
							case 30: // no drinks
								rules.setRules(3);
								return false;
							case 31: // no food
								rules.setRules(4);
								return false;
							case 32: // no prayer
								rules.setRules(5);
								return false;
							case 33: // no movement
								rules.setRules(25);
								if (rules.getRule(6)) {
									rules.setRule(6, false);
									rules.setConfigs();
									player.getPackets().sendGameMessage("You can't have movement without obstacles.");
								}
								return false;
							case 34: // obstacles
								rules.setRules(6);
								if (rules.getRule(25)) {
									rules.setRule(25, false);
									rules.setConfigs();
									player.getPackets().sendGameMessage("You can't have obstacles without movement.");
								}
								return false;
							case 35: // enable summoning
								rules.setRules(24);
								return false;
							case 36:// no spec
								rules.setRules(9);
								return false;
							case 43:// no helm
								rules.setRules(10);
								return false;
							case 44:// no cape
								rules.setRules(11);
								return false;
							case 45:// no ammy
								rules.setRules(12);
								return false;
							case 53:// arrows
								rules.setRules(23);
								return false;
							case 46:// weapon
								rules.setRules(13);
								return false;
							case 47:// body
								rules.setRules(14);
								return false;
							case 48:// shield
								rules.setRules(15);
								return false;
							case 49:// legs
								rules.setRules(17);
								return false;
							case 50:// ring
								rules.setRules(19);
								return false;
							case 51: // bots
								rules.setRules(20);
								return false;
							case 52: // gloves
								rules.setRules(22);
								return false;
							case 86:
								closeDuelInteraction(true, DuelStage.DECLINED);
								return false;
							case 21:
								accept(true);
								return false;
						}
					case 639:
						switch (componentId) {
							case 25:
								accept(false);
								return false;
						}
				}
			}
		}
		return true;
	}

	public boolean isDueling() {
		return isDueling;
	}

	public boolean hasTarget() {
		return target != null;
	}

	public Entity getTarget() {
		if (hasTarget()) {
			return target;
		}
		return null;
	}

	enum DuelStage {
		DECLINED,
		NO_SPACE,
		SECOND,
		DONE
	}
}
