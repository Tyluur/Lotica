package com.runescape.game.interaction.dialogues;

import com.runescape.game.GameConstants;
import com.runescape.utility.Utils;

import java.util.HashMap;

public final class DialogueHandler {

	private static final HashMap<Object, Class<Dialogue>> handledDialogues = new HashMap<Object, Class<Dialogue>>();

	@SuppressWarnings("unchecked")
	public static void init() {
		handledDialogues.clear();
		try {
			for (String packageName : Utils.getSubDirectories(DialogueHandler.class.getPackage().getName() + ".impl")) {
				for (Object clazz : Utils.getClassesInDirectory(DialogueHandler.class.getPackage().getName() + ".impl." + packageName)) {
					Dialogue dialogue = (Dialogue) clazz;
					handledDialogues.put(dialogue.getClass().getSimpleName(), (Class<Dialogue>) dialogue.getClass());
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public static Dialogue getDialogue(Object key) {
		if (key instanceof Dialogue) {
			return (Dialogue) key;
		}
		Class<Dialogue> classD = handledDialogues.get(key);
		if (classD == null) {
			if (GameConstants.DEBUG)
			System.out.println("Dialogue attempted to open with key [" + key + "] and did not exist.");
			return null;
		}
		try {
			return classD.newInstance();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return null;
	}

	private DialogueHandler() {

	}
}
