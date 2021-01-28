package com.runescape.game.content.global.commands.player;

import com.runescape.game.content.global.commands.CommandHandler;
import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.event.interaction.button.Scrollable;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.ChatColors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Jun 17, 2015
 */
public class Commands extends CommandSkeleton<String[]> {

	@Override
	public String[] getIdentifiers() {
		return new String[] { "cmds", "commands" };
	}

	@Override
	public boolean shownOnInterface() {
		return false;
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		List<CommandSkeleton<?>> commands = new ArrayList<>(CommandHandler.getCommands());
		Collections.sort(commands, (o1, o2) -> o1.getPrimaryRightRequired().compareTo(o2.getPrimaryRightRequired()));
		List<String> commandsAccessible = new ArrayList<>();
		for (CommandSkeleton<?> command : commands) {
			if (!command.shownOnInterface())
				continue;
			if (command.getPrimaryRightRequired().isAvailableFor(player)) {
				StringBuilder bldr = new StringBuilder();
				if (command.getIdentifiers() instanceof String[]) {
					String[] keys = (String[]) command.getIdentifiers();
					for (int index = 0; index < keys.length; index++) {
						String key = keys[index];
						bldr.append(key).append("").append(index == keys.length - 1 ? "" : ", ");
					}
				} else if (command.getIdentifiers() instanceof String) {
					bldr.append((String) command.getIdentifiers());
				}
				commandsAccessible.add("[<col=" + command.getPrimaryRightRequired().getChatColour() + ">" + command.getPrimaryRightRequired().getName() + "</col>] ::<col=" + ChatColors.BLUE + ">" + bldr.toString());
			}
		}
		Scrollable.sendQuestScroll(player, "Commands", commandsAccessible.toArray(new String[commandsAccessible.size()]));
	}

}
