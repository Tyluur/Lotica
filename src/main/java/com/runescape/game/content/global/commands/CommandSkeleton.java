package com.runescape.game.content.global.commands;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.rights.Right;
import com.runescape.game.world.entity.player.rights.RightManager;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 18, 2015
 */
public abstract class CommandSkeleton<K> {

	/**
	 * The rights for easy access
	 */
	protected static final Right ALL = RightManager.PLAYER, SUPPORT = RightManager.SUPPORT, MOD = RightManager.SERVER_MODERATOR, ADMIN = RightManager.ADMINISTRATOR, OWNER = RightManager.OWNER;

	/**
	 * The identifiers of this command
	 */
	public abstract K getIdentifiers();

	/**
	 * The required rights to use this command
	 */
	private Right primaryRightRequired;

	/**
	 * Handles the command
	 *
	 * @param player
	 * 		The player
	 * @param cmd
	 * 		The command, split by a space
	 */
	public abstract void handleCommand(Player player, String[] cmd);

	/**
	 * If this command can only be ran from the console
	 */
	public boolean consoleCommand() {
		return false;
	}

	/**
	 * If the command is shown over the ::commands interface
	 */
	public boolean shownOnInterface() { return true; }

	/**
	 * Gets a completed version of a string array
	 *
	 * @param array
	 * 		The array
	 * @param index
	 * 		The index to start at
	 */
	protected String getCompleted(String[] array, int index) {
		StringBuilder sb = new StringBuilder();
		for (int i = index; i < array.length; i++) {
			if (i == array.length - 1 || array[i + 1].startsWith("+")) {
				return sb.append(array[i]).toString();
			}
			sb.append(array[i]).append(" ");
		}
		return "null";
	}

    public Right getPrimaryRightRequired() {
        return this.primaryRightRequired;
    }

    public void setPrimaryRightRequired(Right primaryRightRequired) {
        this.primaryRightRequired = primaryRightRequired;
    }
}
