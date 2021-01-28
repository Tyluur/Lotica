package com.runescape.game.content.global.commands.support;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.event.interaction.button.Scrollable;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.utility.Utils;
import com.runescape.utility.world.player.PlayerSaving;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/4/2015
 */
public class CheckEquipment extends CommandSkeleton<String[]> {
	
	@Override
	public String[] getIdentifiers() {
		return new String[] { "checkequipment", "checke" };
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		String name = getCompleted(cmd, 1).replaceAll(" ", "_");
		if (World.containsPlayer(name)) {
			Player target = World.getPlayer(name);
			boolean offline = false;
			if (target == null) {
				target = PlayerSaving.fromFile(Utils.formatPlayerNameForProtocol(name));
				offline = true;
			}
			if (target == null) {
				player.sendMessage(name + " is an invalid name for a player.");
				return;
			}
			List<String> messages = new ArrayList<>();
			messages.add(target.getUsername() + "'s equipment has: ");
			for (Item item : target.getEquipment().getItems().toArray()) {
				if (item == null)
					continue;
				messages.add(Utils.format(item.getAmount()) + "x " + item.getName() + ". [" + item.getId() + "]");
			}
			Scrollable.sendQuestScroll(player, name + "'s equipment" + (offline ? "OFFLINE" : ""), messages.toArray(new String[messages.size()]));

		} else {
			player.sendMessage(name + " is an invalid name for a player.");
		}
	}
}
