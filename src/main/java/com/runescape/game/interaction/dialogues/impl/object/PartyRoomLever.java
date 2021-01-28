package com.runescape.game.interaction.dialogues.impl.object;

import com.runescape.game.content.PartyRoom;
import com.runescape.game.interaction.dialogues.Dialogue;

public class PartyRoomLever extends Dialogue {
	
	@Override
	public void start() {
		sendOptionsDialogue(DEFAULT_OPTIONS, "Balloon Bonanza (1000 coins).", "Nightly Dance (500 coins).", "No action.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if(componentId == FIRST) {
			PartyRoom.purchase(player, true);
		} else if(componentId == SECOND) {
			PartyRoom.purchase(player, false);
		}
		end();
	}

	@Override
	public void finish() {
		
	}
}
