package com.runescape.game.content.global.commands.player;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.event.interaction.button.Scrollable;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.rights.Right;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 8/10/2016
 */
public class MyRanks extends CommandSkeleton<String[]> {

	@Override
	public String[] getIdentifiers() {
		return new String[] { "myranks" };
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		List<String> lines = new ArrayList<>();
		List<Right> localList = new ArrayList<>(player.getRights());
		Collections.sort(localList, (o1, o2) -> Integer.compare(o1.getIndex(), o2.getIndex()));
		lines.addAll(localList.stream().map(right -> "<img=" + right.getCrownIcon() + "><col=" + right.getChatColour() + ">" + right.getProperName() + "").collect(Collectors.toList()));
		Scrollable.sendQuestScroll(player, "My Ranks", lines.toArray(new String[lines.size()]));
	}
}
