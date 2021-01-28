package com.runescape.game.world.item;

import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.player.Player;

public class FloorItem extends Item {

	private transient WorldTile tile;

	private transient Player owner;

	private boolean invisible;

	private boolean publicItem;

	// 0 visible, 1 invisible, 2 visible and reappears 30sec after taken
	private int type;

	public FloorItem(int id) {
		super(id);
	}

	@Override
	public void setAmount(int amount) {
		this.amount = amount;
	}

	public FloorItem(Item item, WorldTile tile, Player owner, boolean publicItem, boolean invisible) {
		super(item.getId(), item.getAmount());
		this.tile = tile;
		this.owner = owner;
		this.publicItem = publicItem;
		this.invisible = invisible;
	}

	public FloorItem(Item item, WorldTile tile, boolean appearforever) {
		super(item.getId(), item.getAmount());
		this.tile = tile;
		this.type = appearforever ? 2 : 0;
	}

	public WorldTile getTile() {
		return tile;
	}

	public boolean isPublicItem() {
		return publicItem;
	}

	public boolean isInvisible() {
		return invisible;
	}

	public Player getOwner() {
		return owner;
	}

	public boolean hasOwner() {
		return owner != null;
	}

	public boolean isForever() {
		return type == 2;
	}

	public void setInvisible(boolean invisible) {
		this.invisible = invisible;
	}
	
	@Override
	public String toString() {
		return "[id=" + getId() + ", amount=" + getAmount() + ", owner=" + (owner == null ? "null" : getOwner().getUsername()) + ", tile=" + tile + "";
	}

}
