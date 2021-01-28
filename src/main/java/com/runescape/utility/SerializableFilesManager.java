package com.runescape.utility;

import com.runescape.game.GameConstants;
import com.runescape.game.content.global.clans.Clan;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.world.player.PlayerSaving;

import java.io.*;

public class SerializableFilesManager {

	public static final String CLAN_PATH = GameConstants.FILES_PATH + "players/clans/";

	public static boolean containsPlayer(String username) {
		return PlayerSaving.playerExists(username);
	}

	public static Player loadPlayer(String username) {
		return PlayerSaving.fromFile(username);
	}

	public static void savePlayer(Player player) {
		PlayerSaving.savePlayer(player);
	}

	public static Object loadSerializedFile(File f) throws IOException, ClassNotFoundException {
		if (!f.exists()) {
			return null;
		}
		ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));
		Object object = in.readObject();
		in.close();
		return object;
	}

	public static void storeSerializableClass(Serializable o, File f) throws IOException {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));
		out.writeObject(o);
		out.close();
	}

	public synchronized static boolean containsClan(String name) {
		return new File(CLAN_PATH + name + ".c").exists();
	}

	public synchronized static Clan loadClan(String name) {
		try {
			return (Clan) loadSerializedFile(new File(CLAN_PATH + name + ".c"));
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	public synchronized static void saveClan(Clan clan) {
		try {
			storeSerializableClass(clan, new File(CLAN_PATH + clan.getClanName() + ".c"));
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public synchronized static void deleteClan(Clan clan) {
		try {
			new File(CLAN_PATH + clan.getClanName() + ".c").delete();
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private SerializableFilesManager() {

	}

}
