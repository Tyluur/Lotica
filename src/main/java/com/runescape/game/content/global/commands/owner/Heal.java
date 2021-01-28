package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 25, 2015
 */
public class Heal extends CommandSkeleton<String>{

	@Override
	public String getIdentifiers() {
		return "heal";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		player.getCombatDefinitions().setSpecialAttack(100);
		player.getPoison().reset();
		player.getPrayer().setPrayerpoints((int) ((player.getSkills().getLevelForXp(Skills.PRAYER) * 10) * 1.15));
		player.getPrayer().refreshPrayerPoints();
		player.heal(player.getMaxHitpoints(), (int) ((player.getSkills().getLevelForXp(Skills.HITPOINTS) * 10) * 0.05));
		player.getSkills().restoreSkills();
		player.setRunEnergy(100);
	}

}
