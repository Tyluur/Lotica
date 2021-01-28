package com.runescape.game.content.global.wilderness;

import com.runescape.game.content.skills.magic.Magic;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.region.Region;
import com.runescape.utility.Utils;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

import java.util.ArrayList;
import java.util.List;

public class WildernessObelisk {

	/**
	 * The left-most tiles for the obelisks
	 */
	public static final WorldTile[] OBELISK_CENTER_TILES = { new WorldTile(2978, 3864, 0), new WorldTile(3033, 3730, 0), new WorldTile(3104, 3792, 0), new WorldTile(3154, 3618, 0), new WorldTile(3217, 3654, 0), new WorldTile(3305, 3914, 0) };

	/**
	 * The active obelisks
	 */
	private static final boolean[] ACTIVE_OBELISK = new boolean[6];

	/**
	 * Activates the obelisk by a player
	 *
	 * @param id
	 * 		The id of the obelisk
	 * @param player
	 * 		The player
	 */
	public static void activateObelisk(int id, final Player player) {
		final int index = id - 14826;
		final WorldTile center = OBELISK_CENTER_TILES[index];
		if (ACTIVE_OBELISK[index]) {
			player.getPackets().sendGameMessage("The obelisk is already active.");
			return;
		}
		ACTIVE_OBELISK[index] = true;
		WorldObject object = World.getObjectWithId(center, id);
		if (object == null) {
			return;
		}
		List<WorldObject> affected = new ArrayList<>();
		int[][] transformations = { { 4, 0, 0 }, { 0, 4, 0 }, { 4, 4, 0 } };
		for (int[] transformation : transformations) {
			WorldObject transformed = World.getObjectWithId(center.transform(transformation[0], transformation[1], transformation[2]), object.getId());
			affected.add(transformed);
		}
		affected.add(object);
		affected.forEach(obelisk -> World.spawnObject(new WorldObject(14825, 10, 0, obelisk.getWorldTile())));

		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				for (int x = 1; x < 4; x++) {
					for (int y = 1; y < 4; y++) {
						World.sendGraphics(player, new Graphics(661), center.transform(x, y, 0));
					}
				}
				teleportPlayers();
				ACTIVE_OBELISK[index] = false;
				affected.forEach(World::spawnObject);
			}

			/**
			 * Teleports the players close to the obelisk to the new position
			 */
			private void teleportPlayers() {
				Region region = World.getRegion(center.getRegionId());
				List<Integer> playerIndexes = region.getPlayerIndexes();
				WorldTile newCenter = OBELISK_CENTER_TILES[Utils.random(OBELISK_CENTER_TILES.length)];
				if (playerIndexes != null) {
					for (Integer i : playerIndexes) {
						Player p = World.getPlayers().get(i);
						if (p == null || (p.getX() < center.getX() + 1 || p.getX() > center.getX() + 3 || p.getY() < center.getY() + 1 || p.getY() > center.getY() + 3)) {
							continue;
						}
						int offsetX = p.getX() - center.getX();
						int offsetY = p.getY() - center.getY();
						Magic.sendTeleportSpell(p, 8939, 8941, 1690, -1, 0, 0, new WorldTile(newCenter.getX() + offsetX, newCenter.getY() + offsetY, 0), 3, false, Magic.OBJECT_TELEPORT);
						p.getPackets().sendGameMessage("Ancient magic teleports you to a place within the wilderness!", true);
					}
				}
			}

		}, 8);

	}
}
