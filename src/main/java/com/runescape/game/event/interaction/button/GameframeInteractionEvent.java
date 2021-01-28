package com.runescape.game.event.interaction.button;

import com.runescape.game.event.interaction.type.InterfaceInteractionEvent;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.interaction.dialogues.impl.misc.AdvisorD;
import com.runescape.game.world.entity.player.Player;
import com.runescape.network.codec.decoders.WorldPacketsDecoder;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 20, 2015
 */
public class GameframeInteractionEvent extends InterfaceInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] { 548, 746 };
	}

	@Override
	public boolean handleInterfaceInteraction(Player player, int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
		switch (componentId) {
			case 180:
			case 182:
				if (componentId == 182 && !player.getInterfaceManager().onResizable()) {
					return false;
				}
				if (player.getInterfaceManager().containsScreenInterface() || player.getInterfaceManager().containsInventoryInter()) {
					player.getPackets().sendGameMessage("Please finish what you're doing before opening the world map.");
					return true;
				}
				player.getDialogueManager().startDialogue(new DialogueExtension());
				return true;
			case 0:
			case 229:
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON7_PACKET) {
					player.getSkills().resetXpCounter();
				}
				return true;
			case 184:
			case 175:
				player.getDialogueManager().startDialogue(AdvisorD.class);
				break;
		}
		return false;
	}

	/**
	 * @author Tyluur
	 */
	private final class DialogueExtension extends Dialogue {

		@Override
		public void start() {
			sendOptionsDialogue("Open World Map?", "Yes", "Cancel");
		}

		@Override
		public void run(int interfaceId, int option) {
			if (option == FIRST) {
//				player.sendMessage("The world map is currently disabled.");
				player.getPackets().sendWindowsPane(755, 0);
				int posHash = player.getX() << 14 | player.getY();
				player.getPackets().sendGlobalConfig(622, posHash);
				player.getPackets().sendGlobalConfig(674, posHash);
			}
			end();
		}

		@Override
		public void finish() {
		}
	}

}
