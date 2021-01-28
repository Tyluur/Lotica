package com.runescape.game.world.entity.player;

import com.runescape.game.event.interaction.button.QuestTabInteractionEvent;
import com.runescape.utility.ChatColors;

import java.util.concurrent.ConcurrentHashMap;

public class InterfaceManager {

	public static final int FIXED_WINDOW_ID = 548;

	public static final int RESIZABLE_WINDOW_ID = 746;

	public static final int CHAT_BOX_TAB = 13;

	public static final int FIXED_SCREEN_TAB_ID = 9;

	public static final int RESIZABLE_SCREEN_TAB_ID = 12;

	public static final int FIXED_INV_TAB_ID = 199;

	public static final int RESIZABLE_INV_TAB_ID = 87;

	private Player player;

	private final ConcurrentHashMap<Integer, int[]> openedinterfaces = new ConcurrentHashMap<>();

	private boolean resizableScreen;

	private int windowsPane;

	private transient boolean clientActive;

	public InterfaceManager(Player player) {
		this.player = player;
	}

	public void sendTab(int tabId, int interfaceId) {
		player.getPackets().sendInterface(true, resizableScreen ? RESIZABLE_WINDOW_ID : FIXED_WINDOW_ID, tabId, interfaceId);
	}

	public void sendChatBoxInterface(int interfaceId) {
		player.getPackets().sendInterface(true, 752, CHAT_BOX_TAB, interfaceId);
	}

	public void closeChatBoxInterface() {
		player.getPackets().closeInterface(CHAT_BOX_TAB);
	}

	public void sendOverlay(int interfaceId, boolean fullScreen) {
		sendTab(resizableScreen ? fullScreen ? 1 : 11 : 0, interfaceId);
	}

	public void closeOverlay(boolean fullScreen) {
		player.getPackets().closeInterface(resizableScreen ? fullScreen ? 1 : 11 : 0);
	}

	public void sendInterface(int interfaceId) {
		player.getPackets().sendInterface(false, resizableScreen ? 746 : 548, resizableScreen ? 12 : 9, interfaceId);
	}

	public void sendInventoryInterface(int childId) {
		player.getPackets().sendInterface(false, resizableScreen ? 746 : 548, resizableScreen ? 87 : 199, childId);
	}

	public final void sendInterfaces() {
		if (player.getDisplayMode() == 2 || player.getDisplayMode() == 3) {
			resizableScreen = true;
			sendFullScreenInterfaces();
		} else {
			resizableScreen = false;
			sendFixedInterfaces();
		}
		player.getCombatDefinitions().sendUnlockAttackStylesButtons();
		player.getMusicsManager().unlockMusicPlayer();
		player.getEmotesManager().unlockEmotesBook();
		player.getInventory().unlockInventoryOptions();
		player.getPrayer().unlockPrayerBookButtons();
		if (player.getFamiliar() != null && player.isRunning()) { player.getFamiliar().unlock(); }
		player.getControllerManager().sendInterfaces();
	}

	public void replaceRealChatBoxInterface(int interfaceId) {
		player.getPackets().sendInterface(true, 752, 11, interfaceId);
	}

	public void closeReplacedRealChatBoxInterface() {
		player.getPackets().closeInterface(752, 11);
	}

	public void sendWindowPane() {
		player.getPackets().sendWindowsPane(resizableScreen ? 746 : 548, 0);
	}

	public void sendFullScreenInterfaces() {
		player.getPackets().sendWindowsPane(746, 0);
		sendTab(15, 745);
		sendTab(19, 751);
		sendTab(73, 752);
		sendTab(72, 754);
		sendTab(177, 748);
		sendTab(178, 749);
		sendTab(179, 750);
		sendTab(180, 747);
		player.getPackets().sendInterface(true, 752, 9, 137);
		sendCombatStyles();
		sendTaskSystem();
		sendSkills();
		sendQuestTab();
		sendInventory();
		sendEquipment();
		sendPrayerBook();
		sendMagicBook();
		sendFriends();
		sendFriendsChat();
		sendClanChat();
		sendSettings();
		sendEmotes();
		sendMusicTab();
		// sendNotesTab();
		sendLogoutTab();
	}

