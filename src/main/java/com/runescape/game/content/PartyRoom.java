package com.runescape.game.content;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.game.world.item.ItemsContainer;
import com.runescape.utility.world.item.ItemSetsKeyGenerator;

import java.util.ArrayList;
import java.util.List;

public class PartyRoom {

	/**
	 * The id of the party chest interface
	 */
	public static final int PARTY_CHEST_INTERFACE = 647;

	/**
	 * The id of the inventory interface
	 */
	public static final int INVENTORY_INTERFACE = 336;

	/**
	 * The list of players that are viewing the chest
	 */
	private static final List<Player> viewers = new ArrayList<>();

	/**
	 * The list of items that will be sent to the {@link #dropList} after being accepted.
	 */
	private static final ItemsContainer<Item> items = new ItemsContainer<>(100, false);

	/**
	 * The list of items that are going to be dropped
	 */
	private static final ItemsContainer<Item> dropList = new ItemsContainer<>(100, false);

	/**
	 * The chest items key
	 */
	private static final int CHEST_INTERFACE_ITEMS_KEY = ItemSetsKeyGenerator.generateKey();

	/**
	 * If we have a party room event currently running
	 */
	private static boolean eventRunning = false;

	/**
	 * Handles the opening of the party chest
	 *
	 * @param player
	 * 		The player opening the chest
	 */
	public static void openPartyChest(final Player player) {
		// Adding the player to the list of viewing players
		viewers.add(player);
		player.putAttribute("viewing_party_room", true);
		player.getInterfaceManager().sendInterface(PARTY_CHEST_INTERFACE);
		player.getInterfaceManager().sendInventoryInterface(INVENTORY_INTERFACE);
		// When the player closes the interface, they are removed from the list of viewers
		player.setCloseInterfacesEvent(() -> viewers.remove(player));
		sendOptions(player);
		refreshItems(player);
	}

	/**
	 * Sends the options for interacting with the party room interface
	 *
	 * @param player
	 * 		The player to send the options to
	 */
	private static void sendOptions(final Player player) {
		player.getPackets().sendInterSetItemsOptionsScript(INVENTORY_INTERFACE, 0, 93, 4, 7, "Deposit", "Deposit-5", "Deposit-10", "Deposit-All", "Deposit-X");
		player.getPackets().sendInterSetItemsOptionsScript(INVENTORY_INTERFACE, 30, CHEST_INTERFACE_ITEMS_KEY, 4, 7, "Value");
		player.getPackets().sendInterSetItemsOptionsScript(PARTY_CHEST_INTERFACE, 33, CHEST_INTERFACE_ITEMS_KEY, true, 4, 7, "Examine");
		player.getPackets().sendIComponentSettings(INVENTORY_INTERFACE, 0, 0, 27, 1278);
		player.getPackets().sendIComponentSettings(PARTY_CHEST_INTERFACE, 30, 0, 27, 1150);
		player.getPackets().sendIComponentSettings(PARTY_CHEST_INTERFACE, 33, 0, 27, 1026);
	}

	/**
	 * Gets the total price of the items in the chest
	 *
	 * @return The total price
	 */
	public static int getTotalCoins() {
		int price = 0;
		for (Item item : items.getItems()) {
			if (item == null)
				continue;
			price += item.getDefinitions().getExchangePrice();
		}
		return price;
	}

	/**
	 * Handles the purchasing of an event in the party room
	 *
	 * @param player
	 * 		The player
	 * @param balloons
	 * 		If we purchased the balloons to be dropped
	 */
	public static void purchase(final Player player, boolean balloons) {
		if (balloons) {
			if (player.getInventory().containsItem(995, 1000)) {
				dropBalloons(player);
			} else {
				player.getDialogueManager().startDialogue("SimpleMessage", "Balloon Bonanza costs 1000 coins.");
			}
		} else {
			if (player.getInventory().containsItem(995, 500)) {
				startDancingKnights();
			} else {
				player.getDialogueManager().startDialogue("SimpleMessage", "Nightly Dance costs 500 coins.");
			}
		}
	}

	/**
	 * This method handles the addition of an item to the {@link #items} {@code ItemsContainer}
	 *
	 * @param item
	 * 		The item to add
	 * @return {@code True} if the item was added successfully
	 */
	public static boolean addItem(Item item) {
		boolean added = items.add(item);
		if (added) {
			viewers.forEach(com.runescape.game.content.PartyRoom::refreshItems);
		}
		return added;
	}

	/**
	 * This method refreshes the items over the interface for the players
	 *
	 * @param viewer
	 * 		The player viewing
	 */
	private static void refreshItems(Player viewer) {
		viewer.getPackets().sendItems(529, items);
		viewer.getPackets().sendItems(91, dropList);
		System.out.println("refreshed items to" + viewer);
	}

	private static void dropBalloons(Player player) {

	}

	private static void startDancingKnights() {

	}

}
