package com.runescape.game.content.global.wilderness.activities;

import com.runescape.game.content.global.wilderness.WildernessActivity;
import com.runescape.game.interaction.controllers.impl.Wilderness;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.ChatColors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Dec 31, 2014
 */
public class FCActivity extends WildernessActivity {
	
	private WildernessLocation location;

	@Override
	public String getDescription() {
		return "Firemaking & Cooking have bonuses in " + location.getLocationInfo();
	}

	@Override
	public String getServerAnnouncement() {
		return "Firemaking and Cooking in <col=" + ChatColors.BLUE + ">" + getLocationInformation() + "</col> will now have significant benefits!";
	}

	/**
	 * The location information
	 */
	public String getLocationInformation() {
		return location.getLocationInfo();
	}

	@Override
	public void onCreate() {
		List<WildernessLocation> locations = new ArrayList<>(Arrays.asList(WildernessLocation.values()));
		Collections.shuffle(locations);
		location = locations.get(0);
		locations.clear();
	}

	@Override
	public void process() {

	}

	@Override
	public void onFinish() {
	}

	@Override
	public long getActivityTime() {
		return TimeUnit.MINUTES.toMillis(10);
	}

	@Override
	public boolean receivesBonus(Player player, Object... params) {
		return location.isInArea(player);
	}

	@Override
	public Integer getBonusPoints(Player player) {
		if (player.isAnyDonator()) {
			return 2;
		}
		return 1;
	}

	@Override
	public Integer getPointChance(Player player) {
		if (player.isAnyDonator()) {
			return 75;
		}
		return 50;
	}

	private enum WildernessLocation {

		EDGEVILLE {
			@Override
			public int[] getRegionIds() {
				return new int[] { 12343, 12087 };
			}

			@Override
			public int getWildernessLevel() {
				return 15;
			}

			@Override
			public String getLocationInfo() {
				return "Edgeville Lvl 11-15";
			}
		},

		VARROCK_HOME {
			@Override
			public int[] getRegionIds() {
				return new int[] { 13111, 13367 };
			}

			@Override
			public int getWildernessLevel() {
				return 13;
			}

			@Override
			public String getLocationInfo() {
				return "North Varrock Wilderness (Lvl 11-13)";
			}

		};

		/**
		 * If the player is in the region
		 *
		 * @param player
		 * 		The player
		 */
		public boolean isInArea(Player player) {
			for (int regionId : getRegionIds()) {
				if (player.getRegionId() == regionId) {
					if (Wilderness.getWildLevel(player) <= getWildernessLevel()) {
						return true;
					}
				}
			}
			return false;
		}

		/**
		 * The region ids relevant
		 */
		public abstract int[] getRegionIds();

		/**
		 * The wilderness level it goes to
		 */
		public abstract int getWildernessLevel();

		/**
		 * Information about the location
		 */
		public abstract String getLocationInfo();

	}

}
