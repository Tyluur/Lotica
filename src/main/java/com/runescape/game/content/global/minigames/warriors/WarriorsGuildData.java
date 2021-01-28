package com.runescape.game.content.global.minigames.warriors;

import com.runescape.game.world.item.Item;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since May 4, 2014
 */
public class WarriorsGuildData {

	/**
	 * @author Tyluur <ItsTyluur@Gmail.com>
	 * @since 2012-07-29
	 */
	public enum WarriorSet {

		BRONZE(new Item[] { new Item(1075), new Item(1117), new Item(1155) }), 
		IRON(new Item[] { new Item(1153), new Item(1115), new Item(1067) }),
		STEEL(new Item[] { new Item(1157), new Item(1119), new Item(1069) }),
		BLACK(new Item[] { new Item(1165), new Item(1125), new Item(1077) }), 
		MITHRIL(new Item[] { new Item(1159), new Item(1121), new Item(1071) }), 
		ADAMANT(new Item[] { new Item(1161), new Item(1123), new Item(1073) }), 
		RUNE(new Item[] { new Item(1127), new Item(1079), new Item(1163) });

		private static Map<String, WarriorSet> MAP = new HashMap<String, WarriorSet>();

		static {
			for (WarriorSet monster : WarriorSet.values()) {
				MAP.put(monster.name().toLowerCase(), monster);
			}
		}

		public static WarriorSet getByName(String name) {
			return MAP.get(name);
		}

		private Item[] armour;

		WarriorSet(Item[] armour) {
			this.armour = armour;
		}

		public Item[] getArmour() {
			return armour;
		}
		
		public int[] getArmourIds() {
			int[] ids = new int[armour.length];
			for (int i = 0; i < armour.length; i++) {
				ids[i] = armour[i].getId();
			}
			return ids;
		}
	}
}
