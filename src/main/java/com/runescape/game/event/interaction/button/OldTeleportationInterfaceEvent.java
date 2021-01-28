package com.runescape.game.event.interaction.button;

import com.runescape.game.content.skills.magic.Magic;
import com.runescape.game.event.interaction.type.InterfaceInteractionEvent;
import com.runescape.game.interaction.controllers.impl.FightCaves;
import com.runescape.game.interaction.controllers.impl.Wilderness;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.ForceTalk;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.ChatColors;
import com.runescape.utility.Utils;
import com.runescape.utility.world.Coordinates;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/27/2016
 */
public class OldTeleportationInterfaceEvent extends InterfaceInteractionEvent {

	/**
	 * The button ids of the interface
	 */
	public static final int[] BUTTONS = new int[] { 68, 73, 67, 72, 66, 71, 65, 70, 64, 69 };

	/**
	 * The lines of the interface
	 */
	public static final int[] LINES = new int[] { 31, 36, 32, 37, 33, 38, 34, 39, 35, 40 };

	/**
	 * The possible messages the wizard can say
	 */
	public static final String[] WIZARD_MESSAGES = new String[] { "Amitus! Setitii!", "Sparanti Morudo Calmentor!", "Daemonicas Abhoris!", "Senventior disthine molenko!" };

	@Override
	public int[] getKeys() {
		return new int[] { 72 };
	}

	@Override
	public boolean handleInterfaceInteraction(Player player, int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
		Pages page = player.getAttribute("travel_page");
		if (page == null) {
			return true;
		}
		int buttonIndex = -1;
		for (int i = 0; i < BUTTONS.length; i++) {
			if (componentId == BUTTONS[i]) {
				buttonIndex = i;
				break;
			}
		}
		if (buttonIndex == -1) {
			return true;
		}
		List<String> texts = page.titleTexts;
		if (buttonIndex >= texts.size()) {
			System.out.println("interfaceId = [" + interfaceId + "], componentId = [" + componentId + "], slotId = [" + slotId + "], slotId2 = [" + slotId2 + "], packetId = [" + packetId + "]");
			return true;
		}
		String option = texts.get(buttonIndex);
		if (option.equalsIgnoreCase("next")) {
			if (page.getNextButton().isPresent()) {
				openPage(player, page.getNextButton().get());
				return true;
			} else {
				System.out.println("next button existed with no page set..." + page);
			}
		} else if (option.equalsIgnoreCase("back")) {
			if (page.getBackButton().isPresent()) {
				openPage(player, page.getBackButton().get());
				return true;
			} else {
				System.out.println("back button existed with no page set..." + page);
			}
		} else if (!page.handleSlotClick(player, buttonIndex)) {
			Map<String, WorldTile> tiles = page.tiles;
			if (tiles.isEmpty()) {
				return true;
			}
			WorldTile tile = null;
			int slot = 0;
			for (Entry<String, WorldTile> entry : tiles.entrySet()) {
				if (slot == buttonIndex) {
					tile = entry.getValue();
					break;
				}
				slot++;
			}
			if (tile == null) {
				throw new IllegalStateException("No tile found for slot " + slot + " on page " + page);
			}
			teleport(player, tile, page, buttonIndex);
		}
		return true;
	}

	/**
	 * Opens a page
	 *
	 * @param player
	 * 		The player
	 * @param page
	 * 		The page to open
	 */
	private static void openPage(Player player, Pages page) {
		int interfaceId = 72;
		int index = 0;

		Utils.clearInterface(player, interfaceId);
		List<String> text = page.titleTexts;
		for (int line : LINES) {
			if (index >= text.size()) {
				continue;
			}
			String textShown = text.get(index);
			String color = textShown.equalsIgnoreCase("next") ? ChatColors.GREEN : textShown.equalsIgnoreCase("back") ? ChatColors.ORANGE : "";
			player.getPackets().sendIComponentText(interfaceId, line, "<col=" + color + ">" + textShown);
			index++;
		}
		for (int i = index; i < BUTTONS.length; i++) {
			player.getPackets().sendHideIComponent(interfaceId, BUTTONS[i], true);
		}

		player.getPackets().sendIComponentText(interfaceId, 55, "Select a Destination");
		player.getInterfaceManager().sendInterface(interfaceId);
		player.putAttribute("travel_page", page);
	}

