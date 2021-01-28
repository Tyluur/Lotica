package com.runescape.game.content.global.miniquest;

import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 7, 2015
 */
public class QuestNPC extends NPC {
	private static final long serialVersionUID = -3900198370982938815L;

	public QuestNPC(Player target, int id, WorldTile tile) {
		super(id, tile, -1, true);
		getCombat().setTarget(this.target = target);
		getCombat().process();
	}
	
	@Override
	public void processNPC() {
		if (target == null || target.hasFinished() || !withinDistance(target, 30)) {
			finish();
			return;
		}
		super.processNPC();
 	}
 	
	@Override
	public boolean canBeAttacked(Player player) {
		if (!player.equals(target)) {
			player.sendMessage("You cannot attack this npc.");
			return false;
		}
		return true;
	}

	/**
	 * The target that this npc must kill
	 */
	protected final Player target;
}
