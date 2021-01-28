package com.runescape.game.content.skills.agility;

import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.ForceMovement;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.game.world.entity.player.achievements.AchievementHandler;
import com.runescape.game.world.entity.player.achievements.easy.Feather_Weight;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

public class GnomeAgility {

	// gnome course

	public static void walkGnomeLog(final Player player) {
		if (player.getX() != 2474 || player.getY() != 3436)
			return;
		final boolean running = player.getRun();
		player.setRunHidden(false);
		player.getLockManagement().lockAll();
		player.addWalkSteps(2474, 3429, -1, false);
		player.getPackets().sendGameMessage("You walk carefully across the slippery log...", true);
		WorldTasksManager.schedule(new WorldTask() {
			boolean secondloop;

			@Override
			public void run() {
				if (!secondloop) {
					secondloop = true;
					player.getAppearence().setRenderEmote(155);
				} else {
					player.getAppearence().setRenderEmote(-1);
					player.setRunHidden(running);
					setGnomeStage(player, 0);
					player.getSkills().addXp(Skills.AGILITY, 7.5);
					player.getPackets().sendGameMessage("... and make it safely to the other side.", true);
					player.getLockManagement().unlockAll();
					stop();
				}
			}
		}, 0, 6);
	}

	public static void climbGnomeObstacleNet(final Player player) {
		if (player.getY() != 3426)
			return;
		player.getPackets().sendGameMessage("You climb the netting.", true);
		player.useStairs(828, new WorldTile(player.getX(), 3423, 1), 1, 2);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				if (getGnomeStage(player) == 0)
					setGnomeStage(player, 1);
				player.getSkills().addXp(Skills.AGILITY, 7.5);
			}
		}, 1);
	}

	public static void climbUpGnomeTreeBranch(final Player player) {
		player.getPackets().sendGameMessage("You climb the tree...", true);
		player.useStairs(828, new WorldTile(2473, 3420, 2), 1, 2, "... to the plantaform above.");
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				if (getGnomeStage(player) == 1)
					setGnomeStage(player, 2);
				player.getSkills().addXp(Skills.AGILITY, 5);
			}
		}, 1);
	}

	public static void walkBackGnomeRope(final Player player) {
		if (player.getX() != 2483 || player.getY() != 3420 || player.getPlane() != 2)
			return;
		final boolean running = player.getRun();
		player.setRunHidden(false);
		player.getLockManagement().lockAll(7000);
		player.addWalkSteps(2477, 3420, -1, false);
		WorldTasksManager.schedule(new WorldTask() {
			boolean secondloop;

			@Override
			public void run() {
				if (!secondloop) {
					secondloop = true;
					player.getAppearence().setRenderEmote(155);
				} else {
					player.getAppearence().setRenderEmote(-1);
					player.setRunHidden(running);
					player.getSkills().addXp(Skills.AGILITY, 7);
					player.getPackets().sendGameMessage("You passed the obstacle succesfully.", true);
					stop();
				}
			}
		}, 0, 5);
	}

	public static void walkGnomeRope(final Player player) {
		if (player.getX() != 2477 || player.getY() != 3420 || player.getPlane() != 2)
			return;
		final boolean running = player.getRun();
		player.setRunHidden(false);
		player.getLockManagement().lockAll(7000);
		player.addWalkSteps(2483, 3420, -1, false);
		WorldTasksManager.schedule(new WorldTask() {
			boolean secondloop;

			@Override
			public void run() {
				if (!secondloop) {
					secondloop = true;
					player.getAppearence().setRenderEmote(155);
				} else {
					player.getAppearence().setRenderEmote(-1);
					player.setRunHidden(running);
					if (getGnomeStage(player) == 2)
						setGnomeStage(player, 3);
					player.getSkills().addXp(Skills.AGILITY, 7);
					player.getPackets().sendGameMessage("You passed the obstacle succesfully.", true);
					stop();
				}
			}
		}, 0, 5);
	}

	public static void climbDownGnomeTreeBranch(final Player player) {
		player.useStairs(828, new WorldTile(2487, 3421, 0), 1, 2, "You climbed the tree branch succesfully.");
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				if (getGnomeStage(player) == 3)
					setGnomeStage(player, 4);
				player.getSkills().addXp(Skills.AGILITY, 5);
			}
		}, 1);
	}

	public static void climbGnomeObstacleNet2(final Player player) {
		if (player.getY() != 3425)
			return;
		player.getPackets().sendGameMessage("You climb the netting.", true);
		player.useStairs(828, new WorldTile(player.getX(), player.getY() == 3425 ? 3428 : 3425, 0), 1, 2);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				if (getGnomeStage(player) == 4)
					setGnomeStage(player, 5);
				player.getSkills().addXp(Skills.AGILITY, 8);
			}
		}, 1);
	}

	public static void enterGnomePipe(final Player player, int objectX, int objectY) {
		final boolean running = player.getRun();
		player.setRunHidden(false);
		player.getLockManagement().lockAll();
		player.addWalkSteps(objectX, objectY == 3431 ? 3437 : 3430, -1, false);
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
					player.getSkills().addXp(Skills.AGILITY, 7);
					if (getGnomeStage(player) == 5) {
						removeGnomeStage(player);
						player.getSkills().addXp(Skills.AGILITY, 39.5);
						AchievementHandler.incrementProgress(player, Feather_Weight.class, 1);
					}
					player.getLockManagement().unlockAll();
					stop();
				}
			}
		}, 0, 6);
	}

	public static void removeGnomeStage(Player player) {
		player.getAttributes().remove("GnomeCourse");
	}

	public static void setGnomeStage(Player player, int stage) {
		player.getAttributes().put("GnomeCourse", stage);
	}

	public static int getGnomeStage(Player player) {
		Integer stage = (Integer) player.getAttributes().get("GnomeCourse");
		if (stage == null)
			return -1;
		return stage;
	}

	public static void climbUpTree(final Player player) {
		if (!Agility.hasLevel(player, 85))
			return;
		player.useStairs(828, new WorldTile(2472, 3419, 3), 1, 2, "You climbed the tree succesfully.");
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				removeGnomeStage(player);
				player.getSkills().addXp(Skills.AGILITY, 25);
			}
		}, 1);
	}


	public static void jumpDown(final Player player, WorldObject object) {
		player.getLockManagement().lockAll(4000);
		final WorldTile toTile = new WorldTile(2485, 3436, 0);
		WorldTasksManager.schedule(new WorldTask() {

			boolean secondLoop;

			@Override
			public void run() {

				if (!secondLoop) {
					player.setNextForceMovement(new ForceMovement(player, 0, toTile, 5, ForceMovement.NORTH));
					player.setNextAnimation(new Animation(2923));
					secondLoop = true;
				} else {
					player.setNextAnimation(new Animation(2924));
					player.setNextWorldTile(toTile);
					player.getSkills().addXp(Skills.AGILITY, 630);
					stop();
				}

			}

		}, 1, 2);
	}

	// advanced

	public static void preSwing(final Player player, final WorldObject object) {
		player.getLockManagement().lockAll(4000);
		player.setNextAnimation(new Animation(11784));
		final WorldTile toTile = new WorldTile(player.getX(), 3421, object.getPlane());
		player.setNextForceMovement(new ForceMovement(player, 0, toTile, 2, ForceMovement.NORTH));
		WorldTasksManager.schedule(new WorldTask() {
			int stage;

			@Override
			public void run() {
				if (stage == 1) {
					player.setNextWorldTile(toTile);
					player.setNextAnimation(new Animation(11785));
					swing(player, object);
					stop();
				}
				stage++;
			}

		}, 0, 1);
	}

	private static void swing(final Player player, final WorldObject object) {
		player.getLockManagement().lockAll(4000);
		final WorldTile toTile = new WorldTile(player.getX(), 3425, object.getPlane());
		player.setNextForceMovement(new ForceMovement(player, 0, toTile, 1, ForceMovement.NORTH));
		WorldTasksManager.schedule(new WorldTask() {
			int stage;

			@Override
			public void run() {
				if (stage == 0) {
					player.setNextAnimation(new Animation(11789));
					player.setNextWorldTile(toTile);
				} else if (stage == 1) {
					swing1(player, object);
					stop();
				}
				stage++;
			}

		}, 0, 1);
	}

	private static void swing1(final Player player, final WorldObject object) {
		player.getLockManagement().lockAll(4000);
		final WorldTile NextTile = new WorldTile(player.getX(), 3429, object.getPlane());
		player.setNextForceMovement(new ForceMovement(player, 2, NextTile, 3, ForceMovement.NORTH));
		WorldTasksManager.schedule(new WorldTask() {
			int stage;

			@Override
			public void run() {
				if (stage == 3) {
					player.setNextWorldTile(NextTile);
					swing2(player, object);
					stop();
				}
				stage++;
			}

		}, 0, 1);
	}

	private static void swing2(final Player player, final WorldObject object) {
		player.getLockManagement().lockAll(4000);
		final WorldTile LastTile = new WorldTile(player.getX(), 3432, object.getPlane());
		player.setNextForceMovement(new ForceMovement(player, 0, LastTile, 1, ForceMovement.NORTH));
		WorldTasksManager.schedule(new WorldTask() {
			int stage;

			@Override
			public void run() {
				if (stage == 2) {
					player.getSkills().addXp(Skills.AGILITY, 25);
					player.setNextWorldTile(LastTile);
					stop();
				}
				stage++;
			}

		}, 0, 1);
	}

	public static void runGnomeBoard(final Player player, final WorldObject object) {
		player.getLockManagement().lockAll();
		player.setNextAnimation(new Animation(2922));
		final WorldTile toTile = new WorldTile(2484, 3418, object.getPlane());
		player.setNextForceMovement(new ForceMovement(new WorldTile(2477, 3418, 3), 1, toTile, 3, ForceMovement.EAST));
		player.getPackets().sendGameMessage("You skillfully run across the Board", true);
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				player.setNextWorldTile(toTile);
				player.getSkills().addXp(Skills.AGILITY, 25);
				player.getLockManagement().unlockAll();
			}

		}, 1);
	}
}


