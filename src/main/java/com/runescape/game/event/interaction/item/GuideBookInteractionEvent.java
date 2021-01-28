package com.runescape.game.event.interaction.item;

import com.runescape.game.GameConstants;
import com.runescape.game.event.interaction.button.Scrollable;
import com.runescape.game.event.interaction.type.ItemInteractionEvent;
import com.runescape.game.world.entity.player.Equipment;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.utility.ChatColors;
import com.runescape.utility.world.ClickOption;
import com.runescape.utility.world.player.StarterList;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 6/29/2015
 */
public class GuideBookInteractionEvent extends ItemInteractionEvent {
	
	@Override
	public int[] getKeys() {
		return new int[] { 1856 };
	}

	@Override
	public boolean handleItemInteraction(Player player, Item item, ClickOption option) {
		giveStarter(player);
		showTutorial(player);
		return true;
	}

	/**
	 * Giving the player the starter setup
	 *
	 * @param player
	 * 		The player
	 */
	private void giveStarter(Player player) {
		// players only receive 1 starter per account, no matter how many times they open this book
		if (player.getFacade().isReceivedStarter()) {
			return;
		}
		/** the mac address can only be used for {@link StarterList#MAX_STARTERS_PER_ADDRESS} starters */
		if (StarterList.canReceiveStarter(player.getMacAddress())) {
			int starterCount = StarterList.getStartersReceived(player.getMacAddress());
			for (Item item : STARTER) {
				if (item.getId() == 995) {
					item.setAmount(starterCount == 0 ? 250_000 : starterCount == 1 ? 150_000 : item.getAmount());
				}
				player.getInventory().addItem(item);
			}
			for (Item item : player.getEquipment().getItems().toArray()) {
				if (item == null) {
					continue;
				}
				player.getBank().addItem(item.getId(), item.getAmount(), true);
				player.sendMessage("Your " + item.getName() + " has been banked.");
			}
			player.setCloseInterfacesEvent(() -> {
				for (Object[] equipment : EQUIPMENT) {
					byte slotId = (byte) equipment[0];
					Item item = (Item) equipment[1];
					player.getEquipment().getItems().set(slotId, item);
					player.getEquipment().refresh(slotId);
				}
				player.getAppearence().generateAppearenceData();
			});
			StarterList.insertStarter(player.getMacAddress());
			player.getFacade().setReceivedStarter(true);
		} else {
			player.sendMessage("You can only receive " + StarterList.MAX_STARTERS_PER_ADDRESS + " starters from your address.");
		}
	}

	/**
	 * The starter kit items
	 */
	private static final Item[] STARTER = new Item[] { new Item(995, 1_000_000), new Item(841), new Item(861),
			                                                 new Item(884, 500), new Item(1169), new Item(1129),
			                                                 new Item(1095), new Item(1099), new Item(579),
			                                                 new Item(577), new Item(1011), new Item(1381),
			                                                 new Item(556, 1_000), new Item(554, 1_000),
			                                                 new Item(555, 1_000), new Item(557, 1_000),
			                                                 new Item(558, 500), new Item(562, 400),
			                                                 new Item(8013, 100), new Item(386, 500) };

	/**
	 * The equipment the player will equip as a result of their starter
	 */
	private static final Object[][] EQUIPMENT = new Object[][] {
			                                                           { Equipment.SLOT_HAT, new Item(1153)},
			                                                           { Equipment.SLOT_WEAPON, new Item(9703)},
			                                                           { Equipment.SLOT_CAPE, new Item(4373)},
			                                                           { Equipment.SLOT_AMULET, new Item(1712)},
			                                                           { Equipment.SLOT_CHEST, new Item(1101)},
			                                                           { Equipment.SLOT_LEGS, new Item(1067)},
			                                                           { Equipment.SLOT_SHIELD, new Item(8844)},
			                                                           { Equipment.SLOT_HANDS, new Item(11118)},
			                                                           { Equipment.SLOT_FEET, new Item(3105)},

	};

	/**
	 * Showing the tutorial message to the player
	 *
	 * @param player
	 * 		The player
	 */
	private void showTutorial(Player player) {
		String[] tutorial = new String[] { "<col=" + ChatColors.MAROON + ">Hello, " + player.getDisplayName() + ", welcome to " + GameConstants.SERVER_NAME + "!",
				                                 "I'm sure you have some questions about the game, and this book has the answers.",
				                                 "", "<col=" + ChatColors.BLUE + ">Where do I make money?",
				                                 "You can start by thieving from the stalls at home.", "",
				                                 "<col=" + ChatColors.BLUE + ">How do I teleport around the world?",
				                                 "Mr. Ex at home can teleport you wherever you want to go, simply talk to him.",
				                                 "",
				                                 "<col=" + ChatColors.BLUE + ">Where can I buy armour, food, and supplies?",
				                                 "In the north-western building in Edgeville, you will find a lot of shops there.",
				                                 "", "<col=" + ChatColors.BLUE + ">Where do I train skills?",
				                                 "Summoning is trained by climbing down the ladder west of edgeville bank, construction is trained by entering the portal north of edgeville bank, and the other skills can be found in Mr. Ex's teleportation locations.",
				                                 "", "<col=" + ChatColors.BLUE + ">How do I open armour sets?",
				                                 "Use the armour set on any deposit box around the world to open the set.",
				                                 "<col=" + ChatColors.BLUE + ">How do I claim my donation?",
				                                 "Click your navigation tab and hit 'Claim Donations' or type ::claim.",
				                                 "",
				                                 "<col=" + ChatColors.BLUE + ">How do I claim my auth codes from voting?",
				                                 "Use the command ::auth followed by your auth code to claim your rewards.",
				                                 "",
				                                 "<col=" + ChatColors.BLUE + ">How do I exchange vote points/gold points for rewards?",
				                                 "Speak to the rewards trader at home for the shop.", "", };
		Scrollable.sendScroll(player, "Guide Book", tutorial);
	}
}
