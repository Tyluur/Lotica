package com.runescape.game.content.global.loyalty;

import com.runescape.cache.Cache;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.utility.Utils;

import java.io.IOException;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 11/14/2015.
 */
public enum LoyaltyRewards {

	JUNIOR_CADET_TITLE("Junior Cadet", "The junior cadet title.", 250) {
		@Override
		public void accept(Player player) {
			player.getAppearence().setTitle(1);
		}
	},

	SERJEANT_TITLE("Serjeant", "The serjeant title.", 350) {
		@Override
		public void accept(Player player) {
			player.getAppearence().setTitle(2);
		}
	},

	COMMANDER_TITLE("Commander", "The commander title", 500) {
		@Override
		public void accept(Player player) {
			player.getAppearence().setTitle(3);
		}
	},

	WAR_CHIEF_TITLE("War-chief", "The war-chief title.", 600) {
		@Override
		public void accept(Player player) {
			player.getAppearence().setTitle(4);
		}
	},

	SIR_TITLE("Sir", "The sir title.", 750) {
		@Override
		public void accept(Player player) {
			player.getAppearence().setTitle(5);
		}
	},

	LORD_TITLE("Lord", "The lord title.", 750) {
		@Override
		public void accept(Player player) {
			player.getAppearence().setTitle(6);
		}
	},

	DUDERINO_TITLE("Duderino", "The duderino title.", 875) {
		@Override
		public void accept(Player player) {
			player.getAppearence().setTitle(7);
		}
	},

	LIONHEART_TITLE("Lionheart", "The lionheart title.", 950) {
		@Override
		public void accept(Player player) {
			player.getAppearence().setTitle(8);
		}
	},

	HELLRAISER_TITLE("Hellraiser", "The hellraiser title.", 1025) {
		@Override
		public void accept(Player player) {
			player.getAppearence().setTitle(9);
		}
	},

	CRUSADER_TITLE("Crusader", "The crusader title.", 1025) {
		@Override
		public void accept(Player player) {
			player.getAppearence().setTitle(10);
		}
	},

	DESPERADO("Desperado", "The desperado ttile.", 1200) {
		@Override
		public void accept(Player player) {
			player.getAppearence().setTitle(11);
		}
	},

	BARON("Baron", "The baron title.", 1300) {
		@Override
		public void accept(Player player) {
			player.getAppearence().setTitle(12);
		}
	},

	COUNT("Count", "The count title.", 1500) {
		@Override
		public void accept(Player player) {
			player.getAppearence().setTitle(13);
		}
	},

	OVERLORD("Overlord", "The overlord title.", 1750) {
		@Override
		public void accept(Player player) {
			player.getAppearence().setTitle(14);
		}
	},

	BANDITO(2000) {
		@Override
		public void accept(Player player) {
			player.getAppearence().setTitle(15);
		}
	},

	DUKE(2000) {
		@Override
		public void accept(Player player) {
			player.getAppearence().setTitle(16);
		}
	},

	KING(2250) {
		@Override
		public void accept(Player player) {
			player.getAppearence().setTitle(17);
		}
	},

	BIG_CHEESE(2300) {
		@Override
		public void accept(Player player) {
			player.getAppearence().setTitle(18);
		}
	},

	BIGWIG(2450) {
		@Override
		public void accept(Player player) {
			player.getAppearence().setTitle(19);
		}
	},

	WUNDERKIND(2500) {
		@Override
		public void accept(Player player) {
			player.getAppearence().setTitle(20);
		}
	},

	VYRELING(2650) {
		@Override
		public void accept(Player player) {
			player.getAppearence().setTitle(21);
		}
	},

	VYRE_GRUNT(2700) {
		@Override
		public void accept(Player player) {
			player.getAppearence().setTitle(22);
		}
	},

	VYREWATCH(2750) {
		@Override
		public void accept(Player player) {
			player.getAppearence().setTitle(23);
		}
	},

	VYRELORD(2750) {
		@Override
		public void accept(Player player) {
			player.getAppearence().setTitle(24);
		}
	},

	// aura start

	ODDBALL_AURA_1("Oddball", "Turns dwarf cannon ammo into odd things, like beer, squids and brains.", 300, 20957) {
		@Override
		public void accept(Player player) {
			giveItem(player, new Item(getItemId()));
		}
	},

	POISON_PURGE("Causes poison to heal instead of damage you.", 350, 20958) {
		@Override
		public void accept(Player player) {
			giveItem(player, new Item(getItemId()));
		}
	},

	KNOCK_OUT("Increase hitchance by 20% if target is below 10% lifepoints.", 400, 20961) {
		@Override
		public void accept(Player player) {
			giveItem(player, new Item(getItemId()));
		}
	},

