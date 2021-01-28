package com.runescape.utility.world.object;

import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since Dec 13, 2013
 */
public class ObjectRemoval {

	/**
	 * The file to read from
	 */
	public static final String NONSPAWNING_OBJECTS_FILE = "data/resource/world/map/nonspawning.txt";

	/**
	 * The list of objects that arent spawned
	 */
	private static final CopyOnWriteArrayList<CustomObject> OBJECTS = new CopyOnWriteArrayList<CustomObject>();

	/**
	 * Starts up and populates the list
	 */
	public static void initialize() {
		populateList();
	}

	/**
	 * Populates the list with data from the file
	 */
	private static void populateList() {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(NONSPAWNING_OBJECTS_FILE));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("//")) {
					continue;
				}
				String[] split = line.split(" ");
				int id = Integer.parseInt(split[0]);
				int x = Integer.parseInt(split[1]);
				int y = Integer.parseInt(split[2]);
				int z = Integer.parseInt(split[3]);
				OBJECTS.add(new CustomObject(id, new WorldTile(x, y, z)));
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<CustomObject> getRemovedAtRegion(int regionId) {
		return OBJECTS.stream().filter(p -> p.getTile().getRegionId() == regionId).collect(Collectors.toList());
	}

	/**
	 * Gets the custom object on the tile
	 *
	 * @param object
	 * 		The object
	 */
	public static CustomObject getCustomObject(WorldObject object, boolean checkId) {
		if (!checkId) {
			for (CustomObject o : OBJECTS) {
				if (o.getTile().getX() == object.getX() && o.getTile().getY() == object.getY() && o.getTile().getPlane() == object.getPlane()) {
					return o;
				}
			}
		} else {
			for (CustomObject o : OBJECTS) {
				if (o.getId() == object.getId() && o.getTile().getX() == object.getX() && o.getTile().getY() == object.getY() && o.getTile().getPlane() == object.getPlane()) {
					return o;
				}
			}
		}
		return null;
	}

	public static class CustomObject {

		/**
		 * The id of the object
		 */
		private final int id;

		/**
		 * The tile of the object
		 */
		private final WorldTile tile;

		@Override
		public String toString() {
			return "id=" + id + ", tile=" + tile + "";
		}

		public CustomObject(int id, WorldTile tile) {
			this.id = id;
			this.tile = tile;
		}

		/**
		 * Getting the tile of the object
		 */
		public WorldTile getTile() {
			return tile;
		}

		/**
		 * Getting the id of the object
		 */
		public int getId() {
			return id;
		}
	}

}
