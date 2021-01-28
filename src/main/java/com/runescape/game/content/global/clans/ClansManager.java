package com.runescape.game.content.global.clans;

import com.runescape.cache.loaders.ClientScriptMap;
import com.runescape.game.event.InputEvent;
import com.runescape.game.event.InputEvent.InputEventType;
import com.runescape.game.interaction.dialogues.impl.misc.ClanInvite;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.ChatMessage;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.QuickChatMessage;
import com.runescape.network.stream.OutputStream;
import com.runescape.utility.SerializableFilesManager;
import com.runescape.utility.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;

public class ClansManager {

	private Clan clan;

	private ArrayList<Player> channelPlayers; // players connected to clan

	// channel, may be less than on
	// clan itself
	private ArrayList<Player> membersOnline; // members connected to clan

	private HashMap<String, Long> bannedChannelPlayers;

	private byte[] clanSettingsDataBlock;

	private byte[] clanChannelDataBlock;

	private int nextUpdateNumber; // increases every time a change is done on
	// clansettings

	private static HashMap<String, ClansManager> cachedClans;

	public static void init() {
		cachedClans = new HashMap<String, ClansManager>();
	}

	public static void viewClanmateDetails(Player player, ClanMember member) {
		player.getPackets().sendGlobalConfig(1500, member.getRank());
		player.getPackets().sendGlobalConfig(1501, member.getJob());
		player.getPackets().sendGlobalConfig(1564, 0);
		player.getPackets().sendGlobalConfig(1565, member.isBanFromKeep() ? 1 : 0);
		player.getPackets().sendGlobalConfig(1566, member.isBanFromCitadel() ? 1 : 0);
		player.getPackets().sendGlobalConfig(1567, member.isBanFromIsland() ? 1 : 0);
		player.getPackets().sendGlobalConfig(1568, member.firstWeek() ? 1 : 0);
		player.getPackets().sendGlobalString(347, Utils.formatPlayerNameForDisplay(member.getUsername()));
		player.getPackets().sendRunScript(4319);
	}

	public static void unlockBanList(Player player) {
		player.getPackets().sendUnlockIComponentOptionSlots(1110, 11, 0, 100, 0);
	}

	public ClansManager(Clan clan) {
		this.clan = clan;
		this.setChannelPlayers(new ArrayList<Player>());
		this.membersOnline = new ArrayList<Player>();
		this.bannedChannelPlayers = new HashMap<String, Long>();
		generateClanSettingsDataBlock();
		generateClanChannelDataBlock();
	}

	public static void viewClammateDetails(Player player, int index) {
		ClansManager manager = player.getClanManager();
		if (manager == null) {
			return;
		}
		ClanMember member = manager.getMemberByIndex(index);
		if (member == null) {
			return;
		}
		viewClanmateDetails(player, member);
		player.getAttributes().remove("editclanmaterank");
		player.getAttributes().remove("editclanmatejob");
		if (manager.hasRankToEditSettings(player)) {
			player.getAttributes().put("editclanmatedetails", member);
		} else {
			player.getAttributes().remove("editclanmatedetails");
		}
	}

	public static void kickClanmate(Player player) {
		ClansManager manager = player.getClanManager();
		if (manager == null) {
			return;
		}
		ClanMember member = (ClanMember) player.getAttributes().remove("editclanmatedetails");
		if (member == null) // means u not owner
		{
			return;
		}
		if (member.getUsername().equals(player.getUsername())) {
			player.getPackets().sendGameMessage("You can't kick yourself!");
			return;
		}
		if (member.getRank() == Clan.LEADER) {
			player.getPackets().sendGameMessage("You can't kick leader!");
			return;
		}
		manager.kickPlayer(member);
	}

	public static void leaveClan(Player player) {
		if (player.getClanManager() == null) {
			return;
		}
		player.getDialogueManager().startDialogue("LeaveClan");
	}

	public static void leaveClanCompletly(Player player) {
		ClansManager manager = player.getClanManager();
		if (manager == null) {
			return;
		}
		ClanMember mate = manager.getMemberByUsername(player.getUsername());
		if (mate == null) {
			return;
		}
		manager.kickPlayer(mate);
	}

	public void kickAllChannelPlayers() {
		synchronized (this) {
			for (Player player : new ArrayList<Player>(getChannelPlayers())) {
				disconnect(player, !membersOnline.contains(player));
			}
		}
	}

	public void kickPlayer(ClanMember mate) {
		synchronized (cachedClans) {
			synchronized (this) {
				clan.getMembers().remove(mate);
				Player player = World.getPlayer(mate.getUsername());
				if (player != null) {
					player.setClanName(null);
					if (player.getClanManager() != null) //somehow it happened
					{
						player.getClanManager().disconnect(player, false);
					}
					player.getPackets().sendGameMessage("You're no longer part of a clan.");
				}
				if (clan.getMembers().isEmpty()) {
					kickAllChannelPlayers();
					SerializableFilesManager.deleteClan(clan);
				} else {
					if (mate.getRank() == Clan.LEADER) {
						ClanMember newLeader = getHighestRank();
						if (newLeader != null) {
							newLeader.setRank(Clan.LEADER);
							clan.setClanLeaderUsername(newLeader);
							generateClanChannelDataBlock();
							refreshClanChannel();
							sendGlobalMessage("<col=7E2217>" + Utils.formatPlayerNameForDisplay(newLeader.getUsername()) + " has been appointed as new leader!");
						}
					}
					generateClanSettingsDataBlock();
					refreshClanSettings();
				}
			}
		}
	}

