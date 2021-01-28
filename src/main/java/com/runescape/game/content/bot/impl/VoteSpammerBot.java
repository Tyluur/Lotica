package com.runescape.game.content.bot.impl;

import com.runescape.game.content.bot.AbstractBot;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.player.PublicChatMessage;
import com.runescape.utility.Utils;

import java.util.concurrent.TimeUnit;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 1/18/2016
 */
public class VoteSpammerBot extends AbstractBot {

	@Override
	public String[] botNames() {
		return new String[] { "ZR-Reminder" };
	}

	@Override
	public Integer getBotAmounts() {
		return 1;
	}

	@Override
	public WorldTile[] hoverTiles() {
		return new WorldTile[] { new WorldTile(3087, 3497, 0) };
	}

	@Override
	public long activityTime() {
		return TimeUnit.MINUTES.toMillis(5);
	}

	@Override
	public void onPulse() {
		Integer[] effects = { 258, 512, 0, 515, 2, 1, 4 };
		String[] messages = { "Want cool cosmetics or exclusive rares? ::vote for Lotica for such.", "Help support Lotica and receive great rewards when you ::donate!" };

		int effect = Utils.randomArraySlot(effects);
		String message = Utils.randomArraySlot(messages);

		bot.sendPublicChatMessage(bot.setLastChatMessage(new PublicChatMessage(message, effect)));
		bot.setLastMsg(message);
	}

}
