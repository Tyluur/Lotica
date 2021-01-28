package com.runescape.game.event.interaction.object;

import com.runescape.game.event.interaction.type.ObjectInteractionEvent;
import com.runescape.game.interaction.dialogues.impl.item.SimpleItemMessage;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.utility.Utils;
import com.runescape.utility.world.ClickOption;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/2/2015
 */
public class CrystalChestInteractionEvent extends ObjectInteractionEvent {
	
	private static final double[][] REWARDS = new double[][] {
			//
			{ 6571, 5 }, // uncut-onyx
			{ 2572, 10 }, // ring of wealth
			{ 7394, 12.5 }, // wiz g
			{ 7386, 12.5 }, // wiz g
			{ 7390, 12.5 }, // wiz g
			{ 7388, 20.5 }, // wiz t
			{ 7392, 20.5 }, // wiz t
			{ 7396, 20.5 }, // wiz t
			{ 2595, 10 }, // black g
			{ 2591, 10 }, // black g
			{ 2593, 10 }, // black g
			{ 2597, 10 }, // black g
			{ 2587, 15 }, // black t
			{ 2583, 15 }, // black t
			{ 2585, 15 }, // black t
			{ 2589, 15 }, // black t
			{ 4224, 5 }, // crystal shield
			{ 4212, 5 } // crystal bow
	};

	private static final int[][] JUNK_REWARDS = new int[][] {
			//
			{ 1635, 1 }, // gold ring
			{ 1637, 1 }, // sapphire ring
			{ 1639, 1 }, // emerald ring
			{ 1641, 1 }, // ruby ring
			{ 1641, 1 }, // diamond ring
			{ 5318, 100 }, // potato seed
			{ 5291, 100 }, // guam seed
			{ 5096, 100 }, // marigold seed
			{ 5101, 100 }, // redberry seed
			{ 2352, 25 }, // iron bar
			{ 2364, 25 }, // rune bar
			{ 995, 500000 }, // coins
	};

	@Override
	public int[] getKeys() {
		return new int[] { 11231 };
	}

	@Override
	public boolean handleObjectInteraction(Player player, WorldObject object, ClickOption option) {
		if (!player.getInventory().contains(989)) {
			player.sendMessage("You need a crystal key to unlock this chest.");
			return true;
		}
		openChest(player);
		return true;
	}

	/**
	 * Opens the chest for a player and gives rewards from a random slot in the {@link #REWARDS} array
	 *
	 * @param player
	 * 		The player opening the chest
	 */
	public static void openChest(final Player player) {
		player.getInventory().deleteItem(989, 1);
		player.setNextAnimation(new Animation(536));
		List<Item> rewards = new ArrayList<>();
		for (int i = 0; i < Utils.random(1, 3); i++) {
			double chance = Utils.random(1, 50);
			int index = Utils.random(REWARDS.length);
			if (chance <= REWARDS[index][1]) {
				rewards.add(new Item((int) REWARDS[index][0], 1));
			}
		}
		int index = Utils.random(JUNK_REWARDS.length);
		int amt = JUNK_REWARDS[index][1];
		rewards.add(new Item(JUNK_REWARDS[index][0], amt == 1 ? amt : Utils.random(amt)));
		for (Item item : rewards) {
			player.getInventory().addItemDrop(item.getId(), item.getAmount());
		}
		player.getInventory().addItemDrop(1631, 1);
		player.getDialogueManager().startDialogue(SimpleItemMessage.class, 989, "You find some treasure in the chest!");
	}
}
