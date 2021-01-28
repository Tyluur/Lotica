package com.runescape.game.interaction.controllers.impl.nmz;

import com.runescape.game.interaction.controllers.impl.nmz.powerup.NMZPowerup;
import com.runescape.game.interaction.controllers.impl.nmz.powerup.PowerupGenerator;
import com.runescape.game.interaction.controllers.impl.nmz.powerup.impl.PowerSurgePowerup;
import com.runescape.game.interaction.controllers.impl.types.NoLeaveController;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 1/8/2016
 */
public class NMZController extends NoLeaveController {

	/**
	 * The tile players are brought to after leaving
	 */
	public static final WorldTile LEAVE_TILE = new WorldTile(3094, 3510, 0);

	/**
	 * The nightmare zone instance
	 */
	private transient NMZInstance instance;

	/**
	 * The points accumulated
	 */
	private transient int points;

	/**
	 * The list of powerups active
	 */
	private transient final List<NMZPowerup> powerups = new ArrayList<>();

	@Override
	public void start() {
		instance = getArgument(0);
		player.getInterfaceManager().closeOverlay();
		player.setForceMultiArea(true);
		System.out.println("Started nmz! team=" + instance.getTeam() + ", mode=" + instance.getMode());
	}

	@Override
	public void process() {
		int interfaceId = 256;
		if (!player.getInterfaceManager().containsInterface(interfaceId)) {
			Utils.clearInterface(player, interfaceId);
			player.getInterfaceManager().sendOverlay(interfaceId);
		}
		refreshGameInterface(interfaceId);
		checkPowerups();
	}

	@Override
	public void moved() {
		if (player.hasWalkSteps()) {
			return;
		}
		Object[] array = instance.getPowerupGenerator().getPowerupAtTile(player.getWorldTile());
		if (array != null) {
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					instance.getPowerupGenerator().applyPowerupEffect(player, (NMZPowerup) array[0], (WorldObject) array[1]);
				}
			});
		}
	}

	@Override
	public boolean login() {
		instance.handlePlayerLeave(player, true);
		return true;
	}

	@Override
	public boolean logout() {
		instance.handlePlayerLeave(player, true);
		return true;
	}

	public void refreshGameInterface(int interfaceId) {
		String indent = "             ";

		player.getPackets().sendIComponentText(interfaceId, 5, "Nightmare Zone (" + Utils.formatPlayerNameForDisplay(instance.getMode().name()) + ")");

		player.getPackets().sendIComponentText(interfaceId, 11, "Party:");
		player.getPackets().sendIComponentText(interfaceId, 12, "Powerups:");
		player.getPackets().sendIComponentText(interfaceId, 13, "Points:");
		player.getPackets().sendIComponentText(interfaceId, 14, "Elapsed:");

		player.getPackets().sendIComponentText(interfaceId, 6, indent + "" + instance.getTeam().size());
		player.getPackets().sendIComponentText(interfaceId, 7, indent + "" + instance.getPowerupGenerator().getActivePowerups().size() + " (" + powerups.size() + ")");
		player.getPackets().sendIComponentText(interfaceId, 8, indent + "" + Utils.format(points));
		player.getPackets().sendIComponentText(interfaceId, 9, indent + "" + instance.getTimeElapsed());
	}

	/**
	 * Checks all the powerups active in the {@link #powerups} list and removes the ones that have been active for too
	 * long. This is base on {@code NMZPowerup#timeEffective()}
	 */
	public void checkPowerups() {
		NMZPowerup toRemove = null;
		for (NMZPowerup powerup : powerups) {
			long activatedTime = powerup.getActivatedTime(player);
			long passed = System.currentTimeMillis() - activatedTime;
			if (passed >= powerup.timeEffective()) {
				toRemove = powerup;
				break;
			}
		}
		if (toRemove != null) {
			removePowerup(toRemove);
		}
		if (hasPowerupActive(PowerupGenerator.powerupByClass(PowerSurgePowerup.class))) {
			player.getCombatDefinitions().restoreSpecialAttack(100);
		}
	}

	/**
	 * Removes the powerup from the list of active powerups for our player
	 *
	 * @param powerup
	 * 		The powerup
	 */
	private void removePowerup(NMZPowerup powerup) {
		Iterator<NMZPowerup> it$ = powerups.iterator();
		while (it$.hasNext()) {
			NMZPowerup listPowerup = it$.next();
			if (listPowerup.equals(powerup)) {
				it$.remove();
				break;
			}
		}
		powerup.onDeplete(player);
		String depletionMessage = powerup.depletionMessage();
		if (depletionMessage != null) { player.sendMessage(depletionMessage); }
	}

	public boolean hasPowerupActive(NMZPowerup powerup) {
		return powerups.contains(powerup);
	}

	@Override
	public String getLeaveAttemptMessage() {
		return "You must find another way out.";
	}

	@Override
	public void onDeath(Player player) {
		leave(player);
	}

	/**
	 * Handles when the player leaves the controller
	 *
	 * @param player
	 * 		The player
	 */
	public void leave(Player player) {
		instance.handlePlayerLeave(player, false);
	}

	/**
	 * Gets the nmz instance
	 */
	public NMZInstance getInstance() {
		return instance;
	}

	public int getPoints() {
		return points;
	}

	public void setPoints(int points) {
		this.points = points;
	}

	public List<NMZPowerup> getPowerups() {
		return powerups;
	}

}
