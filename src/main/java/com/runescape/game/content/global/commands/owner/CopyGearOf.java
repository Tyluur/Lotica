package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 1/9/2016
 */
public class CopyGearOf extends CommandSkeleton<String> {

	@Override
	public String getIdentifiers() {
		return "copy";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		Player target = World.getPlayer(cmd[1]);
		if (target == null) {
			player.sendMessage("Couldn't find player...");
			return;
		}
		System.arraycopy(target.getInventory().getItems().toArray(), 0, player.getInventory().getItems().toArray(), 0, player.getInventory().getItems().toArray().length);
		System.arraycopy(target.getEquipment().getItems().toArray(), 0, player.getEquipment().getItems().toArray(), 0, player.getEquipment().getItems().toArray().length);
		player.getInventory().refresh();
		player.getEquipment().refreshAll();
		player.getSkills().passLevels(target);

		player.getSkills().refreshAllSkills();
		player.getAppearence().generateAppearenceData();
	}
}
