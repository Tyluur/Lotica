package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.event.interaction.button.Scrollable;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 1/8/2016
 */
public class DumpSessionInformation extends CommandSkeleton<String> {

	@Override
	public String getIdentifiers() {
		return "sessions";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		List<String> results = new ArrayList<>();
		for (Player pl : World.getPlayers()) {
			if (pl == null || pl.getSession() == null || pl.getSession().getChannel() == null) {
				continue;
			}
			results.add(pl.getDisplayName() + ": open=" + pl.getSession().getChannel().isOpen() + ",connected=" + pl.getSession().getChannel().isOpen() + ",bound=" + pl.getSession().getChannel().isBound());
		}
		Scrollable.sendQuestScroll(player, "Session Information", results.toArray(new String[results.size()]));
	}
}
