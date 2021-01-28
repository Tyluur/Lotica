package com.runescape.game.interaction.dialogues;

import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.cache.loaders.NPCDefinitions;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.ChatColors;
import com.runescape.utility.external.gson.GsonStartup;
import com.runescape.utility.external.gson.loaders.StoreLoader;
import com.runescape.utility.world.player.Expressions;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Dialogue implements Expressions, ChatColors {

	public static final int FIRST = 1, SECOND = 2, THIRD = 3, FOURTH = 4, FIFTH = 5, YES = 1, NO = 2;

	public static final int OPTION_1 = 1, OPTION_2 = 2, OPTION_3 = 3, OPTION_4 = 4, OPTION_5 = 5;

	protected static final short SEND_1_TEXT_INFO = 210;

	protected static final short SEND_2_TEXT_INFO = 211;

	protected static final short SEND_3_TEXT_INFO = 212;

	protected static final short SEND_4_TEXT_INFO = 213;

	protected static final String DEFAULT_OPTIONS = "Select an Option";

	protected static final short SEND_1_TEXT_CHAT = 241;

	protected static final short SEND_2_TEXT_CHAT = 242;

	protected static final short SEND_3_TEXT_CHAT = 243;

	protected static final short SEND_4_TEXT_CHAT = 244;

	protected static final short SEND_NO_EMOTE = -1;

	protected static final byte IS_PLAYER = 0;

	protected static final byte IS_NPC = 1;

	protected static final byte IS_ITEM = 2;

	public Object[] parameters;

	protected Player player;

	protected byte stage = -1;

	public Dialogue() {

	}

	public static boolean sendNPCDialogueNoContinue(Player player, int npcId, int animationId, String... text) {
		return sendEntityDialogueNoContinue(player, IS_NPC, npcId, animationId, text);
	}

	/*
	 *
	 * auto selects title, new dialogues
	 */
	public static boolean sendEntityDialogueNoContinue(Player player, int type, int entityId, int animationId, String... text) {
		String title = "";
		if (type == IS_PLAYER) {
			title = player.getDisplayName();
		} else if (type == IS_NPC) {
			title = NPCDefinitions.getNPCDefinitions(entityId).getName();
		} else if (type == IS_ITEM) {
			title = ItemDefinitions.forId(entityId).getName();
		}
		return sendEntityDialogueNoContinue(player, type, title, entityId, animationId, text);
	}

	public static boolean sendEntityDialogueNoContinue(Player player, int type, String title, int entityId, int animationId, String... texts) {
		boolean npc = type != IS_PLAYER;

		StringBuilder bldr = new StringBuilder();
		int interfaceId = npc ? 240 : 63;
		for (String element : texts) {
			interfaceId++;
			bldr.append(" " + element);
		}
		int[] componentOptions = getIComponentsIds((short) interfaceId);
		String[] messages = getMessages(title, texts);
		if (componentOptions == null || (messages.length) != componentOptions.length) {
			return false;
		}
		player.getInterfaceManager().sendChatBoxInterface(interfaceId);
		for (int i = 0; i < componentOptions.length; i++) {
			player.getPackets().sendIComponentText(interfaceId, componentOptions[i], messages[i]);
		}
		player.getPackets().sendEntityOnIComponent(!npc, entityId, interfaceId, 2);
		player.getPackets().sendIComponentAnimation(animationId, interfaceId, 2);
		return true;
	}

	private static int[] getIComponentsIds(short interId) {
		int childOptions[];
		switch (interId) {

			case 458:
				childOptions = new int[4];
				for (int i = 0; i < childOptions.length; i++) {
					childOptions[i] = i;
				}
				break;
			case 210:
				childOptions = new int[1];
				childOptions[0] = 1;
				break;

			case 211:
				childOptions = new int[2];
				childOptions[0] = 1;
				childOptions[1] = 2;
				break;

			case 212:
				childOptions = new int[3];
				childOptions[0] = 1;
				childOptions[1] = 2;
				childOptions[2] = 3;
				break;

			case 213:
				childOptions = new int[4];
				childOptions[0] = 1;
				childOptions[1] = 2;
				childOptions[2] = 3;
				childOptions[3] = 4;
				break;

			case 229:
				childOptions = new int[3];
				childOptions[0] = 1;
				childOptions[1] = 2;
				childOptions[2] = 3;
				break;

			case 230:
				childOptions = new int[4];
				childOptions[0] = 1;
				childOptions[1] = 2;
				childOptions[2] = 3;
				childOptions[3] = 4;
				break;

			case 231:
				childOptions = new int[4];
				childOptions[0] = 1;
				childOptions[1] = 2;
				childOptions[2] = 3;
				childOptions[3] = 4;
				break;

			case 235:
				childOptions = new int[4];
				childOptions[0] = 1;
				childOptions[1] = 2;
				childOptions[2] = 3;
				childOptions[3] = 4;
				break;

			case 236:
				childOptions = new int[3];
				childOptions[0] = 0;
				childOptions[1] = 1;
				childOptions[2] = 2;
				break;

			case 237:
				childOptions = new int[5];
				childOptions[0] = 0;
				childOptions[1] = 1;
				childOptions[2] = 2;
				childOptions[3] = 3;
				childOptions[4] = 4;
				break;

			case 238:
				childOptions = new int[6];
				childOptions[0] = 0;
				childOptions[1] = 1;
				childOptions[2] = 2;
				childOptions[3] = 3;
				childOptions[4] = 4;
				childOptions[5] = 5;
				break;

			case 64:
				childOptions = new int[2];
				childOptions[0] = 3;
				childOptions[1] = 4;
				break;

			case 65:
				childOptions = new int[3];
				childOptions[0] = 3;
				childOptions[1] = 4;
				childOptions[2] = 5;
				break;

			case 66:
				childOptions = new int[4];
				childOptions[0] = 3;
				childOptions[1] = 4;
				childOptions[2] = 5;
				childOptions[3] = 6;
				break;

			case 67:
				childOptions = new int[5];
				childOptions[0] = 3;
				childOptions[1] = 4;
				childOptions[2] = 5;
				childOptions[3] = 6;
				childOptions[4] = 7;
				break;

			case 241:
			case 245:
				childOptions = new int[2];
				childOptions[0] = 3;
				childOptions[1] = 4;
				break;

			case 242:
			case 246:
				childOptions = new int[3];
				childOptions[0] = 3;
				childOptions[1] = 4;
				childOptions[2] = 5;
				break;

			case 243:
			case 247:
				childOptions = new int[4];
				childOptions[0] = 3;
				childOptions[1] = 4;
				childOptions[2] = 5;
				childOptions[3] = 6;
				break;

			case 244:
			case 248:
				childOptions = new int[5];
				childOptions[0] = 3;
				childOptions[1] = 4;
				childOptions[2] = 5;
				childOptions[3] = 6;
				childOptions[4] = 7;
				break;

			case 214:
			case 215:
			case 216:
			case 217:
			case 218:
			case 219:
			case 220:
			case 221:
			case 222:
			case 223:
			case 224:
			case 225:
			case 226:
			case 227:
			case 228:
			case 232:
			case 233:
			case 234:
			case 239:
			case 240:
			default:
				return null;
		}
		return childOptions;
	}

	private static String[] getMessages(String title, String[] message) {
		List<String> textList = new ArrayList<>();
		textList.add(title);
		Collections.addAll(textList, message);
		return textList.toArray(new String[textList.size()]);
	}

	public static void closeNoContinueDialogue(Player player) {
		player.getInterfaceManager().closeReplacedRealChatBoxInterface();
	}

	public static boolean sendPlayerDialogueNoContinue(Player player, int animationId, String... text) {
		return sendEntityDialogueNoContinue(player, IS_PLAYER, -1, animationId, text);
	}

	public abstract void start();

	public abstract void run(int interfaceId, int option);

	public abstract void finish();

	public void sendNPCDialogue(int npcId, int animationId, String... message) {
		sendEntityDialogue(true, npcId, animationId, message);
	}

	private void sendEntityDialogue(boolean npc, int entityId, int animationId, String... message) {
		StringBuilder bldr = new StringBuilder();
		int interfaceId = npc ? 240 : 63;
		for (String element : message) {
			interfaceId++;
		}
		for (String element : message) {
			bldr.append(" ").append(element);
		}
		int[] componentOptions = getIComponentsIds((short) interfaceId);
		String title = npc ? NPCDefinitions.getNPCDefinitions(entityId).getName() : player.getDisplayName();
		String[] messages = getMessages(title, message);
		if (componentOptions == null || (messages.length) != componentOptions.length) {
			return;
		}
		player.getInterfaceManager().sendChatBoxInterface(interfaceId);
		for (int i = 0; i < componentOptions.length; i++) {
			player.getPackets().sendIComponentText(interfaceId, componentOptions[i], messages[i]);
		}
		player.getPackets().sendEntityOnIComponent(!npc, entityId, interfaceId, 2);
		player.getPackets().sendIComponentAnimation(animationId, interfaceId, 2);
	}

	/**
	 * Sends an npc dialogue without the continue button available. After the {@link #ticks} ticks have passed, it will
	 * then be visible
	 *
	 * @param npcId
	 * 		The npc id to send the dialogue of
	 * @param animationId
	 * 		The animation of the dialogue
	 * @param ticks
	 * 		The ticks to display the continue button afte
	 * @param message
	 * 		The message to send
	 */
	public void sendNPCDialogueNoContinue(int npcId, int animationId, int ticks, String... message) {
		int interfaceId = 240;
		for (String aMessage : message) {
			interfaceId++;
		}
		final int hideLine = getHideLine(interfaceId);
		if (hideLine != -1) {
			player.getPackets().sendHideIComponent(interfaceId, hideLine, true);
			final int interf = interfaceId;
			if (ticks != -1) {
				WorldTasksManager.schedule(new WorldTask() {

					@Override
					public void run() {
						player.getPackets().sendHideIComponent(interf, hideLine, false);
					}
				}, ticks);
			}
		}
		sendEntityDialogue(true, npcId, animationId, message);
	}

	private static int getHideLine(int interfaceId) {
		switch (interfaceId) {
			case 64:
			case 241:
				return 5;
			case 65:
			case 242:
				return 6;
			case 66:
			case 243:
				return 7;
			case 67:
			case 244:
				return 8;
		}
		return -1;
	}

	/**
	 * Sends a player dialogue without the continue button available. After the ticks have passed, it will then be
	 * visible
	 *
	 * @param animationId
	 * 		The animation on the dialogue
	 * @param ticks
	 * 		The ticks to pass for the continue button to display
	 * @param message
	 * 		The dialogue message
	 */
	public void sendPlayerDialogueNoContinue(int animationId, int ticks, String... message) {
		int interfaceId = 63;
		for (String aMessage : message) {
			interfaceId++;
		}
		final int hideLine = getHideLine(interfaceId);
		if (hideLine != -1) {
			player.getPackets().sendHideIComponent(interfaceId, hideLine, true);
			final int interf = interfaceId;
			if (ticks != -1) {
				WorldTasksManager.schedule(new WorldTask() {

					@Override
					public void run() {
						player.getPackets().sendHideIComponent(interf, hideLine, false);
					}
				}, ticks);
			}
		}
		sendEntityDialogue(false, player.getIndex(), animationId, message);
	}

	public void sendPlayerDialogue(int animationId, String... message) {
		sendEntityDialogue(false, player.getIndex(), animationId, message);
	}

	protected final void end() {
		player.getDialogueManager().finishDialogue();
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	/**
	 * Gets a parameter from the {@link #parameters} array and casts the type to it
	 *
	 * @param indexId
	 * 		The index in the parameters array
	 */
	@SuppressWarnings("unchecked")
	protected <K> K getParam(int indexId) {
		return (K) parameters[indexId];
	}

	public void sendItemDialogue(int itemId, int itemAmount, String... messages) {
		int l = messages.length;
		short interfaceId = (l == 1 ? SEND_1_TEXT_CHAT : l == 2 ? SEND_2_TEXT_CHAT : l == 3 ? SEND_3_TEXT_CHAT : SEND_4_TEXT_CHAT);
		List<String> text = new ArrayList<>();
		text.add("");
		Collections.addAll(text, messages);
		String[] message = text.toArray(new String[text.size()]);
		sendEntityDialogue(interfaceId, message, IS_ITEM, itemId, itemAmount);
	}

	/*
	 *
	 * auto selects title, new dialogues
	 */
	public boolean sendEntityDialogue(int type, int entityId, int animationId, String... text) {
		String title = "";
		if (type == IS_PLAYER) {
			title = player.getDisplayName();
		} else if (type == IS_NPC) {
			title = NPCDefinitions.getNPCDefinitions(entityId).getName();
		} else if (type == IS_ITEM) {
			title = ItemDefinitions.forId(entityId).getName();
		}
		return sendEntityDialogue(type, title, entityId, animationId, text);
	}

	/*
	 * new dialogues
	 */
	public boolean sendEntityDialogue(int type, String title, int entityId, int animationId, String... texts) {
		/*StringBuilder builder = new StringBuilder();
		for (int line = 0; line < texts.length; line++) {
			builder.append(texts[line] + "<br>");
		}
		String text = builder.toString();
		if (type == IS_NPC) {
			player.getInterfaceManager().sendChatBoxInterface(1184);
			player.getPackets().sendIComponentText(1184, 17, title);
			player.getPackets().sendIComponentText(1184, 13, text);
			player.getPackets().sendNPCOnIComponent(1184, 11, entityId);
			if (animationId != -1) {
				player.getPackets().sendIComponentAnimation(animationId, 1184, 11);
			}
		} else if (type == IS_PLAYER) {
			player.getInterfaceManager().sendChatBoxInterface(1191);
			player.getPackets().sendIComponentText(1191, 8, title);
			player.getPackets().sendIComponentText(1191, 17, text);
			player.getPackets().sendPlayerOnIComponent(1191, 15);
			if (animationId != -1) {
				player.getPackets().sendIComponentAnimation(animationId, 1191, 15);
			}
		} else if (type == IS_ITEM) {
			int interfaceId = 1189;
			player.getInterfaceManager().sendChatBoxInterface(interfaceId);
			player.getPackets().sendIComponentText(interfaceId, 4, text);
			player.getPackets().sendItemOnIComponent(interfaceId, 1, entityId, animationId);
		}*/
		// todo fix this
		player.sendMessage("This has not been added yet, please report on forums");
		return true;
	}

	public void sendOptionsDialogue(String... text) {
		int l = text.length;
		int interfaceId = (l == 6 ? 238 : l == 5 ? 237 : l == 4 ? 230 : 236);

		String[] messages = new String[text.length + 1];
		System.arraycopy(text, 0, messages, 0, text.length);
		sendDialogue((short) interfaceId, messages);
	}

	public boolean sendDialogue(short interId, String... talkDefinitons) {
		int[] componentOptions = getIComponentsIds(interId);
		if (componentOptions == null) {
			return false;
		}
		if (player == null) { return false; }
		player.getInterfaceManager().sendChatBoxInterface(interId);
		int properLength = (interId > 213 ? talkDefinitons.length - 1 : talkDefinitons.length);
		if (properLength != componentOptions.length) {
			return false;
		}
		for (int childOptionId = 0; childOptionId < componentOptions.length; childOptionId++) {
			player.getPackets().sendIComponentText(interId, componentOptions[childOptionId], talkDefinitons[childOptionId]);
		}
		return true;
	}

	public void sendDialogue(String... text) {
		int l = text.length;
		short interfaceId = (l == 4 ? SEND_4_TEXT_INFO : l == 3 ? SEND_3_TEXT_INFO : l == 2 ? SEND_2_TEXT_INFO : SEND_1_TEXT_INFO);
		sendDialogue(interfaceId, text);
	}

	public boolean sendEntityDialogue(short interId, String[] talkDefinitons, byte type, int entityId, int animationId) {
		if (type == IS_PLAYER || type == IS_NPC) { // auto convert to new
			// dialogue all old
			// dialogues
			String[] texts = new String[talkDefinitons.length - 1];
			for (int i = 0; i < texts.length; i++) {
				texts[i] = talkDefinitons[i + 1];
			}
			sendEntityDialogue(type, talkDefinitons[0], entityId, animationId, texts);
			return true;
		}
		int[] componentOptions = getIComponentsIds(interId);
		if (componentOptions == null) {
			return false;
		}
		player.getInterfaceManager().sendChatBoxInterface(interId);
		if (talkDefinitons.length != componentOptions.length) {
			return false;
		}
		for (int childOptionId = 0; childOptionId < componentOptions.length; childOptionId++) {
			player.getPackets().sendIComponentText(interId, componentOptions[childOptionId], talkDefinitons[childOptionId]);
		}
		if (type == IS_ITEM) {
			player.getPackets().sendItemOnIComponent(interId, 2, entityId, animationId);
		}
		return true;
	}

	/**
	 * Opens a specific store
	 *
	 * @param name
	 * 		The name of the store
	 */
	protected void openStore(String name) {
		GsonStartup.getOptional(StoreLoader.class).ifPresent(c -> c.openStore(player, name));
	}

	public int getStage() {
		return stage;
	}
}