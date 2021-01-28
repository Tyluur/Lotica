package com.runescape.game.world.entity.player;

import com.runescape.game.world.item.Item;
import com.runescape.game.world.item.ItemDegrading.DegradeDefinitions;
import com.runescape.game.world.item.ItemDegrading.DegradeType;
import com.runescape.utility.ChatColors;
import com.runescape.utility.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 4, 2015
 */
public class DegradeManager {

	/**
	 * The player that manages this class
	 */
	private transient Player player;

	/**
	 * The map of items and their degrade ticks
	 */
	private final Map<Integer, Integer> degradeTicks = new HashMap<>();

	/**
	 * When players equip items, they will receive a message about the time left with this item. This message is sent in
	 * intervals of this delay
	 */
	private static final long DELAY_PER_MESSAGE = TimeUnit.MINUTES.toMillis(10);

	/**
	 * This method will process all worn items by the player. We first filter through all items that are not
	 * degradeable. Once we have the items that are, we find out if they exist in the {@link #degradeTicks} map. If they
	 * don't we place them there. Once they exist, we wait until they reach their max degrade time to update them to
	 * their next stage. If they have no next stage, they are deleted and removed.
	 *
	 * @param type
	 * 		The type of armour we will be degrading
	 */
	public void process(DegradeType type) {
		Item[] items = player.getEquipment().getItems().toArray();
		for (int slot = 0; slot < items.length; slot++) {
			Item item = items[slot];
			if (item == null) {
				continue;
			}
			DegradeDefinitions definitions = DegradeDefinitions.getDegradeDefinition(item.getId());
			// If this item can't be degraded, we should not modify anything
			if (definitions == null) {
				continue;
			}
			// if the degrade type isnt the type we're modifying, we skip it
			if (type != definitions.getType()) {
				continue;
			}
			// If the item hasn't degraded yet
			if (!degradeExists(item.getId())) {
				degradeTicks.put(item.getId(), 0);
			} else {
				int ticksPassed = getTicksPassed(item.getId());
				// the ticks passed with this item aren't high enough to degrade
				if (ticksPassed < definitions.getDegradeTicks()) {
					increaseTicks(item.getId());
					continue;
				}
				int firstId = item.getId();
				int nextId = definitions.getNextDegradeId(item.getId());
				String itemName = item.getName().toLowerCase();
				if (nextId == -1) {
					player.getEquipment().deleteItem(item.getId(), item.getAmount());
					player.sendMessage("<col=" + ChatColors.RED + ">It looks like your " + itemName + " has degraded from overuse...");
				} else {
					// if the item should be added to the inventory after degrading
					if (definitions.inventoryOnCompletion(item.getId())) {
						player.getEquipment().deleteItem(item.getId(), item.getAmount());
						player.getInventory().addItemDrop(nextId, item.getAmount());
					} else {
						item.setId(nextId);
						player.getEquipment().refresh(slot);
					}
					player.sendMessage("<col=" + ChatColors.RED + ">Your " + itemName + " has degraded.");
				}
				degradeTicks.remove(firstId);
				player.getAppearence().generateAppearenceData();
			}
		}
	}

	/**
	 * When equipping items, it is important to know how much time left you have with the item until it degrades. Every
	 * {@link #DELAY_PER_MESSAGE} minutes, this message is sent if the player equips the item.
	 *
	 * @param item
	 * 		The item we're wearing
	 */
	public void sendDegradeInformation(Item item) {
		boolean shouldSendMessage = false;
		if (!degradeExists(item.getId())) {
			//if (GameConstants.DEBUG) { System.out.println("degrade doesnt exist"); }
			return;
		}
		DegradeDefinitions definitions = DegradeDefinitions.getDegradeDefinition(item.getId());
		if (definitions == null) {
			//if (GameConstants.DEBUG) { System.out.println("no definitions"); }
			return;
		}
		Long lastTimeSent = player.getAttribute("degrade_item_" + item.getId());
		if (lastTimeSent == null) {
			shouldSendMessage = true;
		} else {
			long difference = System.currentTimeMillis() - lastTimeSent;
			if (difference >= DELAY_PER_MESSAGE) {
				shouldSendMessage = true;
			}
		}
		int ticksPassed = getTicksPassed(item.getId());
		int ticksToReach = definitions.getDegradeTicks();
		int ticksLeft = ticksToReach - ticksPassed;
		if (ticksLeft <= 0) {
			shouldSendMessage = false;
		}
		if (shouldSendMessage) {
			long millis = ticksLeft * 600;
			player.sendMessage("You have " + Utils.convertMillisecondsToTime(millis) + "left with your " + item.getName().toLowerCase() + " " + definitions.getType().getDegradeInformation() + ".");
			player.putAttribute("degrade_item_" + item.getId(), System.currentTimeMillis());
		}
	}

	/**
	 * Sends the time left for an item
	 *
	 * @param item
	 * 		The item
	 */
	public void sendTimeLeft(Item item) {
		DegradeDefinitions definitions = DegradeDefinitions.getDegradeDefinition(item.getId());
		if (definitions == null) {
			//if (GameConstants.DEBUG) { System.out.println("no definitions"); }
			return;
		}
		int degradeTicks = definitions.getDegradeTicks();
		if (degradeTicks <= 0) {
			return;
		}
		int ticksLeft = degradeTicks - getTicksPassed(item.getId());
		long millis = ticksLeft * 600;
		player.sendMessage("You have " + Utils.convertMillisecondsToTime(millis) + "left with your " + item.getName().toLowerCase() + " " + definitions.getType().getDegradeInformation() + ".");
	}

	/**
	 * Increases the ticks in the {@link #degradeTicks} map for the itemId
	 *
	 * @param itemId
	 * 		The id of the item we must increment the ticks of
	 */
	private void increaseTicks(int itemId) {
		int current = degradeTicks.get(itemId);
		degradeTicks.put(itemId, current + 1);
	}

	/**
	 * Checking if the {@link #degradeTicks} map contains the item
	 *
	 * @param itemId
	 * 		The item
	 */
	private boolean degradeExists(int itemId) {
		return degradeTicks.containsKey(itemId);
	}

	/**
	 * Gets the ticks that have passed with this itemId from the {@link #degradeTicks} map
	 *
	 * @param itemId
	 * 		The item id
	 */
	public int getTicksPassed(int itemId) {
		Integer ticksPassed = degradeTicks.get(itemId);
		if (ticksPassed == null) {
			return -1;
		}
		return ticksPassed;
	}

	/**
	 * @return the player
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * @param player
	 * 		the player to set
	 */
	public void setPlayer(Player player) {
		this.player = player;
	}
}
