package com.runescape.game.world.entity.player;

import com.runescape.game.GameConstants;
import com.runescape.game.content.*;
import com.runescape.game.content.GreegreeHandler.Greegree;
import com.runescape.game.content.global.cannon.DwarfCannon;
import com.runescape.game.content.global.clans.ClansManager;
import com.runescape.game.content.global.lottery.Lottery;
import com.runescape.game.content.global.minigames.clanwars.FfaZone;
import com.runescape.game.content.global.minigames.clanwars.WarControler;
import com.runescape.game.content.global.minigames.duel.DuelArena;
import com.runescape.game.content.global.minigames.duel.DuelRules;
import com.runescape.game.content.global.minigames.warriors.WarriorsGuild;
import com.runescape.game.content.global.wilderness.WildernessActivityManager;
import com.runescape.game.content.global.wilderness.activities.PvpRegionActivity;
import com.runescape.game.content.skills.DXPAlgorithms;
import com.runescape.game.content.skills.SkillCapeCustomizer;
import com.runescape.game.content.skills.construction.House;
import com.runescape.game.content.skills.slayer.SlayerManagement;
import com.runescape.game.content.skills.summoning.Summoning;
import com.runescape.game.event.interaction.button.QuestTabInteractionEvent;
import com.runescape.game.interaction.controllers.Controller;
import com.runescape.game.interaction.controllers.impl.*;
import com.runescape.game.interaction.controllers.impl.castlewars.CastleWarsPlaying;
import com.runescape.game.interaction.controllers.impl.castlewars.CastleWarsWaiting;
import com.runescape.game.interaction.controllers.impl.fightpits.FightPitsArena;
import com.runescape.game.interaction.controllers.impl.pestcontrol.PestControlGame;
import com.runescape.game.interaction.controllers.impl.pestcontrol.PestControlLobby;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleNPCMessage;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.ForceTalk;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.masks.Hit;
import com.runescape.game.world.entity.masks.Hit.HitLook;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.familiar.impl.BeastOfBurden;
import com.runescape.game.world.entity.npc.familiar.impl.Familiar;
import com.runescape.game.world.entity.npc.godwars.zaros.Nex;
import com.runescape.game.world.entity.npc.pet.Pet;
import com.runescape.game.world.entity.player.LockManagement.LockType;
import com.runescape.game.world.entity.player.achievements.AchievementData.AchievementType;
import com.runescape.game.world.entity.player.achievements.AchievementHandler;
import com.runescape.game.world.entity.player.achievements.easy.Grand_Exchanger;
import com.runescape.game.world.entity.player.achievements.easy.The_Resurrection;
import com.runescape.game.world.entity.player.achievements.medium.Ruthless;
import com.runescape.game.world.entity.player.actions.PlayerCombat;
import com.runescape.game.world.entity.player.pet.PetManager;
import com.runescape.game.world.entity.player.rights.Right;
import com.runescape.game.world.entity.player.rights.RightManager;
import com.runescape.game.world.item.FloorItem;
import com.runescape.game.world.item.Item;
import com.runescape.game.world.item.ItemConstants;
import com.runescape.game.world.item.ItemDegrading.DegradeType;
import com.runescape.network.Session;
import com.runescape.network.codec.decoders.handlers.ButtonHandler;
import com.runescape.network.codec.encoders.WorldPacketsEncoder;
import com.runescape.utility.ChatColors;
import com.runescape.utility.Utils;
import com.runescape.utility.cache.IsaacKeyPair;
import com.runescape.utility.external.gson.GsonStartup;
import com.runescape.utility.external.gson.loaders.ExchangeItemLoader;
import com.runescape.utility.external.gson.loaders.LentItemsLoader;
import com.runescape.utility.tools.WebPage;
import com.runescape.utility.world.player.PkRank;
import com.runescape.utility.world.player.PlayerSaving;
import com.runescape.workers.db.DatabaseConnection;
import com.runescape.workers.db.mysql.impl.DatabaseFunctions;
import com.runescape.workers.game.core.CoresManager;
import com.runescape.workers.game.log.GameLog;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;
import com.runescape.workers.tasks.impl.ShootingStarTick;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Player extends Entity {

	public static final int TELE_MOVE_TYPE = 127, WALK_MOVE_TYPE = 1, RUN_MOVE_TYPE = 2;

	private static final long serialVersionUID = 2011932556974180375L;

	private int temporaryMovementType;

	private boolean updateMovementType;

	private String passwordHash;

	private String macAddress;

	private String displayName;

	private String lastIP;

	private Appearence appearence;

	private Inventory inventory;

	private DwarfCannon dwarfCannon;

	private BankPinManager pinManager;

	private BossKillTimeManager killTimeManager;

	private SecurityDetails securityDetails;

	private LoyaltyManager loyaltyManager;

	private CostumeManager costumeManager;

	private Equipment equipment;

	private Skills skills;

	private BrawlingGlovesManager brawlingGlovesManager;

	private CombatDefinitions combatDefinitions;

	private Prayer prayer;

	private Bank bank;

	private ControllerManager controllerManager;

	private PresetManager presetManager;

	private MusicsManager musicsManager;

	private EmotesManager emotesManager;

	private FriendsIgnores friendsIgnores;

	private DominionTower dominionTower;

	private FarmingManager farmingManager;

	private FamiliarSerialization familiarSerialization;

	private QuestManager questManager;

	private RandomEventManager randomEventManager;

	private AuraManager auraManager;

	private PetManager petManager;

	private PlayerFacade facade;

	private DegradeManager degradeManager;

	private House house;

	private double runEnergy;

	private boolean allowChatEffects;

	private boolean mouseButtons;

	private int privateChatSetup;

	private int friendChatSetup;

	private int skullDelay;

	private int skullId;

	private boolean forceNextMapLoadRefresh;

	private long poisonImmune;

	// interface

	private long fireImmune;

	private boolean killedQueenBlackDragon;

	private int runeSpanPoints;

	private int[] pouches;

	private boolean filterGame;

	// game bar status
	private int publicStatus;

	private int clanStatus;

	private int tradeStatus;

	private int assistStatus;

	private String lastMsg;

	private double[] warriorPoints;

	// Used for storing recent ips and password
	private ArrayList<String> ipList = new ArrayList<>();

	/**
	 * The mac address of the player we last killed
	 */
	private String lastKilled;

	/**
	 * The map of the recent macs the player has killed
	 */
	private List<KillInformation> recentKills;

	// honor
	private int killCount, deathCount;

	// barrows
	private boolean[] killedBarrowBrothers;

	private int hiddenBrother;

	private int barrowsKillCount;

	private int pestPoints;

	// skill capes customizing
	private int[] maxedCapeCustomized;

	private int[] completionistCapeCustomized;

	// completionistcape reqs
	private boolean completedFightCaves;

	private boolean completedFightKiln;

	private boolean wonFightPits;

	// crucible
	private boolean talkedWithMarv;

	private int crucibleHighScore;

	private int overloadDelay;

	private int prayerRenewalDelay;

	private String currentFriendChatOwner = "Tyluur";

	private int summoningLeftClickOption;

	private List<String> ownedObjectsManagerKeys;

	// objects
	private boolean khalphiteLairEntranceSetted;

	private boolean khalphiteLairSetted;

	// voting
	private int votes;

	private boolean oldItemsLook = true;

	private String clanName;

	private int clanChatSetup;

	private boolean connectedClanChannel;

	private int guestChatSetup;

	private boolean filteringProfanity;

	private List<Item> lostUntradeables;

	// transient stuff
	private transient String username;

	private transient String password;

	private transient Session session;

	private transient boolean clientLoadedMapRegion;

	private transient long signInTime;

	private transient int displayMode;

	private transient int screenWidth;

	private transient int screenHeight;

	private transient InterfaceManager interfaceManager;

	private transient DialogueManager dialogueManager;

	private transient HintIconsManager hintIconsManager;

	private transient ActionManager actionManager;

	private transient CutscenesManager cutscenesManager;

	private transient PriceCheckManager priceCheckManager;

	private transient FriendChatsManager currentFriendChat;

	private transient Object lastChatMessage;

	private transient Trade trade;

	private transient DuelRules lastDuelRules;

	private transient IsaacKeyPair isaacKeyPair;

	private transient RouteEvent routeEvent;

	private transient ClansManager clanManager;

	private transient ClansManager guestClanManager;

	private transient Pet pet;

	// used for packets logic
	private transient ConcurrentLinkedQueue<LogicPacket> logicPackets;

	// used for update
	private transient LocalPlayerUpdate localPlayerUpdate;

	private transient LocalNPCUpdate localNPCUpdate;

	// player stages
	private transient boolean started;

	private transient boolean running;

	private transient boolean resting;

	private transient boolean canPvp;

	private transient boolean cantTrade;

	private transient int chatType;

	private transient Runnable closeInterfacesEvent;

	private transient Runnable logicPacketProcessEvent;

	private transient long lastPublicMessage;

	private transient long polDelay;

	private transient List<Integer> switchItemCache;

	private transient List<Right> rights;

	private transient Right primaryRight;

	private transient ClientVarpsManager clientVarpsManager;

	private transient boolean spawnsMode;

	private transient boolean invulnerable;

	private transient double hpBoostMultiplier;

	private transient boolean largeSceneView;

	private transient Familiar familiar;

	private transient LockManagement lockManagement;

	private transient boolean securityQuestionsAnswered;

	private transient boolean finishing;

	private transient Notes notes;

	private transient Map<String, Long> processData;

	@Override
	public void finish() {
		finish(0);
	}

	@Override
	public double getMagePrayerMultiplier() {
		return 0.6;
	}

	@Override
	public double getRangePrayerMultiplier() {
		return 0.6;
	}

	@Override
	public double getMeleePrayerMultiplier() {
		return 0.6;
	}

	@Override
	public void reset() {
		reset(true);
	}

	@Override
	public void reset(boolean attributes) {
		super.reset(attributes);
		refreshHitPoints();
		hintIconsManager.removeAll();
		skills.restoreSkills();
		combatDefinitions.resetSpecialAttack();
		prayer.reset();
		combatDefinitions.resetSpells(true);
		resting = false;
		skullDelay = 0;
		poisonImmune = 0;
		fireImmune = 0;
		setFreezeDelay(0);
		lockManagement.unlockAll();
		setRunEnergy(100);
		appearence.generateAppearenceData();
	}

	@Override
	public int getMaxHitpoints() {
		return skills.getLevel(Skills.HITPOINTS) * 10 + equipment.getEquipmentHpIncrease();
	}

	@Override
	public boolean isPlayer() {
		return true;
	}

	@Override
	public int getSize() {
		return appearence.getSize();
	}

	@Override
	public boolean restoreHitPoints() {
		boolean update = super.restoreHitPoints();
		if (update) {
			if (prayer.usingPrayer(0, 9)) {
				super.restoreHitPoints();
			}
			if (resting) {
				super.restoreHitPoints();
			}
			refreshHitPoints();
		}
		return update;
	}

	@Override
	public boolean needMasksUpdate() {
		return super.needMasksUpdate() || temporaryMovementType != -1 || updateMovementType;
	}

	@Override
	public void resetMasks() {
		super.resetMasks();
		temporaryMovementType = -1;
		updateMovementType = false;
		if (!clientHasLoadedMapRegion()) {
			refreshSpawnedObjects();
			refreshSpawnedItems();
			setClientHasLoadedMapRegion();
		}
	}

	@Override
	public long processEntity() {
		if (processData == null) {
			processData = new ConcurrentHashMap<>();
		}
		processData.clear();
		long initial = System.currentTimeMillis();
		long start = System.currentTimeMillis();

		super.processEntity();
		start = storeProcessData("super.processEntity()", start);
//		session.processQueuedStream();
//		start = storeProcessData("sessionqueue", start);
		cutscenesManager.process();
		start = storeProcessData("custscenes", start);
		processSwitches();
		start = storeProcessData("switches", start);
		if (routeEvent != null && routeEvent.processEvent(this, 0)) {
			routeEvent = null;
		}
		start = storeProcessData("Routeevent", start);
		if (musicsManager.musicEnded()) {
			musicsManager.replayMusic();
		}
		start = storeProcessData("musicmanager", start);
		if (hasSkull()) {
			skullDelay--;
			if (!hasSkull()) {
				appearence.generateAppearenceData();
			}
		}
		start = storeProcessData("hasskull", start);
		if (polDelay != 0 && polDelay <= Utils.currentTimeMillis()) {
			getPackets().sendGameMessage("The power of the light fades. Your resistance to melee attacks return to normal.");
			polDelay = 0;
		}
		start = storeProcessData("pldelay", start);
		if (overloadDelay > 0) {
			if (overloadDelay == 1 || isDead()) {
				Pots.resetOverLoadEffect(this);
				return start;
			} else if ((overloadDelay - 1) % 25 == 0) {
				Pots.applyOverLoadEffect(this);
			}
			overloadDelay--;
		}
		start = storeProcessData("overloaddelay", start);
		if (getAttribute("greegree_worn") != null) {
			Greegree greegree = getAttribute("greegree_worn");
			short transformedId = appearence.getTransformedNpcId();
			// we should transform into the npc
			if (transformedId == -1) {
				appearence.transformIntoNPC(greegree.getNpcId());
			} else {
				// if we should transform back to the original form
				if (GreegreeHandler.shouldTransformBack(this, greegree)) {
					// we transform back
					GreegreeHandler.transformBack(this);
				}
			}
		}
		start = storeProcessData("greegreeworn", start);
		if (prayerRenewalDelay > 0) {
			if (prayerRenewalDelay == 1 || isDead()) {
				getPackets().sendGameMessage("<col=0000FF>Your prayer renewal has ended.");
				prayerRenewalDelay = 0;
				return System.currentTimeMillis() - start;
			} else {
				if (prayerRenewalDelay == 50) {
					getPackets().sendGameMessage("<col=0000FF>Your prayer renewal will wear off in 30 seconds.");
				}
				if (!prayer.hasFullPrayerpoints()) {
					getPrayer().restorePrayer(1);
					if ((prayerRenewalDelay - 1) % 25 == 0) {
						setNextGraphics(new Graphics(1295));
					}
				}
			}
			prayerRenewalDelay--;
		}
		start = storeProcessData("prayerrenewal", start);
		degradeManager.process(DegradeType.TIMED_DEGRADE);
		start = storeProcessData("degrademanageR", start);
		auraManager.process();
		start = storeProcessData("auramanager", start);
		actionManager.process();
		start = storeProcessData("actionmanager", start);
		prayer.processPrayer();
		start = storeProcessData("prayerprocess", start);
		controllerManager.process();
		start = storeProcessData("cotnrollermanager", start);
		randomEventManager.process();
		start = storeProcessData("randomevent", start);
		farmingManager.process();
		start = storeProcessData("farmingmanager", start);
		loyaltyManager.process(this);
		start = storeProcessData("loyaltymanager", start);
		prayer.processPrayerDrain();
		start = storeProcessData("drainprayer", start);
		getRegion().increaseTimeSpent(this);
		storeProcessData("region", start);
		checkDonatorEligibility();
		storeProcessData("donatorCheck", start);
		return System.currentTimeMillis() - initial;
	}

	private long storeProcessData(String key, long start) {
		processData.put(key, System.currentTimeMillis() - start);
		start = System.currentTimeMillis();
		return start;
	}

	@Override
	public void processReceivedHits() {
		if (lockManagement.isLocked(LockType.DAMAGE)) {
			return;
		}
		super.processReceivedHits();
	}

	@Override
	public void loadMapRegions() {
		boolean wasAtDynamicRegion = isAtDynamicRegion();
		super.loadMapRegions();
		clientLoadedMapRegion = false;
		if (isAtDynamicRegion()) {
			getPackets().sendDynamicMapRegion(!started);
			if (!wasAtDynamicRegion) {
				localNPCUpdate.reset();
			}
		} else {
			getPackets().sendMapRegion(!started);
			if (wasAtDynamicRegion) {
				localNPCUpdate.reset();
			}
		}
		forceNextMapLoadRefresh = false;
	}

	@Override
	public void removeHitpoints(Hit hit) {
		super.removeHitpoints(hit);
		refreshHitPoints();
	}

	@Override
	public void sendDeath(final Entity source) {
		if (prayer.hasPrayersOn() && getAttributes().get("startedDuel") != Boolean.TRUE) {
			if (prayer.usingPrayer(0, 22)) {
				setNextGraphics(new Graphics(437));
				final Player target = this;
				if (isAtMultiArea()) {
					for (int regionId : getMapRegionsIds()) {
						List<Integer> playersIndexes = World.getRegion(regionId).getPlayerIndexes();
						if (playersIndexes != null) {
							for (int playerIndex : playersIndexes) {
								Player player = World.getPlayers().get(playerIndex);
								if (player == null || !player.hasStarted() || player.isDead() || player.hasFinished() || !player.withinDistance(this, 1) || !player.isCanPvp() || !target.getControllerManager().canHit(player)) {
									continue;
								}
								player.applyHit(new Hit(target, Utils.getRandom((int) (skills.getLevelForXp(Skills.PRAYER) * 2.5)), HitLook.REGULAR_DAMAGE));
							}
						}
						List<Integer> npcsIndexes = World.getRegion(regionId).getNPCsIndexes();
						if (npcsIndexes != null) {
							for (int npcIndex : npcsIndexes) {
								NPC npc = World.getNPCs().get(npcIndex);
								if (npc == null || npc.isDead() || npc.hasFinished() || !npc.withinDistance(this, 1) || !npc.getDefinitions().hasAttackOption() || !target.getControllerManager().canHit(npc)) {
									continue;
								}
								npc.applyHit(new Hit(target, Utils.getRandom((int) (skills.getLevelForXp(Skills.PRAYER) * 2.5)), HitLook.REGULAR_DAMAGE));
							}
						}
					}
				} else {
					if (source != null && source != this && !source.isDead() && !source.hasFinished() && source.withinDistance(this, 1)) {
						source.applyHit(new Hit(target, Utils.getRandom((int) (skills.getLevelForXp(Skills.PRAYER) * 2.5)), HitLook.REGULAR_DAMAGE));
					}
				}
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						World.sendGraphics(target, new Graphics(438), new WorldTile(target.getX() - 1, target.getY(), target.getPlane()));
						World.sendGraphics(target, new Graphics(438), new WorldTile(target.getX() + 1, target.getY(), target.getPlane()));
						World.sendGraphics(target, new Graphics(438), new WorldTile(target.getX(), target.getY() - 1, target.getPlane()));
						World.sendGraphics(target, new Graphics(438), new WorldTile(target.getX(), target.getY() + 1, target.getPlane()));
						World.sendGraphics(target, new Graphics(438), new WorldTile(target.getX() - 1, target.getY() - 1, target.getPlane()));
						World.sendGraphics(target, new Graphics(438), new WorldTile(target.getX() - 1, target.getY() + 1, target.getPlane()));
						World.sendGraphics(target, new Graphics(438), new WorldTile(target.getX() + 1, target.getY() - 1, target.getPlane()));
						World.sendGraphics(target, new Graphics(438), new WorldTile(target.getX() + 1, target.getY() + 1, target.getPlane()));
					}
				});
			} else if (prayer.usingPrayer(1, 17)) {
				World.sendProjectile(this, new WorldTile(getX() + 2, getY() + 2, getPlane()), 2260, 24, 0, 41, 35, 30, 0);
				World.sendProjectile(this, new WorldTile(getX() + 2, getY(), getPlane()), 2260, 41, 0, 41, 35, 30, 0);
				World.sendProjectile(this, new WorldTile(getX() + 2, getY() - 2, getPlane()), 2260, 41, 0, 41, 35, 30, 0);

				World.sendProjectile(this, new WorldTile(getX() - 2, getY() + 2, getPlane()), 2260, 41, 0, 41, 35, 30, 0);
				World.sendProjectile(this, new WorldTile(getX() - 2, getY(), getPlane()), 2260, 41, 0, 41, 35, 30, 0);
				World.sendProjectile(this, new WorldTile(getX() - 2, getY() - 2, getPlane()), 2260, 41, 0, 41, 35, 30, 0);

				World.sendProjectile(this, new WorldTile(getX(), getY() + 2, getPlane()), 2260, 41, 0, 41, 35, 30, 0);
				World.sendProjectile(this, new WorldTile(getX(), getY() - 2, getPlane()), 2260, 41, 0, 41, 35, 30, 0);
				final Player target = this;
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						setNextGraphics(new Graphics(2259));

						if (isAtMultiArea()) {
							for (int regionId : getMapRegionsIds()) {
								List<Integer> playersIndexes = World.getRegion(regionId).getPlayerIndexes();
								if (playersIndexes != null) {
									for (int playerIndex : playersIndexes) {
										Player player = World.getPlayers().get(playerIndex);
										if (player == null || !player.hasStarted() || player.isDead() || player.hasFinished() || !player.isCanPvp() || !player.withinDistance(target, 2) || !target.getControllerManager().canHit(player)) {
											continue;
										}
										player.applyHit(new Hit(target, Utils.getRandom((skills.getLevelForXp(Skills.PRAYER) * 3)), HitLook.REGULAR_DAMAGE));
									}
								}
								List<Integer> npcsIndexes = World.getRegion(regionId).getNPCsIndexes();
								if (npcsIndexes != null) {
									for (int npcIndex : npcsIndexes) {
										NPC npc = World.getNPCs().get(npcIndex);
										if (npc == null || npc.isDead() || npc.hasFinished() || !npc.withinDistance(target, 2) || !npc.getDefinitions().hasAttackOption() || !target.getControllerManager().canHit(npc)) {
											continue;
										}
										npc.applyHit(new Hit(target, Utils.getRandom((skills.getLevelForXp(Skills.PRAYER) * 3)), HitLook.REGULAR_DAMAGE));
									}
								}
							}
						} else {
							if (source != null && source != target && !source.isDead() && !source.hasFinished() && source.withinDistance(target, 2)) {
								source.applyHit(new Hit(target, Utils.getRandom((skills.getLevelForXp(Skills.PRAYER) * 3)), HitLook.REGULAR_DAMAGE));
							}
						}

						World.sendGraphics(target, new Graphics(2260), new WorldTile(getX() + 2, getY() + 2, getPlane()));
						World.sendGraphics(target, new Graphics(2260), new WorldTile(getX() + 2, getY(), getPlane()));
						World.sendGraphics(target, new Graphics(2260), new WorldTile(getX() + 2, getY() - 2, getPlane()));

						World.sendGraphics(target, new Graphics(2260), new WorldTile(getX() - 2, getY() + 2, getPlane()));
						World.sendGraphics(target, new Graphics(2260), new WorldTile(getX() - 2, getY(), getPlane()));
						World.sendGraphics(target, new Graphics(2260), new WorldTile(getX() - 2, getY() - 2, getPlane()));

						World.sendGraphics(target, new Graphics(2260), new WorldTile(getX(), getY() + 2, getPlane()));
						World.sendGraphics(target, new Graphics(2260), new WorldTile(getX(), getY() - 2, getPlane()));

						World.sendGraphics(target, new Graphics(2260), new WorldTile(getX() + 1, getY() + 1, getPlane()));
						World.sendGraphics(target, new Graphics(2260), new WorldTile(getX() + 1, getY() - 1, getPlane()));
						World.sendGraphics(target, new Graphics(2260), new WorldTile(getX() - 1, getY() + 1, getPlane()));
						World.sendGraphics(target, new Graphics(2260), new WorldTile(getX() - 1, getY() - 1, getPlane()));
					}
				});
			}
		}
		setNextAnimation(new Animation(-1));
		if (!controllerManager.sendDeath()) {
			return;
		}
		stopAll();
		getLockManagement().lockAll(7000);
		if (familiar != null) {
			familiar.sendDeath(this);
		}
		final Player thisPlayer = this;
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					setNextAnimation(new Animation(836));
				} else if (loop == 1) {
					getPackets().sendGameMessage("Oh dear, you have died.");
					if (source instanceof Player) {
						Player killer = (Player) source;
						killer.setAttackedByDelay(4);
					}
				} else if (loop == 3) {
					thisPlayer.sendItemsOnDeath(thisPlayer);
					thisPlayer.getEquipment().init();
					thisPlayer.getInventory().init();
					thisPlayer.reset();
					thisPlayer.setNextWorldTile(new WorldTile(GameConstants.RESPAWN_PLAYER_LOCATION));
					thisPlayer.setNextAnimation(new Animation(-1));
				} else if (loop == 4) {
					getPackets().sendMusicEffect(90);
					stop();
				}
				loop++;
			}
		}, 0, 1);
	}

	@Override
	public void heal(int ammount, int extra) {
		super.heal(ammount, extra);
		refreshHitPoints();
	}

	@Override
	public void handleIngoingHit(final Hit hit) {
		if (hit.getLook() != HitLook.MELEE_DAMAGE && hit.getLook() != HitLook.RANGE_DAMAGE && hit.getLook() != HitLook.MAGIC_DAMAGE) {
			return;
		}
		if (getAttribute("god_mode", false) && hit.getDamage() > 0) {
			hit.setSoaking(new Hit(hit.getSource(), hit.getDamage(), HitLook.ABSORB_DAMAGE));
			hit.setDamage(0);
		}
		if (invulnerable) {
			hit.setDamage(0);
			return;
		}
		if (auraManager.usingPenance()) {
			int amount = (int) (hit.getDamage() * 0.2);
			if (amount > 0) {
				prayer.restorePrayer(amount);
			}
		}
		Entity source = hit.getSource();
		if (source == null) {
			return;
		}
		if (polDelay > Utils.currentTimeMillis()) {
			hit.setDamage((int) (hit.getDamage() * 0.5));
		}
		if (prayer.hasPrayersOn() && hit.getDamage() != 0) {
			if (hit.getLook() == HitLook.MAGIC_DAMAGE) {
				if (prayer.usingPrayer(0, 17)) {
					hit.setDamage((int) (hit.getDamage() * source.getMagePrayerMultiplier()));
				} else if (prayer.usingPrayer(1, 7)) {
					int deflectedDamage = source instanceof Nex ? 0 : (int) (hit.getDamage() * 0.1);
					hit.setDamage((int) (hit.getDamage() * source.getMagePrayerMultiplier()));
					if (deflectedDamage > 0) {
						source.applyHit(new Hit(this, deflectedDamage, HitLook.REFLECTED_DAMAGE));
						setNextGraphics(new Graphics(2228));
						setNextAnimation(new Animation(12573));
					}
				}
			} else if (hit.getLook() == HitLook.RANGE_DAMAGE) {
				if (prayer.usingPrayer(0, 18)) {
					hit.setDamage((int) (hit.getDamage() * source.getRangePrayerMultiplier()));
				} else if (prayer.usingPrayer(1, 8)) {
					int deflectedDamage = source instanceof Nex ? 0 : (int) (hit.getDamage() * 0.1);
					hit.setDamage((int) (hit.getDamage() * source.getRangePrayerMultiplier()));
					if (deflectedDamage > 0) {
						source.applyHit(new Hit(this, deflectedDamage, HitLook.REFLECTED_DAMAGE));
						setNextGraphics(new Graphics(2229));
						setNextAnimation(new Animation(12573));
					}
				}
			} else if (hit.getLook() == HitLook.MELEE_DAMAGE) {
				if (prayer.usingPrayer(0, 19)) {
					hit.setDamage((int) (hit.getDamage() * source.getMeleePrayerMultiplier()));
				} else if (prayer.usingPrayer(1, 9)) {
					int deflectedDamage = source instanceof Nex ? 0 : (int) (hit.getDamage() * 0.1);
					hit.setDamage((int) (hit.getDamage() * source.getMeleePrayerMultiplier()));
					if (deflectedDamage > 0) {
						source.applyHit(new Hit(this, deflectedDamage, HitLook.REFLECTED_DAMAGE));
						setNextGraphics(new Graphics(2230));
						setNextAnimation(new Animation(12573));
					}
				}
			}
		}
		if (hit.getDamage() >= 200) {
			if (hit.getLook() == HitLook.MELEE_DAMAGE) {
				int reducedDamage = hit.getDamage() * combatDefinitions.getBonuses()[CombatDefinitions.ABSORVE_MELEE_BONUS] / 100;
				if (reducedDamage > 0) {
					hit.setDamage(hit.getDamage() - reducedDamage);
					hit.setSoaking(new Hit(source, reducedDamage, HitLook.ABSORB_DAMAGE));
				}
			} else if (hit.getLook() == HitLook.RANGE_DAMAGE) {
				int reducedDamage = hit.getDamage() * combatDefinitions.getBonuses()[CombatDefinitions.ABSORVE_RANGE_BONUS] / 100;
				if (reducedDamage > 0) {
					hit.setDamage(hit.getDamage() - reducedDamage);
					hit.setSoaking(new Hit(source, reducedDamage, HitLook.ABSORB_DAMAGE));
				}
			} else if (hit.getLook() == HitLook.MAGIC_DAMAGE) {
				int reducedDamage = hit.getDamage() * combatDefinitions.getBonuses()[CombatDefinitions.ABSORVE_MAGE_BONUS] / 100;
				if (reducedDamage > 0) {
					hit.setDamage(hit.getDamage() - reducedDamage);
					hit.setSoaking(new Hit(source, reducedDamage, HitLook.ABSORB_DAMAGE));
				}
			}
		}
		int shieldId = equipment.getShieldId();
		if (shieldId == 13742) { // elsyian
			if (Utils.getRandom(100) <= 70) {
				hit.setDamage((int) (hit.getDamage() * 0.75));
			}
		} else if (shieldId == 13740) { // divine
			int drain = (int) (Math.ceil(hit.getDamage() * 0.3) / 2);
			if (prayer.getPrayerpoints() >= drain) {
				hit.setDamage((int) (hit.getDamage() * 0.70));
				prayer.drainPrayer(drain);
			}
		}
		if (getAttribute("cast_veng", false) && hit.getDamage() >= 4) {
			setNextForceTalk(new ForceTalk("Taste vengeance!"));
			removeAttribute("cast_veng");
			source.applyHit(new Hit(this, (int) (hit.getDamage() * 0.75), HitLook.REGULAR_DAMAGE));
		}
		if (source instanceof Player) {
			final Player p2 = (Player) source;
			if (p2.prayer.hasPrayersOn()) {
				if (p2.prayer.usingPrayer(0, 24)) { // smite
					int drain = hit.getDamage() / 4;
					if (drain > 0) {
						prayer.drainPrayer(drain);
					}
				} else {
					if (hit.getDamage() == 0) {
						return;
					}
					if (!p2.prayer.isBoostedLeech()) {
						if (hit.getLook() == HitLook.MELEE_DAMAGE) {
							if (p2.prayer.usingPrayer(1, 19)) {
								if (Utils.getRandom(4) == 0) {
									p2.prayer.increaseTurmoilBonus(this);
									p2.prayer.setBoostedLeech(true);
									return;
								}
							} else if (p2.prayer.usingPrayer(1, 1)) { // sap att
								if (Utils.getRandom(4) == 0) {
									if (p2.prayer.reachedMax(0)) {
										p2.getPackets().sendGameMessage("Your opponent has been weakened so much that your sap curse has no effect.", true);
									} else {
										p2.prayer.increaseLeechBonus(0);
										p2.getPackets().sendGameMessage("Your curse drains Attack from the enemy, boosting your Attack.", true);
									}
									p2.setNextAnimation(new Animation(12569));
									p2.setNextGraphics(new Graphics(2214));
									p2.prayer.setBoostedLeech(true);
									World.sendProjectile(p2, this, 2215, 35, 35, 20, 5, 0, 0);
									WorldTasksManager.schedule(new WorldTask() {
										@Override
										public void run() {
											setNextGraphics(new Graphics(2216));
										}
									}, 1);
									return;
								}
							} else {
								if (p2.prayer.usingPrayer(1, 10)) {
									if (Utils.getRandom(7) == 0) {
										if (p2.prayer.reachedMax(3)) {
											p2.getPackets().sendGameMessage("Your opponent has been weakened so much that your leech curse has no effect.", true);
										} else {
											p2.prayer.increaseLeechBonus(3);
											p2.getPackets().sendGameMessage("Your curse drains Attack from the enemy, boosting your Attack.", true);
										}
										p2.setNextAnimation(new Animation(12575));
										p2.prayer.setBoostedLeech(true);
										World.sendProjectile(p2, this, 2231, 35, 35, 20, 5, 0, 0);
										WorldTasksManager.schedule(new WorldTask() {
											@Override
											public void run() {
												setNextGraphics(new Graphics(2232));
											}
										}, 1);
										return;
									}
								}
								if (p2.prayer.usingPrayer(1, 14)) {
									if (Utils.getRandom(7) == 0) {
										if (p2.prayer.reachedMax(7)) {
											p2.getPackets().sendGameMessage("Your opponent has been weakened so much that your leech curse has no effect.", true);
										} else {
											p2.prayer.increaseLeechBonus(7);
											p2.getPackets().sendGameMessage("Your curse drains Strength from the enemy, boosting your Strength.", true);
										}
										p2.setNextAnimation(new Animation(12575));
										p2.prayer.setBoostedLeech(true);
										World.sendProjectile(p2, this, 2248, 35, 35, 20, 5, 0, 0);
										WorldTasksManager.schedule(new WorldTask() {
											@Override
											public void run() {
												setNextGraphics(new Graphics(2250));
											}
										}, 1);
										return;
									}
								}

							}
						}
						if (hit.getLook() == HitLook.RANGE_DAMAGE) {
							if (p2.prayer.usingPrayer(1, 2)) { // sap range
								if (Utils.getRandom(4) == 0) {
									if (p2.prayer.reachedMax(1)) {
										p2.getPackets().sendGameMessage("Your opponent has been weakened so much that your sap curse has no effect.", true);
									} else {
										p2.prayer.increaseLeechBonus(1);
										p2.getPackets().sendGameMessage("Your curse drains Range from the enemy, boosting your Range.", true);
									}
									p2.setNextAnimation(new Animation(12569));
									p2.setNextGraphics(new Graphics(2217));
									p2.prayer.setBoostedLeech(true);
									World.sendProjectile(p2, this, 2218, 35, 35, 20, 5, 0, 0);
									WorldTasksManager.schedule(new WorldTask() {
										@Override
										public void run() {
											setNextGraphics(new Graphics(2219));
										}
									}, 1);
									return;
								}
							} else if (p2.prayer.usingPrayer(1, 11)) {
								if (Utils.getRandom(7) == 0) {
									if (p2.prayer.reachedMax(4)) {
										p2.getPackets().sendGameMessage("Your opponent has been weakened so much that your leech curse has no effect.", true);
									} else {
										p2.prayer.increaseLeechBonus(4);
										p2.getPackets().sendGameMessage("Your curse drains Range from the enemy, boosting your Range.", true);
									}
									p2.setNextAnimation(new Animation(12575));
									p2.prayer.setBoostedLeech(true);
									World.sendProjectile(p2, this, 2236, 35, 35, 20, 5, 0, 0);
									WorldTasksManager.schedule(new WorldTask() {
										@Override
										public void run() {
											setNextGraphics(new Graphics(2238));
										}
									});
									return;
								}
							}
						}
						if (hit.getLook() == HitLook.MAGIC_DAMAGE) {
							if (p2.prayer.usingPrayer(1, 3)) { // sap mage
								if (Utils.getRandom(4) == 0) {
									if (p2.prayer.reachedMax(2)) {
										p2.getPackets().sendGameMessage("Your opponent has been weakened so much that your sap curse has no effect.", true);
									} else {
										p2.prayer.increaseLeechBonus(2);
										p2.getPackets().sendGameMessage("Your curse drains Magic from the enemy, boosting your Magic.", true);
									}
									p2.setNextAnimation(new Animation(12569));
									p2.setNextGraphics(new Graphics(2220));
									p2.prayer.setBoostedLeech(true);
									World.sendProjectile(p2, this, 2221, 35, 35, 20, 5, 0, 0);
									WorldTasksManager.schedule(new WorldTask() {
										@Override
										public void run() {
											setNextGraphics(new Graphics(2222));
										}
									}, 1);
									return;
								}
							} else if (p2.prayer.usingPrayer(1, 12)) {
								if (Utils.getRandom(7) == 0) {
									if (p2.prayer.reachedMax(5)) {
										p2.getPackets().sendGameMessage("Your opponent has been weakened so much that your leech curse has no effect.", true);
									} else {
										p2.prayer.increaseLeechBonus(5);
										p2.getPackets().sendGameMessage("Your curse drains Magic from the enemy, boosting your Magic.", true);
									}
									p2.setNextAnimation(new Animation(12575));
									p2.prayer.setBoostedLeech(true);
									World.sendProjectile(p2, this, 2240, 35, 35, 20, 5, 0, 0);
									WorldTasksManager.schedule(new WorldTask() {
										@Override
										public void run() {
											setNextGraphics(new Graphics(2242));
										}
									}, 1);
									return;
								}
							}
						}

						if (p2.prayer.usingPrayer(1, 13)) { // leech defence
							if (Utils.getRandom(10) == 0) {
								if (p2.prayer.reachedMax(6)) {
									p2.getPackets().sendGameMessage("Your opponent has been weakened so much that your leech curse has no effect.", true);
								} else {
									p2.prayer.increaseLeechBonus(6);
									p2.getPackets().sendGameMessage("Your curse drains Defence from the enemy, boosting your Defence.", true);
								}
								p2.setNextAnimation(new Animation(12575));
								p2.prayer.setBoostedLeech(true);
								World.sendProjectile(p2, this, 2244, 35, 35, 20, 5, 0, 0);
								WorldTasksManager.schedule(new WorldTask() {
									@Override
									public void run() {
										setNextGraphics(new Graphics(2246));
									}
								}, 1);
								return;
							}
						}

						if (p2.prayer.usingPrayer(1, 15)) {
							if (Utils.getRandom(10) == 0) {
								if (getRunEnergy() <= 0) {
									p2.getPackets().sendGameMessage("Your opponent has been weakened so much that your leech curse has no effect.", true);
								} else {
									p2.setRunEnergy(p2.getRunEnergy() > 90 ? 100 : p2.getRunEnergy() + 10);
									setRunEnergy(p2.getRunEnergy() > 10 ? getRunEnergy() - 10 : 0);
								}
								p2.setNextAnimation(new Animation(12575));
								p2.prayer.setBoostedLeech(true);
								World.sendProjectile(p2, this, 2256, 35, 35, 20, 5, 0, 0);
								WorldTasksManager.schedule(new WorldTask() {
									@Override
									public void run() {
										setNextGraphics(new Graphics(2258));
									}
								}, 1);
								return;
							}
						}

						if (p2.prayer.usingPrayer(1, 16)) {
							if (Utils.getRandom(10) == 0) {
								if (combatDefinitions.getSpecialAttackPercentage() <= 0) {
									p2.getPackets().sendGameMessage("Your opponent has been weakened so much that your leech curse has no effect.", true);
								} else {
									p2.combatDefinitions.restoreSpecialAttack();
									combatDefinitions.desecreaseSpecialAttack(10);
								}
								p2.setNextAnimation(new Animation(12575));
								p2.prayer.setBoostedLeech(true);
								World.sendProjectile(p2, this, 2252, 35, 35, 20, 5, 0, 0);
								WorldTasksManager.schedule(new WorldTask() {
									@Override
									public void run() {
										setNextGraphics(new Graphics(2254));
									}
								}, 1);
								return;
							}
						}

						if (p2.prayer.usingPrayer(1, 4)) { // sap spec
							if (Utils.getRandom(10) == 0) {
								p2.setNextAnimation(new Animation(12569));
								p2.setNextGraphics(new Graphics(2223));
								p2.prayer.setBoostedLeech(true);
								if (combatDefinitions.getSpecialAttackPercentage() <= 0) {
									p2.getPackets().sendGameMessage("Your opponent has been weakened so much that your sap curse has no effect.", true);
								} else {
									combatDefinitions.desecreaseSpecialAttack(10);
								}
								World.sendProjectile(p2, this, 2224, 35, 35, 20, 5, 0, 0);
								WorldTasksManager.schedule(new WorldTask() {
									@Override
									public void run() {
										setNextGraphics(new Graphics(2225));
									}
								}, 1);
							}
						}
					}
				}
			}
		} else {
			NPC n = (NPC) source;
			if (n.getId() == 13448) {
				sendSoulSplit(hit, n);
			}
		}
	}

	@Override
	public void setRun(boolean run) {
		if (run != getRun()) {
			super.setRun(run);
			updateMovementType = true;
			sendRunButtonConfig();
		}
	}

	@Override
	public void checkMultiArea() {
		if (!started) {
			return;
		}
		boolean isAtMultiArea = isForceMultiArea() || World.isMultiArea(this);
		if (isAtMultiArea && !isAtMultiArea()) {
			setAtMultiArea(isAtMultiArea);
			getPackets().sendGlobalConfig(616, 1);
		} else if (!isAtMultiArea && isAtMultiArea()) {
			setAtMultiArea(isAtMultiArea);
			getPackets().sendGlobalConfig(616, 0);
		}
	}

	@Override
	public boolean canBeAttacked(Player player) {
		if (invalidDueToSecurityQuestions()) {
			player.sendMessage(getDisplayName() + " cannot be attacked yet.");
			return false;
		}
		return true;
	}

	public Player player() {
		return this;
	}

	public boolean invalidDueToSecurityQuestions() {
		return securityDetails.hasSecurityEnabled() && !securityQuestionsAnswered;
	}

	/**
	 * Sends a message
	 */
	public void sendMessage(String text, boolean... filter) {
		getPackets().sendGameMessage(text, filter);
	}

	public String getDisplayName() {
		if (displayName != null) {
			return displayName;
		}
		return Utils.formatPlayerNameForDisplay(username);
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void sendRunButtonConfig() {
		getPackets().sendConfig(173, resting ? 3 : getRun() ? 1 : 0);
	}

	public void setRunEnergy(double runEnergy) {
		if (runEnergy > 100) {
			runEnergy = 100;
		}
		this.runEnergy = runEnergy;
		getPackets().sendRunEnergy();
	}

	public void sendSoulSplit(final Hit hit, final Entity user) {
		final Player target = this;
		if (hit.getDamage() > 0) {
			World.sendProjectile(user, this, 2263, 11, 11, 20, 5, 0, 0);
		}
		user.heal(hit.getDamage() / 5);
		prayer.drainPrayer(hit.getDamage() / 5);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				setNextGraphics(new Graphics(2264));
				if (hit.getDamage() > 0) {
					World.sendProjectile(target, user, 2263, 11, 11, 20, 5, 0, 0);
				}
			}
		}, 0);
	}

	private void processSwitches() {
		List<Integer> pendingSwitches = getAttribute("pending_switches");
		if (pendingSwitches == null || pendingSwitches.isEmpty()) {
			return;
		}
		int[] slotIds = new int[pendingSwitches.size()];
		for (int i = 0; i < pendingSwitches.size(); i++) {
			slotIds[i] = pendingSwitches.get(i);
		}
		ButtonHandler.sendWear(this, slotIds);
		pendingSwitches.clear();
		removeAttribute("switching_happening");
		if (getAttribute("special_attack_toggled", false)) {
			PlayerCombat.checkSpecialToggle(this);
		}
	}

	public boolean hasSkull() {
		return skullDelay > 0;
	}

	public Prayer getPrayer() {
		return prayer;
	}

	public boolean clientHasLoadedMapRegion() {
		return clientLoadedMapRegion;
	}

	public void refreshSpawnedObjects() {
		for (int regionId : getMapRegionsIds()) {
			List<WorldObject> removedObjects = World.getRegion(regionId).getRemovedObjects();
			for (WorldObject object : removedObjects) { getPackets().sendDestroyObject(object); }
			List<WorldObject> spawnedObjects = World.getRegion(regionId).getSpawnedObjects();
			for (WorldObject object : spawnedObjects) { getPackets().sendSpawnedObject(object); }
		}
	}

	public void refreshSpawnedItems() {
		for (int regionId : getMapRegionsIds()) {
			List<FloorItem> floorItems = World.getRegion(regionId).getFloorItems();
			if (floorItems == null) {
				continue;
			}
			for (FloorItem item : floorItems) {
				if ((item.isInvisible() || item.isPublicItem()) && (item.getOwner() != null && !item.getOwner().equals(this)) || item.getTile().getPlane() != getPlane()) {
					continue;
				}
				getPackets().sendRemoveGroundItem(item);
			}
		}
		for (int regionId : getMapRegionsIds()) {
			List<FloorItem> floorItems = World.getRegion(regionId).getFloorItems();
			if (floorItems == null) {
				continue;
			}
			for (FloorItem item : floorItems) {
				if ((item.isInvisible() || item.isPublicItem()) && (item.getOwner() != null && !item.getOwner().equals(this)) || item.getTile().getPlane() != getPlane()) {
					continue;
				}
				getPackets().sendGroundItem(item);
			}
		}
	}

	public void setClientHasLoadedMapRegion() {
		clientLoadedMapRegion = true;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Player) {
			if (((Player) o).username.equals(username)) {
				return true;
			}
		}
		return false;
	}

	public void refreshHitPoints() {
		getPackets().sendConfigByFile(7198, getHitpoints());
	}

	public WorldPacketsEncoder getPackets() {
		return session.getWorldPackets();
	}

	@Override
	public String toString() {
		return "username=" + username + ", primaryRight=" + getPrimaryRight();
	}

	public Right getPrimaryRight() {
		return primaryRight;
	}

	// creates Player and saved classes
	public Player() {
		super(GameConstants.START_PLAYER_LOCATION);
	}

	public int getEnergyValue() {
		return (int) runEnergy;
	}

	public Player constructPlayer() {
		setLocation(GameConstants.START_PLAYER_LOCATION);
		setHitpoints(GameConstants.START_PLAYER_HITPOINTS);
		appearence = new Appearence().setAppearance().setDefaultAppearance();
		inventory = new Inventory();
		equipment = new Equipment();
		skills = new Skills();
		combatDefinitions = new CombatDefinitions();
		prayer = new Prayer();
		bank = new Bank();
		controllerManager = new ControllerManager().create();
		musicsManager = new MusicsManager();
		emotesManager = new EmotesManager();
		friendsIgnores = new FriendsIgnores();
		dominionTower = new DominionTower();
		setQuestManager(new QuestManager());
		auraManager = new AuraManager();
		petManager = new PetManager();
		facade = new PlayerFacade();
		house = new House();
		setDegradeManager(new DegradeManager());
		runEnergy = 100;
		allowChatEffects = true;
		mouseButtons = true;
		pouches = new int[4];
		resetBarrows();
		SkillCapeCustomizer.resetSkillCapes(this);
		ownedObjectsManagerKeys = new LinkedList<>();
		ipList = new ArrayList<>();
		poison = new Poison();
		brawlingGlovesManager = new BrawlingGlovesManager();
		pinManager = new BankPinManager();
		killTimeManager = new BossKillTimeManager();
		presetManager = new PresetManager();
		securityDetails = new SecurityDetails();
		loyaltyManager = new LoyaltyManager();
		costumeManager = new CostumeManager();
		setFriendChatSetup(12);
		return this;
	}

	public void resetBarrows() {
		hiddenBrother = -1;
		killedBarrowBrothers = new boolean[7]; // includes new bro for future
		// use
		barrowsKillCount = 0;
	}

	public void setFriendChatSetup(int friendChatSetup) {
		this.friendChatSetup = friendChatSetup;
	}

	public void init(Session session, String username, String password, String macAddress, int displayMode, int screenWidth, int screenHeight, IsaacKeyPair isaacKeyPair) {
		this.session = session;
		this.username = username;
		this.password = password;
		this.macAddress = macAddress;
		this.displayMode = displayMode;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.isaacKeyPair = isaacKeyPair;
		this.signInTime = System.currentTimeMillis();
		if (farmingManager == null) {
			farmingManager = new FarmingManager();
		}
		this.processData = new ConcurrentHashMap<>();
		this.rights = new ArrayList<>();
		this.lockManagement = new LockManagement();
		this.randomEventManager = new RandomEventManager();
		notes = new Notes(this);
		interfaceManager = new InterfaceManager(this);
		dialogueManager = new DialogueManager(this);
		hintIconsManager = new HintIconsManager(this);
		priceCheckManager = new PriceCheckManager(this);
		localPlayerUpdate = new LocalPlayerUpdate(this);
		localNPCUpdate = new LocalNPCUpdate(this);
		actionManager = new ActionManager(this);
		cutscenesManager = new CutscenesManager(this);
		setClientVarpsManager(new ClientVarpsManager(this));
		trade = new Trade(this);
		if (warriorPoints == null) {
			warriorPoints = new double[6];
		}
		if (lostUntradeables == null) {
			lostUntradeables = new ArrayList<>();
		}
		appearence.setPlayer(this);
		inventory.setPlayer(this);
		equipment.setPlayer(this);
		skills.setPlayer(this);
		randomEventManager.initializeVars(this);
		combatDefinitions.setPlayer(this);
		house.setPlayer(this);
		prayer.setPlayer(this);
		bank.setPlayer(this);
		controllerManager.setPlayer(this);
		brawlingGlovesManager.setPlayer(this);
		musicsManager.setPlayer(this);
		emotesManager.setPlayer(this);
		friendsIgnores.setPlayer(this);
		dominionTower.setPlayer(this);
		auraManager.setPlayer(this);
		pinManager.setPlayer(this);
		petManager.setPlayer(this);
		getDegradeManager().setPlayer(this);
		getQuestManager().setPlayer(this);
		presetManager.setPlayer(this);
		killTimeManager.setPlayer(this);
		farmingManager.setPlayer(this);
		setDirection(Utils.getFaceDirection(0, -1));
		temporaryMovementType = -1;
		logicPackets = new ConcurrentLinkedQueue<>();
		switchItemCache = Collections.synchronizedList(new ArrayList<>());
		initEntity();
		World.addPlayer(this);
		World.updateEntityRegion(this);
		updateIPList();
		initializeRights();
		System.out.println("Initiated player: " + username + ". Total players: " + World.getPlayers().size() + ".");
	}

	/**
	 * @param clientVarpsManager
	 * 		the clientVarpsManager to set
	 */
	public void setClientVarpsManager(ClientVarpsManager clientVarpsManager) {
		this.clientVarpsManager = clientVarpsManager;
	}

	/**
	 * @return the degradeManager
	 */
	public DegradeManager getDegradeManager() {
		return degradeManager;
	}

	/**
	 * @return the questManager
	 */
	public QuestManager getQuestManager() {
		return questManager;
	}

	public void updateIPList() {
		if (getIPList().size() > 50) {
			getIPList().clear();
		}
		if (!getIPList().contains(getSession().getIP())) {
			getIPList().add(getSession().getIP());
			CoresManager.LOG_PROCESSOR.appendLog(new GameLog("iplist", getUsername(), "Signed in from an unusual ip address: " + getSession().getIP()));
		}
	}

	/**
	 * This method initializes the player's rights
	 */
	public void initializeRights() {
		if (GameConstants.SQL_ENABLED && !getMacAddress().equals("BOT MAC ADDRESS")) {
			DatabaseFunctions.setRightsFromForums(this);
			addRight(RightManager.PLAYER);
		} else {
			addRight(GameConstants.DEBUG ? RightManager.OWNER : RightManager.PLAYER);
		}
		setPrimaryRight();
	}
	
	public ArrayList<String> getIPList() {
		return ipList;
	}

	public Session getSession() {
		return session;
	}

	public String getUsername() {
		return username;
	}

	/*
	 * do not use this, only used by pm
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the macAddress
	 */
	public String getMacAddress() {
		return macAddress;
	}

	public void addRight(Right right) {
		if (!rights.contains(right)) {
			rights.add(right);
		}
	}

	public void setPrimaryRight() {
		int lowest = -1;
		Right best = null;
//		System.out.println(rights);
		for (Right right : rights) {
			Optional<Right> optional = RightManager.getRight(right.getName());
			if (optional.isPresent()) {
				right.setIndex(optional.get().getIndex());
			}
		}
		for (Right right : rights) {
			if (lowest == -1 || right.getIndex() < lowest) {
				lowest = right.getIndex();
				best = right;
			}
		}
		if (best == null) {
			System.err.println("No best group with lowest index.");
		}
		this.primaryRight = best;
	}

	/**
	 * @param macAddress
	 * 		the macAddress to set
	 */
	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	/**
	 * @param questManager
	 * 		the questManager to set
	 */
	public void setQuestManager(QuestManager questManager) {
		this.questManager = questManager;
	}

	/**
	 * @param degradeManager
	 * 		the degradeManager to set
	 */
	public void setDegradeManager(DegradeManager degradeManager) {
		this.degradeManager = degradeManager;
	}

	@SuppressWarnings("unchecked")
	public <T> T getForumTable(String table) {
		DatabaseConnection connection = World.getConnectionPool().nextFree();
		try {
			Statement stmt = connection.createStatement();
			String username = getUsername();
			ResultSet rs = stmt.executeQuery(String.format("SELECT username, email, user_group_id, secondary_group_ids FROM `xf_user` WHERE username='%s' LIMIT 1", Utils.formatPlayerNameForDisplay(username)));
			if (rs.next()) {
				return (T) rs.getObject(table);
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				connection.returnConnection();
			}
		}
		return null;
	}

	/**
	 * This method sets your primary right to the parameterized one IF POSSIBLE. If possible meaning if the current
	 * primary usergroup is a group that can't be overriden, the new right is set to your secondary group.
	 *
	 * @param right
	 * 		The new right to set
	 */
	public void addForumUsergroup(Right right) {
		try {
			DatabaseFunctions.addForumGroup(this, right);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Adds a donator usergrou to the players rights
	 *
	 * @param right
	 * 		The right
	 */
	public void addDonatorUsergroup(Right right) {
		try {
			DatabaseFunctions.addDonatorGroup(this, right);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the non-donator forum groups
	 */
	public List<Integer> getNonDonatorGroups() {
		List<Integer> list = new ArrayList<>();
		for (Right right : rights) {
			if (right.isDonator()) {
				continue;
			}
			list.add(right.getForumGroupId());
		}
		if (!primaryRight.isDonator()) {
			list.add(primaryRight.getForumGroupId());
		}
		return list;
	}

	/**
	 * Checks if the player is eligible to be a donator still, and if they aren't they are removed
	 */
	private void checkDonatorEligibility() {
		if (shouldRemoveDonatorRanks()) {
			removeAllNonDonatorGroups();
			sendMessage("Your donator membership has expired.");
		}
	}

	/**
	 * Removes all non-donator groups
	 */
	public void removeAllNonDonatorGroups() {
		try {
			DatabaseFunctions.setForumGroups(this, getNonDonatorGroups());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks if we should remove donator ranks by seeing if the time has elapsed for donators' ranks, and as well as if
	 * they have donator ranks
	 */
	public boolean shouldRemoveDonatorRanks() {
		return isAnyDonator() && !facade.hasDonatorTimeRemaining();
	}

	/**
	 * Adds the membership group
	 *
	 * @param membershipGroup
	 * 		The membership group
	 */
	public void addToMembershipGroup(Right membershipGroup) {
		try {
			facade.setDonatorExpirationTime(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30));
			// no change
			if (hasPrivilegesOf(membershipGroup)) {
				return;
			}
			addDonatorUsergroup(membershipGroup);
			System.out.println(getUsername() + " upgrading to " + membershipGroup + "!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * When called, this method will check the player's main forum group. If the main forum group is a prioritized one
	 * (ordinal <= 5), the secondary group will then include the membership group. If not, the main member group will
	 * now be the membership group. <p/> This method updates the SQL database only - not the player's list
	 */
	public boolean updateMembershipRights() {
		try {
			Right membershipGroup = getMembershipGroup();
			// hasnt donated enough to deserve a rank
			if (membershipGroup == null) {
				return false;
			}
			facade.setDonatorExpirationTime(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30));
			// no change
			if (hasPrivilegesOf(membershipGroup)) {
				return false;
			}
			addDonatorUsergroup(membershipGroup);
			System.out.println(getUsername() + " upgrading to " + membershipGroup + "!");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Finds the membership group that is applicable to your user. This is based on total gold points purchased. If the
	 * user has purchased a lot of gold points, they will have access to a higher ranked membership group.
	 *
	 * @return A {@code Rights} {@code Object}
	 */
	public Right getMembershipGroup() {
		long purchasedTotal = facade.getTotalPointsPurchased() / 100;
		if (purchasedTotal >= 5 && purchasedTotal <= 50) {
			return RightManager.DONATOR;
		} else if (purchasedTotal > 50 && purchasedTotal <= 100) {
			return RightManager.SUPREME_DONATOR;
		} else if (purchasedTotal > 100 && purchasedTotal <= 250) {
			return RightManager.EXTREME_DONATOR;
		} else if (purchasedTotal > 250 && purchasedTotal <= 500) {
			return RightManager.LEGENDARY_DONATOR;
		} else if (purchasedTotal > 500) {
			return RightManager.ELITE_DONATOR;
		}
		return null;
	}

	public double[] getWarriorPoints() {
		return warriorPoints;
	}

	public void setWarriorPoints(int index, double pointsDifference) {
		warriorPoints[index] += pointsDifference;
		if (warriorPoints[index] < 0) {
			Controller controler = getControllerManager().getController();
			if (controler == null || !(controler instanceof WarriorsGuild)) {
				return;
			}
			WarriorsGuild guild = (WarriorsGuild) controler;
			guild.setInCyclopse(false);
			setNextWorldTile(WarriorsGuild.CYCLOPS_LOBBY);
			warriorPoints[index] = 0;
		} else if (warriorPoints[index] > 65535) {
			warriorPoints[index] = 65535;
		}
		refreshWarriorPoints(index);
	}

	public ControllerManager getControllerManager() {
		return controllerManager;
	}

	public void refreshWarriorPoints(int index) {
		getVarsManager().sendVarBit(index + 8662, (int) warriorPoints[index]);
	}

	/**
	 * @return the clientVarpsManager
	 */
	public ClientVarpsManager getVarsManager() {
		return clientVarpsManager;
	}

	public int getSkullDelay() {
		return skullDelay;
	}

	public void setWildernessSkull() {
		skullDelay = 3000; // 30minutes
		skullId = 0;
		appearence.generateAppearenceData();
	}

	public void setFightPitsSkull() {
		skullDelay = Integer.MAX_VALUE; // infinite
		skullId = 1;
		appearence.generateAppearenceData();
	}

	public void setSkullInfiniteDelay(int skullId) {
		skullDelay = Integer.MAX_VALUE; // infinite
		this.skullId = skullId;
		appearence.generateAppearenceData();
	}

	public void removeSkull() {
		skullDelay = -1;
		appearence.generateAppearenceData();
	}

	public int setSkullDelay(int delay) {
		return this.skullDelay = delay;
	}

	// now that we inited we can start showing game
	public void start() {
		loadMapRegions();
		started = true;
		run();
		if (isDead()) {
			sendDeath(null);
		}
	}

	public void stopAllButCombat() {
		routeEvent = null;
		closeInterfaces();
		resetWalkSteps();
		if (actionManager.getAction() != null && actionManager.getAction() instanceof PlayerCombat) {
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					combatDefinitions.resetSpells(false);
					actionManager.forceStop();
				}
			});
		} else {
			actionManager.forceStop();
			combatDefinitions.resetSpells(false);
		}
		setNextFaceEntity(null);
	}

	public void closeInterfaces() {
		if (interfaceManager.containsScreenInterface()) {
			interfaceManager.closeScreenInterface();
		}
		if (interfaceManager.containsInventoryInter()) {
			interfaceManager.closeInventoryInterface();
		}
		dialogueManager.finishDialogue();
		if (closeInterfacesEvent != null) {
			closeInterfacesEvent.run();
			closeInterfacesEvent = null;
		}
	}

	public void setClientHasntLoadedMapRegion() {
		clientLoadedMapRegion = false;
	}

	public void toogleRun(boolean update) {
		super.setRun(!getRun());
		updateMovementType = true;
		if (update) {
			sendRunButtonConfig();
		}
	}

	public void setRunHidden(boolean run) {
		super.setRun(run);
		updateMovementType = true;
	}

	public void restoreRunEnergy() {
		if (getNextRunDirection() == -1 && runEnergy < 100) {
			runEnergy++;
			if (resting && runEnergy < 100) {
				runEnergy++;
			}
			getPackets().sendRunEnergy();
		}
	}

	/**
	 * Checks if the player has the item on them
	 *
	 * @param itemId
	 * 		The item id
	 */
	public boolean hasItem(int itemId) {
		for (Item _item : inventory.getItems().toArray()) {
			if (_item == null) { continue; }
			if (_item.getId() == itemId) {
				return true;
			}
		}
		for (Item _item : equipment.getItems().toArray()) {
			if (_item == null) { continue; }
			if (_item.getId() == itemId) {
				return true;
			}
		}
		for (Item _item : bank.generateContainer()) {
			if (_item == null) { continue; }
			if (_item.getId() == itemId) {
				return true;
			}
		}
		return false;
	}

	public void run() {
		if (World.exiting_start != 0) {
			int delayPassed = (int) ((Utils.currentTimeMillis() - World.exiting_start) / 1000);
			getPackets().sendSystemUpdate(World.exiting_delay - delayPassed);
		}
		lastIP = getSession().getIP();
		interfaceManager.sendInterfaces();
		sendMessage("Welcome to " + GameConstants.SERVER_NAME + ".");
//		sendMessage("Use ::commands to find out which commands you can use.");
		if (Lottery.getWins(this).size() > 0) {
			sendMessage("<col=" + ChatColors.BLUE + ">You have unclaimed lottery wins, please speak to the Gambler to receive your lotto cash.");
		}
		ShootingStarTick shootingStarTick = WorldTasksManager.getTask(ShootingStarTick.class);
		if (shootingStarTick != null && shootingStarTick.getSpawnLocation() != null) {
			sendMessage("There is currently a shooting star at: " + shootingStarTick.getSpawnLocation() + ".");
		}
		if (!getPinManager().hasPin() || !securityDetails.hasSecurityEnabled()) {
			sendMessage(new String[] { "<col=" + ChatColors.RED + ">Your account is vulernable to hacking - please set a bank pin & security question." }, 6);
		}
		if (DXPAlgorithms.isDoubleExperienceOn()) {
			sendMessage("<col=" + ChatColors.RED + ">Bonus experience is currently active, don't experience waste!");
			skills.sendBonusConfigs();
		}
//		checkEmail();
		sendDefaultPlayersOptions();
		checkMultiArea();
		inventory.init();
		equipment.init();
		skills.init();
		combatDefinitions.init();
		prayer.init();
		friendsIgnores.init();
		farmingManager.init();
		getHouse().init();
		refreshHitPoints();
		prayer.refreshPrayerPoints();
		getPoison().refresh();
		getPackets().sendConfig(281, 1000); // unlock can't do this on tutorial
		getPackets().sendConfig(1160, -1); // unlock summoning orb
		getPackets().sendConfig(1159, 1);
		getPackets().sendGameBarStages();
		musicsManager.init();
		emotesManager.refreshListConfigs();
		sendUnlockedObjectConfigs();
		if (currentFriendChatOwner != null) {
			FriendChatsManager.joinChat(currentFriendChatOwner, this);
			if (currentFriendChat == null) {
				currentFriendChatOwner = null;
			}
		}
		if (getClanName() != null) {
			if (!ClansManager.connectToClan(this, getClanName(), false)) {
				setClanName(null);
			}
		}
		if (familiarSerialization != null) {
			loadFamiliar(true);
		} else {
			petManager.init();
		}
		updateMovementType = true;
		appearence.generateAppearenceData();
		controllerManager.login();
		getPackets().sendRunEnergy();
		refreshAllowChatEffects();
		refreshMouseButtons();
		refreshPrivateChatSetup();
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				refreshOtherChatsSetup();
			}
		}, 1);
		sendRunButtonConfig();
		OwnedObjectManager.linkKeys(this);
		Notes.unlock(this);
		GlobalPlayers.refreshOnlineToday();
		QuestTabInteractionEvent.displayAchievements(this, AchievementType.EASY);
		GsonStartup.getOptional(ExchangeItemLoader.class).ifPresent(loader -> loader.sendLogin(this));
		for (int i = 0; i < 2; i++) {
			toogleRun(true); // temp fix
		}
		if (isAnyIronman() && !AchievementHandler.isFinished(this, Grand_Exchanger.class)) {
			AchievementHandler.forceFinish(this, Grand_Exchanger.class);
			QuestTabInteractionEvent.refresh(this);
		}
		if (isAnyDonator() && facade.hasDonatorTimeRemaining()) {
			sendMessage("Your donator rank will expire on " + new Date(facade.getDonatorExpirationTime()) + ".");
		}
		running = true;
	}

	private void checkEmail() {
		if (GameConstants.SQL_ENABLED) {
			if (!facade.isEmailRegistered()) {
				CoresManager.DATABASE_WORKER.submit(() -> {
					try {
						String email = getEmailFromForum();
						if (email.equalsIgnoreCase(Utils.formatPlayerNameForProtocol(username + "_register@lotica.org"))) {
							dialogueManager.startDialogue(SimpleNPCMessage.class, 945, "You have not registered your email yet.", "Please visit ::email to do this.");
							sendMessage("<col=" + ChatColors.RED + ">" + "You have not registered your email, please visit ::email to do this.");
						} else {
							facade.setEmailRegistered(true);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			}
		}
	}

	private String getEmailFromForum() {
		String url = "http://167.114.0.218/lotica/forums/lotica.php?getEmail&username=" + Utils.formatPlayerNameForURL(username) + "&password=" + password;
		WebPage page = new WebPage(url);
		try {
			page.load(false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String result = "";
		List<String> results = page.getLines();
		for (String resultLine : results) {
			result += resultLine + "\n";
		}
		result = result.trim();
		return result;
	}

	public void loadFamiliar(boolean sendPackets) {
		Familiar newFamiliar = Summoning.createFamiliar(this, familiarSerialization.getPouch());
		if (newFamiliar != null) {
			BeastOfBurden bob = familiarSerialization.getBob();
			if (bob != null) {
				newFamiliar.setBob(bob);
			}
			newFamiliar.setTicks(familiarSerialization.getTicks());
			newFamiliar.setSpecialEnergy(familiarSerialization.getSpecialEnergy());
			newFamiliar.setTrackDrain(familiarSerialization.isTrackDrain());
			newFamiliar.setTrackTimer(familiarSerialization.getTrackTimer());
			setFamiliar(newFamiliar);
			if (sendPackets) {
				familiar.respawnFamiliar(this);
			}
		} else {
			System.err.println("Familiar was null: " + familiar.getPouch());
		}
	}

	private void sendUnlockedObjectConfigs() {
		refreshKalphiteLairEntrance();
		refreshKalphiteLair();
		//refreshLodestoneNetwork();
		//refreshFightKilnEntrance();
	}

	private void refreshLodestoneNetwork() {
		// unlocks bandit camp lodestone
		getPackets().sendConfigByFile(358, 15);
		// unlocks lunar isle lodestone
		getPackets().sendConfigByFile(2448, 190);
		// unlocks alkarid lodestone
		getPackets().sendConfigByFile(10900, 1);
		// unlocks ardougne lodestone
		getPackets().sendConfigByFile(10901, 1);
		// unlocks burthorpe lodestone
		getPackets().sendConfigByFile(10902, 1);
		// unlocks catherbay lodestone
		getPackets().sendConfigByFile(10903, 1);
		// unlocks draynor lodestone
		getPackets().sendConfigByFile(10904, 1);
		// unlocks edgeville lodestone
		getPackets().sendConfigByFile(10905, 1);
		// unlocks falador lodestone
		getPackets().sendConfigByFile(10906, 1);
		// unlocks lumbridge lodestone
		getPackets().sendConfigByFile(10907, 1);
		// unlocks port sarim lodestone
		getPackets().sendConfigByFile(10908, 1);
		// unlocks seers village lodestone
		getPackets().sendConfigByFile(10909, 1);
		// unlocks taverley lodestone
		getPackets().sendConfigByFile(10910, 1);
		// unlocks varrock lodestone
		getPackets().sendConfigByFile(10911, 1);
		// unlocks yanille lodestone
		getPackets().sendConfigByFile(10912, 1);
	}

	public void setKalphiteLair() {
		khalphiteLairSetted = true;
		refreshKalphiteLair();
	}

	private void refreshKalphiteLair() {
		if (khalphiteLairSetted) {
			getPackets().sendConfigByFile(7263, 1);
		}
	}

	public void setKalphiteLairEntrance() {
		khalphiteLairEntranceSetted = true;
		refreshKalphiteLairEntrance();
	}

	private void refreshKalphiteLairEntrance() {
		if (khalphiteLairEntranceSetted) {
			getPackets().sendConfigByFile(7262, 1);
		}
	}

	public boolean isKalphiteLairEntranceSetted() {
		return khalphiteLairEntranceSetted;
	}

	public boolean isKalphiteLairSetted() {
		return khalphiteLairSetted;
	}

	public void sendDefaultPlayersOptions() {
		getPackets().sendPlayerOption("Follow", 2, false);
		getPackets().sendPlayerOption("Trade with", 4, false);
	}

	/**
	 * Logs the player out.
	 */
	public void logout() {
		if (!running) {
			return;
		}
		long currentTime = Utils.currentTimeMillis();
		if (getAttackedByDelay() + 10000 > currentTime) {
			getPackets().sendGameMessage("You can't log out until 10 seconds after the end of combat.");
			return;
		}
		if (getEmotesManager().getNextEmoteEnd() >= currentTime) {
			getPackets().sendGameMessage("You can't log out while performing an emote.");
			return;
		}
		if (lockManagement.isLocked(LockType.EMOTES)) {
			getPackets().sendGameMessage("You can't log out while performing an action.");
			return;
		}
		getPackets().sendLogout();
		running = false;
	}

	public EmotesManager getEmotesManager() {
		return emotesManager;
	}

	public void forceLogout() {
		getPackets().sendLogout();
		realFinish();
		running = false;
	}

	public void realFinish() {
		if (hasFinished()) {
			return;
		}
		stopAll();
		getHouse().finish();
		cutscenesManager.logout();
		controllerManager.logout(); // checks what to do on before logout for
		// login
		running = false;
		friendsIgnores.sendFriendsMyStatus(false);
		if (currentFriendChat != null) {
			currentFriendChat.leaveChat(this, true);
		}
		boolean savedFamiliar = false;
		if (familiar != null && !familiar.isFinished()) {
			familiar.dissmissFamiliar(true);
			familiarSerialization = new FamiliarSerialization(familiar.getPouch(), familiar.getId(), familiar.getSpecialEnergy(), familiar.getTicks(), familiar.getTrackTimer(), familiar.getBob(), familiar.isTrackDrain());
			savedFamiliar = true;
		} else if (pet != null) {
			pet.finish();
		}
		if (!savedFamiliar) {
			familiarSerialization = null;
		}
		if (clanManager != null) {
			clanManager.disconnect(this, false);
		}
		if (guestClanManager != null) {
			guestClanManager.disconnect(this, true);
		}
		if (getDwarfCannon() != null) {
			getDwarfCannon().finish(true);
		}
		facade.addTimeOnline(signInTime);
		SlayerManagement.removeSocialSlayer(this);
		LentItemsLoader.handlePlayerLogout(this);
		setFinished(true);
		session.setDecoder(-1);
		DatabaseFunctions.saveHighscores(this);
		PlayerSaving.savePlayer(this);
		World.updateEntityRegion(this);
		World.removePlayer(this);
		System.out.println("Finished Player: " + username + ", left online: " + World.getPlayers().size());
	}

	public void stopAll() {
		stopAll(true);
	}

	/**
	 * @return the house
	 */
	public House getHouse() {
		return house;
	}

	/**
	 * @param house
	 * 		the house to set
	 */
	public void setHouse(House house) {
		this.house = house;
	}

	/**
	 * @return the dwarfCannon
	 */
	public DwarfCannon getDwarfCannon() {
		return dwarfCannon;
	}

	public void stopAll(boolean stopWalk) {
		stopAll(stopWalk, true);
	}

	public void stopAll(boolean stopWalk, boolean stopInterface) {
		stopAll(stopWalk, stopInterface, true, true);
	}

	// as walk done clientsided
	public void stopAll(boolean stopWalk, boolean stopInterfaces, boolean stopActions, boolean resetSpells) {
		routeEvent = null;
		if (stopInterfaces) {
			closeInterfaces();
		}
		if (stopWalk) {
			resetWalkSteps();
		}
		if (stopActions) {
			actionManager.forceStop();
		}
		if (resetSpells) { combatDefinitions.resetSpells(false); }
		removeAttribute("input_event");
		setNextFaceEntity(null);
	}

	/**
	 * @param dwarfCannon
	 * 		the dwarfCannon to set
	 */
	public void setDwarfCannon(DwarfCannon dwarfCannon) {
		this.dwarfCannon = dwarfCannon;
	}

	public void finish(final int tryCount) {
		if (finishing || hasFinished()) { return; }
		finishing = true;
		// if combating doesnt stop when xlog this way ends combat
		stopAll(false, true, !(actionManager.getAction() instanceof PlayerCombat), false);
		if (isDead() || (isUnderCombat() && tryCount < 6) || (lockManagement.isAnyLocked() && tryCount < 6) || (getEmotesManager().isDoingEmote() && tryCount < 6)) {
			String message = "Couldnt log out, details:\tisDead()=" + isDead() + " isUnderCombat()=" + isUnderCombat() + ", lockDebug=" + lockManagement.debugLogInformation() + " doingEmote=" + emotesManager.isDoingEmote() + ", tryCount=" + tryCount;
			CoresManager.LOG_PROCESSOR.appendLog(new GameLog("logout_timer", username, message));
			System.out.println(getDisplayName() + "\t:" + message);
			CoresManager.schedule(() -> {
				try {
					finishing = false;
					finish(tryCount + 1);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}, 10, TimeUnit.SECONDS);
			return;
		}
		realFinish();
	}

	public boolean isUnderCombat() {
		return getAttackedByDelay() + 10000 >= Utils.currentTimeMillis();
	}

	public int getMessageIcon() {
		return getPrimaryRight().getMessageIcon();
	}

	public boolean isRunning() {
		return running;
	}

	public boolean hasDisplayName() {
		return displayName != null;
	}

	public LocalNPCUpdate getLocalNPCUpdate() {
		return localNPCUpdate;
	}

	public int getDisplayMode() {
		return displayMode;
	}

	public void setDisplayMode(int displayMode) {
		this.displayMode = displayMode;
	}

	public InterfaceManager getInterfaceManager() {
		return interfaceManager;
	}

	public int getScreenWidth() {
		return screenWidth;
	}

	public void setScreenWidth(int screenWidth) {
		this.screenWidth = screenWidth;
	}

	public int getScreenHeight() {
		return screenHeight;
	}

	public void setScreenHeight(int screenHeight) {
		this.screenHeight = screenHeight;
	}

	public void drainRunEnergy() {
		if (getNextRunDirection() != -1) {
			double toLose = (0.67 + (getWeight() / 100)) / 2;
			setRunEnergy(runEnergy - toLose);
		}
	}

	public double getWeight() {
		return inventory.getInventoryWeight() + equipment.getEquipmentWeight();
	}

	public boolean isAnyDonator() {
		return hasPrivilegesOf(RightManager.DONATOR, RightManager.SUPREME_DONATOR, RightManager.EXTREME_DONATOR, RightManager.LEGENDARY_DONATOR, RightManager.ELITE_DONATOR);
	}

	public boolean isResting() {
		return resting;
	}

	public void setResting(boolean resting) {
		this.resting = resting;
		sendRunButtonConfig();
	}

	public DialogueManager getDialogueManager() {
		return dialogueManager;
	}

	/**
	 * This method finds all the items that the player contains on them.
	 */
	public CopyOnWriteArrayList<Item> findContainedItems() {
		CopyOnWriteArrayList<Item> containedItems = new CopyOnWriteArrayList<>();
		for (int i = 0; i < 14; i++) {
			if (equipment.getItem(i) != null && equipment.getItem(i).getId() != -1 && equipment.getItem(i).getAmount() != -1) {
				containedItems.add(new Item(equipment.getItem(i).getId(), equipment.getItem(i).getAmount()));
			}
		}
		for (int i = 0; i < 28; i++) {
			if (inventory.getItem(i) != null && inventory.getItem(i).getId() != -1 && inventory.getItem(i).getAmount() != -1) {
				containedItems.add(new Item(getInventory().getItem(i).getId(), getInventory().getItem(i).getAmount()));
			}
		}
		return containedItems;
	}

	public void sendItemsOnDeath(Player killer) {
		auraManager.removeAura();
		CopyOnWriteArrayList<Item> containedItems = findContainedItems();
		if (containedItems.isEmpty()) {
			return;
		}
		if (!World.containsPlayer(getUsername())) {
			System.out.println(getUsername() + " was offline when we were supposed to send their items on death.");
			return;
		}
		Map<Integer, List<Item>> deathList = getItemsOnDeath(containedItems);

		List<Item> itemsDropped = deathList.get(0);
		List<Item> itemsKept = deathList.get(1);
		List<Item> untradeables = deathList.get(2);
/*
		System.out.println("Contained Items:\t" + containedItems);
		System.out.println("Items dropped:\t" + itemsDropped);
		System.out.println("Items kept:\t" + itemsKept);
		System.out.println("Untradebles:\t" + untradeables);*/

		// deleting untradeable items from the containers if they are deleted on drop
		boolean deletedUntradeable = false;
		for (Iterator<Item> it$ = untradeables.iterator(); it$.hasNext(); ) {
			Item untradeableItem = it$.next();
			if (ItemConstants.destroysOnDrop(untradeableItem)) {
				deletedUntradeable = true;
				it$.remove();
			}
		}

		if (deletedUntradeable) {
			sendMessage("You feel an item be removed from your bag and sent to its original place...", false);
		}

		inventory.reset();
		equipment.reset();
		itemsKept.stream().filter(item -> item.getId() != 1).forEach(item -> getInventory().addItem(item));

		// if the killer is an ironman, the drop is handled differently.
		WorldTile lootTile = getLastWorldTile();

		if (getControllerManager().getController() instanceof QueenBlackDragonController) {
			sendMessage("Since you died in the queen black dragon lair, your lost items have been dropped at home.");
			lootTile = GameConstants.START_PLAYER_LOCATION;
		}

		// making the untradeable items be dropped on deaths
		for (Iterator<Item> iterator = untradeables.iterator(); iterator.hasNext(); ) {
			Item item = iterator.next();
			if (ItemConstants.untradeableDropsOnDeath(item)) {
				iterator.remove();
				itemsDropped.add(item);
			}
		}

		if (killer != null && killer.isAnyIronman()) {
			// we an ironman and we died from an npc, so we can fetch our items
			if (isAnyIronman() && killer.equals(this)) {
				for (Item item : itemsDropped) {
					World.addGroundItem(item, lootTile, killer, true, 180);
				}
			} else {
				// if the killer was an ironman, the items are public
				// and the ironman can't loot them.
				for (Item item : itemsDropped) {
					World.addGroundItem(item, lootTile, null, false, 180, 2, 150);
				}
			}
		} else {
			// if the killer is not an ironman, the drop is regular and loots
			// are public for all after some time
			for (Item item : itemsDropped) {
				World.addGroundItem(item, lootTile, killer == null ? this : killer, true, 180);
			}
		}
		World.addGroundItem(new Item(526), lootTile, killer, true, 180, 2, 150);
		if (isUltimateIronman()) {
			handleUltimateIronmanDeath();
			return;
		}
		// untradeables are handled separately
		// if players are donators, they keep untradeables in their
		// inventory on death
		if (isAnyDonator()) {
			for (Item item : untradeables) {
				getInventory().addItem(item);
			}
		} else {
			// regular players buy back lost untradeables
			lostUntradeables.addAll(untradeables.stream().filter(item -> !item.getDefinitions().isLended()).collect(Collectors.toList()));
			untradeables.stream().filter(item -> item.getDefinitions().isLended()).forEach(inventory::addItem);
			if (!untradeables.isEmpty()) {
				sendMessage("The untradeable items you lost can be claimed by speaking to a banker.");
			}
		}
		AchievementHandler.incrementProgress(this, The_Resurrection.class, 1);
	}

	private void handleUltimateIronmanDeath() {
		World.sendWorldMessage("<img=6><shad=000000><col=" + ChatColors.RED + ">" + getDisplayName() + " just died in Ultimate Ironman Mode with a skill total of " + getSkills().getTotalLevel() + ".", false);
		inventory = new Inventory();
		skills = new Skills();
		bank = new Bank();
		inventory.setPlayer(this);
		skills.setPlayer(this);
		bank.setPlayer(this);
		lostUntradeables.clear();
	}

	/**
	 * This method finds out the items that we keep on death, items we drop on death, and the untradeables on death. The
	 * first index in this array is the items dropped, second is items kept, third is the untradeables. No operations
	 * are done to the items in this method nor the containedItems list, they are just put into lists.
	 *
	 * @param containedItems
	 * 		The items the player contains in their inventory and equipment
	 */
	@SuppressWarnings("unchecked")
	public Map<Integer, List<Item>> getItemsOnDeath(List<Item> containedItems) {
		List<Item> itemsDropped = new ArrayList<>(containedItems);
		List<Item> itemsKept = new ArrayList<>();
		List<Item> untradeables = new ArrayList<>();

		// removing untradeable items from the contained items
		for (Iterator<Item> it$ = itemsDropped.iterator(); it$.hasNext(); ) {
			Item deathItem = it$.next();
			if ((!ItemConstants.isTradeable(deathItem) && !ItemConstants.untradeableDropsOnDeath(deathItem)) || deathItem.getDefinitions().isLended()) {
				untradeables.add(deathItem);
				it$.remove();
			}
		}

		int amountToKeep = 0;
		if (!(controllerManager.getController() instanceof CorpBeastControler) && !(controllerManager.getController() instanceof CrucibleControler)) {
			amountToKeep = hasSkull() ? 0 : 3;
			if (prayer.usingPrayer(0, 10) || prayer.usingPrayer(1, 0)) {
				amountToKeep++;
			}
		}

		Collections.sort(itemsDropped, (o1, o2) -> Integer.compare(o2.getDefinitions().getExchangePrice(), o1.getDefinitions().getExchangePrice()));
		int tempAmountKept = 0;
		k:
		for (Iterator<Item> it$ = itemsDropped.iterator(); it$.hasNext(); ) {
			Item item = it$.next();
			for (int i = 0; i <= item.getAmount(); i++) {
				if (tempAmountKept++ == amountToKeep) {
					break k;
				}
				Item saved = new Item(item.getId(), 1);
				itemsKept.add(saved);
				if (item.getAmount() >= 1) {
					item.setAmount(item.getAmount() - 1);
				}
				if (item.getAmount() < 1) {
					it$.remove();
				}
			}
		}

		/*System.out.println("---- FINISHED OPERATIONS ----");
		System.out.println("itemsDropped:\t" + itemsDropped);
		System.out.println("itemsKept:\t" + itemsKept);
		System.out.println("untradeables:\t" + untradeables);
		System.out.println("---- ALL ITEMS ----");*/

		Map<Integer, List<Item>> items = new HashMap<>();
		items.put(0, itemsDropped);
		items.put(1, itemsKept);
		items.put(2, untradeables);
		return items;
	}

	public void increaseKillCount(Player killed) {
		if (isWildernessPointFarming(killed)) {
			sendMessage("You do not receive any wilderness points for this kill.");
			return;
		}

		// the ks for the player who died
		int playerKilledStreak = killed.getFacade().getKillstreak();

		// the potential reward for ending a killstreak
		int pointRewardFromKilledKillstreak = Wilderness.getPointRewardFromKillstreakEnding(playerKilledStreak);
		if (pointRewardFromKilledKillstreak != 0) {
			World.sendWorldMessage("<col=" + ChatColors.MAROON + "><img=6>Wilderness</col>: " + getDisplayName() + " has just ruined " + killed.getDisplayName() + "'s killstreak of " + playerKilledStreak + " for " + pointRewardFromKilledKillstreak + " points.", false);
			inventory.addItem(Wilderness.WILDERNESS_TOKEN, pointRewardFromKilledKillstreak);
		}

		// the player who died loses their killstreak
		killed.getFacade().setKillstreak(0);
		// our killstreak goes up by 1
		getFacade().setKillstreak(getFacade().getKillstreak() + 1);

		// announcing our killstreak
		int pointRewardFromKillstreak = Wilderness.getPointRewardFromKillstreakEnding(getFacade().getKillstreak());
		// if people could get a reward from killing us
		if (pointRewardFromKillstreak != 0) {
			World.sendWorldMessage("<img=6><col=" + ChatColors.RED + ">Wilderness</col>: " + getDisplayName() + "'s killstreak of " + getFacade().getKillstreak() + " can be ended for " + pointRewardFromKillstreak + " points.", false);
		}

		// Increasing the kdr for both players
		killed.deathCount++;
		killCount++;

		getAppearence().generateAppearenceData();
		PkRank.updateRankings(killed);
		AchievementHandler.incrementProgress(this, Ruthless.class, 1);
		PkRank.updateRankings(this);

		int reward = Wilderness.wildernessPointsAfterKillstreakModifier(facade.getKillstreak());
		if (receivesWildernessActivityBonus()) {
			reward = reward * 2;
		}

		inventory.addItem(Wilderness.WILDERNESS_TOKEN, reward);
		sendMessage("<col=" + ChatColors.MAROON + ">You have defeated " + killed.getDisplayName() + " for " + reward + " wilderness points!");
		if (receivesWildernessActivityBonus()) {
			WildernessActivityManager.getSingleton().giveBonusPoints(this);
		}
		getRecentKills().add(new KillInformation(killed.getMacAddress(), System.currentTimeMillis()));
		lastKilled = killed.getMacAddress();
	}

	/**
	 * This method finds out if the player is wilderness point farming so restrictions can be applied. We loop through
	 * the {@link #recentKills} list and check if the player was recently killed. This operation is performed by
	 * checking if there are more than 3 kills for the killed player's mac address.
	 *
	 * @param killed
	 * 		The player that was just killed by us
	 * @return {@code True} if they are farming with the other player
	 */
	public boolean isWildernessPointFarming(Player killed) {
		if (killed.equals(this)) {
			return true;
		}
		// You can't kill the same player twice in a row.
		if (getLastKilled() != null && getLastKilled().equalsIgnoreCase(killed.getMacAddress())) {
			return true;
		}
		List<KillInformation> recentKillList = getRecentKills();
		Map<String, Integer> killMap = new HashMap<>();
		for (Iterator<KillInformation> it$ = recentKillList.iterator(); it$.hasNext(); ) {
			KillInformation kill = it$.next();
			// pruning old kills
			if (TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - kill.getKillTime()) >= 12) {
				it$.remove();
				continue;
			}
			// We only work with the kill if the mac address is the killed player's mac address.
			if (!kill.getMacAddress().equalsIgnoreCase(killed.getMacAddress())) {
				continue;
			}
			// if the kill was less than an hour ago
			if (TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - kill.getKillTime()) < 60) {
				Integer killCount = killMap.get(kill.getMacAddress());
				if (killCount == null) {
					killCount = 1;
				} else {
					killCount = killCount + 1;
				}
				killMap.put(kill.getMacAddress(), killCount);
			}
		}
		for (Entry<String, Integer> entry : killMap.entrySet()) {
			Integer killCount = entry.getValue();
			if (killCount >= 3) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return the facade
	 */
	public PlayerFacade getFacade() {
		return facade;
	}

	public Appearence getAppearence() {
		return appearence;
	}

	/**
	 * If we should receive bonuses for the wilderness activity
	 */
	private boolean receivesWildernessActivityBonus() {
		if (WildernessActivityManager.getSingleton().isActivityCurrent(PvpRegionActivity.class)) {
			PvpRegionActivity activity = WildernessActivityManager.getSingleton().getWildernessActivity(PvpRegionActivity.class);
			if (activity.receivesBonus(this)) {
				return true;
			}
		}
		return false;
	}

	public List<KillInformation> getRecentKills() {
		if (recentKills == null) {
			recentKills = new ArrayList<>();
		}
		return recentKills;
	}

	public String getLastKilled() {
		return lastKilled;
	}

	/**
	 * @param facade
	 * 		the facade to set
	 */
	public void setFacade(PlayerFacade facade) {
		this.facade = facade;
	}

	public boolean isCanPvp() {
		return canPvp;
	}

	public void setCanPvp(boolean canPvp) {
		this.canPvp = canPvp;
		appearence.generateAppearenceData();
		getPackets().sendPlayerOption(canPvp ? "Attack" : "null", 1, true);
		getPackets().sendPlayerUnderNPCPriority(canPvp);
	}

	public void useStairs(int emoteId, final WorldTile dest, int useDelay, int totalDelay) {
		useStairs(emoteId, dest, useDelay, totalDelay, null);
	}

	public void useStairs(int emoteId, final WorldTile dest, int useDelay, int totalDelay, final String message) {
		stopAll();
		getLockManagement().lockAll(totalDelay * 1000);
		if (emoteId != -1) {
			setNextAnimation(new Animation(emoteId));
		}
		if (useDelay == 0) {
			setNextWorldTile(dest);
		} else {
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					if (isDead()) {
						return;
					}
					setNextWorldTile(dest);
					if (message != null) {
						getPackets().sendGameMessage(message);
					}
				}
			}, useDelay - 1);
		}
	}

	public LockManagement getLockManagement() {
		return lockManagement;
	}

	public void switchMouseButtons() {
		mouseButtons = !mouseButtons;
		refreshMouseButtons();
	}

	public void refreshMouseButtons() {
		getPackets().sendConfig(170, mouseButtons ? 0 : 1);
	}

	public void switchAllowChatEffects() {
		allowChatEffects = !allowChatEffects;
		refreshAllowChatEffects();
	}

	public void refreshAllowChatEffects() {
		getPackets().sendConfig(171, allowChatEffects ? 0 : 1);
	}

	public void refreshPrivateChatSetup() {
		getPackets().sendConfig(287, privateChatSetup);
	}

	public void refreshOtherChatsSetup() {
		int value = friendChatSetup << 6;
		getPackets().sendConfig(1438, value);
		getPackets().sendConfigByFile(3612, clanChatSetup);
		getPackets().sendConfigByFile(9191, getGuestChatSetup());
	}

	public int getGuestChatSetup() {
		return guestChatSetup;
	}

	public void setGuestChatSetup(int guestChatSetup) {
		this.guestChatSetup = guestChatSetup;
	}

	public void kickPlayerFromClanChannel(String name) {
		if (clanManager == null) {
			return;
		}
		clanManager.kickPlayerFromChat(this, name);
	}

	public void sendClanChannelMessage(ChatMessage message) {
		if (clanManager == null) {
			return;
		}
		clanManager.sendMessage(this, message);
	}

	public void sendClanChannelQuickMessage(QuickChatMessage message) {
		if (clanManager == null) {
			return;
		}
		clanManager.sendQuickMessage(this, message);
	}

	public void sendGuestClanChannelMessage(ChatMessage message) {
		if (guestClanManager == null) {
			return;
		}
		guestClanManager.sendMessage(this, message);
	}

	public void sendGuestClanChannelQuickMessage(QuickChatMessage message) {
		if (guestClanManager == null) {
			return;
		}
		guestClanManager.sendQuickMessage(this, message);
	}

	public int getClanChatSetup() {
		return clanChatSetup;
	}

	public void setClanChatSetup(int clanChatSetup) {
		this.clanChatSetup = clanChatSetup;
	}

	public int getPrivateChatSetup() {
		return privateChatSetup;
	}

	public void setPrivateChatSetup(int privateChatSetup) {
		this.privateChatSetup = privateChatSetup;
	}

	public boolean isForceNextMapLoadRefresh() {
		return forceNextMapLoadRefresh;
	}

	public void setForceNextMapLoadRefresh(boolean forceNextMapLoadRefresh) {
		this.forceNextMapLoadRefresh = forceNextMapLoadRefresh;
	}

	public void addPoisonImmune(long time) {
		poisonImmune = time + Utils.currentTimeMillis();
		getPoison().reset();
	}

	public long getPoisonImmune() {
		return poisonImmune;
	}

	public void addFireImmune(long time) {
		fireImmune = time + Utils.currentTimeMillis();
	}

	public long getFireImmune() {
		return fireImmune;
	}

	public MusicsManager getMusicsManager() {
		return musicsManager;
	}

	public HintIconsManager getHintIconsManager() {
		return hintIconsManager;
	}

	public int getKillCount() {
		return killCount;
	}

	public int getBarrowsKillCount() {
		return barrowsKillCount;
	}

	public int setBarrowsKillCount(int barrowsKillCount) {
		return this.barrowsKillCount = barrowsKillCount;
	}

	public int setKillCount(int killCount) {
		return this.killCount = killCount;
	}

	public int getDeathCount() {
		return deathCount;
	}

	public int setDeathCount(int deathCount) {
		return this.deathCount = deathCount;
	}

	public boolean[] getKilledBarrowBrothers() {
		return killedBarrowBrothers;
	}

	public int getHiddenBrother() {
		return hiddenBrother;
	}

	public void setHiddenBrother(int hiddenBrother) {
		this.hiddenBrother = hiddenBrother;
	}

	public int getVotes() {
		return votes;
	}

	public void setVotes(int votes) {
		this.votes = votes;
	}

	public String getLastMsg() {
		return lastMsg;
	}

	public void setLastMsg(String lastMsg) {
		this.lastMsg = lastMsg;
	}

	public int[] getPouches() {
		return pouches;
	}

	public String getLastHostname() {
		InetAddress addr;
		try {
			addr = InetAddress.getByName(getLastIP());
			String hostname = addr.getHostName();
			return hostname;
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getLastIP() {
		return lastIP;
	}

	public PriceCheckManager getPriceCheckManager() {
		return priceCheckManager;
	}

	public int getPestPoints() {
		return pestPoints;
	}

	public void setPestPoints(int pestPoints) {
		this.pestPoints = pestPoints;
	}

	public boolean isUpdateMovementType() {
		return updateMovementType;
	}

	public long getLastPublicMessage() {
		return lastPublicMessage;
	}

	public void setLastPublicMessage(long lastPublicMessage) {
		this.lastPublicMessage = lastPublicMessage;
	}

	public CutscenesManager getCutscenesManager() {
		return cutscenesManager;
	}

	public void kickPlayerFromFriendsChannel(String name) {
		if (currentFriendChat == null) {
			return;
		}
		currentFriendChat.kickPlayerFromChat(this, name);
	}

	public void sendFriendsChannelMessage(ChatMessage message) {
		if (currentFriendChat == null) {
			return;
		}
		currentFriendChat.sendMessage(this, message);
	}

	public void sendFriendsChannelQuickMessage(QuickChatMessage message) {
		if (currentFriendChat == null) {
			return;
		}
		currentFriendChat.sendQuickMessage(this, message);
	}

	public void sendPublicChatMessage(PublicChatMessage message) {
		for (int regionId : getMapRegionsIds()) {
			List<Integer> playersIndexes = World.getRegion(regionId).getPlayerIndexes();
			if (playersIndexes == null) {
				continue;
			}
			for (Integer playerIndex : playersIndexes) {
				Player p = World.getPlayers().get(playerIndex);
				if (p == null || !p.hasStarted() || p.hasFinished() || p.getLocalPlayerUpdate().getLocalPlayers()[getIndex()] == null) {
					continue;
				}
				if (p.getFriendsIgnores().containsIgnore(this.getUsername()) || this.getFriendsIgnores().containsIgnore(p.getUsername())) {
					continue;
				}
				p.getPackets().sendPublicMessage(this, message);
			}
		}
	}

	public boolean hasStarted() {
		return started;
	}

	public LocalPlayerUpdate getLocalPlayerUpdate() {
		return localPlayerUpdate;
	}

	public FriendsIgnores getFriendsIgnores() {
		return friendsIgnores;
	}

	public int[] getCompletionistCapeCustomized() {
		return completionistCapeCustomized;
	}

	public void setCompletionistCapeCustomized(int[] skillcapeCustomized) {
		this.completionistCapeCustomized = skillcapeCustomized;
	}

	public int[] getMaxedCapeCustomized() {
		return maxedCapeCustomized;
	}

	public void setMaxedCapeCustomized(int[] maxedCapeCustomized) {
		this.maxedCapeCustomized = maxedCapeCustomized;
	}

	public int getSkullHeadiconId() {
		int killStreak = facade.getKillstreak();

		if (killStreak >= 10) {
			return 2;
		} else if (killStreak >= 7) {
			return 3;
		} else if (killStreak >= 5) {
			return 4;
		} else if (killStreak >= 3) {
			return 5;
		} else if (killStreak >= 1) {
			return 6;
		}
		return getSkullId();
	}

	public int getSkullId() {
		return skullId;
	}

	public void setSkullId(int skullId) {
		this.skullId = skullId;
	}

	public boolean isFilterGame() {
		return filterGame;
	}

	public void setFilterGame(boolean filterGame) {
		this.filterGame = filterGame;
	}

	public void addLogicPacketToQueue(LogicPacket packet) {
		for (LogicPacket p : logicPackets) {
			if (p.getId() == packet.getId()) {
				logicPackets.remove(p);
				break;
			}
		}
		logicPackets.add(packet);
	}

	public DominionTower getDominionTower() {
		return dominionTower;
	}

	public void setPrayerRenewalDelay(int delay) {
		this.prayerRenewalDelay = delay;
	}

	public int getOverloadDelay() {
		return overloadDelay;
	}

	public void setOverloadDelay(int overloadDelay) {
		this.overloadDelay = overloadDelay;
	}

	public Trade getTrade() {
		return trade;
	}

	public long getPrayerDelay() {
		Long teleblock = (Long) getAttributes().get("PrayerBlocked");
		if (teleblock == null) {
			return 0;
		}
		return teleblock;
	}

	public void setPrayerDelay(long teleDelay) {
		getAttributes().put("PrayerBlocked", teleDelay + Utils.currentTimeMillis());
		prayer.closeAllPrayers();
	}

	public FriendChatsManager getCurrentFriendChat() {
		return currentFriendChat;
	}

	public void setCurrentFriendChat(FriendChatsManager currentFriendChat) {
		this.currentFriendChat = currentFriendChat;
	}

	public String getCurrentFriendChatOwner() {
		return currentFriendChatOwner;
	}

	public void setCurrentFriendChatOwner(String currentFriendChatOwner) {
		this.currentFriendChatOwner = currentFriendChatOwner;
	}

	public int getSummoningLeftClickOption() {
		return summoningLeftClickOption;
	}

	public void setSummoningLeftClickOption(int summoningLeftClickOption) {
		this.summoningLeftClickOption = summoningLeftClickOption;
	}

	public boolean isAvailable() {
		if (Wilderness.isAtWild(this) || getControllerManager().getController() instanceof FightPitsArena || getControllerManager().getController() instanceof CorpBeastControler || getControllerManager().getController() instanceof PestControlLobby || getControllerManager().getController() instanceof PestControlGame || getControllerManager().getController() instanceof ZGDControler || getControllerManager().getController() instanceof GodWars || getControllerManager().getController() instanceof DTControler || getControllerManager().getController() instanceof DuelArena || getControllerManager().getController() instanceof CastleWarsPlaying || getControllerManager().getController() instanceof CastleWarsWaiting || getControllerManager().getController() instanceof FightCaves || getControllerManager().getController() instanceof FightKiln || FfaZone.inPvpArea(this) || getControllerManager().getController() instanceof NomadsRequiem || getControllerManager().getController() instanceof QueenBlackDragonController || getControllerManager().getController() instanceof WarControler) {
			return false;
		}
		if (getControllerManager().getController() instanceof CrucibleControler) {
			CrucibleControler controler = (CrucibleControler) getControllerManager().getController();
			return !controler.isInside();
		}
		return true;
	}

	public long getPolDelay() {
		return polDelay;
	}

	public void setPolDelay(long delay) {
		this.polDelay = delay;
	}

	public AuraManager getAuraManager() {
		return auraManager;
	}

	public int getMovementType() {
		if (getTemporaryMoveType() != -1) {
			return getTemporaryMoveType();
		}
		return getRun() ? RUN_MOVE_TYPE : WALK_MOVE_TYPE;
	}

	public int getTemporaryMoveType() {
		return temporaryMovementType;
	}

	public void setTemporaryMoveType(int temporaryMovementType) {
		this.temporaryMovementType = temporaryMovementType;
	}

	public List<String> getOwnedObjectManagerKeys() {
		if (ownedObjectsManagerKeys == null) // temporary
		{
			ownedObjectsManagerKeys = new LinkedList<>();
		}
		return ownedObjectsManagerKeys;
	}

	public boolean hasInstantSpecial(final int weaponId) {
		switch (weaponId) {
			case 4153:
			case 15486:
			case 22207:
			case 22209:
			case 22211:
			case 22213:
			case 1377:
			case 13472:
			case 35:// Excalibur
			case 8280:
			case 14632:
				return true;
			default:
				return false;
		}
	}

	public void performInstantSpecial(final int weaponId) {
		int specAmt = PlayerCombat.getSpecialAmmount(weaponId);
		if (combatDefinitions.hasRingOfVigour()) {
			specAmt *= 0.9;
		}
		if (combatDefinitions.getSpecialAttackPercentage() < specAmt) {
			getPackets().sendGameMessage("You don't have enough power left.");
			combatDefinitions.desecreaseSpecialAttack(0);
			return;
		}/*
		if (this.getSwitchItemCache().size() > 0) {
			getCombatDefinitions().switchUsingSpecialAttack();
			return;
		}*/
		switch (weaponId) {
			case 4153:
			case 14679:
				if (!(getActionManager().getAction() instanceof PlayerCombat)) {
					getPackets().sendGameMessage("Warning: Since the maul's special is an instant attack, it will be wasted when used on a first strike.");
					combatDefinitions.switchUsingSpecialAttack();
					return;
				}
				PlayerCombat combat = (PlayerCombat) getActionManager().getAction();
				Entity target = combat.getTarget();
				if (!Utils.isInRange(getX(), getY(), getSize(), target.getX(), target.getY(), target.getSize(), 3)) {
					combatDefinitions.switchUsingSpecialAttack();
					return;
				}
				if (target.isDead()) {
					combatDefinitions.switchUsingSpecialAttack();
					return;
				}
				setNextAnimation(new Animation(1667));
				setNextGraphics(new Graphics(340, 0, 96 << 16));
				int attackStyle = getCombatDefinitions().getAttackStyle();
				combat.delayNormalHit(weaponId, attackStyle, combat.getMeleeHit(this, combat.getRandomMaxHit(this, weaponId, attackStyle, false, true, 1.1, true)));
				combatDefinitions.desecreaseSpecialAttack(specAmt);
				break;
			case 1377:
			case 13472:
				setNextAnimation(new Animation(1056));
				setNextGraphics(new Graphics(246));
				setNextForceTalk(new ForceTalk("Raarrrrrgggggghhhhhhh!"));
				int defence = (int) (skills.getLevelForXp(Skills.DEFENCE) * 0.90D);
				int attack = (int) (skills.getLevelForXp(Skills.ATTACK) * 0.90D);
				int range = (int) (skills.getLevelForXp(Skills.RANGE) * 0.90D);
				int magic = (int) (skills.getLevelForXp(Skills.MAGIC) * 0.90D);
				int strength = (int) (skills.getLevelForXp(Skills.STRENGTH) * 1.2D);
				skills.setLevel(Skills.DEFENCE, defence);
				skills.setLevel(Skills.ATTACK, attack);
				skills.setLevel(Skills.RANGE, range);
				skills.setLevel(Skills.MAGIC, magic);
				skills.setLevel(Skills.STRENGTH, strength);
				combatDefinitions.desecreaseSpecialAttack(specAmt);
				break;
			case 35:// Excalibur
			case 8280:
			case 14632:
				setNextAnimation(new Animation(1168));
				setNextGraphics(new Graphics(247));
				setNextForceTalk(new ForceTalk("For " + GameConstants.SERVER_NAME));
				final boolean enhanced = weaponId == 14632;
				skills.setLevel(Skills.DEFENCE, enhanced ? (int) (skills.getLevelForXp(Skills.DEFENCE) * 1.15D) : (skills.getLevel(Skills.DEFENCE) + 8));
				WorldTasksManager.schedule(new WorldTask() {
					int count = 5;

					@Override
					public void run() {
						if (isDead() || hasFinished() || getHitpoints() >= getMaxHitpoints()) {
							stop();
							return;
						}
						heal(enhanced ? 80 : 40);
						if (count-- == 0) {
							stop();
							return;
						}
					}
				}, 4, 2);
				combatDefinitions.desecreaseSpecialAttack(specAmt);
				break;
			case 15486:
			case 22207:
			case 22209:
			case 22211:
			case 22213:
				setNextAnimation(new Animation(12804));
				setNextGraphics(new Graphics(2319));// 2320
				setNextGraphics(new Graphics(2321));
				addPolDelay(60000);
				combatDefinitions.desecreaseSpecialAttack(specAmt);
				break;
		}
	}

	public List<Integer> getSwitchItemCache() {
		return switchItemCache;
	}

	public CombatDefinitions getCombatDefinitions() {
		return combatDefinitions;
	}

	public ActionManager getActionManager() {
		return actionManager;
	}

	public void addPolDelay(long delay) {
		polDelay = delay + Utils.currentTimeMillis();
	}

	public int getPublicStatus() {
		return publicStatus;
	}

	public void setPublicStatus(int publicStatus) {
		this.publicStatus = publicStatus;
	}

	public int getClanStatus() {
		return clanStatus;
	}

	public void setClanStatus(int clanStatus) {
		this.clanStatus = clanStatus;
	}

	public int getTradeStatus() {
		return tradeStatus;
	}

	public void setTradeStatus(int tradeStatus) {
		this.tradeStatus = tradeStatus;
	}

	public int getAssistStatus() {
		return assistStatus;
	}

	public void setAssistStatus(int assistStatus) {
		this.assistStatus = assistStatus;
	}

	public boolean isSpawnsMode() {
		return spawnsMode;
	}

	public void setSpawnsMode(boolean spawnsMode) {
		this.spawnsMode = spawnsMode;
	}

	public Notes getNotes() {
		return notes;
	}

	public IsaacKeyPair getIsaacKeyPair() {
		return isaacKeyPair;
	}

	public boolean isCompletedFightCaves() {
		return completedFightCaves;
	}

	public void setCompletedFightCaves() {
		if (!completedFightCaves) {
			completedFightCaves = true;
			refreshFightKilnEntrance();
		}
	}

	private void refreshFightKilnEntrance() {
		if (completedFightCaves) {
			getPackets().sendConfigByFile(10838, 1);
		}
	}

	public boolean isCompletedFightKiln() {
		return completedFightKiln;
	}

	public void setCompletedFightKiln() {
		completedFightKiln = true;
	}

	public boolean isWonFightPits() {
		return wonFightPits;
	}

	public void setWonFightPits() {
		wonFightPits = true;
	}

	public boolean isCantTrade() {
		return cantTrade;
	}

	public void setCantTrade(boolean canTrade) {
		this.cantTrade = canTrade;
	}

	/**
	 * Gets the pet.
	 *
	 * @return The pet.
	 */
	public Pet getPet() {
		return pet;
	}

	/**
	 * Sets the pet.
	 *
	 * @param pet
	 * 		The pet to set.
	 */
	public void setPet(Pet pet) {
		this.pet = pet;
	}

	/**
	 * Gets the petManager.
	 *
	 * @return The petManager.
	 */
	public PetManager getPetManager() {
		return petManager;
	}

	/**
	 * Sets the petManager.
	 *
	 * @param petManager
	 * 		The petManager to set.
	 */
	public void setPetManager(PetManager petManager) {
		this.petManager = petManager;
	}

	public void setInvulnerable(boolean invulnerable) {
		this.invulnerable = invulnerable;
	}

	public double getHpBoostMultiplier() {
		return hpBoostMultiplier;
	}

	public void setHpBoostMultiplier(double hpBoostMultiplier) {
		this.hpBoostMultiplier = hpBoostMultiplier;
	}

	/**
	 * Gets the killedQueenBlackDragon.
	 *
	 * @return The killedQueenBlackDragon.
	 */
	public boolean isKilledQueenBlackDragon() {
		return killedQueenBlackDragon;
	}

	/**
	 * Sets the killedQueenBlackDragon.
	 *
	 * @param killedQueenBlackDragon
	 * 		The killedQueenBlackDragon to set.
	 */
	public void setKilledQueenBlackDragon(boolean killedQueenBlackDragon) {
		this.killedQueenBlackDragon = killedQueenBlackDragon;
	}

	public boolean hasLargeSceneView() {
		return largeSceneView;
	}

	public void setLargeSceneView(boolean largeSceneView) {
		this.largeSceneView = largeSceneView;
	}

	public boolean isOldItemsLook() {
		return oldItemsLook;
	}

	/**
	 * @return the runeSpanPoint
	 */
	public int getRuneSpanPoints() {
		return runeSpanPoints;
	}

	public void setRuneSpanPoint(int runeSpanPoints) {
		this.runeSpanPoints = runeSpanPoints;
	}

	/**
	 * Adds points
	 */
	public void addRunespanPoints(int points) {
		this.runeSpanPoints += points;
	}

	public DuelRules getLastDuelRules() {
		return lastDuelRules;
	}

	public void setLastDuelRules(DuelRules duelRules) {
		this.lastDuelRules = duelRules;
	}

	public boolean isTalkedWithMarv() {
		return talkedWithMarv;
	}

	public void setTalkedWithMarv() {
		talkedWithMarv = true;
	}

	public int getCrucibleHighScore() {
		return crucibleHighScore;
	}

	public void increaseCrucibleHighScore() {
		crucibleHighScore++;
	}

	public boolean isStaff() {
		return hasPrivilegesOf(RightManager.OWNER, RightManager.ADMINISTRATOR, RightManager.SERVER_MODERATOR, RightManager.SUPPORT);
	}

	/**
	 * If we have the privileges of another right
	 *
	 * @param checkRights
	 * 		The rights to check for
	 */
	public boolean hasPrivilegesOf(Right... checkRights) {
		for (Right right : checkRights) {
			for (Right ourRights : rights) {
				if (ourRights.equals(right)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Sends a message after ticks
	 *
	 * @param text
	 * 		The message
	 * @param ticks
	 * 		The amount of ticks that must pass to see the message
	 * @param filter
	 * 		If we're filtering (the game filter)
	 */
	public void sendMessage(String text, int ticks, boolean... filter) {
		sendMessage(new String[] { text }, ticks, filter);
	}

	public void sendMessage(String[] texts, int ticks, boolean... filter) {
		for (String text : texts) {
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					getPackets().sendGameMessage(text, filter);
				}
			}, ticks);
		}
	}

	public boolean canTakeMoney(int amount) {
		return inventory.getNumerOf(995) >= amount || facade.getMoneyPouchCoins() >= amount;
	}

	public int getContainerCoins() {
		int inventoryCoins = inventory.getNumerOf(995);
		int pouchCoins = facade.getMoneyPouchCoins();
		return inventoryCoins > pouchCoins ? inventoryCoins : pouchCoins;
	}

	public boolean takeMoney(int amount) {
		if (inventory.getNumerOf(995) >= amount) {
			inventory.deleteItem(995, amount);
			return true;
		} else if (facade.getMoneyPouchCoins() >= amount) {
			MoneyPouchManagement.withdrawCoins(this, amount, false);
			return true;
		} else {
			return false;
		}
	}

	public void removeItemCompletely(int target) {
		removeItemCompletely(new Item(target));
	}

	public void removeItemCompletely(Item target) {
		for (Item item : getInventory().getItems().toArray()) {
			if (item == null) {
				continue;
			}
			if (item.getId() == target.getId()) {
				getInventory().deleteItem(item);
			}
		}
		for (Item item : getEquipment().getItems().toArray()) {
			if (item == null) {
				continue;
			}
			if (item.getId() == target.getId()) {
				getEquipment().deleteItem(item.getId(), item.getAmount());
			}
		}
		getBank().removeItem(target.getId());
		if (getFamiliar() != null && getFamiliar().getBob() != null) {
			for (Item item : getFamiliar().getBob().getBeastItems().toArray()) {
				if (item == null) {
					continue;
				}
				if (item.getId() == target.getId()) {
					getFamiliar().getBob().getBeastItems().remove(item);
				}
			}
		}
		getAppearence().generateAppearenceData();
	}

	public Inventory getInventory() {
		return inventory;
	}

	public Equipment getEquipment() {
		return equipment;
	}

	public Bank getBank() {
		return bank;
	}

	public Familiar getFamiliar() {
		return familiar;
	}

	public void setFamiliar(Familiar familiar) {
		this.familiar = familiar;
	}

	public void setRouteEvent(RouteEvent routeEvent) {
		this.routeEvent = routeEvent;
	}

	public void restoreAll() {
		setHitpoints(getMaxHitpoints());
		refreshHitPoints();
		prayer.setPrayerpoints(getSkills().getLevel(Skills.PRAYER) * 10);
		combatDefinitions.resetSpecialAttack();
		combatDefinitions.resetSpells(true);
		getPoison().reset();
		poisonImmune = 0;
		setAttackedBy(null);
		setRunEnergy(100);
		appearence.generateAppearenceData();
		getSkills().restoreSkills();
		prayer.refreshPrayerPoints();
		resetCombat();
	}

	public Skills getSkills() {
		return skills;
	}

	public ClansManager getClanManager() {
		return clanManager;
	}

	public void setClanManager(ClansManager clanManager) {
		this.clanManager = clanManager;
	}

	public ClansManager getGuestClanManager() {
		return guestClanManager;
	}

	public void setGuestClanManager(ClansManager guestClanManager) {
		this.guestClanManager = guestClanManager;
	}

	public String getClanName() {
		return clanName;
	}

	public void setClanName(String clanName) {
		this.clanName = clanName;
	}

	public boolean isConnectedClanChannel() {
		return connectedClanChannel;
	}

	public void setConnectedClanChannel(boolean connectedClanChannel) {
		this.connectedClanChannel = connectedClanChannel;
	}

	public boolean isFilteringProfanity() {
		return filteringProfanity;
	}

	public void setFilteringProfanity(boolean filteringProfanity) {
		this.filteringProfanity = filteringProfanity;
	}

	public BankPinManager getPinManager() {
		return pinManager;
	}

	public List<Item> getLostUntradeables() {
		return lostUntradeables;
	}

	/**
	 * @return the brawlingGlovesManager
	 */
	public BrawlingGlovesManager getBrawlingGlovesManager() {
		return brawlingGlovesManager;
	}

	/**
	 * Gets the preset manager
	 */
	public PresetManager getPresetManager() {
		return presetManager;
	}

	/**
	 * Gets the killtime manager
	 */
	public BossKillTimeManager getKillTimeManager() {
		return killTimeManager;
	}

	public boolean isSecurityQuestionsAnswered() {
		return securityQuestionsAnswered;
	}

	public void setSecurityQuestionsAnswered(boolean flag) {
		this.securityQuestionsAnswered = flag;
	}

	public SecurityDetails getSecurityDetails() {
		return securityDetails;
	}

	public <K> K setLastChatMessage(K lastChatMessage) {
		this.lastChatMessage = lastChatMessage;
		return lastChatMessage;
	}

	public Object getLastChatMessage() {
		return lastChatMessage;
	}

	public RandomEventManager getRandomEventManager() {
		return randomEventManager;
	}

	public LoyaltyManager getLoyaltyManager() {
		return loyaltyManager;
	}

	public CostumeManager getCostumeManager() { return costumeManager; }

	public String getTeleBlockTimeleft() {
		long minutes = TimeUnit.MILLISECONDS.toMinutes(getTeleBlockDelay() - Utils.currentTimeMillis());
		long seconds = TimeUnit.MILLISECONDS.toSeconds(getTeleBlockDelay() - Utils.currentTimeMillis());
		String secondsMessage = (seconds != 1 ? seconds + " seconds" : "second");
		String minutesMessage = (minutes != 1 ? minutes + " minutes" : "minute");
		return (minutes > 0 ? minutesMessage : secondsMessage);
	}

	public long getTeleBlockDelay() {
		Long teleblock = (Long) getAttributes().get("TeleBlocked");
		if (teleblock == null) {
			return 0;
		}
		return teleblock;
	}

	public void setTeleBlockDelay(long teleDelay) {
		getAttributes().put("TeleBlocked", teleDelay + Utils.currentTimeMillis());
	}

	public void storeSwitch(int slot) {
		List<Integer> pendingSwitches = getAttribute("pending_switches");
		if (pendingSwitches == null) {
			pendingSwitches = new ArrayList<>();
		}
		if (!pendingSwitches.contains(slot)) {
			pendingSwitches.add(slot);
		}
		putAttribute("pending_switches", pendingSwitches);
	}

	public Runnable getLogicPacketProcessEvent() {
		return logicPacketProcessEvent;
	}

	public void setLogicPacketProcessEvent(Runnable event) {
		this.logicPacketProcessEvent = event;
	}

	public boolean isIronman() {
		return hasPrivilegesOf(RightManager.IRONMAN);
	}

	public boolean isUltimateIronman() {
		return hasPrivilegesOf(RightManager.ULTIMATE_IRONMAN);
	}

	public boolean isAnyIronman() {
		return hasPrivilegesOf(RightManager.IRONMAN, RightManager.ULTIMATE_IRONMAN);
	}

	public void reset(boolean inventory, boolean equipment, boolean bank, boolean familiar) {
		if (inventory) {
			this.inventory = new Inventory();
			this.lostUntradeables.clear();
			this.facade.setMoneyPouchCoins(0);
		}
		if (equipment) {
			this.equipment = new Equipment();
		}
		if (bank) {
			this.bank = new Bank();
		}
		if (familiar) {
			if (familiarSerialization != null) {
				familiarSerialization.getBob().getBeastItems().clear();
			}
		}
	}

	public FarmingManager getFarmingManager() {
		return farmingManager;
	}

	public Runnable getCloseInterfacesEvent() {
		return closeInterfacesEvent;
	}

	public void setCloseInterfacesEvent(Runnable closeInterfacesEvent) {
		this.closeInterfacesEvent = closeInterfacesEvent;
	}

	public void refreshLootShare() {
		getPackets().sendConfig(1083, facade.isLootshareEnabled() ? 1 : 0);
	}

	public void increaseFireChargesIfPossible() {
		if (equipment.getShieldId() == 11283 || equipment.getShieldId() == 11284) {
			if (facade.getDragonFireCharges() >= 40) {
				facade.setDragonFireCharges(40);
				sendMessage("Your shield is fully charged.");
			} else {
				facade.setDragonFireCharges(facade.getDragonFireCharges() + 1);
				sendMessage("You absorb the fire breath and charge your dragonfire shield.");
			}
			setNextAnimation(new Animation(6695));
			setNextGraphics(new Graphics(1164));
			if (equipment.getShieldId() != 11284) {
				equipment.getItems().set(Equipment.SLOT_SHIELD, new Item(11284));
			}
			equipment.refreshAll();
		}
	}

    public String getPasswordHash() {
        return this.passwordHash;
    }

    public double getRunEnergy() {
        return this.runEnergy;
    }

    public int getFriendChatSetup() {
        return this.friendChatSetup;
    }

    public String getPassword() {
        return this.password;
    }

    public long getSignInTime() {
        return this.signInTime;
    }

    public int getChatType() {
        return this.chatType;
    }

    public List<Right> getRights() {
        return this.rights;
    }

    public Map<String, Long> getProcessData() {
        return this.processData;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setChatType(int chatType) {
        this.chatType = chatType;
    }

    public void setRights(List<Right> rights) {
        this.rights = rights;
    }
}