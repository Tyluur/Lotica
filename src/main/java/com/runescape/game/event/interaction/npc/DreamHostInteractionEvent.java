package com.runescape.game.event.interaction.npc;

import com.runescape.game.event.interaction.type.NPCInteractionEvent;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.external.gson.GsonStartup;
import com.runescape.utility.external.gson.loaders.StoreLoader;
import com.runescape.utility.world.ClickOption;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/21/2016
 */
public class DreamHostInteractionEvent extends NPCInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] { 4516 };
	}

	@Override
	public boolean handleNPCInteraction(Player player, NPC npc, ClickOption option) {
		if (option == ClickOption.SECOND) {
			GsonStartup.getOptional(StoreLoader.class).ifPresent(loader -> loader.openStore(player, "Nightmare Zone Shop"));
			return true;
		}
		return false;
	}
}
