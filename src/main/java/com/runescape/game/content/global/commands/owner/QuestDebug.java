package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.quests.impl.RecipeForDisaster;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 18, 2015
 */
public class QuestDebug extends CommandSkeleton<String[]> {

	@Override
	public String[] getIdentifiers() {
		return new String[] { "qdb" };
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		player.getQuestManager().startQuest(RecipeForDisaster.class);
		System.out.println(player.getQuestManager().isFinished(RecipeForDisaster.class));
	}

}
