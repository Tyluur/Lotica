package com.runescape.utility.external.gson.resource;

import com.runescape.utility.Utils;

import java.util.Objects;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 17, 2015
 */
public class Punishment {

	public Punishment(String key, PunishmentType punishmentType, long completionTime) {
		Objects.requireNonNull(key);
		this.key = key;
		this.punishmentType = punishmentType;
		this.registrationTime = System.currentTimeMillis();
		this.completionTime = completionTime;
	}

	/**
	 * On registration, {@link PunishmentType#BAN} punishments must be handled
	 * on the forum as well.
	 */
	public void executeRegistrationEvent() {
		switch (punishmentType) {
		case BAN:
			// TODO: add ban to forum
			break;
		default:
			break;
		}
	}

	/**
	 * On deregistration, {@link PunishmentType#BAN} punishments must be handled
	 * on the forum as well.
	 */
	public void executeDeregistrationEvent() {
		switch (punishmentType) {
		case BAN:
			// TODO: remove ban from forum
			break;
		default:
			break;
		}
	}

	/**
	 * If the duration for the punishment has expired
	 * 
	 * @return
	 */
	public boolean hasExpired() {
		return System.currentTimeMillis() > getCompletionTime();
	}

	/**
	 * @return the punishmentType
	 */
	public PunishmentType getPunishmentType() {
		return punishmentType;
	}

	/**
	 * @return the registrationTime
	 */
	public long getRegistrationTime() {
		return registrationTime;
	}

	/**
	 * @return the completionTime
	 */
	public long getCompletionTime() {
		return completionTime;
	}

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @return
	 */
	public String getTimeLeft() {
		long millis = completionTime - System.currentTimeMillis();
		return Utils.getDurationBreakdown(millis);
	}

	@Override
	public String toString() {
		return "key=" + key + ", type=" + punishmentType + ", completionTime=" + completionTime;
	}

	/**
	 * The key of the punishment
	 */
	private final String key;

	/**
	 * The type of punishment this is
	 */
	private final PunishmentType punishmentType;

	/**
	 * The time this punishment was registered at
	 */
	private final long registrationTime;

	/**
	 * The time this punishment will be completed at
	 */
	private final long completionTime;

	public enum PunishmentType {
		MUTE, BAN, MACMUTE, MACBAN
	}

}