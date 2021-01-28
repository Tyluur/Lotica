package com.runescape.utility.applications.console;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.utility.world.player.PlayerSaving;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/5/2015
 */
public abstract class GameScript {

	protected static BufferedWriter bufferedWriter;

	protected String getFileLocation() {
		return "";
	}

	public static File[] getAccounts() {
		File dir = new File(PlayerSaving.FILES_LOCATION);
		return dir.listFiles();
	}

	public static File getFile(String name) {
		return new File(PlayerSaving.FILES_LOCATION + name + PlayerSaving.SUFFIX);
	}

	public static Player getPlayer(String name) throws IOException, ClassNotFoundException {
		return getPlayer(getFile(name));
	}

	public static Player getPlayer(File file) throws ClassNotFoundException, IOException {
		Player player = PlayerSaving.fromFile(file.getName().replace(PlayerSaving.SUFFIX, ""));
		if (player == null) {
			return null;
		}
		player.setUsername(file.getName().replace(PlayerSaving.SUFFIX, ""));
		return player;
	}

	public static void savePlayer(Player player, File account, boolean... print) {
		PlayerSaving.savePlayer(player);
		if (print.length > 0 && print[0]) {
			System.out.println("Saved " + account + "");
		}
	}

	protected void write(String data) {
		if (bufferedWriter == null) {
			try {
				bufferedWriter = new BufferedWriter(new FileWriter(getFileLocation()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			bufferedWriter.write(data);
			bufferedWriter.newLine();
			bufferedWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Item[] getBankItems(Player player) {
		return player.getBank().generateContainer();
	}

}
