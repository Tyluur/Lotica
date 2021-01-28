package com.runescape.game.content.skills.slayer;

import com.runescape.cache.loaders.NPCDefinitions;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 24, 2015
 */
public enum SlayerTasks {

	EASY("Goblin", 81, 117, 18, 103, "Ghost", 1648, 1612) {
		@Override
		public int[] getAmounts() {
			return new int[] { 30, 70 };
		}

		@Override
		public int[] getCoinLoots() {
			return new int[] { 100_000, 25_0000 };
		}

		@Override
		public int getSlayerPointReward() {
			return 15;
		}
	},
	/** Medium tasks */
	MEDIUM(52, 125, 1643, 82, 1616, 1610, 112, "Bloodveld") {
		@Override
		public int[] getAmounts() {
			return new int[] { 30, 90 };
		}

		@Override
		public int[] getCoinLoots() {
			return new int[] { 100_000, 35_0000 };
		}

		@Override
		public int getSlayerPointReward() {
			return 40;
		}
	},
	/** Hard tasks */
	HARD(6221, 83, 8832, 8833, 8834, "Frost dragon", "Bronze dragon", "Iron dragon", "Steel Dragon", "Mithril Dragon", "Kurask", "Green dragon", "Blue dragon", 1624, 1604, 9172, 1610, 1613, 1615, 2783, 55, 49, 84, 13820, 13821, 13822) {
		@Override
		public int[] getAmounts() {
			return new int[] { 40, 120 };
		}

		@Override
		public int[] getCoinLoots() {
			return new int[] { 200_000, 450_000 };
		}

		@Override
		public int getSlayerPointReward() {
			return 75;
		}
	},
	/** Elite tasks */
	ELITE("Kalphite Queen", "Tormented demon", "Commander Zilyana", "Ice strykewyrm", "Glacor", 50, 6260, 6203, 6222) {
		@Override
		public int[] getAmounts() {
			return new int[] { 30, 70 };
		}

		@Override
		public int[] getCoinLoots() {
			return new int[] { 300_000, 650_000 };
		}

		@Override
		public int getSlayerPointReward() {
			return 100;
		}
	};

	/**
	 * The parameters applicable for this task type, can be an integer or string (id or name)
	 */
	private final Object[] parameters;

	/**
	 * The names of the slayer tasks
	 */
	private final List<String> names;

	/**
	 * The {@code Tasks} constructor. The parameters array will accept String and integers. The npc ids entered are
	 * transformed into a String for the task.
	 *
	 * @param parameters
	 * 		The parameters of the task. String or integers only.
	 */
	SlayerTasks(Object... parameters) {
		this.parameters = parameters;
		names = loadProperties();
	}

	/**
	 * Creates a list of all of the names that can be generated from the parameters array
	 */
	private List<String> getNamesApplicable() {
		return names;
	}

	/**
	 * The amounts of this slayer task we should give. The first index is the lowest amount possible, the second index
	 * is the highest amount possible.
	 */
	public abstract int[] getAmounts();

	/**
	 * The amount of coins we recieve after completing the task. The first index is the minimum, the second index is the
	 * maximum.
	 */
	public abstract int[] getCoinLoots();

	/**
	 * The amount of slayer points to give players after a task of this type
	 */
	public abstract int getSlayerPointReward();

	/**
	 * Loads all of the information into the list
	 */
	private List<String> loadProperties() {
		List<String> loadedNames = new ArrayList<>();
		for (Object object : parameters) {
			if (object instanceof String) {
				loadedNames.add((String) object);
			} else if (object instanceof Integer) {
				int id = (int) object;
				loadedNames.add(NPCDefinitions.getNPCDefinitions(id).getName());
			}
		}
		return loadedNames;
	}

	/**
	 * Finds all of the tasks available for the type you are looking for
	 *
	 * @param type
	 * 		The type of task you want @see {@link SlayerTasks}
	 */
	public static List<String> getTasksNames(SlayerTasks type) {
		for (SlayerTasks task : SlayerTasks.values()) {
			if (task == type) {
				return task.getNamesApplicable();
			}
		}
		return null;
	}

	/**
	 * Looks for the task that would give the name
	 *
	 * @param name
	 * 		The name to look for
	 * @return A {@code Tasks} {@code Object}
	 */
	public static SlayerTasks getTasksByName(String name) {
		for (SlayerTasks tasks : SlayerTasks.values()) {
			for (String names : tasks.getNamesApplicable()) {
				if (names.equalsIgnoreCase(name)) {
					return tasks;
				}
			}
		}
		return null;
	}
}