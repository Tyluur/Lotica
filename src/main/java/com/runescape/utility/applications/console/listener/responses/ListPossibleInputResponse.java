package com.runescape.utility.applications.console.listener.responses;

import com.runescape.utility.applications.console.listener.ConsoleListener;
import com.runescape.utility.applications.console.listener.ConsoleResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 8/14/2015
 */
public class ListPossibleInputResponse implements ConsoleResponse<String> {

	@Override
	public String query() {
		return "help";
	}

	@Override
	public void onCall(String text) {
		System.out.println("This is the console response system, all console commands must start with >>.");
		List<String> responses = new ArrayList<>();
		for (ConsoleResponse<?> response : ConsoleListener.get().getResponses()) {
			Object data = response.query();
			if (data instanceof String) {
				responses.add(">>" + data);
			} else if (data instanceof String[]) {
				String[] keys = (String[]) data;
				for (String key : keys) {
					responses.add(">>" + key);
				}
			}
		}
		System.out.println("All possible Console Responses begin as follows:");
		for (String response : responses) {
			System.out.println("\t\t" + response);
		}
	}
}
