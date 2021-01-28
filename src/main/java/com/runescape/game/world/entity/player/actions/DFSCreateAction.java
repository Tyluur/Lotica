package com.runescape.game.world.entity.player.actions;

import com.runescape.game.interaction.dialogues.impl.item.SimpleItemMessage;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleMessage;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.player.LockManagement.LockType;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since Mar 23, 2014
 */
public class DFSCreateAction extends Action {

	private int ticksPassed;

	@Override
	public boolean start(Player player) {
		if (!checkAll(player)) {
			return false;
		}
		player.getLockManagement().lockActions(10000, LockType.PLAYER_INTERACTION, LockType.INTERFACE_INTERACTION, LockType.ITEM_INTERACTION, LockType.FOOD, LockType.WALKING);
		return true;
	}

	@Override
	public boolean process(Player player) {
		return checkAll(player);
	}

	@Override
	public int processWithDelay(Player player) {
		ticksPassed = ticksPassed + 1;
		switch (ticksPassed) {
			case 1:
				player.setNextAnimation(new Animation(898));
				player.getDialogueManager().startDialogue(SimpleItemMessage.class, 11286, "You set to work, trying to attach the ancient draconic", "visage to your anti-dragonbreath shield. It's not easy to", "work with the ancient artifact and it takes all of your", "skill as a master smith.");
				break;
			case 4:
			case 6:
			case 8:
				player.setNextAnimation(new Animation(898));
				break;
			case 10:
				player.getLockManagement().unlockAll();
				player.getInventory().deleteItem(11286, 1);
				player.getInventory().deleteItem(1540, 1);
				player.getInventory().addItem(11283, 1);
				player.getSkills().addXpNoModifier(Skills.SMITHING, 50000);
				player.stopAll();

				player.getDialogueManager().startDialogue(SimpleItemMessage.class, 11283, "Even for an expert armourer it is not an easy task,", "but eventually it is ready. You have crafted the", "draconic visage and anti-dragonbreath shield into a", "dragonfire shield");
				break;
		}
		return 0;
	}

	@Override
	public void stop(Player player) {

	}

	private boolean checkAll(Player player) {
		if (player.getSkills().getLevelForXp(Skills.SMITHING) < 90) {
			player.getDialogueManager().startDialogue(SimpleMessage.class, "You need a smithing level of 90 to smith a dragon fire shield.");
			return false;
		}
		if (!player.getInventory().containsItems(new int[] { 11286, 2347, 1540 }, new int[] { 1, 1, 1 })) {
			player.getDialogueManager().startDialogue(SimpleMessage.class, "You need to have a visage, hammer, and anti-", "dragon fire shield in your inventory to do this.");
			return false;
		}
		return true;
	}

}
