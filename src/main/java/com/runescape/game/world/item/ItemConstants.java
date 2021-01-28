package com.runescape.game.world.item;

import com.runescape.game.GameConstants;
import com.runescape.game.interaction.controllers.impl.Wilderness;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleMessage;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.QuestManager;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.game.world.entity.player.achievements.AbstractAchievement;
import com.runescape.game.world.entity.player.achievements.AchievementHandler;
import com.runescape.game.world.entity.player.quests.Quest;
import com.runescape.utility.ChatColors;

public class ItemConstants {

	/** If an item dropped has any of these keywords in its name, everyone will see the drop notification. */
	private static final String[] DROP_ANNOUNCEMENT_ITEMS = new String[] { "infinity", "mages'", "saradomin sword", "hilt", "whip", "visage", "vesta", "statius", "zuriel", "morrigan", "virtus", "morrigan", "pernix", "torva", "virtus", "sigil", "steadfast", "ragefire", "glaiven", "elixir", "third age", "dragon claw", "armadyl", "trickster", "battle-mage", "zanik" };

	/**
	 * Several items must pass checks before players can weild them. This method does that
	 *
	 * @param player
	 * 		The player
	 * @param item
	 * 		The item
	 */
	public static boolean canWear(Player player, Item item) {
		if (!QuestManager.handleItemEquipping(player, item.getId())) {
			return false;
		} else if (item.getId() == 20767 || item.getId() == 20768) {
			int maxedInformation = player.getSkills().isMaxed();
			if (maxedInformation == -1) {
				return true;
			} else {
				String message = "You are not maxed. You must level your <col=" + ChatColors.MAROON + ">" + Skills.SKILL_NAME[maxedInformation] + "</col> level to 99 first.";
				player.getDialogueManager().startDialogue(SimpleMessage.class, message);
				player.sendMessage(message);
				return false;
			}
		} else if (item.getName().toLowerCase().contains("completionist")) {
			int maxedInformation = player.getSkills().isMaxed();
			if (maxedInformation == -1) {
				AbstractAchievement achievement = AchievementHandler.finishedAllAchievements(player);
				if (achievement == null) {
					Quest<?> quest = player.getQuestManager().finishedAllQuests();
					if (quest == null) {
						return true;
					} else {
						String message = "You have not finished every quest.<br>You must finish quest: <col=" + ChatColors.MAROON + ">" + quest.getName() + ".";
						player.getDialogueManager().startDialogue(SimpleMessage.class, message);
						player.sendMessage(message);
						return false;
					}
				} else {
					String message = "You have not finished all achievements.<br>You must finish achievement: <col=" + ChatColors.MAROON + ">" + achievement.title() + ".";
					player.getDialogueManager().startDialogue(SimpleMessage.class, message);
					player.sendMessage(message);
					return false;
				}
			} else {
				String message = "You are not maxed. You must level your <col=" + ChatColors.MAROON + ">" + Skills.SKILL_NAME[maxedInformation] + "</col> level to 99 first.";
				player.getDialogueManager().startDialogue(SimpleMessage.class, message);
				player.sendMessage(message);
				return false;
			}
		} else if (item.getName().toLowerCase().contains("master cape")) {
			String skillName = item.getName().split(" ")[0].trim();
			int skillId = -1;
			for (int i = 0; i < Skills.SKILL_NAME.length; i++) {
				if (Skills.SKILL_NAME[i].equalsIgnoreCase(skillName)) {
					skillId = i;
					break;
				}
			}
			if (skillId == -1) {
				System.err.println("Couldn't find skill necessary for " + item);
				return GameConstants.DEBUG;
			}
			if (player.getSkills().getXp(skillId) < Skills.MAXIMUM_EXP) {
				player.sendMessage("You need to have 200M experience in this " + skillName + " to wield this cape.");
				return false;
			}
		} else if (item.getName().contains("(broken")) {
			player.sendMessage("You cannot equip a broken weapon.");
			return false;
		}
		return true;
	}

	/**
	 * In order to find if an item is legible to be traded, this method checks several things
	 *
	 * @param item
	 * 		The item
	 */
	public static boolean isTradeable(Item item) {
		switch (item.getId()) {
			case 21477:
			case 21478:
			case 21479:
			case Wilderness.WILDERNESS_TOKEN:
			case 13663:
			case 18344:
			case 3709:
			case 13727:
			case 18768:
			case 6199:
				return true;
			case 4447:
			case 12158:
			case 12159:
			case 12160:
			case 12161:
			case 12162:
			case 12163:
			case 12164:
			case 12165:
			case 12166:
			case 12167:
			case 12168:
				return false;
		}
		return !(ItemProperties.isUntradeable(item) || item.getDefinitions().isDestroyItem() || item.getDefinitions().isLended());
	}

