package com.runescape.game.event.interaction.npc;

import com.runescape.game.event.interaction.type.NPCInteractionEvent;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.external.gson.GsonStartup;
import com.runescape.utility.external.gson.loaders.StoreLoader;
import com.runescape.utility.world.ClickOption;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Jun 17, 2015
 */
public class JossikInteractionEvent extends NPCInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] { 1334 };
	}

	@Override
	public boolean handleNPCInteraction(Player player, NPC npc, ClickOption option) {
		if (option == ClickOption.SECOND) {
			GsonStartup.getClass(StoreLoader.class).openStore(player, "Jossik's Book Shop");
			return true;
		}
		return false;
	}

}
