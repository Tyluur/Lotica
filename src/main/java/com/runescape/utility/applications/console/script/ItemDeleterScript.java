package com.runescape.utility.applications.console.script;

import com.runescape.cache.Cache;
import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.game.content.economy.exchange.ExchangeOffer;
import com.runescape.game.content.economy.exchange.ExchangeType;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.utility.applications.console.GameScript;
import com.runescape.utility.external.gson.GsonStartup;
import com.runescape.utility.external.gson.loaders.ExchangeItemLoader;
import com.runescape.utility.world.player.PlayerSaving;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/11/2015
 */
public class ItemDeleterScript extends GameScript {

	/**
	 * If the items the player has contains these strings, it will be removed.
	 */
	private static final String[] CONTAINED_TO_REMOVE = { "wolf bone" };

	public static void main(String[] args) throws IOException {
		Cache.init();
		GsonStartup.loadAll();
		System.out.println(PlayerSaving.FILES_LOCATION);
		for (File acc : getAccounts()) {
			try {
				Player player = getPlayer(acc);
				if (player == null) {
					continue;
				}
				boolean modified = false;
				for (Item item : player.getInventory().getItems().toArray()) {
					if (item == null) {
						continue;
					}
					String name = item.getName().toLowerCase();
					for (String contained : CONTAINED_TO_REMOVE) {
						if (name.toLowerCase().contains(contained.toLowerCase())) {
							System.out.println("Removed " + name + " from inventory");
							player.getInventory().forceRemove(item.getId(), player.getInventory().getNumerOf(item.getId()));
							modified = true;
						}
					}
				}
				for (Item item : player.getEquipment().getItems().toArray()) {
					if (item == null) {
						continue;
					}
					String name = item.getName().toLowerCase();
					for (String contained : CONTAINED_TO_REMOVE) {
						if (name.toLowerCase().contains(contained.toLowerCase())) {
							System.out.println("Removed " + name + " from equipment");
							player.getEquipment().forceRemove(item.getId(), player.getEquipment().getItems().getNumberOf(item.getId()));
							modified = true;
						}
					}
				}
				for (Item item : player.getBank().getContainerCopy()) {
					if (item == null) {
						continue;
					}
					String name = item.getName().toLowerCase();
					for (String contained : CONTAINED_TO_REMOVE) {
						if (name.toLowerCase().contains(contained.toLowerCase())) {
							System.out.println("Removed " + name + " from bank");
							player.getBank().deleteItem(item.getId(), false);
							modified = true;
						}
					}
				}
				if (modified) {
					savePlayer(player, acc);
					System.out.println(acc.getName() + " had items and was deleted");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		boolean modified = false;
		List<ExchangeOffer> offers = GsonStartup.getClass(ExchangeItemLoader.class).getExchangeOffers();
		for (Iterator<ExchangeOffer> iterator = offers.iterator(); iterator.hasNext(); ) {
			ExchangeOffer offer = iterator.next();
			String name = ItemDefinitions.getItemDefinitions(offer.getItemId()).getName();
			if (offer.getType() != ExchangeType.SELL)  {
				continue;
			}
			for (String contained : CONTAINED_TO_REMOVE) {
				if (name.toLowerCase().contains(contained.toLowerCase())) {
					System.out.println("Removed " + offer + " from " + offer.getOwner() + "'s offer");
					iterator.remove();
					modified = true;
				}
			}
		}
		if (modified) {
			GsonStartup.getClass(ExchangeItemLoader.class).save(offers);
		}
		System.exit(-1);
	}
}
