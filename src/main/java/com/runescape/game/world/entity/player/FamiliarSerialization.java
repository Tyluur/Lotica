package com.runescape.game.world.entity.player;

import com.runescape.game.content.skills.summoning.Summoning.Pouches;
import com.runescape.game.world.entity.npc.familiar.impl.BeastOfBurden;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/5/2015
 *
 */
public class FamiliarSerialization {

	private final int npcId, specialEnergy, ticks, trackTimer;

	private final BeastOfBurden bob;

	private final boolean trackDrain;

	private final Pouches pouch;

	public FamiliarSerialization(Pouches pouch, int npcId, int specialEnergy, int ticks, int trackTimer, BeastOfBurden bob, boolean trackDrain) {
		this.pouch = pouch;
		this.npcId = npcId;
		this.specialEnergy = specialEnergy;
		this.ticks = ticks;
		this.trackTimer = trackTimer;
		this.bob = bob;
		this.trackDrain = trackDrain;
	}

	public int getNpcId() {
		return npcId;
	}

	public int getSpecialEnergy() {
		return specialEnergy;
	}

	public int getTicks() {
		return ticks;
	}

	public int getTrackTimer() {
		return trackTimer;
	}

	public BeastOfBurden getBob() {
		return bob;
	}

	public boolean isTrackDrain() {
		return trackDrain;
	}

	public Pouches getPouch() {
		return pouch;
	}
}
