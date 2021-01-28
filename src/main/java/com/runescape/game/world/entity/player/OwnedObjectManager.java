package com.runescape.game.world.entity.player;

import com.runescape.game.world.World;
import com.runescape.game.world.WorldObject;
import com.runescape.utility.Utils;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class OwnedObjectManager {

	public static final AtomicLong keyMaker = new AtomicLong();

	private static final Map<String, OwnedObjectManager> ownedObjects = new ConcurrentHashMap<>();

	private Player player;

	private WorldObject[] objects;

	private int count;

	private long[] cycleTimes;

	private long lifeTime;

	private String managerKey;

	private ProcessEvent event;

	public static void processAll() {
		ownedObjects.values().forEach(OwnedObjectManager::process);
	}

	public static boolean isPlayerObject(Player player, WorldObject object) {
		for (Iterator<String> it = player.getOwnedObjectManagerKeys().iterator(); it.hasNext(); ) {
			OwnedObjectManager manager = ownedObjects.get(it.next());
			if (manager == null) {
				it.remove();
				continue;
			}
			if (manager.getCurrentObject().getX() == object.getX() && manager.getCurrentObject().getY() == object.getY() && manager.getCurrentObject().getPlane() == object.getPlane() && manager.getCurrentObject().getId() == object.getId()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets the owner of the world object
	 */
	public static Player getOwner(WorldObject object) {
		Player owner = null;
		for (Player p : World.getPlayers()) {
			if (OwnedObjectManager.isPlayerObject(p, object)) {
				owner = p;
			}
		}
		return owner;
	}

	public interface ConvertEvent {

		boolean canConvert(Player player);

	}

	public static boolean convertIntoObject(WorldObject object, WorldObject toObject, ConvertEvent event) {
		for (OwnedObjectManager manager : ownedObjects.values()) {
			if (manager.getCurrentObject().getX() == toObject.getX() && manager.getCurrentObject().getY() == toObject.getY() && manager.getCurrentObject().getPlane() == toObject.getPlane() && manager.getCurrentObject().getId() == object.getId()) {
				if (event != null && !event.canConvert(manager.player)) {
					return false;
				}
				manager.convertIntoObject(toObject);
				return true;
			}
		}
		return false;
	}

	public static boolean removeObject(Player player, WorldObject object) {
		for (Iterator<String> it = player.getOwnedObjectManagerKeys().iterator(); it.hasNext(); ) {
			final OwnedObjectManager manager = ownedObjects.get(it.next());
			if (manager == null) {
				it.remove();
				continue;
			}
			if (manager.getCurrentObject().getX() == object.getX() && manager.getCurrentObject().getY() == object.getY() && manager.getCurrentObject().getPlane() == object.getPlane() && manager.getCurrentObject().getId() == object.getId()) {
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						manager.delete();
					}
				});
				return true;
			}
		}
		return false;
	}

	public static void linkKeys(Player player) {
		for (Iterator<String> it = player.getOwnedObjectManagerKeys().iterator(); it.hasNext(); ) {
			OwnedObjectManager manager = ownedObjects.get(it.next());
			if (manager == null) {
				it.remove();
				continue;
			}
			manager.player = player;
		}
	}

	public static void addOwnedObjectManager(Player player, WorldObject object, long cycleTime) {
		addOwnedObjectManager(player, new WorldObject[] { object }, new long[] { cycleTime });
	}

	public static void addOwnedObjectManager(Player player, WorldObject[] object, long[] cycleTimes) {
		addOwnedObjectManager(player, object, cycleTimes, null);
	}

	public static void addOwnedObjectManager(Player player, WorldObject[] object, long[] cycleTimes, ProcessEvent event) {
		new OwnedObjectManager(player, object, cycleTimes, event);
	}

	private OwnedObjectManager(Player player, WorldObject[] objects, long[] cycleTimes, ProcessEvent event) {
		managerKey = player.getUsername() + "_" + keyMaker.getAndIncrement();
		this.cycleTimes = cycleTimes;
		this.objects = objects;
		this.player = player;
		this.event = event;
		spawnObject();
		player.getOwnedObjectManagerKeys().add(managerKey);
		ownedObjects.put(managerKey, this);
	}

	public static int getObjectsforValue(Player player, int objectId) {
		int count = 0;
		for (Iterator<String> it = player.getOwnedObjectManagerKeys().iterator(); it.hasNext(); ) {
			OwnedObjectManager manager = ownedObjects.get(it.next());
			if (manager == null) {
				it.remove();
				continue;
			}
			if (manager.getCurrentObject().getId() == objectId) {
				count++;
			}
		}
		return count;
	}

	public static boolean containsObjectValue(Player player, int... objectIds) {
		for (Iterator<String> it = player.getOwnedObjectManagerKeys().iterator(); it.hasNext(); ) {
			OwnedObjectManager manager = ownedObjects.get(it.next());
			if (manager == null) {
				it.remove();
				continue;
			}
			for (int objectId : objectIds) {
				if (manager.getCurrentObject().getId() == objectId) {
					return true;
				}
			}
		}
		return false;
	}

	public void reset() {
		ownedObjects.values().forEach(OwnedObjectManager::delete);
	}

	public void resetLifeTime() {
		this.lifeTime = Utils.currentTimeMillis() + cycleTimes[count];
	}

	public boolean forceMoveNextStage() {
		if (count != -1) {
			destroyObject(objects[count]);
		}
		count++;
		if (count == objects.length) {
			remove();
			return false;
		}
		spawnObject();
		return true;
	}

	private void spawnObject() {
		World.spawnObject(objects[count]);
		if (event != null) {
			event.spawnObject(player, getCurrentObject());
		}
		resetLifeTime();
	}

	public void convertIntoObject(WorldObject object) {
		destroyObject(objects[count]);
		objects[count] = object;
		spawnObject();
	}

	private void remove() {
		ownedObjects.remove(managerKey);
		if (player != null) {
			player.getOwnedObjectManagerKeys().remove(managerKey);
		}
	}

	public void delete() {
		destroyObject(objects[count]);
		remove();
	}

	public void process() {
		boolean infinite = getCurrentObject().getDefinitions().name.equals("Dwarf multicannon");
		if (!infinite && Utils.currentTimeMillis() > lifeTime) {
			forceMoveNextStage();
		} else if (event != null) {
			event.process(player, getCurrentObject());
		}
	}

	public WorldObject getCurrentObject() {
		return objects[count];
	}

	public void destroyObject(WorldObject object) {
		World.removeObject(object);
	}

	public interface ProcessEvent {

		void spawnObject(Player player, WorldObject object);

		void process(Player player, WorldObject currentObject);

	}

}
