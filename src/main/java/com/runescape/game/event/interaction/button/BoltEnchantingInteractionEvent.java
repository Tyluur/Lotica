package com.runescape.game.event.interaction.button;

import com.runescape.game.content.skills.magic.Enchanting;
import com.runescape.game.event.interaction.type.InterfaceInteractionEvent;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/5/2015
 */
public class BoltEnchantingInteractionEvent extends InterfaceInteractionEvent {
	
	@Override
	public int[] getKeys() {
		return new int[] { 432 };
	}

	@Override
	public boolean handleInterfaceInteraction(Player player, int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
		final int index = Enchanting.getComponentIndex(componentId);
		if (index == -1) {
			return true;
		}
		Enchanting.processBoltEnchantSpell(player, index, packetId == 14 ? 1 : packetId == 67 ? 5 : 10);
		return true;
	}
}
