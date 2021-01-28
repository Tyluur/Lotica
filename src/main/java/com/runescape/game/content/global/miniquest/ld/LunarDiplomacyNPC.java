package com.runescape.game.content.global.miniquest.ld;

import com.runescape.game.content.global.miniquest.QuestNPC;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.quests.impl.LunarDiplomacy;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 17, 2015
 */
public class LunarDiplomacyNPC extends QuestNPC {

	private static final long serialVersionUID = 8801572783935652680L;

	public LunarDiplomacyNPC(Player target, int id, WorldTile tile) {
		super(target, id, tile);
	}

	@Override
	public void processNPC() {
		super.processNPC();
		if (target == null) {
			return;
		}
		int[] bonuses = new int[10];
		for (int i = 0; i < target.getCombatDefinitions().getBonuses().length; i++) {
			if (i > 9) {
				continue;
			}
			double newDef = target.getCombatDefinitions().getBonuses()[i] * 0.65;
			double newAttack = i == 3 ? 150 : target.getCombatDefinitions().getBonuses()[i] * (getHitpoints() <= 300 ? 6 : 3.75);
			bonuses[i] = i > 4 ? (int) newDef : (int) newAttack;
		}
		setBonuses(bonuses);
	}
	
	@Override
	public void drop() {
		target.getControllerManager().forceStop();
		target.getQuestManager().finishQuest(LunarDiplomacy.class);
	}

}
