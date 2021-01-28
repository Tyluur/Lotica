package com.runescape.game.event.interaction.button;

import com.runescape.game.content.global.loyalty.LoyaltyRewards;
import com.runescape.game.event.interaction.type.InterfaceInteractionEvent;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.ChatColors;
import com.runescape.utility.Utils;

import java.util.Optional;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 11/8/2015
 */
public class LoyaltyInterfaceInteractionEvent extends InterfaceInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] { 825 };
	}

	@Override
	public boolean handleInterfaceInteraction(Player player, int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
		LoyaltyPages page = player.getAttribute("current_loyalty_page");
		if (page == null) {
			return false;
		}
		int indexId = getIndexOfComponent(componentId);

		Optional<LoyaltyPages[]> alternatePages = page.alternatePagesButtons();
		// checking if we're tapping the back/next button
		if (alternatePages.isPresent() && indexId >= page.options.length) {
			boolean backButton = indexId == page.options.length;
			LoyaltyPages[] pages = alternatePages.get();
			if (backButton) {
				if (pages.length >= 1) {
					showPage(player, pages[0]);
				}
			} else {
				if (pages.length >= 2) {
					showPage(player, pages[1]);
				}
			}
		} else {
			page.clickIndex(player, indexId);
		}
//		System.out.println(componentId + ", " + page + ", " + indexId + ", " + page.options.length);
		return true;
	}

	/**
	 * Displays the interface to the player
	 *
	 * @param player
	 * 		The player
	 */
	public static void displayInterface(Player player) {
		showPage(player, LoyaltyPages.HOMEPAGE);
	}

	/**
	 * Gets the index of the component based on the {@link #PAGE_COMPONENT_IDS} third slot
	 *
	 * @param componentId
	 * 		The component
	 */
	private static int getIndexOfComponent(int componentId) {
		for (int i = 0; i < PAGE_COMPONENT_IDS.length; i++) {
			if (PAGE_COMPONENT_IDS[i][2] == componentId) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Shows the page to the player
	 *
	 * @param player
	 * 		The player
	 * @param page
	 * 		The page
	 */
	private static void showPage(Player player, LoyaltyPages page) {
		player.closeInterfaces();
		int interfaceId = 825;
		int componentLength = Utils.getInterfaceDefinitionsComponentsSize(interfaceId);
		for (int i = 0; i < componentLength; i++) {
			player.getPackets().sendIComponentText(interfaceId, i, "");
		}
		player.getPackets().sendIComponentText(interfaceId, 29, "Loyalty Point Store");
		String[] options = page.options;
		int lastIndex = -1;
		for (int i = 0; i < options.length; i++) {
//			System.out.println("last sent: " + options[i]);
			player.getPackets().sendIComponentText(interfaceId, PAGE_COMPONENT_IDS[i][0], "" + (i + 1));
			player.getPackets().sendIComponentText(interfaceId, PAGE_COMPONENT_IDS[i][1], "" + options[i]);
			lastIndex = i;
		}
		Optional<LoyaltyPages[]> alternatePages = page.alternatePagesButtons();
		// first back, then next
		if (alternatePages.isPresent()) {
			LoyaltyPages[] pages = alternatePages.get();
			LoyaltyPages back = null, next = null;
			if (pages.length >= 1) {
				back = pages[0];
			}
			if (pages.length >= 2) {
				next = pages[1];
			}
			if (back != null) {
				player.getPackets().sendIComponentText(interfaceId, PAGE_COMPONENT_IDS[lastIndex + 1][0], "" + (lastIndex + 2));
				player.getPackets().sendIComponentText(interfaceId, PAGE_COMPONENT_IDS[lastIndex + 1][1], "<col=" + ChatColors.YELLOW + ">Back");
			}
			if (next != null) {
				player.getPackets().sendIComponentText(interfaceId, PAGE_COMPONENT_IDS[lastIndex + 2][0], "" + (lastIndex + 3));
				player.getPackets().sendIComponentText(interfaceId, PAGE_COMPONENT_IDS[lastIndex + 2][1], "<col=" + ChatColors.YELLOW + ">Next");
			}
		}
		refreshPoints(player);
		player.putAttribute("current_loyalty_page", page);
		player.getInterfaceManager().sendInterface(interfaceId);
	}

	/**
	 * Refreshes the loyalty point text on the interface
	 *
	 * @param player
	 * 		The player
	 */
	private static void refreshPoints(Player player) {
		int interfaceId = 825;
		player.getPackets().sendIComponentText(interfaceId, 54, "Points");
		player.getPackets().sendIComponentText(interfaceId, 55, "" + Utils.format(player.getLoyaltyManager().getPoints()));
	}

	/**
	 * Refreshes the reward text on the interface
	 *
	 * @param player
	 * 		The player
	 * @param reward
	 * 		The reward
	 */
	private static void refreshReward(Player player, LoyaltyRewards reward) {
		int interfaceId = 825;
		player.getPackets().sendIComponentText(interfaceId, 66, reward == null ? "" : reward.getName());
		player.getPackets().sendIComponentText(interfaceId, 67, reward == null ? "" : reward.getDescription());
		player.getPackets().sendIComponentText(interfaceId, 69, reward == null ? "" : "Cost: " + Utils.format(reward.getPointCost()));
	}

	/**
	 * Prepares the player to purchase a loyalty reward
	 *
	 * @param player
	 * 		The player
	 * @param reward
	 * 		The reward
	 */
	private static void preparePurchase(Player player, LoyaltyRewards reward) {
		if (player.getLoyaltyManager().purchasedReward(reward)) {
			player.getDialogueManager().startDialogue(new Dialogue() {
				@Override
				public void start() {
					sendDialogue("You have already purchased this reward so you have an option to reclaim it.", "Do you wish to do so?");
				}

				@Override
				public void run(int interfaceId, int option) {
					switch (stage) {
						case -1:
							sendOptionsDialogue(DEFAULT_OPTIONS, "Yes, I wish to reclaim this reward.", "No, never mind.");
							stage = 0;
							break;
						case 0:
							if (option == FIRST) {
								if (!reward.hasReward(player)) {
									sendDialogue("You have just reclaimed your reward.");
									reward.accept(player);
								} else {
									sendPlayerDialogue(CALM, "I already have this reward, no need to reclaim it...", "It must be in my inventory, equipment, or bank.");
								}
								stage = -2;
							} else {
								end();
							}
							break;
					}
				}

				@Override
				public void finish() {
					refreshReward(player, null);
					refreshPoints(player);
				}
			});
		} else {
			player.getDialogueManager().startDialogue(new Dialogue() {
				@Override
				public void start() {
					sendDialogue("Purchase " + reward.getName() + " for " + reward.getPointCost() + " loyalty points?");
				}

				@Override
				public void run(int interfaceId, int option) {
					switch (stage) {
						case -1:
							sendOptionsDialogue(DEFAULT_OPTIONS, "Yes, please.", "No, thank you.");
							stage = 0;
							break;
						case 0:
							if (option == FIRST) {
								if (reward.canPurchase(player)) {
									sendPlayerDialogue(CALM, "Thank you!");
									player.getLoyaltyManager().addPurchasedReward(reward);
									player.getLoyaltyManager().setPoints(player.getLoyaltyManager().getPoints() - reward.getPointCost());
									reward.accept(player);
								} else {
									sendPlayerDialogue(SAD, "I don't have enough loyalty points...");
								}
								stage = -2;
							} else {
								end();
							}
							break;
					}
				}

				@Override
				public void finish() {
					refreshReward(player, null);
					refreshPoints(player);
				}
			});
		}
		refreshReward(player, reward);
	}

	private enum LoyaltyPages {

		HOMEPAGE("Auras", "Titles", "Costumes", "Emotes") {
			@Override
			public void clickIndex(Player player, int index) {
				switch (index) {
					case 0:
						showPage(player, AURA_HOMEPAGE_SELECTION);
						break;
					case 1:
						showPage(player, TITLES_PAGE_SELECTION);
						break;
					case 2:
					case 3:
						player.sendMessage("These will be added soon.");
						break;
				}
			}

			@Override
			public Optional<LoyaltyPages[]> alternatePagesButtons() {
				return Optional.empty();
			}
		},

		AURA_HOMEPAGE_SELECTION("Tier 1 Auras", "Tier 2 Auras", "Tier 3 Auras") {
			@Override
			public void clickIndex(Player player, int index) {
				switch (index) {
					case 0:
						showPage(player, AURA_TIER_1);
						break;
					case 1:
						showPage(player, AURA_TIER_2);
						break;
					case 2:
						showPage(player, AURA_TIER_3);
						break;
				}
			}

			@Override
			public Optional<LoyaltyPages[]> alternatePagesButtons() {
				return Optional.of(new LoyaltyPages[] { HOMEPAGE });
			}
		},

		AURA_TIER_1("Oddball", "Poison Purge", "Knock-Out", "Runic Accuracy", "Sharpshooter", "Quarrymaster", "Call of the sea", "Reverence", "Five-Finger Discount", "Lumberjack") {
			@Override
			public void clickIndex(Player player, int index) {
				LoyaltyRewards reward = LoyaltyRewards.getRewardByName(getOptions()[index]);
				if (reward == null) {
					throw new IllegalStateException("Could not find reward by name: " + getOptions()[index]);
				}
				preparePurchase(player, reward);
			}

			@Override
			public Optional<LoyaltyPages[]> alternatePagesButtons() {
				return Optional.of(new LoyaltyPages[] { AURA_HOMEPAGE_SELECTION });
			}
		},

		AURA_TIER_2("Greater Poison Purge", "Greater Call Of The Sea", "Greater Lumberjack", "Greater Quarrymaster", "Greater Five Finger Discount", "Greater Reverence", "Greater Sharpshooter") {
			@Override
			public void clickIndex(Player player, int index) {
				LoyaltyRewards reward = LoyaltyRewards.getRewardByName(getOptions()[index]);
				if (reward == null) {
					throw new IllegalStateException("Could not find reward by name: " + getOptions()[index]);
				}
				preparePurchase(player, reward);
			}

			@Override
			public Optional<LoyaltyPages[]> alternatePagesButtons() {
				return Optional.of(new LoyaltyPages[] { AURA_HOMEPAGE_SELECTION, AURA_TIER_2_SECOND });
			}
		},

		AURA_TIER_2_SECOND("Greater Runic Accuracy", "Equilibrium", "Vampyrism", "Penance", "Resourceful") {
			@Override
			public void clickIndex(Player player, int index) {
				LoyaltyRewards reward = LoyaltyRewards.getRewardByName(getOptions()[index]);
				if (reward == null) {
					throw new IllegalStateException("Could not find reward by name: " + getOptions()[index]);
				}
				preparePurchase(player, reward);
			}

			@Override
			public Optional<LoyaltyPages[]> alternatePagesButtons() {
				return Optional.of(new LoyaltyPages[] { AURA_TIER_1 });
			}

		},

		TITLES_PAGE_SELECTION("Basic Titles", "Advanced Titles", "Pro Titles") {
			@Override
			public void clickIndex(Player player, int index) {
				switch (index) {
					case 0:
						showPage(player, TITLES_PAGE_1);
						break;
					case 1:
						showPage(player, TITLES_PAGE_2);
						break;
					case 2:
						showPage(player, TITLES_PAGE_3);
						break;
				}
			}

			@Override
			public Optional<LoyaltyPages[]> alternatePagesButtons() {
				return Optional.of(new LoyaltyPages[] { LoyaltyPages.HOMEPAGE });
			}
		},

		TITLES_PAGE_1("Junior<br>Cadet", "Serjeant", "Commander", "War-Chief", "Sir", "Lord", "Duderino", "Lionheart", "Hellrasier", "Crusader", "Desparado") {
			@Override
			public void clickIndex(Player player, int index) {
				switch (index) {
					case 0:
						preparePurchase(player, LoyaltyRewards.JUNIOR_CADET_TITLE);
						break;
					case 1:
						preparePurchase(player, LoyaltyRewards.SERJEANT_TITLE);
						break;
					case 2:
						preparePurchase(player, LoyaltyRewards.COMMANDER_TITLE);
						break;
					case 3:
						preparePurchase(player, LoyaltyRewards.WAR_CHIEF_TITLE);
						break;
					case 4:
						preparePurchase(player, LoyaltyRewards.SIR_TITLE);
						break;
					case 5:
						preparePurchase(player, LoyaltyRewards.LORD_TITLE);
						break;
					case 6:
						preparePurchase(player, LoyaltyRewards.DUDERINO_TITLE);
						break;
					case 7:
						preparePurchase(player, LoyaltyRewards.LIONHEART_TITLE);
						break;
					case 8:
						preparePurchase(player, LoyaltyRewards.HELLRAISER_TITLE);
						break;
					case 9:
						preparePurchase(player, LoyaltyRewards.CRUSADER_TITLE);
						break;
					case 10:
						preparePurchase(player, LoyaltyRewards.DESPERADO);
						break;
				}
			}

			@Override
			public Optional<LoyaltyPages[]> alternatePagesButtons() {
				return Optional.of(new LoyaltyPages[] { TITLES_PAGE_SELECTION });
			}
		},

		TITLES_PAGE_2("Baron", "Count", "Overlord", "Bandito", "Duke", "King", "Big Cheese", "Bigwig", "Wunderkind", "Vyreling", "Vyre grunt") {
			@Override
			public void clickIndex(Player player, int index) {
				LoyaltyRewards reward = LoyaltyRewards.getRewardByName(getOptions()[index]);
				if (reward == null) {
					throw new IllegalStateException("Could not find reward by name: " + getOptions()[index]);
				}
				preparePurchase(player, reward);
			}

			@Override
			public Optional<LoyaltyPages[]> alternatePagesButtons() {
				return Optional.of(new LoyaltyPages[] { LoyaltyPages.TITLES_PAGE_SELECTION });
			}
		},

		TITLES_PAGE_3("Vyrewatch", "Vyrelord") {
			@Override
			public void clickIndex(Player player, int index) {
				LoyaltyRewards reward = LoyaltyRewards.getRewardByName(getOptions()[index]);
				if (reward == null) {
					throw new IllegalStateException("Could not find reward by name: " + getOptions()[index]);
				}
				preparePurchase(player, reward);
			}

			@Override
			public Optional<LoyaltyPages[]> alternatePagesButtons() {
				return Optional.of(new LoyaltyPages[] { LoyaltyPages.TITLES_PAGE_SELECTION });
			}
		},

		EMOTES_PAGE_SELECTION("Basic Emotes", "Advanced Emotes") {
			@Override
			public void clickIndex(Player player, int index) {

			}

			@Override
			public Optional<LoyaltyPages[]> alternatePagesButtons() {
				return Optional.of(new LoyaltyPages[] { LoyaltyPages.HOMEPAGE });
			}
		},

		EMOTES_PAGE_1("Goblin Bow", "Goblin Salute") {
			@Override
			public void clickIndex(Player player, int index) {

			}

			@Override
			public Optional<LoyaltyPages[]> alternatePagesButtons() {
				return Optional.of(new LoyaltyPages[] { LoyaltyPages.EMOTES_PAGE_SELECTION });
			}
		},

		AURA_TIER_3("Wisdom") {
			@Override
			public void clickIndex(Player player, int index) {
				LoyaltyRewards reward = LoyaltyRewards.getRewardByName(getOptions()[index]);
				if (reward == null) {
					throw new IllegalStateException("Could not find reward by name: " + getOptions()[index]);
				}
				preparePurchase(player, reward);
			}

			@Override
			public Optional<LoyaltyPages[]> alternatePagesButtons() {
				return Optional.of(new LoyaltyPages[] { AURA_HOMEPAGE_SELECTION });
			}
		},


		;

		/**
		 * The options that will be displayed on the loyalty page
		 */
		private final String[] options;

		LoyaltyPages(String... options) {
			this.options = options;
		}

		/**
		 * This method is called when an index is clicked on the page. This handles what happens next.
		 *
		 * @param player
		 * 		The player
		 * @param index
		 * 		The index
		 */
		public abstract void clickIndex(Player player, int index);

		/**
		 * The optional with the alternate pages buttons (next/back).
		 */
		public abstract Optional<LoyaltyPages[]> alternatePagesButtons();

		/**
		 * Gets the options
		 */
		public String[] getOptions() {
			return options;
		}

	}

	/**
	 * The component ids per box in the homepage
	 */
	private static final int[][] PAGE_COMPONENT_IDS = new int[][] { { 95, 96, 93 }, { 99, 100, 97 }, { 103, 104, 101 }, { 107, 108, 105 }, { 111, 112, 109 }, { 115, 116, 113 }, { 119, 120, 117 }, { 123, 124, 121 }, { 127, 128, 125 }, { 131, 132, 129 }, { 134, 135, 133 }, { 138, 139, 136 } };
}
