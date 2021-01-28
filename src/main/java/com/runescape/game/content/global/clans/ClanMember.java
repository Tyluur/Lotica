package com.runescape.game.content.global.clans;

import com.runescape.utility.Utils;

import java.io.Serializable;

/**
 * 
 * @author Tyluur <itstyluur@gmail.com>
 * @since Dec 12, 2013
 */
public class ClanMember implements Serializable {

	/**
	 * Creates a new {@code ClanMember} {@code Object}
	 * @param username The name of the clan member
	 * @param rank The rank of the clan member
	 */
	public ClanMember(String username, int rank) {
		this.username = username;
		this.rank = rank;
		joinDate = Utils.currentTimeMillis();
	}

	/**
	 * Getting the rank of the user
	 * @return
	 */
	public int getRank() {
		return rank;
	}

	/**
	 * Getting the name of the user
	 * @return
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Getting the job of the user
	 * @return
	 */
	public int getJob() {
		return job;
	}

	public void setJob(int job) {
		this.job = job;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public boolean isBanFromCitadel() {
		return banFromCitadel;
	}

	public void setBanFromCitadel(boolean banFromCitadel) {
		this.banFromCitadel = banFromCitadel;
	}

	public boolean isBanFromKeep() {
		return banFromKeep;
	}

	public void setBanFromKeep(boolean banFromKeep) {
		this.banFromKeep = banFromKeep;
	}

	public boolean isBanFromIsland() {
		return banFromIsland;
	}

	public void setBanFromIsland(boolean banFromIsland) {
		this.banFromIsland = banFromIsland;
	}

	/**
	 * If the user joined in the first week
	 * @return
	 */
	public boolean firstWeek() {
		return Utils.currentTimeMillis() - joinDate < 7 * 24 * 60 * 60 * 1000;
	}

	/**
	 * The name of the user in the clan.
	 */
	private String username;
	
	/**
	 * The rank of the user in the clan
	 */
	private int rank;
	
	/**
	 * The job of the user in the clan
	 */
	private int job;
	
	/**
	 * If the user is banned from our citadel
	 */
	private boolean banFromCitadel;
	
	/**
	 * If the user is banned from our island
	 */
	private boolean banFromIsland;
	
	/**
	 * If the user is banned from our keep
	 */
	private boolean banFromKeep;
	
	/**
	 * The date the user joined
	 */
	private long joinDate;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8637398992882314328L;
}
