package com.runescape.game.content;

import com.runescape.game.GameConstants;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Handles everything for a bank pin system.
 *
 * @author Bart
 * @author Tyluur
 * @since November 28th, 2013
 */
public class BankPinManager implements Serializable {

	private static final long serialVersionUID = -7132860503719848112L;

	/**
	 * The array of the users bank pin
	 */
	private int[] currentPin = new int[] { -1, -1, -1, -1 };

	/**
	 * The temporary pin, set when handling buttons on the interface
	 */
	private int[] temporaryPin = new int[] { -1, -1, -1, -1 };

	/**
	 * If the users recovery time is seven days. 7 if true, 3 if not.
	 */
	private boolean sevenDayRecoveryDelay = false;

	/**
	 * The time the user has to wait for the pin to be activated
	 */
	private long pinActivationDelay = -1L;

	/**
	 * The time the player can't open their bank for.
	 */
	private long inactiveTime;

	/**
	 * The time the bank pin will be cancelled, either 3 or 7 days after the player requests it.
	 */
	private long pinCancelationTime;

	/**
	 * The player instance
	 */
	private transient Player player;

	/**
	 * The amount of times the player has failed to enter the right pin
	 */
	private transient int failCount;

	public BankPinManager() {

	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	/**
	 * If the player has entered their bank pin during the session. Always returns true when you are not on the vps.
	 */
	public boolean enteredPinDuringSession() {
		return player.getAttribute("entered_bank_pin", false);
	}

	/**
	 * Sets the bank pin to the numbers parameterized
	 *
	 * @param numbers
	 * 		The numbers to set the bank pin to
	 */
	public void setPin(int... numbers) {
		System.arraycopy(numbers, 0, currentPin, 0, 4);
	}

	public void setRecoveryDelay(boolean sevenDays) {
		sevenDayRecoveryDelay = sevenDays;
		sendRecoveryString();
	}

	/**
	 * Sends the information about the recovery delay
	 */
	public void sendRecoveryString() {
		player.getPackets().sendGlobalString(344, "Your recovery delay has now been set to " + (sevenDayRecoveryDelay ? "7" : "3") + " days.<br><br>You would have to wait this long to delete your PIN if you set one and then forgot it.");
	}

	/**
	 * Shows the interface that tells asks the user if they want to confirm their bank pin
	 */
	public void showConfirmSetPin() {
		player.getInterfaceManager().sendInterface(14);

		player.getPackets().sendGlobalConfig(98, -1);

		player.getPackets().sendIComponentText(14, 32, "Do you really wish to set a PIN on your bank account?");
		player.getPackets().sendIComponentText(14, 34, "Yes, I really want a Bank PIN. I will never forget it!");
		player.getPackets().sendIComponentText(14, 36, "No, I might forget it!");
	}

	/**
	 * Shows the interface that asks the user if they want to confirm deleting their pin
	 */
	public void showConfirmDeletePin() {
		player.getInterfaceManager().sendInterface(14);

		player.getPackets().sendGlobalConfig(98, -1);

		player.getPackets().sendIComponentText(14, 32, "Do you really wish to delete your Bank PIN?");
		player.getPackets().sendIComponentText(14, 34, "Yes, I don't need a PIN anymore.");
		player.getPackets().sendIComponentText(14, 36, "No thanks, I'd rather keep the extra security!");
	}

	/**
	 * Shows the interface asking the player if they're sure they want to change their pin
	 */
	public void showConfirmChangePin() {
		player.getInterfaceManager().sendInterface(14);

		player.getPackets().sendGlobalConfig(98, -1);

		player.getPackets().sendIComponentText(14, 32, "Do you really wish to change your Bank PIN?");
		player.getPackets().sendIComponentText(14, 34, "Yes, I am ready for a new one.");
		player.getPackets().sendIComponentText(14, 36, "No thanks, I'll stick with my present one.");
	}

	/**
	 * Shows the interface that tells the user to enter their bank pin
	 *
	 * @param banking
	 * 		If we're entering the pin from the bank or settings
	 */
	public void showEnterPin(boolean banking) {
		player.getAttributes().remove("pin_number_stage");

		temporaryPin = new int[] { -1, -1, -1, -1 };

		player.getPackets().sendConfig(163, 0);
		player.getPackets().sendGlobalConfig(98, 0);
		player.getPackets().sendGlobalConfig(199, -1);

		player.getInterfaceManager().sendInterface(13);
		player.getPackets().sendInterface(true, 13, 5, 759);
		player.getPackets().sendIComponentSettings(13, 24, -1, -1, 0);

		player.getPackets().sendIComponentText(13, 27, banking ? "Enter your PIN" : "Set new PIN");
		player.getPackets().sendIComponentText(13, 26, banking ? "Please enter your PIN using the buttons below." : "Please choose a new FOUR DIGIT PIN using the buttons below.");

		player.getAttributes().put("pin_enter_reason", "setfirst");

		clearPinDialogue();
	}

	/**
	 * Clears the bank pin configurations
	 */
	public void clearPinDialogue() {
		player.getPackets().sendConfigByFile(1010, 0);
		player.getPackets().sendRunScript(1271, 1);
	}

	/**
	 * Toggles the recover delay and sends the info.
	 */
	public void switchRecoveryDelay() {
		sevenDayRecoveryDelay = !sevenDayRecoveryDelay;
		sendRecoveryString();
	}

	/**
	 * Clears the bank pin number, and the time
	 */
	public void clearPin() {
		currentPin = new int[] { -1, -1, -1, -1 };
		pinActivationDelay = -1l;
	}

	public void handlePinDigit(int stage, int pinNumber) {
		if (player.getAttributes().get("pin_enter_reason") == null) {
			return;
		}
		String reason = (String) player.getAttributes().get("pin_enter_reason");
		if (reason.equals("setfirst")) {
			temporaryPin[stage] = pinNumber;
			// player.getPackets().sendMessage(99, "Current temp pin: " +
			// Arrays.toString(temporaryPin), player);
		} else if (reason.equals("setsecond")) {
			temporaryPin[stage] = pinNumber;
			// player.getPackets().sendMessage(99, "Current 2temp pin: " +
			// Arrays.toString(temporaryPin), player);
		}
	}

	/**
	 * Sets the players bank pin information
	 *
	 * @param num
	 * 		The number which will be the last value in the array
	 */
	public void finishPin(int num) {
		for (int i = 0; i < 3; i++) {
			if (temporaryPin[i] == -1) {
				return;
			}
		}
		temporaryPin[3] = num;
		if (player.getAttributes().get("entering_pin") != null) {
			if (!Arrays.equals(currentPin, temporaryPin)) {
				player.getInterfaceManager().closeScreenInterface();
				player.getDialogueManager().startDialogue("SimpleMessage", "You entered " + Arrays.toString(temporaryPin) + "; it was not your bank pin!");
				setFailCount(getFailCount() + 1);
				if (getFailCount() == 3) {
					setInactiveTime(Utils.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
				}
				return;
			}
			player.getInterfaceManager().closeScreenInterface();
			player.getAttributes().put("entered_bank_pin", true);
			String task = (String) player.getAttributes().remove("entering_pin");
			switch (task) {
				case "bank":
					player.getBank().openBank();
					break;
			}
			return;
		}
		String reason = (String) player.getAttributes().get("pin_enter_reason");
		if (reason.equals("setfirst")) {
			player.getAttributes().put("first_pin", Arrays.copyOf(temporaryPin, 4));
			showEnterSecondPin();
		} else if (reason.equals("setsecond")) {
			int[] first = (int[]) player.getAttributes().get("first_pin");
			for (int i = 0; i < first.length; i++) {
				if (temporaryPin[i] != first[i]) {
					temporaryPin = new int[] { -1, -1, -1, -1 };
					openSettingsScreen();
					player.getPackets().sendGlobalString(344, "Those numbers did not match.<br><br>Your PIN has not been set; please try again if you wish to set a new PIN.");
					return;
				}
			}
			pinActivationDelay = GameConstants.HOSTED ? (System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)) : 0;
			player.getPackets().sendGlobalString(344, "You have requested that a PIN be set on your bank account. This will take effect in 6 days.<br><br>If you wish to cancel this PIN, please use the button on the left.");
			System.arraycopy(temporaryPin, 0, currentPin, 0, 4);
			player.removeAttribute("entered_bank_pin");
			openSettingsScreen();
		}
		temporaryPin = new int[] { -1, -1, -1, -1 };
	}

	public int getFailCount() {
		return failCount;
	}

	/**
	 * Shows the interface that requests the user to input their bank pin again.
	 */
	public void showEnterSecondPin() {
		player.getAttributes().remove("pin_number_stage");

		player.getPackets().sendConfig(163, 0);

		player.getPackets().sendGlobalConfig(98, 0);
		player.getPackets().sendGlobalConfig(199, -1);

		player.getInterfaceManager().sendInterface(13);
		player.getPackets().sendInterface(true, 13, 5, 759);
		player.getPackets().sendIComponentSettings(13, 24, -1, -1, 0);

		player.getPackets().sendIComponentText(13, 27, "Confirm new PIN");
		player.getPackets().sendIComponentText(13, 26, "Now please enter that number again.");

		player.getAttributes().put("pin_enter_reason", "setsecond");

		clearPinDialogue();
	}

	/**
	 * Sends the settings screen to the player
	 */
	public void openSettingsScreen() {
		player.getInterfaceManager().sendInterface(14);

		boolean waiting = pinActivationDelay > 0;
		boolean waitingfor = waiting && System.currentTimeMillis() < pinActivationDelay;
		int pinState = hasPin() && !waitingfor ? 1 : waiting ? 3 : 0;
		if (sevenDayRecoveryDelay) {
			pinState |= 1024;
		}

		if (waiting && System.currentTimeMillis() < pinActivationDelay) {
			player.getPackets().sendGlobalString(344, "You have requested that a PIN be set on your bank account. This will take effect in 6 days.<br><br>If you wish to cancel this PIN, please use the button on the left.");
		} else {
			player.getPackets().sendGlobalString(344, "Customers are reminded that they should NEVER tell anyone their Bank PINs or passwords, nor should they ever enter their PINs on any website form.<br><br>Have you read the PIN guide on the website?");
		}

		player.getPackets().sendGlobalConfig(98, pinState);

		// 0 = no pin
		// 1 & 2 = pin
		// 3 = coming soon
	}

	/**
	 * Tells you if the user has a bank pin
	 *
	 * @return A {@code boolean} {@code Object}, true if the user has a bank pin
	 */
	public boolean hasPin() {
		for (int i : currentPin) {
			if (i == -1) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Cancels the user's current bank pin
	 */
	public void cancelPin() {
		deletePinVariables();
		openSettingsScreen();
		player.getPackets().sendGlobalString(344, "The PIN has been cancelled and will NOT be set.<br><br>You still do not have a Bank PIN.");
	}

	/**
	 * Deletes the pin variables
	 */
	public void deletePinVariables() {
		currentPin = new int[] { -1, -1, -1, -1 };
		temporaryPin = new int[] { -1, -1, -1, -1 };
		pinActivationDelay = -1;
		player.removeAttribute("entered_bank_pin");
	}

    public boolean isSevenDayRecoveryDelay() {
        return this.sevenDayRecoveryDelay;
    }

    public long getInactiveTime() {
        return this.inactiveTime;
    }

    public long getPinCancelationTime() {
        return this.pinCancelationTime;
    }

    public void setInactiveTime(long inactiveTime) {
        this.inactiveTime = inactiveTime;
    }

    public void setPinCancelationTime(long pinCancelationTime) {
        this.pinCancelationTime = pinCancelationTime;
    }

    public void setFailCount(int failCount) {
        this.failCount = failCount;
    }
}
