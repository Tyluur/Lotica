package com.runescape.game.content.economy.exchange;

import com.runescape.game.content.economy.exchange.ExchangeConfiguration.Progress;
import com.runescape.game.event.interaction.button.GrandExchangeInteractionEvent;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleNPCMessage;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;
import com.runescape.utility.external.gson.GsonStartup;
import com.runescape.utility.external.gson.loaders.ExchangeItemLoader;
import com.runescape.utility.external.gson.loaders.ExchangePriceLoader;
import com.runescape.workers.game.core.CoresManager;

import static com.runescape.game.content.economy.exchange.ExchangeConfiguration.MAIN_INTERFACE;

/**
 * Handles all interaction with grand exchange buttons, displaying of the items, collecting the items, and all internal
 * processes of the grand exchange
 *
 * @author Lazarus <lazarus.rs.king@gmail.com>
 * @since Jun 22, 2014
 */
public class ExchangeManagement {

	/**
	 * Display the main grand exchange interface with the progression of all of the player's offers
	 *
	 * @param player
	 * 		The player
	 */
	public static void sendSummary(final Player player) {
		if (player.isAnyIronman()) {
			player.getDialogueManager().startDialogue(SimpleNPCMessage.class, 2241, "As an ironman, you do not have access to the grand exchange.");
			return;
		}
		player.stopAll();
		player.getInterfaceManager().closeChatBoxInterface();
		player.getInterfaceManager().closeInventoryInterface();

		sendMainComponentConfigs(player);

		player.getPackets().sendUnlockIComponentOptionSlots(MAIN_INTERFACE, 209, -1, -1, 1, 2, 3, 5, 6);
		player.getPackets().sendUnlockIComponentOptionSlots(MAIN_INTERFACE, 211, -1, -1, 1, 2, 3, 5, 6);

		sendProgress(player);

		player.getInterfaceManager().sendInterface(MAIN_INTERFACE);
		/** Closes the search bar when the interface is closed */
		player.setCloseInterfacesEvent(() -> {
			player.getAttributes().remove("exchange_offer");
			player.getAttributes().remove("exchange_sell_item");
			closeSearchBar(player);
		});
	}

	/**
	 * Sends the progress bars information
	 *
	 * @param player
	 * 		The player
	 */
	public static void sendProgress(Player player) {
		for (int i = 0; i < 6; i++) {
			player.getPackets().sendGrandExchangeBar(player, i, 0, Progress.RESET, 0, 0, 0);
		}

		for (ExchangeOffer offer : GsonStartup.getClass(ExchangeItemLoader.class).getOffersList(player.getUsername())) {
			switch (offer.getType()) {
				case BUY:
					if (!offer.isAborted()) {
						player.getPackets().sendGrandExchangeBar(player, offer.getSlot(), offer.getItemId(), offer.isFinished() ? Progress.FINISHED_BUYING : Progress.BUY_PROGRESSING, offer.getPrice(), offer.getAmountProcessed(), offer.getAmountRequested());
					} else {
						player.getPackets().sendGrandExchangeBar(player, offer.getSlot(), offer.getItemId(), Progress.BUY_ABORTED, offer.getPrice(), offer.getAmountProcessed(), offer.getAmountRequested());
					}
					break;
				case SELL:
					if (!offer.isAborted()) {
						player.getPackets().sendGrandExchangeBar(player, offer.getSlot(), offer.getItemId(), offer.isFinished() ? Progress.FINISHED_SELLING : Progress.SELL_PROGRESSING, offer.getPrice(), offer.getAmountProcessed(), offer.getAmountRequested());
					} else {
						player.getPackets().sendGrandExchangeBar(player, offer.getSlot(), offer.getItemId(), Progress.SELL_ABORTED, offer.getPrice(), offer.getAmountProcessed(), offer.getAmountRequested());
					}
					break;
			}
		}
	}

