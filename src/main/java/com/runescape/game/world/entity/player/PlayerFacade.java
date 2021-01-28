package com.runescape.game.world.entity.player;

import com.runescape.game.content.economy.exchange.ExchangeOffer;
import com.runescape.game.content.economy.treasure.TreasureTrailData;
import com.runescape.game.content.skills.slayer.SlayerTask;
import com.runescape.game.event.interaction.button.TeleportationSelectionInteractionEvent.TransportationLocation;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.npc.godwars.Bosses;
import com.runescape.game.world.entity.npc.pet.RewardPet;
import com.runescape.game.world.entity.player.achievements.AbstractAchievement;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 18, 2015
 */
public class PlayerFacade {

	/** Important information about the player's treasure trail */
	private final TreasureTrailData trailData = new TreasureTrailData();

	/** The progress players make in achievements */
	private final Map<String, Integer> achievementProgress = new HashMap<>();

	/** The map of the bosses killed by name, the value is the amount of times the boss was killed. */
	private final Map<String, Integer> bossKillCounts = new HashMap<>();

	/** The killcount in godwars */
	private final int[] gwdKillcount = new int[4];

	/** The list of names unable to attack you in the wilderness */
	private final List<String> wildernessBlacklist = new ArrayList<>();

	/** The map of stored attributes */
	private final ConcurrentHashMap<Object, Object> storedAttributes = new ConcurrentHashMap<>();

	/**
	 * The map of the time spent online daily
	 */
	private final Map<String, Map<Long, Long>> timeSpentDaily = new HashMap<>();

	private final boolean[] unlockedPrayers = new boolean[2];

	/** The amount of coins in the money pouch */
	private int moneyPouchCoins = 0;

	/** The amount of slayer points the player has */
	private int slayerPoints = 0;

	/** The amount of dream points the player has */
	private int dreamPoints = 0;

	/** The amount of vote points the player has */
	private int votePoints = 0;

	/** The amount of times the player has voted */
	private int voteCount = 0;

	/** The amount of wilderness points the player has */
	private int wildernessPoints = 0;

	/** The amount of gold points the player has */
	private int goldPoints = 0;

	/** The amount of ring of wealth charges the player has */
	private int rowCharges;

	/** The amount of runespan points we have */
	private int runespanPoints = 0;

	/** If the player can gamble */
	private boolean canGamble;

	/** The current slayer task of the player */
	private SlayerTask slayerTask;

	private String slayerPartner;

	/** The email */
	private String forumEmail;

	/** If the player has received their starter */
	private boolean receivedStarter;

	/** The amount of money the player has spent on points */
	private long totalPointsPurchased;

	/** The amount of seconds the player has spent online */
	private long secondsSpentOnline;

	/** If we have yell off */
	private boolean yellOff;

	/** If we have experience locked */
	private boolean experienceLocked;

	/** The time the player is jailed until */
	private long jailedUntil;

	/** The killstreak the player is on */
	private int killstreak;

	/** If loot share is on */
	private boolean lootshareEnabled;

	/**
	 * If the player has registered an email
	 */
	private boolean emailRegistered;

	/**
	 * When the player's double experience is over
	 */
	private long doubleExperienceOverAt;

	/**
	 * The amount of dragon fire charges the player has
	 */
	private int dragonFireCharges;

	/**
	 * The amount of mage arena points
	 */
	private int mageArenaPoints;

	/**
	 * The array of the player's offer history
	 */
	private ExchangeOffer[] offerHistory = new ExchangeOffer[5];

	private TransportationLocation lastTransportationLocation;

	private long donatorExpirationTime;

	private final List<String> donatorRanksPurchased = new ArrayList<>();

	private final List<RewardPet> rewardPets = new ArrayList<>();

	/**
	 * The progress the player has made towards this achievement
	 *
	 * @param achievement
	 * 		The achievements
	 */
	public int getProgress(AbstractAchievement achievement) {
		if (achievementProgress.containsKey(achievement.key())) {
			return achievementProgress.get(achievement.key());
		} else {
			return 0;
		}
	}

	/**
	 * Sets the progress of an achievement
	 *
	 * @param achievement
	 * 		The achievement
	 * @param progress
	 * 		The progress
	 */
	public void setProgress(AbstractAchievement achievement, int progress) {
		achievementProgress.put(achievement.key(), progress);
	}

	/**
	 * Adds coins to the money pouch
	 *
	 * @param coins
	 * 		The amount of coins to add
	 */
	public boolean addMoneyPouchCoins(int coins) {
		long newTotal = moneyPouchCoins + coins;
		if (newTotal < 0 || newTotal > Integer.MAX_VALUE) {
			return false;
		}
		this.moneyPouchCoins += coins;
		return true;
	}

