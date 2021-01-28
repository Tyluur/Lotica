package com.runescape.utility.applications.console.listener;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 8/14/2015
 */
public interface ConsoleResponse<K> {

	/**
	 * The query the user must enter into the console
	 */
	K query();

	/**
	 * What happens when the query is called
	 *
	 * @param text
	 * 		The text that was sent with the query
	 */
	void onCall(String text);

}
