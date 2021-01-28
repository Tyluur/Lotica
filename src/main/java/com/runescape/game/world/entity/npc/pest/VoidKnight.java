package com.runescape.game.world.entity.npc.pest;

import com.runescape.game.content.global.minigames.pest.PestControl;
import com.runescape.game.world.WorldTile;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/24/2016
 */
public class VoidKnight extends PestPortal {

	public VoidKnight(int id, boolean canbeAttackedOutOfArea, WorldTile tile, PestControl control) {
		super(id, canbeAttackedOutOfArea, tile, control);
	}
}
