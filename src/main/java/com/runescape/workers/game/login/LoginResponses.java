package com.runescape.workers.game.login;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 6/25/2015
 */
public enum LoginResponses {

	/**
	 * Runescape defaults
	 */
	INVALID_CREDENTIALS(3),

	/** Modified ones */
	REGISTERED_NOTIFICATION(14),
	NULLED_ACCOUNT(20),
	INVALID_USERNAME(27),
	NOT_LEGIBLE_TO_LOGIN(32),
	DATABASE_CONNECTION_ERROR(35),;

	LoginResponses(int opcode) {
		this.opcode = opcode;
	}

	/**
	 * Gets the opcode
	 */
	public int getOpcode() {
		return opcode;
	}

	/**
	 * The opcode of the login
	 */
	private final int opcode;
}
