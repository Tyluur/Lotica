package com.runescape.game.event.interaction.npc;

import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.game.content.ArmourSets;
import com.runescape.game.content.economy.exchange.ExchangeManagement;
import com.runescape.game.content.economy.exchange.ExchangeOffer;
import com.runescape.game.content.economy.exchange.ExchangeType;
import com.runescape.game.event.interaction.type.NPCInteractionEvent;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;
import com.runescape.utility.world.ClickOption;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/13/2016
 */
public class GrandExchangeClerkInteractionEvent extends NPCInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] { 2241 };
	}

	@Override
	public boolean handleNPCInteraction(Player player, NPC npc, ClickOption option) {
		if (option == ClickOption.SECOND) {
			ExchangeManagement.sendSummary(player);
		} else if (option == ClickOption.FOURTH) {
			showOfferHistory(player);
		} else if (option == ClickOption.THIRD) {
			ArmourSets.openSets(player);
		}
		return true;
	}

	/**
	 * Shows the offer history interface to the player
	 * @param player The player
	 */
	private void showOfferHistory(Player player) {
		int interfaceId = 643;
		player.getInterfaceManager().sendInterface(interfaceId);
		for (int i = 0; i < 5; i++) {
			ExchangeOffer[] offerHistory = player.getFacade().getOfferHistory();
			ExchangeOffer offer = i >= offerHistory.length ? null : offerHistory[i];

			player.getPackets().sendIComponentText(643, 25 + i, offer == null ? "" : offer.getType() == ExchangeType.BUY ? "You bought" : "You sold");
			player.getPackets().sendIComponentText(643, 35 + i, offer == null ? "" : ItemDefinitions.getItemDefinitions(offer.getItemId()).getName());
			player.getPackets().sendIComponentText(643, 30 + i, offer == null ? "" : Utils.format(offer.getAmountProcessed()));
			player.getPackets().sendIComponentText(643, 40 + i, offer == null ? "" : Utils.format(offer.getPrice()));
		}
	}

}
