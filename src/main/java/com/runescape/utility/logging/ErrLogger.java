package com.runescape.utility.logging;

import com.runescape.game.GameConstants;

import java.io.*;
import java.text.DateFormatSymbols;
import java.util.Date;

/**
 *
 * @author Tyluur <itstyluur@gmail.com>
 * @since Dec 14, 2013
 */
public class ErrLogger extends PrintStream {

	public ErrLogger(OutputStream out) {
		super(out);
	}

	@Override
	public void println(Object x) {
		logError(x);
	}

	@Override
	public void println(String x) {
		writeDropboxLog("errors/", x, true);
		super.println(x);
	}

	private void logError(Object object) {
		String text = object.toString();
		writeDropboxLog("errors/", text, true);
		super.println(object);
	}
	
	@SuppressWarnings("deprecation")
	public static void writeDropboxLog(String place, String text, boolean append) {
		if (GameConstants.DEBUG) {
			return;
		}
		place = place + "" + DateFormatSymbols.getInstance().getMonths()[new Date().getMonth()] + "/" + new Date().getDate() + ".txt";
		File writeFile = new File("data/logs/" + place);
		if (!writeFile.getParentFile().exists()) {
			writeFile.getParentFile().mkdirs();
		}
		try {
			BufferedWriter fileWriter = new BufferedWriter(new FileWriter("data/logs/" + place, append));
			fileWriter.write("[" + new Date().toLocaleString() + "] " + text.toString());
			fileWriter.newLine();
			fileWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
