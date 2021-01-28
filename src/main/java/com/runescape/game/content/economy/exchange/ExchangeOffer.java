package com.runescape.game.content.economy.exchange;

import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.game.world.item.ItemsContainer;
import com.runescape.workers.game.core.CoresManager;

/**
 * @author Lazarus <lazarus.rs.king@gmail.com>
 * @since Jun 22, 2014
 */
public class ExchangeOffer {

	/**
	 * The name of the owner of this offer
	 */
	private final String owner;

	/**
	 * The id of the item
	 */
	private final int itemId;

	/**
	 * The type of exchange offer this is
	 *
	 * @see {@link ExchangeType}
	 */
	private final ExchangeType type;

	/**
	 * The exchange slot this offer will display on for the owner
	 */
	private final int slot;

	/**
	 * If the offer is an unlimited one
	 */
	private final boolean unlimited;
	
	/**
	 * The amount of the offer that was initially requested
	 */
	private int amountRequested;

	/**
	 * The amount of the offer that has been processed
	 */
	private int amountProcessed;

	/**
	 * The amount of the offer that the owner has selected out of the processed amount
	 */
	private int amountReceived;

	/**
	 * The price of the offer that was entered;
	 */
	private int price;

	/**
	 * The surplus of money
	 */
	private int surplus;

	/**
	 * If the offer has been aborted
	 */
	private boolean aborted;

	/**
	 * If the offer is being processed
	 */
	private boolean processing;

	@Override
	public boolean equals(Object o) {
		if (o instanceof ExchangeOffer) {
			ExchangeOffer other = (ExchangeOffer) o;
			return other.owner.equals(owner) && other.getItemId() == getItemId() && other.unlimited == unlimited && other.getSlot() == getSlot() && other.type == type;
		}
		return false;
	}

	@Override
	public String toString() {
		return "[owner=" + owner + ", id=" + itemId + ", type=" + type + ", price=" + price + ", amountRequested=" + getAmountRequested() + ", amountProcessed=" + amountProcessed + ", amountReceived=" + amountReceived + ", slot=" + slot + "]";
	}

	/**
	 * @return the amountRequested
	 */
	public int getAmountRequested() {
		return amountRequested;
	}

	/**
	 * @param amountRequested
	 * 		the amountRequested to set
	 */
	public void setAmountRequested(int amountRequested) {
		long price2 = (long) amountRequested * price;
		if (price2 > Integer.MAX_VALUE || price2 == Integer.MAX_VALUE || price2 >= Integer.MAX_VALUE || price2 <= 0 || price <= 0) {
			System.out.println("tooo high~");
			return;
		}
		this.amountRequested = amountRequested;
	}

	/**
	 * @return the itemId
	 */
	public int getItemId() {
		return itemId;
	}

	/**
	 * @return the slot
	 */
	public int getSlot() {
		return slot;
	}

	/**
	 * Constructs a new exchange offer object
	 *
	 * @param owner
	 * 		The owner of the offer
	 * @param itemId
	 * 		The id of the offer
	 * @param type
	 * 		The type of offer
	 * @param slot
	 * 		The slot of the offer
	 * @param amount
	 * 		The amount that is being sold/bought
	 * @param price
	 * 		The price of the offer
	 */
	public ExchangeOffer(String owner, int itemId, ExchangeType type, int slot, int amount, int price) {
		this.owner = owner;
		this.itemId = itemId;
		this.type = type;
		this.slot = slot;
		this.price = price;
		this.amountRequested = amount;
		this.setAmountProcessed(0);
		this.setAmountReceived(0);
		this.unlimited = false;
	}

	/**
	 * Constructs a new exchange offer object
	 *
	 * @param owner
	 * 		The owner of the offer
	 * @param itemId
	 * 		The id of the offer
	 * @param type
	 * 		The type of offer
	 * @param slot
	 * 		The slot of the offer
	 * @param amount
	 * 		The amount that is being sold/bought
	 * @param price
	 * 		The price of the offer
	 * @param unlimited
	 * 		If the offer is unlimited
	 */
	public ExchangeOffer(String owner, int itemId, ExchangeType type, int slot, int amount, int price, boolean unlimited) {
		this.owner = owner;
		this.itemId = itemId;
		this.type = type;
		this.slot = slot;
		this.price = price;
		this.amountRequested = amount;
		this.setAmountProcessed(0);
		this.setAmountReceived(0);
		this.unlimited = unlimited;
	}

