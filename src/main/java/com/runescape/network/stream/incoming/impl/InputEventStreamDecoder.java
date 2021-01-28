package com.runescape.network.stream.incoming.impl;

import com.runescape.game.content.Notes.Note;
import com.runescape.game.content.global.minigames.clanwars.ClanWars;
import com.runescape.game.content.global.minigames.creations.StealingCreation;
import com.runescape.game.event.InputEvent;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.network.stream.InputStream;
import com.runescape.network.stream.incoming.IncomingStreamDecoder;
import com.runescape.workers.game.core.CoresManager;
import com.runescape.workers.game.log.GameLog;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/27/2016
 */
public class InputEventStreamDecoder extends IncomingStreamDecoder {

	private final static int ENTER_INTEGER_PACKET = 3;

	private final static int ENTER_STRING_PACKET = 59;

	private final static int ENTER_LONG_STRING_PACKET = 7;

	@Override
	public int[] getKeys() {
		return new int[] { 3, 59, 7 };
	}

	@Override
	public void decode(Player player, InputStream stream, int packetId, int length) {
		switch (packetId) {
			case ENTER_INTEGER_PACKET:
				handleIntegerInput(player, packetId, length, stream);
				break;
			case ENTER_STRING_PACKET:
				handleStringInput(player, packetId, length, stream);
				break;
			case ENTER_LONG_STRING_PACKET:
				String value = stream.readString();
				if (value.equals("")) {
					return;
				}
				CoresManager.LOG_PROCESSOR.appendLog(new GameLog("input_event", player.getUsername(), "Entered " + value + " on an input screen."));
				if (player.getAttribute("input_event") != null) {
					InputEvent event = player.removeAttribute("input_event");
					event.setInput(value);
					event.handleInput();
				}
				break;
		}
	}

	private void handleIntegerInput(Player player, Integer packetId, Integer length, InputStream stream) {
		if (!player.isRunning() || player.isDead()) {
			return;
		}
		int value = stream.readInt();
		if (value < 0) {
			System.err.println("Invalid quantity entered: " + value);
			return;
		}
		CoresManager.LOG_PROCESSOR.appendLog(new GameLog("input_event", player.getUsername(), "Entered " + value + " on an input screen."));
		if (player.getAttribute("input_event") != null) {
			InputEvent event = player.removeAttribute("input_event");
			event.setInput(value);
			event.handleInput();
			return;
		}
		if ((player.getInterfaceManager().containsInterface(762) && player.getInterfaceManager().containsInterface(763)) || player.getInterfaceManager().containsInterface(11)) {
			if (value < 0) {
				return;
			}
			Integer bank_item_X_Slot = (Integer) player.getAttributes().remove("bank_item_X_Slot");
			if (bank_item_X_Slot == null) {
				return;
			}
			player.getBank().setLastX(value);
			player.getBank().refreshLastX();
			if (player.getAttributes().remove("bank_isWithdraw") != null) {
				player.getBank().withdrawItem(bank_item_X_Slot, value);
			} else {
				player.getBank().depositItem(bank_item_X_Slot, value, !player.getInterfaceManager().containsInterface(11));
			}
		} else if (player.getInterfaceManager().containsInterface(206) && player.getInterfaceManager().containsInterface(207)) {
			if (value < 0) {
				return;
			}
			Integer pc_item_X_Slot = (Integer) player.getAttributes().remove("pc_item_X_Slot");
			if (pc_item_X_Slot == null) {
				return;
			}
			if (player.getAttributes().remove("pc_isRemove") != null) {
				player.getPriceCheckManager().removeItem(pc_item_X_Slot, value);
			} else {
				player.getPriceCheckManager().addItem(pc_item_X_Slot, value);
			}
		} else if (player.getInterfaceManager().containsInterface(671) && player.getInterfaceManager().containsInterface(665)) {
			if (player.getFamiliar() == null || player.getFamiliar().getBob() == null) {
				return;
			}
			if (value < 0) {
				return;
			}
			Integer bob_item_X_Slot = (Integer) player.getAttributes().remove("bob_item_X_Slot");
			if (bob_item_X_Slot == null) {
				return;
			}
			if (player.getAttributes().remove("bob_isRemove") != null) {
				player.getFamiliar().getBob().removeItem(bob_item_X_Slot, value);
			} else {
				player.getFamiliar().getBob().addItem(bob_item_X_Slot, value);
			}
		} else if (player.getInterfaceManager().containsInterface(335) && player.getInterfaceManager().containsInterface(336)) {
			if (value < 0) {
				return;
			}
			Integer trade_item_X_Slot = (Integer) player.getAttributes().remove("trade_item_X_Slot");
			if (trade_item_X_Slot == null) {
				return;
			}
			if (player.getAttributes().remove("trade_isRemove") != null) {
				player.getTrade().removeItem(trade_item_X_Slot, value);
			} else {
				player.getTrade().addItem(trade_item_X_Slot, value);
			}
		} else if (player.getAttributes().get("skillId") != null) {
			if (player.getEquipment().wearingArmour()) {
				player.getDialogueManager().finishDialogue();
				player.getDialogueManager().startDialogue("SimpleMessage", "You cannot do this while having armour on!");
				return;
			}
			int skillId = (Integer) player.getAttributes().remove("skillId");
			if (skillId == Skills.HITPOINTS && value <= 9) {
				value = 10;
			} else if (value < 1) {
				value = 1;
			} else if (value > 99) {
				value = 99;
			}
			player.getSkills().setLevel(skillId, value);
			player.getSkills().setXp(skillId, Skills.getXPForLevel(value));
			player.getAppearence().generateAppearenceData();
			player.getDialogueManager().finishDialogue();
		} else if (player.getAttributes().get("kilnX") != null) {
			int index = (Integer) player.getAttributes().get("scIndex");
			int componentId = (Integer) player.getAttributes().get("scComponentId");
			int itemId = (Integer) player.getAttributes().get("scItemId");
			player.getAttributes().remove("kilnX");
			if (StealingCreation.proccessKilnItems(player, componentId, index, itemId, value)) {
				return;
			}
		}
	}

