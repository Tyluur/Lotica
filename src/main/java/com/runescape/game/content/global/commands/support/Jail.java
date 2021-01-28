package com.runescape.game.content.global.commands.support;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.event.InputEvent;
import com.runescape.game.event.InputEvent.InputEventType;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Player;

import java.util.concurrent.TimeUnit;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/4/2015
 */
public class Jail extends CommandSkeleton<String> {
	
	@Override
	public String getIdentifiers() {
		return "jail";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		String name = getCompleted(cmd, 1).replaceAll("_", " ");
		final Player target = World.getPlayerByDisplayName(name);
		if (target == null) {
			player.sendMessage("No such player by the name: " + name);
			return;
		}
		player.getPackets().requestClientInput(new InputEvent("How many hours should they be jailed for?", InputEventType.INTEGER) {
			@Override
			public void handleInput() {
				Integer input = getInput();
				target.getFacade().setJailedUntil(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(input));
				target.getControllerManager().startController("JailController");
			}
		});
	}
}
