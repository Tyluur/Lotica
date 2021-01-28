package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 18, 2015
 */
public class Dbi extends CommandSkeleton<String[]> {

	@Override
	public String[] getIdentifiers() {
		return new String[] { "dbi" };
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		int interfaceId = Integer.parseInt(cmd[1]);
		int componentLength = Utils.getInterfaceDefinitionsComponentsSize(interfaceId);
		boolean sendInterface = true;
		if (cmd.length >= 3) {
			sendInterface = Boolean.parseBoolean(cmd[2]);
		}
		boolean breakGlobalLoop = false;
		int globalLoopHighest = -1;
		if (cmd.length == 4) {
			globalLoopHighest = Integer.parseInt(cmd[3]);
			breakGlobalLoop = true;
		}
		for (int i = 0; i < componentLength; i++) {
			player.getPackets().sendIComponentText(interfaceId, i, "" + i);
		}
		for (int i = 0; i <= (breakGlobalLoop ? globalLoopHighest : 354); i++) {
			player.getPackets().sendGlobalString(i, "g" + i);
		}
		if (sendInterface) {
			player.closeInterfaces();
			player.getInterfaceManager().sendInterface(interfaceId);
		}
		System.out.println("Component length: " + componentLength);
	}

}
