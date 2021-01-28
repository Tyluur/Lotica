package com.runescape.game.world.entity.player;

import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.game.GameConstants;
import com.runescape.game.content.BankPinManager;
import com.runescape.game.content.MoneyPouchManagement;
import com.runescape.game.interaction.controllers.impl.Wilderness;
import com.runescape.game.world.entity.npc.familiar.impl.Familiar;
import com.runescape.game.world.item.Item;
import com.runescape.utility.external.gson.loaders.ItemInformationLoader;
import com.runescape.workers.game.core.CoresManager;
import com.runescape.workers.game.log.GameLog;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Bank implements Serializable {

	public static final int MAX_ULTIMATE_CAN_HAVE = 28;

	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");

	/**
	 *
	 */
	private static final long serialVersionUID = 1551246756081236625L;

	private static final long MAX_BANK_SIZE = 506;

	//tab, items
	private Item[][] bankTabs;

	@SuppressWarnings("unused")
	private short bankPin;

	private int lastX;

	private transient Player player;

	private transient int currentTab;

	private transient Item[] lastContainerCopy;

	private transient boolean withdrawNotes;

	private transient boolean insertItems;

	public Bank() {
		bankTabs = new Item[1][0];
	}

	public void removeItem(int id) {
		if (bankTabs != null) {
			for (Item[] bankTab : bankTabs) {
				for (int i2 = 0; i2 < bankTab.length; i2++) {
					if (bankTab[i2].getId() == id) {
						bankTab[i2].setId(0); //dwarf remains
					}
				}
			}
		}
	}

	public void setPlayer(Player player) {
		this.player = player;
		if (bankTabs == null || bankTabs.length == 0) {
			bankTabs = new Item[1][0];
		}
	}

	@SuppressWarnings("null")
	public void setItem(int slotId, int amt) {
		Item item = getItem(slotId);
		if (item == null) {
			item.setAmount(amt);
			refreshItems();
			refreshTabs();
			refreshViewingTab();
		}
	}

	public void withdrawLastAmount(int bankSlot) {
		withdrawItem(bankSlot, lastX);
	}

	public void withdrawItemButOne(int fakeSlot) {
		int[] fromRealSlot = getRealSlot(fakeSlot);
		Item item = getItem(fromRealSlot);
		if (item == null) {
			return;
		}
		if (item.getAmount() <= 1) {
			player.getPackets().sendGameMessage("You only have one of this item in your bank");
			return;
		}
		withdrawItem(fakeSlot, item.getAmount() - 1);
	}

	public void depositLastAmount(int bankSlot) {
		depositItem(bankSlot, lastX, true);
	}

	public boolean depositAllInventory(boolean banking) {
		if ((player.isUltimateIronman() ? MAX_ULTIMATE_CAN_HAVE : Bank.MAX_BANK_SIZE) - getBankSize() < player.getInventory().getItems().getUsedSlots()) {
			player.getPackets().sendGameMessage("Not enough space in your bank.");
			return false;
		}
		for (int i = 0; i < 28; i++) {
			depositItem(i, Integer.MAX_VALUE, false);
		}
		refreshTab(currentTab);
		refreshItems();
		return true;
	}

	public void depositAllBob(boolean banking) {
		Familiar familiar = player.getFamiliar();
		if (familiar == null || familiar.getBob() == null) {
			return;
		}
		int space = addItems(familiar.getBob().getBeastItems().getItems(), banking);
		if (space != 0) {
			for (int i = 0; i < space; i++) {
				familiar.getBob().getBeastItems().set(i, null);
			}
			familiar.getBob().sendInterItems();
		}
		if (space < familiar.getBob().getBeastItems().getSize()) {
			player.getPackets().sendGameMessage("Not enough space in your bank.");
			return;
		}
	}

	public boolean depositAllEquipment(boolean banking) {
		int space = addItems(player.getEquipment().getItems().getItems(), banking);
		if (space != 0) {
			for (int i = 0; i < space; i++) {
				player.getEquipment().getItems().set(i, null);
			}
			player.getEquipment().init();
			player.getAppearence().generateAppearenceData();
		}
		if (space < player.getEquipment().getItems().getSize()) {
			player.getPackets().sendGameMessage("Not enough space in your bank.");
			return false;
		}
		return true;
	}

	public void depositMoneyPouch(boolean banking) {
		int numCoins = player.getFacade().getMoneyPouchCoins();
		if (numCoins <= 0) {
			player.sendMessage("You have no coins in your money pouch to deposit.");
			return;
		}
		int space = addItems(new Item[] { new Item(995, numCoins) }, banking);
		//System.out.println(space);
		if (space != 0) {
			player.getFacade().setMoneyPouchCoins(0);
			MoneyPouchManagement.sendCoinAmount(player);
		}

	}

	public void collapse(int tabId) {
		if (tabId == 0 || tabId >= bankTabs.length) {
			return;
		}
		Item[] items = bankTabs[tabId];
		for (Item item : items) {
			removeItem(getItemSlot(item.getId()), item.getAmount(), false, true);
		}
		for (Item item : items) {
			addItem(item.getId(), item.getAmount(), 0, false);
		}
		refreshTabs();
		refreshItems();
	}

	public void switchItem(int fromSlot, int toSlot, int fromComponentId, int toComponentId) {
//		System.out.println(fromSlot + ", " + toSlot + ", " + fromComponentId + ", " + toComponentId);
		if (toSlot == 65535) {
			int toTab = toComponentId >= 74 ? 8 - (82 - toComponentId) : 9 - ((toComponentId - 44) / 2);
			if (toTab < 0 || toTab > 9) {
				return;
			}
			if (bankTabs.length == toTab) {
				int[] fromRealSlot = getRealSlot(fromSlot);
				if (fromRealSlot == null) {
					return;
				}
				if (toTab == fromRealSlot[0]) {
					switchItem(fromSlot, getStartSlot(toTab));
					return;
				}
				Item item = getItem(fromRealSlot);
				if (item == null) {
					return;
				}
				removeItem(fromSlot, item.getAmount(), false, true);
				createTab();
				bankTabs[bankTabs.length - 1] = new Item[] { item };
				refreshTab(fromRealSlot[0]);
				refreshTab(toTab);
				refreshItems();
			} else if (bankTabs.length > toTab) {
				int[] fromRealSlot = getRealSlot(fromSlot);
				if (fromRealSlot == null) {
					return;
				}
				if (toTab == fromRealSlot[0]) {
					switchItem(fromSlot, getStartSlot(toTab));
					return;
				}
				Item item = getItem(fromRealSlot);
				if (item == null) {
					return;
				}
				boolean removed = removeItem(fromSlot, item.getAmount(), false, true);
				if (!removed) {
					refreshTab(fromRealSlot[0]);
				} else if (fromRealSlot[0] != 0 && toTab >= fromRealSlot[0]) {
					toTab -= 1;
				}
				refreshTab(fromRealSlot[0]);
				addItem(item.getId(), item.getAmount(), toTab, true);
			}
		} else {
			if (insertItems) {
				insertItem(fromSlot, toSlot);
			} else {
				switchItem(fromSlot, toSlot);
			}
		}
	}

	public void insertItem(int fromSlot, int toSlot) {
		int[] fromRealSlot = getRealSlot(fromSlot);
		Item fromItem = getItem(fromRealSlot);
		if (fromItem == null) {
			return;
		}
		int[] toRealSlot = getRealSlot(toSlot);
		Item toItem = getItem(toRealSlot);
		if (toItem == null) {
			return;
		}
		if (toRealSlot[0] != fromRealSlot[0]) {
			bankTabs[fromRealSlot[0]][fromRealSlot[1]] = toItem;
			bankTabs[toRealSlot[0]][toRealSlot[1]] = fromItem;
		} else {
			if (toRealSlot[1] > fromRealSlot[1]) {
				for (int i = fromRealSlot[1]; i < toRealSlot[1]; i++) {
					Item toShift = bankTabs[toRealSlot[0]][fromRealSlot[1] += 1];
					bankTabs[fromRealSlot[0]][i] = toShift;
				}
			} else if (fromRealSlot[1] > toRealSlot[1]) {
				for (int i = fromRealSlot[1]; i > toRealSlot[1]; i--) {
					Item toShift = bankTabs[toRealSlot[0]][fromRealSlot[1] -= 1];
					bankTabs[fromRealSlot[0]][i] = toShift;
				}
			}
			bankTabs[toRealSlot[0]][toRealSlot[1]] = fromItem;
		}
		refreshTab(fromRealSlot[0]);
		if (fromRealSlot[0] != toRealSlot[0]) {
			refreshTab(toRealSlot[0]);
		}
		refreshItems();
	}

	public void switchItem(int fromSlot, int toSlot) {
		int[] fromRealSlot = getRealSlot(fromSlot);
		Item fromItem = getItem(fromRealSlot);
		if (fromItem == null) {
			return;
		}
		int[] toRealSlot = getRealSlot(toSlot);
		Item toItem = getItem(toRealSlot);
		if (toItem == null) {
			return;
		}
		bankTabs[fromRealSlot[0]][fromRealSlot[1]] = toItem;
		bankTabs[toRealSlot[0]][toRealSlot[1]] = fromItem;
		refreshTab(fromRealSlot[0]);
		if (fromRealSlot[0] != toRealSlot[0]) {
			refreshTab(toRealSlot[0]);
		}
		refreshItems();
	}

	public void openDepositBox() {
		player.getInterfaceManager().sendInterface(11);
		player.getInterfaceManager().closeInventory();
		player.getInterfaceManager().closeEquipment();
		final int lastGameTab = player.getInterfaceManager().openGameTab(9); // friends
		// tab
		sendBoxInterItems();
		player.getPackets().sendIComponentText(11, 13, "Bank Of " + GameConstants.SERVER_NAME + " - Deposit Box");
		player.setCloseInterfacesEvent(() -> {
			player.getInterfaceManager().sendInventory();
			player.getInventory().unlockInventoryOptions();
			player.getInterfaceManager().sendEquipment();
			player.getInterfaceManager().openGameTab(lastGameTab);
		});
	}

	public void sendBoxInterItems() {
		player.getPackets().sendInterSetItemsOptionsScript(11, 17, 93, 6, 5, "Deposit-1", "Deposit-5", "Deposit-10", "Deposit-All", "Deposit-X", "Examine");
		player.getPackets().sendUnlockIComponentOptionSlots(11, 17, 0, 27, 0, 1, 2, 3, 4, 5);
	}

	public void openBank() {
		BankPinManager manager = player.getPinManager();
		if (manager.getPinCancelationTime() != -1 && System.currentTimeMillis() >= manager.getPinCancelationTime()) {
			manager.setPinCancelationTime(-1);
			manager.deletePinVariables();
			player.sendMessage("Your bank pin has just been deleted!");
		}
		if (!player.getSession().isMasterSession() && !player.getPinManager().enteredPinDuringSession() && player.getPinManager().hasPin()) {
			player.putAttribute("entering_pin", "bank");
			player.setCloseInterfacesEvent(() -> player.removeAttribute("entering_pin"));
			manager.showEnterPin(true);
			if (!player.getAttribute("showed_pin_delete_time", false) && manager.getPinCancelationTime() != -1 && System.currentTimeMillis() < manager.getPinCancelationTime()) {
				player.sendMessage("Your bank pin will be deleted on: " + DATE_FORMAT.format(new Date(manager.getPinCancelationTime())));
				player.putAttribute("showed_pin_delete_time", true);
			}
			return;
		}
		player.getInterfaceManager().sendInterface(762);
		player.getInterfaceManager().sendInventoryInterface(763);
		refreshViewingTab();
		refreshTabs();
		unlockButtons();
		sendItems();
		refreshLastX();
	}

	public void refreshViewingTab() {
		player.getPackets().sendConfigByFile(4893, currentTab + 1);
	}

	public void refreshTabs() {
		for (int slot = 1; slot < 9; slot++) {
			refreshTab(slot);
		}
	}
	
	public void unlockButtons() {
		int interfaceId = 762;
		// removing the equipment stats button
		player.getPackets().sendHideIComponent(interfaceId, 117, true);
		player.getPackets().sendHideIComponent(interfaceId, 118, true);
		// unlock bank inter all options
		player.getPackets().sendIComponentSettings(interfaceId, 93, 0, 516, 2622718);
		// unlock bank inv all options
		player.getPackets().sendIComponentSettings(763, 0, 0, 27, 2425982);
	}

	public void sendItems() {
		player.getPackets().sendItems(95, getContainerCopy());
	}

	public void refreshLastX() {
		player.getPackets().sendConfig(1249, lastX);
	}

	public void refreshTab(int slot) {
		if (slot == 0) {
			return;
		}
		player.getPackets().sendConfigByFile(4885 + (slot - 1), getTabSize(slot));
	}

	public Item[] getContainerCopy() {
		if (lastContainerCopy == null) {
			lastContainerCopy = generateContainer();
		}
		return lastContainerCopy;
	}

	public int getTabSize(int slot) {
		if (slot >= bankTabs.length) {
			return 0;
		}
		return bankTabs[slot].length;
	}

	public Item[] generateContainer() {
		Item[] container = new Item[getBankSize()];
		int count = 0;
		for (int slot = 1; slot < bankTabs.length; slot++) {
			System.arraycopy(bankTabs[slot], 0, container, count, bankTabs[slot].length);
			count += bankTabs[slot].length;
		}
		System.arraycopy(bankTabs[0], 0, container, count, bankTabs[0].length);
		return container;
	}

	public int getBankSize() {
		int size = 0;
		for (Item[] bankTab : bankTabs) {
			size += bankTab.length;
		}
		return size;
	}

	public void createTab() {
		int slot = bankTabs.length;
		Item[][] tabs = new Item[slot + 1][];
		System.arraycopy(bankTabs, 0, tabs, 0, slot);
		tabs[slot] = new Item[0];
		bankTabs = tabs;
	}

	public void destroyTab(int slot) {
		Item[][] tabs = new Item[bankTabs.length - 1][];
		System.arraycopy(bankTabs, 0, tabs, 0, slot);
		System.arraycopy(bankTabs, slot + 1, tabs, slot, bankTabs.length - slot - 1);
		bankTabs = tabs;
		if (currentTab != 0 && currentTab >= slot) {
			currentTab--;
		}
	}

	public boolean hasBankSpace() {
		return getBankSize() < (player.isUltimateIronman() ? MAX_ULTIMATE_CAN_HAVE : MAX_BANK_SIZE);
	}

	public void withdrawItem(int bankSlot, int quantity) {
		if (quantity < 1) {
			return;
		}
		Item item = getItem(getRealSlot(bankSlot));
		if (item == null) {
			return;
		}
		if (player.getAttribute("viewing_banks", false)) {
			return;
		}
		if (item.getAmount() < quantity) {
			item = new Item(item.getId(), item.getAmount());
		} else {
			item = new Item(item.getId(), quantity);
		}
		boolean noted = false;
		ItemDefinitions defs = item.getDefinitions();
		if (withdrawNotes) {
			if (!defs.isNoted() && defs.getCertId() != -1) {
				item.setId(defs.getCertId());
				noted = true;
			} else {
				player.getPackets().sendGameMessage("You cannot withdraw this item as a note.");
			}
		}
		if (noted || defs.isStackable()) {
			if (player.getInventory().getItems().containsOne(item)) {
				int slot = player.getInventory().getItems().getThisItemSlot(item);
				Item invItem = player.getInventory().getItems().get(slot);
				if (invItem.getAmount() + item.getAmount() <= 0) {
					item.setAmount(Integer.MAX_VALUE - invItem.getAmount());
					player.getPackets().sendGameMessage("Not enough space in your inventory.");
				}
			} else if (!player.getInventory().hasFreeSlots()) {
				player.getPackets().sendGameMessage("Not enough space in your inventory.");
				return;
			}
		} else {
			int freeSlots = player.getInventory().getFreeSlots();
			if (freeSlots == 0) {
				player.getPackets().sendGameMessage("Not enough space in your inventory.");
				return;
			}
			if (freeSlots < item.getAmount()) {
				item.setAmount(freeSlots);
				player.getPackets().sendGameMessage("Not enough space in your inventory.");
			}
		}
		removeItem(bankSlot, item.getAmount(), true, false);
		player.getInventory().addItem(item);
		CoresManager.LOG_PROCESSOR.appendLog(new GameLog("bank", player.getUsername(), "Withdrew " + item.getAmount() + "x " + item.getName() + "[" + item.getId() + "] from the bank."));
	}

	public void sendExamine(int fakeSlot) {
		int[] slot = getRealSlot(fakeSlot);
		if (slot == null) {
			return;
		}
		Item item = bankTabs[slot[0]][slot[1]];
		player.getPackets().sendGameMessage(ItemInformationLoader.getExamine(item.getId()));
	}

	public int[] getRealSlot(int slot) {
		for (int tab = 1; tab < bankTabs.length; tab++) {
			if (slot >= bankTabs[tab].length) {
				slot -= bankTabs[tab].length;
			} else {
				return new int[] { tab, slot };
			}
		}
		if (slot >= bankTabs[0].length) {
			return null;
		}
		return new int[] { 0, slot };
	}

	public void depositItem(int invSlot, int quantity, boolean refresh) {
		if (quantity < 1 || invSlot < 0 || invSlot > 27) {
			return;
		}
		Item item = player.getInventory().getItem(invSlot);
		if (item == null) {
			return;
		}
		if (item.getId() == Wilderness.WILDERNESS_TOKEN && player.getControllerManager().verifyControlerForOperation(Wilderness.class).isPresent()) {
			player.sendMessage("You must leave the wilderness in order to deposit these tokens.");
			return;
		}
		int amt = player.getInventory().getItems().getNumberOf(item);
		if (amt < quantity) {
			item = new Item(item.getId(), amt);
		} else {
			item = new Item(item.getId(), quantity);
		}
		ItemDefinitions defs = item.getDefinitions();
		int originalId = item.getId();
		if (defs.isNoted() && defs.getCertId() != -1) {
			item.setId(defs.getCertId());
		}
		Item bankedItem = getItem(item.getId());
		if (bankedItem != null) {
			if (bankedItem.getAmount() + item.getAmount() <= 0) {
				item.setAmount(Integer.MAX_VALUE - bankedItem.getAmount());
				player.getPackets().sendGameMessage("Not enough space in your bank.");
			}
		} else if (!hasBankSpace()) {
			player.getPackets().sendGameMessage("Not enough space in your bank.");
			return;
		}
		player.getInventory().deleteItem(invSlot, new Item(originalId, item.getAmount()));
		addItem(item, refresh);
	}

	private void addItem(Item item, boolean refresh) {
		addItem(item.getId(), item.getAmount(), refresh);
	}

	public int addItems(Item[] items, boolean refresh) {
		int space = (int) ((player.isUltimateIronman() ? MAX_ULTIMATE_CAN_HAVE : MAX_BANK_SIZE) - getBankSize());
		if (space != 0) {
			space = (space < items.length ? space : items.length);
			for (int i = 0; i < space; i++) {
				if (items[i] == null) {
					continue;
				}
				addItem(items[i], false);
			}
			if (refresh) {
				refreshTabs();
				refreshItems();
			}
		}
		return space;
	}

	public boolean addItem(int id, int quantity, boolean refresh) {
		return addItem(id, quantity, currentTab, refresh);
	}

	public boolean addItem(int id, int quantity, int creationTab, boolean refresh) {
		int[] slotInfo = getItemSlot(id);

		// so ults can have too many items in bank
		if (player.isUltimateIronman() && !canDepositItem(id, slotInfo)) {
			player.sendMessage("Ultimate Ironman accounts can only have " + MAX_ULTIMATE_CAN_HAVE + " items in their bank.");
			return false;
		}
		// if we cant find an item in the bank with that id, we must add it to the bank
		if (slotInfo == null) {
			if (creationTab >= bankTabs.length) {
				creationTab = bankTabs.length - 1;
			}
			if (creationTab < 0) {
				creationTab = 0;
			}
			int slot = bankTabs[creationTab].length;
			Item[] tab = new Item[slot + 1];
			System.arraycopy(bankTabs[creationTab], 0, tab, 0, slot);
			tab[slot] = new Item(id, quantity);
			bankTabs[creationTab] = tab;
			if (refresh) {
				refreshTab(creationTab);
			}
		} else {
			Item item = bankTabs[slotInfo[0]][slotInfo[1]];
			bankTabs[slotInfo[0]][slotInfo[1]] = new Item(item.getId(), item.getAmount() + quantity);
		}
		if (refresh) {
			refreshItems();
		}
		CoresManager.LOG_PROCESSOR.appendLog(new GameLog("bank", player.getUsername(), "Deposited " + quantity + "x " + ItemDefinitions.getItemDefinitions(id).name + "[" + id + "] from the bank."));
		return true;
	}

	private boolean canDepositItem(int id, int[] slotInfo) {
		int currentSize = getBankSize();

		// we dont have the item in our bank
		if (slotInfo == null) {
			if (currentSize == MAX_ULTIMATE_CAN_HAVE) {
				return false;
			}
		}

		// otherwise - we have the item in our bank
		// and will always be allowed to incremente it,
		return true;
	}

	public boolean removeItem(int fakeSlot, int quantity, boolean refresh, boolean forceDestroy) {
		return removeItem(getRealSlot(fakeSlot), quantity, refresh, forceDestroy);
	}

	public boolean removeItem(int[] slot, int quantity, boolean refresh, boolean forceDestroy) {
		if (slot == null) {
			return false;
		}
		Item item = bankTabs[slot[0]][slot[1]];
		boolean destroyed = false;
		if (quantity >= item.getAmount()) {
			if (bankTabs[slot[0]].length == 1 && (forceDestroy || bankTabs.length != 1)) {
				destroyTab(slot[0]);
				if (refresh) {
					refreshTabs();
				}
				destroyed = true;
			} else {
				Item[] tab = new Item[bankTabs[slot[0]].length - 1];
				System.arraycopy(bankTabs[slot[0]], 0, tab, 0, slot[1]);
				System.arraycopy(bankTabs[slot[0]], slot[1] + 1, tab, slot[1], bankTabs[slot[0]].length - slot[1] - 1);
				bankTabs[slot[0]] = tab;
				if (refresh) {
					refreshTab(slot[0]);
				}
			}
		} else {
			bankTabs[slot[0]][slot[1]] = new Item(item.getId(), item.getAmount() - quantity);
		}
		if (refresh) {
			refreshItems();
		}
		return destroyed;
	}

	/**
	 * This method deletes an item from the bank
	 *
	 * @param itemId
	 * 		The id of the item to delete from the bank
	 * @param itemAmount
	 * 		The amount of the item to delete from the bank
	 * @param refresh
	 * 		If we should refresh the bank interface data
	 * @return We return the item that was deleted from the bank.
	 */
	public Item deleteItem(int itemId, int itemAmount, boolean refresh) {
		int[] slotInfo = getItemSlot(itemId);
		if (slotInfo == null) {
			return null;
		}
		int itemTab = slotInfo[0];
		int itemSlot = slotInfo[1];
		Item item = bankTabs[itemTab][itemSlot];
		if (item == null) {
			return null;
		}
		boolean deleteItem = itemAmount >= item.getAmount();
		if (deleteItem) {
			// we can't destroy the first tab, and if there is only 1 item in our tab
			// we should destroy that tab
			if (itemTab != 0 && bankTabs[itemTab].length == 1) {
				destroyTab(itemTab);
				if (refresh) {
					refreshTabs();
				}
			} else {
				// removing the item from the tab and sorting the tab array
				Item[] tab = new Item[bankTabs[itemTab].length - 1];
				System.arraycopy(bankTabs[itemTab], 0, tab, 0, itemSlot);
				System.arraycopy(bankTabs[itemTab], itemSlot + 1, tab, itemSlot, bankTabs[itemTab].length - itemSlot - 1);
				bankTabs[itemTab] = tab;
				if (refresh) {
					refreshTab(itemTab);
				}
			}
		} else {
			item.setAmount(item.getAmount() - itemAmount);
			return new Item(item.getId(), itemAmount);
		}
		if (refresh) {
			refreshItems();
		}
		return item;
	}

	/**
	 * This method deletes an item from the players bank
	 *
	 * @param itemId
	 * 		The item to delete
	 * @param refresh
	 * 		If we should refresh the bank
	 */
	public boolean deleteItem(int itemId, boolean refresh) {
		int[] slotInfo = getItemSlot(itemId);
		if (slotInfo == null) {
			return false;
		}
		int itemTab = slotInfo[0];
		int itemSlot = slotInfo[1];
		Item item = getItem(getRealSlot(itemSlot));
		if (item == null) {
			return false;
		}
		// we can't destroy the first tab, and if there is only 1 item in our tab
		// we should destroy that tab
		if (itemTab != 0 && bankTabs[itemTab].length == 1) {
			destroyTab(itemTab);
			if (refresh) {
				refreshTabs();
			}
		} else {
			// removing the item from the tab and sorting the tab array
			Item[] tab = new Item[bankTabs[itemTab].length - 1];
			System.arraycopy(bankTabs[itemTab], 0, tab, 0, itemSlot);
			System.arraycopy(bankTabs[itemTab], itemSlot + 1, tab, itemSlot, bankTabs[itemTab].length - itemSlot - 1);
			bankTabs[itemTab] = tab;
			if (refresh) {
				refreshTab(itemTab);
			}
		}
		if (refresh) {
			refreshItems();
		}
		return true;
	}

	public Item getItem(int id) {
		for (Item[] bankTab : bankTabs) {
			for (Item item : bankTab) {
				if (item == null) {
					continue;
				}
				if (item.getId() == id) {
					return item;
				}
			}
		}
		return null;
	}

	public int[] getItemSlot(int id) {
		for (int tab = 0; tab < bankTabs.length; tab++) {
			for (int slot = 0; slot < bankTabs[tab].length; slot++) {
				if (bankTabs[tab][slot].getId() == id) {
					//System.out.println("tab=" + tab + ", slot=" + slot + ", " + bankTabs[tab][slot]);
					return new int[] { tab, slot };
				}
			}
		}
		return null;
	}

	public Item getItem(int[] slot) {
		if (slot == null) {
			return null;
		}
		return bankTabs[slot[0]][slot[1]];
	}

	public int getStartSlot(int tabId) {
		int slotId = 0;
		for (int tab = 1; tab < (tabId == 0 ? bankTabs.length : tabId); tab++) {
			slotId += bankTabs[tab].length;
		}
		return slotId;
	}

	public void refreshItems(int[] slots) {
		player.getPackets().sendUpdateItems(95, getContainerCopy(), slots);
	}

	public void refreshItems() {
		refreshItems(generateContainer(), getContainerCopy());
	}

	public void refreshItems(Item[] itemsAfter, Item[] itemsBefore) {
		if (itemsBefore.length != itemsAfter.length) {
			lastContainerCopy = itemsAfter;
			sendItems();
			return;
		}
		int[] changedSlots = new int[itemsAfter.length];
		int count = 0;
		for (int index = 0; index < itemsAfter.length; index++) {
			if (itemsBefore[index] != itemsAfter[index]) {
				changedSlots[count++] = index;
			}
		}
		int[] finalChangedSlots = new int[count];
		System.arraycopy(changedSlots, 0, finalChangedSlots, 0, count);
		lastContainerCopy = itemsAfter;
		refreshItems(finalChangedSlots);
	}

	public void switchWithdrawNotes() {
		withdrawNotes = !withdrawNotes;
	}

	public void switchInsertItems() {
		insertItems = !insertItems;
		player.getPackets().sendConfig(305, insertItems ? 1 : 0);
	}

	public void setCurrentTab(int currentTab) {
		if (currentTab >= bankTabs.length) {
			return;
		}
		this.currentTab = currentTab;
	}
	
	public int getLastX() {
		return lastX;
	}
	
	public void setLastX(int lastX) {
		this.lastX = lastX;
	}

	/**
	 * Removes the item in the bank by replacing it with 100 coins
	 *
	 * @param id
	 * 		The id of the item to remove
	 */
	public void forceRemove(int id) {
		if (bankTabs != null) {
			for (Item[] bankTab : bankTabs) {
				for (Item element : bankTab) {
					if (element.getId() == id) {
						element.setId(995);
						element.setAmount(100);
					}
				}
			}
		}
	}
}
