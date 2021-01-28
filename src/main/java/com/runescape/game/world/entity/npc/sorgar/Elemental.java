package com.runescape.game.world.entity.npc.sorgar;

import com.runescape.game.content.FadingScreen;
import com.runescape.game.interaction.controllers.impl.SorceressGarden;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class Elemental extends NPC {
	
	private boolean beingTeleported = false;
	
	private Player player;

	private static final WorldTile[][] tiles = { 
		{new WorldTile(2908, 5460, 0), new WorldTile(2898, 5460, 0)}, 
		{new WorldTile(2900, 5448, 0), new WorldTile(2900, 5455, 0)},
		{new WorldTile(2905, 5449, 0), new WorldTile(2899, 5449, 0)},
		{new WorldTile(2903, 5451, 0), new WorldTile(2903, 5455, 0), new WorldTile(2905, 5455, 0), new WorldTile(2905, 5451, 0)},
		{new WorldTile(2903, 5457, 0), new WorldTile(2917, 5457, 0)},
		{new WorldTile(2908, 5455, 0), new WorldTile(2917, 5455, 0)},
		{new WorldTile(2922, 5471, 0), new WorldTile(2922, 5459, 0)},
		{new WorldTile(2924, 5463, 0), new WorldTile(2928, 5463, 0), new WorldTile(2928, 5461, 0), new WorldTile(2924, 5461, 0)},
		{new WorldTile(2924, 5461, 0), new WorldTile(2926, 5461, 0), new WorldTile(2926, 5458, 0), new WorldTile(2924, 5458, 0)},
		{new WorldTile(2928, 5458, 0), new WorldTile(2928, 5460, 0), new WorldTile(2934, 5460, 0), new WorldTile(2934, 5458, 0)},
		{new WorldTile(2931, 5477, 0), new WorldTile(2931, 5470, 0)},
		{new WorldTile(2935, 5469, 0), new WorldTile(2928, 5469, 0)},
		{new WorldTile(2925, 5464, 0), new WorldTile(2925, 5475, 0)},
		{new WorldTile(2931, 5477, 0), new WorldTile(2931, 5470, 0)},
		{new WorldTile(2907, 5488, 0), new WorldTile(2907, 5482, 0)},
		{new WorldTile(2907, 5490, 0), new WorldTile(2907, 5495, 0)},
		{new WorldTile(2910, 5493, 0), new WorldTile(2910, 5487, 0)},
		{new WorldTile(2918, 5483, 0), new WorldTile(2918, 5485, 0), new WorldTile(2915, 5485, 0), new WorldTile(2915, 5483, 0), 
			new WorldTile(2912, 5483, 0), new WorldTile(2912, 5485, 0), new WorldTile(2915, 5485, 0), new WorldTile(2915, 5483, 0)},
			{new WorldTile(2921, 5486, 0), new WorldTile(2923, 5486, 0), new WorldTile(2923, 5490, 0), new WorldTile(2923, 5486, 0)},
			{new WorldTile(2921, 5491, 0), new WorldTile(2923, 5491, 0), new WorldTile(2923, 5495, 0), new WorldTile(2921, 5495, 0)},
			{new WorldTile(2899, 5466, 0), new WorldTile(2899, 5468, 0), new WorldTile(2897, 5468, 0), new WorldTile(2897, 5466, 0), 
				new WorldTile(2897, 5468, 0), new WorldTile(2899, 5468, 0)},
				{new WorldTile(2897, 5470, 0), new WorldTile(2891, 5470, 0)},
				{new WorldTile(2897, 5471, 0), new WorldTile(2899, 5471, 0), new WorldTile(2899, 5478, 0), new WorldTile(2897, 5478, 0)},
				{new WorldTile(2896, 5483, 0), new WorldTile(2900, 5483, 0), new WorldTile(2900, 5480, 0), new WorldTile(2897, 5480, 0), 
					new WorldTile(2896, 5482, 0)},
					{new WorldTile(2896, 5483, 0), new WorldTile(2896, 5481, 0), new WorldTile(2891, 5481, 0), new WorldTile(2891, 5483, 0)},
					{new WorldTile(2889, 5485, 0), new WorldTile(2900, 5485, 0)}	
	};

	/**
	 * 
	 * @param id NPC id
	 * @param tile WorldTile
	 * @param mapAreaNameHash -1
	 * @param canBeAttackFromOutOfArea true
	 * @param spawned false
	 */
	public Elemental(int id, WorldTile tile, int mapAreaNameHash,
			boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setCantFollowUnderCombat(true);
		setCantInteract(true);
	}

	@Override
	public ArrayList<Entity> getPossibleTargets() {
		ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
		for (int regionId : getMapRegionsIds()) {
			List<Integer> playerIndexes = World.getRegion(regionId)
					.getPlayerIndexes();
			if (playerIndexes != null) {
				for (int playerIndex : playerIndexes) {
					Player player = World.getPlayers().get(playerIndex);
					if (player == null || player.isDead() || player.hasFinished() || !SorceressGarden.inGardens(player))
						continue;
					possibleTarget.add(player);
				}
			}
		}
		return possibleTarget;
	}
	
	public boolean canBeTeleported(Entity t) {
		final int NORTH = 1, SOUTH = 6, WEST = 3, EAST = 4;
		return getNextWalkDirection() == EAST && (t.getX() - getX()) < 4 && (t.getY() - getY()) == 0;
	}
	
	private int steps;

	@Override
	public void processNPC() {
		super.processNPC();
		int index = getId() - 5533;
		if (!isForceWalking()) {
			if (steps >= tiles[index].length)
				steps = 0;
			setForceWalk(tiles[index][steps]);
			if (withinDistance(tiles[index][steps], 0))
				steps++;
		}
		for (Entity t : getPossibleTargets()) {
			if (player == null)
				player = (Player) t;
			if (getId() == 5533)
				System.out.println((player.getX() - getX())+" "+(t.getY() - getY()));
			if (withinDistance(player, 1) && canBeTeleported(player)) {
				if (!beingTeleported) {
					beingTeleported = true;
					setNextAnimation(new Animation(5803));
					player.setNextGraphics(new Graphics(110, 0, 100));
					player.reset();
					player.stopAll();
					WorldTasksManager.schedule(new WorldTask() {
						int i = 0;
						@Override
						public void run() {
							if (i == 0) {
								FadingScreen.fade(player, new Runnable() {
									@Override
									public void run() {
										player.getPackets().sendBlackOut(0);
										player.getPackets().sendConfig(1241, 0);
									}

								});
							} else if (i == 2) {
								player.getPackets().sendGameMessage("You've been spotted by an elemental and teleported out of its garden.");
								player.setNextWorldTile(
										SorceressGarden.inAutumnGarden(player) ? new WorldTile(2913, 5467, 0) : 
										(SorceressGarden.inSpringGarden(player) ? new WorldTile(2916, 5473, 0) :
										(SorceressGarden.inSummerGarden(player) ? new WorldTile(2910, 5476, 0) : 
											new WorldTile(2906, 5470, 0))));
								beingTeleported = false;
								stop();
							}
							i++;
						}
					}, 0, 1);
				}
			}
		}
	}
}
