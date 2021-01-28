package com.runescape.game.interaction.dialogues.impl.npc;

import com.runescape.game.GameConstants;
import com.runescape.game.content.global.lottery.Lottery;
import com.runescape.game.content.global.lottery.LotteryWinner;
import com.runescape.game.event.InputEvent;
import com.runescape.game.event.InputEvent.InputEventType;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.world.World;
import com.runescape.utility.ChatColors;
import com.runescape.utility.Utils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 6/19/2015
 */
public class Gambler extends Dialogue {

	@Override
	public void start() {
		sendNPCDialogue(npcId = getParam(0), HAPPY, "Hello, fair " + GameConstants.SERVER_NAME + " player.", "I am the lottery manager. How may I help you today?");
	}

	@Override
	public void run(int interfaceId, int option) {
		List<LotteryWinner> wins = Lottery.getWins(player);
		switch (stage) {
			case -1:
				if (wins.size() > 0) {
					sendOptionsDialogue(DEFAULT_OPTIONS, "Buy Tickets (" + Utils.numberToCashDigit(Lottery.COST_PER_TICKET) + " each)", "Information", "Next Draw", "Claim Wins");
				} else {
					sendOptionsDialogue(DEFAULT_OPTIONS, "Buy Tickets (" + Utils.numberToCashDigit(Lottery.COST_PER_TICKET) + " each)", "Information", "Next Draw", "Cancel");
				}
				stage = 0;
				break;
			case 0:
				switch (option) {
					case 1:
						sendOptionsDialogue(DEFAULT_OPTIONS, "1 Ticket", "5 Tickets", "10 Tickets", "100 Tickets", "X Tickets");
						stage = 1;
						break;
					case 2:
						sendPlayerDialogue(CALM, "I would like some information about the lottery, please...");
						stage = 2;
						break;
					case 3:
						if (Lottery.running()) {
							long hours = TimeUnit.MILLISECONDS.toHours((TimeUnit.HOURS.toMillis(24) + Lottery.getLotteryStartTime()) - System.currentTimeMillis());
							long minutes = TimeUnit.MILLISECONDS.toMinutes((TimeUnit.HOURS.toMillis(24) + Lottery.getLotteryStartTime()) - System.currentTimeMillis());
							String timeLeft = hours <= 0 ? minutes + " minutes" : hours + " hours";
							sendNPCDialogue(npcId, CALM, "The next draw will happen in " + timeLeft + ".");
						} else {
							sendNPCDialogue(npcId, CALM, "There is no lottery running right now.", "You can buy a ticket and start it!");
						}
						stage = -2;
						break;
					case 4:
						if (wins.size() > 0) {
							long total = 0;
							for (LotteryWinner win : wins) {
								player.getInventory().addItem(995, win.getCashWon());
								total += win.getCashWon();
							}
							sendNPCDialogue(npcId, CALM, "You have just claimed " + Utils.format(total) + " coins from the lottery.");
							World.sendWorldMessage("<img=6><col=" + ChatColors.MAROON + ">Lottery</col>: " + player.getDisplayName() + " has just claimed " + Utils.format(total) + " cash from the lottery!", false);
							World.sendWorldMessage("<img=6><col=" + ChatColors.MAROON + ">Lottery</col>: Enter the lottery for a chance at such rewards!", false);
							Lottery.removeWinner(player);
							stage = -2;
						} else {
							end();
						}
						break;
				}
				break;
			case 1:
				end();
				switch (option) {
					case FIRST:
						Lottery.purchaseTickets(player, 1);
						break;
					case SECOND:
						Lottery.purchaseTickets(player, 5);
						break;
					case THIRD:
						Lottery.purchaseTickets(player, 10);
						break;
					case FOURTH:
						Lottery.purchaseTickets(player, 100);
						break;
					case FIFTH:
						player.getPackets().requestClientInput(new InputEvent("Enter Amount", InputEventType.INTEGER) {
							@Override
							public void handleInput() {
								Lottery.purchaseTickets(player, getInput());
							}
						});
						break;
				}
				break;
			case 2:
				sendNPCDialogue(npcId, CALM, "The lottery is an event that runs 24/7 here on " + GameConstants.SERVER_NAME, "that gives poor players the opportunity to get rich", "literally overnight! You have a higher chance of", "winning if you buy more tickets.");
				stage = 3;
				break;
			case 3:
				sendNPCDialogue(npcId, CALM, "The draw happens 24 hours from the time", "the first ticket was bought.");
				stage = -2;
				break;
		}
	}

	@Override
	public void finish() {

	}

	private int npcId;

}
