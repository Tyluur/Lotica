package com.runescape.game.world.entity.npc.pest;

import com.runescape.game.content.global.minigames.pest.PestControl;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class PestMonsters extends NPC {

	protected PestControl manager;

	protected int portalIndex;

	public PestMonsters(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned, int index, PestControl manager) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		this.manager = manager;
		this.portalIndex = index;
		setForceMultiArea(true);
		setForceAgressive(true);
		setForceTargetDistance(70);
	}

	@Override
	public void processNPC() {
		super.processNPC();
		if (!getCombat().underCombat()) { checkAggressivity(); }
	}

	@Override
	public ArrayList<Entity> getPossibleTargets() {
		ArrayList<Entity> possibleTarget = new ArrayList<>();
		List<Integer> playerIndexes = World.getRegion(getRegionId()).getPlayerIndexes();
		if (playerIndexes != null) {
			for (int playerIndex : playerIndexes) {
				Player player = World.getPlayers().get(playerIndex);
				if (player == null || player.isDead() || player.hasFinished() || !player.isRunning() || !player.withinDistance(this, 10)) {
					continue;
				}
				possibleTarget.add(player);
			}
		}
		if (possibleTarget.isEmpty() || Utils.random(2) == 0) {
			possibleTarget.clear();
			if (manager.getKnight() != null) { possibleTarget.add(manager.getKnight()); }
		}
		return possibleTarget;
	}

	@Override
	public void sendDeath(Entity source) {
		super.sendDeath(source);
		manager.getPestCounts()[portalIndex]--;
	}
}
