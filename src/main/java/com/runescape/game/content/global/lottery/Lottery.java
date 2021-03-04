package com.runescape.game.content.global.lottery;

import com.runescape.game.GameConstants;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleNPCMessage;
import com.runescape.game.interaction.dialogues.impl.misc.SimplePlayerMessage;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.ChatColors;
import com.runescape.utility.Utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 6/19/2015
 */
public class Lottery {

	/**
	 * This list contains all the winners of the lottery
	 */
	private static final List<LotteryWinner> WINNERS = new ArrayList<>();

	/**
	 * The time the lottery was started
	 */
	private static long lotteryStartTime = 0;

	/**
	 * The amount of cash people have put towards the lottery
	 */
	private static int lottoCash = 0;

	/**
	 * The cost for one ticket in the lottery
	 */
	public static final int COST_PER_TICKET = 750_000;

	/**
	 * The location of the file that has information about the lottery state
	 */
	private static final String LOTTERY_STATE_FILE = GameConstants.FILES_PATH + "players/lottery/state.txt";

	/**
	 * The location of the file that has information about the amount of money in the lottery
	 */
	private static final String LOTTERY_CASH_FILE = GameConstants.FILES_PATH + "players/lottery/cash.txt";

	/**
	 * The location of the file that has information about the winners of the lottery
	 */
	private static final String LOTTERY_WINNERS_FILE = GameConstants.FILES_PATH + "players/lottery/winners.txt";

	/**
	 * The location of the file that has information about the people who bought a ticket
	 */
	private static final String LOTTERY_BUYERS_FILE = GameConstants.FILES_PATH + "players/lottery/buyers.txt";

	/**
	 * The lock object
	 */
	private static final Object LOCK = new Object();

	/**
	 * This method loads all information about the lottery
	 */
	public static void loadLottery() {
		loadCash();
		loadState();
		loadWinners();
	}

