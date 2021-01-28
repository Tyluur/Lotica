package com.runescape.game.content.economy.exchange;

import com.runescape.Main;
import com.runescape.utility.external.gson.GsonStartup;
import com.runescape.utility.external.gson.loaders.ExchangeItemLoader;
import com.runescape.utility.external.gson.loaders.ExchangePriceLoader;
import com.runescape.workers.game.log.GameLog;
import com.runescape.workers.game.log.GameLogProcessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 5/5/16
 */
public class ExchangeWorker implements Runnable {

	/**
	 * The instance of this class
	 */
	private static ExchangeWorker singleton;

	/**
	 * The instance of the loader
	 */
	private static ExchangeItemLoader loader;

	/**
	 * The instance of the price loader
	 */
	private static ExchangePriceLoader priceLoader;

	/**
	 * The queue of offers we are processing
	 */
	private final Queue<ExchangeOffer> queuedProcess = new ConcurrentLinkedQueue<>();

	/**
	 * The queue of offers waiting to be processed
	 */
	private final Queue<ExchangeOffer> waitingToBeAdded = new ConcurrentLinkedQueue<>();

	private boolean setLoaders = false;

	@Override
	public void run() {
		while (true) {
			try {
				if (Main.get().hasStarted()) {
					processQueue();
				}
				Thread.sleep(2500);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Sets the {@link #loader} and {@link #priceLoader} once
	 */
	private void setGsons() {
		if (loader == null) {
			loader = GsonStartup.getClass(ExchangeItemLoader.class);
		}
		if (priceLoader == null) {
			priceLoader = GsonStartup.getClass(ExchangePriceLoader.class);
		}
		if (loader != null && priceLoader != null) { setLoaders = true; }
	}

	/**
	 * Processes the queues
	 */
	public void processQueue() {
		try {
			if (!setLoaders) {setGsons(); }
			processWaitingQueue();
			while (!queuedProcess.isEmpty()) {
				ExchangeOffer offer = queuedProcess.poll();
				List<ExchangeOffer> offers = getBarteringOffers(offer, loader);

				// the list of offer that are a barter to the current offer
				List<ExchangeOffer> sortedBarters = getOffersByType(offers, offer.getType().equals(ExchangeType.BUY) ? ExchangeType.SELL : ExchangeType.BUY);
				if (offer.getType().equals(ExchangeType.BUY)) {
					final int buyPrice = offer.getPrice();
					final int buy = offer.getAmountRequested() - offer.getAmountReceived();
					for (ExchangeOffer sellOffer : sortedBarters) {
						if (offer.isFinished() || offer.isAborted()) {
							continue;
						}
						int sellPrice = sellOffer.getPrice();
						if (sellPrice > offer.getPrice()) {
							continue;
						}
						int difference = buyPrice - sellPrice;
						int sellAmount = sellOffer.getAmountRequested() - sellOffer.getAmountProcessed();

						int newAmount;
						if (buy > sellAmount) {
							newAmount = sellAmount;
						} else {
							newAmount = buy;
						}

						if ((offer.getAmountReceived() + newAmount) > offer.getAmountRequested()) {
							newAmount = offer.getAmountRequested() - offer.getAmountReceived();
						}

						if (newAmount == -1) { continue; }

						if (difference > 0) {
							offer.setSurplus(offer.getSurplus() + (difference * newAmount));
						}

						priceLoader.addPrice(offer.getItemId(), sellPrice);
						priceLoader.addPrice(offer.getItemId(), buyPrice);

						offer.setAmountProcessed(offer.getAmountProcessed() + newAmount);
						offer.setAmountReceived(offer.getAmountReceived() + newAmount);

						GameLogProcessor.submitLog(new GameLog("exchange", offer.getOwner(), offer + " was updated by " + sellOffer + " [newAmount=" + newAmount + "]"));

						if (!sellOffer.isUnlimited()) {
							sellOffer.setAmountProcessed(sellOffer.getAmountProcessed() + newAmount);
							sellOffer.setAmountReceived(sellOffer.getAmountReceived() + newAmount);
						}

						loader.removeOffer(offer);
						loader.addOffer(offer);

						if (!sellOffer.isUnlimited()) {
							loader.removeOffer(sellOffer);
							loader.addOffer(sellOffer);
						}

						if (sellOffer.isUnlimited()) { sellOffer.notifyUpdated(); }

						offer.notifyUpdated();
					}
				} else {
					int infiniteQuantityPrice = ExchangePriceLoader.getInfiniteQuantityPrice(offer.getItemId());
					int fivePercentLess = (int) Math.ceil(infiniteQuantityPrice * 0.95);

					boolean updated = false;
					if (offer.getPrice() <= fivePercentLess) {
						int amountNeeded = offer.getAmountRequested() - offer.getAmountReceived();

						offer.setAmountProcessed(offer.getAmountProcessed() + amountNeeded);
						offer.setAmountReceived(amountNeeded);
						priceLoader.addPrice(offer.getItemId(), infiniteQuantityPrice);
						updated = true;
					}
					if (!updated && !sortedBarters.isEmpty()) {
						sortedBarters.forEach(this::queue);
					}
					if (updated) {
						offer.notifyUpdated();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Processes the waiting queue by removing the offers waiting to be queued and adding them to the {@link
	 * #queuedProcess} queue
	 */
	private void processWaitingQueue() {
		try {
			ExchangeOffer o;
			while ((o = waitingToBeAdded.poll()) != null) {
				queuedProcess.add(o);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets a list of offers which are relevant to the parameterized offer
	 *
	 * @param bartered
	 * 		The offer
	 * @param itemLoader
	 * 		The item loader
	 */
	private List<ExchangeOffer> getBarteringOffers(ExchangeOffer bartered, ExchangeItemLoader itemLoader) {
		List<ExchangeOffer> list = new ArrayList<>(itemLoader.getExchangeOffers());
		return list.stream().filter(offer -> offer.getType() != bartered.getType() && offer.getItemId() == bartered.getItemId()).collect(Collectors.toList());
	}

	/**
	 * Gets a list of offers by the type and sorts them according to price
	 *
	 * @param list
	 * 		The list of offers
	 * @param type
	 * 		The type
	 */
	private List<ExchangeOffer> getOffersByType(List<ExchangeOffer> list, ExchangeType type) {
		List<ExchangeOffer> offers = list.stream().filter(offer -> !offer.isFinished() && !offer.isAborted() && offer.getType() == type).collect(Collectors.toList());
		Collections.sort(offers, (o1, o2) -> Integer.compare(type == ExchangeType.SELL ? o1.getPrice() : o2.getPrice(), type == ExchangeType.SELL ? o2.getPrice() : o1.getPrice()));
		return offers;
	}

	/**
	 * @return the instance
	 */
	public static ExchangeWorker get() {
		if (singleton == null) {
			singleton = new ExchangeWorker();
		}
		return singleton;
	}

	/**
	 * Queues an offer to the {@link #waitingToBeAdded} queue
	 *
	 * @param offer
	 * 		The offer to queue
	 */
	public void queue(ExchangeOffer offer) {
		try {
			waitingToBeAdded.add(offer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Starts the worker thread
	 */
	public void loadUp() {
		Thread thread = new Thread(this);
		thread.setName("ExchangeWorker");
		thread.start();
	}
}
