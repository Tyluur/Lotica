package com.runescape.game.interaction.controllers.impl.nmz.monster;

import com.runescape.game.interaction.controllers.impl.nmz.NMZInstance;
import com.runescape.game.world.WorldTile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 1/8/2016
 */
public enum PossibleBosses {

	/*
	--  the kendal:              1813
	--  witches experiments:     897, 898, 899, 900
	--  king roald:             5838
	--  dad:                    14518
	--
	*/

	RECIPE_FOR_DISASTER_BOSSES(220, 3493, 3494, 3495, 3496, 3491),
	DESERT_TREASURE_BOSSES(275, 1914, 1913, 1977, 1974),
	BLACK_KNIGHT_TITAN(200, 221),
	ME(250, 4509),
	ELVARG(100, 742),
	EVIL_CHICKEN(110, 3375),
	BOUNCER(95, 269),
	GLOD(185, 5996),
	DAD(170, 1125),
	BLACK_DEMON(130, 4705),
//	BARRELCHEST(180, 5666),
	THE_UNTOUCHABLE(195, 5904),

	;

	/**
	 * The base points players receive for the death of this monster
	 */
	private final int basePoints;

	/**
	 * The id of the monster
	 */
	private final Integer npcIds[];

	PossibleBosses(int basePoints, int npcId) {
		this.basePoints = basePoints;
		this.npcIds = new Integer[] { npcId };
	}

	PossibleBosses(int basePoints, Integer... npcIds) {
		this.basePoints = basePoints;
		this.npcIds = npcIds;
	}

	/**
	 * Gets a random {@link PossibleBosses} {@code Object} by transforming all values to an array and grabbing a random
	 * index.
	 */
	public static Object[] randomBossInstance() {
		List<Object[]> list = new ArrayList<>();
		for (PossibleBosses bosses : values()) {
			for (int npcId : bosses.getNpcIds())
			list.add(new Object[] { bosses, npcId});
		}
		Collections.shuffle(list);
		return list.get(0);
	}

	public NMZMonster createNPC(int npcId, WorldTile tile, NMZInstance game) {
		return new NMZMonster(npcId, this, tile, game);
	}

	/**
	 * Gets this monster's base points
	 */
	public int getBasePoints() {
		return basePoints;
	}

	/**
	 * Gets the npc ids applicable to this monster
	 */
	public Integer[] getNpcIds() {
		return npcIds;
	}

}
