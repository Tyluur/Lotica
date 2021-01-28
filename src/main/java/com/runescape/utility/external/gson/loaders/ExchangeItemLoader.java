package com.runescape.utility.external.gson.loaders;

import com.google.gson.reflect.TypeToken;
import com.runescape.game.GameConstants;
import com.runescape.game.content.economy.exchange.ExchangeOffer;
import com.runescape.game.content.economy.exchange.ExchangeType;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;
import com.runescape.utility.external.gson.GsonCollections;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.stream.Collectors;

/**
 * @author Lazarus <lazarus.rs.king@gmail.com>
 * @since Jun 22, 2014
 */
public class ExchangeItemLoader extends GsonCollections<ExchangeOffer> {

	/**
	 * The object with which to synchronize
	 */
	public static final Object LOCK = new Object();

	/**
	 * The list of grand exchange offers
	 */
	private List<ExchangeOffer> exchangeOffers = Collections.synchronizedList(new ArrayList<>());

	/**
	 * The list of names of offers which have unlimited stock
	 */
	private List<String> unlimitedNames;

	/**
	 * If the unlimited data has been loaded, there is a lot so it is only loaded once.
	 */
	private boolean loadedUnlimited;

	/**
	 * Gets the best offer from the list
	 *
	 * @param type
	 * 		The type of item to filter for
	 * @param itemId
	 * 		The id of the item you're buying
	 */
	public ExchangeOffer getBestOffer(ExchangeType type, int itemId) {
		synchronized (LOCK) {
			List<ExchangeOffer> sorted = new ArrayList<>();
			for (ExchangeOffer offer : getExchangeOffers()) {
				if (offer.isAborted() || offer.isFinished()) { continue; }
				if (offer.getType() == type && offer.getItemId() == itemId) {
					sorted.add(offer);
				}
			}
			Collections.sort(sorted, (arg0, arg1) -> Integer.compare(arg0.getPrice(), arg1.getPrice()));
//			Collections.sort(sorted, (arg0, arg1) -> Boolean.compare(arg0.isUnlimited(), arg1.isUnlimited()));
			if (sorted.size() > 0) { return sorted.get(0); } else { return null; }
		}
	}

	/**
	 * @return the exchangeOffers
	 */
	public List<ExchangeOffer> getExchangeOffers() {
		return exchangeOffers;
	}

	/**
	 * Sends the player a login notification of their exchange items
	 *
	 * @param player
	 * 		The player
	 */
	public void sendLogin(Player player) {
		List<ExchangeOffer> offersList = getOffersList(player.getUsername());
		if (offersList.size() == 0) {
			return;
		}
		int complete = 0;
		for (ExchangeOffer offer : offersList) {
			if (offer.getItemsToCollect().getUsedSlots() > 0) { complete++; }
		}
		if (complete > 0) {
			player.sendMessage("You have " + complete + " item" + (complete == 1 ? "" : "s") + " from the Grand Exchange waiting in your collection box.");
		}
	}

	/**
	 * Gets a list of the offers owned by the owner
	 *
	 * @param owner
	 * 		The owner we're getting the offers of
	 */
	public List<ExchangeOffer> getOffersList(String owner) {
		synchronized (LOCK) {
			return getExchangeOffers().stream().filter(offer -> offer.getOwner().equalsIgnoreCase(owner)).collect(Collectors.toList());
		}
	}

	/**
	 * Removes an offer from the {@link #exchangeOffers} list
	 *
	 * @param offer
	 * 		The offer to remove
	 */
	public void removeOffer(ExchangeOffer offer) {
		synchronized (LOCK) {
			exchangeOffers.remove(offer);
//			save(getExchangeOffers());
//			initialize();
		}
	}

	/**
	 * Saves the data to the file
	 *
	 * @param data
	 * 		The list to save
	 */
	public void save(List<ExchangeOffer> data) {
		synchronized (LOCK) {
			/** So we don't save unlimited offers to the list */
			List<ExchangeOffer> newList = data.stream().filter(p -> !p.isUnlimited()).collect(Collectors.toList());
			try {
				File file = new File(getFileLocation());
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}
				FileWriter fw = new FileWriter(getFileLocation());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(gson.toJson(newList));
				bw.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void initialize() {
		synchronized (LOCK) {
			if (unlimitedNames == null) {
				loadUnlimitedNames();
			}
			clearData();
			for (ExchangeOffer offer : generateList()) {
				getExchangeOffers().add(offer);
			}
			// System.out.println("Registered " + getExchangeOffers().size() +
			// " exchange offers from " + getFileLocation() + " in " +
			// (System.currentTimeMillis() - ms) + " ms");
		}
	}

	public void setUnlimitedPrices() {
		if (!loadedUnlimited) {
			for (String name : unlimitedNames) {
				int item = Integer.parseInt(name);
				ExchangeOffer offer = new ExchangeOffer("", item, ExchangeType.SELL, -1, Integer.MAX_VALUE, ExchangePriceLoader.getInfiniteQuantityPrice(item), true);
				getExchangeOffers().add(offer);
			}
			loadedUnlimited = true;
		}
	}

	public void loadUnlimitedNames() {
		try {
			unlimitedNames = Files.readAllLines(new File("./data/resource/items/exchange/unlimited.txt").toPath(), Charset.defaultCharset());
		} catch (IOException e) {
			e.printStackTrace();
		}
		unlimitedNames = unlimitedNames.stream().filter(line -> !line.startsWith("//")).collect(Collectors.toList());
		List<String> namesList = new ArrayList<>();
		for (String line : unlimitedNames) {
			String[] split = line.split(": ");
			namesList.add(split[1]);
		}
		unlimitedNames = namesList;
	}

	public boolean isUnlimited(Integer id) {
		for (String name : unlimitedNames) {
			if (name.equalsIgnoreCase(String.valueOf(id))) {
				return true;
			}
		}
		return false;
	}

	private void clearData() {
		// clearing all except those of unlimited quantity
		exchangeOffers = exchangeOffers.stream().filter(p -> p.getOwner().equals("")).collect(Collectors.toList());
	}

	@Override
	public String getFileLocation() {
		return GameConstants.HOSTED ? GameConstants.FILES_PATH + "players/exchange/offers.json" : GameConstants.FILES_PATH + "/resource/items/exchange/offers.json";
	}

	public List<ExchangeOffer> loadList() {
		return gson.fromJson(Utils.getText(getFileLocation()), new TypeToken<List<ExchangeOffer>>() {}.getType());
	}

	/**
	 * Adds an offer to the {@link #exchangeOffers} list
	 *
	 * @param offer
	 * 		The offer to add
	 */
	public void addOffer(ExchangeOffer offer) {
		synchronized (LOCK) {
			exchangeOffers.add(offer);
//			save(getExchangeOffers());
//			initialize();
		}
	}

	public void save() {
		save(getExchangeOffers());
	}

	/**
	 * Saves the progress of an offer by removing the previous one from the list and adding the new one
	 *
	 * @param offer
	 * 		The offer to save the progress of
	 */
	public void saveProgress(ExchangeOffer offer) {
		synchronized (LOCK) {
			ListIterator<ExchangeOffer> it$ = getExchangeOffers().listIterator();
			while (it$.hasNext()) {
				ExchangeOffer exchangeOffer = it$.next();
				if (exchangeOffer.equals(offer)) {
					// System.out.println("Found offer that equals it.");
					it$.remove();
					break;
				}
			}
			getExchangeOffers().add(offer);
//			save(getExchangeOffers());
		}
	}
}