	/**
	 * Processes the lottery
	 */
	public static void process() {
		if (lotteryStartTime != 0) {
			// If 24 hours have passed, we will draw a winner
			if (TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - lotteryStartTime) >= 24) {
				drawWinner();
			}
		}
	}

	/**
	 * This method loads the cash from the file into the {@link #lottoCash}
	 */
	public static void loadCash() {
		String cash = Utils.getText(LOTTERY_CASH_FILE).trim();
		if (cash.length() > 0) {
			lottoCash = Integer.parseInt(cash);
		}
	}

	/**
	 * This method saves the amount of cash in the lottery to the {@link #LOTTERY_CASH_FILE}
	 */
	public static void saveCash() {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOTTERY_CASH_FILE))) {
			writer.write("" + lottoCash);
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method loads the state of the lottery from the file
	 */
	public static void loadState() {
		String state = Utils.getText(LOTTERY_STATE_FILE).trim();
		if (state.length() > 0) {
			lotteryStartTime = Long.parseLong(state);
		}
	}

	/**
	 * This method saves the state of the lottery to the file
	 */
	public static void saveState() {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOTTERY_STATE_FILE))) {
			writer.write("" + lotteryStartTime);
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method loads the winners from the {@link #LOTTERY_WINNERS_FILE} into the {@link #WINNERS} list
	 */
	public static void loadWinners() {
		synchronized (LOCK) {
			WINNERS.clear();
			List<String> fileText = Utils.getFileText(LOTTERY_WINNERS_FILE);
			for (String text : fileText) {
				if (!text.contains("~")) {
					continue;
				}
				String[] split = text.split("~");
				String winnerName = split[0];
				int cashWon = Integer.parseInt(split[1]);
				WINNERS.add(new LotteryWinner(winnerName, cashWon));
			}
		}
	}

	/**
	 * This method dumps all the winners from the {@link #WINNERS} list to a file located in {@link
	 * #LOTTERY_WINNERS_FILE}
	 */
	public static void dumpWinners() {
		synchronized (LOCK) {
			Utils.clearFile(LOTTERY_WINNERS_FILE);
			for (LotteryWinner winner : WINNERS) {
				System.out.println("writing winner: " + winner.getName() + "." + winner.getCashWon());
				try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOTTERY_WINNERS_FILE, true))) {
					writer.write(winner.getName() + "~" + winner.getCashWon());
					writer.newLine();
					writer.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * This method handles the buying of tickets
	 *
	 * @param player
	 * 		The player who's buying tickets
	 * @param amount
	 * 		The amount of tickets bought
	 */
	public static boolean purchaseTickets(Player player, int amount) {
		synchronized (LOCK) {
			long totalPrice = amount * COST_PER_TICKET;
			if (totalPrice < 0 || totalPrice > Integer.MAX_VALUE) {
				player.getDialogueManager().startDialogue(SimpleNPCMessage.class, 2998, "You can't buy that many tickets.");
				return false;
			}
			int price = (int) totalPrice;
			if (player.takeMoney(price)) {
				player.getLockManagement().lockAll();
				lottoCash += price;
				for (int i = 0; i < amount; i++) {
					addBuyer(player.getUsername());
				}
				saveCash();
				loadCash();

				if (lotteryStartTime == 0) {
					startLottery();
				}

				player.getLockManagement().unlockAll();
				player.getDialogueManager().startDialogue(SimpleNPCMessage.class, 2998, "You have entered the lottery " + amount + " times, good luck!");
				return true;
			} else {
				player.getDialogueManager().startDialogue(SimplePlayerMessage.class, "I don't have enough money to do that!");
				return false;
			}
		}
	}

	/**
	 * This method officially starts the lottery by initializing the time it was started
	 */
	private static void startLottery() {
		lotteryStartTime = System.currentTimeMillis();
		saveState();
		World.sendWorldMessage("<img=6>Lottery: The lottery has just started! Speak to the Gambler to enter!.", false);
	}

	/**
	 * This method resets the lottery and saves all reset variables
	 */
	private static void resetLottery() {
		Utils.clearFile(LOTTERY_BUYERS_FILE);
		lotteryStartTime = 0;
		lottoCash = 0;
		saveCash();
		saveState();
	}

	/**
	 * This method draws a winner from the file and sets them as the winner
	 */
	public static void drawWinner() {
		synchronized (LOCK) {
			List<String> names = Utils.getFileText(LOTTERY_BUYERS_FILE);
			Collections.shuffle(names);
			String winner = names.get(0);
			addWinner(winner, lottoCash / 2);
			resetLottery();
		}
	}

	/**
	 * This method adds a winner to the list of all winners
	 *
	 * @param name
	 * 		The name of the winner
	 * @param amount
	 * 		The amount of cash the winner won
	 */
	public static void addWinner(String name, int amount) {
		synchronized (LOCK) {
			WINNERS.add(new LotteryWinner(name, amount));
			dumpWinners();
			loadWinners();
			World.sendWorldMessage("<img=6><col=" + ChatColors.RED + ">Lottery</col>: " + Utils.formatPlayerNameForDisplay(name) + " has just won the lottery! Claim your reward at the gambler.", false);
		}
	}

	/**
	 * This method adds the name of the lottery ticket buyer to the file in {@link #LOTTERY_BUYERS_FILE}
	 *
	 * @param name
	 * 		The name of the buyer
	 */
	public static void addBuyer(String name) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOTTERY_BUYERS_FILE, true))) {
			writer.write(name);
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method gets the lottery reward for the player, assuming they've won the lottery.
	 *
	 * @param name
	 * 		The name of the player
	 * @return A {@code Integer} {@code Object}
	 */
	public static int getLotteryReward(String name) {
		int reward = 0;
		for (LotteryWinner winner : WINNERS) {
			if (winner.getName().equalsIgnoreCase(name)) {
				reward += winner.getCashWon();
			}
		}
		return reward;
	}

	/**
	 * This method removes all winners from the {@link #WINNERS} list that has the same name as the player claiming
	 * their rewards.
	 *
	 * @param player
	 * 		The player
	 */
	public static void removeWinner(Player player) {
		Iterator<LotteryWinner> it$ = WINNERS.iterator();
		while (it$.hasNext()) {
			LotteryWinner winner = it$.next();
			if (winner.getName().equalsIgnoreCase(player.getUsername())) {
				it$.remove();
			}
		}
		dumpWinners();
		loadWinners();
	}

	/**
	 * Gets the amount of wins for the player
	 *
	 * @param player
	 * 		The player
	 * @return
	 */
	public static List<LotteryWinner> getWins(Player player) {
		synchronized (LOCK) {
			List<LotteryWinner> myWins = new ArrayList<>();
			for (LotteryWinner winner : WINNERS) {
				if (winner.getName().equalsIgnoreCase(player.getUsername())) {
					myWins.add(winner);
				}
			}
			return myWins;
		}
	}

	/**
	 * Finds the time the lottery was started
	 *
	 * @return
	 */
	public static long getLotteryStartTime() {
		return lotteryStartTime;
	}

	/**
	 * If the lottery is runninng
	 *
	 * @return {@code True} if it is running
	 */
	public static boolean running() {
		return lotteryStartTime > 0;
	}

}
