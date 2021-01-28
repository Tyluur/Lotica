package com.runescape.utility.applications.console.script;

import com.runescape.cache.Cache;
import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.game.content.economy.exchange.ExchangeOffer;
import com.runescape.utility.Utils;
import com.runescape.utility.applications.console.GameScript;
import com.runescape.utility.external.gson.GsonStartup;
import com.runescape.utility.external.gson.loaders.ExchangeItemLoader;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 5/5/2016
 */
public class GrandExchangeChecker extends GameScript {

	private static final Map<String, Long> OFFER_VALUES = new HashMap<>();

	@Override
	protected String getFileLocation() {
		return "./info/out/exchangevalues.txt";
	}

	public static void main(String[] args) throws IOException {
		Cache.init();
		GsonStartup.loadAll();

		ExchangeItemLoader loader = GsonStartup.getClass(ExchangeItemLoader.class);

		for (ExchangeOffer offer : loader.getExchangeOffers()) {
			if (offer == null) { continue; }
			Long wealth = OFFER_VALUES.get(offer.getOwner());
			if (wealth == null) { wealth = 0L; }
			switch (offer.getType()) {
				case BUY:
					if (offer.getPrice() == -1) {
						System.out.println(offer);
					} else {
						wealth += offer.getPrice() * offer.getAmountRequested();
					}
					break;
				case SELL:
					wealth += (long) (offer.getAmountRequested() * getValue(offer));
					break;
			}
			OFFER_VALUES.put(offer.getOwner(), wealth);
		}
		Map<String, Long> sorted = sortByValue(OFFER_VALUES);
		for (Entry<String, Long> entry : sorted.entrySet()) {
			new GrandExchangeChecker().write(entry.getKey() + " has " + Utils.format(entry.getValue()) + " in the grand exchange.");
		}
		System.out.println("Done!");
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		Map<K, V> result = new LinkedHashMap<>();
		Stream<Entry<K, V>> st = map.entrySet().stream();

		st.sorted(Map.Entry.comparingByValue(new Comparator<V>() {
			@Override
			public int compare(V o1, V o2) {
				return o2.compareTo(o1);
			}
		})).forEachOrdered(e -> result.put(e.getKey(), e.getValue()));

		return result;
	}

	private static int getValue(ExchangeOffer item) {
		return ItemDefinitions.getItemDefinitions(item.getItemId()).getExchangePrice();
	}

}