	/**
	 * This method checks for untradeables that should drop on death.
	 *
	 * @param item
	 * 		The item
	 */
	public static boolean untradeableDropsOnDeath(Item item) {
		switch (item.getId()) {
			case 13860:
			case 13863:
			case 13866:
			case 13869:
			case 13872:
			case 13875:
			case 13878:
			case 13886:
			case 13889:
			case 13892:
			case 13895:
			case 13898:
			case 13901:
			case 13904:
			case 13907:
			case 13910:
			case 13913:
			case 13916:
			case 13919:
			case 13922:
			case 13925:
			case 13928:
			case 13931:
			case 13934:
			case 13937:
			case 13940:
			case 13943:
			case 13946:
			case 13949:
			case 13952:
			case 4856:
			case 4857:
			case 4858:
			case 4859:
			case 4862:
			case 4863:
			case 4864:
			case 4865:
			case 4868:
			case 4869:
			case 4870:
			case 4871:
			case 4874:
			case 4875:
			case 4876:
			case 4877:
			case 4880:
			case 4881:
			case 4882:
			case 4883:
			case 4886:
			case 4887:
			case 4888:
			case 4889:
			case 4892:
			case 4893:
			case 4894:
			case 4895:
			case 4898:
			case 4899:
			case 4900:
			case 4901:
			case 4904:
			case 4905:
			case 4906:
			case 4907:
			case 4910:
			case 4911:
			case 4912:
			case 4913:
			case 4916:
			case 4917:
			case 4918:
			case 4919:
			case 4922:
			case 4923:
			case 4924:
			case 4925:
			case 4928:
			case 4929:
			case 4930:
			case 4931:
			case 4934:
			case 4935:
			case 4936:
			case 4937:
			case 4940:
			case 4941:
			case 4942:
			case 4943:
			case 4946:
			case 4947:
			case 4948:
			case 4949:
			case 4952:
			case 4953:
			case 4954:
			case 4955:
			case 4958:
			case 4959:
			case 4960:
			case 4961:
			case 4964:
			case 4965:
			case 4966:
			case 4967:
			case 4970:
			case 4971:
			case 4972:
			case 4973:
			case 4976:
			case 4977:
			case 4978:
			case 4979:
			case 4982:
			case 4983:
			case 4984:
			case 4985:
			case 4988:
			case 4989:
			case 4990:
			case 4991:
			case 4994:
			case 4995:
			case 4996:
			case 4997:
			/*case 12158:
			case 12159:
			case 12160:
			case 12161:
			case 12162:
			case 12163:
			case 12164:
			case 12165:
			case 12166:
			case 12167:
			case 12168:*/
				return true;
			default:
				return false;
		}
	}

	/**
	 * Gets the degraded barrows id for an item
	 *
	 * @param item
	 * 		The item
	 */
	public static int getBarrowsDegradedId(Item item) {
		if (item.getDefinitions().isNoted()) {
			return -1;
		}
		String itemName = item.getName().toLowerCase();
		if (itemName.contains("ahrim")) {
			if (itemName.contains("staff")) { return 4866; }
			if (itemName.contains("skirt")) { return 4878; }
			if (itemName.contains("top")) { return 4872; }
			if (itemName.contains("hood")) { return 4860; }
		}
		if (itemName.contains("guthan")) {
			if (itemName.contains("spear")) { return 4914; }
			if (itemName.contains("skirt")) { return 4926; }
			if (itemName.contains("body")) { return 4920; }
			if (itemName.contains("helm")) { return 4908; }
		}
		if (itemName.contains("torag")) {
			if (itemName.contains("hammers")) { return 4962; }
			if (itemName.contains("legs")) { return 4974; }
			if (itemName.contains("body")) { return 4968; }
			if (itemName.contains("helm")) { return 4956; }
		}
		if (itemName.contains("dharok")) {
			if (itemName.contains("axe")) { return 4890; }
			if (itemName.contains("legs")) { return 4902; }
			if (itemName.contains("body")) { return 4896; }
			if (itemName.contains("helm")) { return 4884; }
		}
		if (itemName.contains("verac")) {
			if (itemName.contains("flail")) { return 4986; }
			if (itemName.contains("skirt")) { return 4998; }
			if (itemName.contains("top") || itemName.contains("brassard")) { return 4992; }
			if (itemName.contains("helm")) { return 4980; }
		}
		if (itemName.contains("karil")) {
			if (itemName.contains("bow")) { return 4938; }
			if (itemName.contains("skirt")) { return 4950; }
			if (itemName.contains("top")) { return 4944; }
			if (itemName.contains("coif")) { return 4932; }
		}
		return -1;
	}

	/**
	 * Finding out if an item is a rare item that is dropped from monsters
	 *
	 * @param item
	 * 		The item to check for
	 */
	public static boolean isRare(Item item) {
		String name = item.getName().toLowerCase();
		for (String rare : DROP_ANNOUNCEMENT_ITEMS) {
			if (name.toLowerCase().contains(rare.toLowerCase())) {
				return sendsGlobalMessage(item);
			}
		}
		return false;
	}

	/**
	 * Whether the global message is sent for this item
	 *
	 * @param item
	 * 		The item
	 */
	private static boolean sendsGlobalMessage(Item item) {
		String name = item.getName().toLowerCase();
		if (name.contains("shards") || name.contains("javelin") || name.contains("throwing axe")) {
			return false;
		}
		return true;
	}

	public static boolean destroysOnDrop(Item item) {
		switch (item.getId()) {
			case 2412:
			case 2413:
			case 2414:
			case 3840:
			case 3842:
			case 3844:
			case 7453:
			case 7454:
			case 7455:
			case 7456:
			case 7457:
			case 7458:
			case 7459:
			case 19613:
			case 19615:
			case 19617:
				return true;
		}
		return false;
	}
}
