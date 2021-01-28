package com.runescape.game.world.entity.player;

import com.runescape.game.event.interaction.button.Scrollable;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.quests.Quest;
import com.runescape.game.world.entity.player.quests.QuestRequirement;
import com.runescape.game.world.entity.player.quests.impl.GertrudesCat.Stages;
import com.runescape.utility.Utils;
import com.runescape.utility.world.ClickOption;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 18, 2015
 */
public class QuestManager implements Serializable {

	private static final long serialVersionUID = 7209119510297047273L;

	/**
	 * The list of game quests
	 */
	private static final List<Quest<?>> QUESTS = new ArrayList<>();

	/**
	 * The map for the attributes players save for quests
	 */
	private final Map<String, Map<String, Object>> questAttributes = new HashMap<>();

	/**
	 * The player
	 */
	private transient Player player;

	/**
	 * Initializes all quests
	 */
	public static void initialize() {
		for (Object clazz : Utils.getClassesInDirectory(Quest.class.getPackage().getName() + ".impl")) {
			Quest<?> quest = (Quest<?>) clazz;
			getQuests().add(quest);
		}
		Collections.sort(QUESTS, (o1, o2) -> o1.getName().compareTo(o2.getName()));
	}

	/**
	 * @return the quests
	 */
	public static List<Quest<?>> getQuests() {
		return QUESTS;
	}

