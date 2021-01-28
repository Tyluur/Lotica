package com.runescape.game.world.entity.player;

import com.runescape.game.world.entity.player.LockManagement.LockType;
import com.runescape.game.world.entity.player.actions.Action;

public final class ActionManager {

	private Player player;

	private Action action;

	private Action lastAction;

	private int actionDelay;

	public ActionManager(Player player) {
		this.player = player;
	}

	public void process() {
		if (player.getLockManagement().isLocked(LockType.ACTION)) {
			return;
		}
		if (action != null) {
			if (player.isDead()) {
				forceStop();
			} else if (!action.process(player)) {
				forceStop();
			}
		}
		if (actionDelay > 0) {
			actionDelay--;
			return;
		}
		if (action == null) { return; }
		int delay = action.processWithDelay(player);
		if (delay == -1) {
			forceStop();
			return;
		}
		actionDelay += delay;
	}

	public boolean setAction(Action action) {
		forceStop();
		if (!action.start(player)) { return false; }
		this.lastAction = this.action = action;
		return true;
	}

	public Action getLastAction() {
		return lastAction;
	}

	public void forceStop() {
		if (action == null) { return; }
		action.stop(player);
		action = null;
	}

	public int getActionDelay() {
		return actionDelay;
	}

	public void addActionDelay(int skillDelay) {
		this.actionDelay += skillDelay;
	}

	public void setActionDelay(int skillDelay) {
		this.actionDelay = skillDelay;
	}

	public boolean hasSkillWorking() {
		return action != null;
	}

	public Action getAction() {
		return action;
	}
}