	/**
	 * Teleports a player to the destination and handles post teleportation
	 *
	 * @param player
	 * 		The player
	 * @param destination
	 * 		The destination
	 * @param page
	 * 		The page we're on
	 * @param optionIndex
	 * 		The option index of the teleport
	 */
	private static void teleport(Player player, WorldTile destination, Pages page, int optionIndex) {
		if (Wilderness.isAtWild(destination)) {
			player.getDialogueManager().startDialogue(new Dialogue() {

				@Override
				public void start() {
					sendNPCDialogue(1263, HAPPY, "This destination is in the wilderness.", "Are you sure you wish to travel here?");
				}

				@Override
				public void run(int interfaceId, int option) {
					switch (stage) {
						case -1:
							sendOptionsDialogue(DEFAULT_OPTIONS, "Yes, I want to travel to a wilderness location.", "No, thanks for the notification!");
							stage = 0;
							break;
						case 0:
							if (option == FIRST) {
								teleportPlayer(player, destination, () -> page.handlePostTeleportation(player, optionIndex));
							}
							end();
							break;
					}
				}

				@Override
				public void finish() {
				}

			});
		} else {
			teleportPlayer(player, destination, () -> page.handlePostTeleportation(player, optionIndex));
		}
	}

	/**
	 * Teleports the player to the destination and performs some graphical things to make it look cool
	 *
	 * @param player
	 * 		The player
	 * @param destination
	 * 		The destination
	 * @param task
	 * 		The task to be performed once the teleport is done
	 */
	private static void teleportPlayer(Player player, WorldTile destination, Runnable task) {
		player.closeInterfaces();
		player.getLockManagement().lockAll(2000);
		NPC wizard = Utils.findLocalNPC(player, 1263);

		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				player.getLockManagement().unlockAll();
				Magic.sendTeleportSpell(player, 14293, -1, 94, -1, 0, 0, destination, 6, false, Magic.MAGIC_TELEPORT);
				player.setCloseInterfacesEvent(task);
			}
		}, 1);

		if (wizard == null) {
			return;
		}

		wizard.resetWalkSteps();
		wizard.setNextFaceWorldTile(player);
		wizard.setNextForceTalk(new ForceTalk(Utils.randomArraySlot(WIZARD_MESSAGES)));
	}

	/**
	 * Sends the destination selection interface by opening the {@link Pages#PAGE_SELECTION} page
	 *
	 * @param player
	 * 		The player
	 */
	public static void sendDestinationSelection(Player player) {
		openPage(player, Pages.PAGE_SELECTION);
	}

	private enum Pages implements Coordinates {

		PAGE_SELECTION("Monsters", "Skilling", "Minigames", "Dungeons", "Bosses", "Cities", "PvP ") {
			@Override
			public boolean handleSlotClick(Player player, int index) {
				switch (index) {
					case 0:
						openPage(player, MONSTER_LOCATIONS_FIRST);
						return true;
					case 1:
						openPage(player, SKILLING_LOCATIONS_FIRST);
						return true;
					case 2:
						openPage(player, MINIGAMES);
						return true;
					case 3:
						openPage(player, DUNGEONS);
						return true;
					case 4:
						openPage(player, BOSSES_FIRST);
						return true;
					case 5:
						openPage(player, CITIES_FIRST);
						return true;
					case 6:
						openPage(player, PVP);
						return true;
				}
				return false;
			}

		},

		MONSTER_LOCATIONS_FIRST {
			@Override
			public Pages loadTiles() {
				add("Rock Crabs", ROCK_CRABS, "Experiments", EXPERIMENTS, "Yaks", YAKS, "Bandits", BANDITS, "Moss Giants", MOSS_GIANTS, "Chaos Druids", DRUIDS, "Tzhaar", TZHAAR, "Dust Devils", DUST_DEVILS);
				return this;
			}

			@Override
			public Pages loadButtons() {
				setBackButton(Optional.of(PAGE_SELECTION));
				setNextButton(Optional.of(MONSTER_LOCATIONS_SECOND));
				return this;
			}
		},

		MONSTER_LOCATIONS_SECOND {
			@Override
			public Pages loadTiles() {
				add("Ape-Atoll Guards", MONKEY_GUARDS, "Armoured Zombies", ARMOURED_ZOMBIES, "Chaos Tunnels", CHAOS_TUNNELS, "Ice Giants", ICE_GIANTS, "Chickens", CHICKENS, "Monkey Skeletons", APE_ATOLL_DUNGEON);
				return this;
			}

			@Override
			public Pages loadButtons() {
				setBackButton(Optional.of(MONSTER_LOCATIONS_FIRST));
				return this;
			}
		},

		SKILLING_LOCATIONS_FIRST {
			@Override
			public Pages loadTiles() {
				add("Wilderness Resource Center", WILDERNESS_RESOURCE_CENTER, "Gnome Agility Course", GNOME_AGILITY, "Barbarian Agility Course", BARBARIAN_AGILITY, "Wilderness Agility Course", WILDERNESS_AGILITY, "The Abyss", ABYSS, "Miscellania", MISCELLANIA, "Draynor", DRAYNOR, "Fishing Guild", FISHING_GUILD, "Rogues' Den", ROGUES_DEN);
				return this;
			}

			@Override
			public Pages loadButtons() {
				setBackButton(Optional.of(PAGE_SELECTION));
				setNextButton(Optional.of(SKILLING_LOCATIONS_SECOND));
				return this;
			}
		},

		SKILLING_LOCATIONS_SECOND {
			@Override
			public Pages loadTiles() {
				add("Falador Mines", FALADOR_MINES, "Catherby Farming", new WorldTile(2817, 3460, 0), "Camelot", CAMELOT, "Crafting Guild", CRAFTING_GUILD, "Karamja", KARAMJA, "Essence Mine", ESSENCE_MINE, "Plank Making", LUMBER_YARD_PLANKS, "Living Rock Cavern", LIVING_ROCK_CAVERNS, "Hunter Training", HUNTER_TRAINING);
				return this;
			}

			@Override
			public Pages loadButtons() {
				setBackButton(Optional.of(SKILLING_LOCATIONS_FIRST));
				return this;
			}
		},

		MINIGAMES {
			@Override
			public Pages loadTiles() {
				add("Duel Arena", DUEL_ARENA, "Ancient Cavern", ANCIENT_CAVERN, "Pest Control", PEST_CONTROL, "Fight Caves", TZHAAR, "Barrows", BARROW, "Warriors Guild", WARRIORS_GUILD, "Clan Wars", CLAN_WARS, "Castle Wars", CASTLE_WARS);
				return this;
			}

			@Override
			public Pages loadButtons() {
				setBackButton(Optional.of(PAGE_SELECTION));
				return this;
			}
		},

		BOSSES_FIRST {
			@Override
			public Pages loadTiles() {
				add("Nex", NEX_DUNGEON, "Godwars", GODWARS_DUNGEON, "Glacors", GLACOR_DUNGEON, "Kalphite Queen", KALPHITE_QUEEN, "King Black Dragon", KING_BLACK_DRAGON, "Chaos Elemental", CHAOS_ELEMENTAL, "Frost Dragons", FROST_DRAGONS, "Tormented Demons", TORMENTED_DEMONS);
				return this;
			}

			@Override
			public Pages loadButtons() {
				setBackButton(Optional.of(PAGE_SELECTION));
				setNextButton(Optional.of(BOSSES_SECOND));
				return this;
			}

			@Override
			public void handlePostTeleportation(Player player, int index) {
				switch (index) {
					case 0:
					case 1:
						player.getControllerManager().startController("GodWars");
						break;
				}
			}
		},

		BOSSES_SECOND {
			@Override
			public Pages loadTiles() {
				add("Dagannoth Kings", DAGANNOTH_KINGS, "Corporeal Beast", CORPOREAL_BEAST, "Ice Strykwyrms", STRYKEWYRM_DUNGEON, "Sea Troll Queen", SEA_TROLL_QUEEN, "Bork", BORK);
				return this;
			}

			@Override
			public Pages loadButtons() {
				setBackButton(Optional.of(BOSSES_FIRST));
				return this;
			}
		},

		CITIES_FIRST {
			@Override
			public Pages loadTiles() {
				add("Varrock", VARROCK, "Falador", FALADOR, "Camelot", CAMELOT, "Draynor", DRAYNOR, "Catherby", CATHERBY, "Al Kharid", AL_KHARID, "Lumbridge", LUMBRIDGE, "Neitiznot", NEITIZNOT);
				return this;
			}

			@Override
			public Pages loadButtons() {
				setBackButton(Optional.of(PAGE_SELECTION));
				setNextButton(Optional.of(CITIES_SECOND));
				return this;
			}
		},

		CITIES_SECOND {
			@Override
			public Pages loadTiles() {
				add("Ardougne", ARDOUGNE, "Relleka", RELLEKKA, "Miscellania", MISCELLANIA, "The Grand Tree", GRAND_TREE, "Yanille", YANILLE, "Watchtower", WATCHTOWER, "Tzhaar City", FightCaves.OUTSIDE);
				return this;
			}

			@Override
			public Pages loadButtons() {
				setBackButton(Optional.of(CITIES_FIRST));
				return this;
			}
		},

		PVP {
			@Override
			public Pages loadTiles() {
				add("Mage Bank", MAGE_BANK, "Deserted Keep", DESERTED_KEEP);
				return this;
			}

			@Override
			public Pages loadButtons() {
				setBackButton(Optional.of(PAGE_SELECTION));
				return this;
			}
		},

		DUNGEONS {
			@Override
			public Pages loadTiles() {
				add("Taverly Dungeon", TAVERLY_DUNGEON, "Fremennik Slayer Dungeon", FREMENNIK_SLAYER_DUNGEON, "Brimhaven Dungeon", BRIMHAVEN_DUNGEON, "Kuradal's Dungeon", KURADAL_SLAYER_DUNGEON, "Asgarnian Dungeon", ASGARNIAN_ICE_DUNGEON, "Slayer Tower", SLAYER_TOWER, "Jadinko Lair", JADINKO_LAIR);
				return this;
			}

			@Override
			public Pages loadButtons() {
				setBackButton(Optional.of(PAGE_SELECTION));
				return this;
			}
		};

		static {
			Arrays.stream(Pages.values()).forEach(page -> page.loadTiles().loadButtons().registerTiles().registerButtons());
		}

		/**
		 * The list of all titleTexts for buttons
		 */
		private final List<String> titleTexts = new ArrayList<>();

		/**
		 * The tiles of this page, the keys being the name of the destinations
		 */
		private final Map<String, WorldTile> tiles = new LinkedHashMap<>();

		private Optional<Pages> nextButton = Optional.empty();

		private Optional<Pages> backButton = Optional.empty();

		Pages(String... titles) {
			Collections.addAll(this.titleTexts, titles);
		}

		/**
		 * Adds the destination to the map
		 *
		 * @param params
		 * 		The array of infinite String, WorldTile parameters
		 */
		protected boolean add(Object... params) {
			for (int i = 0; i < params.length; i++) {
				Object param = params[i];
				if (param instanceof String) {
					String name = (String) param;
					WorldTile tile = (WorldTile) params[i + 1];
					tiles.put(name, tile);
				}
			}
			return true;
		}

		/**
		 * This method is overriden and is used to load all the tiles of the page
		 */
		public Pages loadTiles() { return this; }
		
		/**
		 * This method is overriden if next/back buttons should be added to the page
		 */
		public Pages loadButtons() { return this; }
		
		/**
		 * Registers the buttons into the {@link #titleTexts}
		 */
		private Pages registerButtons() {
			getNextButton().ifPresent(next -> {
				if (titleTexts.size() >= 10) {
					System.err.println("Next button didn't have space to show on " + name() + ", remove elements.");
				} else {
					titleTexts.add("Next");
				}
			});
			getBackButton().ifPresent(back -> {
				if (titleTexts.size() >= 10) {
					System.err.println("Back button didn't have space to show on " + name() + ", remove elements.");
				} else {
					titleTexts.add("Back");
				}
			});
			return this;
		}
		
		/**
		 * Registers all the tile names into the {@link #titleTexts}
		 */
		private Pages registerTiles() {
			tiles.entrySet().forEach(entry -> titleTexts.add(entry.getKey()));
			return this;
		}

		/**
		 * Handling a teleporation for a custom index in the {@link #tiles}
		 *
		 * @param player
		 * 		The player teleporting
		 * @param index
		 * 		The index of the teleport
		 */
		public void handlePostTeleportation(Player player, int index) {

		}

		/**
		 * Handles the click of a slot
		 *
		 * @param player
		 * 		The player
		 * @param index
		 * 		The slot
		 */
		public boolean handleSlotClick(Player player, int index) {
			return false;
		}

        public Optional<Pages> getNextButton() {
            return this.nextButton;
        }

        public Optional<Pages> getBackButton() {
            return this.backButton;
        }

        public void setNextButton(Optional<Pages> nextButton) {
            this.nextButton = nextButton;
        }

        public void setBackButton(Optional<Pages> backButton) {
            this.backButton = backButton;
        }
    }
}
