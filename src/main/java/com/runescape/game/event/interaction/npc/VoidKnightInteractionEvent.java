package com.runescape.game.event.interaction.npc;

import com.runescape.game.content.global.minigames.pest.CommendationExchange;
import com.runescape.game.event.interaction.type.NPCInteractionEvent;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.world.ClickOption;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/21/2016
 */
public class VoidKnightInteractionEvent extends NPCInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] { 3786 };
	}

	@Override
	public boolean handleNPCInteraction(Player player, NPC npc, ClickOption option) {
		CommendationExchange.openExchangeShop(player);
		return true;
	}
}
