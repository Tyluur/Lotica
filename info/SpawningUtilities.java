/*
package com.zulrune.game.content.economy;

import com.zulrune.game.content.economy.treasure.TreasureTrailData.TreasureTrailTier;
import com.zulrune.game.world.entity.player.Player;
import com.zulrune.game.world.item.Item;
import com.zulrune.game.world.item.ItemProperties;
import com.zulrune.utility.ChatColors;
import com.zulrune.utility.Utils;
import com.zulrune.utility.external.gson.loaders.StoreLoader;
import com.zulrune.workers.game.CoresManager;
import com.zulrune.workers.game.log.GameLog;

*/
/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 12/31/2015
 *//*

public class SpawningUtilities {

	*/
/**
	 * Handles the spawning of an item
	 *
	 * @param player
	 * 		The player
	 * @param item
	 * 		The item
	 *//*

	public static boolean spawnItem(Player player, Item item) {
		if (!player.isAvailable()) {
			player.sendMessage("You cannot spawn items in this activity.");
			return false;
		}

		if (!player.getInventory().getItems().canAdd(item)) {
			boolean full = false;
			if (!item.getDefinitions().isStackable()) {
				item.setAmount(player.getInventory().getFreeSlots());
				if (item.getAmount() == 0 || !player.getInventory().getItems().canAdd(item)) {
					full = true;
				}
			} else {
				full = true;
			}
			if (full) {
				player.sendMessage("Not enough inventory space.");
				return false;
			}
		}


		// we can spawn the item by default
		boolean canSpawn = true;

		// the reason the player will see if they cannot spawn an item
		String reason = null;

		// doing checks to ensure the item can be spawned
		// we do else statements because if one of these are true, it wont be spawned
		if (containedInRareShopStocks(item)) {
			canSpawn = false;
			reason = "it must be bought from shops";
		} else if (!ItemProperties.isSpawnable(item.getName())) {
			canSpawn = false;
			reason = "it is not spawnable";
		} else if (item.getDefinitions().isLended()) {
			canSpawn = false;
			reason = "lended items cannot be spawned";
		} else if (ItemProperties.isUntradeable(item)) {
			canSpawn = false;
			reason = "it is untradeable and therefore unspawnable";
		} else if (TreasureTrailTier.isTreasureTrailReward(item.getId())) {
			canSpawn = false;
			reason = "it can only be received from treasure trails";
		}

		if (reason == null) {
			reason = "it must be accessed another way";
		}

		// there are some items that are guaranteed to be able to be spawned
		if (!canSpawn && ItemProperties.guaranteedSpawn(item.getId(), item.getName())) {
			canSpawn = true;
		}

		if (!canSpawn) {
			player.sendMessage("You cannot spawn this item - <col=" + ChatColors.MAROON + ">" + reason + "</col>.");
			return false;
		}

		// converting to a long to hold a larger number
		long totalCost = (long) (item.getAmount() * getItemCost(item));

		// if the cost is too great, it would end up breaking the spawn command
		if (totalCost > Integer.MAX_VALUE) {
			player.sendMessage("You can't spawn that many of this item... Try again with a lower quantity.");
			return false;
		}

		// we now know it can be held as an integer
		int intValue = (int) totalCost;

		if (player.takeMoney(intValue)) {
			CoresManager.logProcessor.appendLog(new GameLog(player.getUsername(), "Spawned " + item + " for " + intValue + " coins."));
			player.getInventory().addItemDrop(item);
			player.sendMessage("You spawn " + item.getAmount() + "x " + item.getName().toLowerCase() + " for " + Utils.format(intValue) + "gp.");
			return true;
		} else {
			player.sendMessage("You need " + Utils.format(intValue) + " coins to spawn " + item.getAmount() + "x " + item.getName().toLowerCase() + ".");
			return false;
		}
	}

	*/
/**
	 * Before a player can spawn an item, we must confirm that the item is not in the stocks of rare shops.
	 *
	 * @param item
	 * 		The item we're checking all stocks for
	 *//*

	private static boolean containedInRareShopStocks(Item item) {
		return StoreLoader.isContainedInRareStock(item.getId());
	}

	*/
/**
	 * Gets the cost of spawning an item
	 *
	 * @param item
	 * 		The item
	 *//*

	private static int getItemCost(Item item) {
		return (int) Math.max((item.getDefinitions().getValue() * 1.15), 1);
	}
}



*/
