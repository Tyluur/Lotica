package com.runescape.game.world.entity.player;

import com.runescape.game.world.entity.player.rights.RightManager;
import com.runescape.game.world.item.Item;
import com.runescape.game.world.item.ItemConstants;
import com.runescape.game.world.item.ItemsContainer;
import com.runescape.utility.ChatColors;
import com.runescape.utility.external.gson.loaders.ExchangePriceLoader;
import com.runescape.utility.external.gson.loaders.ItemInformationLoader;
import com.runescape.utility.external.gson.loaders.LentItemsLoader;
import com.runescape.workers.game.core.CoresManager;
import com.runescape.workers.game.log.GameLog;

import java.util.stream.Stream;

public class Trade {

	/** The important player objects in this class. Us and the recipient of our items */
	private Player player, target;

	/** The items we are offering */
	private ItemsContainer<Item> items;

	/** The item we're offering to lend */
	private ItemsContainer<Item> itemToLend;

	/** If the item we're lending is lent until logout */
	private boolean lentTillLogout;

	/** The time the item was lent for */
	private long hoursLentFor;

	/** If the trade page was modified */
	private boolean tradeModified;

	/** If we have accepted */
	private boolean accepted;

	public Trade(Player player) {
		this.player = player; //player reference
		items = new ItemsContainer<>(28, false);
		itemToLend = new ItemsContainer<>(1, false);
	}

	/*
	 * called to both players
	 */
	public void openTrade(Player target) {
		synchronized (this) {
			synchronized (target.getTrade()) {
				if (!player.getControllerManager().canTrade() || !target.getControllerManager().canTrade()) {
					return;
				}
				this.target = target;
				this.lentTillLogout = true;
				this.hoursLentFor = 0;
				player.getPackets().sendIComponentText(335, 15, "Trading With: " + target.getDisplayName());
				player.getPackets().sendGlobalString(203, target.getDisplayName());
				sendInterItems();
				sendOptions();
				sendTradeModified();
				refreshFreeInventorySlots();
				refreshTradeWealth();
				refreshStageMessage(true);
				refreshLentItems();
				player.getInterfaceManager().sendInterface(335);
				player.getInterfaceManager().sendInventoryInterface(336);
				player.setCloseInterfacesEvent(() -> closeTrade(CloseTradeStage.CANCEL));
			}
		}
	}

	public void removeItem(final int slot, int amount) {
		synchronized (this) {
			if (!isTrading()) {
				return;
			}
			if (!player.getInterfaceManager().containsInventoryInter()) {
				System.out.println(player.getUsername() + " attempted to remove items after trade stage.");
				return;
			}
			synchronized (target.getTrade()) {
				Item item = items.get(slot);
				if (item == null) {
					return;
				}
				Item[] itemsBefore = items.getItemsCopy();
				int maxAmount = items.getNumberOf(item);
				if (amount < maxAmount) {
					item = new Item(item.getId(), amount);
				} else {
					item = new Item(item.getId(), maxAmount);
				}
				items.remove(slot, item);
				player.getInventory().addItemDrop(item);
				refreshItems(itemsBefore);
				cancelAccepted();
				setTradeModified(true);
			}
		}
	}

	public void sendFlash(int slot) {
		target.getPackets().sendInterFlashScript(335, 33, 4, 7, slot);
		player.getPackets().sendInterFlashScript(335, 36, 4, 7, slot);
	}

	public void cancelAccepted() {
		boolean canceled = false;
		if (accepted) {
			accepted = false;
			canceled = true;
		}
		if (target.getTrade().accepted) {
			target.getTrade().accepted = false;
			canceled = true;
		}
		if (canceled) {
			refreshBothStageMessage(true);
		}
	}

	public void addItem(Item item, boolean deleteFromInventory) {
		synchronized (this) {
			if (!isTrading()) {
				return;
			}
			synchronized (target.getTrade()) {
				if (item == null) {
					return;
				}
				if (!ItemConstants.isTradeable(item) && !player.hasPrivilegesOf(RightManager.OWNER, RightManager.ADMINISTRATOR)) {
					player.getPackets().sendGameMessage("That item isn't tradeable.");
					return;
				}
				Item[] itemsBefore = items.getItemsCopy();
				items.add(item);
				if (deleteFromInventory) {
					player.getInventory().deleteItem(item);
				}
				refreshItems(itemsBefore);
				cancelAccepted();
			}
		}
	}

