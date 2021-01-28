package com.runescape.game.event.interaction.button;

import com.runescape.game.event.interaction.type.InterfaceInteractionEvent;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.QuestManager;
import com.runescape.game.world.entity.player.quests.Quest;
import com.runescape.game.world.entity.player.quests.QuestRequirement;
import com.runescape.utility.ChatColors;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 19, 2015
 */
public class QuestsJournalEvent extends InterfaceInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] { 761 };
	}

	@Override
	public boolean handleInterfaceInteraction(Player player, int interfaceId, int buttonId, int slotId, int slotId2, int packetId) {
		if (buttonId >= 10 && buttonId <= 21) {
			List<Quest<?>> quests = QuestManager.getQuests();
			int index = Math.abs(10 - buttonId);
			if (index >= quests.size()) {
				return true;
			}
			Quest<?> quest = quests.get(index);
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
				for (String text : quest.getJournalText(player)) {
					requirements.add(text);
				}
				Scrollable.sendQuestScroll(player, quest.getName(), requirements.toArray(new String[requirements.size()]));
			} else {
				Scrollable.sendQuestScroll(player, quest.getName(), quest.getJournalText(player));
			}
			return true;
		}
		return true;
	}

	/**
	 * Displays the player's progress for all the possible quests
	 * 
	 * @param player
	 *            The player
	 */
	public static void display(Player player) {
		List<Quest<?>> quests = QuestManager.getQuests();
		int interfaceId = 761;
		int componentLength = 24;
		int startLine = 10;
		player.closeInterfaces();
		
		List<String> questText = new ArrayList<>();
		for (Quest<?> quest : quests) {
			String color = "";
			String text = quest.getName();
			if (!player.getQuestManager().hasStarted(quest.getClass())) {
				color = ChatColors.RED;
			} else {
				if (player.getQuestManager().isFinished(quest.getClass())) {
					color = ChatColors.GREEN;
				} else {
					color = ChatColors.ORANGE;
				}
			}
			questText.add("<col=" + color + ">" + text);
		}
		
		for (int i = 0; i < componentLength; i++) {
			player.getPackets().sendIComponentText(interfaceId, i, "");
		}
		for (String text : questText) {
			player.getPackets().sendIComponentText(interfaceId, startLine, text);
			startLine++;
		}
		player.getPackets().sendIComponentText(interfaceId, 6, "Quest Journal");
		player.getPackets().sendIComponentText(interfaceId, 9, "Quest Points: " + player.getQuestManager().getPoints());
		player.getPackets().sendIComponentText(interfaceId, 23, "Click on the quest to view your progress.");
		player.getInterfaceManager().sendInterface(interfaceId);
	}

}
