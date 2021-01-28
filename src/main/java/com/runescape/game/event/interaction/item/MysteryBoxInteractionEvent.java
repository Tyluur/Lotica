package com.runescape.game.event.interaction.item;

import com.runescape.game.event.interaction.type.ItemInteractionEvent;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.utility.Utils;
import com.runescape.utility.world.ClickOption;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 6/30/2015
 */
public class MysteryBoxInteractionEvent extends ItemInteractionEvent {

	private static final int DONATING_CHANCE = 100, VOTING_CHANCE = 300, DONATING_AMOUNT = 7, VOTING_AMOUNT = 5;

	@Override
	public int[] getKeys() {
		return new int[] { 6199, 18768 };
	}

	@Override
	public boolean handleItemInteraction(Player player, Item item, ClickOption option) {
		double randomChance;
		boolean donatorBox = item.getId() == 6199;
		int totalAmount = Utils.random(0, donatorBox ? DONATING_AMOUNT : VOTING_AMOUNT);
		int received = 0;
		List<RewardItem> itemList = new ArrayList<>();
		boolean rolledRare = false;
		for (int i = 0; i < totalAmount; i++) {
			randomChance = Utils.random(0, donatorBox ? DONATING_CHANCE : VOTING_CHANCE);
			// roll again for voting box
			if (randomChance <= 10 && !donatorBox) {
				randomChance = Utils.random(0, VOTING_CHANCE);
			}
			if (randomChance <= 10) {
				rolledRare = true;
			}
			for (MysteryBoxRewards rewards : MysteryBoxRewards.values()) {
				if (randomChance <= rewards.getChance()) {
					for (Item rewardItem : rewards.getRewards()) {
						itemList.add(new RewardItem(rewardItem, rewards));
					}
				}
			}
		}
		player.getInventory().deleteItem(item);
		if (itemList.size() == 0) {
			player.sendMessage("You weren't lucky enough to get anything... Better luck next time.");
			return true;
		}
		for (int i = 0; i < totalAmount; i++) {
			if (received++ == totalAmount) {
				break;
			}
			player.getInventory().addItemDrop(itemList.get(Utils.random(itemList.size())).getItem());
		}
		if (rolledRare) {
			player.sendMessage("The chances of you getting a rare item were high!");
		}
		return true;
	}

	private enum MysteryBoxRewards {

		VERY_RARE(1.5D, new Item(1053), new Item(1055), new Item(1057), new Item(1419), new Item(1037), new Item(1050)),
		RARE(5D, new Item(995, 1_000_000), new Item(2579), new Item(4278, 200), new Item(2577), new Item(2581), new Item(6585), new Item(6889), new Item(6914), new Item(6916), new Item(6918), new Item(6920), new Item(6922), new Item(6924)),
		COMMON(20D, new Item(12158, 100), new Item(12159, 100), new Item(12160, 100), new Item(12163, 100), new Item(15273, 100), new Item(15332, 3), new Item(995, 250_000), new Item(985), new Item(987), new Item(7390), new Item(7394), new Item(7386), new Item(7388), new Item(7392), new Item(7396)),
		JUNK(150D, new Item(995, 100_000), new Item(2366), new Item(2368), new Item(12158, 25), new Item(12159, 25), new Item(12160, 25), new Item(12163, 25), new Item(7937, 100), new Item(1437, 100));

		private final double chance;

		private final Item[] rewards;

		MysteryBoxRewards(double chance, Item... rewards) {
			this.chance = chance;
			this.rewards = rewards;
		}

        public double getChance() {
            return this.chance;
        }

        public Item[] getRewards() {
            return this.rewards;
        }
    }

	private class RewardItem {

		private final Item item;

		private final MysteryBoxRewards boxRewards;

		private RewardItem(Item item, MysteryBoxRewards boxRewards) {
			this.item = item;
			this.boxRewards = boxRewards;
		}

		@Override
		public String toString() {
			return item.toString() + ", " + boxRewards;
		}

        public Item getItem() {
            return this.item;
        }

        public MysteryBoxRewards getBoxRewards() {
            return this.boxRewards;
        }
    }

	/**
	 * The array of all party hats
	 */
	private static final Integer[] PARTY_HATS = { 1038, 1040, 1042, 1044, 1046, 1048 };
}
