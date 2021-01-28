package com.runescape.game.interaction.dialogues.impl.misc;

import com.runescape.game.event.interaction.button.Scrollable;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.ChatColors;
import com.runescape.utility.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/5/2015
 */
public class MyAccountD extends Dialogue {
	
	@Override
	public void start() {
		sendOptionsDialogue(DEFAULT_OPTIONS, "My Presets", "My Options",  "Toggle Experience Lock", "My Boss Kill Info", "Game Points");
	}

	@Override
	public void run(int interfaceId, int option) {
		switch (stage) {
			case -1:
				switch (option) {
					case FIRST:
						end();
						player.getPresetManager().showPresetsInterface();
						break;
					case SECOND:
						sendOptionsDialogue(DEFAULT_OPTIONS, "Remove title", "Cancel");
						stage = 1;
						break;
					case THIRD:
						player.getFacade().toggleExperienceLock();
						sendDialogue("Your experience is now " + (player.getFacade().isExperienceLocked() ? "" : "un") + "locked.");
						stage = -2;
						break;
					case FOURTH:
						sendOptionsDialogue(DEFAULT_OPTIONS, "Boss Kill Times", "Boss Kill Counts");
						stage = 0;
						break;
					case FIFTH:
						Scrollable.sendQuestScroll(player, player.getDisplayName() + "'s Point Info.", getGamePointsInformation());
						break;
				}
				break;
			case 0:
				switch (option) {
					case FIRST:
						player.getKillTimeManager().displayBossKillTimes();
						break;
					case SECOND:
						displayBossKillCounts(player);
						break;
				}
				break;
			case 1:
				if (option == FIRST) {
					player.getAppearence().setTitle(-1);
					player.getAppearence().generateAppearenceData();
					sendDialogue("Your display title has been removed. You can have it back for free", "by claiming it back from the loyalty point store.");
					stage = -2;
				} else {
					end();
				}
				break;
		}
	}

	/**
	 * Displays the kill counts interface to the player
	 */
	public static void displayBossKillCounts(Player player) {
		List<String> messages = new ArrayList<>();
		Map<String, Integer> killCounts = player.getFacade().getBossKillCounts();
		for (String name : NPC.BOSS_NAMES) {
			Integer value = killCounts.get(name);
			messages.add(name + ": <col=" + ChatColors.MAROON + ">" + (value == null ? 0 : Utils.format(value)));
		}
		Scrollable.sendQuestScroll(player, "Boss Kill Counts", messages.toArray(new String[messages.size()]));
	}

	/**
	 * Gets the game point information of the player and formats it into a pretty string
	 */
	private String getGamePointsInformation() {
		StringBuilder bldr = new StringBuilder();

		bldr.append("Dream Points: <col=" + ChatColors.WHITE + ">").append(Utils.format(player.getFacade().getDreamPoints())).append("<br>");
		bldr.append("Vote Points: <col=" + ChatColors.WHITE + ">").append(Utils.format(player.getFacade().getVotePoints())).append("<br>");
		bldr.append("Wilderness Points: <col=" + ChatColors.WHITE + ">").append(Utils.format(player.getFacade().getWildernessPoints())).append("<br>");
		bldr.append("Gold Points: <col=" + ChatColors.WHITE + ">").append(Utils.format(player.getFacade().getGoldPoints())).append("<br>");
		bldr.append("Slayer Points: <col=" + ChatColors.WHITE + ">").append(Utils.format(player.getFacade().getSlayerPoints())).append("<br>");
		bldr.append("Loyalty Points: <col=" + ChatColors.WHITE + ">").append(Utils.format(player.getLoyaltyManager().getPoints())).append("<br>");
		bldr.append("<br><br>");
		bldr.append("Gold Points: <col=" + ChatColors.WHITE + ">" + Utils.format(player.getFacade().getGoldPoints()) + "<br>");
		bldr.append("Total Points Purchased: <col=" + ChatColors.WHITE + ">" + Utils.format(player.getFacade().getTotalPointsPurchased()) + "<br>");
		return bldr.toString();
	}

	@Override
	public void finish() {

	}
}
