package com.runescape.game.content;

import com.runescape.cache.Cache;
import com.runescape.cache.loaders.ClientScriptMap;
import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;
import com.runescape.utility.external.gson.GsonStartup;
import com.runescape.utility.external.gson.loaders.ExchangeItemLoader;
import com.runescape.utility.external.gson.loaders.ItemInformationLoader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ArmourSets {

	public static final int CHISEL = 1755;

	public static void main(String[] args) throws IOException {
		Cache.init();
		GsonStartup.loadAll();
		ExchangeItemLoader itemLoader = GsonStartup.getClass(ExchangeItemLoader.class);
		for (Sets set : Sets.values()) {
			if (!itemLoader.isUnlimited(set.setId)) {
				continue;
			}
			long piecePrice = 0L;
			for (int item : set.items) {
				int exchangePrice = ItemDefinitions.getItemDefinitions(item).getExchangePrice();
				piecePrice += exchangePrice;
			}
			int setPrice = ItemDefinitions.getItemDefinitions(set.setId).getExchangePrice();
			if (piecePrice > setPrice) {
				System.out.println(set + "[" + set.setId + "] needs to have a price of " + Utils.format(piecePrice) + "\t\tcurrent=" + Utils.format(setPrice));
			}
		}
		System.exit(-1);
	}

	public enum Sets { // Hours of finding id's :S - Sonic

		BRONZE_LG(11814, 1155, 1117, 1075, 1189),
		BRONZE_SK(11816, 1155, 1117, 1087, 1189),
		IRON_LG(11818, 1153, 1115, 1067, 1191),
		IRON_SK(11820, 1153, 1115, 1081, 1191),
		STEEL_LG(11822, 1157, 1119, 1069, 1193),
		STEEL_SK(11824, 1157, 1119, 1083, 1193),
		BLACK_LG(11826, 1165, 1125, 1077, 1195),
		BLACK_SK(11828, 1165, 1125, 1089, 1195),
		MITHRIL_LG(11830, 1159, 1121, 1071, 1197),
		MITHRIL_SK(11832, 1159, 1121, 1085, 1197),
		ADAMANT_LG(11834, 1161, 1123, 1073, 1199),
		ADAMANT_SK(11836, 1161, 1123, 1091, 1199),
		PROSELYTE_LG(9666, 9672, 9674, 9676),
		PROSELYTE_SK(9670, 9672, 9674, 9678),
		RUNE_LG(11838, 1163, 1127, 1079, 1201),
		RUNE_SK(11840, 1163, 1127, 1093, 1201),
		DRAG_CHAIN_LG(11842, 1149, 2513, 4087, 1187),
		DRAG_CHAIN_SK(11844, 1149, 2513, 4585, 1187),
		DRAG_PLATE_LG(14529, 11335, 14479, 4087, 1187),
		DRAG_PLATE_SK(14531, 11335, 14479, 4585, 1187),
		BLACK_H1_LG(19520, 10306, 19167, 7332, 19169),
		BLACK_H1_SK(19530, 10306, 19167, 7332, 19171),
		BLACK_H2_LG(19522, 10308, 19188, 7338, 19190),
		BLACK_H2_SK(19532, 10308, 19188, 7338, 19192),
		BLACK_H3_LG(19524, 10310, 19209, 7344, 19211),
		BLACK_H3_SK(19534, 10310, 19209, 7344, 19213),
		BLACK_H4_LG(19526, 10312, 19230, 7350, 19232),
		BLACK_H4_SK(19536, 10312, 19230, 7350, 19234),
		BLACK_H5_LG(19528, 10314, 19251, 7356, 19253),
		BLACK_H5_SK(19538, 10314, 19251, 7356, 19255),
		BLACK_TRIM_LG(11878, 2587, 2583, 2585, 2589),
		BLACK_TRIM_SK(11880, 2587, 2583, 3472, 2589),
		BLACK_GTRIM_LG(11878, 2595, 2591, 2593, 2597),
		BLACK_GTRIM_SK(11880, 2595, 2591, 3473, 2597),
		ADAMANT_H1_LG(19540, 10296, 19173, 7334, 19175),
		ADAMANT_H1_SK(19550, 10296, 19173, 7334, 19177),
		ADAMANT_H2_LG(19542, 10298, 19194, 7340, 19196),
		ADAMANT_H2_SK(19552, 10298, 19194, 7340, 19198),
		ADAMANT_H3_LG(19544, 10300, 19215, 7346, 19217),
		ADAMANT_H3_SK(19554, 10300, 19215, 7346, 19219),
		ADAMANT_H4_LG(19546, 10302, 19236, 7352, 19238),
		ADAMANT_H4_SK(19556, 10302, 19236, 7352, 19240),
		ADAMANT_H5_LG(19548, 10304, 19257, 7358, 19259),
		ADAMANT_H5_SK(19558, 10304, 19257, 7358, 19261),
		ADAMANT_TRIM_LG(11886, 2605, 2599, 2601, 2603),
		ADAMANT_TRIM_SK(11888, 2605, 2599, 3474, 2603),
		ADAMANT_GTRIM_LG(11890, 2613, 2607, 2609, 2611),
		ADAMANT_GTRIM_SK(11892, 2613, 2607, 3475, 2611),
		RUNE_H1_LG(19560, 10286, 19179, 19182, 7336),
		RUNE_H1_SK(19570, 10286, 19179, 19185, 7336),
		RUNE_H2_LG(19562, 10288, 19200, 19203, 7342),
		RUNE_H2_SK(19572, 10288, 19200, 19206, 7342),
		RUNE_H3_LG(19564, 10290, 19221, 19224, 7348),
		RUNE_H3_SK(19574, 10290, 19221, 19227, 7348),
		RUNE_H4_LG(19566, 10292, 19242, 19245, 7354),
		RUNE_H4_SK(19576, 10292, 19242, 19248, 7354),
		RUNE_H5_LG(19568, 10294, 19263, 19266, 7360),
		RUNE_H5_SK(19578, 10294, 19263, 19269, 7360),
		RUNE_TRIM_LG(11894, 2627, 2623, 2625, 2629),
		RUNE_TRIM_SK(11896, 2627, 2623, 3477, 2629),
		RUNE_GTRIM_LG(11898, 2619, 2615, 2617, 2621),
		RUNE_GTRIM_SK(11900, 2619, 2615, 3676, 2621),
		GUTHIX_LG(11926, 2673, 2669, 2671, 2675),
		GUTHIX_SK(11932, 2673, 2669, 3480, 2675),
		SARADOMIN_LG(11928, 2665, 2661, 2663, 2667),
		SARADOMIN_SK(11934, 2665, 2661, 3479, 2667),
		ZAMORAK_LG(11930, 2657, 2653, 2655, 2659),
		ZAMORAK_SK(11936, 2657, 2653, 3478, 2659),
		ROCKSHELL(11942, 6128, 6129, 6130, 6151, 6145),
		ELITEBLACK(14527, 14494, 14492, 14490),
		DAGONHAI(14525, 14499, 14497, 14501),
		THIRDAGEMELEE(11858, 10350, 10348, 10352, 10346),
		THIRDAGERANGE(11860, 10334, 10330, 10332, 10336),
		THIRDAGEMAGE(11862, 10342, 10334, 10338, 10340),
		THIRDAGEPRAYER(19580, 19308, 19311, 19314, 19317, 19320),
		GREEN_DHIDE(11864, 1099, 1135, 1065),
		BLUE_DHIDE_SET(11866, 2493, 2499, 2487),
		RED_DHIDE_SET(11868, 2495, 2501, 2489),
		BLACK_DHIDE_SET(11870, 2497, 2503, 2491),
		AHRIM_ARMOUR_SET(11846, 4708, 4710, 4712, 4714),
		DHAROK_ARMOUR_SET(11848, 4716, 4718, 4720, 4722),
		GUTHAN_ARMOUR_SET(11850, 4724, 4726, 4728, 4730),
		KARIL_ARMOUR_SET(11852, 4732, 4734, 4736, 4738),
		TORAG_ARMOUR_SET(11854, 4745, 4747, 4749, 4751),
		VERAC_ARMOUR_SET(11856, 4753, 4755, 4757, 4759),
		BLUE_MYSTIC_SET(11872, 4089, 4091, 4093, 4095, 4097),
		LIGHT_MYSTIC_SET(11960, 4109, 4111, 4113, 4115, 4117),
		DARK_MYSTIC_SET(11962, 4099, 4101, 4103, 4105, 4107),
		INITIATE_SET(9668, 5574, 5575, 5576),
		DWARF_CANNON_SET(11967, 6, 8, 10, 12),
		SPLITBARK(11876, 3385, 3387, 3389, 3391, 3393),

		GREEN_DRAGONHIDE(11864, 1135, 1099, 1065),
		BLUE_DRAGONHIDE(11866, 2499, 2493, 2487),
		RED_RAGONHIDE(11868, 2501, 2495, 2489),
		BLACK_DRAGONHIDE(11870, 2503, 2497, 2491),
		ROYAL_DRAGONHIDE(24386, 24382, 24376, 24379),
		MYSTIC_ROBES(11872, 4089, 4091, 4093, 4095, 4097),
		LIGHT_MYSTIC_ROBES(11960, 4109, 4111, 4113, 4115, 4117),
		DARK_MYSTIC_ROBES(11962, 4099, 4101, 4103, 4105, 4107),
		INFINITY_ROBES(11874, 6918, 6916, 6924, 6922, 6920),
		ENCHANTED(11902, 7400, 7399, 7398),
		WIZARD_TRIM(11904, 7396, 7392, 7388),
		WIZARD_GOLD_TRIM(11906, 7394, 7390, 7386),
		TRIMMED_LEATHER(11908, 7364, 7368),
		TRIMMED_LEATHER_GOLD(11910, 7362, 7366),
		GREEN_DHIDE_TRIMMED(11912, 7372, 7380),
		GREEN_DHIDE_TRIMMED_GOLD(11914, 7370, 7378),
		BLUE_DHIDE_TRIMMED(11916, 7376, 7384),
		BLUE_DHIDE_TRIMMED_GOLD(11918, 7374, 7382),
		GREEN_DHIDE_BLESSED(11920, 10378, 10380, 10376, 10382),
		BLUE_DHIDE_BLESSED(11922, 10386, 10388, 10384, 10390),
		RED_DHIDE_BLESSED(11924, 10370, 10372, 10368, 10374),
		BROWN_DHIDE_BLESSED(19582, 19453, 19455, 19451, 19457),
		SILVER_DHIDE_BLESSED(19586, 19461, 19463, 19459, 19465),
		PURPLE_DHIDE_BLESSED(19584, 19445, 19447, 19443, 19449),
		BANDOS_LG(19592, 19437, 19428, 19431, 19440),
		BANDOS_SK(19594, 19437, 19428, 19434, 19440),
		ARMADYL_LG(19588, 19422, 19413, 19416, 19425),
		ARMADYL_SK(19590, 19422, 19413, 19419, 19425),
		ANCIENT_LG(19596, 19407, 19398, 19401, 19410),
		ANCIENT_SK(19598, 19407, 19398, 19404, 19410),
		GILDED_LG(11938, 3486, 3481, 3483, 3488),
		GILDED_SK(11940, 3486, 3481, 3485, 3488),
		SPINED(11944, 6131, 6133, 6135, 6143, 6149),
		SKELETAL(11496, 6137, 6139, 6141, 6147, 6153),
		DWARF_CANNON(11967, 6, 8, 10, 12),
		TORVA(30901, 20135, 20139, 20143, 24977, 24983),
		VIRTUS(30903, 20159, 20163, 20167, 24974, 24986),
		PERNIX(30905, 20147, 20151, 20155, 24980, 24989);

		private int setId;

		private int[] items;

		Sets(int setId, int... items) {
			this.setId = setId;
			this.items = items;
		}

		private static Map<Integer, Sets> sets = new HashMap<Integer, Sets>();

		public static Sets forId(int itemId) {
			return sets.get(itemId);
		}

		static {
			for (Sets set : Sets.values()) {
				sets.put(set.getId(), set);
			}
		}

		public int getId() {
			return setId;
		}

		public int[] getItems() {
			return items;
		}
	}

	public static Sets getSet(int id) {
		for (Sets set : Sets.values()) { if (set.setId == id) { return set; } }
		return null;
	}

	public static void exchangeSets(Player player, Sets set) {
		if (set != null) {
			player.getInventory().deleteItem(set.getId(), 1);
			for (int exchangedId : set.getItems()) {
				player.getInventory().addItemDrop(exchangedId, 1);
			}
		}
	}

	public static Sets getArmourSet(int itemUsedId, int itemUsedWithId) {
		Sets set = Sets.forId(itemUsedId);
		int selected;
		if (set != null) {
			selected = itemUsedWithId;
		} else {
			set = Sets.forId(itemUsedWithId);
			selected = itemUsedId;
		}
		return set != null && CHISEL == selected ? set : null;
	}

	public static void openSets(Player player) {
		player.getInterfaceManager().sendInterface(645);
		player.getInterfaceManager().sendInventoryInterface(644);
		player.getPackets().sendIComponentSettings(645, 16, 0, 115, 14);
		player.getPackets().sendUnlockIComponentOptionSlots(644, 0, 0, 27, 0, 1, 2);
		player.getPackets().sendInterSetItemsOptionsScript(644, 0, 93, 4, 7, "Components", "Exchange", "Examine");
		player.getPackets().sendRunScript(676);
	}

	public static void sendComponents(Player player, int id) {
		Sets set = getSet(id);
		if (set == null) {
			player.getPackets().sendGameMessage("This isn't a set item.");
			return;
		}
		String message = ClientScriptMap.getMap(1088).getStringValue(id);
		if (message == null) { return; }
		player.getPackets().sendGameMessage(message);
	}

	public static void exchangeSet(Player player, int id) {
		Sets set = getSet(id);
		if (set == null) {
			player.getPackets().sendGameMessage("This isn't a set item.");
			return;
		}
		for (int itemId : set.items) {
			if (!player.getInventory().containsItem(itemId, 1)) {
				player.getPackets().sendGameMessage("You don't have the parts to make up this set.");
				return;
			}
		}
		for (int itemId : set.items) { player.getInventory().deleteItem(itemId, 1); }
		player.getInventory().addItem(id, 1);
	}

	public static void examineSet(Player player, int id) {
		Sets set = getSet(id);
		if (set == null) {
			player.getPackets().sendGameMessage("This isn't a set item.");
			return;
		}
		player.getPackets().sendGameMessage(ItemInformationLoader.getExamine(id));
	}

}
