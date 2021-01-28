package com.runescape.game.event.interaction.button;

import com.runescape.game.event.InputEvent;
import com.runescape.game.event.InputEvent.InputEventType;
import com.runescape.game.event.interaction.type.InterfaceInteractionEvent;
import com.runescape.game.interaction.dialogues.impl.misc.MyAccountD;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AchievementHandler;
import com.runescape.workers.db.mysql.impl.DatabaseFunctions;

import java.util.concurrent.TimeUnit;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 19, 2015
 */
public class NavigationBookInteractionEvent extends InterfaceInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] { 506 };
	}

	@Override
	public boolean handleInterfaceInteraction(Player player, int interfaceId, int buttonId, int slotId, int slotId2, int packetId) {
		switch (buttonId) {
			case 2: // my account
				player.getDialogueManager().startDialogue(MyAccountD.class);
				break;
			case 4: // quests
				QuestsJournalEvent.display(player);
				break;
			case 6: // achievements
				AchievementHandler.displayTypeSelection(player);
				break;
			case 8: // claim auth
				Long lastTime = player.getAttribute("last_auth_sent");
				if (lastTime == null || (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - lastTime) > 10)) {
					player.getPackets().requestClientInput(new InputEvent("Enter Auth", InputEventType.LONG_TEXT) {
						@Override
						public void handleInput() {
							DatabaseFunctions.checkAuth(player, getInput());
						}
					});
					player.getAttributes().put("last_auth_sent", System.currentTimeMillis());
				} else {
					player.sendMessage("You can only do this once every 10 seconds...");
				}
				break;
			case 10: // claim donations
				Long lastDonationTime = player.getAttribute("last_payment_check");
				if (lastDonationTime == null || (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - lastDonationTime) > 10)) {
					DatabaseFunctions.claimGoldPoints(player);
					player.putAttribute("last_payment_check", System.currentTimeMillis());
				} else {
					player.sendMessage("You can only do this once every 10 seconds...");
				}
				break;
			case 12: // switch mage/pray
				player.getPresetManager().showPresetsInterface();
				break;
			case 14: // switch item looks
				player.getInventory().refresh();
				break;
		}
		return true;
	}

}
