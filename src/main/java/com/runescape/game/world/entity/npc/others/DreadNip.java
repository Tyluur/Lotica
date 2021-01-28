package com.runescape.game.world.entity.npc.others;

import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;

@SuppressWarnings("serial")
public class DreadNip extends NPC {

	public static final String[] DREADNIP_MESSAGES = {
		"Your dreadnip couldn't attack so it left.",
		"The dreadnip gave up as you were too far away.",
	"Your dreadnip served its purpose and fled."};

	private Player owner;
	private int ticks;

	public DreadNip(Player owner, int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
		this.owner = owner;
	}

	@Override
	public void processNPC() {
		super.processNPC();
		if (owner == null || owner.hasFinished()) {
			finish(-1);
			return;
		} else if (getCombat().getTarget() == null || getCombat().getTarget().isDead()) {
			finish(0);
			return;
		} else if (Utils.getDistance(owner, this) >= 10) {
			finish(1);
			return;
		} else if (ticks++ == 33) {
			finish(2);
			return;
		} 
	}

	private void finish(int index) {
		if (index != -1) {
			owner.getPackets().sendGameMessage(DREADNIP_MESSAGES[index]);
			owner.getAttributes().remove("hasDN");
		}
		this.finish();
	}

	public Player getOwner() {
		return owner;
	}

	public int getTicks() {
		return ticks;
	}
}
