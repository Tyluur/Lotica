package com.runescape.game.content.global.miniquest.dt;

import com.runescape.game.content.global.miniquest.QuestNPC;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.ForceTalk;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.quests.impl.DesertTreasure;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 7, 2015
 */
public class DesertTreasureNPC extends QuestNPC {
	private static final long serialVersionUID = 1L;

	/**
	 * @param target
	 *            The target of the npc
	 * @param id
	 *            The id of the npc
	 * @param tile
	 *            The tile to spawn the npc at
	 */
	public DesertTreasureNPC(Player target, int id, WorldTile tile) {
		super(target, id, tile);
		this.setForceAgressive(true);
		this.setIntelligentRouteFinder(true);
	}
	
	@Override
	public void sendDeath(Entity source) {
		// damis
		if (getId() == 1974) {
			transformIntoNPC(1975);
			setHitpoints(getMaxHitpoints());
			setNextForceTalk(new ForceTalk("I am Damis, invincible Lord of the Shadows!"));
		} else {
			super.sendDeath(source);
		}
	}

	@Override
	public void drop() {
		target.getControllerManager().verifyControlerForOperation(DesertTreasureController.class).ifPresent(c -> {
			DesertTreasure.updateMonsterProgress(target, DesertTreasure.getBossIndex(getId()), true);
			target.setNextWorldTile(DesertTreasure.FINISH_TILE);
		});
		Map<String, Boolean> monsters = target.getQuestManager().getAttribute(DesertTreasure.class, DesertTreasure.MONSTERS_KEY, new HashMap<>());
		if (monsters.size() == 4) {
			boolean allComplete = true;
			for (Entry<String, Boolean> entry : monsters.entrySet()) {
				if (!entry.getValue()) {
					allComplete = false;
					break;

				}
			}
			if (allComplete) {
				target.getQuestManager().finishQuest(DesertTreasure.class);
			}
		}
	}
	
}
