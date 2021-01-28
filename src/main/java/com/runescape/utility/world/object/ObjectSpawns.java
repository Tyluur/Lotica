package com.runescape.utility.world.object;

import com.runescape.game.world.World;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.utility.Utils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

public final class ObjectSpawns {

	private static final String PACKED_DIRECTORY = "data/resource/world/map/packedSpawns";

	private static final String UNPACKED_LOCATION = "data/resource/world/map/unpackedSpawnsList.txt";

	public static void init() {
		if (!new File(PACKED_DIRECTORY).exists()) { packObjectSpawns(); }
	}

	private static void packObjectSpawns() {
		long start = System.currentTimeMillis();
		System.out.println("Packing object spawns...");
		if (!new File(PACKED_DIRECTORY).mkdir()) {
			throw new RuntimeException("Couldn't create packedSpawns directory.");
		}
		//String line = in.readLine();
		for (String line : Utils.getFileText(UNPACKED_LOCATION)) {
			if (line.startsWith("//")) { continue; }
			String[] splitedLine = line.split(" ~ ");
			if (splitedLine.length != 2) { throw new RuntimeException("Invalid Object Spawn line: " + line); }
			String[] objectInformation = splitedLine[0].split(" ");
			String[] spawnInformation = splitedLine[1].split(" ");
			if (objectInformation.length != 3 || spawnInformation.length != 4) {
				throw new RuntimeException("Invalid Object Spawn line: " + line);
			}
			int objectId = Integer.parseInt(objectInformation[0]);
			int type = Integer.parseInt(objectInformation[1]);
			int rotation = Integer.parseInt(objectInformation[2]);

			WorldTile tile = new WorldTile(Integer.parseInt(spawnInformation[0]), Integer.parseInt(spawnInformation[1]), Integer.parseInt(spawnInformation[2]));
			addObjectSpawn(objectId, type, rotation, tile.getRegionId(), tile, Boolean.parseBoolean(spawnInformation[3]));
		}
		System.out.println("Packed in " + (System.currentTimeMillis() - start) + " ms.");
	}

	@SuppressWarnings("unused")
	public static void loadObjectSpawns(int regionId) {
		File file = new File("data/resource/world/map/packedSpawns/" + regionId + ".os");
		if (!file.exists()) { return; }
		try {
			RandomAccessFile in = new RandomAccessFile(file, "r");
			FileChannel channel = in.getChannel();
			ByteBuffer buffer = channel.map(MapMode.READ_ONLY, 0, channel.size());
			while (buffer.hasRemaining()) {
				int objectId = buffer.getShort() & 0xffff;
				int type = buffer.get() & 0xff;
				int rotation = buffer.get() & 0xff;
				int plane = buffer.get() & 0xff;
				int x = buffer.getShort() & 0xffff;
				int y = buffer.getShort() & 0xffff;
				boolean cliped = buffer.get() == 1;
				if (objectId == 65535) {
					objectId = -1;
				}
				World.spawnObject(new WorldObject(objectId, type, rotation, x, y, plane));
			}
			channel.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void dumpObjectSpawn(int objectId, int type, int rotation, WorldTile tile) throws IOException {
		File file = new File(UNPACKED_LOCATION);
		BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
		// 47120 10 0 - 2591 3919 0 true
		writer.write(objectId + " " + type + " " + rotation + " ~ " + tile.getX() + " " + tile.getY() + " " + tile.getPlane() + " true");
		writer.newLine();
		writer.flush();
		writer.close();
		if (!Utils.deleteDirectory(new File(PACKED_DIRECTORY))) { System.out.println("Could not delete directory..."); }
	}

	private static void addObjectSpawn(int objectId, int type, int rotation, int regionId, WorldTile tile, boolean cliped) {
		try {
			DataOutputStream out = new DataOutputStream(new FileOutputStream("data/resource/world/map/packedSpawns/" + regionId + ".os", true));
			out.writeShort(objectId);
			out.writeByte(type);
			out.writeByte(rotation);
			out.writeByte(tile.getPlane());
			out.writeShort(tile.getX());
			out.writeShort(tile.getY());
			out.writeBoolean(cliped);
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private ObjectSpawns() {
	}

}
