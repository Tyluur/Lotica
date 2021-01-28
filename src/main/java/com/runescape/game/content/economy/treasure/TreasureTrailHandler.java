package com.runescape.game.content.economy.treasure;

import com.runescape.game.GameConstants;
import com.runescape.game.content.economy.treasure.TreasureTrailData.TrailActionType;
import com.runescape.game.content.economy.treasure.TreasureTrailData.TreasureTrailTier;
import com.runescape.game.content.economy.treasure.TreasureTrailData.TreasureTrailType;
import com.runescape.game.content.economy.treasure.type.AbstractActionTrail;
import com.runescape.game.content.economy.treasure.type.AbstractCoordinateTrail;
import com.runescape.game.content.economy.treasure.type.AbstractEmoteTrail;
import com.runescape.game.content.economy.treasure.type.AbstractTreasureTrail;
import com.runescape.game.event.interaction.button.Scrollable;
import com.runescape.game.interaction.dialogues.impl.item.SimpleItemMessage;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.EmotesManager.Emotes;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AchievementHandler;
import com.runescape.game.world.entity.player.achievements.easy.Novice_Clue_Finisher;
import com.runescape.game.world.item.Item;
import com.runescape.utility.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 16, 2015
 */
public class TreasureTrailHandler {

	/**
	 * All {@link AbstractTreasureTrail}s must be loaded into the
	 */
	public static void loadAll() {
		for (String directory : Utils.getSubDirectories(AbstractCoordinateTrail.class)) {
			try {
				treasureTrails.addAll(Utils.getClassesInDirectory(AbstractCoordinateTrail.class.getPackage().getName() + "." + directory).stream().map(clazz -> (AbstractTreasureTrail) clazz).collect(Collectors.toList()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Handles the reading over a clue scroll
	 *
	 * @param player
	 * 		The player
	 * @param itemId
	 * 		The id of the scroll
	 */
	public static boolean read(Player player, int itemId) {
		TreasureTrailTier tier = getTier(itemId);
		if (tier == null) {
			return false;
		}
		TreasureTrailData data = player.getFacade().getTrailData();
		AbstractTreasureTrail trail = getTrailByName(data.getTrailByTier(tier));
		// the player doesnt have trail information for this yet, we must
		// generate and store
		if (trail == null) {
			giveTrail(player, data, tier, true);
			return read(player, itemId);
		} else {
			int componentLength = Utils.getInterfaceDefinitionsComponentsSize(trail.interfaceId());
			for (int i = 0; i < componentLength; i++) {
				player.getPackets().sendIComponentText(trail.interfaceId(), i, "");
			}
			switch (trail.type()) {
				case COORDINATE:
					AbstractCoordinateTrail coordinateTrail = (AbstractCoordinateTrail) trail;
					String[] split = coordinateTrail.information().split(",");
					// we must first delete all the text from the interface
					Utils.clearInterface(player, trail.interfaceId());
					int component = 4;
					for (String coordinate : split) {
						player.getPackets().sendIComponentText(trail.interfaceId(), component, coordinate);
						component++;
					}
					break;
				case ACTION:
					Scrollable.sendQuestScroll(player, "Treasure Trail Information", ((AbstractActionTrail) trail).information());
					break;
				case EMOTE:
					Scrollable.sendQuestScroll(player, "Treasure Trail Information", ((AbstractEmoteTrail) trail).information());
					break;
				default:
					break;
			}
			if (trail.type() != TreasureTrailType.EMOTE && trail.type() != TreasureTrailType.ACTION) {
				player.getInterfaceManager().sendInterface(trail.interfaceId());
			}
			if (GameConstants.DEBUG) {
				player.setNextWorldTile(trail.coordinates());
			}
		}
		return true;
	}

	/**
	 * Gives a random trail to the player for the tier and sets all prerequisites for the scroll
	 *
	 * @param player
	 * 		The player receiving the trail
	 * @param data
	 * 		The data
	 * @param tier
	 * 		The tier
	 * @param setSteps
	 * 		If we should set the steps information
	 */
	private static void giveTrail(Player player, TreasureTrailData data, TreasureTrailTier tier, boolean setSteps) {
		if (setSteps) {
			int[] steps = tier.getSteps();
			int random = Utils.random(steps[0], steps[1]);
			data.getStepsToComplete()[tier.ordinal()] = random;
			data.getStepsComplete()[tier.ordinal()] = 0;
		}
		data.getTrailInformation().put(tier, getRandomTrail().getClass().getSimpleName());
		if (!setSteps) {
			player.getDialogueManager().startDialogue(SimpleItemMessage.class, EASY_SCROLL_ID, "You receive a clue scroll!");
		}
	}

	/**
	 * Handles the digging by looping through all the player's treasure trails, if they are on the correct tile, they
	 * proceed to the next step (if possible)
	 *
	 * @param player
	 * 		The player
	 */
	public static boolean handleDig(Player player) {
		for (Entry<TreasureTrailTier, String> entry : player.getFacade().getTrailData().getTrailInformation().entrySet()) {
			TreasureTrailTier tier = entry.getKey();
			String name = entry.getValue();

			AbstractTreasureTrail trail = getTrailByName(name);
			if (trail.actionType() == TrailActionType.DIG) {
				if (player.getWorldTile().withinDistance(trail.coordinates(), tier == TreasureTrailTier.ELITE ? 1 : 3)) {
					completeTrail(player, tier, trail, false);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Handles the player performing the emote
	 *
	 * @param player
	 * 		The player
	 * @param emote
	 * 		The emote performed
	 */
	public static boolean handleEmote(Player player, Emotes emote) {
		for (Entry<TreasureTrailTier, String> entry : player.getFacade().getTrailData().getTrailInformation().entrySet()) {
			TreasureTrailTier tier = entry.getKey();
			String name = entry.getValue();

			AbstractTreasureTrail trail = getTrailByName(name);
			if (!(trail instanceof AbstractEmoteTrail)) {
				continue;
			}
			AbstractEmoteTrail mapTrail = (AbstractEmoteTrail) trail;
			if (mapTrail.emote() == emote && mapTrail.passedRequirements(player) && trail.actionType() == TrailActionType.PERFORM_EMOTE) {
				if (player.getWorldTile().withinDistance(trail.coordinates(), tier == TreasureTrailTier.ELITE ? 1 : 3)) {
					completeTrail(player, tier, trail, false);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Handles the object interaction
	 *
	 * @param player
	 * 		The player
	 * @param object
	 * 		The object
	 */
	public static boolean handleObject(Player player, WorldObject object) {
		for (Entry<TreasureTrailTier, String> entry : player.getFacade().getTrailData().getTrailInformation().entrySet()) {
			TreasureTrailTier tier = entry.getKey();
			String name = entry.getValue();

			AbstractTreasureTrail trail = getTrailByName(name);
			if (!(trail instanceof AbstractActionTrail)) {
				continue;
			}
			AbstractActionTrail actionTrail = (AbstractActionTrail) trail;
			if (actionTrail.getInteractionId() == object.getId() && object.getWorldTile().withinDistance(trail.coordinates(), tier == TreasureTrailTier.ELITE ? 1 : 3)) {
				completeTrail(player, tier, trail, false);
				return true;
			}
		}
		return false;
	}

	/**
	 * Handles the npc interaction
	 *
	 * @param player
	 * 		The player
	 * @param npc
	 * 		the npc
	 */
	public static boolean handleNPC(Player player, NPC npc) {
		for (Entry<TreasureTrailTier, String> entry : player.getFacade().getTrailData().getTrailInformation().entrySet()) {
			TreasureTrailTier tier = entry.getKey();
			String name = entry.getValue();

			AbstractTreasureTrail trail = getTrailByName(name);
			if (!(trail instanceof AbstractActionTrail)) {
				continue;
			}
			AbstractActionTrail actionTrail = (AbstractActionTrail) trail;
			if (actionTrail.getInteractionId() == npc.getId()) {
				completeTrail(player, tier, trail, false);
				return true;
			}
		}
		return false;
	}

	/**
	 * Completes the trail for the player
	 *
	 * @param player
	 * 		The player
	 * @param tier
	 * 		The tier of the trail
	 * @param trail
	 * 		The trail
	 */
	public static void completeTrail(Player player, TreasureTrailTier tier, AbstractTreasureTrail trail, boolean skipMonsterCheck) {
		TreasureTrailData data = player.getFacade().getTrailData();
		data.incrementSteps(tier);

		TreasureTrailNPC npc = trail.findFighterNPC(player, skipMonsterCheck);
		if (npc == null || skipMonsterCheck) {
			if (data.getStepsComplete()[tier.ordinal()] >= data.getStepsToComplete()[tier.ordinal()]) {
				if (tier == TreasureTrailTier.EASY) {
					AchievementHandler.incrementProgress(player, Novice_Clue_Finisher.class, 1);
				}
				player.getInventory().deleteItem(getScrollByTier(tier), 1);
				giveCasket(player, tier);
				data.incrementTrails(tier);
				removeTierData(player, tier);
			} else {
				giveTrail(player, data, tier, false);
			}
		} else {
			npc.putAttribute("trail_tier", tier);
			npc.putAttribute("trail_class", trail);
			player.putAttribute("clue_npc", npc);
		}
	}

	/**
	 * Removes all the data for the tier from the player's {@link TreasureTrailData}
	 *
	 * @param player
	 * 		The player
	 * @param tier
	 * 		The tier
	 */
	private static void removeTierData(Player player, TreasureTrailTier tier) {
		TreasureTrailData data = player.getFacade().getTrailData();
		data.getStepsToComplete()[tier.ordinal()] = data.getStepsComplete()[tier.ordinal()] = 0;
		data.getTrailInformation().remove(tier);
	}

	/**
	 * Handles the opening of a trail casket
	 *
	 * @param player
	 * 		The player
	 * @param itemId
	 * 		The id of the casket
	 */
	public static boolean handleTrailCasket(Player player, int itemId) {
		TreasureTrailTier tier = getCasketTier(itemId);
		if (tier == null) {
			return false;
		}
		List<Item> items = new ArrayList<>();
		int rewardAmount = Utils.random(2, 5);
		for (int i = 0; i < rewardAmount; i++) {
			double random = Utils.random(1, 200);
			if (random <= tier.rewardChance()) {
				items.add(Utils.randomArraySlot(tier.getRewardItems()));
			} else {
				Item randomItem = Utils.randomArraySlot(COMMON_CASKET_REWARDS);
				switch (randomItem.getId()) {
					case 995:
						randomItem.setAmount(Utils.random(2_000, 50_000));
						break;
					case 10476:
						randomItem.setAmount(Utils.random(2, 50));
						break;
				}
				items.add(randomItem);
			}
		}
		player.getInterfaceManager().sendInterface(364);
		player.getPackets().sendItems(141, items.toArray(new Item[items.size()]));
		player.getInventory().deleteItem(itemId, 1);
		player.setCloseInterfacesEvent(() -> {
			for (Item item : items) {
				player.getInventory().addItemDrop(item.getId(), item.getAmount());
			}
		});
		return true;
	}

	/**
	 * Gives the player the casket
	 *
	 * @param player
	 * 		The player
	 * @param tier
	 * 		The tier of the casket to give
	 */
	private static void giveCasket(Player player, TreasureTrailTier tier) {
		int casketId = getCasketByTier(tier);
		if (casketId == -1) {
			throw new IllegalStateException();
		}
		player.getInventory().addItem(casketId, 1);
		player.getDialogueManager().startDialogue(SimpleItemMessage.class, casketId, "You have found a casket!");
	}

	/**
	 * Finds the id of the casket by the tier
	 *
	 * @param tier
	 * 		The tier
	 */
	private static int getCasketByTier(TreasureTrailTier tier) {
		switch (tier) {
			case EASY:
				return EASY_CASKET_ID;
			case MEDIUM:
				return MED_CASKET_ID;
			case HARD:
				return HARD_CASKET_ID;
			case ELITE:
				return ELITE_CASKET_ID;
			default:
				return -1;
		}
	}

	/**
	 * Finds the id of the scroll by the tier
	 *
	 * @param tier
	 * 		The tier
	 */
	private static int getScrollByTier(TreasureTrailTier tier) {
		switch (tier) {
			case EASY:
				return EASY_SCROLL_ID;
			case MEDIUM:
				return MED_SCROLL_ID;
			case HARD:
				return HARD_SCROLL_ID;
			case ELITE:
				return ELITE_SCROLL_ID;
			default:
				return -1;
		}
	}

	/**
	 * Finds an {@code AbstractTreasureTrail} by the name of the class
	 *
	 * @param name
	 * 		The name of the class
	 */
	private static AbstractTreasureTrail getTrailByName(String name) {
		for (AbstractTreasureTrail trail : treasureTrails) {
			if (trail.getClass().getSimpleName().equalsIgnoreCase(name)) {
				return trail;
			}
		}
		return null;
	}

	/**
	 * Populates a new arraylist full of the values in {@link #treasureTrails}, and shuffles it. The first index is then
	 * used as a random trail.
	 */
	private static AbstractTreasureTrail getRandomTrail() {
		List<AbstractTreasureTrail> trails = new ArrayList<>(treasureTrails);
		Collections.shuffle(trails);
		return trails.get(0);
	}

	/**
	 * Finds the tier for the clue scroll
	 *
	 * @param itemId
	 * 		The id of the scroll
	 */
	private static TreasureTrailTier getTier(int itemId) {
		switch (itemId) {
			case EASY_SCROLL_ID:
				return TreasureTrailTier.EASY;
			case MED_SCROLL_ID:
				return TreasureTrailTier.MEDIUM;
			case HARD_SCROLL_ID:
				return TreasureTrailTier.HARD;
			case ELITE_SCROLL_ID:
				return TreasureTrailTier.ELITE;
		}
		return null;
	}

	/**
	 * Finds the tier of a casket
	 *
	 * @param itemId
	 * 		The id of the cakset
	 */
	public static TreasureTrailTier getCasketTier(int itemId) {
		switch (itemId) {
			case EASY_CASKET_ID:
				return TreasureTrailTier.EASY;
			case MED_CASKET_ID:
				return TreasureTrailTier.MEDIUM;
			case HARD_CASKET_ID:
				return TreasureTrailTier.HARD;
			case ELITE_CASKET_ID:
				return TreasureTrailTier.ELITE;
		}
		return null;
	}

	/**
	 * The list of all treasure trails that are completeable
	 */
	private static final List<AbstractTreasureTrail> treasureTrails = Collections.synchronizedList(new ArrayList<>());

	/**
	 * The id of the scrolls
	 */
	public static final int EASY_SCROLL_ID = 10180, MED_SCROLL_ID = 10254, HARD_SCROLL_ID = 10234, ELITE_SCROLL_ID = 19064;

	/**
	 * The id of the caskets
	 */
	public static final int EASY_CASKET_ID = 10181, MED_CASKET_ID = 7318, HARD_CASKET_ID = 10253, ELITE_CASKET_ID = 19039;

	/**
	 * The array of common rewards from caskets
	 */
	public static final Item[] COMMON_CASKET_REWARDS = new Item[] { new Item(995), // coins
	                                                                new Item(10476), // sweets
	                                                                new Item(19600), // bandospage1
	                                                                new Item(19601), // bandospage2
	                                                                new Item(19602), // bandospage3
	                                                                new Item(19603), // bandospage4
	                                                                new Item(19604), // armadylpage1
	                                                                new Item(19605), // armadylpage2
	                                                                new Item(19606), // armadylpage3
	                                                                new Item(19607), // armadylpage4
	                                                                new Item(19608), // ancientpage1
	                                                                new Item(19609), // ancientpage2
	                                                                new Item(19610), // ancientpage3
	                                                                new Item(19611), // ancientpage4
	                                                                new Item(7329), // redfirelighter
	                                                                new Item(7330), // greenfirelighter
	                                                                new Item(7331), // bluefirelighter
	                                                                new Item(10326), // purplefirelighter
	                                                                new Item(10327), // whitefirelighter
	                                                                new Item(19152, 20), // saradominarrows
	                                                                new Item(19157, 20), // guthixarrows
	                                                                new Item(19162, 20), // zamorakarrows
	                                                                new Item(18778, 1), // effigy
	                                                                new Item(2441, 5), // superstrength
	                                                                new Item(2437, 5), // superattack
	                                                                new Item(2503), // blackdhidebody
	                                                                new Item(2497), // blackdhidechaps
	                                                                new Item(1275), // runepick
	                                                                new Item(1303), // runelong
	                                                                new Item(1127), // runeplatebody
	                                                                new Item(1147), // runehelm
	                                                                new Item(1201), // runekite
	                                                                new Item(892, 150), // runearrow
	                                                                new Item(561, 75), // naturerune
	                                                                new Item(563, 75), // lawrune
	                                                                new Item(565, 75), // bloodrune
	};
}