	public void sendFixedInterfaces() {

		player.getPackets().sendWindowsPane(548, 0);
		sendTab(15, 745);
		sendTab(68, 751);
		sendTab(192, 752);
		player.getPackets().sendInterface(true, 752, 9, 137);
		sendTab(17, 754);
		sendTab(183, 748);
		sendTab(185, 749);
		sendTab(186, 750);
		sendTab(188, 747);
		sendCombatStyles();// combat styles
		sendTaskSystem();
		sendCombatStyles();
		sendSkills();// skills
		sendQuestTab();// info tab
		sendInventory();
		sendEquipment();
		sendPrayerBook();
		sendMagicBook();
		sendFriends();
		sendFriendsChat();// friends chat
		sendClanChat();// clan chat
		sendSettings();
		sendEmotes();// emotes
		sendMusicTab();// music
		// Notes Interface
		// sendNotesTab();
		sendLogoutTab(); // Logout tab
	}

	public void sendFriends() {
		sendTab(resizableScreen ? 99 : 213, 550);
	}

	public void sendTaskSystem() {
		int interfaceId = 930;
		sendTab(resizableScreen ? 91 : 205, interfaceId);
		player.getPackets().sendIComponentText(interfaceId, 10, "<col=" + ChatColors.RED + ">Information");
		player.getPackets().sendIComponentText(interfaceId, 16, "");
//		player.getPackets().sendHideIComponent(930, 12, true); //scroll bar
		for (byte i = 17; i < 25; i++) { player.getPackets().sendHideIComponent(930, i, true); }
	}

	public void sendLogoutTab() {
		sendTab(resizableScreen ? 108 : 222, 182);
	}

	public void sendMusicTab() {
		sendTab(resizableScreen ? 104 : 218, 187);
	}

	public void sendClanChat() {
		sendTab(resizableScreen ? 101 : 215, 875);
	}

	public void sendFriendsChat() {
		sendTab(resizableScreen ? 100 : 214, 1109);
	}

	public void sendQuestTab() {
		int interfaceId = 34;
		sendTab(onResizable() ? 93 : 207, interfaceId);
		QuestTabInteractionEvent.sendLoginConfiguration(player);
	}

	public void sendEquipment() {
		sendTab(resizableScreen ? 95 : 209, 387);
	}

	public void closeInterface(int one, int two) {
		player.getPackets().closeInterface(resizableScreen ? two : one);
	}

	public void closeEquipment() {
		removeTab(resizableScreen ? 95 : 209);
	}

	public void sendInventory() {
		sendTab(resizableScreen ? 94 : 208, Inventory.INVENTORY_INTERFACE);
	}

	public void closeInventory() {
		removeTab(resizableScreen ? 94 : 208);
	}

	public void closeSkills() {
		player.getPackets().closeInterface(resizableScreen ? 113 : 206);
	}

	public void closeCombatStyles() {
		player.getPackets().closeInterface(resizableScreen ? 111 : 204);
	}

	public void closeTaskSystem() {
		player.getPackets().closeInterface(resizableScreen ? 112 : 205);
	}

	public void sendCombatStyles() {
		sendTab(resizableScreen ? 90 : 204, 884);
	}

	public void sendSkills() {
		sendTab(resizableScreen ? 92 : 206, 320);
	}

	public void sendSettings() {
		sendSettings(261);
	}

	public void sendSettings(int interfaceId) {
		sendTab(resizableScreen ? 102 : 216, interfaceId);
	}

	public void sendPrayerBook() {
		sendTab(resizableScreen ? 96 : 210, 271);
	}

	public void closePrayerBook() {
		player.getPackets().closeInterface(resizableScreen ? 117 : 210);
	}

	public void sendMagicBook() {
		sendTab(resizableScreen ? 97 : 211, player.getCombatDefinitions().getSpellBook());
	}

	public void closeMagicBook() {
		player.getPackets().closeInterface(resizableScreen ? 118 : 211);
	}

	public void sendEmotes() {
		sendTab(resizableScreen ? 103 : 217, 464);
	}

	public void closeEmotes() {
		player.getPackets().closeInterface(resizableScreen ? 124 : 217);
	}

	public boolean addInterface(int windowId, int tabId, int childId) {
		if (openedinterfaces.containsKey(tabId)) { player.getPackets().closeInterface(tabId); }
		openedinterfaces.put(tabId, new int[] { childId, windowId });
		return openedinterfaces.get(tabId)[0] == childId;
	}

	public boolean containsInterface(int tabId, int childId) {
		if (childId == windowsPane) { return true; }
		if (!openedinterfaces.containsKey(tabId)) { return false; }
		return openedinterfaces.get(tabId)[0] == childId;
	}

