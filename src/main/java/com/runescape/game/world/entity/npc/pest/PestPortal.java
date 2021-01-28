package com.runescape.game.world.entity.npc.pest;

import com.runescape.game.content.global.minigames.pest.PestControl;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.combat.NPCCombatDefinitions;
import com.runescape.utility.Utils;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

@SuppressWarnings("serial")
public class PestPortal extends NPC {

	boolean isLocked;

	PestControl control;

	int ticks;

	public PestPortal(int id, boolean canbeAttackedOutOfArea, WorldTile tile, PestControl control) {
		super(id, tile, -1, canbeAttackedOutOfArea, true);
		this.control = control;
		setCantFollowUnderCombat(true);
		setForceMultiArea(true);
		setCapDamage(400);
		isLocked = true;
	}

	public boolean isLocked() {
		return isLocked;
	}

	public void unlock() {
		if (getId() >= 6146) {
			transformIntoNPC(getId() - 4);
			control.sendTeamMessage(getStringForId() + " portal shield has been dropped!");
			control.addDroppedPortal(getHiddenComponentId());
			control.sendPortalInterfaces();
		}
		this.isLocked = false;
	}

	private int getHiddenComponentId() {
		switch (getId()) {
			case 6142:
				return 18;
			case 6143:
				return 19;
			case 6144:
				return 21;
			case 6145:
				return 23;
		}
		return 0;
	}

	private String getStringForId() {
		switch (getId()) {
			case 6142:
				return "The purple, western";
			case 6143:
				return "The blue, eastern";
			case 6144:
				return "The yellow, south-eastern";
			case 6145:
				return "The red, south-western";
		}
		return "THIS SHOULDN'T EVER HAPPEN.";
	}

	private int getIndexForId() {
		switch (getId()) {
			case 6146:
			case 6142:
				return 0;
			case 6147:
			case 6143:
				return 1;
			case 6148:
			case 6144:
				return 2;
			case 6149:
			case 6145:
				return 3;
			case 3782:
			case 3784:
			case 3785:
				return 4;
		}
		return -1;
	}

	@Override
	public void processNPC() {
		super.processNPC();
		ticks++;
		if (ticks % 25 == 0) {
			if (control.createPestNPC(getIndexForId())) {
				// chance for a double spawn
				if (Utils.random(3) == 0) { control.createPestNPC(getIndexForId()); }
			}
		}
		if (isDead() || isLocked) { return; }
		cancelFaceEntityNoCheck();
	}

	@Override
	public void sendDeath(Entity source) {
		final NPCCombatDefinitions defs = getCombatDefinitions();
		resetWalkSteps();
		setNextAnimation(null);
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					setNextAnimation(new Animation(defs.getDeathEmote()));
				} else if (loop >= defs.getDeathDelay()) {
					if (control.canFinish()) {
						control.endGame();
						return;
					}
					control.unlockRandomPortal(false);
					control.getKnight().heal(500);
					finish();
					stop();
				}
				loop++;
			}
		}, 0, 1);
	}
}