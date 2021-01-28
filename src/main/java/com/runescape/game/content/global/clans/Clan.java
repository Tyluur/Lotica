package com.runescape.game.content.global.clans;

import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.game.world.entity.player.Player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since Dec 12, 2013
 */
public class Clan implements Serializable {

	/**
	 * The {@code Clan} constructor, making a new clan.
	 *
	 * @param clanName
	 * 		The name of the clan
	 * @param leader
	 * 		The leader of the clan
	 */
	public Clan(String clanName, Player leader) {
		this.members = new ArrayList<ClanMember>();
		this.bannedUsers = new ArrayList<String>();
		setClanLeaderUsername(addMember(leader, LEADER));
		setClanName(clanName);
		setDefaults();
	}

	/**
	 * Setting the default variables of the clan
	 */
	public void setDefaults() {
		recruiting = true;
		guestsInChatCanEnter = true;
		guestsInChatCanTalk = true;
		worldId = 1;
		mottifColors = Arrays.copyOf(ItemDefinitions.forId(20709).originalModelColors, 4);
	}

	/**
	 * Loads all exterior variables
	 */
	public void load() {

	}

	public void setClanName(String clanName) {
		this.clanName = clanName;
	}

	/**
	 * Adds a clan member to the clan
	 *
	 * @param player
	 * 		The player to add to the clan
	 * @param rank
	 * 		The rank to make the player
	 * @return The {@code ClanMember} {@code Object} which is added to the clan
	 */
	public ClanMember addMember(Player player, int rank) {
		ClanMember member = new ClanMember(player.getUsername(), rank);
		members.add(member);
		return member;
	}

	public void setClanLeaderUsername(ClanMember member) {
		clanLeaderUsername = member.getUsername();
	}

	public int getMemberId(ClanMember member) {
		return members.indexOf(member);
	}

	public List<ClanMember> getMembers() {
		return members;
	}

	public List<String> getBannedUsers() {
		return bannedUsers;
	}

	public String getClanName() {
		return clanName;
	}

	public int getTimeZone() {
		return timeZone;
	}

	public boolean isRecruiting() {
		return recruiting;
	}

	public void switchRecruiting() {
		recruiting = !recruiting;
	}

	public void setTimeZone(int gameTime) {
		this.timeZone = gameTime;
	}

	public int getMinimumRankForKick() {
		return minimumRankForKick;
	}

	public void setMinimumRankForKick(int minimumRankForKick) {
		this.minimumRankForKick = minimumRankForKick;
	}

	public String getThreadId() {
		return threadId;
	}

	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}

	public boolean isGuestsInChatCanEnter() {
		return guestsInChatCanEnter;
	}

	public void switchGuestsInChatCanEnter() {
		this.guestsInChatCanEnter = !guestsInChatCanEnter;
	}

	public String getMotto() {
		return motto;
	}

	public void setMotto(String motto) {
		this.motto = motto;
	}

	public boolean isClanTime() {
		return isClanTime;
	}

	public void switchClanTime() {
		isClanTime = !isClanTime;
	}

	public int getWorldId() {
		return worldId;
	}

	public void setWorldId(int worldId) {
		this.worldId = worldId;
	}

	public int getClanFlag() {
		return clanFlag;
	}

	public void setClanFlag(int clanFlag) {
		this.clanFlag = clanFlag;
	}

	public String getClanLeaderUsername() {
		return clanLeaderUsername;
	}

	public boolean isGuestsInChatCanTalk() {
		return guestsInChatCanTalk;
	}

	public int getMottifTop() {
		return mottifTop;
	}

	public void setMottifTop(int mottifTop) {
		this.mottifTop = mottifTop;
	}

	public int getMottifBottom() {
		return mottifBottom;
	}

	public void setMottifBottom(int mottifBottom) {
		this.mottifBottom = mottifBottom;
	}

	public int[] getMottifColors() {
		return mottifColors;
	}

	public void setMottifColours(int[] mottifColors) {
		this.mottifColors = mottifColors;
	}

	public void switchGuestsInChatCanTalk() {
		guestsInChatCanTalk = !guestsInChatCanTalk;
	}

	/**
	 * The name of the clan leader
	 */
	private String clanLeaderUsername;

	/**
	 * The list of clan members
	 */
	private List<ClanMember> members;

	/**
	 * The list of banned users
	 */
	private List<String> bannedUsers;

	/**
	 * The timezone of the clan
	 */
	private int timeZone;

	/**
	 * If the clan is recruiting or not
	 */
	private boolean recruiting;

	/**
	 * If we are using clan time.
	 */
	private boolean isClanTime;

	/**
	 * The world that is active
	 */
	private int worldId;

	/**
	 * The clan flag
	 */
	private int clanFlag;

	/**
	 * If guests can enter the chat
	 */
	private boolean guestsInChatCanEnter;

	/**
	 * If guests can talk in the chat
	 */
	private boolean guestsInChatCanTalk;

	/**
	 * The the threadId of the chat
	 */
	private String threadId;

	/**
	 * The motto of the chat
	 */
	private String motto;

	/**
	 * The mottif numbers
	 */
	private int mottifTop, mottifBottom;

	/**
	 * The array of the colors for the mottif
	 */
	private int[] mottifColors;

	/**
	 * The minimum rank that can be kicked
	 */
	private int minimumRankForKick;

	/**
	 * The name of the clan
	 */
	private String clanName;

	/**
	 * The recruit rank number
	 */
	public static final int RECRUIT = 0;

	/**
	 * The admin rank number
	 */
	public static final int ADMIN = 100;

	/**
	 * The deputy owner rank number
	 */
	public static final int DEPUTY_OWNER = 125;

	/**
	 * The leader rank number.
	 */
	public static final int LEADER = 126;

	/**
	 * The maximum amount of members in a clan.
	 */
	public static final int MAX_MEMBERS = 500;

	/**
	 *
	 */
	private static final long serialVersionUID = -3022814330682606425L;


}
