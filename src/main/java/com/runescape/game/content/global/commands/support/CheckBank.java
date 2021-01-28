package com.runescape.game.content.global.commands.support;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;
import com.runescape.utility.world.player.PlayerSaving;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/4/2015
 */
public class CheckBank extends CommandSkeleton<String> {
	
	@Override
	public String getIdentifiers() {
		return "checkbank";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		String name = getCompleted(cmd, 1);
		Player target = World.getPlayerByDisplayName(name);
		boolean offline = false;
		if (target == null) {
			target = PlayerSaving.fromFile(Utils.formatPlayerNameForProtocol(name));
			offline = true;
		}
		if (target == null) {
			player.sendMessage("No such player!");
			return;
		}
		player.putAttribute("viewing_banks", true);
		player.getInterfaceManager().sendInterface(762);
		player.getInterfaceManager().sendInventoryInterface(763);
		player.getPackets().sendItems(95, target.getBank().getContainerCopy());
		player.getPackets().sendConfigByFile(4893, 0);
		player.getPackets().sendIComponentSettings(762, 95, 0, 516, 2622718);
		player.getPackets().sendIComponentSettings(763, 0, 0, 27, 2425982);
		player.setCloseInterfacesEvent(() -> player.removeAttribute("viewing_banks"));
		if (offline) {
			player.sendMessage(name + " was offline so we loaded their character file.");
		}
	}
}
