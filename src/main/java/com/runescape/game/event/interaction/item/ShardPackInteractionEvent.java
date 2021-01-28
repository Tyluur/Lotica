package com.runescape.game.event.interaction.item;

import com.runescape.game.event.interaction.type.ItemInteractionEvent;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.utility.world.ClickOption;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/4/2015
 */
public class ShardPackInteractionEvent extends ItemInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] { 15262 };
	}

	@Override
	public boolean handleItemInteraction(Player player, Item item, ClickOption option) {
		openPack(player, option == ClickOption.SECOND);
		return true;
	}

	/**
	 * Handles the opening of spirit shard packs
	 *
	 * @param player
	 * 		The player
	 * @param all
	 * 		If we should open all the packs the player has
	 */
	private void openPack(Player player, boolean all) {
		int numToOpen = all ? player.getInventory().getNumerOf(15262) : 1;
		long shardsToGive = 5000 * numToOpen;
		if (shardsToGive > Integer.MAX_VALUE) {
			player.sendMessage("You can't open that many...");
			return;
		}
		int intShards = (int) shardsToGive;
		player.getInventory().deleteItem(15262, numToOpen);
		player.getInventory().addItem(12183, intShards);
	}
}
