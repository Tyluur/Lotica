package com.runescape.utility.external.gson.resource;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.utility.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 2, 2015
 */
public class PlayerShopData {

	/**
	 * The maximum amount of items that can be in the shop
	 */
	private static final int MAX_ITEM_SIZE = 30;

	public static class PSItem extends Item {

		private static final long serialVersionUID = 4359744114070696318L;

		public PSItem(int id, int amount, int price) {
			super(id, amount);
			this.price = price;
		}

		/**
		 * @return the price
		 */
		public int getPrice() {
			return price;
		}

		/**
		 * The price of the item
		 */
		private final int price;

		@Override
		public String toString() {
			return "id=" + getId() + ", amount=" + amount + ", price=" + price + ", name=" + getDefinitions().getName();
		}
	}

	public static class PlayerShop {

		public PlayerShop(String ownerName) {
			this.ownerName = ownerName;
			this.items = new ArrayList<>();
		}

		/**
		 * Adds a {@code PSItem} to the {@link #items} list
		 *
		 * @param item
		 * 		The item to adda
		 */
		public boolean addItem(PSItem item) {
			for (PSItem shopItem : items) {
				// found matching item
				if (shopItem.getId() == item.getId() && shopItem.getPrice() == item.getPrice()) {
					long newTotal = (long) shopItem.getAmount() + (long) item.getAmount();
					if (newTotal > Integer.MAX_VALUE) {
						return false;
					}
					shopItem.setAmount((int) newTotal);
					return true;
				}
			}
			if (getItems().size() < MAX_ITEM_SIZE) {
				getItems().add(item);
				return true;
			}
			return false;
		}

		/**
		 * @return the ownerName
		 */
		public String getOwnerName() {
			return ownerName;
		}

		/**
		 * Formats the owner's name and adds a trailing 's or ' appropriately
		 */
		public String getFormattedName() {
			return Utils.formatPlayerNameForDisplay(ownerName) + (ownerName.endsWith("s") ? "'" : "'s");
		}

		/**
		 * @return the viewingPlayers
		 */
		public List<Player> getViewingPlayers() {
			if (viewingPlayers == null) {
				viewingPlayers = new ArrayList<>();
			}
			return viewingPlayers;
		}

		/**
		 * @return the items
		 */
		public List<PSItem> getItems() {
			return items;
		}

		/**
		 * @return the beingEditted
		 */
		public boolean isBeingEditted() {
			return beingEditted;
		}

		/**
		 * @param beingEditted
		 * 		the beingEditted to set
		 */
		public void setBeingEditted(boolean beingEditted) {
			this.beingEditted = beingEditted;
		}

		/**
		 * @return the coinsForCollection
		 */
		public long getCoinsForCollection() {
			return coinsForCollection;
		}

		/**
		 * @param coinsForCollection
		 * 		the coinsForCollection to set
		 */
		public void setCoinsForCollection(long coinsForCollection) {
			this.coinsForCollection = coinsForCollection;
		}

		/**
		 * Adds coins to the {@link #coinsForCollection} if possible. The possibility check is to ensure that we don't
		 * overflow the {@link Integer#MAX_VALUE}
		 *
		 * @param update
		 * 		If we should update the value
		 * @param amount
		 * 		The amount of coins to add
		 */
		public boolean addCoinsForCollection(boolean update, int amount) {
			long newAmount = coinsForCollection + (long) amount;
			if (newAmount > Integer.MAX_VALUE) {
				return false;
			}
			if (update) {
				coinsForCollection = newAmount;
			}
			return true;
		}

		/**
		 * Checking if the item is in the shop
		 *
		 * @param itemId
		 * 		The id of the item
		 * @param amount
		 * 		The amount of the item
		 */
		public boolean hasItem(int itemId, int amount) {
			for (PSItem item : items) {
				if (item == null) {
					continue;
				}
				if (item.getId() == itemId) {
					if (item.getAmount() >= amount) {
						return true;
					}
				}
			}
			return false;
		}

		/**
		 * The name of the owner of this shop
		 */
		private final String ownerName;

		/**
		 * The items in the shop
		 */
		private final List<PSItem> items;

		/**
		 * The amount of coins the player has available for collection
		 */
		private long coinsForCollection;

		/**
		 * If the store is being editted
		 */
		private transient boolean beingEditted = false;

		/**
		 * The list of players viewing the shop
		 */
		private transient List<Player> viewingPlayers = new ArrayList<>(MAX_ITEM_SIZE);
	}

}
