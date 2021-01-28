package com.runescape.game.world.entity.npc.pest;

import com.runescape.game.content.global.minigames.pest.PestControl;
import com.runescape.game.world.WorldTile;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/24/2016
 */
public class ProjectilePest extends PestMonsters {

	public ProjectilePest(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned, int index, PestControl manager) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned, index, manager);
	}
}
