package com.runescape.game.event.interaction.item;

import com.runescape.game.content.Foods;
import com.runescape.game.event.interaction.type.ItemInteractionEvent;
import com.runescape.game.world.entity.masks.ForceTalk;
import com.runescape.game.world.entity.masks.Hit;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.utility.world.ClickOption;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 8/18/2015
 */
public class DwarvenRockCakeInteractionEvent extends ItemInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] { 7509 };
	}

	@Override
	public boolean handleItemInteraction(Player player, Item item, ClickOption option) {
		player.setNextAnimation(Foods.EAT_ANIM);
		player.setNextForceTalk(new ForceTalk("Ow! I nearly broke a tooth!"));
		player.applyHit(new Hit(player, 10));
		return true;
	}
}