	public static void saveClanmateDetails(Player player) {
		ClansManager manager = player.getClanManager();
		if (manager == null) {
			return;
		}
		ClanMember member = (ClanMember) player.getAttributes().get("editclanmatedetails");
		if (member == null) // means u not owner
		{
			return;
		}
		Integer rank = (Integer) player.getAttributes().remove("editclanmaterank");
		Integer job = (Integer) player.getAttributes().remove("editclanmatejob");
		if (rank == null && job == null) {
			return;
		}
		synchronized (manager) {
			if (rank != null && member.getRank() == Clan.LEADER) {
				// sets highest rank member to leader
				ClanMember newLeader = manager.getDeputyOwner();
				if (newLeader == null) {
					player.getPackets().sendGameMessage("Please select a deputy owner before changing your own rank.");
					return;
				}
				newLeader.setRank(Clan.LEADER);
				manager.clan.setClanLeaderUsername(newLeader);
				manager.sendGlobalMessage("<col=7E2217>" + Utils.formatPlayerNameForDisplay(newLeader.getUsername()) + " has been appointed as new leader!");
			}
			if (rank != null) {
				member.setRank(rank);
				player.getPackets().sendGlobalConfig(1500, rank);
				manager.generateClanChannelDataBlock();
				manager.refreshClanChannel();
			}
			if (job != null) {
				member.setJob(job);
				player.getPackets().sendGlobalConfig(1501, job);
			}
			manager.generateClanSettingsDataBlock();
			manager.refreshClanSettings();
		}
	}

	public void sendGlobalMessage(String message) {
		synchronized (this) {
			for (Player player : membersOnline) {
				player.getPackets().sendGameMessage(message);
			}
		}
	}

	public ClanMember getHighestRank() {
		synchronized (this) {
			ClanMember highest = null;
			for (ClanMember member : clan.getMembers()) {
				if (highest == null || member.getRank() > highest.getRank()) {
					highest = member;
				}
			}
			return highest;
		}
	}

	public ClanMember getDeputyOwner() {
		synchronized (this) {
			for (ClanMember member : clan.getMembers()) {
				if (member.getRank() == Clan.DEPUTY_OWNER) {
					return member;
				}
			}
			return null;
		}
	}

	public ClanMember getMemberByIndex(int index) {
		synchronized (this) {
			if (clan.getMembers().size() <= index) {
				return null;
			}
			return clan.getMembers().get(index);
		}
	}

	public ClanMember getMemberByUsername(String username) {
		synchronized (this) {
			for (ClanMember member : clan.getMembers()) {
				if (member.getUsername().equals(username)) {
					return member;
				}
			}
			return null;
		}
	}

	public static void openNationalFlagInterface(Player player) {
		ClansManager manager = player.getClanManager();
		if (manager == null) {
			return;
		}
		if (!manager.hasRankToEditSettings(player)) {
			return;
		}
		player.getInterfaceManager().sendInterface(1089);
		player.getPackets().sendUnlockIComponentOptionSlots(1089, 30, 0, 241, 0);
	}

	public static void openForumThreadInterface(Player player) {
		ClansManager manager = player.getClanManager();
		if (manager == null) {
			return;
		}
		if (!manager.hasRankToEditSettings(player)) {
			return;
		}
		player.stopAll();
		player.getDialogueManager().startDialogue("ForumThreadId");
	}

	public static void openClanMottoInterface(Player player) {
		ClansManager manager = player.getClanManager();
		if (manager == null) {
			return;
		}
		if (!manager.hasRankToEditSettings(player)) {
			return;
		}
		player.stopAll();
		player.getDialogueManager().startDialogue("ClanMotto");
	}

	public static void openClanMottifInterface(Player player) {
		ClansManager manager = player.getClanManager();
		if (manager == null) {
			return;
		}
		if (!manager.hasRankToEditSettings(player)) {
			return;
		}
		player.stopAll();
		player.getInterfaceManager().sendInterface(1105);
		//colors hides
		player.getPackets().sendHideIComponent(1105, 35, true);
		player.getPackets().sendHideIComponent(1105, 36, true);
		player.getPackets().sendHideIComponent(1105, 37, true);
		player.getPackets().sendHideIComponent(1105, 37, true);
		player.getPackets().sendHideIComponent(1105, 38, true);
		player.getPackets().sendHideIComponent(1105, 39, true);
		player.getPackets().sendHideIComponent(1105, 43, true);
		player.getPackets().sendHideIComponent(1105, 44, true);
		player.getPackets().sendHideIComponent(1105, 45, true);
		//bottom layer hides
		player.getPackets().sendHideIComponent(1105, 62, true);
		player.getPackets().sendHideIComponent(1105, 63, true);
		player.getPackets().sendHideIComponent(1105, 69, true);
		player.getPackets().sendUnlockIComponentOptionSlots(1105, 66, 0, 116, 0); // top
		player.getPackets().sendUnlockIComponentOptionSlots(1105, 63, 0, 116, 0); // button
		for (int i = 0; i < manager.clan.getMottifColors().length; i++) {
			player.getPackets().sendConfig(2094 + i, manager.clan.getMottifColors()[i]);
		}
	}

	public static void openSetMottifColor(Player player, int part) {
		player.getAttributes().put("MottifCustomize", part);
		player.getInterfaceManager().sendInterface(1106);
	}

	public static void setMottifColor(Player player, int color) {
		ClansManager manager = player.getClanManager();
		if (manager == null) {
			return;
		}
		Integer part = (Integer) player.getAttributes().remove("MottifCustomize");
		if (part == null) {
			return;
		}
		manager.setMottifColor(player, color, part);
	}

