package com.runescape.game.interaction.dialogues.impl.npc;

import com.runescape.game.event.interaction.button.PlankCreationInteractionEvent;
import com.runescape.game.interaction.dialogues.Dialogue;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 16, 2015
 */
public class Sawmill_operator extends Dialogue {

	@Override
	public void start() {
		PlankCreationInteractionEvent.display(player);
		sendNPCDialogue(npcId = (int) parameters[0], HAPPY, "Select the type of planks you wish to create.");
	}

	@Override
	public void run(int interfaceId, int option) {
		start();
	}

	@Override
	public void finish() {
	}
	
	int npcId;

}
