package com.runescape.utility.external.gson.loaders;

import com.google.gson.reflect.TypeToken;
import com.runescape.game.GameConstants;
import com.runescape.utility.Utils;
import com.runescape.utility.external.gson.GsonCollections;
import com.runescape.utility.external.gson.resource.Punishment;
import com.runescape.utility.external.gson.resource.Punishment.PunishmentType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 17, 2015
 */
public class PunishmentLoader extends GsonCollections<Punishment> {

	@Override
	public void initialize() {
		punishments.clear();
		List<Punishment> loadedPunishments = generateList();
		punishments.addAll(loadedPunishments);
	}

	@Override
	public List<Punishment> loadList() {
		List<Punishment> data = gson.fromJson(Utils.getText(getFileLocation()), new TypeToken<List<Punishment>>() {
		}.getType());
		return data;
	}

	@Override
	public String getFileLocation() {
		return GameConstants.FILES_PATH + "punishments.json";
	}

	/**
	 * This method adds a punishment to the list of punishments
	 *
	 * @param key
	 * 		The key
	 * @param type
	 * 		The type of punishment
	 * @param duration
	 * 		The duration of the punishment
	 * @return True if the punishment was added successfully
	 */
	public boolean addPunishment(String key, PunishmentType type, long duration) {
		synchronized (LOCK) {
			Punishment punishment = new Punishment(key, type, System.currentTimeMillis() + duration);
			boolean added = punishments.add(punishment);
			punishment.executeRegistrationEvent();
			save(punishments);
			initialize();
			return added;
		}
	}

	/**
	 * Removes the punishment from the list and executes the deregistration event for the punishment
	 *
	 * @param key
	 * 		The key of the punishment
	 * @param type
	 * 		The type of punishment
	 */
	public boolean removePunishment(String key, PunishmentType type) {
		synchronized (LOCK) {
			boolean needsUpdate = false;
			for (Iterator<Punishment> it$ = punishments.iterator(); it$.hasNext(); ) {
				Punishment punishment = it$.next();
				if (punishment.getPunishmentType() != type) {
					continue;
				}
				if (punishment.getKey().equalsIgnoreCase(key)) {
					punishment.executeDeregistrationEvent();
					it$.remove();
					needsUpdate = true;
				}
			}
			if (needsUpdate) {
				save(punishments);
				initialize();
			}
			return needsUpdate;
		}
	}

	/**
	 * If a punishment exists for the key
	 *
	 * @param key
	 * 		The punishment's key
	 * @param type
	 * 		The type of punishment
	 */
	public Punishment isPunished(String key, PunishmentType type) {
		synchronized (LOCK) {
			for (Punishment punishment : punishments) {
				if (punishment.getPunishmentType() != type) {
					continue;
				}
				if (punishment.getKey().equalsIgnoreCase(key)) {
					return punishment;
				}
			}
			return null;
		}
	}

	/**
	 * This method is used to check for an array of punishments to be true
	 *
	 * @param params
	 * 		The array of punishment details that could be true
	 */
	public Punishment isPunished(Object[][] params) {
		for (Object[] parameters : params) {
			Punishment punishment = isPunished((String) parameters[0], (PunishmentType) parameters[1]);
			if (punishment != null) {
				return punishment;
			}
		}
		return null;
	}

	/**
	 * @return the punishments
	 */
	public List<Punishment> getPunishments() {
		return punishments;
	}

	/**
	 * The object all punishment modifications are synchronized through
	 */
	public static final Object LOCK = new Object();

	/**
	 * The list of punishments
	 */
	private final List<Punishment> punishments = Collections.synchronizedList(new ArrayList<>());

}
