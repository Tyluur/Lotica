package com.runescape.utility;

import com.runescape.game.GameConstants;

import java.util.ArrayList;

/**
 * Anti Flood
 *
 * @Author Apache Ah64
 */
public final class AntiFlood {

	private static ArrayList<String> connections = new ArrayList<String>(GameConstants.PLAYERS_LIMIT * 3);

	public static void add(String ip) {
		connections.add(ip);
	}

	public static void remove(String ip) {
		connections.remove(ip);
	}

	public static int getSessionsIP(String ip) {
		int amount = 1;
		for (String connection : connections) {
			if (connection.equalsIgnoreCase(ip)) { amount++; }
		}
		return amount;
	}
}