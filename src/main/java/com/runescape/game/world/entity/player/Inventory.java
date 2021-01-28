package com.runescape.game.world.entity.player;

import com.runescape.game.world.World;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.item.Item;
import com.runescape.game.world.item.ItemsContainer;
import com.runescape.utility.Utils;
import com.runescape.utility.world.item.ItemWeights;

import java.io.Serializable;
import java.util.List;

public final class Inventory implements Serializable {

	public static final int INVENTORY_INTERFACE = 679;

	private static final long serialVersionUID = 8842800123753277093L;

	private ItemsContainer<Item> items;

	private transient Player player;

	private transient double inventoryWeight;

	public Inventory() {
		items = new ItemsContainer<>(28, false);
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public void unlockInventoryOptions() {
		player.getPackets().sendIComponentSettings(INVENTORY_INTERFACE, 0, 0, 27, 4554126);
		player.getPackets().sendIComponentSettings(INVENTORY_INTERFACE, 0, 28, 55, 2097152);
	}

	public void reset() {
		items.reset();
		init(); // as all slots reseted better just send all again
	}

	public void init() {
		player.getPackets().sendItems(93, items);
	}

	public void refreshConfigs(boolean init) {
		double w = 0;
		for (Item item : items.getItems()) {
			if (item == null) { continue; }
			w += ItemWeights.getWeight(item, false);
		}
		inventoryWeight = w;
		player.getPackets().refreshWeight();
	}

	public boolean addItem(int itemId, int amount) {
		if (itemId < 0 || amount < 0 || !Utils.itemExists(itemId) || !player.getControllerManager().canAddInventoryItem(itemId, amount)) {
			return false;
		}
		Item[] itemsBefore = items.getItemsCopy();
		if (!items.add(new Item(itemId, amount))) {
			items.add(new Item(itemId, items.getFreeSlots()));
			player.getPackets().sendGameMessage("Not enough space in your inventory.");
			refreshItems(itemsBefore);
			return false;
		}
		refreshItems(itemsBefore);
		return true;
	}

	public void refreshItems(Item[] itemsBefore) {
		int[] changedSlots = new int[itemsBefore.length];
		int count = 0;
		for (int index = 0; index < itemsBefore.length; index++) {
			if (itemsBefore[index] != items.getItems()[index]) {
				changedSlots[count++] = index;
			}
		}
		int[] finalChangedSlots = new int[count];
		System.arraycopy(changedSlots, 0, finalChangedSlots, 0, count);
		refresh(finalChangedSlots);
	}

	public void refresh(int... slots) {
		player.getPackets().sendUpdateItems(93, items, slots);
		refreshConfigs(false);
	}

	public boolean addItem(Item item) {
		if (item.getId() < 0 || item.getAmount() < 0 || !Utils.itemExists(item.getId()) || !player.getControllerManager().canAddInventoryItem(item.getId(), item.getAmount())) {
			return false;
		}
		item = new Item(item.getId(), item.getAmount());
		Item[] itemsBefore = items.getItemsCopy();
		if (!items.add(item)) {
			items.add(new Item(item.getId(), items.getFreeSlots()));
			player.getPackets().sendGameMessage("Not enough space in your inventory.");
			refreshItems(itemsBefore);
			return false;
		}
		refreshItems(itemsBefore);
		return true;
	}

	public void deleteItem(int slot, Item item) {
		if (!player.getControllerManager().canDeleteInventoryItem(item.getId(), item.getAmount())) {
			return;
		}
		Item[] itemsBefore = items.getItemsCopy();
		items.remove(slot, item);
		refreshItems(itemsBefore);
	}

	public boolean removeItems(int amount, int... list) {
		for (int itemId : list) {
			if (!contains(itemId)) {
				return false;
			}
			deleteItem(itemId, amount);
		}
		return true;
	}

	public boolean contains(int itemId) {
		return items.contains(new Item(itemId, 1));
	}

	public void deleteItem(int itemId, int amount) {
		if (!player.getControllerManager().canDeleteInventoryItem(itemId, amount)) {
			return;
		}
		Item[] itemsBefore = items.getItemsCopy();
		items.remove(new Item(itemId, amount));
		refreshItems(itemsBefore);
	}

	public boolean removeItems(List<Item> list) {
		for (Item item : list) {
			if (item == null) {
				continue;
			}
			deleteItem(item);
		}
		return true;
	}

	public void deleteItem(Item item) {
		if (!player.getControllerManager().canDeleteInventoryItem(item.getId(), item.getAmount())) {
			return;
		}
		Item[] itemsBefore = items.getItemsCopy();
		items.remove(item);
		refreshItems(itemsBefore);
	}

	public boolean removeItems(Item... list) {
		for (Item item : list) {
			if (item == null) {
				continue;
			}
			deleteItem(item);
		}
		return true;
	}

	/*
	 * No refresh needed its client to who does it :p
	 */
	public void switchItem(int fromSlot, int toSlot) {
		Item[] itemsBefore = items.getItemsCopy();
		Item fromItem = items.get(fromSlot);
		Item toItem = items.get(toSlot);
		items.set(fromSlot, toItem);
		items.set(toSlot, fromItem);
		refreshItems(itemsBefore);
	}

	public ItemsContainer<Item> getItems() {
		return items;
	}

	public boolean hasFreeSlots() {
		return items.getFreeSlot() != -1;
	}

	public int getFreeSlots() {
		return items.getFreeSlots();
	}

	public int getNumerOf(int itemId) {
		return items.getNumberOf(itemId);
	}

	public Item getItem(int slot) {
		return items.get(slot);
	}

	public boolean containsItems(List<Item> list) {
		for (Item item : list) {
			if (!items.contains(item)) {
				return false;
			}
		}
		return true;
	}

	public boolean containsItems(Item[] item) {
		for (int i = 0; i < item.length; i++) {
			if (!items.contains(item[i])) {
				return false;
			}
		}
		return true;
	}

	public boolean containsItems(int[] itemIds, int[] ammounts) {
		int size = itemIds.length > ammounts.length ? ammounts.length : itemIds.length;
		for (int i = 0; i < size; i++) {
			if (!items.contains(new Item(itemIds[i], ammounts[i]))) {
				return false;
			}
		}
		return true;
	}

	public boolean containsItem(int itemId, int ammount) {
		return items.contains(new Item(itemId, ammount));
	}

	public boolean containsOneItem(int... itemIds) {
		for (int itemId : itemIds) {
			if (items.containsOne(new Item(itemId, 1))) {
				return true;
			}
		}
		return false;
	}

	public void sendExamine(int slotId) {
		if (slotId >= getItemsContainerSize()) {
			return;
		}
		Item item = items.get(slotId);
		if (item == null) {
			return;
		}
		player.sendMessage("You look closer at this item and recognize it as: " + Utils.format(item.getAmount()) + "x " + item.getName() + "" + (item.getAmount() > 1 ? item.getName().endsWith("s") ? "" : "s" : "") + ".");
		refreshConfigs(true);
	}

	public int getItemsContainerSize() {
		return items.getSize();
	}

	public void refresh() {
		player.getPackets().sendItems(93, items);
	}

	public boolean addItemDrop(int itemId, int amount) {
		return addItemDrop(itemId, amount, player.getWorldTile());
	}

	public boolean addItemDrop(int itemId, int amount, WorldTile tile) {
		if (itemId < 0 || amount < 0 || !Utils.itemExists(itemId) || !player.getControllerManager().canAddInventoryItem(itemId, amount)) {
			return false;
		}
		Item[] itemsBefore = items.getItemsCopy();
		if (!items.add(new Item(itemId, amount))) {
			Item item = new Item(itemId, amount);
			if (item.getDefinitions().isStackable()) {
				World.addGroundItem(new Item(itemId, amount), tile, player, true, 180);
			} else {
				for (int i = 0; i < amount; i++) {
					World.addGroundItem(new Item(itemId, 1), tile, player, true, 180);
				}
			}
			String name = item.getName();
			String formattedName = name + (item.getAmount() > 1 ? (name.endsWith("s") ? "" : "s") : "");
			player.sendMessage(item.getAmount() + " " + formattedName + " have been dropped to your feet because your inventory was full.");
		} else {
			refreshItems(itemsBefore);
		}
		return true;
	}

	public boolean addItemDrop(Item item) {
		if (item.getId() < 0 || item.getAmount() < 0 || !Utils.itemExists(item.getId()) || !player.getControllerManager().canAddInventoryItem(item.getId(), item.getAmount())) {
			return false;
		}
		Item[] itemsBefore = items.getItemsCopy();
		WorldTile tile = player;
		if (!items.add(item)) {
			if (item.getDefinitions().isStackable()) {
				World.addGroundItem(item, tile, player, true, 180, 3, 150);
			} else {
				for (int i = 0; i < item.getAmount(); i++) {
					World.addGroundItem(new Item(item.getId(), 1), tile, player, true, 180, 3, 150);
				}
			}
			String name = item.getName();
			String formattedName = name + (item.getAmount() > 1 ? (name.endsWith("s") ? "" : "s") : "");
			player.sendMessage(item.getAmount() + " " + formattedName + " have been dropped to your feet because your inventory was full.");
		} else {
			refreshItems(itemsBefore);
		}
		return true;
	}

	public int getCoinsAmount() {
		int coins = items.getNumberOf(995);
		return coins < 0 ? Integer.MAX_VALUE : coins;
	}

	public int getAmountOf(int itemId) {
		return items.getNumberOf(itemId);
	}

	public void replaceItem(int id, int amount, int slot) {
		Item item = items.get(slot);
		if (item == null) {
			return;
		}
		item.setId(id);
		item.setAmount(amount);
		refresh(slot);
	}

	public void forceRemove(int itemId, int amount) {
		items.remove(new Item(itemId, amount));
	}

	public double getInventoryWeight() {
		return inventoryWeight;
	}
}
