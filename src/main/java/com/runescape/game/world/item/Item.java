package com.runescape.game.world.item;

import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.cache.loaders.ItemsEquipIds;

/**
 * Represents a single item. <p/>
 *
 * @author Graham
 * -    edited by Dragonkk(Alex)
 * @since 3/24/2016
 */
public class Item {

	protected int amount;

	private short id;

	protected transient ItemDefinitions definitions;

	@Override
	public Item clone() {
		return new Item(id, amount);
	}

	@Override
	public String toString() {
		return "[id=" + id + ", amount=" + amount + ", name=" + getDefinitions().name + ", value=" + getDefinitions().getValue() + "]";
	}

	public Item(int id) {
		this(id, 1);
	}

	public Item(int id, int amount) {
		this(id, amount, false);
	}

	public Item(int id, int amount, boolean amt0) {
		this.id = (short) id;
		this.amount = amount;
		if (this.amount <= 0 && !amt0) {
			this.amount = 1;
		}
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = (short) id;
	}

	public int getAmount() {
		return amount;
	}

	public Item reduceAmount(int amount) {
		this.amount -= amount;
		return this;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public int getEquipId() {
		return ItemsEquipIds.getEquipId(id);
	}

	public String getName() {
		return getDefinitions().getName();
	}

	/**
	 * Gets the {@code ItemDefinition}s {@code Object} for this {@code Item}
	 */
	public ItemDefinitions getDefinitions() {
		if (definitions == null) { definitions = ItemDefinitions.getItemDefinitions(id); }
		return definitions;
	}

}
