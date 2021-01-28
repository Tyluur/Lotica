package com.runescape.game.interaction.dialogues.impl.misc;

import com.runescape.game.content.PlayerLook;
import com.runescape.game.content.global.TicketSystem;
import com.runescape.game.content.global.TicketSystem.TicketEntry;
import com.runescape.game.event.InputEvent;
import com.runescape.game.event.InputEvent.InputEventType;
import com.runescape.game.event.interaction.button.Scrollable;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.utility.ChatColors;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 4/20/2016
 */
public class AdvisorD extends Dialogue {

	/**
	 * The combat experience rates the player can have
	 */
	private static final int[] SETTABLE_RATES = { 1, 50, 150, 400, 500 };

	@Override
	public void start() {
		sendOptionsDialogue(DEFAULT_OPTIONS, "My account", "Set combat XP rate", "Change appearance", "Read donator benefits", player.isStaff() ? "Answer tickets" : "Request staff assistance");
	}

	@Override
	public void run(int interfaceId, int option) {
		switch (stage) {
			case -1:
				switch (option) {
					case FIRST:
						player.getDialogueManager().startDialogue(MyAccountD.class);
						break;
					case SECOND:
						List<String> options = new ArrayList<>();
						options.add(DEFAULT_OPTIONS);
						for (int rate : SETTABLE_RATES) {
							if (rate == 500 && !player.isAnyDonator()) {
								continue;
							}
							options.add("x" + rate + " combat experience " + (rate == 400 ? "(default)" : ""));
						}
						sendOptionsDialogue(options.toArray(new String[options.size()]));
						stage = 0;
						break;
					case THIRD:
						sendOptionsDialogue(DEFAULT_OPTIONS, "Change Gender/Skin", "Change Clothing", "Change Hair/Beard", "Change Shoes");
						stage = 1;
						break;
					case FOURTH:
						sendDonatorBenefits();
						break;
					case FIFTH:
						if (player.isStaff()) {
							TicketSystem.answerTicket(player);
							end();
						} else {
							sendOptionsDialogue("What do you need<br> assistance with?", "General Questions (Ingame)", "Player Report / Bug report", "Other", "Cancel");
							stage = 2;
						}
						break;
				}
				break;
			case 0:
				int rateSlot = option - 1;
				if (rateSlot < 0 || rateSlot >= SETTABLE_RATES.length) {
					return;
				}
				player.getSkills().setCombatRate(SETTABLE_RATES[rateSlot]);
				sendDialogue("Your combat experience rates are now x" + SETTABLE_RATES[rateSlot] + ".");
				stage = -2;
				break;
			case 1:
				if (player.getEquipment().wearingArmour()) {
					sendDialogue("You must take off your armour first.");
					stage = -2;
					return;
				}
				if (player.getControllerManager().getController() != null) {
					sendDialogue("You can't change your appearance here.");
					stage = -2;
					return;
				}
				switch (option) {
					case FIRST:
						PlayerLook.openGenderSelection(player);
						break;
					case SECOND:
						PlayerLook.openClothingSelection(player);
						break;
					case THIRD:
						PlayerLook.openHairSelection(player);
						break;
					case FOURTH:
						PlayerLook.openShoeSelection(player);
						break;
				}
				end();
				break;
			case 2:
				String reason = "";
				switch (option) {
					case FIRST:
						reason = "General Questions";
						break;
					case SECOND:
						reason = "Player Report / Bug report";
						break;
					case THIRD:
						end();
						player.getPackets().requestClientInput(new InputEvent("What do you need assistance with?", InputEventType.LONG_TEXT) {
							@Override
							public void handleInput() {
								TicketSystem.requestTicket(player, new TicketEntry(player, getInput().toString()));
							}
						});
						return;
					case FOURTH:
						end();
						break;
				}
				if (TicketSystem.requestTicket(player, new TicketEntry(player, reason))) {
					sendDialogue("Your ticket has been submitted.");
				} else {
					end();
				}
				stage = -2;
				break;
		}
	}

	private void sendDonatorBenefits() {
		String[] messages = new String[] { "<col=" + ChatColors.MAROON + ">Read ::openthread 96 to see more information.", "<col=" + ChatColors.MAROON + ">Once purchased you will be given +30 days of membership.", "<col=" + ChatColors.MAROON + ">Membership can be tracked in your info tab.", "", "<col=" + ChatColors.MAROON + ">Experience Benefits", "<col=" + ChatColors.MAROON + ">------------------------------------", "", "<col=" + ChatColors.BLUE + ">The ability to receive trimmed skill capes", "<col=" + ChatColors.BLUE + ">without maxing out 5 skills beforehand.", "", "<col=" + ChatColors.BLUE + ">The ability to change your combat XP Rate", "<col=" + ChatColors.BLUE + ">to x500 from the '?' Advisor menu.", "", "<col=" + ChatColors.BLUE + ">Non-combat stats will receive 15% more", "<col=" + ChatColors.BLUE + "><col=" + ChatColors.BLUE + ">experience than normal.", "", "<col=" + ChatColors.BLUE + ">Bones used on the gilded altar in edgeville", "<col=" + ChatColors.BLUE + ">will result in a larger experience bonus.", "", "<col=" + ChatColors.MAROON + ">Activity Benefits", "<col=" + ChatColors.MAROON + ">------------------------------------", "", "<col=" + ChatColors.BLUE + ">You will join the fight caves on wave 30 (or", "<col=" + ChatColors.BLUE + ">wave 56 as an elite donator) instead of wave 1.", "", "<col=" + ChatColors.BLUE + ">Defenders will drop more frequently during the", "<col=" + ChatColors.BLUE + ">Warriors Guild activity.", "", "<col=" + ChatColors.BLUE + ">God Wars monsters will give double the kill count." };
		Scrollable.sendQuestScroll(player, "Donator Benefits", messages);
	}

	@Override
	public void finish() {

	}
}
