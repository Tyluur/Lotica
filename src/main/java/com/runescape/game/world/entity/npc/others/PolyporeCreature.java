package com.runescape.game.world.entity.npc.others;

import com.runescape.game.world.World;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.*;
import com.runescape.game.world.entity.masks.Hit.HitLook;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.RouteEvent;
import com.runescape.utility.Utils;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

import java.util.List;

@SuppressWarnings("serial")
public class PolyporeCreature extends NPC {

	private int realId;

	public PolyporeCreature(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		realId = id;
		setWalkType(id == 14694 ? NORMAL_WALK | FLY_WALK : id == 14698 ? 0 : NORMAL_WALK);
		if (id == 14698) {
			setCantFollowUnderCombat(true);
		}
	}

	@Override
	public void processMovement() {
		super.processMovement();
		if (realId == 14696 && getNextWalkDirection() != -1) {
			this.setNextAnimation(new Animation(15465));
			this.setNextForceMovement(new NewForceMovement(this, 1, null, 0, Utils.getFaceDirection(Utils.DIRECTION_DELTA_X[getNextWalkDirection()], Utils.DIRECTION_DELTA_Y[getNextWalkDirection()])));
		}
	}

	public static void useStairs(final Player player, WorldTile tile, final boolean down) {
		player.useStairs(down ? 15458 : 15456, tile, 2, 3); //TODO find correct emote
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				player.setNextAnimation(new Animation(down ? 15459 : 15457));
			}
		}, 1);
	}

	public static void sprinkleOil(final Player player, NPC target) {
		if (target == null) {
			for (int regionId : player.getMapRegionsIds()) {
				List<Integer> npcIndexes = World.getRegion(regionId).getNPCsIndexes();
				if (npcIndexes != null) {
					for (int npcIndex : npcIndexes) {
						final NPC n = World.getNPCs().get(npcIndex);
						if (n == null || n.isDead() || n.hasFinished() || n.getPlane() != player.getPlane() || n.getCombat().getTarget() != player || !(n instanceof PolyporeCreature) || ((PolyporeCreature) n).realId != n.getId()) {
							continue;
						}
						player.setRouteEvent(new RouteEvent(n, new Runnable() {
							@Override
							public void run() {
								player.faceEntity(n);
								sprinkleOil(player, n);
								return;
							}
						}, false));
						return;
					}
				}
			}
			player.getPackets().sendGameMessage("There are no suitable targets nearby.");
			return;
		} else {
			if (!(target instanceof PolyporeCreature) || target.isDead() || target.hasFinished()) {
				return;
			}
			if (((PolyporeCreature) target).realId != target.getId()) {
				player.getPackets().sendGameMessage("That creature has already been weakened.");
				return;
			}
		}
		((PolyporeCreature) target).useOil(player);
	}

	public void useOil(Player player) {
		if (getId() != realId) {
			return;
		}
		player.getLockManagement().lockAll(1000);
		player.setNextGraphics(new Graphics(2014));
		player.setNextAnimation(new Animation(9954));
		transformIntoNPC(realId + 1);
		setNextForceTalk(new ForceTalk("Ssss!"));
		if (getCombat().getTarget() == null) {
			setTarget(player);
		}
		//NeemDrupeSqueeze.removeCharge(player);
		//TODO find emote and message
	}

	@Override
	public void reset() {
		setNPC(realId);
		super.reset();
	}

	public boolean canInfect() {
		return realId == getId();
	}

	@Override
	public void handleIngoingHit(final Hit hit) {
		if (hit.getLook() == HitLook.MELEE_DAMAGE || hit.getLook() == HitLook.RANGE_DAMAGE) {
			hit.setDamage(hit.getDamage() / 5);
		}
		super.handleIngoingHit(hit);
	}

	public int getInfectEmote() {
		switch (realId) {
			case 14688:
				return 15484;
			case 14690:
				return 15507;
			case 14692:
				return 15514;
			case 14696:
				return 15466;
			case 14698:
				return 15477;
			case 14700:
				return 15492;
			default:
				return -1;
		}

	}

}
