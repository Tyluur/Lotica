package com.runescape.game.content.global.minigames.clanwars;

import com.runescape.game.interaction.controllers.Controller;
import com.runescape.game.interaction.controllers.impl.Wilderness;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.player.Player;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

/**
 * Handles the FFA Clan Wars zone.
 * @author Emperor
 *
 */
public final class FfaZone extends Controller {

	/**
	 * If the FFA zone is the risk zone.
	 */
	private boolean risk;
	
	/**
	 * If the player was in the ffa pvp area.
	 */
	private transient boolean wasInArea;
	
	@Override
	public void start() {
		if (getArguments() == null || getArguments().length < 1) {
			this.risk = player.getX() >= 2948 && player.getY() >= 5508 && player.getX() <= 3071 && player.getY() <= 5631;
		} else {
			this.risk = (Boolean) getArguments()[0];
		}
		player.getInterfaceManager().sendTab(player.getInterfaceManager().onResizable() ? 11 : 27, 789);
		moved();
	}
	
	@Override
	public boolean sendDeath() {
		player.getLockManagement().lockAll(7000);
		player.stopAll();
		WorldTasksManager.schedule(new WorldTask() {
			int loop;
			@Override
			public void run() {
				if (loop == 0) {
					player.setNextAnimation(new Animation(836));
				} else if (loop == 1) {
					player.getPackets().sendGameMessage("Oh dear, you have died.");
				} else if (loop == 3) {
					if (risk) {
						Player killer = player.getMostDamageReceivedSourcePlayer();
						if (killer != null) {
							killer.removeDamage(player);
							killer.increaseKillCount(player);
							player.sendItemsOnDeath(killer);
						}
						player.getEquipment().init();
						player.getInventory().init();
					}
					player.setNextWorldTile(new WorldTile(2993, 9679, 0));
					player.getControllerManager().startController("clan_wars_request");
					player.reset();
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
	public void magicTeleported(int type) {
		player.getControllerManager().forceStop();
	}
	
	@Override
	public boolean processObjectClick1(WorldObject object) {
		switch (object.getId()) {
		case 38700:
			player.setNextWorldTile(player.removeAttribute("entered_portal_via_home", false) ? new WorldTile(3080, 3486, 0) : new WorldTile(2993, 9679, 0));
			player.getControllerManager().startController("clan_wars_request");
			return false;
		}
		return true;
	}
	
	@Override
	public void moved() {
		boolean inArea = inPvpArea(player);
		if (inArea && !wasInArea) {
			player.getPackets().sendPlayerOption("Attack", 1, true);
			player.setCanPvp(true);
			wasInArea = true;
			if (risk) {
				player.setWildernessSkull();
			}
			Wilderness.checkBoosts(player);
			Wilderness.checkLentItems(player);
		} else if (!inArea && wasInArea) {
			player.getPackets().sendPlayerOption("null", 1, true);
			player.setCanPvp(false);
			wasInArea = false;
		}
	}
	
	@Override
	public boolean keepCombating(Entity victim) {
		return !(victim instanceof Player) || player.isCanPvp() && ((Player) victim).isCanPvp();
	}
	
	@Override
	public void forceClose() {
		player.setCanPvp(false);
		player.getPackets().sendPlayerOption("null", 1, true);
		boolean resized = player.getInterfaceManager().onResizable();
		player.getPackets().closeInterface(resized ? 746 : 548, resized ? 11 : 27);
	}
	
	@Override
	public boolean logout() {
		return false;
	}
	
	/**
	 * Checks if the location is in a ffa pvp zone.
	 * @param t The world tile.
	 * @return {@code True} if so.
	 */
	public static boolean inPvpArea(WorldTile t) {
		return (t.getX() >= 2948 && t.getY() >= 5512 && t.getX() <= 3071 && t.getY() <= 5631)		//Risk area.
				|| (t.getX() >= 2756 && t.getY() >= 5512 && t.getX() <= 2879 && t.getY() <= 5631);	//Safe area.
	}
	
	/**
	 * Checks if the location is in a ffa zone.
	 * @param t The world tile.
	 * @return {@code True} if so.
	 */
	public static boolean inArea(WorldTile t) {
		return (t.getX() >= 2948 && t.getY() >= 5508 && t.getX() <= 3071 && t.getY() <= 5631)		//Risk area.
				|| (t.getX() >= 2756 && t.getY() >= 5508 && t.getX() <= 2879 && t.getY() <= 5631);	//Safe area.
	}

	/**
	 * Checks if a player's overload effect is changed (due to being in the risk ffa zone, in pvp)
	 * @param player The player.
	 * @return {@code True} if so.
	 */
	public static boolean isOverloadChanged(Player player) {
		if (!(player.getControllerManager().getController() instanceof FfaZone)) {
			return false;
		}
		return player.isCanPvp() && ((FfaZone) player.getControllerManager().getController()).risk;
	}
}