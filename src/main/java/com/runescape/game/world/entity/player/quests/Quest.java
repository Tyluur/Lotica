package com.runescape.game.world.entity.player.quests;

import com.runescape.game.event.interaction.button.QuestTabInteractionEvent;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;
import com.runescape.utility.world.ClickOption;

import java.util.Objects;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 18, 2015
 */
public abstract class Quest<K extends Enum<?>> {

	/**
	 * The key in the player's quest attributes that shows the stage of the quest
	 */
	public static final String STAGE_KEY = "current_stage";

	/**
	 * Gets the text that will be shown in the journal for the stage
	 *
	 * @param player
	 * 		The player
	 */
	public abstract String[] getJournalText(Player player);

	/**
	 * Handles what to do when the player has started the Quest
	 *
	 * @param player
	 * 		The player
	 */
	public abstract void onStart(Player player);

	/**
	 * The quest points rewarded for completing this quest
	 */
	public abstract int getQuestPoints();

	/**
	 * Handles the interaction with an npc
	 *
	 * @param player
	 * 		The player
	 * @param npc
	 * 		The npc
	 * @param option
	 * 		The option
	 */
	public abstract boolean handleNPCInteract(Player player, NPC npc, ClickOption option);

	/**
	 * Handles the interaction with an object
	 *
	 * @param player
	 * 		The player
	 * @param object
	 * 		The object
	 * @param option
	 * 		The option
	 */
	public boolean handleObjectInteract(Player player, WorldObject object, ClickOption option) {
		return false;
	}

	/**
	 * Handles the equipping of an item
	 *
	 * @param player
	 * 		The player
	 * @param itemId
	 * 		The item id
	 */
	public boolean handleItemEquipping(Player player, int itemId) {
		return true;
	}

	/**
	 * Handles what to do when the quest is finished
	 *
	 * @param player
	 * 		The player
	 */
	public void onFinish(Player player) {

	}

	/**
	 * Gets the first stage in the quest.
	 */
	@SuppressWarnings("unchecked")
	public K getFirstStage() {
		return (K) getEnumConstants()[0];
	}

	/**
	 * Gets the enumeration in the tags
	 */
	public Enum<?>[] getEnumConstants() {
		QuestTag tag = getClass().getAnnotation(QuestTag.class);
		if (tag == null) {
			throw new IllegalStateException();
		}
		return tag.value().getEnumConstants();
	}

	/**
	 * Gets the last stage in the quest.
	 */
	@SuppressWarnings("unchecked")
	public K getLastStage() {
		return (K) getEnumConstants()[getEnumConstants().length - 1];
	}

	/**
	 * Gets the stage of a quest
	 *
	 * @param player
	 * 		The player
	 */
	@SuppressWarnings("unchecked")
	public K getStage(Player player) {
		Enum<?>[] constants = getEnumConstants();
		for (Enum<?> constant : constants) {
			Object stage = player.getQuestManager().getAttribute(getClass(), STAGE_KEY);
			if (stage == null) {
				break;
			}
			if (stage.toString().equals(constant.toString())) {
				return (K) constant;
			}
		}
		return null;
	}

	/**
	 * Sets the stage
	 *
	 * @param player
	 * 		The player
	 * @param stage
	 * 		The stage
	 */
	public void setStage(Player player, Object stage) {
		storeAttribute(player, STAGE_KEY, stage);
	}

	/**
	 * Stores an attribute to the player's quest attribute's map.
	 *
	 * @param player
	 * 		The player
	 * @param key
	 * 		The key
	 * @param value
	 * 		The value
	 */
	protected void storeAttribute(Player player, String key, Object value) {
		player.getQuestManager().addAttribute(getClass(), key, value);
		if (Objects.equals(key, STAGE_KEY)) {
			QuestTabInteractionEvent.refresh(player);
		}
	}

	/**
	 * If the player has passed all requirements for the quest
	 *
	 * @param player
	 * 		The player
	 */
	public boolean passedRequirements(Player player) {
		for (QuestRequirement requirement : getQuestRequirements(player)) {
			if (!requirement.isCompleted()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Gets the quest requirements
	 *
	 * @param player
	 * 		The player
	 */
	public abstract QuestRequirement[] getQuestRequirements(Player player);

	/**
	 * Sends the interface for completing a quest
	 *
	 * @param player
	 * 		The player that just finished the quest
	 */
	public void sendCompletionInterface(Player player) {
		int interfaceId = 277;
		int componentLength = Utils.getInterfaceDefinitionsComponentsSize(interfaceId);
		for (int i = 0; i < componentLength; i++) {
			player.getPackets().sendIComponentText(interfaceId, i, "");
		}
		player.getPackets().sendIComponentText(interfaceId, 3, "Congratulations!");
		player.getPackets().sendIComponentText(interfaceId, 4, "You have completed " + getName() + "!");
		player.getPackets().sendIComponentText(interfaceId, 9, "You are awarded:");
		player.getPackets().sendIComponentText(interfaceId, 10, getRewardInformation());
		player.getPackets().sendItemOnIComponent(interfaceId, 5, getQuestCompletionItemId(), 1);
		player.getInterfaceManager().sendInterface(interfaceId);
	}

	/**
	 * The information about rewards for finishing the quest
	 */
	public abstract String getRewardInformation();

	/**
	 * The name of the quest
	 */
	public abstract String getName();

	/**
	 * Gets the item id that will be sent over the completion interface
	 */
	public abstract int getQuestCompletionItemId();
}
