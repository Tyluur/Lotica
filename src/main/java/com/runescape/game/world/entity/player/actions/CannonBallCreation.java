package com.runescape.game.world.entity.player.actions;

import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;

/**
 *
 * @author Tyluur <itstyluur@gmail.com>
 * @since Jan 15, 2014
 */
public class CannonBallCreation extends Action {

	private boolean checkAll(Player player) {
		if (!player.getInventory().contains(2353)) {
			player.sendMessage("You have run out of steel bars to use!");
			return false;
		}
		if (!player.getInventory().contains(4)) {
			player.sendMessage("You do not have an ammo mould in your inventory.");
			return false;
		}
		return amount >= 0;
	}

	@Override
	public boolean start(Player player) {
		return checkAll(player);
	}

	@Override
	public boolean process(Player player) {
		return checkAll(player);
	}

	@Override
	public int processWithDelay(Player player) {
		player.setNextAnimation(new Animation(3243));
		player.getSkills().addXp(Skills.SMITHING, 25.6);
		player.sendMessage("The molten metal cools slowly to form 4 cannonballs.");
		player.getInventory().deleteItem(2353, 1);
		player.getInventory().addItem(2, 4);
		amount--;
		return 4;
	}

	@Override
	public void stop(Player player) {

	}

	public CannonBallCreation(int amount) {
		this.amount = amount;
	}

	private int amount;

}