	/**
	 * @return the owner
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * @return the type
	 */
	public ExchangeType getType() {
		return type;
	}

	/**
	 * @return the amountProcessed
	 */
	public int getAmountProcessed() {
		return amountProcessed;
	}

	/**
	 * @param amountProcessed
	 * 		the amountProcessed to set
	 */
	public void setAmountProcessed(int amountProcessed) {
		this.amountProcessed = amountProcessed;
	}

	/**
	 * @return the amountReceived
	 */
	public int getAmountReceived() {
		return amountReceived;
	}

	/**
	 * @param amountReceived
	 * 		the amountReceived to set
	 */
	public void setAmountReceived(int amountReceived) {
		this.amountReceived = amountReceived;
	}

	/**
	 * @return the price
	 */
	public int getPrice() {
		return price;
	}

	/**
	 * @param price
	 * 		the price to set
	 */
	public void setPrice(int price) {
		long price2 = (long) amountRequested * price;
		if (price2 > Integer.MAX_VALUE || price2 == Integer.MAX_VALUE || price2 >= Integer.MAX_VALUE || price2 <= 0 || price <= 0) {
			return;
		}
		this.price = price;
	}

	/**
	 * @return the unlimited
	 */
	public boolean isUnlimited() {
		return unlimited;
	}

	/**
	 * If the offer has finished, based on whether the {@link #amountProcessed} is at the {@link #amountRequested}
	 * value
	 */
	public boolean isFinished() {
		return amountProcessed >= amountRequested;
	}

	/**
	 * Gets the container of items to collect
	 *
	 * @return The items
	 */
	public ItemsContainer<Item> getItemsToCollect() {
		ItemsContainer<Item> items = new ItemsContainer<>(2, true);
		final int amount = amountRequested - amountProcessed;
		if (isAborted()) {
			switch (type) {
				case BUY:
					items.add(new Item(995, price * amount));
					break;
				case SELL:
					items.add(new Item(itemId, amount));
					break;
			}
		} else {
			if (amountReceived > 0) {
				switch (type) {
					case BUY:
						items.add(new Item(itemId, amountReceived));
						break;
					case SELL:
						items.add(new Item(995, price * amountReceived));
						break;
				}
			}
		}
		if (surplus > 0) {
			switch (type) {
				case BUY:
					items.set(1, new Item(995, surplus));
					break;
				case SELL:
					items.add(new Item(995, surplus));
					break;
			}
		}
		return items;
	}

	/**
	 * @return the aborted
	 */
	public boolean isAborted() {
		return aborted;
	}

	/**
	 * @param aborted
	 * 		the aborted to set
	 */
	public void setAborted(boolean aborted) {
		this.aborted = aborted;
	}

	/**
	 * @return the surplus
	 */
	public int getSurplus() {
		return surplus;
	}

	/**
	 * @param surplus
	 * 		the surplus to set
	 */
	public void setSurplus(int surplus) {
		this.surplus = surplus;
	}

	/**
	 * Notifys the owner of an updated offer if they are online
	 */
	public void notifyUpdated() {
		CoresManager.submit(() -> {
			Player player = World.getPlayerByDisplayName(owner);
			if (player != null) {
				boolean hasOpen = false;
				if (player.getInterfaceManager().containsInterface(105) || player.getInterfaceManager().containsTab(105)) {
					ExchangeManagement.sendProgress(player);
					hasOpen = true;
				}
				if (!hasOpen) { player.sendMessage("One or more of your grand exchange offers have been updated!"); }
			}
		});
	}

    public boolean isProcessing() {
        return this.processing;
    }

    public void setProcessing(boolean processing) {
        this.processing = processing;
    }
}
