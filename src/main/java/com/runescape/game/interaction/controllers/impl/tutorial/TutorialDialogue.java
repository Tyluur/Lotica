package com.runescape.game.interaction.controllers.impl.tutorial;

import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.world.entity.player.Player;

/**
 * This dialogue type is for dialogues in whi
 *
 * @author Tyluur<itstyluur@gmail.com>
 * @since 12/30/2015
 */
public abstract class TutorialDialogue extends Dialogue {

	private final int ticks;

	private final int animationId;

	private final boolean isPlayer;

	private final int npcId = 945;

	private final String[] message;

	/**
	 * Constructs a new tutorial dialogue
	 *
	 * @param ticks
	 * 		The ticks for which the button will appear after, -1 if it should stay forevre
	 * @param animationId
	 * 		The animation of the dialogue
	 * @param isPlayer
	 * 		If we send a player dialogue
	 * @param message
	 * 		The message sent over the dialogue
	 */
	public TutorialDialogue(int ticks, int animationId, boolean isPlayer, String... message) {
		this.ticks = ticks;
		this.animationId = animationId;
		this.isPlayer = isPlayer;
		this.message = message;
	}

	@Override
	public void start() {
		if (isPlayer) {
			sendPlayerDialogueNoContinue(animationId, ticks, message);
		} else {
			sendNPCDialogueNoContinue(npcId, animationId, ticks, message);
		}
	}

	public static void closeChatbox(Player player) {
		closeNoContinueDialogue(player);
		if (player.getInterfaceManager().containsChatBoxInter()) {
			player.getInterfaceManager().closeChatBoxInterface();
		}
	}

	@Override
	public abstract void run(int interfaceId, int option);

	@Override
	public void finish() {

	}
}
