package com.runescape.game.content.global.miniquest.hftd;

import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.ForceTalk;
import com.runescape.game.world.entity.masks.Hit;
import com.runescape.game.world.entity.masks.Hit.HitLook;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.quests.impl.HorrorFromTheDeep;
import com.runescape.utility.Utils;

import java.util.concurrent.TimeUnit;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since Dec 13, 2013
 */
class Dagannoth_Mother extends NPC {

	private static final long serialVersionUID = 883380186489520619L;

	/**
	 * The npcs weakness
	 */
	private Weaknesses weakness;

	/**
	 * The last time we changed the weakness type
	 */
	private long lastWeaknessUpdate;

	Dagannoth_Mother(WorldTile tile) {
		super(1351, tile, -1, true);
		this.setForceMultiArea(true);
		this.setSpawned(true);

		setWeakness(Weaknesses.MELEE);
	}

	/**
	 * Transforms into the weakness npc id and sets the weakness
	 */
	private void setWeakness(Weaknesses weakness) {
		this.weakness = weakness;
		transformIntoNPC(weakness.getId());
		lastWeaknessUpdate = Utils.currentTimeMillis();
	}

	@Override
	public void processNPC() {
		super.processNPC();
		if (TimeUnit.MILLISECONDS.toSeconds(Utils.currentTimeMillis() - lastWeaknessUpdate) >= 5) {
			setNextForceTalk(new ForceTalk("I bet you aren't ready for this!"));
			setNextAnimation(new Animation(1340));
			setRandomWeakness();
		}
	}

	@Override
	public void handleIngoingHit(final Hit hit) {
		if (hit.getSource() instanceof Player) {
			double percentage = 0.25;
			int dmg = (int) ((getHitpoints() * percentage));
			if (dmg <= 15) {
				dmg = getHitpoints();
			}
			switch (weakness) {
				case AIR:
				case WATER:
				case EARTH:
				case FIRE:
					if (hit.getLook() == HitLook.MAGIC_DAMAGE) {
						hit.setDamage(dmg);
					} else {
						hit.setDamage(0);
					}
					break;
				case MELEE:
					if (hit.getLook() == HitLook.MELEE_DAMAGE) {
						hit.setDamage(dmg);
					} else {
						hit.setDamage(0);
					}
					break;
				case RANGED:
					if (hit.getLook() == HitLook.RANGE_DAMAGE) {
						hit.setDamage(dmg);
					} else {
						hit.setDamage(0);
					}
					break;
			}
		}
	}

	@Override
	public void drop() {
		Player player = getMostDamageReceivedSourcePlayer();
		if (player == null) {
			return;
		}
		player.getQuestManager().finishQuest(HorrorFromTheDeep.class);
	}

	/**
	 * Sets a random weakness
	 */
	private void setRandomWeakness() {
		Weaknesses[] weaknesses;
		switch (weakness) {
			case AIR:
			case EARTH:
			case FIRE:
			case WATER:
				weaknesses = new Weaknesses[] { Weaknesses.MELEE, Weaknesses.RANGED };
				break;
			case MELEE:
				weaknesses = new Weaknesses[] { Weaknesses.AIR, Weaknesses.EARTH, Weaknesses.FIRE, Weaknesses.WATER, Weaknesses.RANGED };
				break;
			case RANGED:
				weaknesses = new Weaknesses[] { Weaknesses.AIR, Weaknesses.EARTH, Weaknesses.FIRE, Weaknesses.WATER, Weaknesses.MELEE };
				break;
			default:
				weaknesses = Weaknesses.values();
				break;
		}
		setWeakness(weaknesses[Utils.random(weaknesses.length - 1)]);
	}

}
