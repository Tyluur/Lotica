package com.runescape.game.world.entity.npc.glacor;

import com.runescape.game.world.World;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Hit;
import com.runescape.game.world.entity.npc.NPC;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since May 3, 2014
 */
@SuppressWarnings("serial")
public class Glacor extends NPC {

	public Glacor(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
		setCapDamage(2500);
		setSpawned(false);
	}

	@Override
	public void handleIngoingHit(final Hit hit) {
		if (!spawnedGlacytes && getHitpoints() <= getMaxHitpoints() / 2) {
			/* We have to spawn glacytes */
			spawnGlacytes();
			spawnedGlacytes = Boolean.TRUE;
		} else if (spawnedGlacytes && getGlacytes().size() > 0) {
			/* We have glacytes to kill */
			hit.setDamage(0);
		}
		super.handleIngoingHit(hit);
	}

	@Override
	public void processNPC() {
		super.processNPC();
		if (shouldRestore()) {
			return;
		}
		String targetName = null;
		if (getCombat().getTarget() != null && getCombat().getTarget().isPlayer()) {
			targetName = getCombat().getTarget().player().getUsername();
		}
		if (targetName != null) {
			if (getCombat().getTarget() == null) {
				Entity target = World.getPlayer(targetName);
				getCombat().setTarget(target);
			}
			for (Glacyte glacyte : glacytes) {
				if (glacyte == null)
					continue;
				glacyte.setTargetName(targetName);
			}
		}
	}
	
	public boolean shouldRestore() {
		if (getLastTimeHit() == -1)
			return false;
		if (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - getLastTimeHit()) >= 360) {
			reset();
			clearGlacytes();
			setLastTimeHit(-1);
			spawnedGlacytes = Boolean.FALSE;
			//System.out.println("A glacor was just restore up to maximum and the glacytes were deleted\t" + getLastTimeHit() + "\t[" + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - getLastTimeHit()) + "]");
			return true;
		}
		return false;
	}

	/**
	 * Clears the glacytes list
	 */
	public void clearGlacytes() {
		Iterator<Glacyte> it$ = glacytes.iterator();
		while(it$.hasNext()) {
			Glacyte glacyte = it$.next();
			glacyte.finish();
			it$.remove();
		}
	}

	/**
	 * Handling the spawning of the glacyte minions
	 */
	public void spawnGlacytes() {
		for (int index = 0; index < 3; index++) {
			tileLoop: for (int tileAttempt = 0; tileAttempt < 10; tileAttempt++) {
				WorldTile tile = new WorldTile(this, 2);
				if (World.isTileFree(0, tile.getX(), tile.getY(), 1)) {
					int id = 14302 + index;
					Glacyte glacyte = new Glacyte(this, id, GlacyteType.getType(id), tile, -1, true);
					getGlacytes().add(glacyte);
					break tileLoop;
				}
			}
		}
	}

	@Override
	public void sendDeath(Entity killer) {
		super.sendDeath(killer);
		spawnedGlacytes = Boolean.FALSE;
	}

	/**
	 * @return the glacytes
	 */
	public List<Glacyte> getGlacytes() {
		return glacytes;
	}

	/**
	 * The list of glacyte minions
	 */
	private List<Glacyte> glacytes = new ArrayList<Glacyte>();

	/**
	 * If we have spawned the glacytes yet.
	 */
	private boolean spawnedGlacytes = Boolean.FALSE;
}
