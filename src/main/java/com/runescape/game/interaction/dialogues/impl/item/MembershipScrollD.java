package com.runescape.game.interaction.dialogues.impl.item;

import com.runescape.game.content.economy.shopping.impl.GoldPoints;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.world.entity.player.rights.Right;
import com.runescape.game.world.entity.player.rights.RightManager;
import com.runescape.utility.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 8/10/2016
 */
public class MembershipScrollD extends Dialogue {

	private enum Upgrades {

		DONATOR(1000, 250),
		SUPREME_DONATOR(5000, 1000),
		EXTREME_DONATOR(10000, 1000),
		LEGENDARY_DONATOR(25000, 2500),
		ELITE_DONATOR(50000, 5000);

		/**
		 * The required amount of gold points to have this rank
		 */
		private final int[] goldPointCosts;

		Upgrades(int... goldPointCosts) {
			this.goldPointCosts = goldPointCosts;
		}

		/**
		 * Gets the proper name of the price
		 */
		public String getProperName() {
			return name().substring(0, 1) + name().substring(1, name().length()).toLowerCase().replaceAll("_", " ");
		}

        public int[] getGoldPointCosts() {
            return this.goldPointCosts;
        }
    }

	/**
	 * The selected price
	 */
	private Upgrades selectedUpgrade = null;

	/**
	 * If this is a reupgrade
	 */
	private boolean reupgrade = false;

	@Override
	public void start() {
		List<String> text = new ArrayList<>();
		text.add(DEFAULT_OPTIONS);
		for (Upgrades upgrade : Upgrades.values()) {
			text.add(upgrade.getProperName() + " [" + Utils.format(upgrade.getGoldPointCosts()[previouslyPurchased(upgrade) ? 1 : 0]) + " GP]");
		}
		sendOptionsDialogue(text.toArray(new String[text.size()]));
	}

	@Override
	public void run(int interfaceId, int option) {
		switch (stage) {
			case -1:
				selectedUpgrade = Upgrades.values()[option - 1];
				if (previouslyPurchased(selectedUpgrade)) {
					reupgrade = true;
				}
				sendDialogue("Please confirm that you wish to " + (reupgrade ? "re" : "") + "upgrade to: " + selectedUpgrade.getProperName());
				stage = 0;
				break;
			case 0:
				sendOptionsDialogue(DEFAULT_OPTIONS, "" + (reupgrade ? "Reu" : "U") + "pgrade to " + selectedUpgrade.getProperName() + " for " + Utils.format(selectedUpgrade.getGoldPointCosts()[reupgrade ? 1 : 0]) + " gold points.", "Cancel upgrade request.");
				stage = 1;
				break;
			case 1:
				if (option == FIRST) {
					if (!takePoints(selectedUpgrade.getGoldPointCosts()[reupgrade ? 1 : 0])) {
						sendDialogue("You did not have enough gold points to upgrade to that rank.");
					} else {
						sendItemDialogue(3709, 1, "Your are now a " + selectedUpgrade.getProperName() + "!");
						Optional<Right> right = RightManager.getRight(selectedUpgrade.name());
						if (!right.isPresent()) {
							throw new IllegalStateException("Couldnt find a right for " + selectedUpgrade);
						}
						addHistory(selectedUpgrade);
						player.addToMembershipGroup(right.get());
					}
					stage = -2;
				} else {
					end();
				}
				break;
		}
	}

	@Override
	public void finish() {

	}

	/**
	 * Takes the points, if possible, from the players inv/points amount
	 *
	 * @param amount
	 * 		The amount of points
	 * @return {@code True} if successful
	 */
	private boolean takePoints(int amount) {
		if (player.getFacade().getGoldPoints() >= amount) {
			player.getFacade().setGoldPoints(player.getFacade().getGoldPoints() - amount);
			return true;
		} else if (player.getInventory().getNumerOf(GoldPoints.GOLD_POINT_TICKET) >= amount) {
			player.getInventory().deleteItem(GoldPoints.GOLD_POINT_TICKET, amount);
			return true;
		}
		return false;
	}

	/**
	 * Checks if the upgrade was previously purchased
	 *
	 * @param upgrade
	 * 		The upgrade
	 */
	private boolean previouslyPurchased(Upgrades upgrade) {
		return player.getFacade().getDonatorRanksPurchased().contains(upgrade.name());
	}

	/**
	 * Adds history
	 *
	 * @param price
	 * 		The price to add
	 */
	private void addHistory(Upgrades price) {
		String name = price.name();
		if (!player.getFacade().getDonatorRanksPurchased().contains(name)) {
			player.getFacade().getDonatorRanksPurchased().add(name);
		}
	}
}