	/**
	 * Removes coins from the money pouch
	 *
	 * @param coins
	 * 		The amount of coins to remove
	 */
	public boolean removeMoneyPouchCoins(int coins) {
		long newTotal = moneyPouchCoins - coins;
		if (newTotal < 0 || newTotal > Integer.MAX_VALUE) {
			return false;
		}
		this.moneyPouchCoins -= coins;
		return true;
	}

	/**
	 * If we have a slayer task
	 */
	public boolean hasSlayerTask() {
		return getSlayerTask() != null;
	}

	/**
	 * Removes the slayer task variables
	 */
	public void removeSlayerTask() {
		setSlayerTask(null);
	}

	/**
	 * @return the achievementProgress
	 */
	public Map<String, Integer> getAchievementProgress() {
		return achievementProgress;
	}

	/**
	 * Adds to the amount of wilderness points
	 */
	public void addWildernessPoints(int amount) {
		this.wildernessPoints += amount;
	}

	/**
	 * Adds to the amount of times voted
	 *
	 * @param amount
	 * 		The amount
	 */
	public void addVoteCount(int amount) {
		this.voteCount += amount;
	}

	/**
	 * Adds the gold points to the total amount
	 *
	 * @param goldPoints
	 * 		The amount to add
	 */
	public void rewardPoints(int goldPoints) {
		this.goldPoints += goldPoints;
	}

	/**
	 * Increments {@link #secondsSpentOnline} by 1
	 */
	public void addSecondsSpentOnline() {
		secondsSpentOnline++;
	}

	/**
	 * Converting {@link #secondsSpentOnline} to a string format.
	 */
	public String getPlaytime() {
		long minutes = TimeUnit.SECONDS.toMinutes(secondsSpentOnline);
		long hours = TimeUnit.SECONDS.toHours(secondsSpentOnline);
		long days = TimeUnit.SECONDS.toDays(secondsSpentOnline);
		if (minutes < 60) {
			return "" + minutes + " mins";
		} else if (hours < 100) {
			return "" + hours + " hours";
		} else {
			return "" + days + " days";
		}
	}

	/**
	 * Toggles the experience log state
	 */
	public void toggleExperienceLock() {
		experienceLocked = !experienceLocked;
	}

	/**
	 * Adds runespan points
	 *
	 * @param runespanPoints
	 * 		The amount of points to add
	 */
	public void addRunespanPoints(int runespanPoints) {
		this.runespanPoints += runespanPoints;
	}

	/**
	 * Increments the boss kills for the boss by 1
	 *
	 * @param name
	 * 		The name of the boss
	 */
	public void incrementBossKills(String name) {
		bossKillCounts.put(name, getTimesKilledBoss(name) + 1);
	}

	/**
	 * Gets the amount of times we have killed a boss
	 *
	 * @param name
	 * 		The name of the boss
	 */
	public int getTimesKilledBoss(String name) {
		Integer value = bossKillCounts.get(name);
		return value == null ? 0 : value;
	}

	/**
	 * Gets the {@link #bossKillCounts}
	 */
	public Map<String, Integer> getBossKillCounts() {
		return bossKillCounts;
	}