	/**
	 * Sends the collection box to the player
	 */
	public static void openCollectionBox(Player player) {
		sendSummary(player);
		for (int i = 0; i < 6; i++) {
			GrandExchangeInteractionEvent.sendCollectInformation(player, i);
		}
		player.getInterfaceManager().sendInterface(109);
		player.getPackets().sendUnlockIComponentOptionSlots(109, 19, 0, 2, 0, 1);
		player.getPackets().sendUnlockIComponentOptionSlots(109, 23, 0, 2, 0, 1);
		player.getPackets().sendUnlockIComponentOptionSlots(109, 27, 0, 2, 0, 1);
		player.getPackets().sendUnlockIComponentOptionSlots(109, 32, 0, 2, 0, 1);
		player.getPackets().sendUnlockIComponentOptionSlots(109, 37, 0, 2, 0, 1);
		player.getPackets().sendUnlockIComponentOptionSlots(109, 42, 0, 2, 0, 1);
	}

	/**
	 * Sends the main screen configs and sets them to their default value
	 *
	 * @param player
	 * 		The player to send it to
	 */
	public static void sendMainComponentConfigs(Player player) {
		player.getPackets().sendConfig(1112, -1);
		player.getPackets().sendConfig(1113, -1);
		player.getPackets().sendConfig(1109, -1);
		player.getPackets().sendConfig(1110, 0);
		player.getPackets().sendConfig(563, 4194304);
		player.getPackets().sendConfig(1112, -1);
		player.getPackets().sendConfig(1113, -1);
		player.getPackets().sendConfig(1114, 0);
		player.getPackets().sendConfig(1109, -1);
		player.getPackets().sendConfig(1110, 0);
		player.getPackets().sendConfig(1111, 1);
		closeSearchBar(player);
	}

	/**
	 * Closes the search bar that displays the names
	 *
	 * @param player
	 * 		The player to close it for
	 */
	public static void closeSearchBar(Player player) {
		player.getPackets().sendRunScript(571);
	}

	/**
	 * Handles what is done when the player selects an item from the list of items to buy
	 *
	 * @param player
	 * 		The player
	 * @param itemId
	 * 		The item id selected
	 */
	public static void chooseBuyItem(Player player, int itemId) {
		CoresManager.SERVICE.submit(() -> {
			try {
				if (player.getAttributes().get("exchange_slot") == null) {
					System.out.println(player.getDisplayName() + " had no exchange slot selected.");
					return;
				}

				ExchangeItemLoader loader = GsonStartup.getClass(ExchangeItemLoader.class);
				ExchangePriceLoader priceLoader = GsonStartup.getClass(ExchangePriceLoader.class);

				ExchangeOffer best = loader.getBestOffer(ExchangeType.SELL, itemId);
				StringBuilder bldr = new StringBuilder();
				if (best == null) {
					bldr.append("There are no sell offers for this item.<br><br>");
				} else {
					bldr.append("We've found the best offer for you!<br><br>");
					bldr.append("Price: ").append(Utils.format(best.getPrice())).append("<br>");
					bldr.append("Quantity: ").append(best.isUnlimited() ? "Unlimited" : best.getAmountRequested() - best.getAmountProcessed());
				}
				player.getPackets().sendIComponentText(MAIN_INTERFACE, 143, bldr.toString());

				int amountToBuy = 0;
				int price = best == null ? priceLoader.getGuidePrice(itemId) : best.getPrice();
				if (price <= 0) {
					price = 1;
				}

				ExchangeOffer offer = new ExchangeOffer(player.getUsername(), itemId, ExchangeType.BUY, (int) player.getAttributes().get("exchange_slot"), amountToBuy, price);

				player.getPackets().sendConfig(1109, offer.getItemId());
				player.getPackets().sendConfig(1110, amountToBuy);
				player.getPackets().sendConfig(1111, price);
				player.getPackets().sendConfig(1114, price);
				player.getAttributes().put("exchange_offer", offer);
				player.getInterfaceManager().sendInterface(MAIN_INTERFACE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

}
