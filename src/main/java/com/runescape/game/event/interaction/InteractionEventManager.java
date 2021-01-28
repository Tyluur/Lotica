package com.runescape.game.event.interaction;

import com.runescape.game.event.interaction.InteractionEvent.InteractionEventType;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.utility.Utils;
import com.runescape.utility.world.ClickOption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 19, 2015
 */
public class InteractionEventManager {

	/**
	 * The list of {@code InteractionEvent}s
	 */
	private static final List<InteractionEvent> events = Collections.synchronizedList(new ArrayList<>());

	/**
	 * Populates the {@link #events} list full of all game events
	 */
	public static void initialize() {
		events.clear();
		for (String directory : Utils.getSubDirectories(InteractionEventManager.class)) {
			if (directory.equals("type")) {
				continue;
			}
			events.addAll(Utils.getClassesInDirectory(InteractionEventManager.class.getPackage().getName() + "." + directory).stream().map(clazz -> (InteractionEvent) clazz).collect(Collectors.toList()));
		}
	}

	/**
	 * Handles the interaction with an interface
	 *
	 * @param player
	 * 		The player
	 * @param interfaceId
	 * 		The interface id
	 * @param buttonId
	 * 		The button id
	 * @param slotId
	 * 		The slot id
	 * @param slotId2
	 * 		The second slot id
	 * @param packetId
	 * 		The packet id
	 */
	public static boolean handleInterfaceInteraction(Player player, int interfaceId, int buttonId, int slotId, int slotId2, int packetId) {
		List<InteractionEvent> events = getEvents(interfaceId, InteractionEventType.INTERFACE);
		for (InteractionEvent event : events) {
			if (event.handleInterfaceInteraction(player, interfaceId, buttonId, slotId, slotId2, packetId)) {
				return true;
			}
		}
		return false;
	}

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
	public static boolean handleObjectInteraction(Player player, WorldObject object, ClickOption option) {
		List<InteractionEvent> events = getEvents(object.getId(), InteractionEventType.OBJECT);
		for (InteractionEvent event : events) {
			if (event.handleObjectInteraction(player, object, option)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Handles the way a player interacts with an object
	 *
	 * @param player
	 * 		The player
	 * @param npc
	 * 		The npc
	 * @param option
	 * 		The option
	 */
	public static boolean handleNPCInteraction(Player player, NPC npc, ClickOption option) {
		List<InteractionEvent> events = getEvents(npc.getId(), InteractionEventType.NPC);
		for (InteractionEvent event : events) {
			if (event.handleNPCInteraction(player, npc, option)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Handles the interaction with an item by looking for an event for it
	 *
	 * @param player
	 * 		The player
	 * @param item
	 * 		The item
	 * @param option
	 * 		The option clicked
	 */
	public static boolean handleItemInteraction(Player player, Item item, ClickOption option) {
		List<InteractionEvent> events = getEvents(item.getId(), InteractionEventType.ITEM);
		for (InteractionEvent event : events) {
			if (event.handleItemInteraction(player, item, option)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets all of the events of a certain type
	 *
	 * @param type
	 * 		The type of events we want
	 */
	private static List<InteractionEvent> getEvents(int key, InteractionEventType type) {
		return InteractionEventManager.events.stream().filter(p -> p.getEventType().equals(type)).filter(p -> {
			for (int keyId : p.getKeys()) {
				if (key == keyId) {
					return true;
				}
			}
			return false;
		}).collect(Collectors.toList());
	}

}
