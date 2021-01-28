package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.event.interaction.button.Scrollable;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.rights.RightManager;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 1/15/2016
 */
public class RightsDisplayer extends CommandSkeleton<String> {

	@Override
	public String getIdentifiers() {
		return "showrights";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		List<String> text = RightManager.getRightList().stream().map(rights -> "<img=" + rights.getCrownIcon() + "><col=" + rights.getChatColour() + ">" + rights).collect(Collectors.toList());
		Scrollable.sendQuestScroll(player, "Rights", text.toArray(new String[text.size()]));
	}
}
