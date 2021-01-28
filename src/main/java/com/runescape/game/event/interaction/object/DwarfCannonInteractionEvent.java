package com.runescape.game.event.interaction.object;

import com.runescape.game.content.global.cannon.CannonAlgorithms;
import com.runescape.game.event.interaction.type.ObjectInteractionEvent;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.entity.player.OwnedObjectManager;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.world.ClickOption;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/5/2015
 */
public class DwarfCannonInteractionEvent extends ObjectInteractionEvent {
	
	@Override
	public int[] getKeys() {
		return new int[] { 6 };
	}

	@Override
	public boolean handleObjectInteraction(Player player, WorldObject object, ClickOption option) {
		if (option == ClickOption.FIRST) {
			CannonAlgorithms.toggleFiring(player, object);
		} else if (option == ClickOption.SECOND) {
			Player owner = OwnedObjectManager.getOwner(object);
			if (owner != null && owner.equals(player) && player.getDwarfCannon() != null) {
				player.getDwarfCannon().finish(false);
			} else {
				player.getDialogueManager().startDialogue("SimpleMessage", "That is not your cannon!");
			}
		}
		return true;
	}
}