	public void setMottifColor(Player player, int color, int part) {
		synchronized (this) {
			player.getPackets().sendConfig(2094 + part, clan.getMottifColors()[part] = color);
			refreshAllMembersAppearence();
			generateClanSettingsDataBlock();
			refreshClanSettings();
			player.getInterfaceManager().sendInterface(1105);
			//hide colors
			player.getPackets().sendHideIComponent(1105, 35, true);
			player.getPackets().sendHideIComponent(1105, 36, true);
			player.getPackets().sendHideIComponent(1105, 37, true);
			player.getPackets().sendHideIComponent(1105, 37, true);
			player.getPackets().sendHideIComponent(1105, 38, true);
			player.getPackets().sendHideIComponent(1105, 39, true);
			player.getPackets().sendHideIComponent(1105, 43, true);
			player.getPackets().sendHideIComponent(1105, 44, true);
			player.getPackets().sendHideIComponent(1105, 45, true);
			//bottom layer hides
			player.getPackets().sendHideIComponent(1105, 62, true);
			player.getPackets().sendHideIComponent(1105, 63, true);
			player.getPackets().sendHideIComponent(1105, 69, true);
		}
	}

	public void refreshAllMembersAppearence() {
		synchronized (this) {
			for (Player player : membersOnline) {
				player.getAppearence().generateAppearenceData();
			}
		}
	}

	public static void setClanMottifTextureInterface(Player player, boolean top, int slot) {
		if (slot > 116) {
			return;
		}
		ClansManager manager = player.getClanManager();
		if (manager == null) {
			return;
		}
		if (!manager.hasRankToEditSettings(player)) {
			return;
		}
		manager.setClanMottifTexture(player, top, slot);
	}

	public void setClanMottifTexture(Player player, boolean top, int slot) {
		synchronized (this) {
			if (top) {
				clan.setMottifTop(slot);
			} else {
				clan.setMottifBottom(slot);
			}
			refreshAllMembersAppearence();
			generateClanSettingsDataBlock();
			refreshClanSettings();
		}
	}

	public static void setClanMottoInterface(Player player, String motto) {
		player.stopAll();
		player.getLockManagement().lockAll(1000);
		ClansManager manager = player.getClanManager();
		if (manager == null) {
			return;
		}
		manager.setMotto(player, motto);
	}

	public void setMotto(Player player, String motto) {
		synchronized (this) {
			if (!hasRankToEditSettings(player)) {
				return;
			}
			clan.setMotto(motto);
			generateClanSettingsDataBlock();
			refreshClanSettings();
		}
	}

	public static void setThreadIdInterface(Player player, String id) {
		id = id.toLowerCase();
		player.stopAll();
		player.getLockManagement().lockAll(1000);
		ClansManager manager = player.getClanManager();
		if (manager == null) {
			return;
		}
		manager.setThreadId(player, id.isEmpty() ? null : id);
	}

	public void setThreadId(Player player, String id) {
		synchronized (this) {
			if (!hasRankToEditSettings(player)) {
				return;
			}
			clan.setThreadId(id);
			generateClanSettingsDataBlock();
			refreshClanSettings();
		}
	}

	public static void setClanFlagInterface(Player player, int flag) {
		if (flag > 241) {
			return;
		}
		ClansManager manager = player.getClanManager();
		if (manager == null) {
			return;
		}
		manager.setClanFlag(player, flag);
	}

	public void setClanFlag(Player player, int flag) {
		synchronized (this) {
			if (!hasRankToEditSettings(player)) {
				return;
			}
			clan.setClanFlag(flag);
			generateClanSettingsDataBlock();
			refreshClanSettings();
		}
	}

	public static void setTimeZoneInterface(Player player, int time) {
		ClansManager manager = player.getClanManager();
		if (manager == null) {
			return;
		}
		manager.setTimeZone(player, time);
	}

	public static void switchRecruitingInterface(Player player) {
		ClansManager manager = player.getClanManager();
		if (manager == null) {
			return;
		}
		manager.switchRecruiting(player);
		// TODO switch flag on inter
	}

	public static void switchClanTimeInterface(Player player) {
		ClansManager manager = player.getClanManager();
		if (manager == null) {
			return;
		}
		manager.switchClanTime(player);
		// TODO switch flag on inter
	}

	public void switchClanTime(Player player) {
		synchronized (this) {
			if (!hasRankToEditSettings(player)) {
				return;
			}
			clan.switchClanTime();
			generateClanSettingsDataBlock();
			refreshClanSettings();
		}
	}

	public void setTimeZone(Player player, int time) {
		synchronized (this) {
			if (!hasRankToEditSettings(player)) {
				return;
			}
			clan.setTimeZone(time);
			generateClanSettingsDataBlock();
			refreshClanSettings();
		}
	}

	public static void setWorldIdInterface(Player player, int worldId) {
		if (worldId > 200) {
			return;
		}
		ClansManager manager = player.getClanManager();
		if (manager == null) {
			return;
		}
		manager.setWorldId(player, worldId);
	}

	public Clan getClan() {
		synchronized (this) {
			return clan;
		}
	}

	public void setWorldId(Player player, int worldId) {
		synchronized (this) {
			if (!hasRankToEditSettings(player)) {
				return;
			}
			clan.setWorldId(worldId);
			generateClanSettingsDataBlock();
			refreshClanSettings();
		}
	}

	public void switchRecruiting(Player player) {
		synchronized (this) {
			if (!hasRankToEditSettings(player)) {
				return;
			}
			clan.switchRecruiting();
			generateClanSettingsDataBlock();
			refreshClanSettings();
		}
	}

