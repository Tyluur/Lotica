package com.runescape.game.interaction.dialogues.impl.skills;

import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.game.content.skills.SkillsDialogue;
import com.runescape.game.content.skills.SkillsDialogue.ItemNameFilter;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.game.world.entity.player.actions.Action;
import com.runescape.game.world.item.Item;

public class SpinningD extends Dialogue {

	public static final int[] INGREDIENT = { 1737, 15415, 1779, 9436, 6051, 10814 };
	public static final int[] PRODUCT = { 1759, 15416, 1777, 9438, 6038, 954 };
	public static final int[] LEVELS = { 1, 1, 1, 10, 19, 30 };
	public static final double[] EXPERIENCE = { 2.5, 2.5, 15, 15, 30, 25 };

	@Override
	public void start() {
		SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.MAKE, "How many strings would you like to make?<br>Choose a number, then click the string to begin.", 28, PRODUCT, new ItemNameFilter() {
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
	public void run(int interfaceId, final int componentId) {
		final int componentIndex = SkillsDialogue.getItemSlot(componentId);
		if (componentIndex > INGREDIENT.length) {
			end();
			return;
		}
		player.getActionManager().setAction(new Action() {

			int ticks;

			@Override
			public boolean start(final Player player) {
				final int levelReq = LEVELS[componentIndex];
				if (player.getSkills().getLevel(Skills.CRAFTING) < levelReq) {
					end();
					player.getPackets().sendGameMessage("You need a Crafting level of " + levelReq + " in order to spin a " + ItemDefinitions.getItemDefinitions(PRODUCT[componentIndex]).getName().toLowerCase() + ".");
					return false;
				}
				int leatherAmount = player.getInventory().getAmountOf(INGREDIENT[componentIndex]);
				if (leatherAmount == 0) {
					player.getPackets().sendGameMessage("You need a piece of " + ItemDefinitions.getItemDefinitions(INGREDIENT[componentIndex]).getName().toLowerCase() + " in order to make a " + ItemDefinitions.getItemDefinitions(PRODUCT[componentIndex]).getName().toLowerCase() + ".");
					end();
					return false;
				}
				int requestedAmount = SkillsDialogue.getQuantity(player);
				if (requestedAmount > leatherAmount)
					requestedAmount = leatherAmount;
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
				player.getInventory().deleteItem(new Item(INGREDIENT[componentIndex], 1));
				player.getInventory().addItem(new Item(PRODUCT[componentIndex], 1));
				return 4;
			}

			@Override
			public void stop(Player player) {
				setActionDelay(player, 3);
			}
		});
		end();
	}

	@Override
	public void finish() {
	}
}
