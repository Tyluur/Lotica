package com.runescape.utility.applications.console.script;

import com.runescape.cache.Cache;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;
import com.runescape.utility.applications.console.GameScript;
import com.runescape.utility.world.player.PlayerSaving;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 9/4/2016
 */
public class DonatorExpirationCheck extends GameScript {

	private static final List<DonatorTime> details = new ArrayList<>();

	public static void main(String[] args) throws IOException {
		long start = System.currentTimeMillis();
		Cache.init();
		System.out.println(PlayerSaving.FILES_LOCATION);
		for (File acc : getAccounts()) {
			try {
				Player player = getPlayer(acc);
				if (player == null) {
					continue;
				}
				long donatorExpirationTime = player.getFacade().getDonatorExpirationTime();
				if (donatorExpirationTime <= 0) {
					continue;
				}
				details.add(new DonatorTime(acc.getName(), donatorExpirationTime - System.currentTimeMillis()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Collections.sort(details, (o1, o2) -> Long.compare(o2.remainder, o1.remainder));
		for (int i = 0; i < 5; i++) {
			System.out.println("########################################################");
		}
		int count = 1;
		for (DonatorTime detail : details) {
			System.out.println(count + ".\t" + detail.name + " has " + Utils.convertMillisecondsToTime(detail.remainder) + " left of donator status.");
			count++;
		}
		for (int i = 0; i < 5; i++) {
			System.out.println("########################################################");
		}
		System.out.println("Calculations finished in " + Utils.format(System.currentTimeMillis() - start) + " ms.");
	}

	private static class DonatorTime {

		private final String name;

		private final long remainder;

		private DonatorTime(String name, long remainder) {
			this.name = name;
			this.remainder = remainder;
		}
	}

}
