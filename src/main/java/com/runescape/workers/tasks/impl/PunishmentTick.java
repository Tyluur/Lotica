package com.runescape.workers.tasks.impl;

import com.runescape.utility.external.gson.GsonStartup;
import com.runescape.utility.external.gson.loaders.PunishmentLoader;
import com.runescape.utility.external.gson.resource.Punishment;
import com.runescape.workers.tasks.WorldTask;

import java.util.Iterator;
import java.util.List;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 17, 2015
 */
public class PunishmentTick extends WorldTask {

	@Override
	public void run() {
		synchronized (PunishmentLoader.LOCK) {
			GsonStartup.getOptional(PunishmentLoader.class).ifPresent(c -> {
				List<Punishment> punishments = c.getPunishments();
				boolean needsSave = false;
				for (Iterator<Punishment> it$ = punishments.iterator(); it$.hasNext();) {
					Punishment punishment = it$.next();
					if (punishment.hasExpired()) {
						System.out.println("Punishment expired: " + punishment);
						punishment.executeDeregistrationEvent();
						it$.remove();
						needsSave = true;
					}
				}
				if (needsSave) {
					c.save(punishments);
					c.initialize();
				}
			});
		}
	}

}
