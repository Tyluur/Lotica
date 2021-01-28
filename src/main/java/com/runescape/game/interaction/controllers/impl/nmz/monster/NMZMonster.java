package com.runescape.game.interaction.controllers.impl.nmz.monster;

import com.runescape.game.interaction.controllers.impl.nmz.NMZController;
import com.runescape.game.interaction.controllers.impl.nmz.NMZInstance;
import com.runescape.game.interaction.controllers.impl.nmz.NMZModes;
import com.runescape.game.interaction.controllers.impl.nmz.powerup.NMZPowerup;
import com.runescape.game.interaction.controllers.impl.nmz.powerup.PowerupGenerator;
import com.runescape.game.interaction.controllers.impl.nmz.powerup.impl.RecurrentDamagePowerup;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.ForceTalk;
import com.runescape.game.world.entity.masks.Hit;
import com.runescape.game.world.entity.masks.Hit.HitLook;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since January 13th, 2016
 */
public class NMZMonster extends NPC {

	/**
	 * The attack bonuses will be multiplied by this number for the hard/elite rates
	 */
	private static final double ATTACK_BONUSES_CHANGE = 1.75;

	/**
	 * The defense bonuses will be multiplied by this number for the hard/elite rates
	 */
	private static final double DEFENSE_BONUSES_CHANGE = 1.15;

	/**
	 * The multiplier on the bonuses changes for the elite mode
	 */
	private static final double ELITE_MODE_MULTIPLIER = 1.05;

	/**
	 * The multiplier for easy mode bonuses
	 */
	private static final double EASY_MODE_MULTIPLIER = 0.60;

	/**
	 * The instance of the game
	 */
	private final NMZInstance game;

	/**
	 * The boss instance
	 */
	private final PossibleBosses boss;

	@Override
	public void sendDeath(Entity source) {
		if (getId() == 1974) {
			transformIntoNPC(1975);
			setHitpoints(getMaxHitpoints());
			setNextForceTalk(new ForceTalk("I am Damis, invincible Lord of the Shadows!"));
		} else if (getId() == 5904) {
			transformIntoNPC(5903);
			setHitpoints(getMaxHitpoints());
		} else if (getId() == 5903) {
			transformIntoNPC(5902);
			setHitpoints(getMaxHitpoints());
		} else {
			super.sendDeath(source);
		}
	}

	@Override
	public void handleIngoingHit(Hit hit) {
		super.handleIngoingHit(hit);
		if (hit.getDamage() > 1 && !hit.getLook().equals(HitLook.REGULAR_DAMAGE) && !hit.getLook().equals(HitLook.REFLECTED_DAMAGE) && hit.getSource().isPlayer()) {
			Player hitter = hit.getSource().player();
			hitter.getControllerManager().verifyControlerForOperation(NMZController.class).ifPresent(game -> {
				NMZPowerup powerup = PowerupGenerator.powerupByClass(RecurrentDamagePowerup.class);
				if (powerup != null) {
					if (game.hasPowerupActive(powerup)) {
						WorldTasksManager.schedule(new WorldTask() {
							@Override
							public void run() {
								applyHit(new Hit(hitter, (int) (hit.getDamage() * 0.75), HitLook.REFLECTED_DAMAGE));
							}
						});
					}
				} else {
					System.out.println("couldnt find powerup.");
				}
			});
		}
	}

	@Override
	public void processNPC() {
		super.processNPC();
		if (getCombat().getTarget() == null) {
			checkAggressivity();
		}
		if (getId() == 4509) {
			Entity targetE = getCombat().getTarget();
			if (targetE == null || !targetE.isPlayer()) {
				return;
			}
			Player target = targetE.player();
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
	}

	@Override
	public void drop() {
		game.getMonsterGenerator().handleDeath(this);
	}

	@Override
	public boolean walksToRespawnTile() {
		return false;
	}

	@Override
	public void forceWalkRespawnTile() {

	}

	@Override
	public ArrayList<Entity> getPossibleTargets(boolean checkNPCs, boolean checkPlayers) {
		ArrayList<Entity> targets = new ArrayList<>();
		for (int regionId : getMapRegionsIds()) {
			if (checkPlayers) {
				List<Integer> playerIndexes = World.getRegion(regionId).getPlayerIndexes();
				if (playerIndexes != null) {
					for (int playerIndex : playerIndexes) {
						Player player = World.getPlayers().get(playerIndex);
						if (player == null || player.isDead() || player.hasFinished() || !player.isRunning() || player.getAppearence().isHidden()) {
							continue;
						}
						targets.add(player);
					}
				}
			}
			if (checkNPCs) {
				List<Integer> npcsIndexes = World.getRegion(regionId).getNPCsIndexes();
				if (npcsIndexes != null) {
					for (int npcIndex : npcsIndexes) {
						NPC npc = World.getNPCs().get(npcIndex);
						if (npc == null || npc == this || npc.isDead() || npc.hasFinished()) {
							continue;
						}
						targets.add(npc);
					}
				}
			}
		}
		return targets;
	}

	public NMZMonster(int id, PossibleBosses boss, WorldTile tile, NMZInstance game) {
		super(id, tile);
		this.boss = boss;
		this.game = game;
		this.respawnTileDistance = 64;

		setForceAgressive(true);
		setForceMultiArea(true);

		setSpawned(true);
		applyDifficultyChanges();
	}

	private void applyDifficultyChanges() {
		switch (game.getMode()) {
			case ELITE:
				int oldDelay = getCombatDefinitions().getAttackDelay();
				int newDelay = (int) (oldDelay * 0.90);
				if (newDelay <= 0) { newDelay = 1; }
				getCombatDefinitions().setAttackDelay(newDelay);
				//System.out.println("old delay=" + oldDelay + ", newdelay=" + newDelay);
				break;
		}
		switch (getId()) {
			case 3491:
				getCombatDefinitions().setAttackStyle(getCombatDefinitions().getAttackDelay() * 2);
				break;
		}
		applyModeBonuses(game.getMode());
	}

	/**
	 * Sets bonuses for modes
	 *
	 * @param mode
	 * 		The mode
	 */
	private void applyModeBonuses(NMZModes mode) {
		int[] bonuses = getBonuses();
		if (bonuses == null) {
			bonuses = new int[10];
		}
		for (int i = 0; i < 5; i++) {
			bonuses[i] = (int) (bonuses[i] * (ATTACK_BONUSES_CHANGE * (mode.equals(NMZModes.ELITE) ? ELITE_MODE_MULTIPLIER : 1)));
		}
		for (int i = 5; i < bonuses.length; i++) {
			bonuses[i] = (int) (bonuses[i] * (DEFENSE_BONUSES_CHANGE * (mode.equals(NMZModes.ELITE) ? ELITE_MODE_MULTIPLIER : 1)));
		}
		if (mode.equals(NMZModes.EASY)) {
			for (int i = 0; i < bonuses.length; i++) {
				bonuses[i] = (int) (bonuses[i] * EASY_MODE_MULTIPLIER);
			}
		}
	}

	/**
	 * Constructs a list of all players who dealt damage to us
	 */
	public List<Player> playersDealtDamage() {
		List<Player> players = new ArrayList<>();
		Map<Entity, Integer> receivedDamage = getReceivedDamage();
		for (Entry<Entity, Integer> entry : receivedDamage.entrySet()) {
			Entity entity = entry.getKey();
			if (entity.isPlayer() && !players.contains(entity.player())) {
				players.add(entity.player());
			}
		}
		return players;
	}

	public PossibleBosses getBoss() {
		return boss;
	}
}
