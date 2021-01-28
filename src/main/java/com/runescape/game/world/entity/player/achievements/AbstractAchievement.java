package com.runescape.game.world.entity.player.achievements;

import com.runescape.game.interaction.dialogues.impl.misc.SimpleMessage;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AchievementData.AchievementType;
import com.runescape.game.world.item.Item;
import com.runescape.utility.Utils;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 17, 2015
 */
public abstract class AbstractAchievement {

	/**
	 * The type of achievements
	 */
	protected final AchievementType easy = AchievementType.EASY, med = AchievementType.MEDIUM, hard = AchievementType.HARD, elite = AchievementType.ELITE;

	/**
	 * The type of achievement this is
	 */
	private AchievementType type;

	/**
	 * The description of the achievement sent over the interface
	 */
	public abstract String description();

	/**
	 * Handles rewarding the player for completing this achievement
	 *
	 * @param player
	 * 		The player
	 */
	public abstract void reward(Player player);

	/**
	 * The id of the item that will be displayed on the interface next to the name of this achievement
	 */
	public int interfaceItemId() {
		return -1;
	}

	/**
	 * If the player has finished this achievement
	 *
	 * @param player
	 * 		The player
	 */
	public boolean isFinished(Player player) {
		return getProgress(player) >= goal();
	}

	public Class<?>[] requiredToComplete() {
		return null;
	}

	/**
	 * The amount of progress the player has made towards this achievement
	 *
	 * @param player
	 * 		The player
	 */
	public int getProgress(Player player) {
		return player.getFacade().getProgress(this);
	}

	/**
	 * The amount of this achievement we must reach
	 */
	public abstract Integer goal();

	/**
	 * The key of the achievement
	 */
	public String key() {
		return getClass().getSimpleName();
	}

	/**
	 * Gets the formatted name of the achievement
	 */
	public String getFormattedName() {
		return Utils.formatPlayerNameForDisplay(key());
	}

	/**
	 * Gets the formatted description
	 */
	public String getFormattedDescription() {
		return description().replaceAll("@TOTAL@", "" + goal());
	}
	
	/**
	 * Gives the player the item reward. If their controller is null it will go to their inventory if they have space
	 * for it. If they don't have space for it it will go to their bank
	 *
	 * @param player
	 * 		The player
	 * @param items
	 * 		The items to give
	 */
	protected void addItem(Player player, Item... items) {
		boolean banked = false;
		if (player.getControllerManager().getController() == null) {
			for (Item item : items) {
				if (!player.getInventory().addItem(item)) {
					player.getBank().addItem(item.getId(), item.getAmount(), true);
					banked = true;
				}
			}
		} else {
			for (Item item : items) {
				player.getBank().addItem(item.getId(), item.getAmount(), true);
				banked = true;
			}
		}
		if (banked) {
			player.getDialogueManager().startDialogue(SimpleMessage.class, "You have just received some achievements rewards in your bank!");
			player.sendMessage("You have just received some achievements rewards in your bank!");
		}
	}

	/**
	 * The title of the achievement sent over the interface
	 */
	public String title() {
		return getClass().getSimpleName().replaceAll("_", " ");
	}

	/**
	 * @return the type
	 */
	public AchievementType getType() {
		return type;
	}

	/**
	 * @param type
	 * 		the type to set
	 */
	public void setType(AchievementType type) {
		this.type = type;
	}

	public String getRequiredText() {
		Class<?>[] requiredToComplete = requiredToComplete();
		if (requiredToComplete == null) {
			return null;
		} else {
			String names = "";
			for (int i = 0; i < requiredToComplete.length; i++) {
				Class clazz = requiredToComplete[i];
				names += Utils.formatPlayerNameForDisplay(clazz.getSimpleName()) + "" + (i == requiredToComplete.length - 1 ? "" : ", ");
			}
			return names;
		}
	}
}
