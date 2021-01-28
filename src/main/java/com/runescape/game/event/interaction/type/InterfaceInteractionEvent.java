package com.runescape.game.event.interaction.type;

import com.runescape.game.event.interaction.InteractionEvent;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.utility.world.ClickOption;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 19, 2015
 */
public abstract class InterfaceInteractionEvent extends InteractionEvent {

	@Override
	public abstract int[] getKeys();

	@Override
	public abstract boolean handleInterfaceInteraction(Player player, int interfaceId, int componentId, int slotId, int slotId2, int packetId);

	@Override
	public InteractionEventType getEventType() {
		return InteractionEventType.INTERFACE;
	}

	@Override
	public boolean handleNPCInteraction(Player player, NPC npc, ClickOption option) {
		return false;
	}

	@Override
	public boolean handleObjectInteraction(Player player, WorldObject object, ClickOption option) {
		return false;
	}

	@Override
	public boolean handleItemInteraction(Player player, Item item, ClickOption option) {
		return false;
	}
}
