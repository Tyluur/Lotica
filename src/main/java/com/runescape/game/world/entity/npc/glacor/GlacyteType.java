package com.runescape.game.world.entity.npc.glacor;

import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.masks.Hit;
import com.runescape.game.world.entity.masks.Hit.HitLook;
import com.runescape.utility.Utils;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since May 3, 2014
 */
public enum GlacyteType {

	/**
	 * Explodes after 20 ticks and loses 90% of health, dealing 1/3 of the
	 * player's hitpoints if they are 1 tile next to it. Every tick afterwards
	 * it will heal 5% health.
	 */
	UNSTABLE(14302) {
		@Override
		public void processGlacyte(Glacyte glacyte) {
			int ticks = 0;
			if (glacyte.getAttributes().get("ticks_passed") != null) {
				ticks = (Integer) glacyte.getAttributes().get("ticks_passed");
			} else {
				glacyte.getAttributes().put("ticks_passed", 0);
			}
			if (ticks == 20) {
				final WorldTile tile = new WorldTile(glacyte);
				for (Entity e : glacyte.getPossibleTargets()) {
					if (e == null || e.isDead() || !e.withinDistance(tile, 1)) {
						continue;
					}
					e.applyHit(new Hit(glacyte, e.getHitpoints() / 3, HitLook.REGULAR_DAMAGE));
				}
				glacyte.applyHit(new Hit(glacyte, (int) (glacyte.getHitpoints() * .9), HitLook.REFLECTED_DAMAGE));
				glacyte.setNextGraphics(new Graphics(956));
				glacyte.getAttributes().remove("ticks_passed");
			} else {
				glacyte.heal((int) (glacyte.getMaxHitpoints() * .05));
				glacyte.getAttributes().put("ticks_passed", ticks + 1);
			}
		}
	},

	/**
	 * Drains 20 prayer points when it hits the opponent
	 */
	SAPPING(14303),

	/**
	 * Takes 60% less damage than the original hit no matter what. However, if
	 * they are within 14 tiles of the glacor, it will take 20% less.
	 */
	ENDURING(14304) {
		@Override
		public void handleIncomingHit(Glacyte glacyte, Hit hit) {
			int damage = hit.getDamage();
			if (damage > 0) {
				damage = (int) ((((6 - Utils.getDistance(hit.getSource(), glacyte)) / 10) + .4D) * damage);
				hit.setDamage(damage);
			}
		}
	};

	GlacyteType(int id) {
		this.id = id;
	}

	/**
	 * The id of the glacyte
	 */
	private final int id;

	/**
	 * Gets the type of glacyte we're dealing with by the ID
	 * 
	 * @param id
	 *            The glacyte id
	 * @return
	 */
	public static final GlacyteType getType(int id) {
		for (GlacyteType type : GlacyteType.values()) {
			if (type.id == id) {
				return type;
			}
		}
		return null;
	}

	/**
	 * Handles how to process the glacyte every 600 ticks
	 * 
	 * @param glacyte
	 *            The glacyte processing
	 */
	public void processGlacyte(Glacyte glacyte) {
		// default nothing
	}

	/**
	 * Handles how to process a hit that's coming into the glacyte
	 * 
	 * @param glacyte
	 *            The glacyte
	 * @param hit
	 *            The hit
	 */
	public void handleIncomingHit(Glacyte glacyte, Hit hit) {
		// default nothing
	}
}