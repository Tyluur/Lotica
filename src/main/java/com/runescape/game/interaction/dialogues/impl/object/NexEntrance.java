package com.runescape.game.interaction.dialogues.impl.object;

import com.runescape.game.content.global.minigames.ZarosGodwars;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.world.WorldTile;

public final class NexEntrance extends Dialogue {

	@Override
	public void start() {
		sendDialogue(
				"The room beyond this point is a prison!",
				"There is no way out other than death or teleport.",
				"Only those who endure dangerous encounters should proceed.");
	}

	@Override
	public void run(int interfaceId, int componentId) {
		if (stage == -1) {
			stage = 0;
			sendOptionsDialogue(
					"There are currently " + ZarosGodwars.getPlayersCount()
							+ " people fighting.<br>Do you wish to join them?",
					"Climb down.", "Stay here.");
		} else if (stage == 0) {
			if (componentId == OPTION_1) {
				player.setNextWorldTile(new WorldTile(2911, 5204, 0));
				player.getControllerManager().startController("ZGDControler");
			}
			end();
		}

	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub

	}

}
