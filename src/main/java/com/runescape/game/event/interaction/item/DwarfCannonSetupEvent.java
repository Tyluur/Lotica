package com.runescape.game.event.interaction.item;

import com.runescape.game.content.global.cannon.CannonAlgorithms;
import com.runescape.game.event.interaction.type.ItemInteractionEvent;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.utility.world.ClickOption;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/5/2015
 */
public class DwarfCannonSetupEvent extends ItemInteractionEvent {
	
	@Override
	public int[] getKeys() {
		return new int[] { 6 };
	}

	@Override
	public boolean handleItemInteraction(Player player, Item item, ClickOption option) {
		if (option == ClickOption.FIRST) {
			CannonAlgorithms.createCannon(player);
			return true;
		}
		return false;
	}
}