	public void addItem(int slot, int amount) {
		synchronized (this) {
			if (!isTrading()) {
				return;
			}
			synchronized (target.getTrade()) {
				Item item = player.getInventory().getItem(slot);
				if (item == null) {
					return;
				}
				if (!ItemConstants.isTradeable(item) && !player.hasPrivilegesOf(RightManager.OWNER, RightManager.ADMINISTRATOR)) {
					player.getPackets().sendGameMessage("That item isn't tradeable.");
					return;
				}
				Item[] itemsBefore = items.getItemsCopy();
				int maxAmount = player.getInventory().getItems().getNumberOf(item);
				if (amount < maxAmount) {
					item = new Item(item.getId(), amount);
				} else {
					item = new Item(item.getId(), maxAmount);
				}
				items.add(item);
				player.getInventory().deleteItem(slot, item);
				refreshItems(itemsBefore);
				cancelAccepted();
			}
		}
	}

	public void addLendItem(int slot) {
		synchronized (this) {
			if (!isTrading()) {
				return;
			}
			synchronized (target.getTrade()) {
				if (LentItemsLoader.hasLentItem(target)) {
					player.sendMessage("You can't lend any items to " + target.getDisplayName() + ", they are already borrowing an item.");
					return;
				}
				Item item = player.getInventory().getItem(slot);
				if (item == null || item.getDefinitions().getLendId() == -1 || !ItemConstants.isTradeable(item)) {
					player.sendMessage("You can't lend that item.");
					return;
				}
				if (itemToLend.add(item)) {
					player.getInventory().deleteItem(slot, item);
					refreshLentItems();
					refreshLendHours();
				}
			}
		}
	}

