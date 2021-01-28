package com.runescape.game.interaction.dialogues.impl.npc;

import com.runescape.game.event.interaction.npc.NurseInteractionEvent;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.world.entity.npc.NPC;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/13/2016
 */
public class NurseD extends Dialogue {

	transient NPC npc;

	int npcId;

	@Override
	public void start() {
		sendNPCDialogue(npcId = (npc = getParam(0)).getId(), CALM_TALKING, "Hey, adventurer! I'm the nurse here at home", "I can heal those who need medical attention.");
	}

	@Override
	public void run(int interfaceId, int option) {
		switch (stage) {
			case -1:
				sendPlayerDialogue(CALM, "Is there a fee for this?");
				stage = 0;
				break;
			case 0:
				sendNPCDialogue(npcId, CALM_TALKING, "No, this is on the house.");
				stage = 1;
				break;
			case 1:
				sendOptionsDialogue(DEFAULT_OPTIONS, "Can you heal me?", "Nice talking to you.");
				stage = 2;
				break;
			case 2:
				switch (option) {
					case FIRST:
						sendPlayerDialogue(HAPPY, "Can you heal me?");
						stage = 3;
						break;
					case SECOND:
						sendPlayerDialogue(CALM, "Very interesting... It was nice talking to you!");
						stage = -2;
						break;
				}
				break;
			case 3:
				end();
				NurseInteractionEvent.healPlayer(player, npc);
				break;
		}
	}

	@Override
	public void finish() {

	}
}