	public static void switchGuestsInChatCanEnterInterface(Player player) {
		ClansManager manager = player.getClanManager();
		if (manager == null) {
			return;
		}
		manager.switchGuestsInChatCanEnter(player);
	}

	public static void switchGuestsInChatCanTalkInterface(Player player) {
		ClansManager manager = player.getClanManager();
		if (manager == null) {
			return;
		}
		manager.switchGuestsInChatCanTalk(player);
	}

	public void switchGuestsInChatCanTalk(Player player) {
		synchronized (this) {
			if (!hasRankToEditSettings(player)) {
				return;
			}
			clan.switchGuestsInChatCanTalk();
			generateClanChannelDataBlock();
			refreshClanChannel();
		}
	}

	public void sendMessage(Player player, ChatMessage message) {
		synchronized (this) {
			String displayName = player.getDisplayName();
			int rights = player.getPrimaryRight().getClientRight();
			for (Player p2 : getChannelPlayers()) {
				p2.getPackets().receiveClanChatMessage(membersOnline.contains(p2), displayName, rights, message);
			}
		}
	}

	public Player getPlayerByDisplayName(String username) {
		synchronized (this) {
			String formatedUsername = Utils.formatPlayerNameForProtocol(username);
			for (Player player : getChannelPlayers()) {
				if (player.getUsername().equals(formatedUsername) || player.getDisplayName().equalsIgnoreCase(username)) {
					return player;
				}
			}
			return null;
		}
	}

	public void kickPlayerFromChat(Player player, String username) {
		String name = "";
		for (char character : username.toLowerCase().toCharArray()) {
			name += Utils.containsInvalidCharacter(character) ? " " : character;
		}
		synchronized (this) {
			Player kicked = getPlayerByDisplayName(name);
			if (kicked == null) {
				player.getPackets().sendGameMessage("This player is not this channel.");
				return;
			}
			if (getMemberByUsername(kicked.getUsername()) != null) {
				return;
			}
			bannedChannelPlayers.put(kicked.getUsername(), Utils.currentTimeMillis());
			disconnect(kicked, true);
			kicked.getPackets().sendGameMessage("You have been kicked from the guest clan chat channel.");
			player.getPackets().sendGameMessage("You have kicked " + kicked.getUsername() + " from clan chat channel.");

		}
	}

	public void sendQuickMessage(Player player, QuickChatMessage message) {
		synchronized (this) {
			player.sendMessage("Temporarily disabled.");
			/*String displayName = player.getDisplayName();
			int rights = player.getPrimaryRight().getClientRight();
			for (Player p2 : getChannelPlayers()) {
				p2.getPackets().receiveClanChatQuickMessage(membersOnline.contains(p2), displayName, rights, message);
			}*/
		}
	}


	public void switchGuestsInChatCanEnter(Player player) {
		synchronized (this) {
			if (!hasRankToEditSettings(player)) {
				return;
			}
			clan.switchGuestsInChatCanEnter();
			generateClanSettingsDataBlock();
			refreshClanSettings();
		}
	}

	public boolean isMember(Player player) {
		synchronized (this) {
			for (ClanMember member : clan.getMembers()) {
				if (member.getUsername().equals(player.getUsername())) {
					return true;
				}
			}
			return false;
		}
	}

	public boolean hasRankToEditSettings(Player player) {
		return getRank(player) >= Clan.ADMIN;
	}

	public boolean hasRankToInvite(Player player) {
		return getRank(player) >= Clan.ADMIN;
	}

	public int getRank(Player player) {
		synchronized (this) {
			for (ClanMember member : clan.getMembers()) {
				if (member.getUsername().equals(player.getUsername())) {
					return member.getRank();
				}
			}
			return -1;
		}
	}

	public void generateClanChannelDataBlock() {
		OutputStream stream = new OutputStream();
		stream.writeByte(0x2); // read name as string, 0x1 for long
		stream.writeLong(4062231702422979939L); // uid
		stream.writeLong(nextUpdateNumber);
		stream.writeString(clan.getClanName());
		stream.writeByte(0); // unused
		stream.writeByte(clan.getMinimumRankForKick());
		stream.writeByte(clan.isGuestsInChatCanTalk() ? -1 : 0); // getMinimumRankForChat
		// maybe
		stream.writeShort(getChannelPlayers().size());
		for (Player player : getChannelPlayers()) {
			// stream.writeLong(
			stream.writeString(player.getDisplayName());
			stream.writeByte(getRank(player));
			stream.writeShort(1); // worldId
		}
		clanChannelDataBlock = new byte[stream.getOffset()];
		stream.setOffset(0);
		stream.getBytes(clanChannelDataBlock, 0, clanChannelDataBlock.length);
	}

