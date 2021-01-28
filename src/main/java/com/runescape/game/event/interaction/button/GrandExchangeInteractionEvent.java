package com.runescape.game.event.interaction.button;

import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.game.content.economy.exchange.ExchangeManagement;
import com.runescape.game.content.economy.exchange.ExchangeOffer;
import com.runescape.game.content.economy.exchange.ExchangeType;
import com.runescape.game.content.economy.exchange.ExchangeWorker;
import com.runescape.game.event.InputEvent;
import com.runescape.game.event.InputEvent.InputEventType;
import com.runescape.game.event.interaction.type.InterfaceInteractionEvent;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AchievementHandler;
import com.runescape.game.world.entity.player.achievements.easy.Grand_Exchanger;
import com.runescape.game.world.item.Item;
import com.runescape.game.world.item.ItemConstants;
import com.runescape.game.world.item.ItemsContainer;
import com.runescape.network.codec.decoders.WorldPacketsDecoder;
import com.runescape.utility.Utils;
import com.runescape.utility.external.gson.GsonStartup;
import com.runescape.utility.external.gson.loaders.ExchangeItemLoader;
import com.runescape.utility.external.gson.loaders.ExchangePriceLoader;
import com.runescape.utility.external.gson.loaders.ItemInformationLoader;
import com.runescape.workers.game.core.CoresManager;
import com.runescape.workers.game.log.GameLog;
import com.runescape.workers.game.log.GameLogProcessor;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

import static com.runescape.game.content.economy.exchange.ExchangeConfiguration.*;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/13/2016
 */
