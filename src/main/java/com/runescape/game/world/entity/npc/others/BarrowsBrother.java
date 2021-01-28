package com.runescape.game.world.entity.npc.others;

import com.runescape.game.interaction.controllers.impl.Barrows;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.utility.Utils;

@SuppressWarnings("serial")
public class BarrowsBrother extends NPC {

	private Barrows barrows;

	public BarrowsBrother(int id, WorldTile tile, Barrows barrows) {
		super(id, tile, -1, true, true);
		this.barrows = barrows;
	}

	@Override
	public void drop() {
		if (barrows != null) {
			barrows.targetDied();
			barrows = null;
		}
	}

	@Override
	public double getMeleePrayerMultiplier() {
		return getId() != 2030 ? 0 : Utils.random(3) == 0 ? 1 : 0;
	}
	
	public void disapear() {
		barrows = null;
		finish();
	}

	@Override
	public void finish() {
		if (hasFinished()) { return; }
		if (barrows != null) {
			barrows.targetFinishedWithoutDie();
			barrows = null;
		}
		super.finish();
	}

}
