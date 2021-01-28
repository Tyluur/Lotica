package com.runescape.utility.applications.console.script;

import com.runescape.cache.Cache;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.applications.console.GameScript;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/5/2015
 */
public class CustomScript extends GameScript {

	@Override
	protected String getFileLocation() {
		return "./info/out/skill_reset";
	}

	private static final String[] names = new String[] { "sin", "saya", "rs_rob", "roli", "path", "nex_to_k0", "le_random" };

	public static void main(String[] args) {
		try {
			Cache.init();
			for (String name : names) {
				File acc = getFile(name);
				Player player;
				player = getPlayer(acc);
				if (player == null) {
					continue;
				}
				player.getFacade().setDonatorExpirationTime(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30));
				savePlayer(player, acc, true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
