package com.runescape.game.interaction.dialogues.impl.skills;

import com.runescape.game.content.skills.construction.House;
import com.runescape.game.event.InputEvent;
import com.runescape.game.event.InputEvent.InputEventType;
import com.runescape.game.interaction.dialogues.Dialogue;

/**
 * 
 * @author Jonathan
 * @since January 22th, 2014
 */
public class EnterHouseD extends Dialogue {

	@Override
	public void start() {
		end();
		//sendOptionsDialogue(DEFAULT_OPTIONS, "Go to your house.", "Go to your house (building mode).", "Go to a friend's house.", "Never mind.");
	}

	@Override
	public void run(int interfaceId, int option) {
		switch (stage) {
		case -1:
			switch (option) {
			case FIRST:
				player.getHouse().setBuildMode(false);
				player.getHouse().enterMyHouse();
				end();
				break;
			case SECOND:
				player.getHouse().setBuildMode(true);
				player.getHouse().enterMyHouse();
				end();
				break;
			case THIRD:
				player.getPackets().requestClientInput(new InputEvent("Enter name of the person who's house you'd like to join:", InputEventType.NAME) {

					@Override
					public void handleInput() {
						House.enterHouse(player, getInput());
					}
				});
				end();
				break;
			case FOURTH:
				end();
				break;
			}
			break;
		}
	}

	@Override
	public void finish() {
	}

}