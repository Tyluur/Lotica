package com.runescape.game.content.skills.magic;

import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.game.world.entity.player.actions.Action;
import com.runescape.game.world.item.Item;

public class Enchanting {

	/**
	 * Jewelry stuff
	 */
	private static byte[] JEWLERY_LEVELS = { 7, 27, 49, 57, 68, 87 };
	private static short[][] JEWLERY_NODES = { { 1694, 1637, 1658, 11072 }, { 1639, 1658, 1696, 11076 }, { 1641, 1660, 1698, 11086 }, { 1643, 1662, 1700, 11092 }, { 1645, 1664, 1702, 11115 }, { 6575, 6581, 6577, 11130 } };
	private static short[][] JEWLERY_PRODUCT = { { 1727, 2550, 3853, 11074 }, { 2552, 12622, 1729, 11079 }, { 2568, 11194, 1725, 11088 }, { 2570, 11090, 1731, 11095 }, { 2572, 11105, 1712, 11118 }, { 6583, 6585, 11128, 11133 } };
	private static int[][] JEWLERY_RUNES = { { 564, 1, 555, 1 }, { 564, 1, 556, 1 }, { 564, 1, 554, 5 }, { 564, 1, 557, 10 }, { 557, 15, 555, 15 }, { 557, 20, 554, 20 }, };
	private static short[] JEWLERY_ANIMATIONS = { 719, 720, 721, 719, 720, 720 };
	private static short[] JEWLERY_GRAPHICS = { 114, 115, 115, 153, 154, 154 };
	private static double[] JEWLERY_EXPERIENCE = { 17.5, 37, 59, 67, 78, 97 };

	/**
	 * Bolt stuff
	 */
	private static short[] BOLT_NODES = { 879, 9337, 9335, 880, 9338, 9336, 9339, 9340, 9341, 9342 };
	private static short[] BOLT_PRODCUTS = { 9236, 9240, 9237, 9238, 9241, 9239, 9242, 9243, 9244, 9245 };
	private static int[][] BOLT_RUNES = { { 564, 1, 556, 2 }, { 564, 1, 555, 1, 558, 1 }, { 564, 1, 557, 2 }, { 564, 1, 555, 2 }, { 564, 1, 556, 3, 561, 1 }, { 564, 1, 554, 2 }, { 564, 1, 554, 5, 565, 1 }, { 564, 1, 557, 10, 563, 2 }, { 564, 1, 557, 15, 566, 1 }, { 564, 1, 554, 20, 560, 1 } };
	private static double[] BOLT_EXPERIENCE = { 9, 17, 19, 29, 37, 33, 59, 67, 78, 97 };
	private static byte[] BOLT_LEVELS = { 4, 7, 14, 24, 27, 29, 49, 57, 68, 87 };

	public static void processMagicEnchantSpell(Player player, int slot, int primaryIndex) {
		Item item = player.getInventory().getItem(slot);
		int secondaryIndex = -1;
		for (int i = 0; i < JEWLERY_NODES[primaryIndex].length; i++) {
			if (item.getId() == JEWLERY_NODES[primaryIndex][i]) {
				secondaryIndex = i;
				break;
			}
		}
		if (secondaryIndex == -1) {
			player.getPackets().sendGameMessage("This spell can only be cast on amulets, necklaces, rings, braclets or shapes in the training arena.");
			return;
		} else if (!Magic.checkSpellLevel(player, JEWLERY_LEVELS[primaryIndex])) {
			return;
		} else if (!Magic.checkRunes(player, true, JEWLERY_RUNES[primaryIndex])) {
			return;
		}
		player.getLockManagement().lockAll(3000);
		player.setNextAnimation(new Animation(JEWLERY_ANIMATIONS[primaryIndex]));
		player.setNextGraphics(new Graphics(JEWLERY_GRAPHICS[primaryIndex], 0, 100));
		player.getInterfaceManager().openGameTab(7);
		player.getSkills().addXp(Skills.MAGIC, JEWLERY_EXPERIENCE[primaryIndex]);
		player.getInventory().replaceItem(JEWLERY_PRODUCT[primaryIndex][secondaryIndex], 1, slot);
	}

	public static void processBoltEnchantSpell(Player player, final int index, final int ticks) {
		player.getLockManagement().lockAll(3000);
		player.getActionManager().setAction(new Action() {

			private int cycle = ticks;

			@Override
			public boolean start(Player player) {
				if (!Magic.checkSpellLevel(player, BOLT_LEVELS[index])) {
					return false;
				}
				player.closeInterfaces();
				return true;
			}

			@Override
			public boolean process(Player player) {
				return player.getInventory().getNumerOf(BOLT_NODES[index]) != 0 && cycle > 0;
			}

			@Override
			public int processWithDelay(Player player) {
				if (!Magic.checkRunes(player, true, BOLT_RUNES[index])) {
					return -1;
				}
				cycle--;
				int preBolt = BOLT_NODES[index];
				int boltAmount = player.getInventory().getNumerOf(preBolt);
				int createdCount = boltAmount > 10 ? 10 : boltAmount;
				player.setNextGraphics(new Graphics(759));
				player.setNextAnimation(new Animation(4462));
				player.getInventory().deleteItem(preBolt, createdCount);
				player.getInventory().addItem(BOLT_PRODCUTS[index], createdCount);
				player.getSkills().addXp(Skills.MAGIC, BOLT_EXPERIENCE[index]);
				return 2;
			}

			@Override
			public void stop(Player player) {
				setActionDelay(player, 3);
			}
		});
	}

	public static int getComponentIndex(int componentId) {
		if (componentId == 14) {
			return 0;
		} else if (componentId == 29) {
			return 1;
		} else if (componentId == 18) {
			return 2;
		} else if (componentId == 22) {
			return 3;
		} else if (componentId == 32) {
			return 4;
		} else if (componentId == 26) {
			return 5;
		} else if (componentId == 35) {
			return 6;
		} else if (componentId == 38) {
			return 7;
		} else if (componentId == 41) {
			return 8;
		} else if (componentId == 44) {
			return 9;
		}
		return -1;
	}

	public static int getJewleryIndex(int spellId) {
		switch (spellId) {
			case 29:
				return 0;
			case 41:
				return 1;
			case 53:
				return 2;
			case 61:
				return 3;
			case 76:
				return 4;
			case 88:
				return 5;
		}
		return 0;
	}
}
