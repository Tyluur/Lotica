package com.runescape.game.world.entity.npc.combat;

import com.runescape.game.GameConstants;
import com.runescape.game.content.skills.Combat;
import com.runescape.game.content.skills.slayer.SlayerMonsters;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.ForceMovement;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.familiar.impl.Familiar;
import com.runescape.game.world.entity.npc.godwars.zaros.Nex;
import com.runescape.game.world.entity.npc.others.SeaTrollQueen;
import com.runescape.game.world.entity.npc.pest.ProjectilePest;
import com.runescape.game.world.entity.npc.pest.VoidKnight;
import com.runescape.utility.Utils;
import com.runescape.utility.world.map.MapAreas;

public final class NPCCombat {

	private NPC npc;

	private int combatDelay;

	private Entity target;

	public NPCCombat(NPC npc) {
		this.npc = npc;
	}

	public int getCombatDelay() {
		return combatDelay;
	}

	/*
	 * returns if under combat
	 */
	public boolean process() {
		if (combatDelay > 0) {
			combatDelay--;
		}
		if (target != null) {
			if (!checkAll()) {
				removeTarget();
				return false;
			}
			if (combatDelay <= 0) {
				combatDelay = combatAttack();
			}
			return true;
		}
		return false;
	}

	/*
	 * return combatDelay
	 */
	private int combatAttack() {
		Entity target = this.target; // prevents multithread issues
		if (target == null) {
			return 0;
		}
		// check if close to target, if not let it just walk and dont attack
		// this gameticket
		NPCCombatDefinitions defs = npc.getCombatDefinitions();
		int attackStyle = defs.getAttackStyle();
		// if hes frooze not gonna attack
		if (npc.getFreezeDelay() >= Utils.currentTimeMillis()) {
			if (attackStyle == NPCCombatDefinitions.MELEE) {
				return 0;
			}
		}

		int maxDistance = attackStyle == NPCCombatDefinitions.MELEE || attackStyle == NPCCombatDefinitions.SPECIAL2 ? 0 : 7;
		if ((!(npc instanceof Nex) && (!(npc instanceof SeaTrollQueen))) && !forceCheckClipAsRange(target) && !npc.clipedProjectile(target, maxDistance == 0)) {
			return 0;
		}
		if ((disregardsClipping() && !npc.clipedProjectile(target, maxDistance == 0 && !forceCheckClipAsRange(target))) || !Utils.isOnRange(npc, target, maxDistance
				                                                                                                                         //correct extra distance for walk. no glitches this way ^^. even if frozen
				                                                                                                                         + (npc.hasWalkSteps() && target.hasWalkSteps() ? (npc.getRun() && target.getRun() ? 2 : 1) : 0)) || (!npc.isCantFollowUnderCombat() && Utils.colides(npc, target))) {//doesnt let u attack when u under / while walking out, remove this check if u want
			return 0;
		}

		addAttackedByDelay(target);
		return CombatScriptsHandler.specialAttack(npc, target);
	}

	private boolean disregardsClipping() {return !(npc instanceof ProjectilePest);}

	protected void doDefenceEmote(Entity target) {
		/*
		 * if (target.getNextAnimation() != null) // if has att emote already
		 * return;
		 */
		target.setNextAnimationNoPriority(new Animation(Combat.getDefenceEmote(target)));
	}

	public Entity getTarget() {
		return target;
	}

	public void addAttackedByDelay(Entity target) { // prevents multithread
		// issues
		target.setAttackedBy(npc);
		target.setAttackedByDelay(Utils.currentTimeMillis() + npc.getCombatDefinitions().getAttackDelay() * 600 + 600); // 8seconds
	}

	public void setTarget(Entity target) {
		this.target = target;
		npc.setNextFaceEntity(target);
		if (!checkAll()) {
			removeTarget();
		}
	}