	private void handleStringInput(Player player, Integer packetId, Integer length, InputStream stream) {
		if (!player.isRunning() || player.isDead()) {
			return;
		}
		String value = stream.readString();
		if (value.equals("")) {
			return;
		}
		CoresManager.LOG_PROCESSOR.appendLog(new GameLog("input_event", player.getUsername(), "Entered " + value + " on an input screen."));
		if (player.getAttribute("input_event", null) != null) {
			InputEvent event = player.removeAttribute("input_event");
			event.setInput(value);
			event.handleInput();
			return;
		}
		if (player.getInterfaceManager().containsInterface(1108)) {
			player.getFriendsIgnores().setChatPrefix(value);
		} else if (player.getAttributes().get("entering_note") == Boolean.TRUE) {
			player.getNotes().add(new Note(value, 1));
			player.getNotes().refresh();
			player.getAttributes().put("entering_note", Boolean.FALSE);
			return;
		} else if (player.getAttributes().get("editing_note") == Boolean.TRUE) {
			Note note = (Note) player.getAttributes().get("curNote");
			player.getNotes().getNotes().get(player.getNotes().getNotes().indexOf(note));
			player.getNotes().refresh();
			player.getAttributes().put("editing_note", Boolean.FALSE);
		} else if (player.getAttributes().get("view_name") == Boolean.TRUE) {
			player.getAttributes().remove("view_name");
			Player other = World.getPlayerByDisplayName(value);
			if (other == null) {
				player.getPackets().sendGameMessage("Couldn't find player.");
				return;
			}
			ClanWars clan = other.getCurrentFriendChat() != null ? other.getCurrentFriendChat().getClanWars() : null;
			if (clan == null) {
				player.getPackets().sendGameMessage("This player's clan is not in war.");
				return;
			}
			if (clan.getSecondTeam().getOwnerDisplayName() != other.getCurrentFriendChat().getOwnerDisplayName()) {
				player.getAttributes().put("view_prefix", 1);
			}
			player.getAttributes().put("view_clan", clan);
			ClanWars.enter(player);
		}
	}
}