	RUNIC_ACCURACY("Gives offensive magic spells 3% greater chance of hitting their target.", 450, 20962) {
		@Override
		public void accept(Player player) {
			giveItem(player, new Item(getItemId()));
		}
	},

	SHARPSHOOTER("Increases your ranged accuracy by 3%.", 550, 20967) {
		@Override
		public void accept(Player player) {
			giveItem(player, new Item(getItemId()));
		}
	},

	QUARRYMASTER("Increases your chance of mining ore and rocks by 3%.", 850, 22284) {
		@Override
		public void accept(Player player) {
			giveItem(player, new Item(getItemId()));
		}
	},

	CALL_OF_THE_SEA("Increases your chance of catching fish by 3%.", 900, 20966) {
		@Override
		public void accept(Player player) {
			giveItem(player, new Item(getItemId()));
		}
	},

	REVERENCE("Slows down prayer drain and increases prayer restoration from potions by 3%.", 1100, 20965) {
		@Override
		public void accept(Player player) {
			giveItem(player, new Item(getItemId()));
		}
	},

	FIVE_FINGER_DISCOUNT("Increases your chance of thieving successfully by 3%.", 1125, 22288) {
		@Override
		public void accept(Player player) {
			giveItem(player, new Item(getItemId()));
		}
	},

	LUMBERJACK("Increases your chance of chopping wood by 3%.", 1150, 22280) {
		@Override
		public void accept(Player player) {
			giveItem(player, new Item(getItemId()));
		}
	},

	// start tier 2 auras

	GREATER_POISON_PURGE("Poison damage heals you with this aura.", 2000, 22268) {
		@Override
		public void accept(Player player) {
			giveItem(player, new Item(getItemId()));
		}
	},

	GREATER_CALL_OF_THE_SEA("Increases your chance of catching fish by 5%.", 2150, 22274) {
		@Override
		public void accept(Player player) {
			giveItem(player, new Item(getItemId()));
		}
	},

	GREATER_LUMBERJACK("Increases chance of chopping wood by 5%.", 2300, 22282) {
		@Override
		public void accept(Player player) {
			giveItem(player, new Item(getItemId()));
		}
	},

	GREATER_QUARRYMASTER("Increases your chance of mining ore and rocks by 5%.", 2400, 22286) {
		@Override
		public void accept(Player player) {
			giveItem(player, new Item(getItemId()));
		}
	},

	GREATER_FIVE_FINGER_DISCOUNT("Increases chance to succeed thieving stat random by 5%.", 2400, 22290) {
		@Override
		public void accept(Player player) {
			giveItem(player, new Item(getItemId()));
		}
	},

	GREATER_REVERENCE("Slows down prayer drain and increases prayer restoration from potions by 5%.", 2550, 22276) {
		@Override
		public void accept(Player player) {
			giveItem(player, new Item(getItemId()));
		}
	},

	GREATER_SHARPSHOOTER("Increases your ranged accuracy by 5%.", 2600, 22272) {
		@Override
		public void accept(Player player) {
			giveItem(player, new Item(getItemId()));
		}
	},

	GREATER_RUNIC_ACCURACY("Gives offensive magic spells 5% greater chance of hitting their target.", 2600, 22270) {
		@Override
		public void accept(Player player) {
			giveItem(player, new Item(getItemId()));
		}
	},

	EQUILIBRIUM("Your successful hits in combat have their max hit reduced by 25%, but also have their min hit increased by the same amount.", 3000, 22294) {
		@Override
		public void accept(Player player) {
			giveItem(player, new Item(getItemId()));
		}
	},

	VAMPYRISM("Your successful hits in combat cause you to regain life points equal to 5% of any damage you deal.", 3000, 22298) {
		@Override
		public void accept(Player player) {
			giveItem(player, new Item(getItemId()));
		}
	},

	PENANCE("Gain prayer points equal to 5% of any damage you receive.", 3000, 22300) {
		@Override
		public void accept(Player player) {
			giveItem(player, new Item(getItemId()));
		}
	},

	RESOURCEFUL("Gives a 10% chance not to deplete a resource when mining or woodcutting when gaining items from them. Cannot happen twice in a row.", 3000, 22292) {
		@Override
		public void accept(Player player) {
			giveItem(player, new Item(getItemId()));
		}
	},

	// end of tier 2 auras

	//  end of cosmetic auras

	//  start of tier 3 auras

	WISDOM("Increases all experience earned by 2.5%. Will only grant up to 100k xp within the activation time. Does not stack with bonus XP weekends.", 6400, 22302),
	//  end of tier 3 auras

