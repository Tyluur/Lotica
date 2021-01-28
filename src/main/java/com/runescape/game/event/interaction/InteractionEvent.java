package com.runescape.game.event.interaction;

import com.runescape.game.world.WorldObject;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.utility.world.ClickOption;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 19, 2015
 */
public abstract class InteractionEvent {

	/**
	 * The keys of this interaction event
	 */
	public abstract int[] getKeys();

	/**
	 * The type of event this is
	 */
	public abstract InteractionEventType getEventType();

	/**
	 * Handles the way a player interacts with an npc
	 *
	 * @param player
	 * 		The player
	 * @param npc
	 * 		The npc
	 * @param option
	 * 		The option
	 */
	public abstract boolean handleNPCInteraction(Player player, NPC npc, ClickOption option);

	/**
	 * Handles the way a player interacts with an object
	 *
	 * @param player
	 * 		The player
	 * @param object
	 * 		The object
	 * @param option
	 * 		The option
	 */
	public abstract boolean handleObjectInteraction(Player player, WorldObject object, ClickOption option);

	/**
	 * Handles the way a player interacts on an interface
	 *
	 * @param player
	 * 		The player
	 * @param interfaceId
	 * 		The interface id
	 * @param componentId
	 * 		The component id
	 * @param slotId
	 * 		The slot id
	 * @param slotId2
	 * 		The second slot id
	 * @param packetId
	 * 		The packet id
	 */
	public abstract boolean handleInterfaceInteraction(Player player, int interfaceId, int componentId, int slotId, int slotId2, int packetId);

	/**
	 * Handles the interaction with an item
	 *
	 * @param player
	 * 		The player
	 * @param item
	 * 		The item
	 * @param option
	 * 		The option
	 */
	public abstract boolean handleItemInteraction(Player player, Item item, ClickOption option);

	/**
	 * The possible types of events
	 */
	public enum InteractionEventType {
		NPC,
		INTERFACE,
		OBJECT,
		ITEM
	}
}
