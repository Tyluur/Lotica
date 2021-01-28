package com.runescape.game.event.interaction.object;

import com.runescape.game.event.interaction.type.ObjectInteractionEvent;
import com.runescape.game.interaction.dialogues.impl.skills.EnterHouseD;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.world.ClickOption;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 21, 2015
 */
public class ConstructionObjectInteractionEvent extends ObjectInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] { 15482 };
	}

	@Override
	public boolean handleObjectInteraction(Player player, WorldObject object, ClickOption option) {
		player.getDialogueManager().startDialogue(EnterHouseD.class);
		return true;
	}

}
