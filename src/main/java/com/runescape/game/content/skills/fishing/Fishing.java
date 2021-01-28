package com.runescape.game.content.skills.fishing;

import com.runescape.game.content.FishingSpotsHandler;
import com.runescape.game.content.global.wilderness.WildernessActivityManager;
import com.runescape.game.content.global.wilderness.activities.FishingActivity;
import com.runescape.game.content.skills.cooking.Cooking.Cookables;
import com.runescape.game.interaction.controllers.impl.Wilderness;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.game.world.entity.player.achievements.AchievementHandler;
import com.runescape.game.world.entity.player.achievements.hard.Sharksman;
import com.runescape.game.world.entity.player.achievements.medium.Shark_Catcher;
import com.runescape.game.world.entity.player.achievements.medium.Swordie_Lover;
import com.runescape.game.world.entity.player.actions.Action;
import com.runescape.game.world.item.Item;
import com.runescape.utility.Utils;

import java.util.HashMap;
import java.util.Map;

public class Fishing extends Action {

	private final int[] BONUS_FISH = { 341, 349, 401, 407 };

	private FishingSpots spot;

	private NPC npc;

	private WorldTile tile;

	private int fishId;

	private boolean multipleCatch;

	@Override
	public boolean start(Player player) {
		if (!checkAll(player)) { return false; }
		fishId = getRandomFish(player);
		if (spot.getFish()[fishId] == Fish.TUNA || spot.getFish()[fishId] == Fish.SHARK || spot.getFish()[fishId] == Fish.SWORDFISH) {
			if (Utils.getRandom(50) <= 5) {
				if (player.getSkills().getLevel(Skills.AGILITY) >= spot.getFish()[fishId].getLevel()) {
					multipleCatch = true;
				}
			}
		}
		player.getPackets().sendGameMessage("You attempt to capture a fish...", true);
		setActionDelay(player, getEndDelay(player));
		return true;
	}

	@Override
	public boolean process(Player player) {
		player.setNextAnimation(spot.getAnimation());
		return checkAll(player);
	}

	@Override
	public int processWithDelay(Player player) {
		addFish(player);
		return getEndDelay(player);
	}

	private void addFish(Player player) {
		Item fish = getFish(player);
		player.getPackets().sendGameMessage(getMessage(fish), true);
		player.getInventory().deleteItem(spot.getBait(), 1);
		double totalXp = spot.getFish()[fishId].getXp();
		if (hasFishingSuit(player)) { totalXp *= 1.025; }
		player.getSkills().addXp(Skills.FISHING, totalXp);
		player.getInventory().addItem(fish);
		if (player.getFamiliar() != null) {
			if (Utils.getRandom(50) == 0 && getSpecialFamiliarBonus(player.getFamiliar().getId()) > 0) {
				player.getInventory().addItem(new Item(BONUS_FISH[Utils.random(BONUS_FISH.length)]));
				player.getSkills().addXp(Skills.FISHING, 5.5);
			}
		}
		if (fish.getId() == 371) {
			AchievementHandler.incrementProgress(player, Swordie_Lover.class);
		}
		fishId = getRandomFish(player);
		if (Utils.getRandom(50) == 0 && FishingSpotsHandler.moveSpot(npc)) {
			player.setNextAnimation(new Animation(-1));
		}
		if (fish.getId() == 383) {
			AchievementHandler.incrementProgress(player, Shark_Catcher.class);
			AchievementHandler.incrementProgress(player, Sharksman.class);
		}
		if (player.getControllerManager().verifyControlerForOperation(Wilderness.class).isPresent()) {
			player.getInventory().addItem(Wilderness.WILDERNESS_TOKEN, multipleCatch ? 4 : 2);
		}
	}

