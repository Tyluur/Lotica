package com.runescape.game.event.interaction.npc;

import com.runescape.game.event.interaction.type.NPCInteractionEvent;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.external.gson.GsonStartup;
import com.runescape.utility.external.gson.loaders.StoreLoader;
import com.runescape.utility.world.ClickOption;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 8/3/2015
 */
public class PolyporeMerchantInteractionEvent extends NPCInteractionEvent {
	
	@Override
	public int[] getKeys() {
		return new int[] { 14620 };
	}

	@Override
	public boolean handleNPCInteraction(Player player, NPC npc, ClickOption option) {
		if (option == ClickOption.FIRST) {
			player.getDialogueManager().startDialogue("Polypore_Store", npc.getId());
			return true;
		} else if (option == ClickOption.SECOND) {
			GsonStartup.getOptional(StoreLoader.class).ifPresent(clazz -> clazz.openStore(player, "Polypore Dungeon Supplies"));
			return true;
		} else {
			return false;
		}
	}
}
