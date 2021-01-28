package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.world.object.ObjectRemoval;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 19, 2015
 */
public class StopObjectSpawn extends CommandSkeleton<String[]> {

	private List<WorldObject> stoppedObjects = new ArrayList<>();

	@Override
	public String[] getIdentifiers() {
		return new String[] { "stos" };
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		WorldObject found = null;
		for (WorldObject object : World.getRegion(player.getRegionId()).getObjects()) {
			if (stoppedObjects.contains(object)) {
				continue;
			}
			if (object.getWorldTile().matches(player.getWorldTile())) {
				try (BufferedWriter bw = new BufferedWriter(new FileWriter(ObjectRemoval.NONSPAWNING_OBJECTS_FILE, true))) {
					bw.append(object.getId() + " " + player.getX() + " " + player.getY() + " " + player.getPlane() + "\n");
				} catch (Exception e) {
					e.printStackTrace();
				}
				stoppedObjects.add(object);
				World.removeObject(object);
				found = object;
				break;
			}
		}
		player.sendMessage(found != null ? "Found and stopped this object from spawning!<br>" + found + "" : "Did not find any object on this tile...");
	}

}