	public Item getFish(Player player) {
		Item fish = new Item(spot.getFish()[fishId].getId(), multipleCatch ? 2 : 1);
		if (WildernessActivityManager.getSingleton().isActivityCurrent(FishingActivity.class)) {
			FishingActivity activity = WildernessActivityManager.getSingleton().getWildernessActivity(FishingActivity.class);
			if (activity.receivesBonus(player, npc)) {
				WildernessActivityManager.getSingleton().giveBonusPoints(player, npc);
				Cookables cookable = Cookables.forId((short) fish.getId());
				if (cookable != null && Utils.percentageChance(30)) {
					player.getSkills().addXpNoModifier(Skills.COOKING, cookable.getXp());
					return new Item(cookable.getProduct().getId(), fish.getAmount());
				}
			}
		}
		return fish;
	}

	private String getMessage(Item fish) {
		if (spot.getFish()[fishId] == Fish.ANCHOVIES || spot.getFish()[fishId] == Fish.SHRIMP) {
			return "You manage to catch some " + fish.getDefinitions().getName().toLowerCase() + ".";
		} else if (multipleCatch) {
			return "Your quick reactions allow you to catch two " + fish.getDefinitions().getName().toLowerCase() + ".";
		} else { return "You manage to catch a " + fish.getDefinitions().getName().toLowerCase() + "."; }
	}

	private boolean hasFishingSuit(Player player) {
		return player.getEquipment().getHatId() == 24427 && player.getEquipment().getChestId() == 24428 && player.getEquipment().getLegsId() == 24429 && player.getEquipment().getBootsId() == 24430;
	}

	@Override
	public void stop(final Player player) {
		setActionDelay(player, 3);
	}

