package com.runescape.utility.external.gson.loaders;

import com.google.gson.reflect.TypeToken;
import com.runescape.game.GameConstants;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleNPCMessage;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.utility.SerializableFilesManager;
import com.runescape.utility.Utils;
import com.runescape.utility.external.gson.GsonCollections;
import com.runescape.utility.external.gson.GsonStartup;
import com.runescape.utility.external.gson.resource.LentItem;
import com.runescape.utility.world.player.PlayerSaving;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/20/2015
 */
public class LentItemsLoader extends GsonCollections<LentItem> {
	
	@Override
	public void initialize() {
		synchronized (LOCK) {
			LENT_ITEMS.clear();
			LENT_ITEMS.addAll(generateList().stream().collect(Collectors.toList()));
		}
	}

	@Override
	public String getFileLocation() {
		return GameConstants.FILES_PATH + "players/loans.json";
	}

	@Override
	public List<LentItem> loadList() {
		return gson.fromJson(Utils.getText(getFileLocation()), new TypeToken<List<LentItem>>() {}.getType());
	}

	/**
	 * Adds a lent item to the list of items
	 *
	 * @param item
	 * 		The item to add
	 */
	public void addLentItem(LentItem item) {
		synchronized (LOCK) {
			LENT_ITEMS.add(item);
			save(LENT_ITEMS);
			initialize();
		}
	}

	/**
	 * Registers an item to lend to the loader
	 *
	 * @param lender
	 * 		The player lending the item
	 * @param receiver
	 * 		The player receiving the item
	 * @param item
	 * 		The item
	 * @param tillLogout
	 * 		If the item is lent till logout
	 * @param hoursToLend
	 * 		The amount of hours to lend the item for
	 */
	public static void registerItemToLend(Player lender, Player receiver, Item item, boolean tillLogout, long hoursToLend) {
		synchronized (LOCK) {
			LentItemsLoader loader = GsonStartup.getClass(LentItemsLoader.class);
			if (loader == null) {
				System.err.println("Error grabbing class LentItemsLoader...");
				return;
			}
			// constructing the lent item
			LentItem lentItem = new LentItem(lender.getUsername(), receiver.getUsername(), item.getId(), item.getDefinitions().getLendId(), hoursToLend, System.currentTimeMillis() + TimeUnit.HOURS.toMillis(hoursToLend), tillLogout);
			// adding the lent item
			loader.addLentItem(lentItem);
			// giving the lent item to the receiver
			receiver.getInventory().addItem(item.getDefinitions().getLendId(), 1);

			System.out.println("Registered item to lend: " + item + "\tlender=" + lender + ",\treceiver=" + receiver);
		}
	}

	/**
	 * Takes the item from the player
	 *
	 * @param player
	 * 		The player
	 * @param item
	 * 		The item to take
	 * @param playerOffline
	 * 		If the item is being taken from an offline player
	 * @return If the item was taken
	 */
	private boolean takeItemFromPlayer(Player player, LentItem item, boolean playerOffline) {
		int itemId = item.getLentItemId();
		// if the player is online
		if (!playerOffline) {
			if (player.getInventory().contains(itemId)) {
				System.out.println("removed item from inv");
				player.getInventory().deleteItem(itemId, 1);
				return true;
			} else if (player.getEquipment().getItems().lookup(itemId) != null) {
				System.out.println("removed item from equipment");
				player.getEquipment().deleteItem(itemId, 1);
				player.getAppearence().generateAppearenceData();
				return true;
			} else if (player.getBank().deleteItem(itemId, true)) {
				System.out.println("removed item from bank");
				return true;
			}
		} else {
			if (player.getInventory().contains(itemId)) {
				System.out.println("Removed from inventory");
				player.getInventory().forceRemove(itemId, Integer.MAX_VALUE);
				return true;
			} else if (player.getEquipment().getItems().lookup(itemId) != null) {
				System.out.println("removed from equipment");
				player.getEquipment().forceRemove(itemId, Integer.MAX_VALUE);
				return true;
			} else if (player.getBank().deleteItem(itemId, false)) {
				System.out.println("Removed from bank.");
				return true;
			}
		}
		System.err.println("couldnt find container to take item from for user: " + player + ", offline?" + playerOffline);
		return false;
	}

