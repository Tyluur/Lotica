package com.runescape.game.content.global.commands.owner;

import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.npc.Drop;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.game.world.item.ItemsContainer;
import com.runescape.utility.Utils;
import com.runescape.utility.external.gson.loaders.NPCDataLoader;
import com.runescape.utility.external.gson.resource.NPCData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/31/2016
 */
public class TestItemDrops extends CommandSkeleton<String> {

	@Override
	public String getIdentifiers() {
		return "testdrops";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		String name = cmd[1].replaceAll("_", " ");
		int kills = Integer.parseInt(cmd[2]);
		int interfaceId = 762;
		long start = System.currentTimeMillis();

		ItemsContainer<Item> items = new ItemsContainer<>(1_000, true);
		NPCData data = NPCDataLoader.getData(name);
		long value = 0;
		if (data == null) {
			player.sendMessage("No npc data for " + name);
			return;
		}

		List<Drop> drops = data.getDrops();
		for (int i = 0; i < kills; i++) {
			List<Drop> dropList = data.generateDrops(player, drops, null);
			for (Drop drop : dropList) {
				items.add(ItemDefinitions.forId(drop.getItemId()).isStackable() ? new Item(drop.getItemId(), Utils.random(drop.getMinAmount(), drop.getMaxAmount())) : new Item(drop.getItemId(), drop.getMinAmount() + Utils.getRandom(drop.getExtraAmount())));
			}
		}

		List<Item> itemList = new ArrayList<>(Arrays.asList(items.toArray()));
		itemList = itemList.stream().filter(p -> p != null).collect(Collectors.toList());
		Collections.sort(itemList, (o1, o2) -> Integer.compare(o2.getId(), o1.getId()));

		items = new ItemsContainer<>(1_000, true);
		itemList.forEach(items::add);
		for (Item item : itemList) {
			value += item.getDefinitions().getValue();
		}
		player.closeInterfaces();

		player.getPackets().sendItems(95, items);
		player.getPackets().sendIComponentText(interfaceId, 45, kills + " kills of " + name + " in " + (System.currentTimeMillis() - start) + " ms [" + Utils.format(value) + "] gp");
		player.getPackets().sendIComponentSettings(interfaceId, 93, 0, 516, 2622718);
		player.getInterfaceManager().sendInterface(interfaceId);
	}
}