	public int getTabWindow(int tabId) {
		if (!openedinterfaces.containsKey(tabId)) { return FIXED_WINDOW_ID; }
		return openedinterfaces.get(tabId)[1];
	}

	public boolean containsInterface(int childId) {
		if (childId == windowsPane) { return true; }
		for (int[] value : openedinterfaces.values()) { if (value[0] == childId) { return true; } }
		return false;
	}

	public boolean containsTab(int tabId) {
		return openedinterfaces.containsKey(tabId);
	}

	public void removeAll() {
		openedinterfaces.clear();
	}

	public boolean containsScreenInterface() {
		return containsTab(resizableScreen ? RESIZABLE_SCREEN_TAB_ID : FIXED_SCREEN_TAB_ID);
	}

	public void closeScreenInterface() {
		player.getPackets().closeInterface(resizableScreen ? 12 : 9);
	}

	public boolean containsInventoryInter() {
		return containsTab(resizableScreen ? 87 : 199);
	}

	public void closeInventoryInterface() {
		player.getPackets().closeInterface(resizableScreen ? RESIZABLE_INV_TAB_ID : FIXED_INV_TAB_ID);
	}

	public boolean containsChatBoxInter() {
		return containsTab(CHAT_BOX_TAB);
	}

	private void removeTab(int tabId) {
		player.getPackets().closeInterface(tabId);
	}

	public boolean removeTabInterface(int tabId) {
		return openedinterfaces.remove(tabId) != null;
	}

	public boolean removeInterface(int tabId, int childId) {
		if (!openedinterfaces.containsKey(tabId)) { return false; }
		if (openedinterfaces.get(tabId)[0] != childId) { return false; }
		return openedinterfaces.remove(tabId) != null;
	}

	public void sendFadingInterface(int backgroundInterface) {
		if (onResizable()) {
			player.getPackets().sendInterface(true, RESIZABLE_WINDOW_ID, 12, backgroundInterface);
		} else { player.getPackets().sendInterface(true, FIXED_WINDOW_ID, 11, backgroundInterface); }
	}

	public void closeFadingInterface() {
		if (onResizable()) { player.getPackets().closeInterface(12); } else { player.getPackets().closeInterface(11); }
	}

	public void sendScreenInterface(int backgroundInterface, int interfaceId) {
		player.getInterfaceManager().closeScreenInterface();
		if (onResizable()) {
			player.getPackets().sendInterface(false, RESIZABLE_WINDOW_ID, 40, backgroundInterface);
			player.getPackets().sendInterface(false, RESIZABLE_WINDOW_ID, 41, interfaceId);
		} else {
			player.getPackets().sendInterface(false, FIXED_WINDOW_ID, 200, backgroundInterface);
			player.getPackets().sendInterface(false, FIXED_WINDOW_ID, 201, interfaceId);
		}
		player.setCloseInterfacesEvent(() -> {
			if (onResizable()) {
				player.getPackets().closeInterface(40);
				player.getPackets().closeInterface(41);
			} else {
				player.getPackets().closeInterface(200);
				player.getPackets().closeInterface(201);
			}
		});
	}

	public boolean onResizable() {
		return resizableScreen;
	}

	public void setWindowsPane(int windowsPane) {
		this.windowsPane = windowsPane;
	}

	public int getWindowsPane() {
		return windowsPane;
	}

	public void gazeOrbOfOculus() {
		if (!player.getInterfaceManager().onResizable()) {
			player.sendMessage("The orb of oculus requires you to be in resizable mode for it to function.");
			return;
		}
		player.getPackets().sendWindowsPane(475, 0);
		player.getPackets().sendInterface(true, 475, 57, 751);
		player.getPackets().sendInterface(true, 475, 55, 752);
		player.setCloseInterfacesEvent(() -> {
			player.getPackets().sendWindowsPane(player.getInterfaceManager().onResizable() ? 746 : 548, 0);
			player.getPackets().sendResetCamera();
		});
	}

	/*
	 * returns lastGameTab
	 */
	public int openGameTab(int tabId) {
		player.getPackets().sendGlobalConfig(168, tabId);
		return 4;
	}

	public void sendOverlay(int interfaceId) {
		sendTab(onResizable() ? 10 : 8, interfaceId);
	}
	
	public void closeOverlay() {
		player.getPackets().closeInterface(onResizable() ? 10 : 8);
	}

    public boolean isClientActive() {
        return this.clientActive;
    }

    public void setClientActive(boolean clientActive) {
        this.clientActive = clientActive;
    }
}
