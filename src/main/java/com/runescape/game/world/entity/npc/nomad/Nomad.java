package com.runescape.game.world.entity.npc.nomad;

import com.runescape.game.content.FadingScreen;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.ForceTalk;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.masks.Hit;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.familiar.impl.Familiar;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class Nomad extends NPC {

	private int nextMove;
	private long nextMovePerform;
	private WorldTile throneTile;
	private ArrayList<NPC> copies;
	private boolean healed;
	private int notAttacked;
	private Player target;

	public Nomad(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setForceMultiArea(true);
		setRun(true);
		setCapDamage(750);
		setForceTargetDistance(5);
		setNextMovePerform();
		setWalkType(NPC.NORMAL_WALK);
	}

	public void setTarget(Player player) {
		target = player;
		super.setTarget(player);
	}

	public void setNextMovePerform() {
		nextMovePerform = Utils.currentTimeMillis() + Utils.random(20000, 30000);
	}

	public boolean isMeleeMode() {
		return nextMove == -1;
	}

	public void setMeleeMode() {
		nextMove = -1;
		setForceFollowClose(true);
	}

	@Override
	public void reset() {
		notAttacked = 0;
		if (nextMove == -1) {
			nextMove = 0;
			setForceFollowClose(false);
		}
		healed = false;
		if (copies != null)
			destroyCopies();
		setNextMovePerform();
		super.reset();

	}

	@Override
	public void sendDeath(Entity source) {
		if (throneTile != null) {
			target.getLockManagement().lockAll();
			target.getPackets().sendConfigByFile(6962, 0);
			Dialogue.sendNPCDialogueNoContinue(target, getId(), 9802, "You...<br>You have doomed this world.");
			target.getPackets().sendVoice(8260);
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					Dialogue.closeNoContinueDialogue(target);
					FadingScreen.fade(target, () -> {
						target.getControllerManager().forceStop();
						target.getLockManagement().unlockAll();
					});
				}
			}, getCombatDefinitions().getAttackDelay() + 1);
		}
		super.sendDeath(source);
	}

	@Override
	public void processNPC() {
		Entity target = getCombat().getTarget();
		if (target instanceof Player && !clipedProjectile(target, false)) {
			notAttacked++;
			if (notAttacked == 10) {
				if (copies != null) {
					destroyCopies();
					notAttacked = 0;
					return;
				}
				final Player player = (Player) target;
				setNextForceTalk(new ForceTalk("Face me!"));
				player.getPackets().sendVoice(7992);
			} else if (notAttacked == 20) {
				final Player player = (Player) target;
				setNextForceTalk(new ForceTalk("Coward."));
				player.getPackets().sendVoice(8055);
				reset();
				setNextFaceEntity(null);
				sendTeleport(getThroneTile());
			}
		} else if (target instanceof Familiar && this.target != null)
			super.setTarget(this.target);
		else
			notAttacked = 0;
		super.processNPC();

	}

	// 0 mines
	// 1 wrath
	// 2 multiplies
	// 3 k0 move
	public int getNextMove() {
		if (nextMove == 4)
			nextMove = 0;
		return nextMove++;
	}

	public void sendTeleport(final WorldTile tile) {
		setNextAnimation(new Animation(12729));
		setNextGraphics(new Graphics(1576));
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				setNextWorldTile(tile);
				setNextAnimation(new Animation(12730));
				setNextGraphics(new Graphics(1577));
				setDirection(6);
			}
		}, 3);
	}

	public boolean useSpecialSpecialMove() {
		return Utils.currentTimeMillis() > nextMovePerform;
	}

	public void setNextMove(int nextMove) {
		this.nextMove = nextMove;
	}

	public WorldTile getThroneTile() {
		/*
		 * if no throne returns middle of area
		 */
		return throneTile == null ? new WorldTile((getRegionX() << 6) + 32, (getRegionY() << 6) + 32, getPlane()) : throneTile;
	}

	public void setThroneTile(WorldTile throneTile) {
		this.throneTile = throneTile;
	}

	public void createCopies(final Player target) {
		setNextAnimation(new Animation(12729));
		setNextGraphics(new Graphics(1576));
		final int thisIndex = Utils.random(4);
		final Nomad thisNpc = this;
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				copies = new ArrayList<NPC>();
				transformIntoNPC(8529);
				for (int i = 0; i < 4; i++) {
					NPC n;
					if (thisIndex == i) {
						n = thisNpc;
						setNextWorldTile(getCopySpot(i));
					} else {
						n = new FakeNomad(getCopySpot(i), thisNpc);
						copies.add(n);
					}
					n.setCantFollowUnderCombat(true);
					n.setNextAnimation(new Animation(12730));
					n.setNextGraphics(new Graphics(1577));
					n.setTarget(target);
				}

			}
		}, 3);
	}

	public WorldTile getCopySpot(int index) {
		WorldTile throneTile = getThroneTile();
		switch (index) {
		case 0:
			return throneTile;
		case 1:
			return throneTile.transform(-3, -3, 0);
		case 2:
			return throneTile.transform(3, -3, 0);
		case 3:
		default:
			return throneTile.transform(0, -6, 0);
		}

	}

	public void destroyCopy(NPC copy) {
		copy.finish();
		if (copies == null)
			return;
		copies.remove(copy);
		if (copies.isEmpty())
			destroyCopies();
	}

	public void destroyCopies() {
		transformIntoNPC(8528);
		setCantFollowUnderCombat(false);
		setNextMovePerform();
		if (copies == null)
			return;
		for (NPC n : copies)
			n.finish();
		copies = null;
	}

	@Override
	public void handleIngoingHit(Hit hit) {
		if (getId() == 8529)
			destroyCopies();
		super.handleIngoingHit(hit);
	}

	public boolean isHealed() {
		return healed;
	}

	public void setHealed(boolean healed) {
		this.healed = healed;
	}

}
