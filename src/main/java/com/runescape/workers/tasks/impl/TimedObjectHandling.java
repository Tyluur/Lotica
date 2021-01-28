package com.runescape.workers.tasks.impl;

import com.runescape.game.world.World;
import com.runescape.game.world.WorldObject;
import com.runescape.workers.tasks.WorldTask;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Jun 17, 2015
 */
public class TimedObjectHandling extends WorldTask {

	@Override
	public void run() {
		spawnObjects();
	}

	private void spawnObjects() {
		List<WorldObject> objects = new ArrayList<>();
		//objects.add(new WorldObject(37121, 0, 2, new WorldTile(3092, 3506, 0))); // edge door
		//objects.add(new WorldObject(13197, 10, 0, new WorldTile(3096, 3507, 0))); // edge altar
		//objects.add(new WorldObject(12309, 10, 3, new WorldTile(3100, 3512, 0))); // rfd chest
		objects.forEach(World::spawnObject);
	}

}
