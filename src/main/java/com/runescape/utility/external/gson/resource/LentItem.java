package com.runescape.utility.external.gson.resource;

import com.runescape.game.interaction.dialogues.impl.item.SimpleItemMessage;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/20/2015
 */
public class LentItem {

	/**
	 * The name of the player who lent the item.
	 */
	private final String lenderName;

	/**
	 * The name of the player who received the lent item
	 */
	private final String receiverName;

	/**
	 * The id of the original item
	 */
	private final int originalItemId;

	/**
	 * The id of the lent item
	 */
	private final int lentItemId;

	/**
	 * The amount of hours the item was lent for
	 */
	private final long hoursLentFor;

	/**
	 * The time the item was lent until
	 */
	private final long lentUntil;

	/**
	 * If the item was lent until logout
	 */
	private final boolean lentTillLogout;

	/**
	 * If the lent item is available for collection
	 */
	private boolean availableForCollection;

	public LentItem(String lenderName, String receiverName, int originalItemId, int lentItemId, long hoursLentFor, long lentUntil, boolean lentTillLogout) {
		this.lenderName = lenderName;
		this.receiverName = receiverName;
		this.originalItemId = originalItemId;
		this.lentItemId = lentItemId;
		this.hoursLentFor = hoursLentFor;
		this.lentUntil = lentUntil;
		this.lentTillLogout = lentTillLogout;
	}

	public String getLenderName() {
		return lenderName;
	}

	public String getReceiverName() {
		return receiverName;
	}

	public int getOriginalItemId() {
		return originalItemId;
	}

	public int getLentItemId() {
		return lentItemId;
	}

	public long getHoursLentFor() {
		return hoursLentFor;
	}

	public long getLentUntil() {
		return lentUntil;
	}

	public boolean isLentTillLogout() {
		return lentTillLogout;
	}

	public boolean isAvailableForCollection() {
		return availableForCollection;
	}

	public long getHoursLeft() {
		return lentUntil - System.currentTimeMillis();
	}

	/**
	 * This method sends a notification to the player who lent the item that they can claim an item they lent out.
	 */
	public void notifyLenderToCollect() {
		Player lender = World.getPlayer(lenderName);
		if (lender == null) {
			return;
		}
		String message = "An item you lent is now available in your collection box.<br>Speak to a banker to claim it.";
		lender.sendMessage(message);
		lender.getDialogueManager().startDialogue(SimpleItemMessage.class, originalItemId, message);
	}

	public void setAvailableForCollection(boolean availableForCollection) {
		this.availableForCollection = availableForCollection;
		if (availableForCollection) {
			notifyLenderToCollect();
		}
	}
}