	/**
	 * Checks if the player can attack us, based on whether or not their name is in our wilderness blacklist
	 *
	 * @param attackerName
	 * 		The name of the attacker
	 */
	public boolean blacklistContains(String attackerName) {
		for (String name : wildernessBlacklist) {
			if (name.equalsIgnoreCase(attackerName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets the wilderness black list
	 */
	public List<String> getWildernessBlacklist() {
		return wildernessBlacklist;
	}

	@SuppressWarnings("unchecked")
	public <K> K getAttribute(String key, K defaultValue) {
		K value = (K) storedAttributes.get(key);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}

	@SuppressWarnings("unchecked")
	public <K> K getAttribute(String key) {
		return (K) storedAttributes.get(key);
	}

	@SuppressWarnings("unchecked")
	public <K> K removeAttribute(String key) {
		return (K) storedAttributes.remove(key);
	}

	@SuppressWarnings("unchecked")
	public <K> K removeAttribute(String key, K defaultValue) {
		K value = (K) storedAttributes.remove(key);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}

	/**
	 * Puts the key into the attributes map
	 *
	 * @param key
	 * 		The key
	 * @param value
	 * 		The value
	 */
	public <K> K putAttribute(String key, K value) {
		storedAttributes.put(key, value);
		return value;
	}

	/**
	 * Gets the {@code Player} {@code Object} instance of the {@link #slayerPartner}
	 */
	public Player getSlayerPartnerPlayer() {
		if (slayerPartner == null) {
			return null;
		}
		return World.getPlayer(slayerPartner);
	}

	/**
	 * Adds to the total time spent online for a certain day
	 *
	 * @param signedInAt
	 * 		The time the player signed in at
	 */
	public void addTimeOnline(long signedInAt) {
		String date = new SimpleDateFormat("MM/dd/yyyy").format(new Date());
		Map<Long, Long> storedToday = timeSpentDaily.get(date);
		if (storedToday == null) {
			storedToday = new HashMap<>();
		}
		storedToday.put(signedInAt, System.currentTimeMillis());
		timeSpentDaily.put(date, storedToday);
	}

	/**
	 * Gets the time spent during the current day, by calculating the time spent since login, and adding the time spent
	 * previously that day.
	 *
	 * @param signedInAt
	 * 		The time of login
	 */
	public Long getTimeSpentToday(long signedInAt) {
		String date = new SimpleDateFormat("MM/dd/yyyy").format(new Date());
		long timeSpent = System.currentTimeMillis() - signedInAt;
		Map<Long, Long> storedToday = timeSpentDaily.get(date);
		for (Entry<Long, Long> entry : storedToday.entrySet()) {
			Long difference = entry.getValue() - entry.getKey();
			timeSpent += difference;
		}
		return TimeUnit.MILLISECONDS.toSeconds(timeSpent);
	}

	/**
	 * Finds out how much time has been spent since some certain day in milliseconds
	 *
	 * @param date
	 * 		The date
	 * @param signedInAt
	 * 		When the player signed in
	 */
	public Long getTimeSpentSinceDate(long date, long signedInAt) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		Date dateToCalculateFrom = new Date(date);
		long timeSpent = 0;
		for (Entry<String, Map<Long, Long>> timeEntry : timeSpentDaily.entrySet()) {
			Date entryDate;
			try {
				entryDate = dateFormat.parse(timeEntry.getKey());
				if (entryDate.getDay() < dateToCalculateFrom.getDay() || entryDate.getMonth() < dateToCalculateFrom.getMonth() || entryDate.getYear() < dateToCalculateFrom.getYear()) {
					continue;
				}
			} catch (ParseException e) {
				e.printStackTrace();
				continue;
			}
			Map<Long, Long> time = timeEntry.getValue();
			for (Entry<Long, Long> entry : time.entrySet()) {
				Long signedOutAt = entry.getValue();
				if (signedOutAt < date) {
					continue;
				}
				Long difference = signedOutAt - entry.getKey();
				timeSpent += difference;
			}
		}
		timeSpent += System.currentTimeMillis() - signedInAt;
		return timeSpent;
	}

	/**
	 * Adds an offer to the offer history
	 *
	 * @param offer
	 * 		The offer
	 */
	public void addOfferHistory(ExchangeOffer offer) {
		if (offer.isAborted()) {
			return;
		}
		ExchangeOffer[] destinationHistory = new ExchangeOffer[5];
		destinationHistory[0] = offer;
		System.arraycopy(offerHistory, 0, destinationHistory, 1, offerHistory.length - 1);
		offerHistory = destinationHistory;
	}

	/**
	 * Increases killcount information
	 *
	 * @param boss
	 * 		The boss
	 * @param amount
	 * 		The amount
	 */
	public void increaseKillCount(Bosses boss, int amount) {
		gwdKillcount[boss.ordinal()] += amount;
	}

	/**
	 * If we have donator time remaining
	 */
	public boolean hasDonatorTimeRemaining() {
		return System.currentTimeMillis() < donatorExpirationTime;
	}

	/**
	 * Adds a reward pet
	 *
	 * @param pet
	 * 		The pet to add
	 */
	public boolean addRewardPet(RewardPet pet) {
		return rewardPets.add(pet);
	}

    public TreasureTrailData getTrailData() {
        return this.trailData;
    }

    public int[] getGwdKillcount() {
        return this.gwdKillcount;
    }

    public Map<String, Map<Long, Long>> getTimeSpentDaily() {
        return this.timeSpentDaily;
    }

    public boolean[] getUnlockedPrayers() {
        return this.unlockedPrayers;
    }

    public int getMoneyPouchCoins() {
        return this.moneyPouchCoins;
    }

    public int getSlayerPoints() {
        return this.slayerPoints;
    }

    public int getDreamPoints() {
        return this.dreamPoints;
    }

    public int getVotePoints() {
        return this.votePoints;
    }

    public int getVoteCount() {
        return this.voteCount;
    }

    public int getWildernessPoints() {
        return this.wildernessPoints;
    }

    public int getGoldPoints() {
        return this.goldPoints;
    }

    public int getRowCharges() {
        return this.rowCharges;
    }

    public int getRunespanPoints() {
        return this.runespanPoints;
    }

    public boolean isCanGamble() {
        return this.canGamble;
    }

    public SlayerTask getSlayerTask() {
        return this.slayerTask;
    }

    public String getSlayerPartner() {
        return this.slayerPartner;
    }

    public String getForumEmail() {
        return this.forumEmail;
    }

    public boolean isReceivedStarter() {
        return this.receivedStarter;
    }

    public long getTotalPointsPurchased() {
        return this.totalPointsPurchased;
    }

    public long getSecondsSpentOnline() {
        return this.secondsSpentOnline;
    }

    public boolean isYellOff() {
        return this.yellOff;
    }

    public boolean isExperienceLocked() {
        return this.experienceLocked;
    }

    public long getJailedUntil() {
        return this.jailedUntil;
    }

    public int getKillstreak() {
        return this.killstreak;
    }

    public boolean isLootshareEnabled() {
        return this.lootshareEnabled;
    }

    public boolean isEmailRegistered() {
        return this.emailRegistered;
    }

    public long getDoubleExperienceOverAt() {
        return this.doubleExperienceOverAt;
    }

    public int getDragonFireCharges() {
        return this.dragonFireCharges;
    }

    public int getMageArenaPoints() {
        return this.mageArenaPoints;
    }

    public ExchangeOffer[] getOfferHistory() {
        return this.offerHistory;
    }

    public TransportationLocation getLastTransportationLocation() {
        return this.lastTransportationLocation;
    }

    public long getDonatorExpirationTime() {
        return this.donatorExpirationTime;
    }

    public List<String> getDonatorRanksPurchased() {
        return this.donatorRanksPurchased;
    }

    public List<RewardPet> getRewardPets() {
        return this.rewardPets;
    }

    public void setMoneyPouchCoins(int moneyPouchCoins) {
        this.moneyPouchCoins = moneyPouchCoins;
    }

    public void setSlayerPoints(int slayerPoints) {
        this.slayerPoints = slayerPoints;
    }

    public void setDreamPoints(int dreamPoints) {
        this.dreamPoints = dreamPoints;
    }

    public void setVotePoints(int votePoints) {
        this.votePoints = votePoints;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    public void setWildernessPoints(int wildernessPoints) {
        this.wildernessPoints = wildernessPoints;
    }

    public void setGoldPoints(int goldPoints) {
        this.goldPoints = goldPoints;
    }

    public void setRowCharges(int rowCharges) {
        this.rowCharges = rowCharges;
    }

    public void setRunespanPoints(int runespanPoints) {
        this.runespanPoints = runespanPoints;
    }

    public void setCanGamble(boolean canGamble) {
        this.canGamble = canGamble;
    }

    public void setSlayerTask(SlayerTask slayerTask) {
        this.slayerTask = slayerTask;
    }

    public void setSlayerPartner(String slayerPartner) {
        this.slayerPartner = slayerPartner;
    }

    public void setForumEmail(String forumEmail) {
        this.forumEmail = forumEmail;
    }

    public void setReceivedStarter(boolean receivedStarter) {
        this.receivedStarter = receivedStarter;
    }

    public void setTotalPointsPurchased(long totalPointsPurchased) {
        this.totalPointsPurchased = totalPointsPurchased;
    }

    public void setYellOff(boolean yellOff) {
        this.yellOff = yellOff;
    }

    public void setJailedUntil(long jailedUntil) {
        this.jailedUntil = jailedUntil;
    }

    public void setKillstreak(int killstreak) {
        this.killstreak = killstreak;
    }

    public void setLootshareEnabled(boolean lootshareEnabled) {
        this.lootshareEnabled = lootshareEnabled;
    }

    public void setEmailRegistered(boolean emailRegistered) {
        this.emailRegistered = emailRegistered;
    }

    public void setDoubleExperienceOverAt(long doubleExperienceOverAt) {
        this.doubleExperienceOverAt = doubleExperienceOverAt;
    }

    public void setDragonFireCharges(int dragonFireCharges) {
        this.dragonFireCharges = dragonFireCharges;
    }

    public void setMageArenaPoints(int mageArenaPoints) {
        this.mageArenaPoints = mageArenaPoints;
    }

    public void setLastTransportationLocation(TransportationLocation lastTransportationLocation) {
        this.lastTransportationLocation = lastTransportationLocation;
    }

    public void setDonatorExpirationTime(long donatorExpirationTime) {
        this.donatorExpirationTime = donatorExpirationTime;
    }
}
