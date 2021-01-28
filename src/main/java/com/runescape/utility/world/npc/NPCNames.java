package com.runescape.utility.world.npc;

import com.runescape.game.GameConstants;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 20, 2015
 */
public enum NPCNames {

	GUIDE(945, GameConstants.SERVER_NAME + " Guide"),
	POS_OWNER(4361, "Shop Manager"),
	GHOST_SHOPKEEPER(1699, "PK Supply Store"),
	ROBE_STOREOWNER(1658, "Magic Store"),
	HORVIK(549, "Melee Store"),
	LOWE(550, "Range Store"),
	COOK(278, "Food & Potions"),
	SIR_PERCIVAL(211, "Achievement Store"),
	MELANA_MOONLANDER(4516, "Dream Host");

	/**
	 * The npc ids that will have this new name
	 */
	private final int[] npcIds;

	/**
	 * The new name of the npc
	 */
	private final String name;

	NPCNames(int npcId, String name) {
		this.npcIds = new int[] { npcId };
		this.name = name;
	}

	NPCNames(int[] npcIds, String name) {
		this.npcIds = npcIds;
		this.name = name;
	}

	/**
	 * @return the npcIds
	 */
	public int[] getNpcIds() {
		return npcIds;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the new name for the npc id
	 * 
	 * @param npcId
	 *            The npc id
	 * @return
	 */
	public static String getName(int npcId) {
		for (NPCNames names : NPCNames.values()) {
			for (int npcIds : names.npcIds) {
				if (npcIds == npcId) {
					return names.getName();
				}
			}
		}
		return null;
	}

}
