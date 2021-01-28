package com.runescape.utility.external.gson.loaders;

import com.google.gson.reflect.TypeToken;
import com.runescape.cache.Cache;
import com.runescape.cache.loaders.ClientScriptMap;
import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.game.GameConstants;
import com.runescape.utility.Utils;
import com.runescape.utility.external.gson.GsonCollections;
import com.runescape.utility.external.gson.GsonStartup;
import com.runescape.utility.external.gson.resource.ExchangeItem;
import com.runescape.workers.game.core.CoresManager;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

/**
 * @author Lazarus <lazarus.rs.king@gmail.com>
 * @since Jun 22, 2014
 */
public class ExchangePriceLoader extends GsonCollections<ExchangeItem> implements Runnable {

    /**
     * The map of the prices of the unlimited items
     */
    private static Map<Integer, Integer> unlimitedPrices = new HashMap<>();

    /**
     * The list of local items
     */
    private static List<ExchangeItem> localItems = new ArrayList<>();

    /**
     * The queued prices to write
     */
    private Queue<Integer[]> queuedPrices = new ConcurrentLinkedDeque<>();

    /**
     * The queue of items to update
     */
    private Queue<ExchangeItem> itemsToUpdate = new ConcurrentLinkedDeque<>();

    /**
     * The map of exchange items with their prices
     */
    private Map<Integer, ExchangeItem> exchangeItems = new HashMap<>();

    /**
     * If the runnable is running
     */
    private boolean taskRunning = false;

