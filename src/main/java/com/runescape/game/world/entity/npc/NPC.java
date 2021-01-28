package com.runescape.game.world.entity.npc;

import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.cache.loaders.NPCDefinitions;
import com.runescape.game.GameConstants;
import com.runescape.game.content.FriendChatsManager;
import com.runescape.game.content.economy.treasure.TreasureTrailHandler;
import com.runescape.game.content.skills.prayer.Burying.Bone;
import com.runescape.game.content.skills.slayer.SlayerManagement;
import com.runescape.game.event.interaction.item.CasketInteractionEvent;
import com.runescape.game.interaction.controllers.impl.Wilderness;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.masks.Hit;
import com.runescape.game.world.entity.masks.Hit.HitLook;
import com.runescape.game.world.entity.npc.combat.NPCCombat;
import com.runescape.game.world.entity.npc.combat.NPCCombatDefinitions;
import com.runescape.game.world.entity.npc.familiar.impl.Familiar;
import com.runescape.game.world.entity.npc.pet.RewardPet;
import com.runescape.game.world.entity.player.BossKillTimeManager;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.game.world.entity.player.achievements.AchievementHandler;
import com.runescape.game.world.entity.player.achievements.easy.Chicken_Murder;
import com.runescape.game.world.entity.player.achievements.easy.Cows_Beware;
import com.runescape.game.world.entity.player.achievements.easy.Manslaughter;
import com.runescape.game.world.entity.player.achievements.elite.Bandos_Hunter;
import com.runescape.game.world.entity.player.achievements.elite.Corporeal_Punishment;
import com.runescape.game.world.entity.player.achievements.elite.Tormention;
import com.runescape.game.world.entity.player.achievements.elite.Whip_Hunter;
import com.runescape.game.world.entity.player.achievements.hard.Demonizer;
import com.runescape.game.world.entity.player.achievements.hard.Dragon_Slayer;
import com.runescape.game.world.entity.player.achievements.medium.Armadyl_Hatred;
import com.runescape.game.world.entity.player.achievements.medium.I_Got_The_Blues;
import com.runescape.game.world.entity.player.achievements.medium.Rock_Hunter;
import com.runescape.game.world.item.Item;
import com.runescape.game.world.item.ItemConstants;
import com.runescape.utility.ChatColors;
import com.runescape.utility.Utils;
import com.runescape.utility.external.gson.loaders.NPCDataLoader;
import com.runescape.utility.external.gson.loaders.NPCSpawnLoader.Direction;
import com.runescape.utility.external.gson.resource.NPCData;
import com.runescape.utility.world.map.MapAreas;
import com.runescape.utility.world.npc.NPCNames;
import com.runescape.workers.game.core.CoresManager;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NPC extends Entity implements Serializable {

	/** The names of bosses */
	public static final String[] BOSS_NAMES = { "King Black Dragon", "Glacor", "Graardor", "Kree'arra", "K'ril Tsutsaroth", "TzTok-Jad", "Nex", "Corporeal Beast", "Kalphite Queen", "Tormented demon", "Chaos elemental", "Sea Troll Queen", "Bork" };

	private static final long serialVersionUID = -4794678936277614443L;

	public static int NO_WALK = 0x0, NORMAL_WALK = 0x2, WATER_WALK = 0x4, FLY_WALK = 0x8;

	public WorldTile forceWalk;

	private int id;

	private WorldTile respawnTile;

	private int mapAreaNameHash;

	private boolean canBeAttackFromOutOfArea;

	private int[] bonuses; // 0 stab, 1 slash, 2 crush,3 mage, 4 range, 5 stab

	// def, blahblah till 9
	private boolean spawned;

	private long lastAttackedByTarget;

	private boolean cantInteract;

	private int capDamage;

	private int lureDelay;

	private boolean cantFollowUnderCombat;

	private boolean forceAggressive;

	private int forceTargetDistance;

	private boolean forceFollowClose;

	private boolean forceMultiAttacked;

	private boolean noDistanceCheck;

	private boolean intelligentRouteFinder;

	private int walkType;

	// name changing masks
	private String name;

	private int combatLevel;

	private boolean hasCustomName;

	protected int respawnTileDistance;

	private transient NPCCombat combat;

	// npc masks
	private transient Transformation nextTransformation;

	private transient NPCCombatDefinitions combatDefinitions;

	private transient NPCDefinitions npcDefinitions;

	private transient boolean changedName;

	private transient Direction faceDirection = Direction.NORTH;

	private transient boolean changedCombatLevel;

	private transient boolean locked;

	@Override
	public void finish() {
		if (hasFinished()) {
			return;
		}
		setFinished(true);
		World.updateEntityRegion(this);
		World.removeNPC(this);
	}

	@Override
	public double getMagePrayerMultiplier() {
		return 0;
	}

	@Override
	public double getRangePrayerMultiplier() {
		return 0;
	}

	@Override
	public double getMeleePrayerMultiplier() {
		return 0;
	}

	@Override
	public void reset() {
		super.reset();
		setDirection(getRespawnDirection());
		combat.reset();
		bonuses = NPCDataLoader.getBonuses(id); // back to real bonuses
		forceWalk = null;
	}

	@Override
	public int getMaxHitpoints() {
		return getCombatDefinitions().getHitpoints();
	}

	@Override
	public int getSize() {
		return getDefinitions().size;
	}

	@Override
	public boolean needMasksUpdate() {
		return super.needMasksUpdate() || nextTransformation != null || changedCombatLevel || hasChangedName();
	}

	@Override
	public void resetMasks() {
		super.resetMasks();
		nextTransformation = null;
		changedCombatLevel = false;
		if (!hasCustomName) {
			setHasChangedName(false);
		}
	}

	@Override
	public long processEntity() {
		long start = System.currentTimeMillis();
		super.processEntity();
		processNPC();
		return System.currentTimeMillis() - start;
	}

	@Override
	public void sendDeath(Entity source) {
		final NPCCombatDefinitions defs = getCombatDefinitions();
		resetWalkSteps();
		combat.removeTarget();
		setNextAnimation(null);
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					setNextAnimation(new Animation(defs.getDeathEmote()));
				} else if (loop >= defs.getDeathDelay()) {
					drop();
					reset();
					setLocation(respawnTile);
					finish();
					if (!isSpawned()) {
						setRespawnTask();
					}
					stop();
				}
				loop++;
			}
		}, 0, 1);
	}

	@Override
	public boolean isNPC() {
		return true;
	}

	@Override
	public void handleIngoingHit(final Hit hit) {
		if (capDamage != -1 && hit.getDamage() > capDamage) {
			hit.setDamage(capDamage);
		}
		if (hit.getLook() != HitLook.MELEE_DAMAGE && hit.getLook() != HitLook.RANGE_DAMAGE && hit.getLook() != HitLook.MAGIC_DAMAGE) {
			return;
		}
		Entity source = hit.getSource();
		if (source == null) {
			return;
		}
		if (source instanceof Player) {
			final Player p2 = (Player) source;
			if (p2.getAttribute("god_mode", false)) {
				hit.setDamage(getHitpoints());
			}
			if (p2.getPrayer().hasPrayersOn()) {
				if (p2.getPrayer().usingPrayer(1, 18)) {
					sendSoulSplit(hit, p2);
				}
				if (hit.getDamage() == 0) {
					return;
				}
				if (!p2.getPrayer().isBoostedLeech()) {
					if (hit.getLook() == HitLook.MELEE_DAMAGE) {
						if (p2.getPrayer().usingPrayer(1, 19)) {
							p2.getPrayer().setBoostedLeech(true);
							return;
						} else if (p2.getPrayer().usingPrayer(1, 1)) { // sap
							// att
							if (Utils.getRandom(4) == 0) {
								if (p2.getPrayer().reachedMax(0)) {
									p2.getPackets().sendGameMessage("Your opponent has been weakened so much that your sap curse has no effect.", true);
								} else {
									p2.getPrayer().increaseLeechBonus(0);
									p2.getPackets().sendGameMessage("Your curse drains Attack from the enemy, boosting your Attack.", true);
								}
								p2.setNextAnimation(new Animation(12569));
								p2.setNextGraphics(new Graphics(2214));
								p2.getPrayer().setBoostedLeech(true);
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
							if (p2.getPrayer().usingPrayer(1, 10)) {
								if (Utils.getRandom(7) == 0) {
									if (p2.getPrayer().reachedMax(3)) {
										p2.getPackets().sendGameMessage("Your opponent has been weakened so much that your leech curse has no effect.", true);
									} else {
										p2.getPrayer().increaseLeechBonus(3);
										p2.getPackets().sendGameMessage("Your curse drains Attack from the enemy, boosting your Attack.", true);
									}
									p2.setNextAnimation(new Animation(12575));
									p2.getPrayer().setBoostedLeech(true);
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
							if (p2.getPrayer().usingPrayer(1, 14)) {
								if (Utils.getRandom(7) == 0) {
									if (p2.getPrayer().reachedMax(7)) {
										p2.getPackets().sendGameMessage("Your opponent has been weakened so much that your leech curse has no effect.", true);
									} else {
										p2.getPrayer().increaseLeechBonus(7);
										p2.getPackets().sendGameMessage("Your curse drains Strength from the enemy, boosting your Strength.", true);
									}
									p2.setNextAnimation(new Animation(12575));
									p2.getPrayer().setBoostedLeech(true);
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
						if (p2.getPrayer().usingPrayer(1, 2)) { // sap range
							if (Utils.getRandom(4) == 0) {
								if (p2.getPrayer().reachedMax(1)) {
									p2.getPackets().sendGameMessage("Your opponent has been weakened so much that your sap curse has no effect.", true);
								} else {
									p2.getPrayer().increaseLeechBonus(1);
									p2.getPackets().sendGameMessage("Your curse drains Range from the enemy, boosting your Range.", true);
								}
								p2.setNextAnimation(new Animation(12569));
								p2.setNextGraphics(new Graphics(2217));
								p2.getPrayer().setBoostedLeech(true);
								World.sendProjectile(p2, this, 2218, 35, 35, 20, 5, 0, 0);
								WorldTasksManager.schedule(new WorldTask() {
									@Override
									public void run() {
										setNextGraphics(new Graphics(2219));
									}
								}, 1);
								return;
							}
						} else if (p2.getPrayer().usingPrayer(1, 11)) {
							if (Utils.getRandom(7) == 0) {
								if (p2.getPrayer().reachedMax(4)) {
									p2.getPackets().sendGameMessage("Your opponent has been weakened so much that your leech curse has no effect.", true);
								} else {
									p2.getPrayer().increaseLeechBonus(4);
									p2.getPackets().sendGameMessage("Your curse drains Range from the enemy, boosting your Range.", true);
								}
								p2.setNextAnimation(new Animation(12575));
								p2.getPrayer().setBoostedLeech(true);
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
						if (p2.getPrayer().usingPrayer(1, 3)) { // sap mage
							if (Utils.getRandom(4) == 0) {
								if (p2.getPrayer().reachedMax(2)) {
									p2.getPackets().sendGameMessage("Your opponent has been weakened so much that your sap curse has no effect.", true);
								} else {
									p2.getPrayer().increaseLeechBonus(2);
									p2.getPackets().sendGameMessage("Your curse drains Magic from the enemy, boosting your Magic.", true);
								}
								p2.setNextAnimation(new Animation(12569));
								p2.setNextGraphics(new Graphics(2220));
								p2.getPrayer().setBoostedLeech(true);
								World.sendProjectile(p2, this, 2221, 35, 35, 20, 5, 0, 0);
								WorldTasksManager.schedule(new WorldTask() {
									@Override
									public void run() {
										setNextGraphics(new Graphics(2222));
									}
								}, 1);
								return;
							}
						} else if (p2.getPrayer().usingPrayer(1, 12)) {
							if (Utils.getRandom(7) == 0) {
								if (p2.getPrayer().reachedMax(5)) {
									p2.getPackets().sendGameMessage("Your opponent has been weakened so much that your leech curse has no effect.", true);
								} else {
									p2.getPrayer().increaseLeechBonus(5);
									p2.getPackets().sendGameMessage("Your curse drains Magic from the enemy, boosting your Magic.", true);
								}
								p2.setNextAnimation(new Animation(12575));
								p2.getPrayer().setBoostedLeech(true);
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

					// overall

					if (p2.getPrayer().usingPrayer(1, 13)) { // leech defence
						if (Utils.getRandom(10) == 0) {
							if (p2.getPrayer().reachedMax(6)) {
								p2.getPackets().sendGameMessage("Your opponent has been weakened so much that your leech curse has no effect.", true);
							} else {
								p2.getPrayer().increaseLeechBonus(6);
								p2.getPackets().sendGameMessage("Your curse drains Defence from the enemy, boosting your Defence.", true);
							}
							p2.setNextAnimation(new Animation(12575));
							p2.getPrayer().setBoostedLeech(true);
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
				}
			}
		}

	}

	public void sendSoulSplit(final Hit hit, final Entity user) {
		final NPC target = this;
		if (hit.getDamage() > 0) {
			World.sendProjectile(user, this, 2263, 11, 11, 20, 5, 0, 0);
		}
		user.heal(hit.getDamage() / 5);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				setNextGraphics(new Graphics(2264));
				if (hit.getDamage() > 0) {
					World.sendProjectile(target, user, 2263, 11, 11, 20, 5, 0, 0);
				}
			}
		}, 1);
	}

	@Override
	public void setAttackedBy(Entity target) {
		super.setAttackedBy(target);
		if (target == combat.getTarget() && !(combat.getTarget() instanceof Familiar)) {
			lastAttackedByTarget = Utils.currentTimeMillis();
		}
	}

	public NPC npc() {
		return this;
	}

	public void setNPC(int id) {
		this.id = id;
		setBonuses(NPCDataLoader.getBonuses(id));
		setCombatDefinitions(NPCDataLoader.getCombatDefinitions(id));
		setNPCDefinitions(NPCDefinitions.getNPCDefinitions(id));
	}

	/**
	 * @param changedName
	 * 		the changedName to set
	 */
	public void setHasChangedName(boolean changedName) {
		this.changedName = changedName;
	}

	public boolean hasChangedName() {
		return changedName;
	}

	@Override
	public String toString() {
		return getDefinitions().getName() + "(" + id + ") @ [" + getX() + " " + getY() + " " + getPlane() + "]";
	}

	public NPCDefinitions getDefinitions() {
		if (npcDefinitions == null) {
			npcDefinitions = NPCDefinitions.getNPCDefinitions(getId());
		}
		return npcDefinitions;
	}

	public int getId() {
		return id;
	}

	public NPC(int id, WorldTile tile) {
		this(id, tile, -1, true);
	}

	public NPC(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
		this(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, false);
	}

	/*
	 * creates and adds npc
	 */
	public NPC(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(tile);
		this.id = id;
		this.respawnTile = new WorldTile(tile);
		this.respawnTileDistance = 16;
		this.mapAreaNameHash = mapAreaNameHash;
		this.canBeAttackFromOutOfArea = canBeAttackFromOutOfArea;
		this.setSpawned(spawned);
		combatDefinitions = NPCDataLoader.getCombatDefinitions(id);
		npcDefinitions = NPCDefinitions.getNPCDefinitions(id);
		combatLevel = -1;
		setHitpoints(getMaxHitpoints());
		setDirection(getRespawnDirection());
		setWalkType(getDefinitions().walkMask);
		bonuses = NPCDataLoader.getBonuses(id);
		combat = new NPCCombat(this);
		capDamage = -1;
		lureDelay = 12000;
		// npc is inited on creating instance
		initEntity();
		World.addNPC(this);
		World.updateEntityRegion(this);
		// npc is started on creating instance
		loadMapRegions();
		checkMultiArea();
		setCustomName();
	}

	private void setCustomName() {
		String customName = NPCNames.getName(id);
		if (customName != null) {
			setName(customName);
			hasCustomName = true;
		}
	}

	/**
	 * If this npc should be updated as part of the inscreen npcs update
	 */
	public boolean needInscreenUpdate() {
		return super.needMasksUpdate() || nextTransformation != null || changedCombatLevel;
	}

	public void transformIntoNPC(int id) {
		setNPC(id);
		nextTransformation = new Transformation(id);
	}

	public void setNPCDefinitions(NPCDefinitions definitions) {
		this.npcDefinitions = definitions;
	}

	public int getMapAreaNameHash() {
		return mapAreaNameHash;
	}

	public void setCanBeAttackFromOutOfArea(boolean b) {
		canBeAttackFromOutOfArea = b;
	}

	public boolean canBeAttackFromOutOfArea() {
		return canBeAttackFromOutOfArea;
	}

	public boolean hasCombatDefinitions() {
		return combatDefinitions.getHitpoints() > 1;
	}

	public int getRandomTileDistance() {
		switch (id) {
			case 1:
				if (GameConstants.START_PLAYER_LOCATION.getRegionId() == getRegionId()) {
					return 3;
				}
			case 961:
				return Utils.random(-2, 2);
			case 1263:
				return Utils.random(-1, 2);
			default:
				return Utils.random(-5, 5);
		}
	}

	public void processNPC() {
		if (isDead() || locked) {
			return;
		}
		if (respawnTile != null && !respawnTile.withinDistance(this, respawnTileDistance) && !hasWalkSteps() && respawnTileDistance > 0) {
			setForceWalk(respawnTile);
		} else if (!combat.process()) { // if not under combat
			if (!isForceWalking()) {// combat still processed for attack
				// delay
				// go down
				// random walk
				if (!cantInteract) {
					if (!checkAggressivity()) {
						if (getFreezeDelay() < Utils.currentTimeMillis()) {
							if (((getWalkType() & NORMAL_WALK) != 0) && Math.random() * 1000.0 < 100.0) {
								int moveX = getRandomTileDistance();
								int moveY = getRandomTileDistance();
								/*if (id == 961 || id == 1263) {
									System.out.println("id=" + id + ", moveX=" + moveX + ", moveY=" + moveY);
								}*/
								resetWalkSteps();
								if (getMapAreaNameHash() != -1) {
									if (!MapAreas.isAtArea(getMapAreaNameHash(), this)) {
										forceWalkRespawnTile();
										return;
									}
									addWalkSteps(getX() + moveX, getY() + moveY, 5);
								} else {
									addWalkSteps(respawnTile.getX() + moveX, respawnTile.getY() + moveY, 5);
								}
							}
						}
					}
				}
			}
		}
		if (isForceWalking()) {
			if (getFreezeDelay() < Utils.currentTimeMillis()) {
				if (getX() != forceWalk.getX() || getY() != forceWalk.getY()) {
					if (!hasWalkSteps()) {
						addWalkSteps(forceWalk.getX(), forceWalk.getY(), getSize(), true);
					}
					if (!hasWalkSteps()) { // failing finding route
						setNextWorldTile(new WorldTile(forceWalk)); // force
						// tele
						// to
						// the
						// forcewalk
						// place
						forceWalk = null; // so ofc reached forcewalk place
					}
				} else
				// walked till forcewalk place
				{
					forceWalk = null;
				}
			}
		}
	}

	public int getRespawnDirection() {
		NPCDefinitions definitions = getDefinitions();
		if (definitions.anInt853 << 32 != 0 && definitions.respawnDirection > 0 && definitions.respawnDirection <= 8) {
			return (4 + definitions.respawnDirection) << 11;
		}
		return 0;
	}

	public void setRespawnTask() {
		if (!hasFinished()) {
			reset();
			setLocation(respawnTile);
			finish();
		}
		CoresManager.schedule(() -> {
			try {
				spawn();
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}, getCombatDefinitions().getRespawnDelay() * 600, TimeUnit.MILLISECONDS);
	}

	public void deserialize() {
		this.setSpawned(spawned);
		combatDefinitions = NPCDataLoader.getCombatDefinitions(id);
		npcDefinitions = NPCDefinitions.getNPCDefinitions(id);
		combatLevel = -1;
		setHitpoints(getMaxHitpoints());
		setDirection(getRespawnDirection());
		setWalkType(getDefinitions().walkMask);
		bonuses = NPCDataLoader.getBonuses(id);
		capDamage = -1;
		lureDelay = 12000;

		// npc is inited on creating instance
		initEntity();
		// npc is started on creating instance
		setCustomName();
		if (combat == null) {
			combat = new NPCCombat(this);
		}
		spawn();
	}

	public void spawn() {
		setFinished(false);
		World.addNPC(this);
		setLastRegionId(0);
		World.updateEntityRegion(this);
		loadMapRegions();
		checkMultiArea();
	}

	public NPCCombat getCombat() {
		return combat;
	}

	public void drop() {
		try {
			Player killer = getMostDamageReceivedSourcePlayer();
			if (killer == null) {
				return;
			}
			sendOtherLoots(killer);
			NPCData npcData = NPCDataLoader.getData(getName());
			if (npcData == null) {
				return;
			}
			List<Drop> drops = npcData.getDrops();
			List<Player> players = FriendChatsManager.getLootSharingPeople(killer);
			List<Drop> dropList = generateDrops(npcData, drops, killer);
			boolean equippingROW = killer.getEquipment().getRingId() == 2572;
			boolean usedRow = false;
			if (players == null || players.size() == 1) {
				for (Drop drop : dropList) {
					if (customDropAction(killer, drop)) {
						continue;
					}
					sendDrop(killer, drop);
					if (equippingROW && killer.getFacade().getRowCharges() > 0) {
						usedRow = true;
					}
					if (equippingROW && killer.getFacade().getRowCharges() <= 0 && !killer.getAttribute("row_notified", false)) {
						killer.putAttribute("row_notified", true);
						killer.sendMessage("You have a ring of wealth equipped with 0 charges. Charge it with Sir Tiffy Cashien at the home portal!");
					}
				}
			} else {
				for (Drop drop : dropList) {
					if (customDropAction(killer, drop)) {
						continue;
					}
					Player luckyPlayer = players.get(Utils.random(players.size()));
					Item item = sendDrop(luckyPlayer, drop);
					luckyPlayer.getPackets().sendGameMessage("<col=00FF00>You received: " + item.getAmount() + " " + item.getName() + ".");
					for (Player p2 : players) {
						if (p2 == luckyPlayer) { continue; }
						p2.getPackets().sendGameMessage("<col=66FFCC>" + luckyPlayer.getDisplayName() + "</col> received: " + item.getAmount() + " " + item.getName() + ".");
						p2.getPackets().sendGameMessage("Your chance of receiving loot has improved.");
					}
				}
			}
			if (usedRow) {
				killer.getFacade().setRowCharges(killer.getFacade().getRowCharges() <= 1 ? 0 : killer.getFacade().getRowCharges() - 1);
				if (killer.getFacade().getRowCharges() > 0) {
					killer.sendMessage("You now have " + killer.getFacade().getRowCharges() + " ring of wealth charges left.");
				}
			}
			dropCharms(killer, npcData);
		} catch (Exception | Error e) {
			e.printStackTrace();
		}
	}

	/**
	 * Handles a custom drop action
	 *
	 * @param killer
	 * 		The player who killed
	 * @param drop
	 * 		The dropped item
	 */
	private boolean customDropAction(Player killer, Drop drop) {
		if (killer.getInventory().contains(18337)) {
			Bone bone = Bone.forId(drop.getItemId());
			if (bone == null) {
				return false;
			}
			killer.getSkills().addXp(Skills.PRAYER, bone.getExperience());
			return true;
		}
		return false;
	}

	/**
	 * Drops a random charm
	 *
	 * @param killer
	 * 		The killer
	 * @param data
	 * 		The npc data instannce
	 */
	private void dropCharms(Player killer, NPCData data) {
		List<Drop> charms = data.getCharmDrops();
		if (charms.isEmpty()) {
			return;
		}
		Collections.shuffle(charms);
		double random = (Utils.getRandomDouble(99) + 1) / 100;
		for (Drop charm : charms) {
			if (random <= charm.getRate()) {
				sendDrop(killer, charm);
				break;
			}
		}
	}

	/**
	 * Generates a list of items to drop
	 *
	 * @param npcData
	 * 		The data
	 * @param drops
	 * 		The drops
	 * @param killer
	 * 		The killer
	 */
	protected List<Drop> generateDrops(NPCData npcData, List<Drop> drops, Player killer) {
		return npcData.generateDrops(killer, drops, this);
	}

	/**
	 * @param killer
	 * 		The killer
	 */
	public void sendOtherLoots(Player killer) {
		boolean slayerTask = SlayerManagement.isTask(killer, this);
		if (slayerTask) {
			SlayerManagement.reduceTaskAmount(killer, this);
		}

		incrementAchievementProgress(killer);
		boolean shouldDropClue = Utils.percentageChance(1);
		if (shouldDropClue) {
			long percentageChange = Utils.random(1, 300);
			int lvl = getCombatLevel();
			int clueId = -1;
			if (lvl >= 90 && percentageChange <= 5) {
				clueId = TreasureTrailHandler.ELITE_SCROLL_ID;
			} else if (lvl >= 50 && percentageChange > 5 && percentageChange <= 50) {
				clueId = TreasureTrailHandler.HARD_SCROLL_ID;
			} else if (lvl >= 30 && percentageChange > 50 && percentageChange <= 75) {
				clueId = TreasureTrailHandler.MED_SCROLL_ID;
			} else if (lvl >= 10 && percentageChange <= 90) {
				clueId = TreasureTrailHandler.EASY_SCROLL_ID;
			}
			if (clueId != -1) {
				sendDrop(killer, new Item(clueId, 1), false);
				killer.sendMessage("<col=" + ChatColors.MAROON + ">You receive a clue scroll drop</col>.");
			}
		}

		if (dropsCasket(slayerTask)) {
			if (killer.getInventory().contains(18344)) {
				CasketInteractionEvent.giveCasketLoot(killer);
			} else {
				sendDrop(killer, new Item(7312, 1), false);
			}
		}

		// killtimes
		if (isBoss()) {
			// killcount
			killer.getFacade().incrementBossKills(getName());
			killer.sendMessage("Your " + getName() + " kill count is now: <col=" + ChatColors.RED + ">" + killer.getFacade().getTimesKilledBoss(getName()) + "</col>.", 1, true);
			// kill times
			if (getAttribute("first_hit_time") != null) {
				Long firstTimeHit = getAttribute("first_hit_time");
				long timeTaken = System.currentTimeMillis() - firstTimeHit;
				killer.getKillTimeManager().storeKillTime(this, timeTaken);
				killer.sendMessage("Fight Duration: <col=" + ChatColors.RED + ">" + BossKillTimeManager.formatTime(timeTaken) + "</col>. Personal Best: <col=" + ChatColors.RED + ">" + BossKillTimeManager.formatTime(killer.getKillTimeManager().getBestTime(this)) + "</col>.");
			}
		}

		boolean revenant = getName().toLowerCase().contains("revenant");
		if (revenant || (Wilderness.getWildLevel(this) >= 10 && getSize() > 1)) {
			int amount = getSize() * 2;

			if (revenant) {
				amount = (getSize() * 3);
			}
			sendDrop(killer, new Item(Wilderness.WILDERNESS_TOKEN, amount), false);
		}
	}

	public void incrementAchievementProgress(Player killer) {
		if (getName().toLowerCase().contains("chicken")) {
			AchievementHandler.incrementProgress(killer, Chicken_Murder.class, 1);
		}
		if (getName().equalsIgnoreCase("Man")) {
			AchievementHandler.incrementProgress(killer, Manslaughter.class, 1);
		}
		if (getName().equalsIgnoreCase("Rock crab")) {
			AchievementHandler.incrementProgress(killer, Rock_Hunter.class, 1);
		}
		if (getName().toLowerCase().contains("dragon")) {
			AchievementHandler.incrementProgress(killer, Dragon_Slayer.class, 1);
		}
		if (getName().toLowerCase().equals("cow")) {
			AchievementHandler.incrementProgress(killer, Cows_Beware.class, 1);
		}
		if (getId() == 6260) {
			AchievementHandler.incrementProgress(killer, Bandos_Hunter.class, 1);
		}
		if (getId() == 6222) {
			AchievementHandler.incrementProgress(killer, Armadyl_Hatred.class, 1);
		}
		if (getName().toLowerCase().contains("tormented demon")) {
			AchievementHandler.incrementProgress(killer, Tormention.class, 1);
		}
		if (getId() == 8133) {
			AchievementHandler.incrementProgress(killer, Corporeal_Punishment.class, 1);
		}
		if (getId() == 1615) {
			AchievementHandler.incrementProgress(killer, Demonizer.class, 1);
			boolean luckyEnough = Utils.percentageChance(5) && Utils.percentageChance(25) && Utils.percentageChance(50);
			if (AchievementHandler.getProgress(killer, Demonizer.class) >= 100 && luckyEnough) {
				RewardPet.addPet(killer, RewardPet.ABYSSAL_MINION);
			}
		}
		if (getName().equalsIgnoreCase("Blue dragon")) {
			AchievementHandler.incrementProgress(killer, I_Got_The_Blues.class, 1);
			boolean luckyEnough = Utils.percentageChance(10) && Utils.percentageChance(25) && Utils.percentageChance(50);
			if (AchievementHandler.getProgress(killer, I_Got_The_Blues.class) >= 50 && luckyEnough) {
				RewardPet.addPet(killer, RewardPet.HATCHLING_DRAGON);
			}
		}
	}

	private boolean dropsCasket(boolean slayerTaskDeath) {
		int chance = Utils.random(1, 400);
		if (slayerTaskDeath) {
			chance -= Utils.random(50);
		}
		int lvl = getCombatLevel();
		if (lvl > 30 && lvl < 50) {
			chance -= Utils.random(10);
		} else if (lvl > 50 && lvl < 90) {
			chance -= Utils.random(20);
		} else if (lvl > 90) {
			chance -= Utils.random(30);
		} else {
			// hp must be atleast greater than 100 to give caskets
			if (getMaxHitpoints() < 100) {
				chance = 50;
			}
		}
		return chance < 20;
	}

	/**
	 * Checking the name of the npc to see if it is a boss
	 */
	public boolean isBoss() {
		for (String bossName : BOSS_NAMES) {
			if (getName().toLowerCase().contains(bossName.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public int getCombatLevel() {
		return combatLevel >= 0 ? combatLevel : getDefinitions().combatLevel;
	}

	public void setCombatLevel(int level) {
		combatLevel = getDefinitions().combatLevel == level ? -1 : level;
		changedCombatLevel = true;
	}

	public Item sendDrop(Player player, Drop drop) {
		Item item = ItemDefinitions.forId(drop.getItemId()).isStackable() ? new Item(drop.getItemId(), Utils.random(drop.getMinAmount(), drop.getMaxAmount())) : new Item(drop.getItemId(), drop.getMinAmount() + Utils.getRandom(drop.getExtraAmount()));
		sendDrop(player, item, true);
		return item;
	}

	public void sendDrop(Player player, Item item, boolean sendRareMessage) {
		int size = getSize();
		World.addGroundItem(item, new WorldTile(getCoordFaceX(size), getCoordFaceY(size), getPlane()), player, true, 90);
		if (sendRareMessage && ItemConstants.isRare(item)) {
			World.sendWorldMessage("<col=" + ChatColors.MAROON + "><img=6>Loot</col>: " + player.getDisplayName() + " has just received " + (item.getAmount() > 1 ? item.getAmount() + "x " : "") + item.getName().toLowerCase() + " from a " + getName().toLowerCase() + ".", false);
		}
		if (item.getId() == 4151) {
			AchievementHandler.incrementProgress(player, Whip_Hunter.class, 1);
		}
	}

	public int getMaxHit() {
		return getCombatDefinitions().getMaxHit();
	}

	public NPCCombatDefinitions getCombatDefinitions() {
		if (combatDefinitions == null) {
			combatDefinitions = NPCDataLoader.getCombatDefinitions(getId());
		}
		return combatDefinitions;
	}

	public void setCombatDefinitions(NPCCombatDefinitions definitions) {
		this.combatDefinitions = definitions;
	}

	public int[] getBonuses() {
		if (bonuses == null) {
			bonuses = NPCDataLoader.getBonuses(id);
		}
		return bonuses;
	}

	/**
	 * Sets the npcs bonuses
	 *
	 * @param bonuses
	 * 		The bonuses to set them to
	 */
	public void setBonuses(int[] bonuses) {
		this.bonuses = bonuses;
	}

	public WorldTile getRespawnTile() {
		return respawnTile;
	}

	public boolean isUnderCombat() {
		return combat.underCombat();
	}

	public boolean canBeAttackedByAutoRelatie() {
		return Utils.currentTimeMillis() - lastAttackedByTarget > lureDelay;
	}

	public boolean isForceWalking() {
		return forceWalk != null;
	}

	public void setTarget(Entity entity) {
		if (isForceWalking()) // if force walk not gonna get target
		{
			return;
		}
		combat.setTarget(entity);
		lastAttackedByTarget = Utils.currentTimeMillis();
	}

	public void removeTarget() {
		if (combat.getTarget() == null) {
			return;
		}
		combat.removeTarget();
	}

	public boolean walksToRespawnTile() {
		return true;
	}

	public void forceWalkRespawnTile() {
		setForceWalk(respawnTile);
	}

	public void setForceWalk(WorldTile tile) {
		resetWalkSteps();
		forceWalk = tile;
	}

	public boolean hasForceWalk() {
		return forceWalk != null;
	}

	public ArrayList<Entity> getPossibleTargets(boolean checkNPCs, boolean checkPlayers) {
		int size = getSize();
		int agroRatio = getCombatDefinitions().getAggressivenessType();
		ArrayList<Entity> possibleTarget = new ArrayList<>();
		for (int regionId : getMapRegionsIds()) {
			if (checkPlayers) {
				List<Integer> playerIndexes = World.getRegion(regionId).getPlayerIndexes();
				if (playerIndexes != null) {
					/*
					 * if (possiblePlayerTargets == playerIndexes.size()) {
					 * continue; } possiblePlayerTargets = 0;
					 */
					for (int playerIndex : playerIndexes) {
						Player player = World.getPlayers().get(playerIndex);
						if (player == null || player.isDead() || player.hasFinished() || !player.isRunning() || player.getAppearence().isHidden() || !Utils.isInRange(getX(), getY(), size, player.getX(), player.getY(), player.getSize(), forceTargetDistance > 0 ? forceTargetDistance : agroRatio) || (!forceMultiAttacked && (!isAtMultiArea() || !player.isAtMultiArea()) && (player.getAttackedBy() != this && (player.getAttackedByDelay() > Utils.currentTimeMillis() || player.getFindTargetDelay() > Utils.currentTimeMillis()))) || !clipedProjectile(player, false) || (!forceAggressive && !Wilderness.isAtWild(this) && player.getSkills().getCombatLevelWithSummoning() >= getCombatLevel() * 2)) {
//							System.out.println("Skipped " + player + " from being aggressive. " + (isAtMultiArea()));
							continue;
						}
						if (getRegion().getTimeSpent(player) > getRegionDeaggressionTicks()) {
//							System.out.println("Was not aggressive to " + player + " b/c they spent " + getRegion().getTimeSpent(player) + " in my region [" + getRegionId() + "]");
							continue;
						}
						possibleTarget.add(player);
						// possiblePlayerTargets++;
					}
				}
			}
			if (checkNPCs) {
				List<Integer> npcsIndexes = World.getRegion(regionId).getNPCsIndexes();
				if (npcsIndexes != null) {
					/*
					 * if (possibleNPCTargets == npcsIndexes.size()) continue;
					 * possibleNPCTargets = 0;
					 */
					for (int npcIndex : npcsIndexes) {
						NPC npc = World.getNPCs().get(npcIndex);
						if (npc == null || npc == this || npc.isDead() || npc.hasFinished() || !Utils.isInRange(getX(), getY(), size, npc.getX(), npc.getY(), npc.getSize(), forceTargetDistance > 0 ? forceTargetDistance : agroRatio) || !npc.getDefinitions().hasAttackOption() || ((!isAtMultiArea() || !npc.isAtMultiArea()) && npc.getAttackedBy() != this && npc.getAttackedByDelay() > Utils.currentTimeMillis()) || !clipedProjectile(npc, false)) {
							continue;
						}
						possibleTarget.add(npc);
						// possibleNPCTargets++;
					}
				}
			}
		}
		return possibleTarget;
	}

	private int getRegionDeaggressionTicks() {
		switch (getId()) {
			default:
				return (int) TimeUnit.MINUTES.toMillis(10) / 600;
		}
	}

	public ArrayList<Entity> getPossibleTargets() {
		return getPossibleTargets(false, true);
	}

	public boolean checkAggressivity() {
		if (!forceAggressive) {
			NPCCombatDefinitions defs = getCombatDefinitions();
			if (defs.getAggressivenessType() == NPCCombatDefinitions.PASSIVE) {
				return false;
			}
		}
		ArrayList<Entity> possibleTarget = getPossibleTargets();
		if (!possibleTarget.isEmpty()) {
			Entity target = possibleTarget.get(Utils.random(possibleTarget.size()));
			setTarget(target);
			target.setAttackedBy(target);
			target.setFindTargetDelay(Utils.currentTimeMillis() + 10000);
			return true;
		}
		return false;
	}

	public boolean isCantInteract() {
		return cantInteract;
	}

	public void setCantInteract(boolean cantInteract) {
		this.cantInteract = cantInteract;
		if (cantInteract) {
			combat.reset();
		}
	}

	public int getCapDamage() {
		return capDamage;
	}

	public void setCapDamage(int capDamage) {
		this.capDamage = capDamage;
	}

	public int getLureDelay() {
		return lureDelay;
	}

	public void setLureDelay(int lureDelay) {
		this.lureDelay = lureDelay;
	}

	public boolean isCantFollowUnderCombat() {
		return cantFollowUnderCombat;
	}

	public void setCantFollowUnderCombat(boolean canFollowUnderCombat) {
		this.cantFollowUnderCombat = canFollowUnderCombat;
	}

	public Transformation getNextTransformation() {
		return nextTransformation;
	}

	public boolean isForceAggressive() {
		return forceAggressive;
	}

	public void setForceAgressive(boolean forceAgressive) {
		this.forceAggressive = forceAgressive;
	}

	public int getForceTargetDistance() {
		return forceTargetDistance;
	}

	public void setForceTargetDistance(int forceTargetDistance) {
		this.forceTargetDistance = forceTargetDistance;
	}

	public boolean isForceFollowClose() {
		return forceFollowClose;
	}

	public void setForceFollowClose(boolean forceFollowClose) {
		this.forceFollowClose = forceFollowClose;
	}

	public boolean isForceMultiAttacked() {
		return forceMultiAttacked;
	}

	public void setForceMultiAttacked(boolean forceMultiAttacked) {
		this.forceMultiAttacked = forceMultiAttacked;
	}

	public String getCustomName() {
		return name;
	}

	public int getCustomCombatLevel() {
		return combatLevel;
	}

	public String getName() {
		return name != null ? name : getDefinitions().getName();
	}

	public void setName(String string) {
		this.name = getDefinitions().getName().equals(string) ? null : string;
		setHasChangedName(true);
	}

	public boolean hasChangedCombatLevel() {
		return changedCombatLevel;
	}

	public WorldTile getMiddleWorldTile() {
		int size = getSize();
		return new WorldTile(getCoordFaceX(size), getCoordFaceY(size), getPlane());
	}

	public boolean isSpawned() {
		return spawned;
	}

	public void setSpawned(boolean spawned) {
		this.spawned = spawned;
	}

	public boolean isNoDistanceCheck() {
		return noDistanceCheck;
	}

	public void setNoDistanceCheck(boolean noDistanceCheck) {
		this.noDistanceCheck = noDistanceCheck;
	}

	public boolean withinDistance(Player tile, int distance) {
		return super.withinDistance(tile, distance);
	}

	/**
	 * Gets the locked.
	 *
	 * @return The locked.
	 */
	public boolean isLocked() {
		return locked;
	}

	/**
	 * Sets the locked.
	 *
	 * @param locked
	 * 		The locked to set.
	 */
	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	/**
	 * @return the walkType
	 */
	public int getWalkType() {
		return walkType;
	}

	/**
	 * @param walkType
	 * 		the walkType to set
	 */
	public void setWalkType(int walkType) {
		this.walkType = walkType;
	}

	/**
	 * @return the faceDirection
	 */
	public Direction getFaceDirection() {
		if (faceDirection == null) {
			faceDirection = Direction.NORTH;
		}
		return faceDirection;
	}

	/**
	 * @param faceDirection
	 * 		the faceDirection to set
	 */
	public void setFaceDirection(Direction faceDirection) {
		this.faceDirection = faceDirection;
	}

	public boolean isIntelligentRouteFinder() {
		return intelligentRouteFinder;
	}

	public void setIntelligentRouteFinder(boolean intelligentRouteFinder) {
		this.intelligentRouteFinder = intelligentRouteFinder;
	}

}
