package com.runescape.game.event.interaction.item;

import com.runescape.game.event.interaction.type.ItemInteractionEvent;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.utility.ChatColors;
import com.runescape.utility.Utils;
import com.runescape.utility.world.ClickOption;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/14/2016
 */
public class CasketInteractionEvent extends ItemInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] { 7312 };
	}

	@Override
	public boolean handleItemInteraction(Player player, Item item, ClickOption option) {
		player.getInventory().deleteItem(item);
		giveCasketLoot(player);
		return true;
	}

	/**
	 * Gives casket loot to the player
	 *
	 * @param player
	 * 		The player
	 */
	public static void giveCasketLoot(Player player) {
		boolean shouldGiveLoop = Utils.percentageChance(15);
		if (shouldGiveLoop) {
			Integer[] halves = new Integer[] { 985, 987 };
			player.getInventory().addItem(Utils.randomArraySlot(halves), 1);
		} else {
			int random = 1_000 + Utils.random(100, 80_000);
			player.getInventory().addItem(995, random);
			player.sendMessage("<col=" + ChatColors.GREEN + ">You open the casket and find " + Utils.format(random) + "gp inside it.");
		}
	}
}