	/**
	 * Handling an npc interaction for the player doing a quest
	 *
	 * @param player
	 * 		The player
	 * @param npc
	 * 		The npc
	 * @param option
	 * 		The option that was clicked on the npc
	 */
	public static boolean handleNPCInteract(Player player, NPC npc, ClickOption option) {
		for (Quest<?> quest : QUESTS) {
			if (quest.handleNPCInteract(player, npc, option)) {
				return true;
			}
		}
		return false;
	}

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
	public static boolean handleObjectInteract(Player player, WorldObject object, ClickOption option) {
		for (Quest<?> quest : QUESTS) {
			if (quest.handleObjectInteract(player, object, option)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Handles the equipping of a quest item
	 *
	 * @param player
	 * 		The player
	 * @param itemId
	 * 		The item id
	 */
	public static boolean handleItemEquipping(Player player, int itemId) {
		for (Quest<?> quest : QUESTS) {
			if (!quest.handleItemEquipping(player, itemId)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Creates a stream for the {@link #QUESTS}
	 */
	public static Stream<Quest<?>> quests() {
		return getQuests().stream();
	}

	/**
	 * This method loops through all quests and checks if there is a quest we have not yet finished
	 *
	 * @return The quest we must finish
	 */
	public Quest<?> finishedAllQuests() {
		for (Quest<?> quest : QUESTS) {
			if (!isFinished(quest.getClass())) {
				return quest;
			}
		}
		return null;
	}

	/**
	 * If the player has finished the quest
	 *
	 * @param clazz
	 * 		The quest class
	 */
	public boolean isFinished(Class<?> clazz) {
		Quest<?> quest = getQuest(clazz);
		if (quest == null) {
			throw new IllegalStateException("Couldn't find quest by name:\t" + clazz);
		}
		Enum<?> lastStage = quest.getLastStage();
		Object ourStage = getAttribute(clazz, Quest.STAGE_KEY);
		return ourStage != null && ourStage.toString().equals(lastStage.toString());
	}

	/**
	 * @param clazz
	 * 		The class
	 */
	private Quest<?> getQuest(Class<?> clazz) {
		for (Quest<?> quest : getQuests()) {
			if (quest.getClass().getSimpleName().equalsIgnoreCase(clazz.getSimpleName())) {
				return quest;
			}
		}
		return null;
	}

	/**
	 * We are getting an attribute from the {@link #questAttributes} map. If the attribute doesn't exist, we return
	 * null.
	 *
	 * @param clazz
	 * 		The quest
	 * @param key
	 * 		The key
	 */
	@SuppressWarnings("unchecked")
	public <K> K getAttribute(Class<?> clazz, String key) {
		Map<String, Object> attributes = findAttributes(clazz);
		if (attributes == null || !attributes.containsKey(key)) {
			return null;
		}
		return (K) attributes.get(key);
	}

	/**
	 * Finds the attributes of a quest from the class
	 *
	 * @param clazz
	 * 		The class
	 */
	private Map<String, Object> findAttributes(Class<?> clazz) {
		Map<String, Object> attributes = questAttributes.get(clazz.getSimpleName());
		if (attributes == null) {
			return null;
		}
		return attributes;
	}

	/**
	 * Sets the stage of a quest
	 *
	 * @param clazz
	 * 		The class of the quest
	 * @param stage
	 * 		The stage
	 */
	public void setStage(Class<?> clazz, Stages stage) {
		addAttribute(clazz, Quest.STAGE_KEY, stage);
	}

	/**
	 * Adds an attribute to the map
	 *
	 * @param clazz
	 * 		The class
	 * @param key
	 * 		The key
	 * @param value
	 * 		The value
	 */
	public boolean addAttribute(Class<?> clazz, String key, Object value) {
		Map<String, Object> attributes = findAttributes(clazz);
		if (attributes == null) {
			attributes = new HashMap<>();
		}
		attributes.put(key, value);
		questAttributes.put(clazz.getSimpleName(), attributes);
		return true;
	}

	/**
	 * We are getting an attribute from the {@link #questAttributes} map. If there is no existant attribute by the key,
	 * we return the default value
	 *
	 * @param clazz
	 * 		The quest class
	 * @param key
	 * 		The key
	 * @param defaultValue
	 * 		The default value to return
	 */
	@SuppressWarnings("unchecked")
	public <K> K getAttribute(Class<?> clazz, String key, K defaultValue) {
		Map<String, Object> attributes = findAttributes(clazz);
		if (attributes == null || !attributes.containsKey(key)) {
			return defaultValue;
		}
		return (K) attributes.get(key);
	}

	/**
	 * Starts the quest for the player
	 *
	 * @param clazz
	 * 		The quest class
	 */
	public void startQuest(Class<?> clazz) {
		Quest<?> quest = getQuest(clazz);
		if (quest == null) {
			throw new IllegalStateException("Couldn't find quest by name:\t" + clazz);
		}
		if (!quest.passedRequirements(player)) {
			Scrollable.sendQuestScroll(player, quest.getName(), "You don't have the requirements to start this quest.", "View your quest journal to see what requirements you must have.");
			return;
		}
		addAttribute(quest.getClass(), Quest.STAGE_KEY, quest.getFirstStage());
		quest.onStart(player);
	}

	/**
	 * Finishes the quest for the player
	 *
	 * @param clazz
	 * 		The quest class
	 */
	public void finishQuest(Class<?> clazz) {
		Quest<?> quest = getQuest(clazz);
		if (quest == null) {
			throw new IllegalStateException("Couldn't find quest by name:\t" + clazz);
		}
		addAttribute(quest.getClass(), Quest.STAGE_KEY, quest.getLastStage());
		quest.onFinish(player);
		quest.sendCompletionInterface(player);
	}

	/**
	 * This method forces quests to be finished for a player and doesn't show them they finished it.
	 *
	 * @param classes
	 * 		The quest classes
	 */
	public void forceFinish(Class... classes) {
		for (Class<?> clazz : classes) {
			Quest<?> quest = getQuest(clazz);
			if (quest == null) {
				throw new IllegalStateException("Couldn't find quest by name:\t" + clazz);
			}
			addAttribute(quest.getClass(), Quest.STAGE_KEY, quest.getLastStage());
			quest.onFinish(player);
		}
	}

	/**
	 * Gets the stage of the quest
	 *
	 * @param clazz
	 * 		The quest class only
	 */
	public Enum<?> getStage(Class<?> clazz) {
		Quest<?> quest = getQuest(clazz);
		if (quest == null) {
			throw new IllegalStateException("Couldn't find quest by name:\t" + clazz);
		}
		return quest.getStage(player);
	}

	/**
	 * @return the questAttributes
	 */
	public Map<String, Map<String, Object>> getQuestAttributes() {
		return questAttributes;
	}

	/**
	 * @return the player
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * @param player
	 * 		the player to set
	 */
	public void setPlayer(Player player) {
		this.player = player;
	}

	/**
	 * Finds how many quest points the player has by looping through all completed quests and adding the quest's reward
	 * points
	 */
	public int getPoints() {
		int points = 0;
		for (Quest<?> quest : QUESTS) {
			if (!isFinished(quest.getClass())) {
				continue;
			}
			points += quest.getQuestPoints();
		}
		return points;
	}

	/**
	 * Sends the scroll of the quest
	 *
	 * @param quest
	 * 		The quest
	 */
	public void sendQuestScroll(Quest<?> quest) {
		if (!player.getQuestManager().hasStarted(quest.getClass())) {
			List<String> requirements = new ArrayList<>();
			QuestRequirement[] questRequirements = quest.getQuestRequirements(player);
			requirements.add("Requirements to start this quest:");
			if (questRequirements.length > 0) {
				requirements.add("");
				for (QuestRequirement requirement : questRequirements) {
					requirements.add((requirement.isCompleted() ? "<str>" : "") + requirement.getText());
				}
				requirements.add("");
			} else {
				requirements.add("<str>NONE!");
				requirements.add("");
			}
			Collections.addAll(requirements, quest.getJournalText(player));
			Scrollable.sendQuestScroll(player, quest.getName(), requirements.toArray(new String[requirements.size()]));
		} else {
			Scrollable.sendQuestScroll(player, quest.getName(), quest.getJournalText(player));
		}
	}

	/**
	 * If we have started a quest
	 *
	 * @param clazz
	 * 		The class of the quest
	 */
	public boolean hasStarted(Class<?> clazz) {
		Quest<?> quest = getQuest(clazz);
		if (quest == null) {
			throw new IllegalStateException("Couldn't find quest by name:\t" + clazz);
		}
		return questAttributes.get(clazz.getSimpleName()) != null;
	}
}
