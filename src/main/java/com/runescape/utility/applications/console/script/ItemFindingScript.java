package com.runescape.utility.applications.console.script;

import com.runescape.cache.Cache;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.utility.Utils;
import com.runescape.utility.applications.console.GameScript;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since Feb 13, 2014
 */
public class ItemFindingScript extends GameScript {

	/**
	 * If the items the player has contains these strings, it will be added to the {@link #DETAILS} list
	 */
	private static final String[] ITEMS_TO_FIND = { "coins" };

	private static final List<String> DETAILS = new ArrayList<>();

	public static void main(String... args) throws IOException {
		Cache.init();
		for (File acc : getAccounts()) {
			try {
				Player player = getPlayer(acc);
				if (player != null) {
					for (Item item : player.getInventory().getItems().toArray()) {
						if (item == null) {
							continue;
						}
						String name = item.getName().toLowerCase();
						for (String contained : ITEMS_TO_FIND) {
							if (name.contains(contained.toLowerCase())) {
								DETAILS.add(acc.getName() + ":\t[INVENTORY]" + item.getAmount() + "x " + item.getName() + ", id=" + item.getId() + " ");
							}
						}
					}
					for (Item item : player.getEquipment().getItems().toArray()) {
						if (item == null) {
							continue;
						}
						String name = item.getName().toLowerCase();
						for (String contained : ITEMS_TO_FIND) {
							if (name.contains(contained.toLowerCase())) {
								DETAILS.add(acc.getName() + ":\t[EQUIPMENT]" + item.getAmount() + "x " + item.getName() + ", id=" + item.getId() + " ");
							}
						}
					}
					for (Item item : player.getBank().getContainerCopy()) {
						if (item == null) {
							continue;
						}
						String name = item.getName().toLowerCase();
						for (String contained : ITEMS_TO_FIND) {
							if (name.contains(contained.toLowerCase())) {
								DETAILS.add(acc.getName() + ":\t[BANK]" + item.getAmount() + "x " + item.getName() + ", id=" + item.getId() + " ");
							}
						}
					}
					if (player.getFamiliar() != null) {
						if (player.getFamiliar().getBob() != null) {
							for (Item item : player.getFamiliar().getBob().getBeastItems().toArray()) {
								if (item == null) {
									continue;
								}
								String name = item.getName().toLowerCase();
								for (String contained : ITEMS_TO_FIND) {
									if (name.contains(contained.toLowerCase())) {
										DETAILS.add(acc.getName() + ":\t[BOB]" + item.getAmount() + "x " + item.getName() + ", id=" + item.getId() + " ");
									}
								}
							}
						}
					}
				}
			} catch (Exception e) {
				System.err.println(acc.getAbsolutePath());
				e.printStackTrace();
			}
		}
		Utils.clearFile("./info/out/items_found.txt");
		DETAILS.forEach(detail -> {
			try(BufferedWriter writer = new BufferedWriter(new FileWriter("./info/out/items_found.txt", true))) {
				writer.write(detail);
				writer.newLine();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

}