	public void generateClanSettingsDataBlock() {
		OutputStream stream = new OutputStream();
		int version = 3;
		stream.writeByte(version); // lowest clan version protocol
		stream.writeByte(0x2); // read name as string, 0x1 for long
		stream.writeInt(nextUpdateNumber);
		stream.writeInt(0); // probably had same usage anyway doesnt anymore
		stream.writeShort(clan.getMembers().size());
		stream.writeByte(clan.getBannedUsers().size());
		stream.writeString(clan.getClanName());
		if (version >= 4) {
			stream.writeInt(0);
		}
		stream.writeByte(clan.isGuestsInChatCanEnter() ? 1 : 0);
		stream.writeByte(1); // unknown
		stream.writeByte(0); // some rank for something in clan channel
		stream.writeByte(0); // unknown
		stream.writeByte(0); // unknown
		for (ClanMember member : clan.getMembers()) {
			// stream.writeLong(
			stream.writeString(Utils.formatPlayerNameForDisplay(member.getUsername()));
			stream.writeByte(member.getRank());
			if (version >= 2) {
				stream.writeInt(0); // unknown
			}
			if (version >= 5) {
				stream.writeShort(0); // unknown
			}
		}
		for (String bannedUser : clan.getBannedUsers()) {
			// stream.writeLong(bannedUser);
			stream.writeString(bannedUser);
		}
		if (version >= 3) {
			int count = 2;
			if (clan.getTimeZone() != 0) {
				count++;
			}
			if (clan.getMotto() != null) {
				count++;
			}
			if (clan.getThreadId() != null) {
				count++;
			}
			if (clan.isRecruiting() || clan.isClanTime() || clan.getWorldId() != 0 || clan.getClanFlag() != 0) {
				count++;
			}
			if (clan.getMottifTop() != 0 || clan.getMottifBottom() != 0) {
				count++;
			}
			stream.writeShort(count); // configsCount
			if (clan.getTimeZone() != 0) {
				stream.writeInt(0 | 0 << 30);
				stream.writeInt(clan.getTimeZone());
			}
			if (clan.getMotto() != null) {
				stream.writeInt(1 | 2 << 30);
				stream.writeString(clan.getMotto());
			}
			if (clan.getThreadId() != null) {
				stream.writeInt(2 | 1 << 30);
				stream.writeLong(convertToLong(clan.getThreadId()));
			}
			if (clan.isRecruiting() || clan.isClanTime() || clan.getWorldId() != 0 || clan.getClanFlag() != 0) {
				stream.writeInt(3 | 0 << 30);
				stream.writeInt((clan.isRecruiting() ? 1 : 0) | (clan.isClanTime() ? 1 : 0) << 1 | clan.getWorldId() << 2 | clan.getClanFlag() << 10);
			}
			if (clan.getMottifTop() != 0 || clan.getMottifBottom() != 0) {
				stream.writeInt(13 | 0 << 30);
				stream.writeInt(clan.getMottifTop() | clan.getMottifBottom() << 16);
			}
			stream.writeInt(16 | 0 << 30);
			stream.writeInt(clan.getMottifColors()[0] | clan.getMottifColors()[1] << 16);
			stream.writeInt(18 | 0 << 30);
			stream.writeInt(clan.getMottifColors()[2] | clan.getMottifColors()[3] << 16);
		}
		clanSettingsDataBlock = new byte[stream.getOffset()];
		stream.setOffset(0);
		stream.getBytes(clanSettingsDataBlock, 0, clanSettingsDataBlock.length);
	}

	public static final char[] VALID_CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
			                                         'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
			                                         'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };

	public static int getCharacterIndex(int c) {
		for (int i = 0; i < VALID_CHARS.length; i++) {
			if (VALID_CHARS[i] == c) {
				return i;
			}
		}
		return -1;
	}

	public static long convertToLong(String text) {
		long hash = 0;
		int count = text.length() - 1;
		for (char c : text.toCharArray()) {
			hash += getCharacterIndex(c) * Math.pow(36, count--);
		}
		return hash;

	}

	public byte[] getClanChannelDataBlock() {
		return clanChannelDataBlock;
	}

	public byte[] getClanSettingsDataBlock() {
		return clanSettingsDataBlock;
	}

	public void refreshClanSettings() {
		synchronized (this) {
			for (Player player : membersOnline) {
				player.getPackets().sendClanSettings(this, true);
			}
		}
	}