	private boolean checkAll(Player player) {
		if (player.getSkills().getLevel(Skills.FISHING) < spot.getFish()[fishId].getLevel()) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You need a fishing level of " + spot.getFish()[fishId].getLevel() + " to fish here.");
			return false;
		}
		if (!player.getInventory().containsOneItem(spot.getTool())) {
			player.getPackets().sendGameMessage("You need a " + new Item(spot.getTool()).getDefinitions().getName().toLowerCase() + " to fish here.");
			return false;
		}
		if (!player.getInventory().containsOneItem(spot.getBait()) && spot.getBait() != -1) {
			player.getPackets().sendGameMessage("You don't have " + new Item(spot.getBait()).getDefinitions().getName().toLowerCase() + " to fish here.");
			return false;
		}
		if (!player.getInventory().hasFreeSlots()) {
			player.setNextAnimation(new Animation(-1));
			player.getDialogueManager().startDialogue("SimpleMessage", "You don't have enough inventory space.");
			return false;
		}
		return !(tile.getX() != npc.getX() || tile.getY() != npc.getY());
	}

	private int getRandomFish(Player player) {
		int random = Utils.random(spot.getFish().length);
		int difference = player.getSkills().getLevel(Skills.FISHING) - spot.getFish()[random].getLevel();
		if (difference < -1) { return random = 0; }
		if (random < -1) { return random = 0; }
		return random;
	}

	public int getEndDelay(Player player) {
		int delay = getFishingDelay(player);
		if (WildernessActivityManager.getSingleton().isActivityCurrent(FishingActivity.class)) {
			FishingActivity activity = WildernessActivityManager.getSingleton().getWildernessActivity(FishingActivity.class);
			if (activity.receivesBonus(player, npc)) {
				return delay / 2;
			}
		}
		return delay;
	}

	private int getFishingDelay(Player player) {
		int playerLevel = player.getSkills().getLevel(Skills.FISHING);
		int fishLevel = spot.getFish()[fishId].getLevel();
		int modifier = spot.getFish()[fishId].getLevel();
		int randomAmt = Utils.random(4);
		double cycleCount = 1, otherBonus = 0;
		if (player.getFamiliar() != null) { otherBonus = getSpecialFamiliarBonus(player.getFamiliar().getId()); }
		cycleCount = Math.ceil(((fishLevel + otherBonus) * 50 - playerLevel * 10) / modifier * 0.25 - randomAmt * 4);
		if (cycleCount < 1) { cycleCount = 1; }
		int delay = (int) cycleCount + 1;
		delay /= player.getAuraManager().getFishingAccurayMultiplier();
		return delay;

	}

	private int getSpecialFamiliarBonus(int id) {
		switch (id) {
			case 6796:
			case 6795:// rock crab
				return 1;
		}
		return -1;
	}

	public Fishing(FishingSpots spot, NPC npc) {
		this.spot = spot;
		this.npc = npc;
		tile = new WorldTile(npc);
	}

	public enum Fish {

		ANCHOVIES(321, 15, 40),

		BASS(363, 46, 100),

		COD(341, 23, 45),

		CAVE_FISH(15264, 85, 300),

		HERRING(345, 10, 30),

		LOBSTER(377, 40, 90),

		MACKEREL(353, 16, 20),

		MANTA(389, 81, 46),

		MONKFISH(7944, 62, 120),

		PIKE(349, 25, 60),

		SALMON(331, 30, 70),

		SARDINES(327, 5, 20),

		SEA_TURTLE(395, 79, 38),

		SEAWEED(401, 30, 0),

		OYSTER(407, 30, 0),

		SHARK(383, 76, 110),

		SHRIMP(317, 1, 10),

		SWORDFISH(371, 50, 100),

		TROUT(335, 20, 50),

		TUNA(359, 35, 80),

		CAVEFISH(15264, 85, 300),

		ROCKTAIL(15270, 90, 385);

		private final int id, level;

		private final double xp;

		Fish(int id, int level, double xp) {
			this.id = id;
			this.level = level;
			this.xp = xp;
		}

		public int getId() {
			return id;
		}

		public int getLevel() {
			return level;
		}

		public double getXp() {
			return xp;
		}
	}

	public enum FishingSpots {
		CAVEFISH_SHOAL(8841, 1, 307, 313, new Animation(622), Fish.CAVE_FISH),

		ROCKTAIL_SHOAL(8842, 1, 307, 15263, new Animation(622), Fish.ROCKTAIL),

		NET(327, 1, 303, -1, new Animation(621), Fish.SHRIMP, Fish.ANCHOVIES, Fish.SARDINES),

		LURE(328, 1, 309, 314, new Animation(622), Fish.TROUT, Fish.SALMON, Fish.SARDINES),

		LURE2(329, 1, 309, 314, new Animation(622), Fish.TROUT, Fish.SALMON),

		BAIT2(328, 2, 307, 313, new Animation(622), Fish.PIKE, Fish.SARDINES),

		BAIT3(329, 2, 307, 313, new Animation(622), Fish.PIKE, Fish.CAVE_FISH),

		CAGE(6267, 1, 301, -1, new Animation(619), Fish.LOBSTER),

		CAGE2(312, 1, 301, -1, new Animation(619), Fish.LOBSTER),

		HARPOON(312, 2, 311, -1, new Animation(618), Fish.TUNA, Fish.SWORDFISH, Fish.SHARK),

		BIG_NET(313, 1, 305, -1, new Animation(620), Fish.MACKEREL, Fish.COD, Fish.BASS, Fish.SEAWEED, Fish.OYSTER),

		HARPOON2(313, 2, 311, -1, new Animation(618), Fish.TUNA, Fish.SWORDFISH, Fish.SHARK),

		NET2(952, 1, 303, -1, new Animation(621), Fish.SHRIMP);

		static final Map<Integer, FishingSpots> spot = new HashMap<Integer, FishingSpots>();

		static {
			for (FishingSpots spots : FishingSpots.values()) { spot.put(spots.id | spots.option << 24, spots); }
		}

		private final Fish[] fish;

		private final int id, option, tool, bait;

		private final Animation animation;

		FishingSpots(int id, int option, int tool, int bait, Animation animation, Fish... fish) {
			this.id = id;
			this.tool = tool;
			this.bait = bait;
			this.animation = animation;
			this.fish = fish;
			this.option = option;
		}

		public static FishingSpots forId(int id) {
			return spot.get(id);
		}

		public Fish[] getFish() {
			return fish;
		}

		public int getId() {
			return id;
		}

		public int getOption() {
			return option;
		}

		public int getTool() {
			return tool;
		}

		public int getBait() {
			return bait;
		}

		public Animation getAnimation() {
			return animation;
		}
	}
}
