package com.runescape.workers.tasks.impl;

import com.runescape.workers.db.mysql.impl.DatabaseFunctions;
import com.runescape.workers.tasks.WorldTask;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 9/3/2016
 */
public class WebsiteUpdateTick extends WorldTask {

	@Override
	public void run() {
		try {
			DatabaseFunctions.updateWebsiteDetails();
			System.out.println("Updated website details!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
