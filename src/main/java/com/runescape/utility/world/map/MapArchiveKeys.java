package com.runescape.utility.world.map;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.util.HashMap;
import java.util.Map;

public class MapArchiveKeys {

	/**
	 * MapKeys.
	 */
	private static Map<Integer, int[]> mapKeys = new HashMap<>();

	/**
	 * Initiating void.
	 */
	public static void init() {
		try {
			if (!loadPackedFile()) {
				loadUnpacked();
			}
			System.out.println("Loaded " + mapKeys.size() + " map XTEA key(s)");
		} catch (Throwable e) {
			System.err.println("Failed to load map xtea(s)!");
			e.printStackTrace();
		}
	}

	public static boolean loadPackedFile() throws IOException {
		File file = new File("data/resource/world/map/packedKeys.bin");
		if (!file.exists()) {
			return false;
		}
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		ByteBuffer buffer = raf.getChannel().map(MapMode.READ_ONLY, 0, raf.length());
		while (buffer.remaining() > 0) {
			int id = buffer.getShort() & 0xFFFF;
			int[] key = new int[4];
			for (int i2 = 0; i2 < 4; i2++) {
				key[i2] = buffer.getInt();
			}
			mapKeys.put(id, key);
		}
		raf.close();
		return true;
	}

	public static void loadUnpacked() throws IOException {
		File directory = new File("data/resource/world/map/containersXteas/unpacked 634/");
		DataOutputStream output = new DataOutputStream(new FileOutputStream("data/resource/world/map/packedKeys.bin"));
		if (directory.isDirectory()) {
			for (File file : directory.listFiles()) {
				if (file.isFile()) {
					BufferedReader input = new BufferedReader(new FileReader(file));
					int id = Integer.parseInt(file.getName().substring(0, file.getName().indexOf(".")));
					int[] keys = new int[4];
					output.writeShort(id);
					for (int i = 0; i < 4; i++) {
						String line = input.readLine();
						try {
							if (line != null) {
								keys[i] = Integer.parseInt(line);
							} else {
								System.out.println("Corrupted XTEA file : " + id);
								keys[i] = 0;
							}
						} catch (NumberFormatException e) {
							System.out.println("Corrupted XTEA file : " + id + "; line: " + line);
							keys[i] = 0;
						}
						output.writeInt(keys[i]);
					}
					input.close();
					mapKeys.put(id, keys);
				}
			}
		}
		output.close();
	}

	public static int[] getKey(int region) {
		return mapKeys.get(region);
	}

	public static Map<Integer, int[]> getMapKeys() {
		return mapKeys;
	}
}