	public void refreshItems(Item[] itemsBefore) {
		int[] changedSlots = new int[itemsBefore.length];
		int count = 0;
		for (int index = 0; index < itemsBefore.length; index++) {
			Item item = items.getItems()[index];
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
		refreshFreeInventorySlots();
		refreshTradeWealth();
		refreshLentItems();
	}

	public void refreshLentItems() {
		player.getPackets().sendItems(541, false, itemToLend);
		target.getPackets().sendItems(541, true, itemToLend);
	}

	public void sendOptions() {
		Object[] tparams1 = new Object[] { "", "", "", "Value<col=FF9040>", "Remove-X", "Remove-All", "Remove-10", "Remove-5", "Remove", -1, 0, 7, 4, 90, 335 << 16 | 31 };
		player.getPackets().sendRunScript(150, tparams1);
		player.getPackets().sendIComponentSettings(335, 31, 0, 27, 1150); // Access
		Object[] tparams3 = new Object[] { "", "", "", "", "", "", "", "", "Value<col=FF9040>", -1, 0, 7, 4, 90, 335 << 16 | 34 };
		player.getPackets().sendRunScript(695, tparams3);
		player.getPackets().sendIComponentSettings(335, 34, 0, 27, 1026); // Access
		Object[] tparams2 = new Object[] { "", "", "Lend", "Value<col=FF9040>", "Offer-X", "Offer-All", "Offer-10", "Offer-5", "Offer", -1, 0, 7, 4, 93, 336 << 16 };
		player.getPackets().sendRunScript(150, tparams2);
		player.getPackets().sendIComponentSettings(336, 0, 0, 27, 1278); // Access
		// lending box
		for (int i = 50; i < 58; i++) { player.getPackets().sendIComponentSettings(335, i, -1, -1, 6); }
	}

	public boolean isTrading() {
		return target != null;
	}

	public void setTradeModified(boolean modified) {
		if (modified == tradeModified) {
			return;
		}
		tradeModified = modified;
		sendTradeModified();
	}

	public void sendInterItems() {
		player.getPackets().sendItems(90, items);
		target.getPackets().sendItems(90, true, items);
	}

	public void refresh(int... slots) {
		player.getPackets().sendUpdateItems(90, items, slots);
		target.getPackets().sendUpdateItems(90, true, items.getItems(), slots);
	}

	public void accept(boolean firstStage) {
		synchronized (this) {
			if (!isTrading()) {
				return;
			}
			synchronized (target.getTrade()) {
				if (target.getTrade().accepted) {
					if (firstStage) {
						if (nextStage()) {
							target.getTrade().nextStage();
						}
					} else {
						player.setCloseInterfacesEvent(null);
						player.closeInterfaces();
						closeTrade(CloseTradeStage.DONE);
					}
					return;
				}
				accepted = true;
				refreshBothStageMessage(firstStage);
			}
		}
	}

	public void sendValue(int slot, boolean traders) {
		if (!isTrading()) {
			return;
		}
		Item item = traders ? target.getTrade().items.get(slot) : items.get(slot);
		if (item == null) {
			return;
		}
		if (!ItemConstants.isTradeable(item)) {
			player.getPackets().sendGameMessage("That item isn't tradeable.");
			return;
		}
		player.getPackets().sendGameMessage(item.getDefinitions().getName() + ": market price is " + ExchangePriceLoader.getEconomicalPrice(item.getId()) + " coins.");
	}

	public void sendValue(int slot) {
		Item item = player.getInventory().getItem(slot);
		if (item == null) {
			return;
		}
		if (!ItemConstants.isTradeable(item)) {
			player.getPackets().sendGameMessage("That item isn't tradeable.");
			return;
		}
		player.getPackets().sendGameMessage(item.getDefinitions().getName() + ": market price is " + ExchangePriceLoader.getEconomicalPrice(item.getId()) + " coins.");
	}

	public void sendExamine(int slot, boolean traders) {
		if (!isTrading()) {
			return;
		}
		Item item = traders ? target.getTrade().items.get(slot) : items.get(slot);
		if (item == null) {
			return;
		}
		player.getPackets().sendGameMessage(ItemInformationLoader.getExamine(item.getId()));
	}

	public boolean nextStage() {
		if (!isTrading()) {
			return false;
		}
		if (player.getInventory().getItems().getUsedSlots() + target.getTrade().items.getUsedSlots() > 28) {
			player.setCloseInterfacesEvent(null);
			player.closeInterfaces();
			closeTrade(CloseTradeStage.NO_SPACE);
			return false;
		}
		Item targetLend = target.getTrade().getItemToLend();
		if (targetLend != null && !player.getInventory().getItems().canAdd(targetLend)) {
			player.setCloseInterfacesEvent(null);
			player.closeInterfaces();
			closeTrade(CloseTradeStage.NO_SPACE);
			return false;
		}
		accepted = false;
		player.getInterfaceManager().sendInterface(334);
		player.getInterfaceManager().closeInventoryInterface();
		player.getPackets().sendHideIComponent(334, 55, !(tradeModified || target.getTrade().tradeModified));
		refreshBothStageMessage(false);
		sentLentMessage();
		return true;
	}

	public void sentLentMessage() {
		Item playerLoan = player.getTrade().getItemToLend();
		if (playerLoan != null) {
			player.sendMessage("<col=" + ChatColors.MAROON + ">You are loaning " + playerLoan.getName() + " " + (lentTillLogout ? "until log out." : "for " + hoursLentFor + " hours."));
		}
		Item targetItem = target.getTrade().getItemToLend();
		if (targetItem != null) {
			player.sendMessage("<col=" + ChatColors.RED + ">" + target.getDisplayName() + " is loaning you " + targetItem.getName() + " " + (target.getTrade().lentTillLogout ? "until log out." : "for " + target.getTrade().hoursLentFor + " hours."));
		}
	}

	public void refreshLendHours() {
		int lentTime = (int) hoursLentFor;
		//player.getPackets().sendConfig(259, lentTime);
		player.getVarsManager().forceSendVarBit(5026, lentTime);//Left side
		target.getVarsManager().forceSendVarBit(5070, lentTime);
		//System.out.println("lent for: " + lentTime);
	}

	public void refreshBothStageMessage(boolean firstStage) {
		refreshStageMessage(firstStage);
		target.getTrade().refreshStageMessage(firstStage);
	}

	public void refreshStageMessage(boolean firstStage) {
		player.getPackets().sendIComponentText(firstStage ? 335 : 334, firstStage ? 37 : 34, getAcceptMessage(firstStage));
	}

	public String getAcceptMessage(boolean firstStage) {
		if (accepted) {
			return "Waiting for other player...";
		}
		if (target.getTrade().accepted) {
			return "Other player has accepted.";
		}
		return firstStage ? "" : "Are you sure you want to make this trade?";
	}

	public void sendTradeModified() {
		player.getPackets().sendConfig(1042, tradeModified ? 1 : 0);
		target.getPackets().sendConfig(1043, tradeModified ? 1 : 0);
	}

	public void refreshTradeWealth() {
		int wealth = getTradeWealth();
		player.getPackets().sendGlobalConfig(729, wealth);
		target.getPackets().sendGlobalConfig(697, wealth);
	}

	public void refreshFreeInventorySlots() {
		int freeSlots = player.getInventory().getFreeSlots();
		target.getPackets().sendIComponentText(335, 21, "has " + (freeSlots == 0 ? "no" : freeSlots) + " free" + "<br>inventory slots");
	}

	public int getTradeWealth() {
		int wealth = 0;
		for (Item item : items.getItems()) {
			if (item == null) {
				continue;
			}
			wealth += ExchangePriceLoader.getEconomicalPrice(item.getId()) * item.getAmount();
		}
		return wealth;
	}

	private enum CloseTradeStage {
		CANCEL,
		NO_SPACE,
		DONE
	}

	public void closeTrade(CloseTradeStage stage) {
		synchronized (this) {
			synchronized (target.getTrade()) {
				Player oldTarget = target;
				target = null;
				tradeModified = false;
				accepted = false;
				if (CloseTradeStage.DONE != stage) {
					Stream.of(items.toArray()).filter(item -> item != null).forEach(item -> player.getInventory().addItemDrop(item));
					Stream.of(itemToLend.toArray()).filter(item -> item != null).forEach(item -> player.getInventory().addItemDrop(item));
					items.clear();
					itemToLend.clear();
					player.getInterfaceManager().closeInventoryInterface();
					player.getInventory().init();
				} else {
					player.getPackets().sendGameMessage("Accepted trade.");
					if (oldTarget.getTrade().getItemToLend() != null) {
						LentItemsLoader.registerItemToLend(oldTarget, player, oldTarget.getTrade().getItemToLend(), oldTarget.getTrade().lentTillLogout, oldTarget.getTrade().hoursLentFor);
						oldTarget.getTrade().itemToLend.clear();
					}
					for (Item item : oldTarget.getTrade().items.toArray()) {
						if (item == null) {
							continue;
						}
						CoresManager.LOG_PROCESSOR.appendLog(new GameLog("trade", player.getUsername(), "Finished trade session & received:\t[tradee=" + oldTarget.getUsername() + ", item=" + item + "]"));
						CoresManager.LOG_PROCESSOR.appendLog(new GameLog("trade", oldTarget.getUsername(), "Finished trade session & gave item:\t[tradee=" + player.getUsername() + ", item=" + item + "]"));
					}
					player.getInventory().getItems().addAll(oldTarget.getTrade().items);
					player.getInventory().init();
					oldTarget.getTrade().items.clear();
				}
				if (oldTarget.getTrade().isTrading()) {
					oldTarget.setCloseInterfacesEvent(null);
					oldTarget.closeInterfaces();
					oldTarget.getTrade().closeTrade(stage);
					if (CloseTradeStage.CANCEL == stage) {
						oldTarget.getPackets().sendGameMessage("<col=ff0000>Other player declined trade!");
					} else if (CloseTradeStage.NO_SPACE == stage) {
						player.getPackets().sendGameMessage("You don't have enough space in your inventory for this trade.");
						oldTarget.getPackets().sendGameMessage("Other player doesn't have enough space in their inventory for this trade.");
					}
				}
			}
		}
	}

	public Player getTarget() {
		return target;
	}

	/**
	 * Gets the item to lend
	 *
	 * @return The item
	 */
	public Item getItemToLend() {
		for (Item item : itemToLend.toArray()) {
			if (item == null) {
				continue;
			}
			return item;
		}
		return null;
	}

	public long getHoursLentFor() {
		return hoursLentFor;
	}

	public void setHoursLentFor(long hoursLentFor) {
		this.hoursLentFor = hoursLentFor;
	}

	public boolean isLentTillLogout() {
		return lentTillLogout;
	}

	public void setLentTillLogout(boolean lentTillLogout) {
		this.lentTillLogout = lentTillLogout;
	}
}
