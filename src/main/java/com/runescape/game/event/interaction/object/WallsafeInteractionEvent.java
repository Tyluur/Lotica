package com.runescape.game.event.interaction.object;

import com.runescape.game.event.interaction.type.ObjectInteractionEvent;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.Hit;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.game.world.entity.player.actions.Action;
import com.runescape.game.world.item.Item;
import com.runescape.utility.Utils;
import com.runescape.utility.world.ClickOption;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 9/17/2016
 */
public class WallsafeInteractionEvent extends ObjectInteractionEvent {

	/** Possible items you can get from cracking the safe. */
	public static final Item LOOT[] = { new Item(1617), new Item(1619), new Item(1621), new Item(1623), new Item(1623), new Item(995, 20), new Item(995, 40) };

	@Override
	public int[] getKeys() {
		return new int[] { 7236 };
	}

	@Override
	public boolean handleObjectInteraction(Player player, WorldObject object, ClickOption option) {
		player.getActionManager().setAction(new Action() {
			@Override
			public boolean start(Player player) {
				if (!canSteal(player)) {
					return false;
				}
				int delay = timetoCrack(player);
				player.faceObject(object);
				player.setNextAnimation(new Animation(881));
				player.getPackets().sendGameMessage("You attempt to pick the safe...");
				setActionDelay(player, delay);
				return true;
			}

			@Override
			public boolean process(Player player) {
				player.setNextAnimation(new Animation(881));
				return canSteal(player);
			}

			@Override
			public int processWithDelay(Player player) {
				if (failChance(player) == 0) {
					player.sendMessage("You slip and trigger a trap!");
					int agilityLevel = player.getSkills().getLevelForXp(Skills.AGILITY);
					int damage = agilityLevel == 99 ? 10 : agilityLevel > 79 ? 20 : agilityLevel > 49 ? 30 : 40;
					player.applyHit(new Hit(player, damage));
					player.setNextAnimation(new Animation(404));
				} else {
					player.getSkills().addXp(Skills.THIEVING, 70);
					player.getInventory().addItem(Utils.randomArraySlot(LOOT));
					player.getLockManagement().unlockAll();
				}
				return -1;
			}

			@Override
			public void stop(Player player) {
				player.setNextAnimation(new Animation(-1));
			}

			private boolean canSteal(Player player) {
				if (player.getSkills().getLevelForXp(Skills.THIEVING) < 50) {
					player.sendMessage("You need a thieving level of 50 to crack this safe.");
					return false;
				}
				if (!player.getInventory().hasFreeSlots()) {
					player.sendMessage("You do not have any space left in your inventory.");
					return false;
				}
				return true;
			}

		});
		return true;
	}

	/**
	 * Adds on to the time that it takes to crack the safe.
	 *
	 * @param player
	 * 		the player trying to crack the safe.
	 * @return the time based on the thieving level of the player.
	 */
	private int timetoCrack(Player player) {
		if (player.getInventory().contains(5560)) {
			return (10 - (int) Math.floor(player.getSkills().getLevel(Skills.THIEVING) / 10) + Utils.getRandom().nextInt(5));
		} else {
			return (10 - (int) Math.floor(player.getSkills().getLevel(Skills.THIEVING) / 10) + Utils.getRandom().nextInt(11));
		}
	}

	/**
	 * Chance to fail cracking the safe. Formula based on player's agility.
	 *
	 * @param player
	 * 		the player trying to crack the safe.
	 * @return chance to fail cracking the safe.
	 */
	private int failChance(Player player) {
		return (Utils.getRandom().nextInt((int) Math.floor(player.getSkills().getLevel(Skills.AGILITY) / 10) + 1));
	}
}