    @Override
    public void run() {
        try {
            processQueuedPrices();
            processItemsToUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Processes the queue
     */
    private void processQueuedPrices() {
        Integer[] result;
        while ((result = queuedPrices.poll()) != null) {
            try {
                int item = result[0];
                int price = result[1];

                ExchangeItem exchangeItem = exchangeItems.get(item);
                if (exchangeItem == null) {
                    System.out.println("No grand exchange item found for id: " + item + " when adding price.");
                    exchangeItem = new ExchangeItem(item);
                    exchangeItems.put(item, exchangeItem);
                }
                List<Integer> prices = exchangeItem.getPrices();
                prices.add(price);

                List<ExchangeItem> list = generateList();
                ListIterator<ExchangeItem> it$ = list.listIterator();

                while (it$.hasNext()) {
                    ExchangeItem listItem = it$.next();
                    if (listItem.getItemId() == item) {
                        it$.remove();
                        break;
                    }
                }
                list.add(exchangeItem);
                localItems = list;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Processes the items to update, these items must have had a change in {@link ExchangeItem#getLastTimePriceChanged()}
     * or {@link ExchangeItem#getGuidePrice()}, so the whole list must be updated as well
     */
    private void processItemsToUpdate() {
        ExchangeItem item;
        while ((item = itemsToUpdate.poll()) != null) {
            List<ExchangeItem> list = generateList();
            ListIterator<ExchangeItem> it$ = list.listIterator();
            while (it$.hasNext()) {
                ExchangeItem listItem = it$.next();
                if (listItem.getItemId() == item.getItemId()) {
                    it$.remove();
                    break;
                }
            }
            list.add(item);
            localItems = list;
        }
    }

    public void save() {
        save(localItems);
    }

    @Override
    public void initialize() {
        exchangeItems.clear();
        for (ExchangeItem item : generateList()) {
            exchangeItems.put(item.getItemId(), item);
        }
        if (unlimitedPrices.size() == 0) {
            try {
                List<String> list = Files.readAllLines(new File("./data/resource/items/exchange/unlimited_prices.txt").toPath(), Charset.defaultCharset());
                for (String line : list) {
                    if (line.startsWith("#") || line.trim().length() <= 0) {
                        continue;
                    }
                    String[] data = line.split(":");
                    if (!line.contains(",")) {
                        unlimitedPrices.put(Integer.parseInt(data[0]), Integer.parseInt(data[1]));
                    } else {
                        String[] digits = data[0].split(",");
                        for (String digit : digits) {
                            unlimitedPrices.put(Integer.parseInt(digit), Integer.parseInt(data[1]));
                        }
                    }
                }
                // sets prices here because we've now loaded unlimited prices
                GsonStartup.getClass(ExchangeItemLoader.class).setUnlimitedPrices();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!taskRunning) {
            CoresManager.scheduleAtFixedRate(this, 1, 1, TimeUnit.SECONDS);
            taskRunning = true;
        }
    }

    @Override
    public String getFileLocation() {
        return GameConstants.HOSTED ? GameConstants.FILES_PATH + "players/exchange/prices.json" : GameConstants.FILES_PATH + "resource/items/exchange/prices.json";
    }

    @Override
    public List<ExchangeItem> loadList() {
        return gson.fromJson(Utils.getText(getFileLocation()), new TypeToken<List<ExchangeItem>>() {
        }.getType());
    }

    public static void main(String... args) throws IOException {
        Cache.init();
        CoresManager.init();
        GsonStartup.loadAll();
        loadDefaultPrices();
        System.exit(-1);
    }

    /**
     * Loads all default prices for all items
     */
    private static void loadDefaultPrices() {
        ExchangePriceLoader loader = GsonStartup.getClass(ExchangePriceLoader.class);
        List<ExchangeItem> list = new ArrayList<>();
        for (int itemId = 0; itemId < Utils.getItemDefinitionsSize(); itemId++) {
            ItemDefinitions def = ItemDefinitions.getItemDefinitions(itemId);
            if (!def.isExchangeable() || def.isNoted() || def.isLended()) {
                continue;
            }
            ExchangeItem item = new ExchangeItem(itemId);
            item.getPrices().add(def.getExchangePrice());
            list.add(item);
        }
        loader.save(list);
        System.out.println("saved " + list.size() + " prices");
    }

    /**
     * Gets the price of an item for its infinite quantity stock
     *
     * @param itemId The item
     */
    public static int getInfiniteQuantityPrice(int itemId) {
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(itemId);
        if (defs.isNoted()) {
            itemId = defs.getCertId();
        }
        Integer mapPrice = unlimitedPrices.get(itemId);
        if (mapPrice != null) {
            return mapPrice;
        } else {
            return getSellPrice(itemId);
        }
    }

    /**
     * Gets the price of the item from the {@link #unlimitedPrices} map
     *
     * @param itemId The id of the item
     */
    public static Integer getUnlimitedPrice(int itemId) {
        return unlimitedPrices.get(itemId);
    }

    /**
     * Gets the sell price of an item
     *
     * @param itemId The item
     */
    private static int getSellPrice(int itemId) {
        int price = ClientScriptMap.getMap(1441).getIntValue(itemId);
        if (price > 0) {
            return price;
        }
        return Math.max(1, (ItemDefinitions.forId(itemId).getValue() * 30) / 50);
    }

    /**
     * Gets the economical price of an item
     *
     * @param itemId The id of the item
     */
    public static int getEconomicalPrice(int itemId) {
        ExchangePriceLoader loader = GsonStartup.getClass(ExchangePriceLoader.class);
        return loader.getGuidePrice(itemId);
    }

    /**
     * Gets the average price of an item from the grand exchange
     *
     * @param item The item
     */
    public int getGuidePrice(int item) {
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(item);
        if (defs.isNoted()) {
            item = defs.getCertId();
        }
        ExchangeItem exchangeItem = exchangeItems.get(item);
        if (exchangeItem == null) {
            return defs.getExchangePrice();
        }
        if (exchangeItem.calculateGuidePrices()) {
            itemsToUpdate.add(exchangeItem);
        }
        int guidePrice = exchangeItem.getGuidePrice();
        if (guidePrice <= 0) {
            return defs.getExchangePrice();
        } else {
            return guidePrice;
        }
    }

    /**
     * Adds the price of the item to the list
     *
     * @param item  The item id
     * @param price The price of the item
     */
    public void addPrice(int item, int price) {
        queuedPrices.add(new Integer[]{item, price});
    }

}
