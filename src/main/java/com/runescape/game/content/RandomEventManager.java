package com.runescape.game.content;

import com.runescape.game.event.InputEvent;
import com.runescape.game.event.InputEvent.InputEventType;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.interaction.dialogues.impl.item.SimpleItemAmountMessage;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.actions.Action;
import com.runescape.game.world.entity.player.actions.PlayerCombat;
import com.runescape.utility.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 10/20/15
 *
 * This class handles all random event execution, chance calculations, and chance operaetions.
 */
public class RandomEventManager {

	/**
	 * The bank of random event questions
	 */
	private static final List<String[]> RANDOM_EVENT_BANK = new ArrayList<>();

	/**
	 * The chance for a random event
	 */
	private int eventChance;

	/**
	 * The last time we checked for the event chance possibility
	 */
	private long lastEventTimeChecked = -1;

	/**
	 * The question and answer for the current event we're in
	 */
	private String[] ourBankQuery;

	/**
	 * The list of events we have checked the player for
	 */
	private transient List<Object> eventsChecked = new ArrayList<>();

	/**
	 * The player
	 */
	private transient Player player;

	public void process() {
		if (!isPlayerAvailable()) {
			return;
		}
		if (!inRandomEvent()) {
			listenForActions();
			if (shouldExecuteEvent()) {
				executeEvent(true, false);
			}
		} else {

			Object inputEvent = player.getAttribute("input_event");
			if (inputEvent == null && player.getDialogueManager().getDialogue() == null || (inputEvent == null && !player.getInterfaceManager().containsChatBoxInter()) || (inputEvent != null && TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - player.getAttribute("requested_client_input", System.currentTimeMillis())) >= 15)) {
				executeEvent(true, false);
			}
		}
	}

	/**
	 * Checks if the player is available for random events
	 */
	private boolean isPlayerAvailable() {
		return player.getAttackedByDelay() + 10000 <= Utils.currentTimeMillis() && !(player.getActionManager().getAction() != null && player.getActionManager().getAction() instanceof PlayerCombat) && !(player.getLockManagement().isAnyLocked()) && (player.getControllerManager().getController() == null) && !(player.getEmotesManager().getNextEmoteEnd() >= Utils.currentTimeMillis());
	}

	/**
	 * This method listens for changes in the player's state which would be a result of actions performed. The {@link
	 * #eventChance} is incremented when this is true.
	 */
	public void listenForActions() {
		Action lastAction = player.getActionManager().getLastAction();
		if (lastAction != null && !eventsChecked.contains(lastAction)) {
			eventsChecked.add(lastAction);
			incrementEventChance(5);
		}
		Animation lastAnimation = player.getLastAnimation();
		if (lastAnimation != null && !eventsChecked.contains(lastAnimation)) {
			eventsChecked.add(lastAnimation);
			incrementEventChance(1);
		}
		Object chatMessage = player.getLastChatMessage();
		if (chatMessage != null && !eventsChecked.contains(chatMessage)) {
			eventsChecked.add(chatMessage);
			incrementEventChance(2);
		}
		ConcurrentLinkedQueue<int[]> walkSteps = player.getWalkSteps();
		if (walkSteps != null && walkSteps.size() > 0) {
			incrementEventChance(1);
		}
	}

	/**
	 * If we should execute a random event
	 */
	public boolean shouldExecuteEvent() {
		if (player.removeAttribute("force_random_event", false)) {
			eventChance = 0;
			lastEventTimeChecked = System.currentTimeMillis();
			ourBankQuery = getRandomQuestionFromBank();
			return true;
		}
		if (eventChance >= 1000) {
			// if this is our first time checking, or we havent checked in 30 seconds
			if (lastEventTimeChecked == -1 || TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - lastEventTimeChecked) >= 30) {
				if (Utils.percentageChance(30)) {
					eventChance = 0;
					lastEventTimeChecked = System.currentTimeMillis();
					ourBankQuery = getRandomQuestionFromBank();
					return true;
				} else {
					lastEventTimeChecked = System.currentTimeMillis();
				}
			}
		}
		return false;
	}

	/**
	 * Executes the random event
	 */
	public void executeEvent(boolean showDialogueFirst, boolean comingBack) {
		player.stopAll();
		player.getLockManagement().lockAll();
		if (showDialogueFirst) {
			player.getDialogueManager().startDialogue(new Dialogue() {
				@Override
				public void start() {
					if (comingBack) {
						sendDialogue("You answered that question wrong! Please try again...");
					} else {
						sendDialogue("You must answer a random event question to continue gameplay.", "This is to ensure the legitimacy of your account.");
					}
				}

				@Override
				public void run(int interfaceId, int option) {
					end();
					requestClientInput();
				}

				@Override
				public void finish() {

				}
			});
		} else {
			requestClientInput();
		}
	}

	/**
	 * Requests the input from the player and validates it
	 */
	private void requestClientInput() {
		player.getPackets().requestClientInput(new InputEvent(ourBankQuery[0], InputEventType.NAME) {
			@Override
			public void handleInput() {
				String givenAnswer = getInput();
				if (givenAnswer.equalsIgnoreCase(ourBankQuery[1])) {
					int coinAmount = 50_000 + Utils.random(10_000, 30_000);
					player.getDialogueManager().startDialogue(SimpleItemAmountMessage.class, 995, coinAmount, "You answered the question correctly and were<br> rewarded with a nice lump of coins. (" + Utils.format(coinAmount) + " gp)");
					player.getInventory().addItemDrop(995, coinAmount);
					player.getLockManagement().unlockAll();
					ourBankQuery = null;
				} else {
					executeEvent(true, true);
				}
			}
		});
	}

	/**
	 * This method gets a random {@code String[]} {@code Object} from the {@link #RANDOM_EVENT_BANK}
	 */
	public String[] getRandomQuestionFromBank() {
		List<String[]> localizedList = new ArrayList<>(RANDOM_EVENT_BANK);
		Collections.shuffle(localizedList);
		return localizedList.get(0);
	}

	/**
	 * Increments the {@link #eventChance} by the given amoutn
	 *
	 * @param amount
	 * 		The amount to increment by
	 */
	public void incrementEventChance(int amount) {
		eventChance += amount;
	}

	/**
	 * This method populates the {@link #RANDOM_EVENT_BANK} with questions from the bank
	 */
	public static void populateQuestionBank() {
		try {
			RANDOM_EVENT_BANK.addAll(Utils.getFileText("./data/random_event_questions.txt").stream().map(line -> line.split("=")).collect(Collectors.toList()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sets the player variable and initializes other requirements
	 */
	public void initializeVars(Player player) {
		this.player = player;
		this.eventsChecked = new ArrayList<>();
	}

	/**
	 * If the player is in a random event
	 *
	 * @return True if they are
	 */
	public boolean inRandomEvent() {
		return ourBankQuery != null;
	}
}