	//  start of tier 4 auras

	//  end of tier 4 auras

	;

	/**
	 * The string qualities of the loyalty reward
	 */
	private final String name, description;

	/**
	 * The cost of the reward in points
	 */
	private final int pointCost;

	/**
	 * The id of the item
	 */
	private final int itemId;

	LoyaltyRewards(int pointCost) {
		this.name = Utils.formatPlayerNameForDisplay(name());
		this.description = "The '" + name + "' title.";
		this.pointCost = pointCost;
		this.itemId = -1;
	}

	LoyaltyRewards(String name, String description, int pointCost) {
		this.name = name;
		this.description = description;
		this.pointCost = pointCost;
		this.itemId = -1;
	}

	LoyaltyRewards(String name, String description, int pointCost, int itemId) {
		this.name = name;
		this.description = description;
		this.pointCost = pointCost;
		this.itemId = itemId;
	}


	LoyaltyRewards(String description, int pointCost, int itemId) {
		this.name = Utils.formatPlayerNameForDisplay(name());
		this.description = description;
		this.pointCost = pointCost;
		this.itemId = itemId;
	}

	/**
	 * Gives the player the items
	 *
	 * @param player
	 * 		The player
	 * @param items
	 * 		The items
	 */
	private static void giveItem(Player player, Item... items) {
		for (Item item : items) {
			player.getInventory().addItemDrop(item.getId(), item.getAmount());
		}
	}

	/**
	 * Checks if the player has the item
	 *
	 * @param player
	 * 		The player
	 * @param itemId
	 * 		The id of the item
	 */
	private static boolean hasItem(Player player, int itemId) {
		return player.hasItem(itemId);
	}

	/**
	 * If the player can purchase this aura
	 *
	 * @param player
	 * 		The player
	 */
	public boolean canPurchase(Player player) {
		return player.getLoyaltyManager().getPoints() >= pointCost;
	}

	/**
	 * When a player receives the reward, this method gives them what they paid for
	 *
	 * @param player
	 * 		The player
	 */
	public void accept(Player player) {
		if (getItemId() == -1) {
			throw new RuntimeException(this + " should override accept(Player)");
		}
		giveItem(player, new Item(getItemId()));
	}

	/**
	 * If the player has the reward
	 *
	 * @param player
	 * 		The player
	 */
	public boolean hasReward(Player player) {
		return itemId != -1 && hasItem(player, itemId);
	}

	/**
	 * Gets the itemId
	 *
	 * @return The itemId
	 */
	public int getItemId() {
		return itemId;
	}

	/**
	 * Finds a loyalty reward by name
	 *
	 * @param name
	 * 		The name
	 */
	public static LoyaltyRewards getRewardByName(String name) {
		name = name.replaceAll("-", " ");
		for (LoyaltyRewards reward : LoyaltyRewards.values()) {
			String rewardNameFormatted = Utils.formatPlayerNameForDisplay(reward.name);
			String nameFormatted = Utils.formatPlayerNameForDisplay(name).replace("<br>", "");
			if (nameFormatted.equalsIgnoreCase(rewardNameFormatted)) {
				return reward;
			}
		}
		return null;
	}

	public String getName() {
		return name;
	}

	public int getPointCost() {
		return pointCost;
	}

	public String getDescription() {
		return description;
	}

	public static void main(String[] args) throws IOException {
		Cache.init();
		for (LoyaltyRewards reward : values()) {
			int itemId = reward.getItemId();
			if (itemId <= 0) {
				continue;
			}
			if (itemId >= Utils.getItemDefinitionsSize()) {
				System.out.println(reward + " is not in 667");
			}
		}


		/*
		List<String> results = new ArrayList<>();
		for (LoyaltyRewards reward : LoyaltyRewards.values()) {
			if (reward.ordinal() >= GREATER_SALVATION.ordinal() && reward.ordinal() <= RESOURCEFUL.ordinal()) {
				results.add(Utils.formatPlayerNameForDisplay(reward.name()));
			}
		}
		System.out.println("Tier 2 Auras:");
		results.forEach(result -> System.out.print("\"" + result + "\", "));
		System.out.println();
		results.clear();

		for (LoyaltyRewards reward : LoyaltyRewards.values()) {
			if (reward.ordinal() >= ABYSSAL_GAZE.ordinal() && reward.ordinal() <= VERNAL_GAZE.ordinal()) {
				results.add(Utils.formatPlayerNameForDisplay(reward.name()));
			}
		}
		System.out.println("Cosmetic Auras:");
		results.forEach(result -> System.out.print("\"" + result + "\", "));
		System.out.println();*/
	}
}