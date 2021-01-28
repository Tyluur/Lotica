package com.runescape.game.interaction.controllers.impl.pestcontrol;

import com.runescape.game.content.global.minigames.pest.Lander;
import com.runescape.game.content.global.minigames.pest.PestControl;
import com.runescape.game.interaction.controllers.Controller;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.utility.Utils;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

public class PestControlGame extends Controller {

	private transient PestControl control;

	private double points;

	@Override
	public void start() {
		control = (PestControl) getArguments()[0];
		setArguments(null);
		setPoints(0.0D);
		sendInterfaces();
		player.setForceMultiArea(true);
	}

	@Override
	public void sendInterfaces() {
		updatePestPoints();
		player.getInterfaceManager().sendOverlay(408);
	}

	private void updatePestPoints() {
		boolean isGreen = getPoints() > 750;
		player.getPackets().sendIComponentText(408, 11, (isGreen ? "<col=75AE49>" : "") + (int) getPoints() + "</col>");
	}

	@Override
	public void forceClose() {
		if (control != null) {
			if (control.getPlayers().contains(player)) { control.getPlayers().remove(player); }
			player.useStairs(-1, Lander.getLanders()[control.getPestData().ordinal()].getLanderRequierment().getExitTile(), 1, 2);
		} else { player.useStairs(-1, new WorldTile(2657, 2639, 0), 1, 2); }
		player.setForceMultiArea(false);
		player.getInterfaceManager().closeOverlay();
		player.reset();
	}

	@Override
	public void magicTeleported(int teleType) {
		player.getControllerManager().forceStop();
	}

	@Override
	public boolean processMagicTeleport(WorldTile toTile) {
		player.getDialogueManager().startDialogue("SimpleMessage", "You can't leave the pest control area like this.");
		return false;
	}

	@Override
	public boolean processItemTeleport(WorldTile toTile) {
		player.getDialogueManager().startDialogue("SimpleMessage", "You can't leave the pest control area like this.");
		return false;
	}

	@Override
	public boolean canMove(int dir) {
		WorldTile toTile = new WorldTile(player.getX() + Utils.DIRECTION_DELTA_X[dir], player.getY() + Utils.DIRECTION_DELTA_Y[dir], player.getPlane());
		return !control.isBrawlerAt(toTile);
	}

	@Override
	public boolean login() {
		return true;
	}

	@Override
	public boolean logout() {
		if (control != null) { control.getPlayers().remove(player); }
		return false;
	}

	@Override
	public boolean canSummonFamiliar() {
		player.getPackets().sendGameMessage("You feel it's best to keep your Familiar away during this game.");
		return false;
	}

	@Override
	public boolean sendDeath() {
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					player.setNextAnimation(new Animation(836));
				} else if (loop == 1) {
					player.getPackets().sendGameMessage("Oh dear, you have died.");
				} else if (loop == 3) {
					player.reset();
					player.setNextWorldTile(control.getWorldTile(35 - Utils.random(4), 54 - (Utils.random(3))));
					player.setNextAnimation(new Animation(-1));
				} else if (loop == 4) {
					player.getPackets().sendMusicEffect(90);
					stop();
				}
				loop++;
			}
		}, 0, 1);
		return false;
	}

	@Override
	public void trackXP(int skillId, double addedXp) {
		if (skillId == 3) // hp
		{ setPoints(getPoints() + ((addedXp / 20) * 2.5)); }
		updatePestPoints();
	}

	public double getPoints() {
		return points;
	}

	public void setPoints(double points) {
		this.points = points;
	}
}
