package com.runescape.game.event.interaction.npc;

import com.runescape.game.event.interaction.type.NPCInteractionEvent;
import com.runescape.game.interaction.dialogues.impl.skills.VannakaD;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.external.gson.GsonStartup;
import com.runescape.utility.external.gson.loaders.StoreLoader;
import com.runescape.utility.world.ClickOption;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 24, 2015
 */
public class VannakaInteractionEvent extends NPCInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] { 1597 };
	}

	@Override
	public boolean handleNPCInteraction(Player player, NPC npc, ClickOption option) {
		if (option == ClickOption.FIRST) {
			player.getDialogueManager().startDialogue(VannakaD.class, npc);
		} else if (option == ClickOption.SECOND) {
			VannakaD.sendTaskDialogue(player, npc);
		} else if (option == ClickOption.FOURTH) {
			GsonStartup.getOptional(StoreLoader.class).ifPresent(c -> c.openStore(player, "Slayer Point Store"));
		}
		return true;
	}

}