public class GrandExchangeInteractionEvent extends InterfaceInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] { COLLECTION_INTERFACE, MAIN_INTERFACE, SELL_INTERFACE };
	}

	@Override
	public boolean handleInterfaceInteraction(Player player, int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
		ExchangeOffer offer;
		switch (interfaceId) {
			case MAIN_INTERFACE: {
				switch (componentId) {
					case 31: // BUY
					case 82:
					case 101:
					case 47:
					case 63:
					case 120:
						player.getAttributes().put("exchange_slot", getSlot(componentId));
						sendScreen(player, ExchangeType.BUY);
						break;
					case 83: // SELL
					case 32:
					case 48:
					case 102:
					case 121:
					case 64:
						player.getAttributes().put("exchange_slot", getSlot(componentId));
						sendScreen(player, ExchangeType.SELL);
						break;
					case 128: // back
						resetInterfaceConfigs(player);
						player.getInterfaceManager().closeInventory();
						player.getInterfaceManager().sendInventory();
						final int lastGameTab = player.getInterfaceManager().openGameTab(4); // inventory
						player.setCloseInterfacesEvent(() -> {
							player.getInterfaceManager().sendInventory();
							player.getInventory().unlockInventoryOptions();
							player.getInterfaceManager().sendEquipment();
							player.getInterfaceManager().openGameTab(lastGameTab);
						});
						ExchangeManagement.sendSummary(player);
						break;
					case 190: // choose item button
						player.getPackets().sendRunScript(570, "Grand Exchange Item Search");
						break;
					case 157: // +1
						offer = (ExchangeOffer) player.getAttributes().get("exchange_offer");
						increaseAmount(player, offer, 1);
						break;
					case 160:
						offer = (ExchangeOffer) player.getAttributes().get("exchange_offer");
						if (offer == null) { break; }
						if (offer.getType() == ExchangeType.SELL) {
							offer.setAmountRequested(1);
							player.getPackets().sendConfig(1110, offer.getAmountRequested());
						} else {
							increaseAmount(player, offer, 1);
						}
						break;
					case 162:
						offer = (ExchangeOffer) player.getAttributes().get("exchange_offer");
						if (offer == null) { break; }
						if (offer.getType() == ExchangeType.SELL) {
							offer.setAmountRequested(10);
							player.getPackets().sendConfig(1110, offer.getAmountRequested());
						} else {
							increaseAmount(player, offer, 10);
						}
						break;
					case 164:
						offer = (ExchangeOffer) player.getAttributes().get("exchange_offer");
						if (offer == null) { break; }
						if (offer.getType() == ExchangeType.SELL) {
							offer.setAmountRequested(100);
							player.getPackets().sendConfig(1110, offer.getAmountRequested());
						} else {
							increaseAmount(player, offer, 100);
						}
						break;
					case 166:
						offer = (ExchangeOffer) player.getAttributes().get("exchange_offer");
						if (offer == null) { break; }
						switch (offer.getType()) {
							case BUY:
								increaseAmount(player, offer, 1000);
								break;
							case SELL:
								if (player.getAttributes().get("exchange_sell_item") != null) {
									int[] ids = (int[]) player.getAttributes().get("exchange_sell_item");
									offer.setAmountRequested(player.getInventory().getNumerOf(ids[0]));
								} else {
									offer.setAmountRequested(player.getInventory().getNumerOf(offer.getItemId()));
								}
								player.getPackets().sendConfig(1110, offer.getAmountRequested());
								break;
						}
						break;
					case 168:
						offer = (ExchangeOffer) player.getAttributes().get("exchange_offer");
						if (offer == null) { break; }
						final ExchangeOffer offer2 = offer;
						player.getPackets().requestClientInput(new InputEvent("Enter amount", InputEventType.INTEGER) {
							@Override
							public void handleInput() {
								offer2.setAmountRequested(getInput());
								player.getPackets().sendConfig(1110, offer2.getAmountRequested());
							}
						});
						break;
					case 181: // -5%
						offer = (ExchangeOffer) player.getAttributes().get("exchange_offer");
						if (offer == null) { break; }
						offer.setPrice((int) Math.ceil(offer.getPrice() - (offer.getPrice() * 0.05)));
						player.getPackets().sendConfig(1111, offer.getPrice());
						break;
					case 175: // set guide price
						offer = (ExchangeOffer) player.getAttributes().get("exchange_offer");
						if (offer == null) { break; }
						offer.setPrice(GsonStartup.getClass(ExchangePriceLoader.class).getGuidePrice(offer.getItemId()));
						player.getPackets().sendConfig(1111, offer.getPrice());
						break;
					case 169:
					case 171:
						offer = (ExchangeOffer) player.getAttributes().get("exchange_offer");
						if (offer == null) { break; }
						offer.setPrice(offer.getPrice() + (componentId == 169 ? -1 : 1));
						player.getPackets().sendConfig(1111, offer.getPrice());
						break;
					case 177: // input price
						offer = (ExchangeOffer) player.getAttributes().get("exchange_offer");
						if (offer == null) { break; }
						player.getPackets().requestClientInput(new InputEvent("Enter amount", InputEventType.INTEGER) {
							@Override
							public void handleInput() {
								((ExchangeOffer) player.getAttributes().get("exchange_offer")).setPrice(getInput());
								player.getPackets().sendConfig(1111, ((ExchangeOffer) player.getAttributes().get("exchange_offer")).getPrice());
							}
						});
						break;
					case 179: // +5%
						offer = (ExchangeOffer) player.getAttributes().get("exchange_offer");
						if (offer == null) { break; }
						offer.setPrice((int) Math.ceil(offer.getPrice() + (offer.getPrice() * 0.05)));
						player.getPackets().sendConfig(1111, offer.getPrice());
						break;
					case 186: // confirm
						offer = (ExchangeOffer) player.getAttributes().get("exchange_offer");
						if (offer == null) { break; }
						final long requestedCash = (long) (offer.getAmountRequested() * offer.getPrice());
						
						if (requestedCash > Integer.MAX_VALUE || requestedCash == Integer.MAX_VALUE || requestedCash >= Integer.MAX_VALUE || requestedCash < 0 || requestedCash <= 0 || offer.getAmountRequested() == 0 || offer.getPrice() == 0) {
							player.getPackets().sendGameMessage("Invalid input.");
							return true;
						}
						int cashAmount = (int) requestedCash;
						switch (offer.getType()) {
							case BUY:
								if (player.takeMoney(cashAmount)) {
									ExchangeItemLoader loader = GsonStartup.getClass(ExchangeItemLoader.class);
									loader.addOffer(offer);
									ExchangeManagement.sendSummary(player);
								} else {
									player.sendMessage("You do not have " + Utils.format(requestedCash) + " coins to make this exchange.");
									return true;
								}
								break;
							case SELL:
								int noteId = -1;
								int sellId = -1;
								if (player.getAttributes().get("exchange_sell_item") != null) {
									int[] ids = (int[]) player.getAttributes().get("exchange_sell_item");
									sellId = ids[1];
									noteId = ids[0];
								} else {
									sellId = offer.getItemId();
								}
								int sellingId = noteId == -1 ? sellId : noteId;
								if (player.getInventory().getNumerOf(sellingId) < offer.getAmountRequested()) {
									player.sendMessage("You do not have " + Utils.format(offer.getAmountRequested()) + " of this item to sell.");
									return true;
								}
								player.getInventory().deleteItem(sellingId, offer.getAmountRequested());
								ExchangeItemLoader loader = GsonStartup.getClass(ExchangeItemLoader.class);
								loader.addOffer(offer);
								ExchangeManagement.sendSummary(player);
								break;
						}
						if (offer.getType() == ExchangeType.BUY) {
							AchievementHandler.incrementProgress(player, Grand_Exchanger.class, 1);
						}
						CoresManager.SERVICE.submit(() -> {
							try {
								GameLogProcessor.submitLog(new GameLog("exchange", player.getUsername(), offer + " was submitted."));
								ExchangeWorker.get().queue(offer);
							} catch (Exception e) {
								e.printStackTrace();
							}
						});
						break;
					case 155:
						offer = (ExchangeOffer) player.getAttributes().get("exchange_offer");
						increaseAmount(player, offer, -1);
						break;
					case 19:
					case 35:
					case 51:
					case 108:
					case 89:
					case 70:
						player.getAttributes().put("exchange_slot", getSlot(componentId));
						offer = getOfferBySlot(player, getSlot(componentId));
						if (offer == null) { break; }
						if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
							sendCollectInformation(player, getSlot(componentId));
						} else {
							abortOffer(player, offer);
						}
						break;
					case 200: // abort via information screen:
						offer = getOfferBySlot(player, (int) player.getAttributes().get("exchange_slot"));
						if (offer == null) { break; }
						abortOffer(player, offer);
						break;
					case 208:
					case 206: // collecting
						offer = getOfferBySlot(player, (int) player.getAttributes().get("exchange_slot"));
						if (offer == null) { break; }
						collectItem(player, offer, slotId2, packetId, componentId);
						break;
				}
			}
			break;
			case SELL_INTERFACE: {
				if (!ItemConstants.isTradeable(new Item(slotId2)) || slotId2 == 995) {
					player.sendMessage("That item cannot be sold on the grand exchange.");
					return true;
				}
				player.getAttributes().remove("exchange_sell_item");

				int itemId2 = slotId2;
				if (ItemDefinitions.getItemDefinitions(slotId2).isNoted()) {
					itemId2 = ItemDefinitions.getItemDefinitions(slotId2).getCertId();
				}

				final int amountToSell = 1;
				final int price = ExchangePriceLoader.getInfiniteQuantityPrice(slotId2);

				ExchangeOffer sellOffer = new ExchangeOffer(player.getUsername(), itemId2, ExchangeType.SELL, (int) player.getAttributes().get("exchange_slot"), amountToSell, price);

				player.getAttributes().put("exchange_offer", sellOffer);
				if (itemId2 != slotId2) {
					player.getAttributes().put("exchange_sell_item", new int[] { slotId2, itemId2 });
				}

				player.getPackets().sendConfig(1109, sellOffer.getItemId());
				player.getPackets().sendConfig(1110, amountToSell);
				player.getPackets().sendConfig(1111, price);
				player.getPackets().sendConfig(1114, price);

				StringBuilder bldr = new StringBuilder();
				bldr.append(ItemInformationLoader.getExamine(itemId2)).append("<br><br>");
				bldr.append("The grand exchange will purchase this item for 5% less than its guide price.");
				player.getPackets().sendIComponentText(MAIN_INTERFACE, 143, bldr.toString());
			}
			break;
			case COLLECTION_INTERFACE: {
				long start = System.currentTimeMillis();
				switch (componentId) {
					default:
					case 19:
						collectItems(player, 0, slotId == 0 ? 0 : 1, packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET ? 0 : 1);
						break;
					case 23:
						collectItems(player, 1, slotId == 0 ? 0 : 1, packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET ? 0 : 1);
						break;
					case 27:
						collectItems(player, 2, slotId == 0 ? 0 : 1, packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET ? 0 : 1);
						break;
					case 32:
						collectItems(player, 3, slotId == 0 ? 0 : 1, packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET ? 0 : 1);
						break;
					case 37:
						collectItems(player, 4, slotId == 0 ? 0 : 1, packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET ? 0 : 1);
						break;
					case 42:
						collectItems(player, 5, slotId == 0 ? 0 : 1, packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET ? 0 : 1);
						break;
				}
			}
			break;
		}
		return true;
	}

	/**
	 * Collects an offer from the collection box
	 */
	private void collectItems(Player player, int offerSlot, int itemSlot, int option) {
		ExchangeOffer offer = getOfferBySlot(player, offerSlot);
		if (offer == null) { return; }
		ExchangeItemLoader loader = GsonStartup.getClass(ExchangeItemLoader.class);
		if (loader == null) { return; }
		Item item = offer.getItemsToCollect().get(itemSlot);
		if (item == null) {
			return;
		}
		int freeSlots = player.getInventory().getFreeSlots();
		if (freeSlots == 0) {
			player.sendMessage("Not enough space in your inventory.");
			return;
		}

		int newId = -1;
		boolean noted = false;
		int amount = item.getAmount();
		if (!item.getDefinitions().isStackable() && item.getAmount() > 1 && option == 0) {
			noted = true;
		}
		if (!item.getDefinitions().isStackable() && option == 1) { noted = true; }
		if (noted) {
			newId = item.getDefinitions().getCertId();
		}
		if (newId == -1) { newId = item.getId(); }

		Item received = new Item(newId, amount);

		if (!player.getInventory().getItems().hasSpaceFor(received)) {
			player.sendMessage("You don't have enough inventory space for this item.");
			return;
		}

		if (itemSlot == 0) {
			offer.setAmountReceived(0);
		} else {
			offer.setSurplus(0);
		}

		if (offer.isAborted()) {
			loader.removeOffer(offer);
			ExchangeManagement.openCollectionBox(player);
		} else {
			if (offer.getAmountProcessed() >= offer.getAmountRequested() && offer.getItemsToCollect().getUsedSlots() == 0) {
				loader.removeOffer(offer);
				ExchangeManagement.openCollectionBox(player);
			} else {
				loader.saveProgress(offer);
				ExchangeManagement.openCollectionBox(player);
			}
		}

		player.getInventory().addItem(received);
		GameLogProcessor.submitLog(new GameLog("exchange", player.getUsername(), "Collected " + received + " from the grand exchange. [offer=" + offer + "]"));
		player.getFacade().addOfferHistory(offer);
	}

	/**
	 * Collects an item from the offer's collection exchange
	 *
	 * @param player
	 * 		The player
	 * @param offer
	 * 		The offer
	 * @param itemId
	 * 		The item id
	 * @param packetId
	 * 		The packet id
	 */
	private void collectItem(Player player, ExchangeOffer offer, int itemId, int packetId, int componentId) {
		synchronized (ExchangeItemLoader.LOCK) {
			Item item = offer.getItemsToCollect().lookup(itemId);
			if (item == null) { return; }
			ExchangeItemLoader loader = GsonStartup.getClass(ExchangeItemLoader.class);
			if (loader == null) { return; }
			int freeSlots = player.getInventory().getFreeSlots();
			if (freeSlots == 0) {
				player.getPackets().sendGameMessage("Not enough space in your inventory.");
				return;
			}
			int amount = item.getAmount();
			int slot = componentId == 206 ? 1 : 2;
			int option = packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET ? 1 : 2;
			boolean toNote = false;

			if (!item.getDefinitions().isStackable() && amount > 1 && option == 1) {
				toNote = true;
			}
			if (!item.getDefinitions().isStackable() && amount == 1 && option == 2) { toNote = true; }

			int newId = toNote ? ItemDefinitions.getItemDefinitions(itemId).getCertId() : itemId;

			if (newId == -1) { newId = itemId; }

			int amountReq = offer.getAmountRequested();

			Item received = new Item(newId, amount);
			boolean deposited = false;
			if (!player.getInventory().getItems().hasSpaceFor(received)) {
				deposited = player.getBank().addItem(received.getId(), received.getAmount(), true);
				if (deposited) {
					player.sendMessage("The " + received.getName().toLowerCase() + " you were collecting has been placed in your bank.");
				} else {
					player.sendMessage("You did not have any space in your inventory or bank for this item.");
					return;
				}
			}

			if (slot == 1) {
				offer.setAmountReceived(0);
			} else {
				offer.setSurplus(0);
			}

			if (offer.isAborted()) {
				loader.removeOffer(offer);
				ExchangeManagement.sendSummary(player);
			} else {
				if (offer.getAmountProcessed() >= amountReq && offer.getItemsToCollect().getUsedSlots() == 0) {
					loader.removeOffer(offer);
					ExchangeManagement.sendSummary(player);
				} else {
					loader.saveProgress(offer);
					sendCollectInformation(player, offer.getSlot());
					if (offer.getItemsToCollect().getUsedSlots() == 0) { ExchangeManagement.sendSummary(player); }
				}
			}
			if (!deposited) {
				player.getInventory().addItem(received);
			}
			GameLogProcessor.submitLog(new GameLog("exchange", player.getUsername(), "Collected " + received + " from the grand exchange. [offer=" + offer + "]"));
			player.getFacade().addOfferHistory(offer);
		}
	}

	/**
	 * Aborts an offer for the player
	 *
	 * @param player
	 * 		The player
	 * @param offer
	 * 		The offer to abort
	 */
	private void abortOffer(Player player, ExchangeOffer offer) {
		synchronized (ExchangeItemLoader.LOCK) {
			if (offer.isProcessing() || offer.isAborted()) {
				player.sendMessage("You cannot abort this offer right now...");
				return;
			}
			if (offer.getItemsToCollect().getUsedSlots() > 0) {
				player.sendMessage("You need to collect your items before aborting the offer.");
				return;
			}
			offer.setAborted(true);
			ExchangeManagement.sendSummary(player);
			sendCollectInformation(player, offer.getSlot());

			GsonStartup.getClass(ExchangeItemLoader.class).saveProgress(offer);
			player.sendMessage("Abort request acknowledged. Please be aware that your offer may have already been completed.");
		}
	}

	/**
	 * Sends the collection box with two slots to the player, This is based on the offer in the slot provided. The
	 * offer's items to collect will display here.
	 *
	 * @see ExchangeOffer#getItemsToCollect()
	 */
	public static void sendCollectInformation(Player player, int slotId) {
		ExchangeOffer offer = getOfferBySlot(player, slotId);
		if (offer == null) {
			ItemsContainer<Item> ic = new ItemsContainer<>(2, true);
			player.getPackets().sendConfig(1112, slotId);
			player.getPackets().sendItems(523 + slotId, ic);
			return;
		}
		ItemsContainer<Item> ic = offer.getItemsToCollect();
		player.getPackets().sendConfig(1113, offer.getType().ordinal());
		player.getPackets().sendConfig(1112, slotId);
		player.getPackets().sendItems(523 + slotId, ic);

		player.getPackets().sendIComponentSettings(MAIN_INTERFACE, 206, -1, -1, 6);
		player.getPackets().sendIComponentSettings(MAIN_INTERFACE, 208, -1, -1, 6);

		// tasking it so it looks real - wont display right when this interface is shown anyways
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				player.getPackets().sendIComponentText(MAIN_INTERFACE, 143, ItemInformationLoader.getExamine(offer.getItemId()));
			}
		});
	}

	/**
	 * Gets the offer for the player in the selected slot
	 *
	 * @param player
	 * 		The player to get the offer of
	 * @param slot
	 * 		The slot of the offer
	 */
	private static ExchangeOffer getOfferBySlot(Player player, int slot) {
		long start = System.currentTimeMillis();
		for (ExchangeOffer offer : GsonStartup.getClass(ExchangeItemLoader.class).getOffersList(player.getUsername())) {
			if (offer.getOwner().equals(player.getUsername()) && offer.getSlot() == slot) { return offer; }
		}
		return null;
	}

	/**
	 * Increases the amount of the offer
	 *
	 * @param player
	 * 		The player
	 * @param offer
	 * 		The offer
	 * @param amount
	 * 		The amount
	 */
	public void increaseAmount(Player player, ExchangeOffer offer, int amount) {
		if (offer == null) {
			return;
		}
		offer.setAmountRequested(offer.getAmountRequested() + amount);
		player.getPackets().sendConfig(1110, offer.getAmountRequested());
	}

	/**
	 * Sends the screen by the type
	 *
	 * @param type
	 * 		The type of offer
	 */
	public void sendScreen(Player player, ExchangeType type) {
		resetInterfaceConfigs(player);
		if (type == ExchangeType.SELL) {
			player.getPackets().sendConfig(1113, 1);
			player.getInterfaceManager().sendInventoryInterface(SELL_INTERFACE);
			final Object[] params = new Object[] { "", "", "", "", "Offer", -1, 0, 7, 4, 93, 7012370 };
			player.getPackets().sendRunScript(149, params);
			player.getPackets().sendItems(93, player.getInventory().getItems());
			player.getPackets().sendHideIComponent(SELL_INTERFACE, 0, false);
			player.getPackets().sendIComponentSettings(SELL_INTERFACE, 18, 0, 27, 1026);
			player.getPackets().sendConfig(1112, (Integer) player.getAttributes().get("exchange_slot"));
			player.getPackets().sendHideIComponent(105, 196, true);
		} else {
			player.getPackets().sendConfig1(744, 0);
			player.getPackets().sendConfig(1112, (Integer) player.getAttributes().get("exchange_slot"));
			player.getPackets().sendConfig(1113, 0);
			player.getPackets().sendInterface(true, 752, 7, 389);
			player.getPackets().sendRunScript(570, "Grand Exchange Item Search");
		}
	}

	/**
	 * Resets interface configurations to prepare for displaying the buy screen
	 *
	 * @param player
	 * 		The player to reset it for
	 */
	private void resetInterfaceConfigs(Player player) {
		player.getPackets().sendConfig2(1109, -1);
		player.getPackets().sendConfig2(1110, 0);
		player.getPackets().sendConfig2(1111, 1);
		player.getPackets().sendConfig2(1112, -1);
		player.getPackets().sendConfig2(1113, 0);
	}

	/**
	 * Finds the slot of the button you are clicking
	 *
	 * @param componentId
	 * 		The button you are clicking
	 */
	private int getSlot(int componentId) {
		switch (componentId) {
			case 31:
			case 32:
			case 19:
				return 0;
			case 47:
			case 35:
			case 48:
				return 1;
			case 63:
			case 51:
			case 64:
				return 2;
			case 82:
			case 83:
			case 70:
				return 3;
			case 101:
			case 102:
			case 89:
				return 4;
			case 120:
			case 108:
			case 121:
				return 5;
			default:
				return -1;
		}
	}
}
