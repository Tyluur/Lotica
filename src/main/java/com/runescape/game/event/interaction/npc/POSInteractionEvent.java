package com.runescape.game.event.interaction.npc;

import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.game.event.InputEvent;
import com.runescape.game.event.InputEvent.InputEventType;
import com.runescape.game.event.interaction.button.PlayerShopInteractionEvent;
import com.runescape.game.event.interaction.button.PlayerShopInteractionEvent.ShopDisplayType;
import com.runescape.game.event.interaction.button.Scrollable;
import com.runescape.game.event.interaction.type.NPCInteractionEvent;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.interaction.dialogues.impl.item.SimpleItemMessage;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleMessage;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleNPCMessage;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;
import com.runescape.utility.external.gson.loaders.PlayerShopLoader;
import com.runescape.utility.external.gson.resource.PlayerShopData.PSItem;
import com.runescape.utility.external.gson.resource.PlayerShopData.PlayerShop;
import com.runescape.utility.world.ClickOption;
import com.runescape.workers.game.core.CoresManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 2, 2015
 */
public class POSInteractionEvent extends NPCInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] { 4361 };
	}

	@Override
	public boolean handleNPCInteraction(Player player, NPC npc, ClickOption option) {
		if (player.isAnyIronman()) {
			player.getDialogueManager().startDialogue(SimpleNPCMessage.class, npc.getId(), "Ironman accounts don't have access to player owned shops, sorry!");
			return true;
		}
		switch (option) {
			case FIRST:
				player.getDialogueManager().startDialogue(new SelectOptionD());
				break;
			case SECOND:
				modifyMyStore(player);
				break;
			case THIRD:
				searchForItem(player);
				break;
			case FOURTH:
				openElseStore(player);
				break;
			default:
				break;
		}
		return true;
	}

	/**
	 * Opens the interface to modify the players store if they have a store. Otherwise, they must pay {@value
	 * #SHOP_PRICE} coins to purchase their own store.
	 *
	 * @param player
	 * 		The player modifying their store
	 */
	private void modifyMyStore(Player player) {
		if (!PlayerShopLoader.hasShop(player.getUsername())) {
			player.getDialogueManager().startDialogue(new CreateStoreD());
		} else {
			PlayerShopInteractionEvent.displayShop(player, PlayerShopLoader.getShop(player.getUsername()), ShopDisplayType.MODIFYING);
		}
	}

	/**
	 * Searches for an item through all player shops and displays the results to the player
	 *
	 * @param player
	 * 		The player
	 */
	private void searchForItem(Player player) {
		player.getLockManagement().lockAll(1000);
		player.getPackets().requestClientInput(new InputEvent("Enter the item name you wish to search for", InputEventType.LONG_TEXT) {

			@Override
			public void handleInput() {
				final String itemRequested = getInput();
				CoresManager.submit(() -> {
					synchronized (PlayerShopLoader.LOCK) {
						long start = System.currentTimeMillis();
						List<PlayerShop> shops = PlayerShopLoader.getAllShops();
						// we construct a search result list so we can sort the results afterwards
						List<SearchResult> results = new ArrayList<>();
						for (PlayerShop shop : shops) {
							if (shop == null) {
								continue;
							}
							List<PSItem> stock = shop.getItems();
							results.addAll(stock.stream().filter(item -> item.getName().toLowerCase().contains(itemRequested.toLowerCase())).map(item -> new SearchResult(shop.getOwnerName(), item.getId(), item.getAmount(), item.getPrice())).collect(Collectors.toList()));
						}

						// the list of text that will be sent over the interface
						List<String> text = new ArrayList<>();
						// sorting the results based on price
						results.sort((o1, o2) -> Integer.compare(o1.cost, o2.cost));
						// populating the text list with the results found
						results.forEach(result -> text.add(result.shopOwner + "'s shop sells " + result.amount + " " + ItemDefinitions.getItemDefinitions(result.itemId).getName() + (result.amount == 1 ? "" : "s") + " for " + Utils.numberToCashDigit(result.cost) + "."));

						// showing the player the result
						Scrollable.sendQuestScroll(player, text.size() + " Result" + (results.size() == 1 ? "" : "s"), text.toArray(new String[text.size()]));

						// debug information
						long total = System.currentTimeMillis() - start;
						if (total > 100) {
							System.out.println("Took " + (total) + " ms in " + Thread.currentThread().getName() + ".");
						}
						player.getLockManagement().unlockAll();
					}

				});
			}

		});
	}

	/**
	 * Claims the coins from the collection box
	 *
	 * @param player
	 * 		The player claiming their coins
	 */
	public void claimCoins(Player player) {
		if (!PlayerShopLoader.hasShop(player.getUsername())) {
			player.sendMessage("You have no shop with coins to claim.", true);
		} else {
			PlayerShop shop = PlayerShopLoader.getShop(player.getUsername());
			if (shop == null) {
				throw new IllegalStateException();
			}
			int coins = (int) shop.getCoinsForCollection();
			if (coins != shop.getCoinsForCollection()) {
				throw new IllegalStateException("Possible overflow of coins in " + shop.getOwnerName() + "'s shop! [" + shop.getCoinsForCollection() + "]");
			}
			if (coins <= 0) {
				player.getDialogueManager().startDialogue(SimpleMessage.class, "You have no coins to claim.");
				return;
			}
			shop.setCoinsForCollection(0);
			player.getInventory().addItem(995, coins);
			player.getDialogueManager().startDialogue(SimpleItemMessage.class, 995, "You receive " + Utils.format(coins) + " coins from your player-owned-shop collection box.");
		}
	}

	/**
	 * Requests the user's input for the store they wish to see. If a valid store by that name exists, we display it for
	 * them
	 *
	 * @param player
	 * 		The player to open the store for
	 */
	private void openElseStore(Player player) {
		player.getPackets().requestClientInput(new InputEvent("Enter Shopkeeper's name", InputEventType.NAME) {

			@Override
			public void handleInput() {
				String input = getInput();
				if (!PlayerShopLoader.hasShop(input)) {
					player.getDialogueManager().startDialogue(SimpleMessage.class, "No shop exists for shopkeeper " + input + "... Try again!");
					return;
				}
				PlayerShopInteractionEvent.displayShop(player, PlayerShopLoader.getShop(input), ShopDisplayType.BUYING);
			}
		});
	}

	/**
	 * The price to make your own shop
	 */
	private static final int SHOP_PRICE = 1_000_000;

	/**
	 * @author Tyluur
	 */
	private final class CreateStoreD extends Dialogue {

		@Override
		public void start() {
			sendDialogue("You do not have a shop of your own yet. You must pay " + Utils.numberToCashDigit(SHOP_PRICE) + " coins to purchase your own.", "Do you wish to proceed?");
		}

		@Override
		public void run(int interfaceId, int option) {
			switch (stage) {
				case -1:
					sendOptionsDialogue("Pay " + Utils.numberToCashDigit(SHOP_PRICE) + " for a shop?", "Yes, proceed.", "Cancel.");
					stage = 0;
					break;
				case 0:
					switch (option) {
						case FIRST:
							if (player.takeMoney(SHOP_PRICE)) {
								end();
								PlayerShopLoader.createShop(player);
								modifyMyStore(player);
							} else {
								sendPlayerDialogue(MIDLY_ANGRY, "I don't have that much money!");
							}
							break;
						case SECOND:
							end();
							break;
					}
					break;
			}
		}

		@Override
		public void finish() {
		}
	}

	/**
	 * @author Tyluur
	 */
	private final class SelectOptionD extends Dialogue {

		@Override
		public void start() {
			sendOptionsDialogue(DEFAULT_OPTIONS, "Claim Coins", "Modify my Store", "Search for Item", "Open Someone's Store");
		}

		@Override
		public void run(int interfaceId, int option) {
			switch (stage) {
				case -1:
					end();
					switch (option) {
						case FIRST:
							claimCoins(player);
							break;
						case SECOND:
							modifyMyStore(player);
							break;
						case THIRD:
							searchForItem(player);
							break;
						case FOURTH:
							openElseStore(player);
							break;
					}
					break;
			}
		}

		@Override
		public void finish() {
		}
	}

	private static class SearchResult {

		private final int itemId, amount, cost;

		private final String shopOwner;

		public SearchResult(String shopOwner, int itemId, int amount, int cost) {
			this.itemId = itemId;
			this.amount = amount;
			this.cost = cost;
			this.shopOwner = shopOwner;
		}

	}


}
