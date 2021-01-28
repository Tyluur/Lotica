package com.runescape.game.interaction.dialogues.impl.npc;

import com.runescape.game.interaction.dialogues.Dialogue;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/19/2015
 */
public class Achievement_Store extends Dialogue {
	
	@Override
	public void start() {
		sendOptionsDialogue(DEFAULT_OPTIONS, "Achievement Rewards Store", "Achievement Diary Equipment");
	}

	@Override
	public void run(int interfaceId, int option) {
		switch(stage) {
			case -1:
				switch(option) {
					case FIRST:
						openStore("Achievement Rewards");
						break;
					case SECOND:
						openStore("Achievement Diary Equipment");
						break;
				}
				break;
		}
	}

	@Override
	public void finish() {

	}
}
