package com.runescape.game.content.global.commands;

import com.runescape.game.GameConstants;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.rights.Right;
import com.runescape.game.world.entity.player.rights.RightManager;
import com.runescape.utility.Utils;
import com.runescape.workers.game.core.CoresManager;
import com.runescape.workers.game.log.GameLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 18, 2015
 */
public class CommandHandler {

	/**
	 * Initializes all commands into the {@link #commands} list
	 */
	public static void initialize() {
		commands.clear();
		for (String directory : Utils.getSubDirectories(CommandHandler.class)) {
			Optional<Right> right = RightManager.getRight(directory);
			if (!right.isPresent()) {
				System.err.println("No right found for directory " + directory);
				continue;
			}
			Utils.getClassesInDirectory(CommandHandler.class.getPackage().getName() + "." + directory).forEach(clazz -> {
				if (clazz instanceof CommandSkeleton) {
					CommandSkeleton<?> command = (CommandSkeleton<?>) clazz;
					command.setPrimaryRightRequired(right.get());
					commands.add(command);
				} else {
					throw new IllegalStateException(clazz + " should not have been in this directory!");
				}
			});
		}
		System.out.println(commands.size() + " commands loaded.");
	}

	/**
	 * Handles an incoming command
	 *
	 * @param player
	 * 		The player
	 * @param unformattedCommand
	 * 		The command
	 * @param clientCommand
	 * 		If the command was a client command
	 */
	public static boolean handleIncomingCommand(Player player, String unformattedCommand, boolean clientCommand) {
		try {
			unformattedCommand = unformattedCommand.replaceAll("::", "").replaceAll(";;", "").trim();
			String[] cmd = unformattedCommand.split(" ");
			String commandName = cmd[0];
			List<CommandSkeleton<?>> commandResults = getCommandByName(commandName);
			CommandSkeleton<?> command = getBestCommandAvailable(player, commandResults);
			if (command == null) {
				player.getPackets().sendGameMessage("No such command '" + commandName + "' - please try again.", false);
				return false;
			}
			if (!command.getPrimaryRightRequired().isAvailableFor(player)) {
				if (GameConstants.DEBUG) {
					player.getPackets().sendGameMessage("Invalid rights: " + command.getClass() + " " + command.getPrimaryRightRequired(), false);
				}
				return false;
			}
			if (command.consoleCommand() && !clientCommand) {
				return false;
			}
			command.handleCommand(player, cmd);
			CoresManager.LOG_PROCESSOR.appendLog(new GameLog("command", player.getUsername(), "Used command:\t" + unformattedCommand));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Filters through the commands we've found thus far and finds the best command, based on rights
	 *
	 * @param player
	 * 		The player attempting to use the command
	 * @param commands
	 * 		The list of commands found thus far (based solely on name)
	 */
	private static CommandSkeleton<?> getBestCommandAvailable(Player player, List<CommandSkeleton<?>> commands) {
		List<CommandSkeleton<?>> filteredList = commands.stream().filter(p -> p.getPrimaryRightRequired().isAvailableFor(player)).collect(Collectors.toList());
		if (filteredList.size() > 0) {
			return filteredList.get(0);
		}
		return null;
	}

	/**
	 * Creates a list of all the commands available by the requested name
	 *
	 * @param commandName
	 * 		The name of the command we're looking for
	 */
	private static List<CommandSkeleton<?>> getCommandByName(String commandName) {
		List<CommandSkeleton<?>> results = new ArrayList<>();
		for (CommandSkeleton<?> command : commands) {
			Object identifiers = command.getIdentifiers();
			if (identifiers instanceof String[]) {
				String[] keys = (String[]) identifiers;
				for (String key : keys) {
					if (key.equalsIgnoreCase(commandName)) {
						results.add(command);
					}
				}
			} else if (identifiers instanceof String) {
				String key = (String) identifiers;
				if (key.equalsIgnoreCase(commandName)) {
					results.add(command);
				}
			} else {
				throw new IllegalStateException();
			}
		}
		Collections.sort(results, (o1, o2) -> o1.getPrimaryRightRequired().compareTo(o2.getPrimaryRightRequired()));
		return results;
	}

	/**
	 * @return the commands
	 */
	public static List<CommandSkeleton<?>> getCommands() {
		return commands;
	}

	/**
	 * The list of commands
	 */
	private static final List<CommandSkeleton<?>> commands = new ArrayList<>();

}
