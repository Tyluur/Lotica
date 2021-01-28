package com.runescape.game.content;

import com.runescape.game.interaction.controllers.impl.Wilderness;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 24, 2015
 */
public class MoneyPouchManagement {

	/**
	 * Toggles the visibility of the money pouch on screen
	 *
	 * @param player
	 * 		The player
	 */
	public static void toggleVisibility(Player player) {
		player.getPackets().sendRunScript(5558, 1);
		sendCoinAmount(player);
	}

	/**
	 * Sends the amount of coins over the player's money pouch
	 *
	 * @param player
	 * 		The player
	 */
	public static void sendCoinAmount(Player player) {
		player.getPackets().sendRunScript(5559, player.getFacade().getMoneyPouchCoins());
	}

	/**
	 * Withdraws coins from the player's money pouch.
	 *
	 * @param player
	 * 		The player
	 * @param amount
	 * 		The amount of coins to withdraw
	 * @param addToInventory
	 * 		If we should add the coins we withdrew to the player's inventory afterwards.
	 */
	public static int withdrawCoins(Player player, int amount, boolean addToInventory) {
		if (amount > player.getFacade().getMoneyPouchCoins()) {
			amount = player.getFacade().getMoneyPouchCoins();
		}
		if (amount <= 0) {
			return -1;
		}
		if (player.getFacade().removeMoneyPouchCoins(amount)) {
			player.getPackets().sendRunScript(5561, 0, amount);
			sendCoinAmount(player);

			if (addToInventory) {
				player.getInventory().addItemDrop(995, amount);
			}
			return amount;
		}
		return -1;
	}

	/**
	 * Adds coins to the player's money pouch
	 *
	 * @param player
	 * 		The player
	 * @param amount
	 * 		The amount of coins to add
	 */
	public static void addCoins(Player player, int amount) {
		if (amount <= 0) {
			return;
		}
		if (player.getControllerManager().verifyControlerForOperation(Wilderness.class).isPresent()) {
			player.sendMessage("You can't add coins to your pouch in the wilderness.");
			return;
		}
		if (player.getFacade().addMoneyPouchCoins(amount)) {
			player.getPackets().sendRunScript(5561, 1, amount);
			player.getInventory().deleteItem(995, amount);
			sendCoinAmount(player);
		} else {
			addCoins(player, Integer.MAX_VALUE - player.getFacade().getMoneyPouchCoins());
		}
	}

}
