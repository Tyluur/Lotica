package com.runescape.game.event.interaction.object;

import com.runescape.game.content.skills.magic.Magic;
import com.runescape.game.event.interaction.type.ObjectInteractionEvent;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.world.ClickOption;
import com.runescape.utility.world.Coordinates;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 4/19/2016
 */
public class EdgevilleLeverInteractionEvent extends ObjectInteractionEvent implements Coordinates {

	@Override
	public int[] getKeys() {
		return new int[] { 1814 };
	}

	@Override
	public boolean handleObjectInteraction(Player player, WorldObject object, ClickOption option) {
		player.getDialogueManager().startDialogue(new Dialogue() {
			@Override
			public void start() {
				sendOptionsDialogue(DEFAULT_OPTIONS, "West Dragons", "East Dragons", "Deserted Keep", "Cancel");
			}

			@Override
			public void run(int interfaceId, int option) {
				switch (option) {
					case FIRST:
						Magic.pushLeverTeleport(player, WEST_DRAGONS);
						break;
					case SECOND:
						Magic.pushLeverTeleport(player, EAST_DRAGONS);
						break;
					case THIRD:
						Magic.pushLeverTeleport(player, new WorldTile(3155, 3923, 0));
						break;
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
