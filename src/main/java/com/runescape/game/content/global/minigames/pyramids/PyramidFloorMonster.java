package com.runescape.game.content.global.minigames.pyramids;

import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Hit;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;

import java.io.IOException;

public class PyramidFloorMonster extends NPC {

	static final long serialVersionUID = -3820836781004011805L;

	/**
	 * Constructs a new {@link PyramidFloorMonster} {@link NPC} {@code Object}
	 *
	 * @param floor
	 * 		The floor the monster was spawned on
	 * @param monster
	 * 		The {@code Monsters} {@code Object} type of monster this is
	 * @param tile
	 * 		The tile that the monster will be spawned on
	 */
	public PyramidFloorMonster(PyramidFloor floor, Monsters monster, WorldTile tile) {
		super(monster.getRandomId(), tile, -1, true);
		this.floor = floor;
		this.monster = monster;
		this.tier = pickRandomTier();
		this.setForceMultiArea(true);
		this.setForceAgressive(true);
		setSpawned(true);
		setTierInformation();
	}

	public PyramidFloorMonster(PyramidFloor floor, int id, WorldTile tile, Monsters monster) {
		super(id, tile, -1, true);
		this.floor = floor;
		this.monster = monster;
		this.tier = pickRandomTier();
		this.setForceMultiArea(true);
		this.setForceAgressive(true);
		setSpawned(true);
		setTierInformation();
	}

	@Override
	public void processNPC() {
		super.processNPC();
		if (tier != null) {
			setName(getDefinitions().getName() + " (T" + (tier.ordinal() + 1) + ")");
		}
	}

	@Override
	public void drop() {
		Player killer = getMostDamageReceivedSourcePlayer();
		floor.removeMonster(this);
		if (killer == null) {
			return;
		}
		int pointsToAdd = monster.getDeathPoints();
		double tierMod = tier == Tiers.TWO ? 1.3 : tier == Tiers.THREE ? 1.67 : 1;
		pointsToAdd = (int) (pointsToAdd * tierMod);
		final int pointsAdded = pointsToAdd;
		killer.getControllerManager().verifyControlerForOperation(PyramidHuntingGame.class).ifPresent(c -> c.addPoints(pointsAdded));
	}

	@Override
	public void handleIngoingHit(final Hit hit) {
		Entity source = hit.getSource();
		if (source == null) {
			return;
		}
		Player player = null;
		if (source.isPlayer()) {
			player = source.player();
		}
		if (player == null) {
			return;
		}
		player.getControllerManager().verifyControlerForOperation(PyramidHuntingGame.class).ifPresent(c -> {
			c.addDamageDealt(hit.getDamage());
		});
		super.handleIngoingHit(hit);
	}

	public Tiers pickRandomTier() {
		int random = Utils.random(0, 10);
		if (random > 6) {
			return Tiers.THREE;
		} else if (random >= 2 && random < 6) {
			return Tiers.TWO;
		} else {
			return Tiers.ONE;
		}
	}

	private void setTierInformation() {
		if (tier == null) {
			return;
		}
		double tierMultiplier = 1;
		int level = floor.getLevel();
		if (level > 5 && level <= 10) {
			tierMultiplier = 1.5;
		} else if (level > 10) {
			tierMultiplier = 2.1;
		}
		setBonuses(new int[13]);
		getCombatDefinitions().setMaxHit((int) (tier.maxHit * tierMultiplier));
		for (int i = 0; i <= 5; i++) {
			getBonuses()[i] = (int) (tier.attackBonus * tierMultiplier);
		}
		for (int i = 5; i <= 10; i++) {
			getBonuses()[i] = (int) (tier.defenceBonus * tierMultiplier);
		}
	}

	private final PyramidFloor floor;

	private final Monsters monster;

	private final Tiers tier;

	public static void main(String[] args) throws IOException {
		int level = 30;
		int gamePoints = 10_000_000;
		int damageDealt = 350_000;

		System.out.println("[level=" + level + ", gamePoints=" + gamePoints + ", damageDealt=" + damageDealt + "]");
		System.out.println("Point Reward: " + Utils.format(PyramidHunterConstants.getPointsToGive(level, gamePoints, damageDealt)));
	}

	public enum Monsters {
		ZOMBIES(1958, 2015, 73, 77, 3066, 5665, 8149) {
			@Override
			public int getDeathPoints() {
				return 100;
			}
		},
		GENERAL(1648, 2025, 2028, 110, 10797) {
			@Override
			public int getDeathPoints() {
				return 80;
			}
		},
		SCARAB(1969) {
			@Override
			public int getDeathPoints() {
				return 20;
			}
		};

		Monsters(int... npcIds) {
			this.npcIds = npcIds;
		}

		Monsters(int npcId) {
			this.npcIds = new int[] { npcId };
		}

		public int[] getNpcIds() {
			return npcIds;
		}

		public int getRandomId() {
			return npcIds[Utils.random(npcIds.length)];
		}

		private final int[] npcIds;

		/**
		 * The amount of points the player receives when this type of monster dies
		 */
		public abstract int getDeathPoints();
	}

	public enum Tiers {

		ONE(90, 75, 150),
		TWO(150, 120, 300),
		THREE(200, 150, 310);

		Tiers(int attackBonus, int defenceBonus, int maxHit) {
			this.attackBonus = attackBonus;
			this.defenceBonus = defenceBonus;
			this.maxHit = maxHit;
		}

		private final int attackBonus, defenceBonus, maxHit;
	}
}
