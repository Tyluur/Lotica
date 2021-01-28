package com.runescape.utility.world.npc;

import com.runescape.game.world.entity.npc.NPC;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 2012-11-16
 */
public class NPCWalkingData {

	/**
	 * The map of npcs with their custom walking definitions
	 */
	private static final Map<Integer, Boolean> definitions = new HashMap<>();
	
	/**
	 * Loads all non moving npcs
	 */
	public static void loadList() {
		try {
			BufferedReader in = new BufferedReader(new FileReader("data/resource/world/npcs/nonwalking.txt"));
			String line;
			while ((line = in.readLine()) != null) {
				String[] split = line.split(" - ");
				int id = Integer.parseInt(split[0]);
				boolean walking = Boolean.parseBoolean(split[1]);
				definitions.put(id, walking);
			}
			in.close();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static boolean containsNPC(int npcId) {
		return definitions.containsKey(npcId);
	}

	/**
	 * Gets the flag for the npc walking data
	 *
	 * @param npcId
	 * 		The npc
	 */
	public static int getWalkingFlag(int npcId) {
		if (!definitions.containsKey(npcId)) {
			return NPC.NO_WALK;
		} else {
			Boolean walking = definitions.get(npcId);
			if (walking) {
				return NPC.NORMAL_WALK;
			} else {
				return NPC.NO_WALK;
			}
		}
	}
}