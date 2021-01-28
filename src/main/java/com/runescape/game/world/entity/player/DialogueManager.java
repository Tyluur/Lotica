package com.runescape.game.world.entity.player;

import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.interaction.dialogues.DialogueHandler;

public class DialogueManager {

	private Player player;

	private Dialogue dialogue;

	public DialogueManager(Player player) {
		this.player = player;
	}

	public void startDialogue(Class<?> clazz, Object... parameters) {
		startDialogue(clazz.getSimpleName(), parameters);
	}

	public void startDialogue(Object key, Object... parameters) {
		if (!player.getControllerManager().useDialogueScript(key)) { return; }
		if (dialogue != null) { dialogue.finish(); }
		dialogue = DialogueHandler.getDialogue(key);
		if (dialogue == null) { return; }
		dialogue.parameters = parameters;
		dialogue.setPlayer(player);
		dialogue.start();
	}

	public void continueDialogue(int interfaceId, int componentId) {
		if (dialogue == null) { return; }
		if (dialogue.getStage() == -2) {
			finishDialogue();
			return;
		}
		dialogue.run(interfaceId, componentId);
	}

	public void finishDialogue() {
		if (dialogue == null) { return; }
		dialogue.finish();
		dialogue = null;
		if (player.getInterfaceManager().containsChatBoxInter()) {
			player.getInterfaceManager().closeChatBoxInterface();
		}
		if (player.getCloseInterfacesEvent() != null) {
			player.getCloseInterfacesEvent().run();
			player.setCloseInterfacesEvent(null);
		}
	}

	public Dialogue getDialogue() {
		return dialogue;
	}
}
