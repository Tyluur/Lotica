package com.runescape.game.event.interaction.object;

import com.runescape.game.GameConstants;
import com.runescape.game.content.skills.agility.Agility;
import com.runescape.game.content.skills.magic.Magic;
import com.runescape.game.event.interaction.type.ObjectInteractionEvent;
import com.runescape.game.interaction.controllers.impl.GodWars;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.npc.godwars.Bosses;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.utility.world.ClickOption;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 27, 2015
 */
public class GodwarsObjectInteractionEvent extends ObjectInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] { 26444, 26445, 26427, 26384, 26425, 26303, 26426, 26439, 26428, 26289, 26288, 26286, 26287 };
	}

	@Override
	public boolean handleObjectInteraction(Player player, WorldObject object, ClickOption option) {
		if (handleAltars(player, object, option)) {
			return true;
		}
		Bosses door = Bosses.getBossDoor(object.getId());
		if (door != null) {
			WorldTile inside = door.getInside();
			WorldTile outside = door.getOutside();
			WorldTile destination = null;
			if (player.getWorldTile().matches(inside)) {
				destination = outside;
			} else {
				destination = inside;
			}
			if (destination == null) {
				return true;
			}
			if (destination.matches(inside)) {
				if (player.getFacade().getGwdKillcount()[door.ordinal()] < door.getKillCount()) {
					player.sendMessage("You need " + door.getKillCount() + " killcount to enter the " + door.name().toLowerCase() + " lair.", false);
					return true;
				}
				player.getFacade().getGwdKillcount()[door.ordinal()] -= door.getKillCount();
				player.getControllerManager().verifyControlerForOperation(GodWars.class).ifPresent(GodWars::updateInterface);
			}
			player.putAttribute("godwar_altar_uses", 0);
			player.setNextWorldTile(destination);
			return true;
		}
		switch (object.getId()) {
			case 26444: // sara first rock
				player.useStairs(828, new WorldTile(2914, 5300, 1), 2, 3);
				break;
			case 26445: // sara second rock
				player.useStairs(828, new WorldTile(2920, 5274, 0), 2, 3);
				break;
			case 26384: // bandos big door
				final boolean withinBandos = inBandosPrepare(player);
				if (!withinBandos) {
					player.setNextAnimation(new Animation(7002));
				}
				if (player.getSkills().getLevel(Skills.STRENGTH) < 70) {
					player.getPackets().sendGameMessage("You attempt to hit the door, but realize that you are not yet experienced enough.");
					return false;
				}
				player.setNextAnimation(new Animation(7002));
				WorldTasksManager.schedule(new WorldTask() {

					@Override
					public void run() {
						player.addWalkSteps(withinBandos ? 2851 : 2850, 5333, -1, false);
					}
				}, withinBandos ? 0 : 1);
				break;
			case 26303: // armadyl pillar
				if (player.getSkills().getLevel(Skills.RANGE) < 70) {
					player.getPackets().sendGameMessage("You attempt to grapple the pillar, but realize that you are not yet experienced enough in range.");
					return false;
				}
				final WorldTile toTile = new WorldTile(object.getX(), player.getY() == 5279 ? 5269 : 5279, player.getPlane());
				player.setNextWorldTile(toTile);
				break;
			case 26439: // zamorak bridge
				if (!Agility.hasLevel(player, 70)) {
					return false;
				}
				final boolean withinZamorak = inZamorakPrepare(player);
				final WorldTile tile = new WorldTile(2885, withinZamorak ? 5333 : 5349, 2);
				if (!withinZamorak) {
					player.getPrayer().drainPrayer();
					player.sendMessage("The extreme evil of this area leaves your prayed drained.", true);
				}
				player.setNextWorldTile(tile);
				break;
		}
		return true;
	}

	private static boolean handleAltars(Player player, WorldObject object, ClickOption option) {
		switch (object.getId()) {
			case 26286: // zamorak
			case 26287: // saradomin
			case 26288: // armadyl
			case 26289: // bandos
				switch (option) {
					case FIRST: // pray-at
						Integer altarUses = player.getAttribute("godwar_altar_uses");
						if (altarUses == null || altarUses == 0) {
							final int maxPrayer = player.getSkills().getLevelForXp(Skills.PRAYER) * 10;
							if (player.getPrayer().getPrayerpoints() < maxPrayer) {
								player.getLockManagement().lockAll(5000);
								player.getPackets().sendGameMessage("You pray to the gods...", true);
								player.setNextAnimation(new Animation(645));
								WorldTasksManager.schedule(new WorldTask() {
									@Override
									public void run() {
										player.putAttribute("godwar_altar_uses", 1);
										player.getPrayer().restorePrayer(maxPrayer);
										player.getPackets().sendGameMessage("...and recharged your prayer.", true);
									}
								}, 2);
							} else {
								player.getPackets().sendGameMessage("You already have full prayer.");
							}
						} else {
							player.sendMessage("You can only use the altar once per trip.");
						}
						break;
					case SECOND: // teleport
						Magic.sendNormalTeleportSpell(player, 0, 0, GameConstants.START_PLAYER_LOCATION);
						break;
					default:
						break;
				}
				return true;
		}
		return false;
	}

	public static boolean inZamorakPrepare(Player player) {
		return player.inArea(2884, 5343, 2890, 5352);
	}

	public static boolean inBandosPrepare(Player player) {
		return player.inArea(2823, 5313, 2850, 5432);
	}
}