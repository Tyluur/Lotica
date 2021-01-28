package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 1/3/2016
 */
public class RemoveAllOfOtherItem extends CommandSkeleton<String> {

	@Override
	public String getIdentifiers() {
		return "removeallof";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		String name = cmd[1];
		Player target = World.getPlayer(name);
		if (target == null) {
			player.sendMessage("Couldn't find " + name + " online.");
			return;
		}
		int itemId = Integer.parseInt(cmd[2]);
		target.getBank().deleteItem(itemId, true);
		target.getInventory().deleteItem(itemId, Integer.MAX_VALUE);
		target.getEquipment().deleteItem(itemId, Integer.MAX_VALUE);
		if (target.getFamiliar() != null) {
			if (target.getFamiliar().getBob() != null && target.getFamiliar().getBob().getBeastItems() != null) {
				target.getFamiliar().getBob().getBeastItems().removeAll(new Item(itemId, Integer.MAX_VALUE));
			}
		}
	}
}
