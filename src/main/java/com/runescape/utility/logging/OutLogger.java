package com.runescape.utility.logging;

import com.runescape.workers.game.log.SystemOutLogger;

import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class modifies previous {@link System#out} logging and prints them with
 * information. We need to know which class printed data and at what time at all
 * times.
 * 
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 9, 2015
 */
public class OutLogger extends PrintStream {

	public OutLogger(OutputStream out) {
		super(out);
	}

	@Override
	public void print(boolean message) {
		Throwable throwable = new Throwable();
		if (throwable.getStackTrace() == null || throwable.getStackTrace()[2] == null || throwable.getStackTrace()[2].getFileName() == null) { 
			return;
		}
		String name = throwable.getStackTrace()[2].getFileName().replaceAll(".java", "");
		String line = String.valueOf(throwable.getStackTrace()[2].getLineNumber());
		log(name + ":" + line, "" + message);
	}

	@Override
	public void print(int message) {
		Throwable throwable = new Throwable();
		if (throwable.getStackTrace() == null || throwable.getStackTrace()[2] == null || throwable.getStackTrace()[2].getFileName() == null) { 
			return;
		}
		String name = throwable.getStackTrace()[2].getFileName().replaceAll(".java", "");
		String line = String.valueOf(throwable.getStackTrace()[2].getLineNumber());
		log(name + ":" + line, "" + message);
	}

	@Override
	public void print(double message) {
		Throwable throwable = new Throwable();
		String name = throwable.getStackTrace()[2].getFileName().replaceAll(".java", "");
		String line = String.valueOf(throwable.getStackTrace()[2].getLineNumber());
		log(name + ":" + line, "" + message);
	}

	@Override
	public void print(long message) {
		Throwable throwable = new Throwable();
		if (throwable.getStackTrace() == null || throwable.getStackTrace()[2] == null || throwable.getStackTrace()[2].getFileName() == null) { 
			return;
		}
		String name = throwable.getStackTrace()[2].getFileName().replaceAll(".java", "");
		String line = String.valueOf(throwable.getStackTrace()[2].getLineNumber());
		log(name + ":" + line, "" + message);
	}

	@Override
	public void print(String message) {
		Throwable throwable = new Throwable();
		if (throwable.getStackTrace() == null || throwable.getStackTrace()[2] == null || throwable.getStackTrace()[2].getFileName() == null) {
			return;
		}
		String name = throwable.getStackTrace()[2].getFileName().replaceAll(".java", "");
		String line = String.valueOf(throwable.getStackTrace()[2].getLineNumber());
		log(name + ":" + line, "" + message);
	}

	@Override
	public void print(Object message) {
		Throwable throwable = new Throwable();
		if (throwable.getStackTrace() == null || throwable.getStackTrace()[2] == null || throwable.getStackTrace()[2].getFileName() == null) { 
			return;
		}
		String name = throwable.getStackTrace()[2].getFileName().replaceAll(".java", "");
		String line = String.valueOf(throwable.getStackTrace()[2].getLineNumber());
		log(name + ":" + line, "" + message);
	}

	/**
	 * Logging information to the super class after we have formatted it
	 * 
	 * @param className
	 *            The name of the class the data is from
	 * @param text
	 *            The text that was originally printed
	 */
	private void log(String className, String text) {
		String message = "[" + className + "][" + getFormattedDate() + "]" + text;

		super.print(message);
		SystemOutLogger.addLogToQueue(message);
	}

	/**
	 * Gets the date in a formatted string.
	 * 
	 * @return The date
	 */
	private String getFormattedDate() {
		return new SimpleDateFormat("MM/dd/yyyy hh:mm:ss").format(new Date());
	}

}
