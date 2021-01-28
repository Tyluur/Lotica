package com.runescape.game.interaction.dialogues.impl.skills;

import com.runescape.game.content.skills.SkillsDialogue;
import com.runescape.game.content.skills.SkillsDialogue.ItemNameFilter;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.game.world.entity.player.actions.Action;

public class PotteryWheel extends Dialogue {

	private static final int[] PRODUCTS = { 1787, 1789, 1791, 5352 };
	private static final int[] LEVELS = { 1, 7, 8, 19 };
	private static final double[] EXPERIENCE = { 6.3, 15, 18, 20 };
	private static final int SOFT_CLAY = 1761;

	@Override
	public void start() {
		SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.MAKE, "How many hides would you like to tan?<br>Choose a number, then click the pot to begin.", 28, PRODUCTS, new ItemNameFilter() {
			int count = 0;

			@Override
			public String rename(String name) {
				int levelRequired = LEVELS[count++];
				if (player.getSkills().getLevel(Skills.CRAFTING) < levelRequired)
					name = "<col=ff0000>" + name + "<br><col=ff0000>Level " + levelRequired;
				return name;
			}
		});
	}

	@Override
	public void run(int interfaceId, int componentId) {
		final int componentIndex = SkillsDialogue.getItemSlot(componentId);
		if (componentIndex > PRODUCTS.length) {
			end();
			return;
		}
		player.getActionManager().setAction(new Action() {
			int ticks;

			@Override
			public boolean start(Player player) {

				int clayAmount = player.getInventory().getAmountOf(SOFT_CLAY);
				if (clayAmount == 0) {
					end();
					player.getPackets().sendGameMessage("You need soft clay in order to spin any pot.");
					return false;
				}
				final int levelReq = LEVELS[componentIndex];
				if (player.getSkills().getLevel(Skills.CRAFTING) < levelReq) {
					end();
					player.getPackets().sendGameMessage("You need a crafting level of " + levelReq + " in order to spin this.");
					return false;
				}
				int requestedAmount = SkillsDialogue.getQuantity(player);
				if (requestedAmount > clayAmount)
					requestedAmount = clayAmount;
				ticks = requestedAmount;
				return true;
			}

			@Override
			public boolean process(Player player) {
				return ticks > 0;
			}

			@Override
			public int processWithDelay(Player player) {
				ticks--;
				player.setNextAnimation(new Animation(896));
				player.getSkills().addXp(Skills.CRAFTING, EXPERIENCE[componentIndex]);
				player.getInventory().deleteItem(SOFT_CLAY, 1);
				player.getInventory().addItem(PRODUCTS[componentIndex], 1);
				return 4;
			}

			@Override
			public void stop(Player player) {
				setActionDelay(player, 3);
			}
		});
	}

	@Override
	public void finish() {

	}
}
