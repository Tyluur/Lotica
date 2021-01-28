package com.runescape.game.interaction.dialogues.impl.npc;

import com.runescape.cache.loaders.NPCDefinitions;
import com.runescape.game.content.PartyRoom;
import com.runescape.game.interaction.dialogues.Dialogue;

public class PartyPete extends Dialogue {

	@Override
	public void start() {
		sendEntityDialogue(SEND_3_TEXT_CHAT, new String[] { NPCDefinitions.getNPCDefinitions(659).getName(), "The items in the party chest are worth " + PartyRoom.getTotalCoins() + "", "coins! Hang around until they drop and you might get", "something valueable!" }, IS_NPC, 659, 9843);
	}

	@Override
	public void run(int interfaceId, int componentId) {
		end();
	}

	@Override
	public void finish() {

	}

}
