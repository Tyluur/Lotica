package com.runescape.utility.applications.console.script;

import com.runescape.cache.Cache;
import com.runescape.game.GameConstants;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.utility.applications.console.GameScript;

import java.io.File;
import java.io.IOException;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 1/15/2016
 */
public class EconomySpawnConvert extends GameScript {

	public static void main(String[] args) throws IOException {
		Cache.init();
		for (File acc : getAccounts()) {
			try {
				Player player = getPlayer(acc);
				if (player != null) {
					player.getPresetManager().clearPresets();
					for (int i = 0; i < Skills.SKILL_NAME.length; i++) {
						if (i <= 6 || i == Skills.DUNGEONEERING) {
							player.getSkills().resetSkillNoRefresh(i);
							if (i == Skills.HITPOINTS) {
								player.getSkills().setXp(i, 1184, false);
								player.getSkills().setLevel(i, 10, false);
								player.setHitpoints(player.getSkills().getLevel(i) * 10);
							}
						}
					}
					player.reset(true, true, true, true);
					player.setLocation(GameConstants.START_PLAYER_LOCATION);
					player.getControllerManager().forceSetLastController(GameConstants.START_CONTROLER);
					savePlayer(player, acc, true);
				}
			} catch (Exception e) {
				System.out.println("error on: " + acc);
				e.printStackTrace();
			}
		}
	}
}
