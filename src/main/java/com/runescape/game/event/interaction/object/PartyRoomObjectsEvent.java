package com.runescape.game.event.interaction.object;

import com.runescape.game.content.PartyRoom;
import com.runescape.game.event.interaction.type.ObjectInteractionEvent;
import com.runescape.game.interaction.dialogues.impl.object.PartyRoomLever;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.world.ClickOption;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 6/19/2015
 */
public class PartyRoomObjectsEvent extends ObjectInteractionEvent {
	@Override
	public int[] getKeys() {
		return new int[] { /*26193, 26194*/ };
	}

	@Override
	public boolean handleObjectInteraction(Player player, WorldObject object, ClickOption option) {
		if (object.getId() == 26193) {
			PartyRoom.openPartyChest(player);
		} else if (object.getId() == 26194) {
			player.getDialogueManager().startDialogue(PartyRoomLever.class);
		}
		return true;
	}
}
