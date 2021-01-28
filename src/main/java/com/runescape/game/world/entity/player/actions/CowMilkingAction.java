package com.runescape.game.world.entity.player.actions;

import com.runescape.game.interaction.dialogues.impl.npc.GrilleGoatsD;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;

public class CowMilkingAction extends Action {

	public static final int EMPTY_BUCKET = 1925;
	
	public CowMilkingAction() {
		
	}

	@Override
	public boolean start(Player player) {
		if (!player.getInventory().containsItem(EMPTY_BUCKET, 1)) {
			player.getDialogueManager().startDialogue(GrilleGoatsD.class);
			return false;
		}
		return true;
	}

	@Override
	public boolean process(Player player) {
		return player.getInventory().hasFreeSlots() && player.getInventory().containsItem(EMPTY_BUCKET, 1);
	}

	@Override
	public int processWithDelay(Player player) {
		player.setNextAnimation(new Animation(2305));
		player.getInventory().deleteItem(new Item(EMPTY_BUCKET, 1));
		player.getInventory().addItem(new Item(1927));
		player.getPackets().sendGameMessage("You milk the cow.");
		return 5;
	}

	@Override
	public void stop(Player player) {
		setActionDelay(player, 3);
	}

}
