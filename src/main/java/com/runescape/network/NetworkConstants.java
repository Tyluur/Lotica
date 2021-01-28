package com.runescape.network;

import com.google.common.collect.ImmutableList;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 5/4/2016
 */
public class NetworkConstants {

	/**
	 * The list of exceptions that are ignored and discarded
	 */
	public static final ImmutableList<String> IGNORED_EXCEPTIONS = ImmutableList.of("An existing connection was forcibly closed by the remote host", "An established connection was aborted by the software in your host machine", "ClosedChannelException");

}
