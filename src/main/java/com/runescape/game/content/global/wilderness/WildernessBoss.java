package com.runescape.game.content.global.wilderness;

import com.runescape.game.content.global.wilderness.activities.WildernessBossActivity;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.npc.NPC;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Jan 1, 2015
 */
@SuppressWarnings("serial")
public class WildernessBoss extends NPC {

	public WildernessBoss(int id, WorldTile tile) {
		super(id, tile, -1, true);
		setSpawned(true);
		if (getBonuses() == null) {
			setBonuses(new int[] { 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, });
			System.out.println(getName() + "[" + getId() + "] had no bonuses, so we generated some.");
		}
		for (int i = 0; i < getBonuses().length; i++) {
			getBonuses()[i] += 120;
		}
	}
	
	@Override
	public void drop() {
		super.drop();
		/** Gives players engaging in combat with the boss bonus points */
		for (Entity source : getReceivedDamage().keySet()) {
			if (!source.isPlayer()) {
				continue;
			}
			Integer damage = getReceivedDamage().get(source);
			if (damage == null || source.hasFinished()) {
				continue;
			}
			double percent = (double) damage / (double) getMaxHitpoints();
			if (percent > 0.10) {
				WildernessActivityManager.getSingleton().giveBonusPoints(source.player());
			}
		}
		if (WildernessActivityManager.getSingleton().isActivityCurrent(WildernessBossActivity.class)) {
			World.sendWorldMessage("<col=FF0000>Wilderness Activity:</col> The wilderness boss has been defeated! A new one is coming...", false);
			WildernessBossActivity activity = WildernessActivityManager.getSingleton().getWildernessActivity(WildernessBossActivity.class);
			activity.setNextSpawnTime(System.currentTimeMillis() + WildernessBossActivity.RESPAWN_DELAY);
		}
	}

}
