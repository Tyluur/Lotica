package com.runescape.game.content.skills.agility;

import com.runescape.game.interaction.controllers.impl.Wilderness;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.ForceMovement;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.game.world.entity.player.achievements.AchievementHandler;
import com.runescape.game.world.entity.player.achievements.medium.Wilderness_Laps;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

public class WildernessAgility {

	public static void swingOnRopeSwing(final Player player, WorldObject object) {
		if (!Agility.hasLevel(player, 52)) {
			return;
		} else if (player.getY() != 3953) {
			player.getPackets().sendGameMessage("You'll need to get closer to make this jump.");
			return;
		}
		player.getLockManagement().lockAll(4000);
		player.setNextAnimation(new Animation(751));
		World.sendObjectAnimation(player, object, new Animation(497));
		final WorldTile toTile = new WorldTile(object.getX(), 3958, object.getPlane());
		player.setNextForceMovement(new ForceMovement(player, 1, toTile, 3, ForceMovement.NORTH));
		player.getSkills().addXp(Skills.AGILITY, 20);
		player.getPackets().sendGameMessage("You skillfully swing across.", true);
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				player.setNextWorldTile(toTile);
				if (getStage(player) != 1) {
					removeStage(player);
				} else {
					setStage(player, 2);
				}
			}
		}, 1);
	}

	public static void walkAcrossLogBalance(final Player player, final WorldObject object) {
		if (!Agility.hasLevel(player, 52)) {
			return;
		}
		if (player.getY() != object.getY()) {
			player.addWalkSteps(3001, 3945, -1, false);
			player.getLockManagement().lockAll(2000);
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					walkAcrossLogBalanceEnd(player, object);
				}
			}, 1);
		} else {
			walkAcrossLogBalanceEnd(player, object);
		}
	}

	private static void walkAcrossLogBalanceEnd(final Player player, WorldObject object) {
		player.getPackets().sendGameMessage("You walk carefully across the slippery log...", true);
		player.getLockManagement().lockAll(1700);
		player.setNextAnimation(new Animation(9908));
		final WorldTile toTile = new WorldTile(2994, object.getY(), object.getPlane());
		player.setNextForceMovement(new ForceMovement(toTile, 10, ForceMovement.WEST));
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				player.setNextAnimation(new Animation(-1));
				player.setNextWorldTile(toTile);
				player.getSkills().addXp(Skills.AGILITY, 20);
				player.getPackets().sendGameMessage("... and make it safely to the other side.", true);
				if (getStage(player) != 3) {
					removeStage(player);
				} else {
					setStage(player, 4);
				}
			}
		}, 9);
	}

	public static void jumpSteppingStones(final Player player, final WorldObject object) {
		if (player.getY() != object.getY()) {
			return;
		}
		player.getLockManagement().lockAll(6000);
		WorldTasksManager.schedule(new WorldTask() {

			int x;

			@Override
			public void run() {
				if (x++ == 6) {
					stop();
					return;
				}
				final WorldTile toTile = new WorldTile(3002 - x, player.getY(), player.getPlane());
				player.setNextForceMovement(new ForceMovement(toTile, 1, ForceMovement.WEST));
				player.setNextAnimation(new Animation(741));
				WorldTasksManager.schedule(new WorldTask() {

					@Override
					public void run() {
						player.setNextWorldTile(toTile);
					}
				}, 0);
			}
		}, 2, 1);
		player.getSkills().addXp(Skills.AGILITY, 20);
		if (getStage(player) != 2) {
			removeStage(player);
		} else {
			setStage(player, 3);
		}
	}

	public static void climbUpWall(final Player player, WorldObject object) {
		if (!Agility.hasLevel(player, 52)) {
			return;
		}
		player.useStairs(3378, new WorldTile(2994, 3932, 0), 7, 9);
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				if (getStage(player) != 4) {
					removeStage(player);
				} else {
					AchievementHandler.incrementProgress(player, Wilderness_Laps.class, 1);
					player.getInventory().addItem(Wilderness.WILDERNESS_TOKEN, 5);
					player.getSkills().addXp(Skills.AGILITY, 498.9);
					setStage(player, 0);
				}
			}
		}, 8);
	}

	public static void enterWildernessCourse(final Player player) {
		if (!Agility.hasLevel(player, 52)) {
			return;
		}
		WorldObject firstGate = new WorldObject(65365, 10, 1, 2998, 3916, 0);
		final WorldObject secondGate = new WorldObject(65367, 10, 1, 2998, 3930, 0);
		player.setNextWorldTile(new WorldTile(firstGate.getX(), firstGate.getY() + 1, 0));
		player.setNextForceMovement(new ForceMovement(secondGate, 8, ForceMovement.NORTH));
		player.setNextAnimation(new Animation(9908));
		player.getLockManagement().lockAll();
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				player.setNextWorldTile(secondGate);
				player.setNextAnimation(new Animation(-1));
				WorldTasksManager.schedule(new WorldTask() {

					@Override
					public void run() {
						player.getLockManagement().unlockAll();
						player.setNextWorldTile(new WorldTile(secondGate.getX(), secondGate.getY() + 1, 0));
					}
				});
			}
		}, 7);
	}

	public static void exitWildernessCourse(final Player player) {
		if (!Agility.hasLevel(player, 52)) {
			return;
		}
		final WorldObject firstGate = new WorldObject(65365, 10, 1, 2998, 3916, 0);
		final WorldObject secondGate = new WorldObject(65367, 10, 1, 2998, 3930, 0);
		player.setNextWorldTile(new WorldTile(secondGate.getX(), secondGate.getY(), 0));
		player.setNextForceMovement(new ForceMovement(new WorldTile(firstGate.getX(), firstGate.getY() + 1, 0), 8, ForceMovement.SOUTH));
		player.setNextAnimation(new Animation(9908));
		player.getLockManagement().lockAll(1000);
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				player.setNextWorldTile(new WorldTile(firstGate.getX(), firstGate.getY() + 1, 0));
				player.setNextAnimation(new Animation(-1));
				WorldTasksManager.schedule(new WorldTask() {

					@Override
					public void run() {
						player.setNextWorldTile(new WorldTile(firstGate.getX(), firstGate.getY() - 1, 0));
					}
				});
			}
		}, 7);
	}

	public static void enterWildernessPipe(final Player player, int objectX, int objectY) {
		final boolean running = player.getRun();
		player.setRunHidden(false);
		player.getLockManagement().lockAll();
		player.addWalkSteps(objectX, 3950, -1, false);
		player.getPackets().sendGameMessage("You pulled yourself through the pipes.", true);
		WorldTasksManager.schedule(new WorldTask() {
			boolean secondloop;

			@Override
			public void run() {
				if (!secondloop) {
					secondloop = true;
					player.getAppearence().setRenderEmote(295);
				} else {
					player.getAppearence().setRenderEmote(-1);
					player.setRunHidden(running);
					setStage(player, 1);
					player.getSkills().addXp(Skills.AGILITY, 12.5);
					player.setNextWorldTile(new WorldTile(3004, 3950, 0));
					player.getLockManagement().unlockAll();
					stop();
				}
			}
		}, 0, 10);
	}

	public static void removeStage(Player player) {
		player.getAttributes().remove("WildernessCourse");
	}

	public static void setStage(Player player, int stage) {
		player.getAttributes().put("WildernessCourse", stage);
	}

	public static int getStage(Player player) {
		Integer stage = (Integer) player.getAttributes().get("WildernessCourse");
		if (stage == null) {
			return -1;
		}
		return stage;
	}
}
