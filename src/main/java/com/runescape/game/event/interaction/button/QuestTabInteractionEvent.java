package com.runescape.game.event.interaction.button;

import com.runescape.game.event.interaction.type.InterfaceInteractionEvent;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.QuestManager;
import com.runescape.game.world.entity.player.achievements.AbstractAchievement;
import com.runescape.game.world.entity.player.achievements.AchievementData.AchievementType;
import com.runescape.game.world.entity.player.achievements.AchievementHandler;
import com.runescape.game.world.entity.player.quests.Quest;
import com.runescape.network.codec.decoders.WorldPacketsDecoder;
import com.runescape.network.codec.encoders.WorldPacketsEncoder;
import com.runescape.utility.ChatColors;
import com.runescape.utility.Utils;

import java.util.List;
import java.util.Objects;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/13/2016
 */
public class QuestTabInteractionEvent extends InterfaceInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] { 34 };
	}

	@Override
	public boolean handleInterfaceInteraction(Player player, int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
		String page = player.getAttribute("current_quest_page");
//		System.out.println("interfaceId = [" + interfaceId + "], componentId = [" + componentId + "], slotId = [" + slotId + "], slotId2 = [" + slotId2 + "], packetId = [" + packetId + "]");
		if (componentId == 9) {
			//player.getPackets().sendConfig(1439, -1);
			if (page == null) {
				return true;
			}
			List<AbstractAchievement> achievements = null;
			boolean checkingQuests = false;
			switch (page) {
				case "achievements_easy":
					achievements = AchievementHandler.getAchievements(AchievementType.EASY);
					break;
				case "achievements_medium":
					achievements = AchievementHandler.getAchievements(AchievementType.MEDIUM);
					break;
				case "achievements_hard":
					achievements = AchievementHandler.getAchievements(AchievementType.HARD);
					break;
				case "achievements_elite":
					achievements = AchievementHandler.getAchievements(AchievementType.ELITE);
					break;
				case "quests_list":
					checkingQuests = true;
					break;
			}
			if (checkingQuests) {
				List<Quest<?>> quests = QuestManager.getQuests();
				if (quests == null || slotId < 0 || slotId >= quests.size()) {
					return true;
				}
				player.getQuestManager().sendQuestScroll(quests.get(slotId));
			} else {
				if (achievements == null || slotId < 0 || slotId >= achievements.size()) {
					return true;
				}
				AbstractAchievement achievement = achievements.get(slotId);
				String description = achievement.getFormattedDescription();
				String requiredText = achievement.getRequiredText();
				player.sendMessage("<col=" + ChatColors.MAROON + ">" + achievement.getFormattedName() + "</col>: " + description + (description.endsWith(".") ? "" : ".") + " Progress: " + achievement.getProgress(player) + "/" + achievement.goal() + " " + (requiredText == null ? "" : "<br>Requirements: " + requiredText) + "");
			}
		} else if (componentId == 8) {
			if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
				// making sure we can send something
				if (page == null) {
					player.putAttribute("current_page", page = "achievements_easy");
				}
				AchievementType nextPage = null;
				boolean questsNext = false;
				switch (page) {
					case "achievements_easy":
						nextPage = AchievementType.MEDIUM;
						break;
					case "achievements_medium":
						nextPage = AchievementType.HARD;
						break;
					case "achievements_hard":
						nextPage = AchievementType.ELITE;
						break;
					case "achievements_elite":
						questsNext = true;
						break;
					case "quests_list":
						nextPage = AchievementType.EASY;
						break;
				}
				if (nextPage == null && !questsNext) {
					return true;
				}
				if (questsNext) { displayQuests(player); } else { displayAchievements(player, nextPage); }
			} else {
				displayQuests(player);
			}
		}
		return true;
	}

	public static void sendLoginConfiguration(Player player) {
		int interfaceId = 34;
		WorldPacketsEncoder packets = player.getPackets();

		packets.sendIComponentSettings(34, 9, 0, 30, 2621470);
		for (int i = 10; i < 16; i++) {
			packets.sendHideIComponent(34, i, true);
		}

//		packets.sendHideIComponent(interfaceId, 3, false);
		packets.sendIComponentModel(interfaceId, 3, 835);
//		packets.sendHideIComponent(interfaceId, 1, true);
	}

	/**
	 * Displays the achievements for a certain type to the player
	 *
	 * @param player
	 * 		The player
	 * @param type
	 * 		The type
	 */
	public static void displayAchievements(Player player, AchievementType type) {
		int completeCount = 0;
		int colour = 0;
		int interfaceId = 34;
		List<AbstractAchievement> achievements = AchievementHandler.getAchievements(type);
		for (int i = 0; i < 30; i++) {
			player.getPackets().sendGlobalString(149 + i, i < achievements.size() ? achievements.get(i).getFormattedName() : "");
		}
		for (int i = 0; i < achievements.size(); i++) {
			AbstractAchievement achievement = achievements.get(i);
			if (achievement.isFinished(player)) {
				colour += colourize(1, i);
				completeCount++;
			} else if (achievement.getProgress(player) > 0) {
				colour += colourize(2, i);
			} else {
				colour += colourize(3, i);
			}
			player.getPackets().sendConfig(1440, colour);
		}
		player.getPackets().sendHideIComponent(interfaceId, 3, true);
		player.getPackets().sendConfig(1439, -1);
		player.getPackets().sendIComponentText(interfaceId, 2, Utils.formatPlayerNameForDisplay(type.name()) + " (" + completeCount + "/" + achievements.size() + ")");
		player.putAttribute("current_quest_page", "achievements_" + type.name().toLowerCase());
	}

	/**
	 * Displays all the quests to the interface
	 *
	 * @param player
	 * 		The player
	 */
	public static void displayQuests(Player player) {
		List<Quest<?>> quests = QuestManager.getQuests();
		int completeCount = 0;
		int colour = 0;
		int interfaceId = 34;
		for (int i = 0; i < 30; i++) {
			player.getPackets().sendGlobalString(149 + i, i < quests.size() ? quests.get(i).getName() : "");
		}
		for (int i = 0; i < quests.size(); i++) {
			Quest<?> quest = quests.get(i);
			if (player.getQuestManager().isFinished(quest.getClass())) {
				colour += colourize(1, i);
				completeCount++;
			} else if (player.getQuestManager().hasStarted(quest.getClass())) {
				colour += colourize(2, i);
			} else {
				colour += colourize(3, i);
			}
			player.getPackets().sendConfig(1440, colour);
		}
		player.getPackets().sendHideIComponent(interfaceId, 3, true);
		player.getPackets().sendConfig(1439, -1);
		player.getPackets().sendIComponentText(interfaceId, 2, "Quest Points: " + player.getQuestManager().getPoints() + " (" + completeCount + "/" + quests.size() + ")");
		player.putAttribute("current_quest_page", "quests_list");
	}

	/**
	 * Refreshes the quest tab
	 *
	 * @param player
	 * 		The player to refresh it to
	 */
	public static void refresh(Player player) {
		String page = player.getAttribute("current_quest_page", "");
		if (Objects.equals(page, "")) {
			page = "achievements_easy";
		}
		AchievementType pageToOpen = null;
		boolean questsNext = false;
		switch (page) {
			case "achievements_easy":
				pageToOpen = AchievementType.EASY;
				break;
			case "achievements_medium":
				pageToOpen = AchievementType.MEDIUM;
				break;
			case "achievements_hard":
				pageToOpen = AchievementType.HARD;
				break;
			case "achievements_elite":
				pageToOpen = AchievementType.ELITE;
				break;
			case "quests_list":
				questsNext = true;
				break;
		}
		if (pageToOpen == null && !questsNext) {
			return;
		}
		if (questsNext) { displayQuests(player); } else { displayAchievements(player, pageToOpen); }
	}

	/**
	 * Colourizes text in the note tab
	 *
	 * @param colour
	 * 		The colour id for the text
	 * @param noteId
	 * 		The note id to colour
	 */
	private static int colourize(int colour, int noteId) {
		return (int) (Math.pow(4, noteId) * colour);
	}
}
