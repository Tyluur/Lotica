package com.runescape.game.content;

import com.runescape.game.GameConstants;
import com.runescape.game.content.global.minigames.clanwars.ClanWars;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.ChatMessage;
import com.runescape.game.world.entity.player.FriendsIgnores;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.QuickChatMessage;
import com.runescape.game.world.entity.player.rights.Right;
import com.runescape.game.world.entity.player.rights.RightManager;
import com.runescape.network.stream.OutputStream;
import com.runescape.utility.SerializableFilesManager;
import com.runescape.utility.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class FriendChatsManager {
	
	private String owner;
	
	private String ownerDisplayName;
	
	private FriendsIgnores settings;
	
	private CopyOnWriteArrayList<Player> players;
	
	private ConcurrentHashMap<String, Long> bannedPlayers;
	
	private byte[] dataBlock;
	
	/**
	 * The clan wars instance (if the clan is in a war).
	 */
	private ClanWars clanWars;
	
	private static HashMap<String, FriendChatsManager> cachedFriendChats;
	
	public static void init() {
		cachedFriendChats = new HashMap<>();
	}
	
	public int getRank(Right rights, String username) {
		if (rights == RightManager.OWNER) { return 127; }
		if (username.equals(owner)) { return 7; }
		return settings.getRank(username);
	}
	
	public CopyOnWriteArrayList<Player> getPlayers() {
		return players;
	}
	
	public int getWhoCanKickOnChat() {
		return settings.getWhoCanKickOnChat();
	}
	
	public String getOwnerDisplayName() {
		return ownerDisplayName;
	}
	
	public String getOwnerName() {
		return owner;
	}
	
	public String getChannelName() {
		return settings.getChatName().replaceAll("<img=", "");
	}
	
	private void joinChat(Player player) {
		synchronized (this) {
			if (!player.getUsername().equals(owner) && !settings.hasRankToJoin(player.getUsername()) && !player.hasPrivilegesOf(RightManager.OWNER)) {
				player.getPackets().sendGameMessage("You do not have a enough rank to join this friends chat channel.");
				return;
			}
			if (players.size() >= 100) {
				player.getPackets().sendGameMessage("This chat is full.");
				return;
			}
			Long bannedSince = bannedPlayers.get(player.getUsername());
			if (bannedSince != null) {
				if (bannedSince + 3600000 > Utils.currentTimeMillis()) {
					player.getPackets().sendGameMessage("You have been banned from this channel.");
					return;
				}
				bannedPlayers.remove(player.getUsername());
			}
			joinChatNoCheck(player);
		}
	}
	
	public void leaveChat(Player player, boolean logout) {
		synchronized (this) {
			player.setCurrentFriendChat(null);
			players.remove(player);
			if (players.size() == 0) { // no1 at chat so uncache it
				synchronized (cachedFriendChats) {
					cachedFriendChats.remove(owner);
				}
			} else { refreshChannel(); }
			if (!logout) {
				player.setCurrentFriendChatOwner(null);
				player.getPackets().sendGameMessage("You have left the channel.");
				player.getPackets().sendFriendsChatChannel();
			}
			if (clanWars != null) {
				clanWars.leave(player, false);
			}
		}
	}
	
	public Player getPlayerByDisplayName(String username) {
		String formatedUsername = Utils.formatPlayerNameForProtocol(username);
		for (Player player : players) {
			if (player.getUsername().equals(formatedUsername) || player.getDisplayName().equals(username)) {
				return player;
			}
		}
		return null;
	}
	
	public void kickPlayerFromChat(Player player, String username) {
		String name = "";
		for (char character : username.toCharArray()) {
			name += Utils.containsInvalidCharacter(character) ? " " : character;
		}
		synchronized (this) {
			int rank = getRank(player.getPrimaryRight(), player.getUsername());
			if (rank < getWhoCanKickOnChat()) { return; }
			Player kicked = getPlayerByDisplayName(name);
			if (kicked == null) {
				player.getPackets().sendGameMessage("This player is not this channel.");
				return;
			}
			if (rank <= getRank(kicked.getPrimaryRight(), kicked.getUsername())) { return; }
			kicked.setCurrentFriendChat(null);
			kicked.setCurrentFriendChatOwner(null);
			players.remove(kicked);
			bannedPlayers.put(kicked.getUsername(), Utils.currentTimeMillis());
			kicked.getPackets().sendFriendsChatChannel();
			kicked.getPackets().sendGameMessage("You have been kicked from the friends chat channel.");
			player.getPackets().sendGameMessage("You have kicked " + kicked.getUsername() + " from friends chat channel.");
			refreshChannel();
			
		}
	}
	
	private void joinChatNoCheck(Player player) {
		synchronized (this) {
			players.add(player);
			player.setCurrentFriendChat(this);
			player.setCurrentFriendChatOwner(owner);
			player.sendMessage("You are now talking in the friends chat channel " + settings.getChatName(), 1, true);
			refreshChannel();
		}
	}
	
	public void destroyChat() {
		synchronized (this) {
			for (Player player : players) {
				player.setCurrentFriendChat(null);
				player.setCurrentFriendChatOwner(null);
				player.getPackets().sendFriendsChatChannel();
				player.getPackets().sendGameMessage("You have been removed from this channel!");
			}
		}
		synchronized (cachedFriendChats) {
			cachedFriendChats.remove(owner);
		}
		
	}
	
	public void sendQuickMessage(Player player, QuickChatMessage message) {
		synchronized (this) {
			if (!player.getUsername().equals(owner) && !settings.canTalk(player) && !player.hasPrivilegesOf(RightManager.OWNER)) {
				player.getPackets().sendGameMessage("You do not have a enough rank to talk on this friends chat channel.");
				return;
			}
			String formatedName = Utils.formatPlayerNameForDisplay(player.getUsername());
			String displayName = player.getDisplayName();
			int rights = player.getMessageIcon();
			for (Player p2 : players) {
				p2.getPackets().receiveFriendChatQuickMessage(formatedName, displayName, rights, settings.getChatName(), message);
			}
		}
	}
	
	public void sendMessage(Player player, ChatMessage message) {
		synchronized (this) {
			if (!player.getUsername().equals(owner) && !settings.canTalk(player) && !player.getPrimaryRight().equals(RightManager.OWNER)) {
				player.getPackets().sendGameMessage("You do not have a enough rank to talk on this friends chat channel.");
				return;
			}
			String formatedName = Utils.formatPlayerNameForDisplay(player.getUsername());
			String displayName = player.getDisplayName();
			int rights = player.getMessageIcon();
			for (Player p2 : players) {
				p2.getPackets().receiveFriendChatMessage(formatedName, displayName, rights, settings.getChatName(), message);
			}
		}
	}
	
	public void sendDiceMessage(Player player, String message) {
		synchronized (this) {
			if (!player.getUsername().equals(owner) && !settings.canTalk(player) && !player.hasPrivilegesOf(RightManager.OWNER)) {
				player.getPackets().sendGameMessage("You do not have a enough rank to talk on this friends chat channel.");
				return;
			}
			for (Player p2 : players) {
				p2.getPackets().sendGameMessage(message);
			}
		}
	}
	
	private void refreshChannel() {
		synchronized (this) {
			OutputStream stream = new OutputStream();
			stream.writeString(ownerDisplayName);
			String ownerName = Utils.formatPlayerNameForDisplay(owner);
			stream.writeByte(getOwnerDisplayName().equals(ownerName) ? 0 : 1);
			if (!getOwnerDisplayName().equals(ownerName)) { stream.writeString(ownerName); }
			stream.writeLong(Utils.stringToLong(getChannelName()));
			int kickOffset = stream.getOffset();
			stream.writeByte(0);
			stream.writeByte(getPlayers().size());
			for (Player player : getPlayers()) {
				String displayName = player.getDisplayName();
				String name = Utils.formatPlayerNameForDisplay(player.getUsername());
				stream.writeString(displayName);
				stream.writeByte(displayName.equals(name) ? 0 : 1);
				if (!displayName.equals(name)) { stream.writeString(name); }
				stream.writeShort(1);
				int rank = getRank(player.getPrimaryRight(), player.getUsername());
				stream.writeByte(rank);
				stream.writeString(GameConstants.SERVER_NAME);
			}
			dataBlock = new byte[stream.getOffset()];
			stream.setOffset(0);
			stream.getBytes(dataBlock, 0, dataBlock.length);
			for (Player player : players) {
				dataBlock[kickOffset] = (byte) (player.getUsername().equals(owner) ? 0 : getWhoCanKickOnChat());
				player.getPackets().sendFriendsChatChannel();
			}
		}
	}
	
	public byte[] getDataBlock() {
		return dataBlock;
	}
	
	private FriendChatsManager(Player player) {
		owner = player.getUsername();
		ownerDisplayName = player.getDisplayName();
		settings = player.getFriendsIgnores();
		players = new CopyOnWriteArrayList<Player>();
		bannedPlayers = new ConcurrentHashMap<String, Long>();
	}
	
	public static void destroyChat(Player player) {
		synchronized (cachedFriendChats) {
			FriendChatsManager chat = getChatInstance(player.getUsername());
			if (chat == null) { return; }
			chat.destroyChat();
			player.getPackets().sendGameMessage("Your friends chat channel has now been disabled!");
		}
	}
	
	public static void linkSettings(Player player) {
		synchronized (cachedFriendChats) {
			FriendChatsManager chat = getChatInstance(player.getUsername());
			if (chat == null) { return; }
			chat.settings = player.getFriendsIgnores();
		}
	}
	
	public static void refreshChat(Player player) {
		synchronized (cachedFriendChats) {
			FriendChatsManager chat = getChatInstance(player.getUsername());
			if (chat == null) { return; }
			chat.refreshChannel();
		}
	}
	
	public static void joinChat(String ownerName, Player player) {
		synchronized (cachedFriendChats) {
			if (player.getCurrentFriendChat() != null) { return; }
			player.getPackets().sendGameMessage("Attempting to join channel...", true);
			String formatedName = Utils.formatPlayerNameForProtocol(ownerName);
			FriendChatsManager chat = getChatInstance(formatedName);
			if (chat == null) {
				Player owner = World.getPlayerByDisplayName(ownerName);
				if (owner == null) {
					if (!SerializableFilesManager.containsPlayer(formatedName)) {
						player.sendMessage("The channel you tried to join does not exist.", 2, true);
						return;
					}
					owner = SerializableFilesManager.loadPlayer(formatedName);
					if (owner == null) {
						player.sendMessage("The channel you tried to join does not exist.", 2, true);
						return;
					}
					owner.setUsername(formatedName);
				}
				FriendsIgnores settings = owner.getFriendsIgnores();
				if (!settings.hasFriendChat()) {
					player.sendMessage("The channel you tried to join does not exist.", 2, true);
					return;
				}
				if (!player.getUsername().equals(ownerName) && !settings.hasRankToJoin(player.getUsername()) && !player.hasPrivilegesOf(RightManager.OWNER)) {
					player.sendMessage("You do not have a high enough rank to join this friends chat channel.", 2, true);
					return;
				}
				chat = new FriendChatsManager(owner);
				cachedFriendChats.put(ownerName, chat);
				chat.joinChatNoCheck(player);
			} else { chat.joinChat(player); }
		}
		
	}
	
	private static FriendChatsManager getChatInstance(String ownerName) {
		for (Entry<String, FriendChatsManager> entry : cachedFriendChats.entrySet()) {
			if (entry.getKey().equalsIgnoreCase(ownerName)) {
				return entry.getValue();
			}
		}
		return null;
	}

	/**
	 * Get's list of loot sharing people.
	 */
	public static List<Player> getLootSharingPeople(Player player) {
		if (!player.getFacade().isLootshareEnabled()) { return null; }
		FriendChatsManager chat = player.getCurrentFriendChat();
		if (chat == null) { return null; }
		List<Player> players = new ArrayList<>(player.getCurrentFriendChat().getPlayers());
		players = players.stream().filter(p2 -> p2.getFacade().isLootshareEnabled() && p2.withinDistance(player)).collect(Collectors.toList());
		return players;
	}

	/**
	 * Gets the clanWars.
	 *
	 * @return The clanWars.
	 */
	public ClanWars getClanWars() {
		return clanWars;
	}
	
	/**
	 * Sets the clanWars.
	 *
	 * @param clanWars
	 * 		The clanWars to set.
	 */
	public void setClanWars(ClanWars clanWars) {
		this.clanWars = clanWars;
	}
}
