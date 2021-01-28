package com.runescape.game.event.interaction.npc;

import com.runescape.game.content.skills.magic.Magic;
import com.runescape.game.event.interaction.type.NPCInteractionEvent;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.world.ClickOption;
import com.runescape.utility.world.Coordinates;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 6/19/2015
 */
public class WildernessZamorakMageEvent extends NPCInteractionEvent {
	@Override
	public int[] getKeys() {
		return new int[] { 2257 };
	}

	@Override
	public boolean handleNPCInteraction(Player player, NPC npc, ClickOption option) {
		player.getDialogueManager().startDialogue(new Dialogue() {
			@Override
			public void start() {
				sendOptionsDialogue("Go to the Abyss?", "Yes", "No");
			}

			@Override
			public void run(int interfaceId, int option) {
				if (option == FIRST) {
					Magic.sendNormalTeleportSpell(player, 0, 0, Coordinates.ABYSS);
				}
				end();
			}

			@Override
			public void finish() {

			}
		});
		return true;
	}
}
