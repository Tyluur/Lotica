package com.runescape.utility.world.player;

import com.runescape.game.GameConstants;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.SerializableFilesManager;
import com.runescape.utility.Utils;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;

public final class PkRank implements Serializable {

	private static final long serialVersionUID = 5403480618483552509L;

	/** The name of the pker */
	private String username;

	/** The data about the pker */
	private int kills, deaths;

	/** The array of ranks */
	private static PkRank[] ranks;

	/** The path that pk ranks will be saved in */
	private static final String PATH = GameConstants.HOSTED ? GameConstants.LINUX_HOST ? "/root/gamedata/pk_rankings.ser" : "C:/gamedata/pk_rankings.ser" : "data/resource/pkRanks.ser";

	public PkRank(Player player) {
		this.username = player.getUsername();
		this.kills = player.getKillCount();
		this.deaths = player.getDeathCount();
	}

	/**
	 * Loads all pk ranks from the {@link #PATH} into the {@link #ranks}
	 */
	public static void init() {
		File file = new File(PATH);
		if (file.exists()) {
			try {
				ranks = (PkRank[]) SerializableFilesManager.loadSerializedFile(file);
				return;
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		ranks = new PkRank[300];
	}

	/**
	 * Saves all pk ranks
	 */
	public static void save() {
		try {
			SerializableFilesManager.storeSerializableClass(ranks, new File(PATH));
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	/**
	 * Shows the ranks over an interface
	 *
	 * @param player
	 * 		The player to show the ranks to
	 */
	public static void showRanks(Player player) {
		int interfaceId = 275;
		Utils.clearInterface(player, interfaceId);
		for (int i = 0; i < 300; i++) {
			if (i >= ranks.length || ranks[i] == null) {
				break;
			}
			String text;
			if (i >= 0 && i <= 2) {
				text = "<col=FFFF66>";
			} else if (i <= 9) {
				text = "<col=CCCCCC>";
			} else if (i <= 49) {
				text = "<col=663300>";
			} else {
				text = "<col=000000>";
			}
			double kdr = (double) ranks[i].kills / (double) ranks[i].deaths;
			player.getPackets().sendIComponentText(interfaceId, i + 16, text + "" + (i + 1) + ". " + Utils.formatPlayerNameForDisplay(ranks[i].username) + ": " + ranks[i].kills + " kills/" + ranks[i].deaths + " deaths (" + Utils.formatDecimal(kdr) + ")");
		}
		player.getPackets().sendIComponentText(interfaceId, 2, "Top PKers");
		player.getInterfaceManager().sendInterface(interfaceId);
	}

	/**
	 * Sorting the ranks
	 */
	public static void sort() {
		Arrays.sort(ranks, (arg0, arg1) -> {
			if (arg0 == null) {
				return 1;
			}
			if (arg1 == null) {
				return -1;
			}
			if (arg0.kills < arg1.kills) {
				return 1;
			} else if (arg0.kills > arg1.kills) {
				return -1;
			} else {
				return 0;
			}
		});
	}

	/**
	 * This method updates the rankings for players.
	 *
	 * @param player
	 * 		The player to update the ranking fora
	 */
	public static void updateRankings(Player player) {
		if (ranks == null) {
			return;
		}
 		for (int i = 0; i < ranks.length; i++) {
			PkRank rank = ranks[i];
			if (rank == null) {
				break;
			}
			if (rank.username.equalsIgnoreCase(player.getUsername())) {
				ranks[i] = new PkRank(player);
				sort();
				return;
			}
		}
		for (int i = 0; i < ranks.length; i++) {
			PkRank rank = ranks[i];
			if (rank == null) {
				ranks[i] = new PkRank(player);
				sort();
				return;
			}
		}
		int kills = player.getKillCount();
		for (int i = 0; i < ranks.length; i++) {
			if (ranks[i].kills < kills) {
				ranks[i] = new PkRank(player);
				sort();
				return;
			}
		}
	}

}
