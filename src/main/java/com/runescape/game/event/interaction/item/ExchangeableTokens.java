package com.runescape.game.event.interaction.item;

import com.runescape.game.GameConstants;
import com.runescape.game.event.interaction.type.ItemInteractionEvent;
import com.runescape.game.interaction.controllers.impl.Wilderness;
import com.runescape.game.interaction.dialogues.impl.item.SimpleItemMessage;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.utility.Utils;
import com.runescape.utility.world.ClickOption;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/1/2015
 */
public class ExchangeableTokens extends ItemInteractionEvent {
	
	@Override
	public int[] getKeys() {
		return new int[] { Wilderness.WILDERNESS_TOKEN, 7775, 13663 };
	}

	@Override
	public boolean handleItemInteraction(Player player, Item item, ClickOption option) {
		switch (item.getId()) {
			case Wilderness.WILDERNESS_TOKEN:
				if (Wilderness.isAtWild(player) || Wilderness.isAtWildSafe(player) || player.getControllerManager().verifyControlerForOperation(Wilderness.class).isPresent()) {
					player.sendMessage("You must leave the wilderness to claim your wilderness points.");
					return true;
				}
				player.getInventory().deleteItem(item);
				player.getFacade().addWildernessPoints(item.getAmount());

				String message = "You receive " + item.getAmount() + " wilderness points from this token.";
				player.sendMessage(message);
				player.getDialogueManager().startDialogue(SimpleItemMessage.class, item.getId(), message);
				break;
			case 7775:
				player.getInventory().deleteItem(item);

				// the amount of vote points to give
				int amount = 5;

				if (GameConstants.DOUBLE_VOTES_ENABLED) {
					amount = amount * 2;
				}

				player.getFacade().setVotePoints(player.getFacade().getVotePoints() + amount);

				player.getDialogueManager().startDialogue(SimpleItemMessage.class, item.getId(), "You exchange your Vote Claimer for " + amount + " vote points", "Spend these in the vote shop at Rewards Trader.");
				break;
			case 13663:
				// giving the reward points
				player.getFacade().rewardPoints(item.getAmount());

				// deleting the item
				player.getInventory().deleteItem(item);

				// sending a message
				player.getDialogueManager().startDialogue(SimpleItemMessage.class, item.getId(), "You have just claimed " + Utils.format(item.getAmount()) + " gold points.", "You now have " + Utils.format(player.getFacade().getGoldPoints()) + " total gold points to use.", "Upgrade to a donator rank today with a membership scroll!", "These are bought from the Gold Points Store (Featured).");
				break;
		}
		return true;
	}
}
