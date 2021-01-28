package com.runescape.game.content.global.commands.owner;

import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.game.GameConstants;
import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.ChatColors;
import com.runescape.utility.Utils;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 18, 2015
 */
public class Itemn extends CommandSkeleton<String[]> {

	@Override
	public String[] getIdentifiers() {
		return new String[] { "itemn" };
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		String keyWords = getCompleted(cmd, 1);

		boolean filter = keyWords.contains("--filter:");
		String[] filterData = keyWords.split("--filter:");

		String filterText = filterData[filterData.length - 1].trim();
		String[] textToFilter = filterText.split(",");

		if (filter) {
			System.out.println("keywords before:\t" + keyWords);
			keyWords = keyWords.substring(0, keyWords.indexOf("--filter:")).trim();
			System.out.println("keywords after:\t" + keyWords);
		}

		keyWords = keyWords.replaceAll("/?", ".*").toLowerCase();

		Pattern pattern = Pattern.compile(keyWords);

		StringBuilder bldr = new StringBuilder();


		k:
		for (int itemId = 0; itemId < Utils.getItemDefinitionsSize(); itemId++) {
			ItemDefinitions def = ItemDefinitions.forId(itemId);
			if (def == null || def.isNoted()) {
				continue;
			}
			String name = def.getName();
			if (filter) {
				for (String text : textToFilter) {
					if (name.contains(text)) {
						continue k;
					}
				}
			}
			if (pattern.matcher(name.toLowerCase()).find()) {
				bldr.append(itemId).append(", ");
				player.getPackets().sendMessage(99, "[<col=FF0000>ITEM</col>] <col=" + ChatColors.LIGHT_BLUE + ">" + def.getName() + "</col> " + Arrays.toString(def.inventoryOptions) + " - ID: " + itemId + "", player);
			}
		}
		if (GameConstants.DEBUG) {
			System.out.println(keyWords + ":\t" + bldr.toString());
		}
	}

}