	public boolean checkAll() {
		Entity target = this.target; // prevents multithread issues
		if (target == null) {
			return false;
		}
		if (npc.isDead() || npc.hasFinished() || npc.isForceWalking() || target.isDead() || target.hasFinished() || npc.getPlane() != target.getPlane()) {
			return false;
		}
		if (npc.getFreezeDelay() >= Utils.currentTimeMillis()) {
			return true; // if freeze cant move ofc
		}
		if (!npc.hasCombatDefinitions()) {
			if (GameConstants.DEBUG) {
				//System.err.println(npc + " has no combat definitions. Fix this.");
			}
		}
		if (target.isNPC()) {
			NPC nTarget = target.npc();
			if (!nTarget.getDefinitions().hasAttackOption()) {
				return false;
			}
			if (!nTarget.hasCombatDefinitions()) {
				if (GameConstants.DEBUG) {
					//System.err.println(npc + " has no combat definitions. Fix this.");
				}
			}
		}
		if (target.isPlayer()) {
			if (target.player().getRandomEventManager().inRandomEvent()) {
				return false;
			}
			if (!SlayerMonsters.canAttack(target.player(), npc, false)) {
				return false;
			}
			if (target.player().invalidDueToSecurityQuestions()) {
				return false;
			}
		}
		int distanceX = npc.getX() - npc.getRespawnTile().getX();
		int distanceY = npc.getY() - npc.getRespawnTile().getY();
		int size = npc.getSize();
		int maxDistance;
		if (!npc.isNoDistanceCheck() && !npc.isCantFollowUnderCombat()) {
			maxDistance = 32;
			if (!(npc instanceof Familiar)) {

				if (npc.getMapAreaNameHash() != -1) {
					// if out his area
					if (!MapAreas.isAtArea(npc.getMapAreaNameHash(), npc) || (!npc.canBeAttackFromOutOfArea() && !MapAreas.isAtArea(npc.getMapAreaNameHash(), target))) {
						npc.forceWalkRespawnTile();
						return false;
					}
				} else if (npc.walksToRespawnTile() && distanceX > size + maxDistance || distanceX < -1 - maxDistance || distanceY > size + maxDistance || distanceY < -1 - maxDistance) {
					// if more than 64 distance from respawn place
					npc.forceWalkRespawnTile();
					return false;
				}/* else if (npc.walksToRespawnTile() && !npc.getWorldTile().withinDistance(npc.getRespawnTile(), npc.getForceWalkRespawnTileDistance())) {
					npc.forceWalkRespawnTile();
					return false;
				}*/
			}
			maxDistance = 16;
			distanceX = target.getX() - npc.getX();
			distanceY = target.getY() - npc.getY();
			if (distanceX > size + maxDistance || distanceX < -1 - maxDistance || distanceY > size + maxDistance || distanceY < -1 - maxDistance) {
				return false; // if target distance higher 16
			}
		} else {
			distanceX = target.getX() - npc.getX();
			distanceY = target.getY() - npc.getY();
		}
		// checks for no multi area :)
		if (npc instanceof Familiar) {
			Familiar familiar = (Familiar) npc;
			if (!familiar.canAttack(target)) {
				return false;
			}
		} else {
			if (!npc.isForceMultiAttacked()) {
				if (!target.isAtMultiArea() || !npc.isAtMultiArea()) {
					if (npc.getAttackedBy() != target && npc.getAttackedByDelay() > Utils.currentTimeMillis()) {
						return false;
					}
					if (target.getAttackedBy() != npc && target.getAttackedByDelay() > Utils.currentTimeMillis()) {
						return false;
					}
				}
			}
		}
		if (!npc.isCantFollowUnderCombat()) {
			// if is under
			int targetSize = target.getSize();
			if (distanceX < size && distanceX > -targetSize && distanceY < size && distanceY > -targetSize && !target.hasWalkSteps()) {

				/*
				 * System.out.println(size + maxDistance); System.out.println(-1
				 * - maxDistance);
				 */
				npc.resetWalkSteps();
				if (!npc.addWalkSteps(target.getX() + 1, npc.getY())) {
					npc.resetWalkSteps();
					if (!npc.addWalkSteps(target.getX() - size, npc.getY())) {
						npc.resetWalkSteps();
						if (!npc.addWalkSteps(npc.getX(), target.getY() + 1)) {
							npc.resetWalkSteps();
							if (!npc.addWalkSteps(npc.getX(), target.getY() - size)) {
								return true;
							}
						}
					}
				}
				return true;
			}
			if (npc.getCombatDefinitions().getAttackStyle() == NPCCombatDefinitions.MELEE && targetSize == 1 && size == 1 && Math.abs(npc.getX() - target.getX()) == 1 && Math.abs(npc.getY() - target.getY()) == 1 && !target.hasWalkSteps()) {

				if (!npc.addWalkSteps(target.getX(), npc.getY(), 1)) {
					npc.addWalkSteps(npc.getX(), target.getY(), 1);
				}
				return true;
			}

			int attackStyle = npc.getCombatDefinitions().getAttackStyle();
			if (npc instanceof SeaTrollQueen) {
				return true;
			} else if (npc instanceof Nex) {
				Nex nex = (Nex) npc;
				maxDistance = nex.isFollowTarget() ? 0 : 7;
				if (nex.getFlyTime() == 0 && (distanceX > size + maxDistance || distanceX < -1 - maxDistance || distanceY > size + maxDistance || distanceY < -1 - maxDistance)) {
					npc.resetWalkSteps();
					npc.addWalkStepsInteract(target.getX(), target.getY(), 2, size, true);
					if (!npc.hasWalkSteps()) {
						int[][] dirs = Utils.getCoordOffsetsNear(size);
						for (int dir = 0; dir < dirs[0].length; dir++) {
							final WorldTile tile = new WorldTile(new WorldTile(target.getX() + dirs[0][dir], target.getY() + dirs[1][dir], target.getPlane()));
							if (World.canMoveNPC(tile.getPlane(), tile.getX(), tile.getY(), size)) { // if
								// found
								// done
								nex.setFlyTime(4);
								npc.setNextForceMovement(new ForceMovement(new WorldTile(npc), 0, tile, 1, Utils.getMoveDirection(tile.getX() - npc.getX(), tile.getY() - npc.getY())));
								npc.setNextAnimation(new Animation(6985));
								npc.setNextWorldTile(tile);
								return true;
							}
						}
					}
					return true;
				} else
				// if doesnt need to move more stop moving
				{
					npc.resetWalkSteps();
				}
			} else {
				maxDistance = npc.isForceFollowClose() ? 0 : (attackStyle == NPCCombatDefinitions.MELEE || attackStyle == NPCCombatDefinitions.SPECIAL2) ? 0 : 9;
				npc.resetWalkSteps();
				// is far from target, moves to it till can attack
				if ((!npc.clipedProjectile(target, maxDistance == 0 && !forceCheckClipAsRange(target))) || !Utils.isOnRange(npc.getX(), npc.getY(), size, target.getX(), target.getY(), targetSize, maxDistance)) {
					npc.calcFollow(target, npc.getRun() ? 2 : 1, true, npc.isIntelligentRouteFinder());
					return true;
				}
			}
		}
		return true;
	}

	private boolean forceCheckClipAsRange(Entity target) {
		return target instanceof VoidKnight;
	}

	public void addCombatDelay(int delay) {
		combatDelay += delay;
	}

	public void setCombatDelay(int delay) {
		combatDelay = delay;
	}

	public boolean underCombat() {
		return target != null;
	}

	public void removeTarget() {
		this.target = null;
		npc.setNextFaceEntity(null);
	}

	public void reset() {
		combatDelay = 0;
		target = null;
	}

}
