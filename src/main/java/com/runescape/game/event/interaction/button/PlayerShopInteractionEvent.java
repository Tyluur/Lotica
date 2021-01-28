package com.runescape.game.event.interaction.button;

import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.game.event.InputEvent;
import com.runescape.game.event.InputEvent.InputEventType;
import com.runescape.game.event.interaction.type.InterfaceInteractionEvent;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleMessage;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.game.world.item.ItemConstants;
import com.runescape.utility.ChatColors;
import com.runescape.utility.Utils;
import com.runescape.utility.external.gson.loaders.ItemInformationLoader;
import com.runescape.utility.external.gson.loaders.PlayerShopLoader;
import com.runescape.utility.external.gson.resource.PlayerShopData.PSItem;
import com.runescape.utility.external.gson.resource.PlayerShopData.PlayerShop;
import com.runescape.workers.game.core.CoresManager;
import com.runescape.workers.game.log.GameLog;

import java.util.Iterator;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 2, 2015
 */
public class PlayerShopInteractionEvent extends InterfaceInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] { INTERFACE_ID, INVENTORY_ID };
	}

	@Override
	public boolean handleInterfaceInteraction(Player player, int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
		if (player.getAttribute("player_shop_display_type", null) == null || player.getAttribute("player_shop_viewing", null) == null) {
			return false;
		}
		ShopDisplayType displayType = player.getAttribute("player_shop_display_type");
		PlayerShop shop = player.getAttribute("player_shop_viewing");
		if (displayType == ShopDisplayType.BUYING && shop.isBeingEditted()) {
			return true;
		}
		switch (interfaceId) {
			case INVENTORY_ID:
				switch (displayType) {
					case MODIFYING:
						if (componentId == 0) {
							switch (packetId) {
								case 14:
									addItemToShop(player, shop, slotId2, 1);
									break;
								case 67:
									addItemToShop(player, shop, slotId2, 5);
									break;
								case 5:
									addItemToShop(player, shop, slotId2, 10);
									break;
								case 55:
									addItemToShop(player, shop, slotId2, Integer.MAX_VALUE);
									break;
								case 68:
									player.getPackets().requestClientInput(new InputEvent("Enter Amount", InputEventType.INTEGER) {

										@Override
										public void handleInput() {
											addItemToShop(player, shop, slotId2, getInput());
										}
									});
									break;
								case 27:
									player.sendMessage(ItemInformationLoader.getExamine(slotId2));
									break;
							}
						}
						break;
					default:
						break;
				}
				break;
			case INTERFACE_ID:
				PSItem item = shop.getItems().get(slotId);
				if (item == null) {
					throw new IllegalStateException();
				}
				switch (displayType) {
					case BUYING:
						switch (packetId) {
							case 14:
								player.sendMessage(item.getName() + " currently costs " + Utils.numberToCashDigit(item.getPrice()) + " coins in " + shop.getFormattedName() + " shop. (" + Utils.format(item.getPrice()) + ")", true);
								break;
							case 67:
								buyFromShop(player, shop, item, 1);
								break;
							case 5:
								buyFromShop(player, shop, item, 5);
								break;
							case 55:
								buyFromShop(player, shop, item, 10);
								break;
							case 68:
								buyFromShop(player, shop, item, Integer.MAX_VALUE);
								break;
							case 27:
								player.getPackets().requestClientInput(new InputEvent("Enter Amount", InputEventType.INTEGER) {

									@Override
									public void handleInput() {
										buyFromShop(player, shop, item, getInput());
									}

								});
								break;
							default:
								System.err.println("case " + packetId + ":");
								System.err.println("break;");
								break;
						}
						break;
					case MODIFYING:
						switch (packetId) {
							case 14:
								player.sendMessage(item.getName() + " currently costs " + Utils.numberToCashDigit(item.getPrice()) + " coins in my shop. (" + Utils.format(item.getPrice()) + ")", true);
								break;
							case 67:
								removeItemFromShop(player, shop, item, 5);
								break;
							case 5:
								removeItemFromShop(player, shop, item, 10);
								break;
							case 55:
								removeItemFromShop(player, shop, item, Integer.MAX_VALUE);
								break;
							case 68:
								player.getPackets().requestClientInput(new InputEvent("Enter Amount", InputEventType.INTEGER) {
									@Override
									public void handleInput() {
										removeItemFromShop(player, shop, item, getInput());
									}
								});
								break;
							case 27:
								player.sendMessage(ItemInformationLoader.getExamine(item.getId()));
								break;
							default:
								break;
						}
						System.out.println(componentId + "," + packetId + "," + slotId + "," + slotId2 + "," + item);
						break;
				}
				break;
		}
		return true;
	}

	/**
	 * Handles the buying of an item from the shop
	 *
	 * @param player
	 * 		The player buying
	 * @param shop
	 * 		The shop being bought from
	 * @param item
	 * 		The item being bought
	 * @param amount
	 * 		The amount of the item we're buying
	 */
	private static void buyFromShop(Player player, PlayerShop shop, PSItem item, int amount) {
		if (amount <= 0) {
			return;
		}
		if (amount > item.getAmount()) {
			amount = item.getAmount();
		}
		long totalPrice = (long) amount * (long) item.getPrice();
		if (totalPrice > Integer.MAX_VALUE) {
			player.sendMessage("You can't buy an item for that much! Please select a lower quantity.");
			return;
		}
		if (player.getAttribute("player_shop_viewing") == null) {
			System.out.println("Attempted to buy item when we didn't have shop open.");
			return;
		}
		int price = (int) totalPrice;
		int coins = player.getContainerCoins();
		boolean updated;

		if (coins >= price) {
			if (!shop.addCoinsForCollection(false, price)) {
				player.getDialogueManager().startDialogue(SimpleMessage.class, Utils.formatPlayerNameForDisplay(shop.getOwnerName()) + " must claim the coins in their collection box before they can sell any more items from their shop.");
				return;
			}
			if (!shop.hasItem(item.getId(), amount)) {
				return;
			}
			if (player.takeMoney(price)) {
				if (item.getAmount() - amount <= 0) {
					shop.getItems().remove(item);
				} else {
					item.setAmount(item.getAmount() - amount);
				}
				player.getInventory().addItemDrop(item.getId(), amount);
				shop.addCoinsForCollection(true, price);
				updated = true;
				
				Player owner = World.getPlayer(shop.getOwnerName());
				if (owner != null) {
					owner.sendMessage("<col=" + ChatColors.BLUE + ">" + amount + " " + item.getName() + (amount == 1 ? "" : "s") + " " + (amount == 1 ? "was" : "were") + " bought from your store for " + Utils.numberToCashDigit(price) + " gp total.", false);
					owner.sendMessage("Claim the coins from this purchase by talking to the Shop Manager at home.", true);
				}
			} else {
				player.sendMessage("You don't have enough coins!", true);
				return;
			}
		} else {
			int amountAllowed = (int) Math.floor(coins / item.getPrice());
			if (amountAllowed <= 0) {
				player.sendMessage("You don't have enough coins!", true);
				return;
			}
			price = amountAllowed * item.getPrice();
			if (player.takeMoney(price)) {
				if (item.getAmount() - amountAllowed <= 0) {
					shop.getItems().remove(item);
				} else {
					item.setAmount(item.getAmount() - amountAllowed);
				}
				player.getInventory().addItem(item.getId(), amountAllowed);
				shop.addCoinsForCollection(true, price);
				updated = true;
			} else {
				player.sendMessage("You don't have enough coins!", true);
				return;
			}
		}
		if (updated) {
			for (Player p : shop.getViewingPlayers()) {
				sendShopItems(p, shop);
			}
		}
		CoresManager.LOG_PROCESSOR.appendLog(new GameLog("player_owned_shop", player.getUsername(), "Bought an item from " + shop.getOwnerName() + "'s shop:\t" + item));
	}

	/**
	 * Removes an item from the shop
	 *
	 * @param player
	 * 		The player
	 * @param shop
	 * 		The shop to remove it from
	 * @param item
	 * 		The item to remove
	 * @param amount
	 * 		The amount of the item to remove
	 */
	private static void removeItemFromShop(Player player, PlayerShop shop, PSItem item, int amount) {
		if (!shop.getItems().contains(item)) {
			System.out.println("Attempted to remove a item that wasnt in the shop.");
			return;
		}
		if (amount <= 0) {
			return;
		}
		if (player.getAttribute("player_shop_viewing") == null) {
			System.out.println("Attempted to remove item when we didnt have interface open.");
			return;
		}
		if (amount > item.getAmount()) {
			amount = item.getAmount();
		}
		if (item.getAmount() - amount <= 0) {
			shop.getItems().remove(item);
		} else {
			item.setAmount(item.getAmount() - amount);
		}
		player.getInventory().addItemDrop(item.getId(), amount);
		sendShopItems(player, shop);
	}

	/**
	 * Adds an item to the shop
	 *
	 * @param player
	 * 		The player
	 * @param shop
	 * 		The shop to add the item to
	 * @param itemId
	 * 		The id of the item
	 * @param amount
	 * 		The amount of the item to add
	 */
	private static void addItemToShop(Player player, PlayerShop shop, int itemId, int amount) {
		ItemDefinitions definitions = ItemDefinitions.forId(itemId);
		if (definitions == null) {
			throw new IllegalStateException();
		}
		if (!ItemConstants.isTradeable(new Item(itemId)) || itemId == 995) {
			player.sendMessage("This item can't be sold.");
			return;
		}
		if (amount > player.getInventory().getNumerOf(itemId)) {
			amount = player.getInventory().getNumerOf(itemId);
		}
		if (amount <= 0) {
			return;
		}
		final int finalAmount = amount;
		player.getPackets().requestClientInput(new InputEvent("Enter Price", InputEventType.INTEGER) {

			@Override
			public void handleInput() {
				if (!player.getInterfaceManager().containsInterface(INTERFACE_ID)) {
					System.out.println(player.getUsername() + " attempted to dupe.");
					return;
				}
				if (!player.getInventory().contains(itemId)) {
					return;
				}
				PSItem item;
				Integer price = getInput();
				if (price <= 0) {
					return;
				}
				if (shop.addItem(item = new PSItem(itemId, finalAmount, price))) {
					CoresManager.LOG_PROCESSOR.appendLog(new GameLog("player_owned_shop", player.getUsername(), "Added an item to their player owned store:\t" + item));
					player.getInventory().deleteItem(itemId, finalAmount);
					sendShopItems(player, shop);
				} else {
					player.getDialogueManager().startDialogue(SimpleMessage.class, "We couldn't store this item in your store.", "Make sure you aren't going over the max limit for items in your store (30).", "You can't have over 2147M of any item either.");
				}
			}
		});
	}

	/**
	 * Displays the shop
	 *
	 * @param player
	 * 		The player to display the shop to
	 * @param shop
	 * 		The shop to dispaly
	 * @param type
	 * 		The type of display we are doing.
	 */
	public static void displayShop(Player player, PlayerShop shop, ShopDisplayType type) {
		if (shop == null) {
			player.sendMessage("Could not find that shop...");
			return;
		}
		player.stopAll();
		switch (type) {
			case BUYING:
				if (shop.isBeingEditted()) {
					player.getDialogueManager().startDialogue(SimpleMessage.class, "This store is being renovated...", "Please come back shortly.");
					return;
				}
				player.getPackets().sendIComponentText(INTERFACE_ID, 14, shop.getFormattedName() + " Store");
				shop.getViewingPlayers().add(player);
				break;
			case MODIFYING:
				player.getPackets().sendIComponentText(INTERFACE_ID, 14, "My Store");
				shop.setBeingEditted(true);
				for (Iterator<Player> it$ = shop.getViewingPlayers().iterator(); it$.hasNext(); ) {
					Player p = it$.next();

					it$.remove();
					p.stopAll();
					p.getDialogueManager().startDialogue(SimpleMessage.class, "This store is being renovated... Please come back shortly.");
				}
				break;
			default:
				break;
		}

		sendScreenInterfaceOptions(player, type);
		sendInventoryOptions(player, type);
		sendShopItems(player, shop);
		player.getInterfaceManager().sendInterface(INTERFACE_ID);
		if (type == ShopDisplayType.MODIFYING) {
			player.getInterfaceManager().sendInventoryInterface(INVENTORY_ID);
		}

		player.putAttribute("player_shop_display_type", type);
		player.putAttribute("player_shop_viewing", shop);
		player.setCloseInterfacesEvent(() -> {
			if (type == ShopDisplayType.BUYING) {
				for (Iterator<Player> it$ = shop.getViewingPlayers().iterator(); it$.hasNext(); ) {
					Player p = it$.next();
					if (p.equals(player)) {
						it$.remove();
					}
				}
			} else {
				shop.setBeingEditted(false);
			}
			if (shop.getViewingPlayers().size() == 0) {
				PlayerShopLoader.save(shop);
				System.out.println("Saved Player Shop for " + shop.getOwnerName());
			}
			player.removeAttribute("player_shop_open");
			player.removeAttribute("player_shop_viewing");
		});
	}

	/**
	 * Sends the items in the shop over the interface
	 *
	 * @param player
	 * 		The player
	 * @param shop
	 * 		The shop
	 */
	private static void sendShopItems(Player player, PlayerShop shop) {
		Item[] items = new Item[shop.getItems().size()];
		for (int i = 0; i < shop.getItems().size(); i++) {
			items[i] = shop.getItems().get(i);
		}
		player.getPackets().sendItems(ITEMS_KEY, items);
		player.getPackets().sendItems(93, player.getInventory().getItems());
	}

	/**
	 * Sends the inventory options for items in the inventory
	 *
	 * @param player
	 * 		The player
	 * @param type
	 * 		The type of display we are sending
	 */
	private static void sendInventoryOptions(Player player, ShopDisplayType type) {
		switch (type) {
			case MODIFYING:
				player.getPackets().sendUnlockIComponentOptionSlots(INVENTORY_ID, 0, 0, 27, 0, 1, 2, 3, 4, 5);
				player.getPackets().sendInterSetItemsOptionsScript(INVENTORY_ID, 0, 93, 4, 7, "Sell", "Sell-5", "Sell-10", "Sell-All", "Sell-X", "Examine");
				break;
			default:
				break;
		}
	}

	/**
	 * Sends the interface options for the screen
	 *
	 * @param player
	 * 		The player
	 * @param type
	 * 		The type of display we are sending
	 */
	private static void sendScreenInterfaceOptions(Player player, ShopDisplayType type) {
		switch (type) {
			case BUYING:
				player.getPackets().sendUnlockIComponentOptionSlots(INTERFACE_ID, 27, 0, ITEMS_KEY, 0, 1, 2, 3, 4, 5);
				player.getPackets().sendInterSetItemsOptionsScript(INTERFACE_ID, 27, ITEMS_KEY, 6, 5, "Value", "Buy-1", "Buy-5", "Buy-10", "Buy-All", "Buy-X");
				break;
			case MODIFYING:
				player.getPackets().sendUnlockIComponentOptionSlots(INTERFACE_ID, 27, 0, ITEMS_KEY, 0, 1, 2, 3, 4, 5);
				player.getPackets().sendInterSetItemsOptionsScript(INTERFACE_ID, 27, ITEMS_KEY, 6, 5, "Value", "Remove-5", "Remove-10", "Remove-All", "Remove-X", "Examine");
				break;
		}
	}

	/**
	 * The interface ids of the shop and inventory
	 */
	private static final int INTERFACE_ID = 671, INVENTORY_ID = 665;

	/**
	 * The item key for the interface
	 */
	private static final int ITEMS_KEY = 530;

	public enum ShopDisplayType {
		MODIFYING,
		BUYING
	}

}
