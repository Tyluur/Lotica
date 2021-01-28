package com.runescape.utility.applications.console.listener.responses;

import com.runescape.utility.applications.console.listener.ConsoleResponse;
import com.runescape.workers.game.core.CoresManager;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 5/1/2016
 */
public class SaveAll implements ConsoleResponse<String[]> {

	@Override
	public String[] query() {
		return new String[] { "save"};
	}

	@Override
	public void onCall(String text) {
		CoresManager.SERVICE.submit(() -> {
			CoresManager.saveFiles(false);
			System.out.println("all saved");
		});
	}
}