	/**
	 * Handles what to do on logout
	 *
	 * @param player
	 * 		The player logging out
	 */
	public static void handlePlayerLogout(Player player) {
		synchronized (LOCK) {
			LentItemsLoader loader = GsonStartup.getClass(LentItemsLoader.class);
			if (loader == null) {
				System.err.println("Error grabbing class LentItemsLoader...");
				return;
			}
			Iterator<LentItem> it$ = LENT_ITEMS.iterator();
			boolean updated = false;
			while (it$.hasNext()) {
				LentItem lentItem = it$.next();
				// items that are available for collection are skipped
				if (lentItem.isAvailableForCollection()) {
					continue;
				}
				// checking that the item is lent until logout
				if (!lentItem.isLentTillLogout()) {
					continue;
				}
				System.out.println("was till logout");
				// verifying the names
				if (lentItem.getLenderName().equalsIgnoreCase(player.getUsername()) || lentItem.getReceiverName().equalsIgnoreCase(player.getUsername())) {
					Player playerToTakeFrom = World.getPlayer(lentItem.getReceiverName());
					if (playerToTakeFrom == null) {
						System.out.println("Couldn't find player by name...");
						continue;
					}
					System.out.println("found name");
					// verifying that the item was taken from the player
					if (loader.takeItemFromPlayer(playerToTakeFrom, lentItem, false)) {
						System.out.println("item was taken");
						// the item is now available for collection
						lentItem.setAvailableForCollection(true);
						updated = true;
					}
				}
			}
			if (updated) {
				loader.save(LENT_ITEMS);
				loader.initialize();
			}
		}
	}

	/**
	 * This method is called every time the {@code LentItemTick} pulses. All lent items are looped through, ignoring
	 * those that are {@link LentItem#isAvailableForCollection()}. We then check to see if the item should be refunded
	 * to the original lender. If it has, we take the item from the player with the item, and flag the item as ready for
	 * claim for the lender.
	 */
	public static void pulse() {
		synchronized (LOCK) {
			LentItemsLoader loader = GsonStartup.getClass(LentItemsLoader.class);
			if (loader == null) {
				System.err.println("Error grabbing class LentItemsLoader...");
				return;
			}
			Iterator<LentItem> it$ = LENT_ITEMS.iterator();
			boolean updated = false;
			while (it$.hasNext()) {
				LentItem lentItem = it$.next();
				// items that are available for collection are skipped
				if (lentItem.isAvailableForCollection()) {
					continue;
				}
				// the item is lent until logout so we will skip it in the pulse
				if (lentItem.isLentTillLogout()) {
					continue;
				}
				//System.out.println(new Date(lentItem.getLentUntil()));
				// the time to lend for has expired....
				if (System.currentTimeMillis() >= lentItem.getLentUntil()) {
					System.out.println("item time was expired");
					String playerWithItemName = lentItem.getReceiverName();
					// if the player is online
					if (World.containsPlayer(playerWithItemName)) {
						// player was online
						Player playerWithItem = World.getPlayer(playerWithItemName);
						if (playerWithItem == null) {
							System.out.println("Couldn't find play with name: " + playerWithItemName);
							continue;
						}
						// if the item was taken from the player
						if (loader.takeItemFromPlayer(playerWithItem, lentItem, false)) {
							System.out.println("item was taken");
							// the item is now available for collection
							lentItem.setAvailableForCollection(true);
							updated = true;
						}
					} else {
						// player is offline... remove from their containers
						Player player = PlayerSaving.fromFile(playerWithItemName);
						if (player == null) {
							System.out.println("Couldn't find file with name: " + playerWithItemName);
							continue;
						}
						player.setUsername(playerWithItemName);
						// making sure the item was taken from the file
						if (loader.takeItemFromPlayer(player, lentItem, true)) {
							SerializableFilesManager.savePlayer(player);
							System.out.println("item was taken");
							// the item is now available for collection
							lentItem.setAvailableForCollection(true);
							updated = true;
						}
					}
				}
			}
			if (updated) {
				loader.save(LENT_ITEMS);
				loader.initialize();
			}
		}
	}

