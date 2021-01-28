package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 18, 2015
 */
public class SpawnItem extends CommandSkeleton<String[]> {

	@Override
	public String[] getIdentifiers() {
		return new String[] { "item", "pickup" };
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		try {
			Item item = new Item(Integer.parseInt(cmd[1]), cmd.length == 2 ? 1 : Integer.parseInt(cmd[2]));
			player.getInventory().addItem(item);
		} catch (Exception e) {
			player.getPackets().sendGameMessage("Format - ::item id amount");
		}
	}

}
