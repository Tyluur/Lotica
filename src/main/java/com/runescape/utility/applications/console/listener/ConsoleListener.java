package com.runescape.utility.applications.console.listener;

import com.runescape.utility.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 8/14/2015
 */
public class ConsoleListener implements Runnable {

	/** The list of all possible responses that can be requested */
	private final List<ConsoleResponse<?>> responses = new ArrayList<>();

	/** If the console listener is running */
	private boolean running = false;

	/** The instance of the console listener */
	private static final ConsoleListener INSTANCE = new ConsoleListener();

	/**
	 * Initializes the console listening
	 */
	public static void initializeConsoleListener() {
		new Thread(INSTANCE).start();
	}

	public ConsoleListener() {
		running = true;
		populateQueries();
		System.out.println("Populated " + responses.size() + " console responses.\nUse >>help for help using this system.");
	}

	/**
	 * Populates the {@link #responses} list with all the {@code ConsoleResponse}s in the response sub-package
	 */
	private void populateQueries() {
		try {
			responses.addAll(Utils.getClassesInDirectory(ConsoleListener.class.getPackage().getName() + ".responses").stream().map(clazz -> (ConsoleResponse<?>) clazz).collect(Collectors.toList()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (running) {
			if (System.console() == null) {
				return;
			}
			String text = System.console().readLine();
			if (text != null && text.startsWith(">>")) {
				String[] querySplit = text.split(">>");
				String[] data = querySplit[1].split(" ");

				String key = data[0].trim();
				ConsoleResponse<?> response = getResponse(key);
				if (response == null) {
					System.out.println("Could not find a ConsoleResponse.");
					System.out.println("Try again...");
				} else {
					response.onCall(Utils.getCompleted(data, 1));
				}
			}
		}
		System.out.println("Stopped console listener.");
	}

	/**
	 * This method gets a {@code ConsoleResponse} from the requested console input
	 *
	 * @param requested
	 * 		The requested console input
	 */
	private ConsoleResponse<?> getResponse(String requested) {
		for (ConsoleResponse<?> response : responses) {
			Object data = response.query();
			if (data instanceof String) {
				String key = (String) data;
				if (key.equalsIgnoreCase(requested)) {
					return response;
				}
			} else if (data instanceof String[]) {
				String[] keys = (String[]) data;
				for (String key : keys) {
					if (key.equalsIgnoreCase(requested)) {
						return response;
					}
				}
			} else {
				throw new IllegalStateException("Unexpected generic data type for ConsoleResponse:\t" + data);
			}
		}
		return null;
	}

	/**
	 * Gets the instance of this class
	 */
	public static ConsoleListener get() {
		return INSTANCE;
	}

	/**
	 * Gets a list of all console responses
	 */
	public List<ConsoleResponse<?>> getResponses() {
		return responses;
	}

}
