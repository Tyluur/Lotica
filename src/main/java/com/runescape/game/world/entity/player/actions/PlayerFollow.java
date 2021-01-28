package com.runescape.game.world.entity.player.actions;

import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.route.RouteFinder;
import com.runescape.game.world.route.strategy.PlayerTileStrategy;
import com.runescape.utility.Utils;

public class PlayerFollow extends Action {

	private final Player target;

	public PlayerFollow(Player target) {
		this.target = target;
	}

	@Override
	public boolean start(Player player) {
		player.setNextFaceEntity(target);
		if (checkAll(player)) {
			return true;
		}
		player.setNextFaceEntity(null);
		player.resetWalkSteps();
		return false;
	}

	@Override
	public boolean process(Player player) {
		if (!checkAll(player)) {
			return false;
		}
		return true;
	}

	@Override
	public int processWithDelay(Player player) {
		handleFollow(player);
		return 0;
	}

	@Override
	public void stop(final Player player) {
		player.setNextFaceEntity(null);
	}

	/**
	 * Checks all prerequisites to ensure the player should be allowe to follow
	 *
	 * @param player
	 * 		The player
	 */
	private boolean checkAll(Player player) {
		if (player.isDead() || player.hasFinished() || target.isDead() || target.hasFinished()) {
			return false;
		}
		int distanceX = player.getX() - target.getX();
		int distanceY = player.getY() - target.getY();
		int size = target.getSize();
		int maxDistance = 16;
		if (player.getPlane() != target.getPlane() || distanceX > size + maxDistance || distanceX < -1 - maxDistance || distanceY > size + maxDistance || distanceY < -1 - maxDistance) {
			return false;
		}
		if (player.getFreezeDelay() >= Utils.currentTimeMillis()) {
			return true;
		}
		return true;
	}

	/**
	 * Handles the following of the player
	 */
	private void handleFollow(Player player) {
		int size = target.getSize();
		// dancing
		if (target.getActionManager().getAction() instanceof PlayerFollow) {
			player.addWalkSteps(target.getX(), target.getY());
			target.addWalkSteps(player.getX(), player.getY());
			player.getActionManager().addActionDelay(1);
		} else if (!player.clipedProjectile(target, true) || !Utils.isInRange(player.getX(), player.getY(), size, target.getX(), target.getY(), target.getSize(), 0)) {
			followPathTo(player, target);
		}
	}

	/**
	 * Follows a path to the target
	 *
	 * @param player
	 * 		The player
	 * @param tile
	 * 		The target tile
	 */
	public static void followPathTo(Player player, WorldTile tile) {
		int steps = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, player.getX(), player.getY(), player.getPlane(), player.getSize(), new PlayerTileStrategy(tile.getX(), tile.getY()), true);
		if (steps == -1) {
			return;
		}
		if (steps > 0) {
			player.resetWalkSteps();
			int[] bufferX = RouteFinder.getLastPathBufferX();
			int[] bufferY = RouteFinder.getLastPathBufferY();
			for (int step = steps - 1; step >= 0; step--) {
				if (!player.addWalkSteps(bufferX[step], bufferY[step], 25, true)) {
					break;
				}
			}
		}
	}

}
