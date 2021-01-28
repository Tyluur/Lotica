package com.runescape.utility;

import com.alex.store.Store;
import com.runescape.cache.Cache;
import com.runescape.game.GameConstants;
import com.runescape.game.content.FriendChatsManager;
import com.runescape.game.content.unique.quickchat.QuickChatType;
import com.runescape.game.interaction.controllers.impl.Wilderness;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.QuickChatMessage;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.utility.external.gson.loaders.NPCSpawnLoader;
import com.runescape.utility.external.gson.loaders.NPCSpawnLoader.Direction;
import com.runescape.utility.tools.TextIO;
import com.runescape.workers.game.core.GameUpdateWorker;

import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class Utils {

	public static final byte[] DIRECTION_DELTA_X = new byte[] { -1, 0, 1, -1, 1, -1, 0, 1 };

	public static final byte[] DIRECTION_DELTA_Y = new byte[] { 1, 1, 1, 0, 0, -1, -1, -1 };

	public static final char[] VALID_CHARS = { '_', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };

	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

	private static final Object ALGORITHM_LOCK = new Object();

	private static final Random RANDOM = new Random();

	public static char[] aCharArray6385 = { '\u20ac', '\0', '\u201a', '\u0192', '\u201e', '\u2026', '\u2020', '\u2021', '\u02c6', '\u2030', '\u0160', '\u2039', '\u0152', '\0', '\u017d', '\0', '\0', '\u2018', '\u2019', '\u201c', '\u201d', '\u2022', '\u2013', '\u2014', '\u02dc', '\u2122', '\u0161', '\u203a', '\u0153', '\0', '\u017e', '\u0178' };

	private static long timeCorrection;

	private static long lastTimeUpdate;

	private static SecureRandom SECURE_RANDOM;

	private static String[] suffix = new String[] { "", "k", "m", "b", "t" };

	private static int MAX_LENGTH = 4;

	static {
		try {
			// native is too slow
			SECURE_RANDOM = SecureRandom.getInstance("SHA1PRNG", "SUN");
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private Utils() {

	}

	public static synchronized long currentTimeMillis() {
		long l = System.currentTimeMillis();
		if (l < lastTimeUpdate) {
			timeCorrection += lastTimeUpdate - l;
		}
		lastTimeUpdate = l;
		return l + timeCorrection;
	}

	public static String formatDecimal(double decimal) {
		return DECIMAL_FORMAT.format(decimal);
	}

	public static String getLagPercentage() {
		long speed = GameUpdateWorker.getLastCycleSpeed();
		double percentage = ((double) (speed == 0 ? 600 : speed) / 600) * 100;
		double total = 100 - percentage;
		NumberFormat formatter = new DecimalFormat("#0.00");
		//System.out.println("[speed=" + speed + ", percentage=" + percentage + "]");
		return formatter.format(total);
	}

	/**
	 * Force deletion of directory
	 */
	public static boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			if (files == null) {
				return false;
			}
			for (File file : files) {
				if (file.isDirectory()) {
					deleteDirectory(file);
				} else {
					file.delete();
				}
			}
		}
		return (path.delete());
	}

	public static double randomDouble() {
		return SECURE_RANDOM.nextDouble();
	}

	public static boolean colides(int x1, int y1, int size1, int x2, int y2, int size2) {
		int distanceX = x1 - x2;
		int distanceY = y1 - y2;
		return distanceX < size2 && distanceX > -size1 && distanceY < size2 && distanceY > -size1;
	}

	public static boolean colides(Entity entity, Entity target) {
		return entity.getPlane() == target.getPlane() && colides(entity.getX(), entity.getY(), entity.getSize(), target.getX(), target.getY(), target.getSize());
	}

	public static void main(String[] args) throws IOException {
		while (true) {
	/*		System.out.print("Enter ks:\t");
			int inputNumber = TextIO.getlnInt();
			System.out.println("wildernessPointsAfterKillstreakModifier= " + Wilderness.wildernessPointsAfterKillstreakModifier(inputNumber));
*/
			System.out.println("Enter ks ended:\t");
			int inputNumber = TextIO.getlnInt();
			System.out.println("getPointRewardFromKillstreakEnding=" + Wilderness.getPointRewardFromKillstreakEnding(inputNumber));
		}
/*		Cache.init();
		final Map<Integer, Integer> itemPrices = new HashMap<>();
		for (int i = 0; i < Utils.getItemDefinitionsSize(); i++) {
			itemPrices.put(i, ItemDefinitions.forId(i).getValue());
		}
		System.out.println("stored all prices in map");
		Utils.clearFile("pricesSorted.txt");

		Stream<Map.Entry<Integer,Integer>> sorted = itemPrices.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue()));

		sorted.forEach(entry -> {
			Utils.writeTextToFile("pricesSorted.txt", entry.getKey() + "(" + ItemDefinitions.getItemDefinitions(entry.getKey()).getName() + "):\t" + format(entry.getValue()) + "\n", true);
		});*/

		/*itemPrices.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEachOrdered(entry -> {
			Utils.writeTextToFile("pricesSorted.txt", entry.getKey() + "(" + ItemDefinitions.getItemDefinitions(entry.getKey()).getName() + "):\t" + entry.getValue() + "\n", true);
		});*/
/*
		ValueComparator bvc = new ValueComparator(itemPrices);
		TreeMap<Integer, Integer> sortedMap = new TreeMap<>(bvc);
		sortedMap.putAll(itemPrices);

		System.out.println("sorted map");

		Utils.clearFile("pricesSorted.txt");
		for (Entry<Integer, Integer> entry : sortedMap.entrySet()) {
			Utils.writeTextToFile("pricesSorted.txt", entry.getKey() + "(" + ItemDefinitions.getItemDefinitions(entry.getKey()).getName() + "):\t" + entry.getValue() + "\n", true);
		}
		System.out.println("done");*/
		/*while(true) {
			System.out.print("Enter base player count:\t");
			System.out.println("The fake player count will be:\t" + (getFakePlayerCount(TextIO.getlnInt())));
		}*/
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		Map<K, V> result = new LinkedHashMap<>();
		Stream<Entry<K, V>> st = map.entrySet().stream();
		st.sorted(Comparator.comparing(Entry::getValue)).forEachOrdered(e -> result.put(e.getKey(), e.getValue()));
		return result;
	}

	public static <T, E> Set<T> getKeysByValue(Map<T, E> map, E value) {
		return map.entrySet().stream().filter(entry -> Objects.equals(entry.getValue(), value)).map(Map.Entry::getKey).collect(Collectors.toSet());
	}

	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
		for (Entry<T, E> entry : map.entrySet()) {
			if (Objects.equals(value, entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}

	public static int getFakePlayerCount() {
		int playerCount = World.getPlayers().size();
		int modifier = playerCount < 10 ? 3 : Math.round(playerCount / 10) * 3;
		return playerCount + modifier;
	}

	public static String convertMillisecondsToTime(long milliseconds) {
		long days = (milliseconds / 86400000L);
		long hours = ((milliseconds / 3600000L) % 24L);
		long minutes = ((milliseconds / 60000L) % 60L);
		//long seconds = ((milliseconds / 1000L) % 60L);
		String string = "";
		if (days > 0) {
			String s = days == 1 ? " day " : " days ";
			string += days + s;
		}
		if (hours > 0) {
			String s = hours == 1 ? " hour " : " hours ";
			string += hours + s;
		}
		if (minutes > 0) {
			String s = minutes == 1 ? " min " : " mins ";
			string += minutes + s;
		}
	/*	if (seconds > 0) {
			String s = seconds == 1 ? " sec " : " secs ";
			string += seconds + s;
		}*/
		if (string.equals("")) {
			string = "1 minute ";
		}
		return string;
	}

	public static byte[] cryptRSA(byte[] data, BigInteger exponent, BigInteger modulus) {
		return new BigInteger(data).modPow(exponent, modulus).toByteArray();
	}

	public static <T> Stream<T> stream(Iterable<T> iterable) {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterable.iterator(), Spliterator.ORDERED), false);
	}

	/**
	 * Gets a completed version of a string array
	 *
	 * @param array
	 * 		The array
	 * @param index
	 * 		The index to start at
	 */
	public static String getCompleted(String[] array, int index) {
		StringBuilder sb = new StringBuilder();
		for (int i = index; i < array.length; i++) {
			if (i == array.length - 1 || array[i + 1].startsWith("+")) {
				return sb.append(array[i]).toString();
			}
			sb.append(array[i]).append(" ");
		}
		return "null";
	}

	public static final byte[] encryptUsingMD5(byte[] buffer) {
		// prevents concurrency problems with the algorithm
		synchronized (ALGORITHM_LOCK) {
			try {
				MessageDigest algorithm = MessageDigest.getInstance("MD5");
				algorithm.update(buffer);
				byte[] digest = algorithm.digest();
				algorithm.reset();
				return digest;
			} catch (Throwable e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	/**
	 * Getting the text in the file as a formatted {@code String} {@code Object}
	 *
	 * @param location
	 * 		The location of the file
	 */
	public static String getText(String location) {
		File file = new File(location);
		if (!file.exists()) {
			throw new IllegalStateException("File doesn't exist:\t" + file.getAbsolutePath());
		}
		StringBuilder text = new StringBuilder();
		for (String fileText : Utils.getFileText(location)) {
			text.append(fileText).append("\n");
		}
		return text.toString();
	}

	public static List<String> getFileText(String file) {
		List<String> text = new ArrayList<>();
		File realFile = new File(file);
		if (!realFile.exists()) {
			return text;
		}
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.equals("") || line.equals(" ")) {
					continue;
				}
				text.add(line);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}

	public static boolean inCircle(WorldTile location, WorldTile center, int radius) {
		return getDistance(center, location) < radius;
	}

	public static final int getDistance(WorldTile t1, WorldTile t2) {
		return getDistance(t1.getX(), t1.getY(), t2.getX(), t2.getY());
	}

	public static final int getDistance(int coordX1, int coordY1, int coordX2, int coordY2) {
		int deltaX = Math.abs(coordX2 - coordX1);
		int deltaY = Math.abs(coordY2 - coordY1);
		return ((int) Math.ceil(Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2))));
	}

	public static void copyFile(File sourceFile, File destFile) throws IOException {
		if (!destFile.exists()) {
			destFile.createNewFile();
		}

		FileChannel source = null;
		FileChannel destination = null;
		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
	}

	@SuppressWarnings({ "rawtypes" })
	public static Class[] getClasses(String packageName) throws ClassNotFoundException, IOException {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		assert classLoader != null;
		String path = packageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<File>();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			dirs.add(new File(resource.getFile().replaceAll("%20", " ")));
		}
		ArrayList<Class> classes = new ArrayList<Class>();
		for (File directory : dirs) {
			classes.addAll(findClasses(directory, packageName));
		}
		return classes.toArray(new Class[classes.size()]);
	}

	@SuppressWarnings("rawtypes")
	private static List<Class> findClasses(File directory, String packageName) {
		List<Class> classes = new ArrayList<Class>();
		if (!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				assert !file.getName().contains(".");
				classes.addAll(findClasses(file, packageName + "." + file.getName()));
			} else if (file.getName().endsWith(".class")) {
				try {
					classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
				} catch (Throwable e) {

				}
			}
		}
		return classes;
	}

	public static int getNpcMoveDirection(int dd) {
		if (dd < 0) {
			return -1;
		}
		return getNpcMoveDirection(DIRECTION_DELTA_X[dd], DIRECTION_DELTA_Y[dd]);
	}

	public static int getNpcMoveDirection(int dx, int dy) {
		if (dx == 0 && dy > 0) {
			return 0;
		}
		if (dx > 0 && dy > 0) {
			return 1;
		}
		if (dx > 0 && dy == 0) {
			return 2;
		}
		if (dx > 0 && dy < 0) {
			return 3;
		}
		if (dx == 0 && dy < 0) {
			return 4;
		}
		if (dx < 0 && dy < 0) {
			return 5;
		}
		if (dx < 0 && dy == 0) {
			return 6;
		}
		if (dx < 0 && dy > 0) {
			return 7;
		}
		return -1;
	}

	public static final int[][] getCoordOffsetsNear(int size) {
		int[] xs = new int[4 + (4 * size)];
		int[] xy = new int[xs.length];
		xs[0] = -size;
		xy[0] = 1;
		xs[1] = 1;
		xy[1] = 1;
		xs[2] = -size;
		xy[2] = -size;
		xs[3] = 1;
		xy[2] = -size;
		for (int fakeSize = size; fakeSize > 0; fakeSize--) {
			xs[(4 + ((size - fakeSize) * 4))] = -fakeSize + 1;
			xy[(4 + ((size - fakeSize) * 4))] = 1;
			xs[(4 + ((size - fakeSize) * 4)) + 1] = -size;
			xy[(4 + ((size - fakeSize) * 4)) + 1] = -fakeSize + 1;
			xs[(4 + ((size - fakeSize) * 4)) + 2] = 1;
			xy[(4 + ((size - fakeSize) * 4)) + 2] = -fakeSize + 1;
			xs[(4 + ((size - fakeSize) * 4)) + 3] = -fakeSize + 1;
			xy[(4 + ((size - fakeSize) * 4)) + 3] = -size;
		}
		return new int[][] { xs, xy };
	}

	public static final int getFaceDirection(int xOffset, int yOffset) {
		return ((int) (Math.atan2(-xOffset, -yOffset) * 2607.5945876176133)) & 0x3fff;
	}

	public static final int getMoveDirection(int xOffset, int yOffset) {
		if (xOffset < 0) {
			if (yOffset < 0) {
				return 5;
			} else if (yOffset > 0) {
				return 0;
			} else {
				return 3;
			}
		} else if (xOffset > 0) {
			if (yOffset < 0) {
				return 7;
			} else if (yOffset > 0) {
				return 2;
			} else {
				return 4;
			}
		} else {
			if (yOffset < 0) {
				return 6;
			} else if (yOffset > 0) {
				return 1;
			} else {
				return -1;
			}
		}
	}

	// 22314

	public static final int getGraphicDefinitionsSize() {
		int lastArchiveId = Cache.STORE.getIndexes()[21].getLastArchiveId();
		return lastArchiveId * 256 + Cache.STORE.getIndexes()[21].getValidFilesCount(lastArchiveId);
	}

	public static final int getAnimationDefinitionsSize() {
		int lastArchiveId = Cache.STORE.getIndexes()[20].getLastArchiveId();
		return lastArchiveId * 128 + Cache.STORE.getIndexes()[20].getValidFilesCount(lastArchiveId);
	}

	public static final int getConfigDefinitionsSize() {
		int lastArchiveId = Cache.STORE.getIndexes()[22].getLastArchiveId();
		return lastArchiveId * 256 + Cache.STORE.getIndexes()[22].getValidFilesCount(lastArchiveId);
	}

	public static final int getObjectDefinitionsSize() {
		int lastArchiveId = Cache.STORE.getIndexes()[16].getLastArchiveId();
		return lastArchiveId * 256 + Cache.STORE.getIndexes()[16].getValidFilesCount(lastArchiveId);
	}

	public static final int getNPCDefinitionsSize() {
		int lastArchiveId = Cache.STORE.getIndexes()[18].getLastArchiveId();
		return lastArchiveId * 128 + Cache.STORE.getIndexes()[18].getValidFilesCount(lastArchiveId);
	}

	public static boolean itemExists(int id) {
		if (id >= getItemDefinitionsSize()) // setted because of custom items
		{
			return false;
		}
		return Cache.STORE.getIndexes()[19].fileExists(id >>> 8, 0xff & id);
	}

	public static final int getItemDefinitionsSize() {
		int lastArchiveId = Cache.STORE.getIndexes()[19].getLastArchiveId();
		return lastArchiveId * 256 + Cache.STORE.getIndexes()[19].getValidFilesCount(lastArchiveId);
	}

	public static double round(double value, int precision) {
		String zeros = "";
		for (int i = 0; i < precision; i++) {
			zeros += "" + 0;
		}
		return Double.parseDouble(new DecimalFormat("#." + zeros).format(value));
	}

	public static int getItemDefinitionsSize(Store store) {
		int lastArchiveId = store.getIndexes()[19].getLastArchiveId();
		return lastArchiveId * 256 + store.getIndexes()[19].getValidFilesCount(lastArchiveId);
	}

	public static final int getInterfaceDefinitionsSize() {
		return Cache.STORE.getIndexes()[3].getLastArchiveId() + 1;
	}

	public static void clearInterface(Player player, int interfaceId) {
		int componentLength = getInterfaceDefinitionsComponentsSize(interfaceId);
		for (int i = 0; i < componentLength; i++) {
			player.getPackets().sendIComponentText(interfaceId, i, "");
		}
	}

	public static final int getInterfaceDefinitionsComponentsSize(int interfaceId) {
		return Cache.STORE.getIndexes()[3].getLastFileId(interfaceId) + 1;
	}

	public static String formatPlayerNameForProtocol(String name) {
		if (name == null) {
			return "";
		}
		name = name.replaceAll(" ", "_");
		name = name.toLowerCase();
		return name;
	}

	public static String formatPlayerNameForURL(String name) {
		name = name.replaceAll(" ", "_");
		name = name.toLowerCase();
		String newName = "";
		boolean uppercased = false;
		for (int i = 0; i < name.toCharArray().length; i++) {
			char c = name.toCharArray()[i];
			if (!uppercased && name.toCharArray()[i] != '_') {
				c = Character.toUpperCase(c);
				uppercased = true;
			}
			newName = newName + "" + c;
		}
		return newName;
	}

	public static String formatPlayerNameForDisplay(String name) {
		if (name == null) {
			return "";
		}
		name = name.replaceAll("_", " ");
		name = name.toLowerCase();
		StringBuilder newName = new StringBuilder();
		boolean wasSpace = true;
		for (int i = 0; i < name.length(); i++) {
			if (wasSpace) {
				newName.append(("" + name.charAt(i)).toUpperCase());
				wasSpace = false;
			} else {
				newName.append(name.charAt(i));
			}
			if (name.charAt(i) == ' ') {
				wasSpace = true;
			}
		}
		return newName.toString();
	}

	public static final int getRandom(int maxValue) {
		return (int) (Math.random() * (maxValue + 1));
	}

	public static final int random(int min, int max) {
		final int n = Math.abs(max - min);
		return Math.min(min, max) + (n == 0 ? 0 : random(n));
	}

	public static final int random(int maxValue) {
		if (maxValue <= 0) {
			return 0;
		}
		return RANDOM.nextInt(maxValue);
	}

	public static final double random(double min, double max) {
		final double n = Math.abs(max - min);
		return Math.min(min, max) + (n == 0 ? 0 : random((int) n));
	}

	public static final int next(int max, int min) {
		return min + (int) (Math.random() * ((max - min) + 1));
	}

	public static final double getRandomDouble(double maxValue) {
		return (Math.random() * (maxValue + 1));

	}

	public static <K> K randomArraySlot(K[] array) {
		return array[random(array.length)];
	}

	public static final String longToString(long l) {
		if (l <= 0L || l >= 0x5b5b57f8a98a5dd1L) {
			return null;
		}
		if (l % 37L == 0L) {
			return null;
		}
		int i = 0;
		char ac[] = new char[12];
		while (l != 0L) {
			long l1 = l;
			l /= 37L;
			ac[11 - i++] = VALID_CHARS[(int) (l1 - l * 37L)];
		}
		return new String(ac, 12 - i, i);
	}

	public static boolean invalidAccountName(String name) {
		return name.length() < 2 || name.length() > 12 || name.startsWith("_") || name.endsWith("_") || name.contains("__") || containsInvalidCharacter(name);
	}

	public static boolean containsInvalidCharacter(String name) {
		for (char c : name.toCharArray()) {
			if (containsInvalidCharacter(c)) {
				return true;
			}
		}
		return false;
	}

	public static boolean containsInvalidCharacter(char c) {
		for (char vc : VALID_CHARS) {
			if (vc == c) {
				return false;
			}
		}
		return true;
	}

	public static boolean invalidAuthId(String auth) {
		return auth.length() != 10 || auth.contains("_") || containsInvalidCharacter(auth);
	}

	public static final long stringToLong(String s) {
		long l = 0L;
		for (int i = 0; i < s.length() && i < 12; i++) {
			char c = s.charAt(i);
			l *= 37L;
			if (c >= 'A' && c <= 'Z') {
				l += (1 + c) - 65;
			} else if (c >= 'a' && c <= 'z') {
				l += (1 + c) - 97;
			} else if (c >= '0' && c <= '9') {
				l += (27 + c) - 48;
			}
		}
		while (l % 37L == 0L && l != 0L) {
			l /= 37L;
		}
		return l;
	}

	public static final int packGJString2(int position, byte[] buffer, String String) {
		int length = String.length();
		int offset = position;
		for (int index = 0; length > index; index++) {
			int character = String.charAt(index);
			if (character > 127) {
				if (character > 2047) {
					buffer[offset++] = (byte) ((character | 919275) >> 12);
					buffer[offset++] = (byte) (128 | ((character >> 6) & 63));
					buffer[offset++] = (byte) (128 | (character & 63));
				} else {
					buffer[offset++] = (byte) ((character | 12309) >> 6);
					buffer[offset++] = (byte) (128 | (character & 63));
				}
			} else {
				buffer[offset++] = (byte) character;
			}
		}
		return offset - position;
	}

	public static final int calculateGJString2Length(String String) {
		int length = String.length();
		int gjStringLength = 0;
		for (int index = 0; length > index; index++) {
			char c = String.charAt(index);
			if (c > '\u007f') {
				if (c <= '\u07ff') {
					gjStringLength += 2;
				} else {
					gjStringLength += 3;
				}
			} else {
				gjStringLength++;
			}
		}
		return gjStringLength;
	}

	public static final int getNameHash(String name) {
		name = name.toLowerCase();
		int hash = 0;
		for (int index = 0; index < name.length(); index++) {
			hash = method1258(name.charAt(index)) + ((hash << 5) - hash);
		}
		return hash;
	}

	public static final byte method1258(char c) {
		byte charByte;
		if (c > 0 && c < '\200' || c >= '\240' && c <= '\377') {
			charByte = (byte) c;
		} else if (c != '\u20AC') {
			if (c != '\u201A') {
				if (c != '\u0192') {
					if (c == '\u201E') {
						charByte = -124;
					} else if (c != '\u2026') {
						if (c != '\u2020') {
							if (c == '\u2021') {
								charByte = -121;
							} else if (c == '\u02C6') {
								charByte = -120;
							} else if (c == '\u2030') {
								charByte = -119;
							} else if (c == '\u0160') {
								charByte = -118;
							} else if (c == '\u2039') {
								charByte = -117;
							} else if (c == '\u0152') {
								charByte = -116;
							} else if (c != '\u017D') {
								if (c == '\u2018') {
									charByte = -111;
								} else if (c != '\u2019') {
									if (c != '\u201C') {
										if (c == '\u201D') {
											charByte = -108;
										} else if (c != '\u2022') {
											if (c == '\u2013') {
												charByte = -106;
											} else if (c == '\u2014') {
												charByte = -105;
											} else if (c == '\u02DC') {
												charByte = -104;
											} else if (c == '\u2122') {
												charByte = -103;
											} else if (c != '\u0161') {
												if (c == '\u203A') {
													charByte = -101;
												} else if (c != '\u0153') {
													if (c == '\u017E') {
														charByte = -98;
													} else if (c != '\u0178') {
														charByte = 63;
													} else {
														charByte = -97;
													}
												} else {
													charByte = -100;
												}
											} else {
												charByte = -102;
											}
										} else {
											charByte = -107;
										}
									} else {
										charByte = -109;
									}
								} else {
									charByte = -110;
								}
							} else {
								charByte = -114;
							}
						} else {
							charByte = -122;
						}
					} else {
						charByte = -123;
					}
				} else {
					charByte = -125;
				}
			} else {
				charByte = -126;
			}
		} else {
			charByte = -128;
		}
		return charByte;
	}

	/**
	 * Convert a millisecond duration to a string format
	 *
	 * @param millis
	 * 		A duration to convert to a string form
	 * @return A string of the form "X Days Y Hours Z Minutes A Seconds".
	 */
	public static String getDurationBreakdown(long millis) {
		if (millis < 0) {
			throw new IllegalArgumentException("Duration must be greater than zero!");
		}

		long days = TimeUnit.MILLISECONDS.toDays(millis);
		millis -= TimeUnit.DAYS.toMillis(days);
		long hours = TimeUnit.MILLISECONDS.toHours(millis);
		millis -= TimeUnit.HOURS.toMillis(hours);
		long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
		millis -= TimeUnit.MINUTES.toMillis(minutes);
		long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

		StringBuilder sb = new StringBuilder(64);
		sb.append(days).append(" days ").append(hours).append(" hours ").append(minutes).append(" minutes ").append(seconds).append(" seconds");

		return (sb.toString());
	}

	public static final String getUnformatedMessage(int messageDataLength, int messageDataOffset, byte[] messageData) {
		char[] cs = new char[messageDataLength];
		int i = 0;
		for (int i_6_ = 0; i_6_ < messageDataLength; i_6_++) {
			int i_7_ = 0xff & messageData[i_6_ + messageDataOffset];
			if ((i_7_ ^ 0xffffffff) != -1) {
				if ((i_7_ ^ 0xffffffff) <= -129 && (i_7_ ^ 0xffffffff) > -161) {
					int i_8_ = aCharArray6385[i_7_ - 128];
					if (i_8_ == 0) {
						i_8_ = 63;
					}
					i_7_ = i_8_;
				}
				cs[i++] = (char) i_7_;
			}
		}
		return new String(cs, 0, i);
	}

	public static final byte[] getFormatedMessage(String message) {
		int i_0_ = message.length();
		byte[] is = new byte[i_0_];
		for (int i_1_ = 0; (i_1_ ^ 0xffffffff) > (i_0_ ^ 0xffffffff); i_1_++) {
			int i_2_ = message.charAt(i_1_);
			if (((i_2_ ^ 0xffffffff) >= -1 || i_2_ >= 128) && (i_2_ < 160 || i_2_ > 255)) {
				if ((i_2_ ^ 0xffffffff) != -8365) {
					if ((i_2_ ^ 0xffffffff) == -8219) {
						is[i_1_] = (byte) -126;
					} else if ((i_2_ ^ 0xffffffff) == -403) {
						is[i_1_] = (byte) -125;
					} else if (i_2_ == 8222) {
						is[i_1_] = (byte) -124;
					} else if (i_2_ != 8230) {
						if ((i_2_ ^ 0xffffffff) != -8225) {
							if ((i_2_ ^ 0xffffffff) != -8226) {
								if ((i_2_ ^ 0xffffffff) == -711) {
									is[i_1_] = (byte) -120;
								} else if (i_2_ == 8240) {
									is[i_1_] = (byte) -119;
								} else if ((i_2_ ^ 0xffffffff) == -353) {
									is[i_1_] = (byte) -118;
								} else if ((i_2_ ^ 0xffffffff) != -8250) {
									if (i_2_ == 338) {
										is[i_1_] = (byte) -116;
									} else if (i_2_ == 381) {
										is[i_1_] = (byte) -114;
									} else if ((i_2_ ^ 0xffffffff) == -8217) {
										is[i_1_] = (byte) -111;
									} else if (i_2_ == 8217) {
										is[i_1_] = (byte) -110;
									} else if (i_2_ != 8220) {
										if (i_2_ == 8221) {
											is[i_1_] = (byte) -108;
										} else if ((i_2_ ^ 0xffffffff) == -8227) {
											is[i_1_] = (byte) -107;
										} else if ((i_2_ ^ 0xffffffff) != -8212) {
											if (i_2_ == 8212) {
												is[i_1_] = (byte) -105;
											} else if ((i_2_ ^ 0xffffffff) != -733) {
												if (i_2_ != 8482) {
													if (i_2_ == 353) {
														is[i_1_] = (byte) -102;
													} else if (i_2_ != 8250) {
														if ((i_2_ ^ 0xffffffff) == -340) {
															is[i_1_] = (byte) -100;
														} else if (i_2_ != 382) {
															if (i_2_ == 376) {
																is[i_1_] = (byte) -97;
															} else {
																is[i_1_] = (byte) 63;
															}
														} else {
															is[i_1_] = (byte) -98;
														}
													} else {
														is[i_1_] = (byte) -101;
													}
												} else {
													is[i_1_] = (byte) -103;
												}
											} else {
												is[i_1_] = (byte) -104;
											}
										} else {
											is[i_1_] = (byte) -106;
										}
									} else {
										is[i_1_] = (byte) -109;
									}
								} else {
									is[i_1_] = (byte) -117;
								}
							} else {
								is[i_1_] = (byte) -121;
							}
						} else {
							is[i_1_] = (byte) -122;
						}
					} else {
						is[i_1_] = (byte) -123;
					}
				} else {
					is[i_1_] = (byte) -128;
				}
			} else {
				is[i_1_] = (byte) i_2_;
			}
		}
		return is;
	}

	public static char method2782(byte value) {
		int byteChar = 0xff & value;
		if (byteChar == 0) {
			throw new IllegalArgumentException("Non cp1252 character 0x" + Integer.toString(byteChar, 16) + " provided");
		}
		if ((byteChar ^ 0xffffffff) <= -129 && byteChar < 160) {
			int i_4_ = aCharArray6385[-128 + byteChar];
			if ((i_4_ ^ 0xffffffff) == -1) {
				i_4_ = 63;
			}
			byteChar = i_4_;
		}
		return (char) byteChar;
	}

	public static int getHashMapSize(int size) {
		size--;
		size |= size >>> -1810941663;
		size |= size >>> 2010624802;
		size |= size >>> 10996420;
		size |= size >>> 491045480;
		size |= size >>> 1388313616;
		return 1 + size;
	}

	/**
	 * Walk dirs 0 - South-West 1 - South 2 - South-East 3 - West 4 - East 5 - North-West 6 - North 7 - North-East
	 */
	public static int getPlayerWalkingDirection(int dx, int dy) {
		if (dx == -1 && dy == -1) {
			return 0;
		}
		if (dx == 0 && dy == -1) {
			return 1;
		}
		if (dx == 1 && dy == -1) {
			return 2;
		}
		if (dx == -1 && dy == 0) {
			return 3;
		}
		if (dx == 1 && dy == 0) {
			return 4;
		}
		if (dx == -1 && dy == 1) {
			return 5;
		}
		if (dx == 0 && dy == 1) {
			return 6;
		}
		if (dx == 1 && dy == 1) {
			return 7;
		}
		return -1;
	}

	public static int getPlayerRunningDirection(int dx, int dy) {
		if (dx == -2 && dy == -2) {
			return 0;
		}
		if (dx == -1 && dy == -2) {
			return 1;
		}
		if (dx == 0 && dy == -2) {
			return 2;
		}
		if (dx == 1 && dy == -2) {
			return 3;
		}
		if (dx == 2 && dy == -2) {
			return 4;
		}
		if (dx == -2 && dy == -1) {
			return 5;
		}
		if (dx == 2 && dy == -1) {
			return 6;
		}
		if (dx == -2 && dy == 0) {
			return 7;
		}
		if (dx == 2 && dy == 0) {
			return 8;
		}
		if (dx == -2 && dy == 1) {
			return 9;
		}
		if (dx == 2 && dy == 1) {
			return 10;
		}
		if (dx == -2 && dy == 2) {
			return 11;
		}
		if (dx == -1 && dy == 2) {
			return 12;
		}
		if (dx == 0 && dy == 2) {
			return 13;
		}
		if (dx == 1 && dy == 2) {
			return 14;
		}
		if (dx == 2 && dy == 2) {
			return 15;
		}
		return -1;
	}

	public static void completeQuickMessage(Player player, QuickChatMessage message) {
		QuickChatType type = message.getType();
		int[] params = message.getParams();
		for (int pos = 0; pos < type.getParamCount(); pos++) {
			switch (type.getParamType(pos)) {
				case STAT_BASE:
					params[pos] = player.getSkills().getLevelForXp(type.getParamKey(pos, 0));
					break;
				case ENUM_STRING_STATBASE:
					params[pos] = player.getSkills().getLevelForXp(type.getParamKey(pos, 1));
					break;
				case ACTIVECOMBATLEVEL:
					params[pos] = player.getSkills().getCombatLevelWithSummoning();
					break;
				case ENUM_STRING_CLAN://Friend chat rank
					FriendChatsManager fc = player.getCurrentFriendChat();
					if (fc != null) {
						params[pos] = fc.getRank(player.getPrimaryRight(), player.getUsername());
					} else {
						params[pos] = -2;
					}
					break;
				case ACC_GETCOUNT_WORLD://Friends chat user count
					fc = player.getCurrentFriendChat();
					if (fc != null) {
						params[pos] = fc.getPlayers().size();
					}
					break;
				case TOSTRING_VARP://Aka "config"
					switch (type.getParamKey(pos, 0)) {
						case 2421://Dominion factor
							params[pos] = (int) player.getDominionTower().getTotalScore();
							break;
						case 2639://Crucible score
							params[pos] = player.getCrucibleHighScore();
							break;
						//Add more items here if needed.
						default:
							if (GameConstants.DEBUG) {
								System.out.println("qc: " + type.getId() + ", varp: " + type.getParamKey(pos, 0));
							}
					}
					break;
				case TOSTRING_VARBIT://Aka "configByFile"
					switch (type.getParamKey(pos, 0)) {
						case 7198://Hitpoints
							params[pos] = player.getHitpoints();
							break;
						case 10057://Boss count
							params[pos] = player.getDominionTower().getKilledBossesCount();
							break;
						case 10118://Max climber floor
							params[pos] = player.getDominionTower().getMaxFloorClimber();
							break;
						case 10119://Max endurance floor
							params[pos] = player.getDominionTower().getMaxFloorEndurance();
							break;
						//Add more items here if needed.
						default:
							if (GameConstants.DEBUG) {
								System.out.println("qc: " + type.getId() + ", varbit: " + type.getParamKey(pos, 0));
							}
					}
					break;
			}
		}
	}

	public static byte[] completeQuickMessage(Player player, int fileId, byte[] data) {
		if (fileId == 1) {
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.AGILITY) };
		} else if (fileId == 8) {
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.ATTACK) };
		} else if (fileId == 13) {
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.CONSTRUCTION) };
		} else if (fileId == 16) {
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.COOKING) };
		} else if (fileId == 23) {
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.CRAFTING) };
		} else if (fileId == 30) {
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.DEFENCE) };
		} else if (fileId == 34) {
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.FARMING) };
		} else if (fileId == 41) {
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.FIREMAKING) };
		} else if (fileId == 47) {
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.FISHING) };
		} else if (fileId == 55) {
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.FLETCHING) };
		} else if (fileId == 62) {
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.HERBLORE) };
		} else if (fileId == 70) {
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.HITPOINTS) };
		} else if (fileId == 74) {
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.HUNTER) };
		} else if (fileId == 135) {
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.MAGIC) };
		} else if (fileId == 127) {
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.MINING) };
		} else if (fileId == 120) {
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.PRAYER) };
		} else if (fileId == 116) {
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.RANGE) };
		} else if (fileId == 111) {
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.RUNECRAFTING) };
		} else if (fileId == 103) {
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.SLAYER) };
		} else if (fileId == 96) {
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.SMITHING) };
		} else if (fileId == 92) {
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.STRENGTH) };
		} else if (fileId == 85) {
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.SUMMONING) };
		} else if (fileId == 79) {
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.THIEVING) };
		} else if (fileId == 142) {
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.WOODCUTTING) };
		} else if (fileId == 990) {
			data = new byte[] { (byte) player.getSkills().getLevelForXp(Skills.DUNGEONEERING) };
		} else if (fileId == 965) {
			int value = player.getHitpoints();
			data = new byte[] { (byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value };
		} else if (fileId == 1108) {
			int value = player.getDominionTower().getKilledBossesCount();
			data = new byte[] { (byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value };
		} else if (fileId == 1109) {
			long value = player.getDominionTower().getTotalScore();
			data = new byte[] { (byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value };
		} else if (fileId == 1110) {
			int value = player.getDominionTower().getMaxFloorClimber();
			data = new byte[] { (byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value };
		} else if (fileId == 1111) {
			int value = player.getDominionTower().getMaxFloorEndurance();
			data = new byte[] { (byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value };
		} else if (fileId == 1134) {
			int value = player.getCrucibleHighScore();
			data = new byte[] { (byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value };
		} else if (GameConstants.DEBUG) {
			System.out.println("qc: " + fileId + ", " + (data == null ? 0 : data.length));
		}
		return data;
	}

	public static String fixChatMessage(String message) {
		StringBuilder newText = new StringBuilder();
		boolean wasSpace = true;
		boolean exception = false;
		for (int i = 0; i < message.length(); i++) {
			if (!exception) {
				if (wasSpace) {
					newText.append(("" + message.charAt(i)).toUpperCase());
					if (!String.valueOf(message.charAt(i)).equals(" ")) {
						wasSpace = false;
					}
				} else {
					newText.append(("" + message.charAt(i)).toLowerCase());
				}
			} else {
				newText.append("").append(message.charAt(i));
			}
			if (String.valueOf(message.charAt(i)).contains(".") || String.valueOf(message.charAt(i)).contains("!") || String.valueOf(message.charAt(i)).contains("?")) {
				wasSpace = true;
			}
		}
		return newText.toString();
	}

	/**
	 * Gets all of the classes in a directory
	 *
	 * @param directory
	 * 		The directory to iterate through
	 * @return The list of classes
	 */
	public static List<Object> getClassesInDirectory(String directory) {
		List<Object> classes = new ArrayList<>();
		for (File file : new File("./bin/" + directory.replace(".", "/")).listFiles()) {
			if (file.getName().contains("$") || file.getName().contains("dropbox")) {
				continue;
			}
			try {
				Object objectEvent = (Class.forName(directory + "." + file.getName().replace(".class", "")).newInstance());
				classes.add(objectEvent);
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		return classes;
	}

	/**
	 * Gets all of the sub directories of a folder
	 */
	public static List<String> getSubDirectories(Object object) {
		String firstDirectory;
		if (object instanceof Class) {
			firstDirectory = ((Class<?>) object).getPackage().getName();
		} else if (object instanceof String) {
			firstDirectory = ((String) object);
		} else {
			throw new IllegalStateException();
		}
		String directory = "./bin/" + firstDirectory.replace(".", "/");
		File file = new File(directory);
		String[] directories = file.list((current, name) -> new File(current, name).isDirectory());
		return Arrays.asList(directories);
	}

	public static String format(Number number) {
		return NumberFormat.getIntegerInstance().format(number);
	}

	public static String numberToCashDigit(int number) {
		String r = new DecimalFormat("##0E0").format(number);
		r = r.replaceAll("E[0-9]", suffix[Character.getNumericValue(r.charAt(r.length() - 1)) / 3]);
		while (r.length() > MAX_LENGTH || r.matches("[0-9]+\\.[a-z]")) {
			r = r.substring(0, r.length() - 2) + r.substring(r.length() - 1);
		}
		return r;
	}

	/**
	 * Finds out if a certain event should happen, and if it should, return true;
	 *
	 * @param chance
	 * 		The chance of the event happening
	 * @return If the event should happen
	 */
	public static boolean percentageChance(int chance) {
		double x = (Math.random() * 100);
		return x < chance;
	}

	public static boolean isInRange(int x1, int y1, int size1, int x2, int y2, int size2, int maxDistance) {
		int distanceX = x1 - x2;
		int distanceY = y1 - y2;
		return !(distanceX > size2 + maxDistance || distanceX < -size1 - maxDistance || distanceY > size2 + maxDistance || distanceY < -size1 - maxDistance);
	}

	/**
	 * Finds an npc in the world by its id.
	 *
	 * @param id
	 * 		The npc id
	 */
	public static NPC findNPC(int id) {
		for (NPC npc : World.getNPCs()) {
			if (npc != null && npc.getId() == id) {
				return npc;
			}
		}
		return null;
	}

	/**
	 * Finds an npc in the players region by the id
	 *
	 * @param player
	 * 		The player
	 * @param id
	 * 		the id of the npc
	 */
	public static NPC findLocalNPC(Player player, int id) {
		for (NPC npc : World.getNPCs()) {
			if (npc == null || npc.getRegionId() != player.getRegionId()) {
				continue;
			}
			if (npc.getId() == id) {
				return npc;
			}
		}
		return null;
	}

	public static int get32BitValue(boolean[] array, boolean trueCondition) {
		int value = 0;
		for (int index = 1; index < array.length + 1; index++) {
			if (array[index - 1] == trueCondition) {
				value += 1 << index;
			}
		}
		return value;
	}

	public static boolean isNumeric(String str) {
		for (char c : str.toCharArray()) {
			if (!Character.isDigit(c)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * This method will find the difference between the two tiles and express them in terms of compass directions
	 * (NESW)
	 *
	 * @param base
	 * 		The start tile
	 * @param end
	 * 		The end tile
	 */
	public static String getTileInformations(WorldTile base, WorldTile end) {
		String information = "";

		int xDiff = base.getX() - end.getX();
		int yDiff = base.getY() - end.getY();

		if (xDiff > 0) {
			information += Math.abs(xDiff) + " tile(s) West";
		} else if (xDiff < 0) {
			information += Math.abs(xDiff) + " tile(s) East";
		}

		if (yDiff > 0) {
			information += ", " + Math.abs(yDiff) + " tile(s) South";
		} else if (yDiff < 0) {
			information += ", " + Math.abs(yDiff) + " tile(s) North";
		}
		return information;
	}

	/**
	 * This method clears all the text inside a file
	 *
	 * @param file
	 * 		The file location
	 */
	public static void clearFile(String file) {
		try (PrintWriter writer = new PrintWriter(file)) {
			writer.print("");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static int[] toPrimitive(Integer[] IntegerArray) {
		int[] result = new int[IntegerArray.length];
		for (int i = 0; i < IntegerArray.length; i++) {
			result[i] = IntegerArray[i];
		}
		return result;
	}

	public static void convertDumpedSpawn(String line) {
		String[] split = line.split(" - ");
		Integer npcId = Integer.parseInt(split[0]);
		Integer x = Integer.parseInt(split[1]);
		Integer y = Integer.parseInt(split[2]);
		Integer z = Integer.parseInt(split[3]);
		NPCSpawnLoader.addSpawn(npcId, new WorldTile(x, y, z), Direction.NORTH);
	}

	public static void convertMatrixSpawn(String line) {
		String[] split = line.split(" - ");
		Integer npcId = Integer.parseInt(split[0]);
		String[] spawnSplit = split[1].split(" ");
		Integer x = Integer.parseInt(spawnSplit[0]);
		Integer y = Integer.parseInt(spawnSplit[1]);
		Integer z = Integer.parseInt(spawnSplit[2]);
		NPCSpawnLoader.addSpawn(npcId, new WorldTile(x, y, z), Direction.NORTH);
	}

	public static void convertProjectInsanitySpawn(String line) {
		String[] split = line.split("spawn = ");
		String[] spawnSplit = split[1].split("\t");
		Integer npcId = Integer.parseInt(spawnSplit[0]);
		Integer x = Integer.parseInt(spawnSplit[1]);
		Integer y = Integer.parseInt(spawnSplit[2]);
		Integer z = Integer.parseInt(spawnSplit[3]);
		NPCSpawnLoader.addSpawn(npcId, new WorldTile(x, y, z), Direction.NORTH);
	}

	public static void writeTextToFile(String file, String text, boolean append) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, append))) {
			writer.write(text);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static class ValueComparator implements Comparator<Integer> {

		private final Map<Integer, Integer> base;

		public int compare(Integer a, Integer b) {
			return a.compareTo(b);
		}

		public ValueComparator(Map<Integer, Integer> base) {
			this.base = base;
		}

	}

	public static boolean clipedProjectile(WorldTile local, WorldTile tile, boolean checkClose, int size) {
		int myX = local.getX();
		int myY = local.getY();
		int destX = tile.getX();
		int destY = tile.getY();
		int lastTileX = myX;
		int lastTileY = myY;
		while (true) {
			if (myX < destX) {
				myX++;
			} else if (myX > destX) {
				myX--;
			}
			if (myY < destY) {
				myY++;
			} else if (myY > destY) {
				myY--;
			}
			int dir = Utils.getMoveDirection(myX - lastTileX, myY - lastTileY);
			if (dir == -1) {
				return false;
			}
			if (checkClose) {
				if (!World.checkWalkStep(local.getPlane(), lastTileX, lastTileY, dir, size)) {
					return false;
				}
			} else if (!World.checkProjectileStep(local.getPlane(), lastTileX, lastTileY, dir, size)) {
				return false;
			}
			lastTileX = myX;
			lastTileY = myY;
			if (lastTileX == destX && lastTileY == destY) {
				return true;
			}
		}
	}

	/**
	 * If we are running from a jar file
	 */
	public static boolean isJarFile() {
		String className = Utils.class.getName().replace('.', '/');
		String classJar = Utils.class.getResource("/" + className + ".class").toString();
		return classJar.startsWith("jar:");
	}

	public static boolean isOnRange(int x1, int y1, int size1, int x2, int y2, int size2, int maxDistance) {
		int distanceX = x1 - x2;
		int distanceY = y1 - y2;
		return !(distanceX > size2 + maxDistance || distanceX < -size1 - maxDistance || distanceY > size2 + maxDistance || distanceY < -size1 - maxDistance);
	}

	public static boolean isOnRange(Entity entity, Entity target, int rangeRatio) {
		return entity.getPlane() == target.getPlane() && isOnRange(entity.getX(), entity.getY(), entity.getSize(), target.getX(), target.getY(), target.getSize(), rangeRatio);
	}

	public static boolean inRange(int npcX, int npcY, int npcWidth, int npcHeight, int playerX, int playerY, int distance) {
		return (double) distance >= getRange(npcX, npcY, npcWidth, npcHeight, playerX, playerY);
	}

	public static double getRange(int npcX, int npcY, int npcWidth, int npcHeight, int playerX, int playerY) {
		return Math.hypot((double) npcX + (double) npcWidth / 2.0D - (double) playerX - 0.5D, (double) npcY + (double) npcHeight / 2.0D - (double) playerY - 0.5D);
	}

	/**
	 * Checks if the amount of time necessary has passed
	 *
	 * @param eventTime
	 * 		The time the certain event happened
	 * @param timeToCheck
	 * 		The amount of time that should have elapsed
	 */
	public static boolean timeHasPassed(long eventTime, long timeToCheck) {
		return System.currentTimeMillis() - eventTime >= timeToCheck;
	}

	public static Random getRandom() {
		return RANDOM;
	}
}
