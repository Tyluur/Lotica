package com.runescape.game.content.skills;

import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.game.world.entity.player.Player;

import java.util.Arrays;

public final class SkillCapeCustomizer {

	private SkillCapeCustomizer() {

	}

	public static void resetSkillCapes(Player player) {
		player.setMaxedCapeCustomized(Arrays.copyOf(ItemDefinitions.getItemDefinitions(20767).originalModelColors, 4));
		player.setCompletionistCapeCustomized(Arrays.copyOf(ItemDefinitions.getItemDefinitions(20769).originalModelColors, 4));
	}

	public static void startCustomizing(Player player, int itemId) {
		player.getAttributes().put("SkillcapeCustomizeId", itemId);
		int[] skillCape = itemId == 20767 ? player.getMaxedCapeCustomized() : player.getCompletionistCapeCustomized();
		player.getInterfaceManager().sendInterface(20);
		for (int i = 0; i < 4; i++) { player.getPackets().sendConfigByFile(9254 + i, skillCape[i]); }
		player.getPackets().sendIComponentModel(20, 55, player.getAppearence().isMale() ? ItemDefinitions.forId(itemId).getMaleWornModelId1() : ItemDefinitions.forId(itemId).getFemaleWornModelId1());
	}

	public static int getCapeId(Player player) {
		Integer id = (Integer) player.getAttributes().get("SkillcapeCustomizeId");
		if (id == null) { return -1; }
		return id;
	}

	public static void handleSkillCapeCustomizerColor(Player player, int colorId) {
		int capeId = getCapeId(player);
		if (capeId == -1) { return; }
		Integer part = (Integer) player.getAttributes().get("SkillcapeCustomize");
		if (part == null) { return; }
		int[] skillCape = capeId == 20767 ? player.getMaxedCapeCustomized() : player.getCompletionistCapeCustomized();
		skillCape[part] = colorId;
		player.getPackets().sendConfigByFile(9254 + part, colorId);
		player.getInterfaceManager().sendInterface(20);
	}

	public static void handleSkillCapeCustomizer(Player player, int buttonId) {
		int capeId = getCapeId(player);
		if (capeId == -1) { return; }
		int[] skillCape = capeId == 20767 ? player.getMaxedCapeCustomized() : player.getCompletionistCapeCustomized();
		if (buttonId == 58) { // reset
			if (capeId == 20767) {
				player.setMaxedCapeCustomized(Arrays.copyOf(ItemDefinitions.forId(capeId).originalModelColors, 4));
			} else {
				player.setCompletionistCapeCustomized(Arrays.copyOf(ItemDefinitions.forId(capeId).originalModelColors, 4));
			}
			for (int i = 0; i < 4; i++) { player.getPackets().sendConfigByFile(9254 + i, skillCape[i]); }
		} else if (buttonId == 34) { // detail top
			player.getAttributes().put("SkillcapeCustomize", 0);
			player.getInterfaceManager().sendInterface(19);
			player.getPackets().sendConfig(2174, skillCape[0]);
		} else if (buttonId == 71) { // background top
			player.getAttributes().put("SkillcapeCustomize", 1);
			player.getInterfaceManager().sendInterface(19);
			player.getPackets().sendConfig(2174, skillCape[1]);
		} else if (buttonId == 83) { // detail button
			player.getAttributes().put("SkillcapeCustomize", 2);
			player.getInterfaceManager().sendInterface(19);
			player.getPackets().sendConfig(2174, skillCape[2]);
		} else if (buttonId == 95) { // background button
			player.getAttributes().put("SkillcapeCustomize", 3);
			player.getInterfaceManager().sendInterface(19);
			player.getPackets().sendConfig(2174, skillCape[3]);
		} else if (buttonId == 114 || buttonId == 142) { // done / close
			player.getAppearence().generateAppearenceData();
			player.closeInterfaces();
		}
	}
}
