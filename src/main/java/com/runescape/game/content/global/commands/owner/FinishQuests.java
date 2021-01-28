package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.QuestManager;
import com.runescape.game.world.entity.player.quests.Quest;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/6/2015
 */
public class FinishQuests extends CommandSkeleton<String> {
	
	@Override
	public String getIdentifiers() {
		return "fq";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		for (Quest<?> quest : QuestManager.getQuests()) {
			player.getQuestManager().startQuest(quest.getClass());
			player.getQuestManager().forceFinish(quest.getClass());
		}
	}
}