	public void refreshClanChannel() {
		try {
			synchronized (this) {
				for (Player player : getChannelPlayers()) {
					player.getPackets().sendClanChannel(this, membersOnline.contains(player));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void connect(Player player, boolean guestClan) {
		synchronized (this) {
			if (!guestClan) {
				membersOnline.add(player);
				player.getPackets().sendClanSettings(this, true);
			}
			if (guestClan || player.isConnectedClanChannel()) {
				connectChannel(player);
			}
			linkClan(player, guestClan);
		}
	}

	public void linkClan(Player player, boolean guestClan) {
		if (guestClan) {
			player.setGuestClanManager(this);
		} else {
			player.setClanManager(this);
		}
	}

	public void unlinkClan(Player player, boolean guestClan) {
		if (guestClan) {
			player.setGuestClanManager(null);
		} else {
			player.setClanManager(null);
		}
	}

	public void disconnect(Player player, boolean guestClan) {
		synchronized (cachedClans) {
			synchronized (this) {
				if (guestClan || player.isConnectedClanChannel()) {
					disconnectChannel(player);
				}
				if (!guestClan) {
					membersOnline.remove(player);
				}
				unlinkClan(player, guestClan);
				destroyIfEmpty();
			}
		}
	}

	/*
	 * dont use this without synchronized (cachedClans) and synchronized (this)
	 * else exept disconnect
	 */
	private void destroyIfEmpty() {
		if (empty()) {
			SerializableFilesManager.saveClan(clan);
			cachedClans.remove(clan.getClanName());
		}
	}

	public void connectChannel(Player player) {
		synchronized (this) {
			getChannelPlayers().add(player);
			generateClanChannelDataBlock();
			refreshClanChannel();
		}
	}

	/*
	 * only used by normal channel by itself
	 */
	private void disconnectChannel(Player player) {
		synchronized (this) {
			getChannelPlayers().remove(player);
			player.getPackets().sendClanChannel(null, membersOnline.contains(player));
			generateClanChannelDataBlock();
			refreshClanChannel();
		}
	}

	public boolean empty() {
		return membersOnline.size() == 0 && getChannelPlayers().size() == 0;
	}

	public static void linkClanMember(Player player, String clanName) {
		player.setClanName(clanName);
		player.setConnectedClanChannel(true);
		player.getPackets().sendGameMessage("You have joined the clan, so you are now part of " + clanName + ".");
		connectToClan(player, clanName, false);
	}

	public static void createClan(Player player, String clanName) {
		clanName = Utils.formatPlayerNameForDisplay(clanName);
		if (player.getClanManager() != null) {
			return;
		}
		synchronized (cachedClans) {
			if (SerializableFilesManager.containsClan(clanName)) {
				player.getPackets().sendGameMessage("The clan name you tried already exists.");
				return;
			}
			Clan clan = new Clan(clanName, player);
			SerializableFilesManager.saveClan(clan);
			linkClanMember(player, clanName);
		}
	}

	public static void joinClan(Player player, Player inviter) {
		player.stopAll();
		if (inviter == null) {
			return;
		}
		ClansManager manager = inviter.getClanManager();
		if (manager == null) {
			return;
		}
		synchronized (manager) {
			if (player.getGuestClanManager() != null) {
				player.getGuestClanManager().disconnect(player, true);
			}
			manager.clan.addMember(player, Clan.RECRUIT);
			manager.generateClanSettingsDataBlock();
			manager.refreshClanSettings();
			linkClanMember(player, manager.clan.getClanName());
		}
	}

	public static boolean connectToClan(Player player, String clanName, boolean guest) {
		clanName = Utils.formatPlayerNameForDisplay(clanName);
		ClansManager manager = guest ? player.getGuestClanManager() : player.getClanManager();
		if (manager != null || guest && player.getClanName() != null && clanName.equalsIgnoreCase(player.getClanName())) // already
		// connected
		// to
		// a
		// clan//create a folder name clans in data
		{
			return false;
		}
		synchronized (cachedClans) {
			manager = cachedClans.get(clanName); // grabs clan
			boolean created = manager != null;
			if (!created) { // not loaded
				if (!SerializableFilesManager.containsClan(clanName)) {
					player.getPackets().sendIComponentText(1110, 70, "Could not find a clan named " + clanName + ". Please check the name and try again.");
					return false;
				}
				Clan clan = SerializableFilesManager.loadClan(clanName);
				if (clan == null) {
					return false;
				}
				clan.setClanName(clanName);
				manager = new ClansManager(clan);
			} else {
				synchronized (manager) {
					if (guest) {
						Long bannedSince = manager.bannedChannelPlayers.get(player.getUsername());
						if (bannedSince != null) {
							if (bannedSince + 3600000 > Utils.currentTimeMillis()) {
								player.getPackets().sendGameMessage("You have been banned from this channel.");
								return false;
							}
							manager.bannedChannelPlayers.remove(player.getUsername());
						}
					}
				}
			}
			synchronized (manager) {
				if (!guest && !manager.isMember(player)) {
					player.getPackets().sendGameMessage("You have been kicked from the clan.");
					return false;
				}
				if (guest) {
					if (!manager.clan.isGuestsInChatCanEnter()) {
						player.getPackets().sendGameMessage("This clan only allows clanmates to join their channel.");
						player.getPackets().sendIComponentText(1110, 70, "This clan only allows clanmates to join their channel.");
						return false;
					}
					if (manager.getClan().getBannedUsers().contains(player.getUsername())) {
						player.getPackets().sendGameMessage("You have been banned from this channel.");
						return false;
					}
				}
				if (!created) {
					cachedClans.put(clanName, manager);
				}
				if (guest) {
					player.getPackets().sendRunScript(4453);
				}
				manager.connect(player, guest);
				return true;
			}
		}
	}

	public static void openClanDetails(Player player) {
		if (player.getClanManager() == null) {
			player.getPackets().sendGameMessage("You're not in a clan.");
			return;
		}
		if (player.getInterfaceManager().containsScreenInterface() || player.getInterfaceManager().containsInventoryInter()) {
			player.getPackets().sendGameMessage("Please close the interface you have open first.");
			return;
		}
		openClanDetails(player, null, player.getClanManager());
	}

	/*
	 * can be used to see clan details when recruiting too
	 */
	public static void openClanDetails(Player player, Player p2, ClansManager manager) {
		player.getPackets().sendClanSettings(manager, false);
		player.getInterfaceManager().sendInterface(1107);
		if (manager.clan.getMotto() != null) {
			player.getPackets().sendIComponentText(1107, 88, manager.clan.getMotto());
		}
		if (manager.clan.getMottifTop() != 0) {
			player.getPackets().sendIComponentModel(1107, 96, getMottifSprite(manager.clan.getMottifTop()));
		}
		if (manager.clan.getMottifBottom() != 0) {
			player.getPackets().sendIComponentModel(1107, 106, getMottifSprite(manager.clan.getMottifBottom()));
		}
		DateFormat dateFormat = new SimpleDateFormat("HH:mm");
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("GMT"));
		player.getPackets().sendIComponentText(1107, 186, dateFormat.format(cal.getTime()));
		cal.add(Calendar.MINUTE, manager.clan.getTimeZone());
		player.getPackets().sendIComponentText(1107, 185, dateFormat.format(cal.getTime()));
		if (p2 != null) {
			player.getPackets().sendIComponentText(1107, 92, p2.getDisplayName());
		} else {
			player.getPackets().sendHideIComponent(1107, 90, true);
		}

	}

	public static int getMottifSprite(int slotId) {
		return ClientScriptMap.getMap(3686).getIntValue(slotId + 1);
	}

	public static int getMottifTexture(int slotId) {
		return ClientScriptMap.getMap(3685).getIntValue(slotId + 1);
	}

	public static void showClanSettingsClanMates(Player player) {
		showClanSettingsClanMates(player, false);
		showClanSettingsSettings(player, true);
		showClanSettingsPermissions(player, true);
	}

	public static void showClanSettingsSettings(Player player) {
		showClanSettingsClanMates(player, true);
		showClanSettingsSettings(player, false);
		showClanSettingsPermissions(player, true);
	}

	public static void showClanSettingsPermissions(Player player) {
		showClanSettingsClanMates(player, true);
		showClanSettingsSettings(player, true);
		showClanSettingsPermissions(player, false);
	}

	private static void showClanSettingsClanMates(Player player, boolean hide) {
		player.getPackets().sendHideIComponent(1096, 83, hide);
		player.getPackets().sendHideIComponent(1096, 110, hide);
	}

	private static void showClanSettingsSettings(Player player, boolean hide) {
		player.getPackets().sendHideIComponent(1096, 84, hide);
		player.getPackets().sendHideIComponent(1096, 117, hide);
	}

	private static void showClanSettingsPermissions(Player player, boolean hide) {
		player.getPackets().sendHideIComponent(1096, 85, hide);
		player.getPackets().sendHideIComponent(1096, 385, hide);
	}

	public static void selectPermissionTab(Player player, int tab) {
		player.getPackets().sendRunScript(5136, tab);
	}

	public static void selectPermissionRank(Player player, int selectedRank) {
		player.getPackets().sendGlobalConfig(1572, 1);
		player.getPackets().sendGlobalConfig(1574, 1);
		player.getPackets().sendGlobalConfig(1576, 1);
		player.getPackets().sendGlobalConfig(1577, 1);
		player.getPackets().sendGlobalConfig(1578, 1);
		player.getPackets().sendGlobalConfig(1579, 1);
		player.getPackets().sendGlobalConfig(1580, 1);
		player.getPackets().sendGlobalConfig(1581, 1);
		player.getPackets().sendGlobalConfig(1582, 1);
		player.getPackets().sendGlobalConfig(1583, 1);
		player.getPackets().sendGlobalConfig(1584, 1);
		player.getPackets().sendGlobalConfig(1585, 1);
		player.getPackets().sendGlobalConfig(1586, 1);
		player.getPackets().sendGlobalConfig(1587, 1);
		player.getPackets().sendGlobalConfig(1588, 1);
		player.getPackets().sendGlobalConfig(1589, 1);

		player.getPackets().sendGlobalConfig(1649, 1);

		player.getPackets().sendGlobalConfig(1590, 1);
		player.getPackets().sendGlobalConfig(1569, selectedRank); // selects
		// rank
		player.getPackets().sendGlobalConfig(1571, 1);
		player.getPackets().sendGlobalConfig(1570, 1);
		player.getPackets().sendGlobalConfig(1573, 1);
		player.getPackets().sendGlobalConfig(1575, 1);
		player.getPackets().sendGlobalConfig(1792, 1);

	}

	public static void openClanSettings(Player player) {
		if (player.getClanManager() == null) {
			player.getPackets().sendGameMessage("You must be in a clan to do that.");
			return;
		}
		player.getInterfaceManager().sendInterface(1096);
		showClanSettingsClanMates(player);
		selectPermissionTab(player, 1);
		player.getPackets().sendIComponentText(1096, 373, "Permissions changing is temporarily disabled.");
		player.getPackets().sendHideIComponent(1096, 202, false); // disable
		// acess to
		// citadel
		// setting
		player.getPackets().sendHideIComponent(1096, 203, false); // disable
		// acess to
		// citadel
		// setting
		player.getPackets().sendHideIComponent(1096, 217, false); // disable
		// signpost
		// permissions
		// setting
		player.getPackets().sendHideIComponent(1096, 218, false); // disable
		// signpost
		// permissions
		// setting
		player.getPackets().sendUnlockIComponentOptionSlots(1096, 240, 0, 144, 0); // unlocks
		// timezone
		// setting
		player.getPackets().sendUnlockIComponentOptionSlots(1096, 290, 0, 200, 0); // unlocks
		// worldid
		// setting
		player.getPackets().sendUnlockIComponentOptionSlots(1096, 41, 0, Clan.MAX_MEMBERS, 0); // unlocks
		// clanmates
		player.getPackets().sendUnlockIComponentOptionSlots(1096, 276, 0, 125, 0); // set
		// member
		// rank
		player.getPackets().sendUnlockIComponentOptionSlots(1096, 262, 0, 500, 0); // set
		// member
		// profession
	}

	public static void joinGuestClanChat(Player player) {
		if (player.getGuestClanManager() != null) {
			player.getGuestClanManager().disconnect(player, true);
		} else {
			player.getPackets().requestClientInput(new InputEvent("What clan would you like to enter?", InputEventType.LONG_TEXT) {
				@Override
				public void handleInput() {
					ClansManager.connectToClan(player, getInput(), true);
				}
			});
			player.getPackets().sendIComponentText(1110, 70, "Please enter the name of the clan whose Clan chat you wish to join as a guest. <br><br>To talk as a guest, start your<br>line<br>of chat with ///");
		}
	}

	public static void banPlayer(Player player) {
		ClansManager manager = player.getClanManager();
		if (manager == null) {
			player.getPackets().sendGameMessage("You're  not in a clan.");
			return;
		}
		if (manager.getRank(player) < Clan.ADMIN) {
			player.getPackets().sendGameMessage("You must be a clan admin to do that.");
			return;
		}
		player.getPackets().requestClientInput(new InputEvent("Enter the name of the play you wish to ban:", InputEventType.NAME) {
			@Override
			public void handleInput() {
				banPlayer(player, getInput());
			}
		});
	}

	public static void banPlayer(Player player, String name) {
		name = Utils.formatPlayerNameForDisplay(name);
		ClansManager manager = player.getClanManager();
		if (manager == null) {
			player.getPackets().sendGameMessage("You're  not in a clan.");
			return;
		}
		if (manager.getRank(player) < Clan.ADMIN) {
			player.getPackets().sendGameMessage("You must be a clan admin to do that.");
			return;
		}
		manager.ban(player, name);
	}

	public void ban(Player player, String name) {
		synchronized (this) {
			if (getMemberByUsername(name) != null) {
				player.getPackets().sendGameMessage("You can't add a member from your clan to banlist.");
				return;
			}
			if (clan.getBannedUsers().size() >= 100) {
				player.getPackets().sendGameMessage("Ban list is currently full.");
				return;
			}
			player.getPackets().sendGameMessage("Attempting to ban " + name + ".");
			clan.getBannedUsers().add(name);
			generateClanSettingsDataBlock();
			refreshClanSettings();
		}
	}

	public static void unbanPlayer(Player player) {
		ClansManager manager = player.getClanManager();
		if (manager == null) {
			player.getPackets().sendGameMessage("You must be a clan admin to do that.");
			return;
		}
		if (manager.getRank(player) < Clan.ADMIN) {
			player.getPackets().sendGameMessage("You must be a clan admin to do that.");
			return;
		}
		player.getPackets().requestClientInput(new InputEvent("Enter the name of the play you wish to unban:", InputEventType.NAME) {
			@Override
			public void handleInput() {
				unbanPlayer(player, getInput());
			}
		});
	}

	public static void unbanPlayer(Player player, String name) {
		name = Utils.formatPlayerNameForDisplay(name);
		ClansManager manager = player.getClanManager();
		if (manager == null) {
			player.getPackets().sendGameMessage("You must be a clan admin to do that.");
			return;
		}
		if (manager.getRank(player) < Clan.ADMIN) {
			player.getPackets().sendGameMessage("You must be a clan admin to do that.");
			return;
		}
		manager.unban(player, name);
	}

	public static void unbanPlayer(Player player, int index) {
		ClansManager manager = player.getClanManager();
		if (manager == null) {
			player.getPackets().sendGameMessage("You must be a clan admin to do that.");
			return;
		}
		if (manager.getRank(player) < Clan.ADMIN) {
			player.getPackets().sendGameMessage("You must be a clan admin to do that.");
			return;
		}
		manager.unban(player, index);
	}

	public void unban(Player player, int slot) {
		synchronized (this) {
			if (clan.getBannedUsers().size() <= slot) {
				return;
			}
			unban(player, clan.getBannedUsers().get(slot));
		}
	}

	public void unban(Player player, String name) {
		player.getPackets().sendGameMessage("Attempting to unban " + name + ".");
		synchronized (this) {
			if (clan.getBannedUsers().remove(name)) {
				generateClanSettingsDataBlock();
				refreshClanSettings();
			} else {
				player.getPackets().sendGameMessage("An error was encountered while applying trying to unban " + name + ". No player of that name could be found.");
			}
		}
	}

	public static void joinClanChatChannel(Player player) {
		if (player.getClanManager() == null) {
			player.getDialogueManager().startDialogue("ClanCreateDialogue");
			return;
		}
		if (player.isConnectedClanChannel()) {
			player.setConnectedClanChannel(false);
			player.getClanManager().disconnectChannel(player);
		} else {
			player.setConnectedClanChannel(true);
			player.getClanManager().connectChannel(player);
		}
	}

	public static boolean viewInvite(final Player player, Player p2) {
		if (player.getAttributes().remove("claninvite") != null) {
			player.getDialogueManager().startDialogue(ClanInvite.class, p2);
			return true;
		}
		return false;
	}

	public static void invite(Player player, Player p2) {
		ClansManager manager = player.getClanManager();
		if (manager == null) {
			player.getPackets().sendGameMessage("You must be in a clan to do that.");
			return;
		}
		synchronized (manager) {
			if (!manager.hasRankToInvite(player)) {
				player.getPackets().sendGameMessage("You don't have permissions to invite.");
				return;
			}
			if (manager.getClan().getBannedUsers().contains(p2.getUsername())) {
				player.getPackets().sendGameMessage("This player has been banned from this clan.");
				return;
			}
			if (manager.clan.getMembers().size() >= Clan.MAX_MEMBERS) {
				player.getPackets().sendGameMessage("Clans can't have over 500 members.");
			}
		}
		if (p2.getClanManager() != null) {
			player.getPackets().sendGameMessage("This player is already a member of another clan.");
			return;
		}
		/*if (p2.getInterfaceManager().containsScreenInterface() || p2.getControllerManager().getController() != null) {
			player.getPackets().sendGameMessage("The other player is busy.");
			return;
		}*/
		player.getPackets().sendGameMessage("Sending " + p2.getDisplayName() + " a invitation...");
		p2.getPackets().sendClanInviteMessage(player);
		p2.getAttributes().put("claninvite", Boolean.TRUE);
	}

	public ArrayList<Player> getChannelPlayers() {
		return channelPlayers;
	}

	public void setChannelPlayers(ArrayList<Player> channelPlayers) {
		this.channelPlayers = channelPlayers;
	}
}