	/**
	 * This method handles the deletion of a lent item. We loop through all lent items, and see if the item is an item
	 * that the player has from a lender. If it is, we mark it for collection for the lender.
	 *
	 * @param player
	 * 		The player
	 * @param item
	 * 		The item
	 */
	public static void deleteLentItem(Player player, Item item) {
		synchronized (LOCK) {
			LentItemsLoader loader = GsonStartup.getClass(LentItemsLoader.class);
			if (loader == null) {
				System.err.println("Error grabbing class LentItemsLoader...");
				return;
			}
			Iterator<LentItem> it$ = LENT_ITEMS.iterator();
			boolean updated = false;
			while (it$.hasNext()) {
				LentItem lentItem = it$.next();
				// items that are available for collection are skipped
				if (lentItem.isAvailableForCollection()) {
					continue;
				}
				// making sure ids match.
				if (lentItem.getLentItemId() != item.getId()) {
					continue;
				}
				// making sure the name matches
				if (!lentItem.getReceiverName().equalsIgnoreCase(player.getUsername())) {
					continue;
				}
				lentItem.setAvailableForCollection(true);
				updated = true;
			}
			if (updated) {
				loader.save(LENT_ITEMS);
				loader.initialize();
			}
		}
	}

	/**
	 * This method claims back all lent items
	 *
	 * @param player
	 * 		The player
	 */
	public static void claimAllLentItems(Player player) {
		synchronized (LOCK) {
			LentItemsLoader loader = GsonStartup.getClass(LentItemsLoader.class);
			if (loader == null) {
				System.err.println("Error grabbing class LentItemsLoader...");
				return;
			}
			Iterator<LentItem> it$ = LENT_ITEMS.iterator();
			int count = 0;
			while (it$.hasNext()) {
				LentItem lentItem = it$.next();
				// if the item isnt yet collectable
				if (!lentItem.isAvailableForCollection()) {
					continue;
				}
				// confirming the name
				if (lentItem.getLenderName().equalsIgnoreCase(player.getUsername())) {
					// adds the item to the player
					player.getInventory().addItemDrop(lentItem.getOriginalItemId(), 1);
					// removes the item from the list
					it$.remove();
					count++;
				}
			}
			// if the player claimed any items, we reload the list and tell the player
			if (count > 0) {
				loader.save(LENT_ITEMS);
				loader.initialize();
				player.getDialogueManager().startDialogue(SimpleNPCMessage.class, 2759, "You receive " + count + " items back from your collection box.");
			} else {
				// if the player didnt claim any items, we tell the player
				player.getDialogueManager().startDialogue(SimpleNPCMessage.class, 2759, "You have no items to claim.");
			}
		}
	}

	/**
	 * This method gets the lent item instance of an item.
	 *
	 * @param player
	 * 		The player
	 * @param item
	 * 		The item
	 */
	public static LentItem getLentItem(Player player, Item item) {
		synchronized (LOCK) {
			LentItemsLoader loader = GsonStartup.getClass(LentItemsLoader.class);
			if (loader == null) {
				System.err.println("Error grabbing class LentItemsLoader...");
				return null;
			}
			for (LentItem lentItem : LENT_ITEMS) {
				if (lentItem.isAvailableForCollection()) {
					continue;
				}
				// verifying name
				if (!lentItem.getReceiverName().equalsIgnoreCase(player.getUsername())) {
					continue;
				}
				// verifying item id
				if (lentItem.getLentItemId() != item.getId()) {
					continue;
				}
				return lentItem;
			}
			return null;
		}
	}

	/**
	 * This method loops through all lent items and checks to see if the receiver is the player. This is used to check
	 * that the player only has 1 lent item at a time.
	 *
	 * @param player
	 * 		The player
	 */
	public static boolean hasLentItem(Player player) {
		synchronized (LOCK) {
			LentItemsLoader loader = GsonStartup.getClass(LentItemsLoader.class);
			if (loader == null) {
				System.err.println("Error grabbing class LentItemsLoader...");
				return false;
			}
			for (LentItem lentItem : LENT_ITEMS) {
				// items that are available for collection are skipped
				if (lentItem.isAvailableForCollection()) {
					continue;
				}
				if (lentItem.getReceiverName().equalsIgnoreCase(player.getUsername())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * The object all lent item operations are synchronized through.
	 */
	public static final Object LOCK = new Object();

	/**
	 * The list of lent items
	 */
	private static final List<LentItem> LENT_ITEMS = Collections.synchronizedList(new ArrayList<>());
}
