package com.runescape.workers.db;

public interface DatabaseConfiguration {

	/**
	 * Create a new database connection
	 *
	 * @return The new connection
	 */
	DatabaseConnection newConnection();

}