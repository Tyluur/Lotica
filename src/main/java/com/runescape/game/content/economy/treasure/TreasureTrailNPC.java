package com.runescape.game.content.economy.treasure;

import com.runescape.game.content.global.miniquest.QuestNPC;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.ForceTalk;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 16, 2015
 */
public class TreasureTrailNPC extends QuestNPC {

	private static final long serialVersionUID = -6730051737609708214L;

	public TreasureTrailNPC(Player target, int id, WorldTile tile) {
		super(target, id, tile);
		setSpawned(true);
		setNextForceTalk(new ForceTalk("Aha! You will not find the treasure."));
	}

	@Override
	public void processNPC() {
		super.processNPC();
		if (target != null) {
			NPC clueNPC = target.getAttribute("clue_npc");
			if (clueNPC != null) {
				if (!clueNPC.equals(this)) {
					finish();
					System.out.println("Multiple clue npcs stopped from spawning.");
				}
			}
		}
	}
	
	@Override
	public void drop() {
		TreasureTrailHandler.completeTrail(target, getAttribute("trail_tier"), getAttribute("trail_class"), true);
	}

}
