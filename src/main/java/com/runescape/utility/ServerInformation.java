package com.runescape.utility;

import com.runescape.Main;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since Oct 12, 2013
 */
public class ServerInformation {

	/**
	 * Gets the uptime of the server in a formatted string in the following format: days, hours, minutes, seconds. This
	 * will also ensure the numbers are all two-digital for prettier formatting
	 */
	public static String getGameUptime() {
		long milliseconds = System.currentTimeMillis() - Main.get().getStartTime();
		long days = (milliseconds / 86400000L);
		long hours = ((milliseconds / 3600000L) % 24L);
		long minutes = ((milliseconds / 60000L) % 60L);
		//long seconds = ((milliseconds / 1000L) % 60L);
		String string = "";
		if (days > 0) {
			String s = days == 1 ? " day " : " days ";
			string += days + s;
		}
		if (hours > 0) {
			String s = hours == 1 ? " hour " : " hours ";
			string += hours + s;
		}
		if (minutes > 0) {
			String s = minutes == 1 ? " min " : " mins ";
			string += minutes + s;
		}
	/*	if (seconds > 0) {
			String s = seconds == 1 ? " sec " : " secs ";
			string += seconds + s;
		}*/
		if (string.equals("")) {
			string = "1 minute";
		}
		return string;
	}

}
