package com.runescape.game.world.entity;

import com.runescape.cache.loaders.AnimationDefinitions;
import com.runescape.cache.loaders.ObjectDefinitions;
import com.runescape.game.GameConstants;
import com.runescape.game.content.Poison;
import com.runescape.game.content.skills.magic.Magic;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.*;
import com.runescape.game.world.entity.masks.Hit.HitLook;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.familiar.impl.Familiar;
import com.runescape.game.world.entity.npc.qbd.TorturedSoul;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.game.world.region.DynamicRegion;
import com.runescape.game.world.region.Region;
import com.runescape.game.world.route.RouteFinder;
import com.runescape.game.world.route.strategy.EntityStrategy;
import com.runescape.game.world.route.strategy.FixedTileStrategy;
import com.runescape.game.world.route.strategy.ObjectStrategy;
import com.runescape.utility.Utils;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Entity extends WorldTile {

	private static final long serialVersionUID = -3372926325008880753L;

	private final static AtomicInteger hashCodeGenerator = new AtomicInteger();

	public abstract void finish();

	public abstract double getMagePrayerMultiplier();

	public abstract double getRangePrayerMultiplier();

	public abstract double getMeleePrayerMultiplier();

	protected Poison poison;

	// saving stuff
	private int hitpoints;

	private int mapSize; // default 0, can be setted other value usefull on

	// static maps
	private boolean run;

	// transient stuff
	private transient int index;

	private transient int lastRegionId; // the last region the entity was at

	private transient WorldTile lastLoadedMapRegionTile;

	private transient CopyOnWriteArrayList<Integer> mapRegionsIds; // called by

	// more than
	// 1thread
	// so
	// concurent
	private transient int direction;

	private transient WorldTile lastWorldTile;

	private transient WorldTile nextWorldTile;

	private transient int nextWalkDirection;

	private transient int nextRunDirection;

	private transient WorldTile nextFaceWorldTile;

	private transient boolean teleported;

	private transient ConcurrentLinkedQueue<int[]> walkSteps;// called by more

	// than 1thread
	// so concurent
	private transient ConcurrentLinkedQueue<Hit> receivedHits;

	private transient Map<Entity, Integer> receivedDamage;

	private transient boolean finished; // if removed

	private transient long freezeDelay;

	// entity masks
	private transient Animation nextAnimation;

	private transient Animation lastAnimation;

	private transient Graphics nextGraphics1;

	private transient Graphics nextGraphics2;

	private transient Graphics nextGraphics3;

	private transient Graphics nextGraphics4;

	private transient ArrayList<Hit> nextHits;

	private transient ForceMovement nextForceMovement;

	private transient ForceTalk nextForceTalk;

	private transient int nextFaceEntity;

	private transient int lastFaceEntity;

	private transient Entity attackedBy; // whos attacking you, used for single

	private transient long attackedByDelay; // delay till someone else can

	// attack you
	private transient boolean multiArea;

	private transient boolean isAtDynamicRegion;

	private transient long lastAnimationEnd;

	private transient boolean forceMultiArea;

	private transient long frozenBlocked;

	private transient long findTargetDelay;

	private transient ConcurrentHashMap<Object, Object> temporaryAttributes;

	private transient int hashCode;

	private transient long lastTimeHit;

	@Override
	public int hashCode() {
		return hashCode;
	}

	// creates Entity and saved classes
	public Entity(WorldTile tile) {
		super(tile);
		poison = new Poison();
	}

	private static boolean colides(int x1, int y1, int size1, int x2, int y2, int size2) {
		for (int checkX1 = x1; checkX1 < x1 + size1; checkX1++) {
			for (int checkY1 = y1; checkY1 < y1 + size1; checkY1++) {
				for (int checkX2 = x2; checkX2 < x2 + size2; checkX2++) {
					for (int checkY2 = y2; checkY2 < y2 + size2; checkY2++) {
						if (checkX1 == checkX2 && checkY1 == checkY2) {
							return true;
						}
					}

				}
			}
		}
		return false;
	}

	public static boolean findBasicRoute(Entity src, WorldTile dest, int maxStepsCount, boolean calculate) {
		int[] srcPos = src.getLastWalkTile();
		int[] destPos = { dest.getX(), dest.getY() };
		int srcSize = src.getSize();
		// set destSize to 0 to walk under it else follows
		int destSize = dest instanceof Entity ? ((Entity) dest).getSize() : 1;
		int[] destScenePos = { destPos[0] + destSize - 1, destPos[1] + destSize - 1 };
		while (maxStepsCount-- != 0) {
			int[] srcScenePos = { srcPos[0] + srcSize - 1, srcPos[1] + srcSize - 1 };
			if (!Utils.isInRange(srcPos[0], srcPos[1], srcSize, destPos[0], destPos[1], destSize, 0)) {
				if (srcScenePos[0] < destScenePos[0] && srcScenePos[1] < destScenePos[1] && (!(src instanceof NPC) || src.canWalkNPC(srcPos[0] + 1, srcPos[1] + 1)) && src.addWalkStep(srcPos[0] + 1, srcPos[1] + 1, srcPos[0], srcPos[1], true)) {
					srcPos[0]++;
					srcPos[1]++;
					continue;
				}
				if (srcScenePos[0] > destScenePos[0] && srcScenePos[1] > destScenePos[1] && (!(src instanceof NPC) || src.canWalkNPC(srcPos[0] - 1, srcPos[1] - 1)) && src.addWalkStep(srcPos[0] - 1, srcPos[1] - 1, srcPos[0], srcPos[1], true)) {
					srcPos[0]--;
					srcPos[1]--;
					continue;
				}
				if (srcScenePos[0] < destScenePos[0] && srcScenePos[1] > destScenePos[1] && (!(src instanceof NPC) || src.canWalkNPC(srcPos[0] + 1, srcPos[1] - 1)) && src.addWalkStep(srcPos[0] + 1, srcPos[1] - 1, srcPos[0], srcPos[1], true)) {
					srcPos[0]++;
					srcPos[1]--;
					continue;
				}
				if (srcScenePos[0] > destScenePos[0] && srcScenePos[1] < destScenePos[1] && (!(src instanceof NPC) || src.canWalkNPC(srcPos[0] - 1, srcPos[1] + 1)) && src.addWalkStep(srcPos[0] - 1, srcPos[1] + 1, srcPos[0], srcPos[1], true)) {
					srcPos[0]--;
					srcPos[1]++;
					continue;
				}
				if (srcScenePos[0] < destScenePos[0] && (!(src instanceof NPC) || src.canWalkNPC(srcPos[0] + 1, srcPos[1])) && src.addWalkStep(srcPos[0] + 1, srcPos[1], srcPos[0], srcPos[1], true)) {
					srcPos[0]++;
					continue;
				}
				if (srcScenePos[0] > destScenePos[0] && (!(src instanceof NPC) || src.canWalkNPC(srcPos[0] - 1, srcPos[1])) && src.addWalkStep(srcPos[0] - 1, srcPos[1], srcPos[0], srcPos[1], true)) {
					srcPos[0]--;
					continue;
				}
				if (srcScenePos[1] < destScenePos[1] && (!(src instanceof NPC) || src.canWalkNPC(srcPos[0], srcPos[1] + 1)) && src.addWalkStep(srcPos[0], srcPos[1] + 1, srcPos[0], srcPos[1], true)) {
					srcPos[1]++;
					continue;
				}
				if (srcScenePos[1] > destScenePos[1] && (!(src instanceof NPC) || src.canWalkNPC(srcPos[0], srcPos[1] - 1)) && src.addWalkStep(srcPos[0], srcPos[1] - 1, srcPos[0], srcPos[1], true)) {
					srcPos[1]--;
					continue;
				}
				return false;
			}
			break; // for now nothing between break and return
		}
		return true;
	}

	public boolean inArea(int a, int b, int c, int d) {
		return getX() >= a && getY() >= b && getX() <= c && getY() <= d;
	}

	public final void initEntity() {
		hashCode = hashCodeGenerator.getAndIncrement();
		mapRegionsIds = new CopyOnWriteArrayList<>();
		walkSteps = new ConcurrentLinkedQueue<>();
		receivedHits = new ConcurrentLinkedQueue<>();
		receivedDamage = new ConcurrentHashMap<>();
		temporaryAttributes = new ConcurrentHashMap<>();
		nextHits = new ArrayList<>();
		nextWalkDirection = nextRunDirection - 1;
		lastFaceEntity = -1;
		nextFaceEntity = -2;
		poison.setEntity(this);
	}

	public Map<Entity, Integer> getReceivedDamage() {
		return receivedDamage;
	}

	public void reset() {
		reset(true);
	}

	public void reset(boolean attributes) {
		setHitpoints(getMaxHitpoints());
		receivedHits.clear();
		resetCombat();
		walkSteps.clear();
		poison.reset();
		resetReceivedDamage();
		if (attributes) {
			temporaryAttributes.clear();
		}
	}

	public abstract int getMaxHitpoints();

	public void resetCombat() {
		attackedBy = null;
		attackedByDelay = 0;
		freezeDelay = 0;
	}

	public void resetReceivedDamage() {
		receivedDamage.clear();
	}

	public void removeDamage(Entity entity) {
		receivedDamage.remove(entity);
	}

	public Player getMostDamageReceivedSourcePlayer() {
		Player player = null;
		int damage = -1;
		Iterator<Entry<Entity, Integer>> it$ = receivedDamage.entrySet().iterator();
		while (it$.hasNext()) {
			Entry<Entity, Integer> entry = it$.next();
			Entity source = entry.getKey();
			if (!source.isPlayer()) {
				continue;
			}
			Integer d = entry.getValue();
			if (d == null || source.hasFinished()) {
				receivedDamage.remove(source);
				continue;
			}
			if (d > damage) {
				player = (Player) source;
				damage = d;
			}
		}
		return player;
	}

	public boolean isPlayer() {
		return false;
	}

	public boolean hasFinished() {
		return finished;
	}

	public boolean hasWalkSteps() {
		return !walkSteps.isEmpty();
	}

	public boolean addWalkSteps(int destX, int destY) {
		return addWalkSteps(destX, destY, -1);
	}

	/*
	 * return added all steps
	 */
	public boolean addWalkSteps(final int destX, final int destY, int maxStepsCount) {
		return addWalkSteps(destX, destY, -1, true);
	}

	/*
	 * return added all steps
	 */
	public boolean addWalkSteps(final int destX, final int destY, int maxStepsCount, boolean check) {
		int[] lastTile = getLastWalkTile();
		int myX = lastTile[0];
		int myY = lastTile[1];
		int stepCount = 0;
		do {
			stepCount++;
			if (myX < destX) { myX++; } else if (myX > destX) { myX--; }
			if (myY < destY) { myY++; } else if (myY > destY) { myY--; }
			if (!addWalkStep(myX, myY, lastTile[0], lastTile[1], check)) { return false; }
			if (stepCount == maxStepsCount) { return true; }
			lastTile[0] = myX;
			lastTile[1] = myY;
		} while (lastTile[0] != destX || lastTile[1] != destY);
		return true;
	}

	public int[] getLastWalkTile() {
		Object[] objects = walkSteps.toArray();
		if (objects.length == 0) {
			return new int[] { getX(), getY() };
		}
		int step[] = (int[]) objects[objects.length - 1];
		return new int[] { step[1], step[2] };
	}

	// return cliped step
	public boolean addWalkStep(int nextX, int nextY, int lastX, int lastY, boolean check) {
		int dir = Utils.getMoveDirection(nextX - lastX, nextY - lastY);
		if (dir == -1) {
			return false;
		}
		if (check) {
			if (!World.checkWalkStep(getPlane(), lastX, lastY, dir, getSize())) {
				return false;
			}
			if (isPlayer()) {
				if (!((Player) this).getControllerManager().checkWalkStep(lastX, lastY, nextX, nextY)) {
					return false;
				}
			}
		}
		walkSteps.add(new int[] { dir, nextX, nextY });
		return true;
	}

	public abstract int getSize();

	/*
	 * returns if cliped
	 */
	public boolean clipedProjectile(WorldTile tile, boolean checkClose) {
		if (tile instanceof NPC) {
			NPC n = (NPC) tile;
			if (isPlayer()) {
				return n.clipedProjectile(this, checkClose);
			}
			tile = n.getMiddleWorldTile();
		} else if (tile instanceof Player && isPlayer()) {
			Player p = (Player) tile;
			return clipedProjectile(tile, checkClose, 1) || p.clipedProjectile(this, checkClose, 1);
		}
		return clipedProjectile(tile, checkClose, 1);
	}

	/*
	 * return added all steps
	 */
	public boolean checkWalkStepsInteract(int fromX, int fromY, final int destX, final int destY, int maxStepsCount, int size, boolean calculate) {
		int[] lastTile = new int[] { fromX, fromY };
		int myX = lastTile[0];
		int myY = lastTile[1];
		int stepCount = 0;
		while (true) {
			stepCount++;
			int myRealX = myX;
			int myRealY = myY;

			if (myX < destX) {
				myX++;
			} else if (myX > destX) {
				myX--;
			}
			if (myY < destY) {
				myY++;
			} else if (myY > destY) {
				myY--;
			}
			if (!checkWalkStep(myX, myY, lastTile[0], lastTile[1], true)) {
				if (!calculate) {
					return false;
				}
				myX = myRealX;
				myY = myRealY;
				int[] myT = checkcalculatedStep(myRealX, myRealY, destX, destY, lastTile[0], lastTile[1], size);
				if (myT == null) {
					return false;
				}
				myX = myT[0];
				myY = myT[1];
			}
			int distanceX = myX - destX;
			int distanceY = myY - destY;
			if (!(distanceX > size || distanceX < -1 || distanceY > size || distanceY < -1)) {
				return true;
			}
			if (stepCount == maxStepsCount) {
				return true;
			}
			lastTile[0] = myX;
			lastTile[1] = myY;
			if (lastTile[0] == destX && lastTile[1] == destY) {
				return true;
			}
		}
	}

	// return cliped step
	public boolean checkWalkStep(int nextX, int nextY, int lastX, int lastY, boolean check) {
		int dir = Utils.getMoveDirection(nextX - lastX, nextY - lastY);
		if (dir == -1) {
			return false;
		}
		return !(check && !World.checkWalkStep(getPlane(), lastX, lastY, dir, getSize()));
	}

	public int[] checkcalculatedStep(int myX, int myY, int destX, int destY, int lastX, int lastY, int size) {
		if (myX < destX) {
			myX++;
			if (!checkWalkStep(myX, myY, lastX, lastY, true)) {
				myX--;
			} else if (!(myX - destX > size || myX - destX < -1 || myY - destY > size || myY - destY < -1)) {
				if (myX == lastX || myY == lastY) {
					return null;
				}
				return new int[] { myX, myY };
			}
		} else if (myX > destX) {
			myX--;
			if (!checkWalkStep(myX, myY, lastX, lastY, true)) {
				myX++;
			} else if (!(myX - destX > size || myX - destX < -1 || myY - destY > size || myY - destY < -1)) {
				if (myX == lastX || myY == lastY) {
					return null;
				}
				return new int[] { myX, myY };
			}
		}
		if (myY < destY) {
			myY++;
			if (!checkWalkStep(myX, myY, lastX, lastY, true)) {
				myY--;
			} else if (!(myX - destX > size || myX - destX < -1 || myY - destY > size || myY - destY < -1)) {
				if (myX == lastX || myY == lastY) {
					return null;
				}
				return new int[] { myX, myY };
			}
		} else if (myY > destY) {
			myY--;
			if (!checkWalkStep(myX, myY, lastX, lastY, true)) {
				myY++;
			} else if (!(myX - destX > size || myX - destX < -1 || myY - destY > size || myY - destY < -1)) {
				if (myX == lastX || myY == lastY) {
					return null;
				}
				return new int[] { myX, myY };
			}
		}
		if (myX == lastX || myY == lastY) {
			return null;
		}
		return new int[] { myX, myY };
	}

	/*
	 * returns if cliped
	 */
	public boolean clipedProjectile(WorldTile tile, boolean checkClose, int size) {
		int myX = getX();
		int myY = getY();
		if (isNPC() && size == 1) {
			NPC n = (NPC) this;
			WorldTile thist = n.getMiddleWorldTile();
			myX = thist.getX();
			myY = thist.getY();
		}
		int destX = tile.getX();
		int destY = tile.getY();
		int lastTileX = myX;
		int lastTileY = myY;
		while (true) {
			if (myX < destX) {
				myX++;
			} else if (myX > destX) {
				myX--;
			}
			if (myY < destY) {
				myY++;
			} else if (myY > destY) {
				myY--;
			}
			int dir = Utils.getMoveDirection(myX - lastTileX, myY - lastTileY);
			if (dir == -1) {
				return false;
			}
			if (checkClose) {
				if (!World.checkWalkStep(getPlane(), lastTileX, lastTileY, dir, size)) {
					return false;
				}
			} else if (!World.checkProjectileStep(getPlane(), lastTileX, lastTileY, dir, size)) {
				return false;
			}
			lastTileX = myX;
			lastTileY = myY;
			if (lastTileX == destX && lastTileY == destY) {
				return true;
			}
		}
	}

	public boolean addWalkStepsInteract(int destX, int destY, int maxStepsCount, int size, boolean calculate) {
		return addWalkStepsInteract(destX, destY, maxStepsCount, size, size, calculate);
	}

	public boolean canWalkNPC(int toX, int toY) {
		return canWalkNPC(toX, toY, false);
	}

	private int getPreviewNextWalkStep() {
		int step[] = walkSteps.poll();
		if (step == null) {
			return -1;
		}
		return step[0];
	}

	public boolean canWalkNPC(int toX, int toY, boolean checkUnder) {
		if (!isAtMultiArea() /*
							 * || (!checkUnder && !canWalkNPC(getX(), getY(),
							 * true))
							 */) {
			return true;
		}
		int size = getSize();
		for (int regionId : getMapRegionsIds()) {
			List<Integer> npcIndexes = World.getRegion(regionId).getNPCsIndexes();
			if (npcIndexes != null) {
				for (int npcIndex : npcIndexes) {
					NPC target = World.getNPCs().get(npcIndex);
					if (target == null || target == this || target.isDead() || target.hasFinished() || target.getPlane() != getPlane() || !target.isAtMultiArea() || (!(this instanceof Familiar) && target instanceof Familiar)) {
						continue;
					}
					int targetSize = target.getSize();
					if (!checkUnder && target.getNextWalkDirection() == -1) { // means
						// the
						// walk
						// hasnt
						// been
						// processed
						// yet
						int previewDir = getPreviewNextWalkStep();
						if (previewDir != -1) {
							WorldTile tile = target.transform(Utils.DIRECTION_DELTA_X[previewDir], Utils.DIRECTION_DELTA_Y[previewDir], 0);
							if (colides(tile.getX(), tile.getY(), targetSize, getX(), getY(), size)) {
								continue;
							}

							if (colides(tile.getX(), tile.getY(), targetSize, toX, toY, size)) {
								return false;
							}
						}
					}
					if (colides(target.getX(), target.getY(), targetSize, getX(), getY(), size)) {
						continue;
					}
					if (colides(target.getX(), target.getY(), targetSize, toX, toY, size)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/*
	 * return added all steps
	 */
	public boolean addWalkStepsInteract(final int destX, final int destY, int maxStepsCount, int sizeX, int sizeY, boolean calculate) {
		int[] lastTile = getLastWalkTile();
		int myX = lastTile[0];
		int myY = lastTile[1];
		int stepCount = 0;
		while (true) {
			stepCount++;
			int myRealX = myX;
			int myRealY = myY;

			if (myX < destX) {
				myX++;
			} else if (myX > destX) {
				myX--;
			}
			if (myY < destY) {
				myY++;
			} else if (myY > destY) {
				myY--;
			}
			if ((isNPC() && !canWalkNPC(myX, myY)) || !addWalkStep(myX, myY, lastTile[0], lastTile[1], true)) {
				if (!calculate) {
					return false;
				}
				myX = myRealX;
				myY = myRealY;
				int[] myT = calculatedStep(myRealX, myRealY, destX, destY, lastTile[0], lastTile[1], sizeX, sizeY);
				if (myT == null) {
					return false;
				}
				myX = myT[0];
				myY = myT[1];
			}
			int distanceX = myX - destX;
			int distanceY = myY - destY;
			if (!(distanceX > sizeX || distanceX < -1 || distanceY > sizeY || distanceY < -1)) {
				return true;
			}
			if (stepCount == maxStepsCount) {
				return true;
			}
			lastTile[0] = myX;
			lastTile[1] = myY;
			if (lastTile[0] == destX && lastTile[1] == destY) {
				return true;
			}
		}
	}

	public int[] calculatedStep(int myX, int myY, int destX, int destY, int lastX, int lastY, int sizeX, int sizeY) {
		if (myX < destX) {
			myX++;
			if ((isNPC() && !canWalkNPC(myX, myY)) || !addWalkStep(myX, myY, lastX, lastY, true)) {
				myX--;
			} else if (!(myX - destX > sizeX || myX - destX < -1 || myY - destY > sizeY || myY - destY < -1)) {
				if (myX == lastX || myY == lastY) {
					return null;
				}
				return new int[] { myX, myY };
			}
		} else if (myX > destX) {
			myX--;
			if ((isNPC() && !canWalkNPC(myX, myY)) || !addWalkStep(myX, myY, lastX, lastY, true)) {
				myX++;
			} else if (!(myX - destX > sizeX || myX - destX < -1 || myY - destY > sizeY || myY - destY < -1)) {
				if (myX == lastX || myY == lastY) {
					return null;
				}
				return new int[] { myX, myY };
			}
		}
		if (myY < destY) {
			myY++;
			if ((isNPC() && !canWalkNPC(myX, myY)) || !addWalkStep(myX, myY, lastX, lastY, true)) {
				myY--;
			} else if (!(myX - destX > sizeX || myX - destX < -1 || myY - destY > sizeY || myY - destY < -1)) {
				if (myX == lastX || myY == lastY) {
					return null;
				}
				return new int[] { myX, myY };
			}
		} else if (myY > destY) {
			myY--;
			if ((isNPC() && !canWalkNPC(myX, myY)) || !addWalkStep(myX, myY, lastX, lastY, true)) {
				myY++;
			} else if (!(myX - destX > sizeX || myX - destX < -1 || myY - destY > sizeY || myY - destY < -1)) {
				if (myX == lastX || myY == lastY) {
					return null;
				}
				return new int[] { myX, myY };
			}
		}
		if (myX == lastX || myY == lastY) {
			return null;
		}
		return new int[] { myX, myY };
	}

	public boolean addWalkSteps(WorldTile tile, int maxStepsCount) {
		return addWalkSteps(tile.getX(), tile.getY(), -1, true);
	}

	public ConcurrentLinkedQueue<int[]> getWalkSteps() {
		return walkSteps;
	}

	public boolean restoreHitPoints() {
		int maxHp = getMaxHitpoints();
		if (hitpoints > maxHp) {
			if (isPlayer()) {
				Player player = (Player) this;
				if (player.getPrayer().usingPrayer(1, 5) && Utils.getRandom(100) <= 15) {
					return false;
				}
			}
			setHitpoints(hitpoints - 1);
			return true;
		} else if (hitpoints < maxHp) {
			setHitpoints(hitpoints + 1);
			if (isPlayer()) {
				Player player = (Player) this;
				if (player.getPrayer().usingPrayer(0, 9) && hitpoints < maxHp) {
					setHitpoints(hitpoints + 1);
				} else if (player.getPrayer().usingPrayer(0, 26) && hitpoints < maxHp) {
					setHitpoints(hitpoints + (hitpoints + 4 > maxHp ? maxHp - hitpoints : 4));
				}

			}
			return true;
		}
		return false;
	}

	public boolean needMasksUpdate() {
		return nextFaceEntity != -2 || nextAnimation != null || nextGraphics1 != null || nextGraphics2 != null || nextGraphics3 != null || nextGraphics4 != null || (nextWalkDirection == -1 && nextFaceWorldTile != null) || !nextHits.isEmpty() || nextForceMovement != null || nextForceTalk != null;
	}

	public void resetMasks() {
		nextAnimation = null;
		nextGraphics1 = null;
		nextGraphics2 = null;
		nextGraphics3 = null;
		nextGraphics4 = null;
		if (nextWalkDirection == -1) {
			nextFaceWorldTile = null;
		}
		nextForceMovement = null;
		nextForceTalk = null;
		nextFaceEntity = -2;
		nextHits.clear();
	}

	public long processEntity() {
		poison.processPoison();
		processMovement();
		processReceivedHits();
		processReceivedDamage();
		return 0;
	}

	public void processMovement() {
		lastWorldTile = new WorldTile(this);
		if (lastFaceEntity >= 0) {
			Entity target = lastFaceEntity >= 32768 ? World.getPlayers().get(lastFaceEntity - 32768) : World.getNPCs().get(lastFaceEntity);
			if (target != null) {
				setDirection(Utils.getFaceDirection(target.getCoordFaceX(target.getSize()) - getX(), target.getCoordFaceY(target.getSize()) - getY()));
			}
		}
		nextWalkDirection = nextRunDirection = -1;
		if (nextWorldTile != null) {
			int lastPlane = getPlane();
			setLocation(nextWorldTile);
			nextWorldTile = null;
			teleported = true;
			if (isPlayer() && ((Player) this).getTemporaryMoveType() == -1) {
				((Player) this).setTemporaryMoveType(Player.TELE_MOVE_TYPE);
			}
			World.updateEntityRegion(this);
			if (needMapUpdate()) {
				loadMapRegions();
			} else if (isPlayer() && lastPlane != getPlane()) {
				((Player) this).setClientHasntLoadedMapRegion();
			}
			resetWalkSteps();
			return;
		}
		teleported = false;
		if (walkSteps.isEmpty()) {
			return;
		}
		if (isPlayer()) {
			if (((Player) this).getEmotesManager().getNextEmoteEnd() >= Utils.currentTimeMillis()) {
				return;
			}
		}
		if (this instanceof TorturedSoul) {
			if (((TorturedSoul) this).switchWalkStep()) {
				return;
			}
		}
		nextWalkDirection = getNextWalkStep();
		if (nextWalkDirection != -1) {
			if (isPlayer()) {
				if (!((Player) this).getControllerManager().canMove(nextWalkDirection)) {
					nextWalkDirection = -1;
					resetWalkSteps();
					return;
				}
			}
			moveLocation(Utils.DIRECTION_DELTA_X[nextWalkDirection], Utils.DIRECTION_DELTA_Y[nextWalkDirection], 0);
//			if (isPlayer()) { System.out.println(run); }
			if (run) {
				if (isPlayer() && ((Player) this).getEnergyValue() <= 0) {
					setRun(false);
				} else {
					nextRunDirection = getNextWalkStep();
					if (nextRunDirection != -1) {
						if (isPlayer()) {
							Player player = (Player) this;
							if (!player.getControllerManager().canMove(nextRunDirection)) {
								nextRunDirection = -1;
								resetWalkSteps();
								return;
							}
							player.drainRunEnergy();
						}
						moveLocation(Utils.DIRECTION_DELTA_X[nextRunDirection], Utils.DIRECTION_DELTA_Y[nextRunDirection], 0);
					} else if (isPlayer()) {
						((Player) this).setTemporaryMoveType(Player.WALK_MOVE_TYPE);
					}
				}
			}
		}
		World.updateEntityRegion(this);
		if (needMapUpdate()) {
			loadMapRegions();
		}
	}

	public void processReceivedHits() {
		if (isPlayer()) {
			if (((Player) this).getEmotesManager().getNextEmoteEnd() >= Utils.currentTimeMillis()) {
				return;
			}
		}
		Hit hit;
		int count = 0;
		while ((hit = receivedHits.poll()) != null && count++ < 10) {
			processHit(hit);
		}
	}

	public void processReceivedDamage() {
		for (Entity source : receivedDamage.keySet()) {
			Integer damage = receivedDamage.get(source);
			if (damage == null || source.hasFinished()) {
				receivedDamage.remove(source);
				continue;
			}
			damage--;
			if (damage == 0) {
				receivedDamage.remove(source);
				continue;
			}
			receivedDamage.put(source, damage);
		}
	}

	private boolean needMapUpdate() {
		int lastMapRegionX = lastLoadedMapRegionTile.getChunkX();
		int lastMapRegionY = lastLoadedMapRegionTile.getChunkY();
		int regionX = getChunkX();
		int regionY = getChunkY();
		int size = ((GameConstants.MAP_SIZES[mapSize] >> 3) / 2) - 1;
		return Math.abs(lastMapRegionX - regionX) >= size || Math.abs(lastMapRegionY - regionY) >= size;
	}

	public void loadMapRegions() {
		mapRegionsIds.clear();
		isAtDynamicRegion = false;
		int chunkX = getChunkX();
		int chunkY = getChunkY();
		int mapHash = GameConstants.MAP_SIZES[mapSize] >> 4;
		int minRegionX = (chunkX - mapHash) / 8;
		int minRegionY = (chunkY - mapHash) / 8;
		for (int xCalc = minRegionX < 0 ? 0 : minRegionX; xCalc <= ((chunkX + mapHash) / 8); xCalc++) {
			for (int yCalc = minRegionY < 0 ? 0 : minRegionY; yCalc <= ((chunkY + mapHash) / 8); yCalc++) {
				int regionId = yCalc + (xCalc << 8);
				if (World.getRegion(regionId, isPlayer()) instanceof DynamicRegion) {
					isAtDynamicRegion = true;
				}
				mapRegionsIds.add(regionId);
			}
		}
		lastLoadedMapRegionTile = new WorldTile(this);
	}

	public void resetWalkSteps() {
		walkSteps.clear();
	}

	private int getNextWalkStep() {
		int step[] = walkSteps.poll();
		if (step == null) {
			return -1;
		}
		return step[0];
	}

	@Override
	public void moveLocation(int xOffset, int yOffset, int planeOffset) {
		super.moveLocation(xOffset, yOffset, planeOffset);
		setDirection(Utils.getFaceDirection(xOffset, yOffset));
	}

	private void processHit(Hit hit) {
		if (isDead()) {
			return;
		}
		removeHitpoints(hit);
		nextHits.add(hit);
	}

	public boolean isDead() {
		return hitpoints <= 0;
	}

	public void removeHitpoints(Hit hit) {
		if (isDead() || hit.getLook() == HitLook.ABSORB_DAMAGE) {
			return;
		}
		if (hit.getLook() == HitLook.HEALED_DAMAGE) {
			heal(hit.getDamage());
			return;
		}
		if (hit.getDamage() > hitpoints) {
			hit.setDamage(hitpoints);
		}
		addReceivedDamage(hit.getSource(), hit.getDamage());
		setHitpoints(hitpoints - hit.getDamage());
		if (hitpoints <= 0) {
			sendDeath(hit.getSource());
		} else if (isPlayer()) {
			Player player = (Player) this;
			if (player.getEquipment().getRingId() == 2550) {
				if (hit.getSource() != null && hit.getSource() != player && hit.getDamage() > 0 && !hit.getLook().equals(HitLook.REFLECTED_DAMAGE)) {
					WorldTasksManager.schedule(new WorldTask() {
						@Override
						public void run() {
							hit.getSource().applyHit(new Hit(player, (int) (hit.getDamage() * 0.1), HitLook.REFLECTED_DAMAGE));
						}
					});
				}
			}
			if (player.getPrayer().hasPrayersOn()) {
				if ((hitpoints < player.getMaxHitpoints() * 0.1) && player.getPrayer().usingPrayer(0, 23)) {
					setNextGraphics(new Graphics(436));
					setHitpoints((int) (hitpoints + player.getSkills().getLevelForXp(Skills.PRAYER) * 2.5));
					player.getSkills().setLevel(Skills.PRAYER, 0);
					player.getPrayer().setPrayerpoints(0);
				} else if (player.getEquipment().getAmuletId() != 11090 && player.getEquipment().getRingId() == 11090 && player.getHitpoints() <= player.getMaxHitpoints() * 0.1) {
					Magic.sendNormalTeleportSpell(player, 1, 0, GameConstants.RESPAWN_PLAYER_LOCATION);
					player.getEquipment().deleteItem(11090, 1);
					player.getPackets().sendGameMessage("Your ring of life saves you, but is destroyed in the process.");
				}
			}
			if (player.getEquipment().getAmuletId() == 11090 && player.getHitpoints() <= player.getMaxHitpoints() * 0.2) {// priority
				// over
				// ring
				// of
				// life
				player.heal((int) (player.getMaxHitpoints() * 0.3));
				player.getEquipment().deleteItem(11090, 1);
				player.getPackets().sendGameMessage("Your pheonix necklace heals you, but is destroyed in the process.");
			}
		}
	}

	public void heal(int ammount) {
		heal(ammount, 0);
	}

	public void addReceivedDamage(Entity source, int amount) {
		if (source == null) {
			return;
		}
		if (source instanceof Familiar) {
			Familiar familiar = (Familiar) source;
			source = familiar.getOwner();
		}
		Integer damage = receivedDamage.get(source);
		damage = damage == null ? amount : damage + amount;
		if (damage < 0) {
			receivedDamage.remove(source);
		} else {
			receivedDamage.put(source, damage);
		}
	}

	public abstract void sendDeath(Entity source);

	public void applyHit(Hit hit) {
		if (isDead()) {
			return;
		}
		if (isPlayer() && getAttribute("teleporting", false)) {
			resetReceivedDamage();
			return;
		}
		// used for kill times
		if (isNPC() && getAttribute("first_hit_time") == null) {
			putAttribute("first_hit_time", System.currentTimeMillis());
		}
		receivedHits.add(hit); // added hit first because, soaking added after,
		// if applyhit used right there shouldnt be any
		// problem
		handleIngoingHit(hit);
	}

	public void applyDelayedHit(final Hit hit, int delay) {
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				applyHit(hit);
			}
		}, delay);
	}

	public void setNextGraphics(Graphics nextGraphics) {
		if (nextGraphics == null) {
			if (nextGraphics4 != null) {
				nextGraphics4 = null;
			} else if (nextGraphics3 != null) {
				nextGraphics3 = null;
			} else if (nextGraphics2 != null) {
				nextGraphics2 = null;
			} else {
				nextGraphics1 = null;
			}
		} else {
			if (nextGraphics.equals(nextGraphics1) || nextGraphics.equals(nextGraphics2) || nextGraphics.equals(nextGraphics3) || nextGraphics.equals(nextGraphics4)) {
				return;
			}
			if (nextGraphics.getId() >= Utils.getGraphicDefinitionsSize()) {
				System.err.println(this + " was about to do a bad gfx( " + nextGraphics.getId() + ")");
				Thread.dumpStack();
				return;
			}
			if (nextGraphics1 == null) {
				nextGraphics1 = nextGraphics;
			} else if (nextGraphics2 == null) {
				nextGraphics2 = nextGraphics;
			} else if (nextGraphics3 == null) {
				nextGraphics3 = nextGraphics;
			} else {
				nextGraphics4 = nextGraphics;
			}
		}
	}

	public int getHitpoints() {
		return hitpoints;
	}

	public void heal(int ammount, int extra) {
		setHitpoints((hitpoints + ammount) >= (getMaxHitpoints() + extra) ? (getMaxHitpoints() + extra) : (hitpoints + ammount));
	}

	@SuppressWarnings("unchecked")
	public <K> K getAttribute(String key, K defaultValue) {
		K value = (K) getAttributes().get(key);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}

	public boolean isNPC() {
		return false;
	}

	@SuppressWarnings("unchecked")
	public <K> K getAttribute(String key) {
		return (K) getAttributes().get(key);
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
		getAttributes().put(key, value);
		return value;
	}

	public abstract void handleIngoingHit(Hit hit);

	public ConcurrentHashMap<Object, Object> getAttributes() {
		return temporaryAttributes;
	}

	public void setHitpoints(int hitpoints) {
		this.hitpoints = hitpoints;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getLastRegionId() {
		return lastRegionId;
	}

	public void setLastRegionId(int lastRegionId) {
		this.lastRegionId = lastRegionId;
	}

	public int getMapSize() {
		return mapSize;
	}

	public void setMapSize(int size) {
		this.mapSize = size;
		loadMapRegions();
	}

	public void setNextAnimationNoPriority(Animation nextAnimation) {
		if (lastAnimationEnd > Utils.currentTimeMillis()) {
			return;
		}
		for (int animId : nextAnimation.getIds()) {
			if (animId >= Utils.getAnimationDefinitionsSize()) {
				System.err.println(this + " was about to do a bad emote( " + animId + ")");
				Thread.dumpStack();
				return;
			}
		}
		setNextAnimation(nextAnimation);
	}

	public Animation getNextAnimation() {
		return nextAnimation;
	}

	public void setNextAnimation(Animation nextAnimation) {
		if (nextAnimation != null && nextAnimation.getIds()[0] >= 0) {
			for (int animId : nextAnimation.getIds()) {
				if (animId >= Utils.getAnimationDefinitionsSize()) {
					System.err.println(this + " was about to do a bad emote( " + animId + ")");
					Thread.dumpStack();
					return;
				}
			}
			lastAnimationEnd = Utils.currentTimeMillis() + AnimationDefinitions.getAnimationDefinitions(nextAnimation.getIds()[0]).getEmoteTime();
		}
		this.lastAnimation = this.nextAnimation = nextAnimation;
	}

	public Animation getLastAnimation() {
		return lastAnimation;
	}

	public Graphics getNextGraphics1() {
		return nextGraphics1;
	}

	public Graphics getNextGraphics2() {
		return nextGraphics2;
	}

	public Graphics getNextGraphics3() {
		return nextGraphics3;
	}

	public Graphics getNextGraphics4() {
		return nextGraphics4;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public WorldTile getNextWorldTile() {
		return nextWorldTile;
	}

	public void setNextWorldTile(WorldTile nextWorldTile) {
		this.nextWorldTile = nextWorldTile;
	}

	public boolean hasTeleported() {
		return teleported;
	}

	public WorldTile getLastLoadedMapRegionTile() {
		return lastLoadedMapRegionTile;
	}

	public int getNextWalkDirection() {
		return nextWalkDirection;
	}

	public int getNextRunDirection() {
		return nextRunDirection;
	}

	public boolean getRun() {
		return run;
	}

	public void setRun(boolean run) {
		this.run = run;
	}

	public WorldTile getNextFaceWorldTile() {
		return nextFaceWorldTile;
	}

	public void setNextFaceWorldTile(WorldTile nextFaceWorldTile) {
		this.nextFaceWorldTile = nextFaceWorldTile;
		if (nextWorldTile != null) {
			setDirection(Utils.getFaceDirection(nextFaceWorldTile.getX() - nextWorldTile.getX(), nextFaceWorldTile.getY() - nextWorldTile.getY()));
		} else {
			setDirection(Utils.getFaceDirection(nextFaceWorldTile.getX() - getX(), nextFaceWorldTile.getY() - getY()));
		}
	}

	public void cancelFaceEntityNoCheck() {
		nextFaceEntity = -2;
		lastFaceEntity = -1;
	}

	public int getNextFaceEntity() {
		return nextFaceEntity;
	}

	public void setNextFaceEntity(Entity entity) {
		if (entity == null) {
			nextFaceEntity = -1;
			lastFaceEntity = -1;
		} else {
			nextFaceEntity = entity.getClientIndex();
			lastFaceEntity = nextFaceEntity;
		}
	}

	public int getClientIndex() {
		return index + (isPlayer() ? 32768 : 0);
	}

	public long getFreezeDelay() {
		return freezeDelay; // 2500 delay
	}

	public void setFreezeDelay(int time) {
		this.freezeDelay = time;
	}

	public int getLastFaceEntity() {
		return lastFaceEntity;
	}

	public long getFrozenBlockedDelay() {
		return frozenBlocked;
	}

	public void setFrozeBlocked(int time) {
		this.frozenBlocked = time;
	}

	public void addFrozenBlockedDelay(int time) {
		frozenBlocked = time + Utils.currentTimeMillis();
	}

	public void addFreezeDelay(long time) {
		addFreezeDelay(time, false);
	}

	public void addFreezeDelay(long time, boolean entangleMessage, Entity... freezer) {
		long currentTime = Utils.currentTimeMillis();
		if (currentTime > freezeDelay) {
			resetWalkSteps();
			freezeDelay = time + currentTime;
			if (isPlayer()) {
				Player p = (Player) this;
				if (!entangleMessage) {
					p.getPackets().sendGameMessage("You have been frozen.");
				}
			}
		}
		if (freezer.length > 0 && freezer[0] != null) {
			putAttribute("frozen_by", freezer[0]);
		}
	}

	public Entity getAttackedBy() {
		return attackedBy;
	}

	public void setAttackedBy(Entity attackedBy) {
		this.attackedBy = attackedBy;
	}

	public long getAttackedByDelay() {
		return attackedByDelay;
	}

	public void setAttackedByDelay(long attackedByDelay) {
		this.attackedByDelay = attackedByDelay;
	}

	public boolean isAtMultiArea() {
		return multiArea;
	}

	public void setAtMultiArea(boolean multiArea) {
		this.multiArea = multiArea;
	}

	public boolean isAtDynamicRegion() {
		return isAtDynamicRegion;
	}

	public ForceMovement getNextForceMovement() {
		return nextForceMovement;
	}

	public void setNextForceMovement(ForceMovement nextForceMovement) {
		this.nextForceMovement = nextForceMovement;
	}

	public Poison getPoison() {
		return poison;
	}

	public ForceTalk getNextForceTalk() {
		return nextForceTalk;
	}

	public void setNextForceTalk(ForceTalk nextForceTalk) {
		this.nextForceTalk = nextForceTalk;
	}

	public void faceEntity(Entity target) {
		// npcs at home don't face anyone
		if (isNPC() && getRegionId() == 12342) {
			if (npc().getId() != 961 && npc().getId() != 1263 && npc().getId() != 1) { return; }
		}
		setNextFaceWorldTile(new WorldTile(target.getCoordFaceX(target.getSize()), target.getCoordFaceY(target.getSize()), target.getPlane()));
	}

	public NPC npc() {
		return null;
	}

	public void faceObject(WorldObject object) {
		ObjectDefinitions objectDef = object.getDefinitions();
		setNextFaceWorldTile(new WorldTile(object.getCoordFaceX(objectDef.getSizeX(), objectDef.getSizeY(), object.getRotation()), object.getCoordFaceY(objectDef.getSizeX(), objectDef.getSizeY(), object.getRotation()), object.getPlane()));
	}

	public long getLastAnimationEnd() {
		return lastAnimationEnd;
	}

	@SuppressWarnings("unchecked")
	public <K> K removeAttribute(String key) {
		K value = (K) getAttributes().remove(key);
		return value;
	}

	@SuppressWarnings("unchecked")
	public <K> K removeAttribute(String key, K defaultValue) {
		K value = (K) getAttributes().remove(key);
		if (value == null) {
			return defaultValue;
		}
		return value;
	}

	public boolean isForceMultiArea() {
		return forceMultiArea;
	}

	public void setForceMultiArea(boolean forceMultiArea) {
		this.forceMultiArea = forceMultiArea;
		checkMultiArea();
	}

	public void checkMultiArea() {
		multiArea = forceMultiArea || World.isMultiArea(this);
	}

	public WorldTile getLastWorldTile() {
		return lastWorldTile;
	}

	public ArrayList<Hit> getNextHits() {
		return nextHits;
	}

	public boolean canBeAttacked(Player player) {
		return true;
	}

	public void playSound(int soundId, int type) {
		for (int regionId : getMapRegionsIds()) {
			List<Integer> playerIndexes = World.getRegion(regionId).getPlayerIndexes();
			if (playerIndexes != null) {
				for (int playerIndex : playerIndexes) {
					Player player = World.getPlayers().get(playerIndex);
					if (player == null || !player.isRunning() || !withinDistance(player)) {
						continue;
					}
					player.getPackets().sendSound(soundId, 0, type);
				}
			}
		}
	}

	public CopyOnWriteArrayList<Integer> getMapRegionsIds() {
		return mapRegionsIds;
	}

	public long getFindTargetDelay() {
		return findTargetDelay;
	}

	public void setFindTargetDelay(long findTargetDelay) {
		this.findTargetDelay = findTargetDelay;
	}

	public boolean isFrozen() {
		return freezeDelay >= Utils.currentTimeMillis();
	}

	public Player player() {
		return null;
	}
	// save mem on normal path

	public boolean calcFollow(WorldTile target, boolean inteligent) {
		return calcFollow(target, -1, inteligent);
	} // used for normal npc follow int maxStepsCount, boolean calculate used to

	public boolean calcFollow(WorldTile target, int maxStepsCount, boolean inteligent) {
		if (inteligent) {
			int steps = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, getX(), getY(), getPlane(), getSize(), target instanceof WorldObject ? new ObjectStrategy((WorldObject) target) : new EntityStrategy((Entity) target), true);
			if (steps == -1) {
				return false;
			}
			if (steps == 0) {
				return true;
			}
			int[] bufferX = RouteFinder.getLastPathBufferX();
			int[] bufferY = RouteFinder.getLastPathBufferY();
			for (int step = steps - 1; step >= 0; step--) {
				if (!addWalkSteps(bufferX[step], bufferY[step], 25, true)) {
					break;
				}
			}
			return true;
		}
		return findBasicRoute(this, target, maxStepsCount, true);
	}

	public boolean calcFollow(WorldTile target, int maxStepsCount, boolean calculate, boolean inteligent) {
		if (inteligent) {
			int steps = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, getX(), getY(), getPlane(), getSize(), target instanceof WorldObject ? new ObjectStrategy((WorldObject) target) : target instanceof Entity ? new EntityStrategy((Entity) target) : new FixedTileStrategy(target.getX(), target.getY()), true);
			if (steps == -1) { return false; }
			if (steps == 0) { return true; }
			int[] bufferX = RouteFinder.getLastPathBufferX();
			int[] bufferY = RouteFinder.getLastPathBufferY();
			for (int step = steps - 1; step >= 0; step--) {
				if (!addWalkSteps(bufferX[step], bufferY[step], 25, true)) { break; }
			}
			return true;
		}
		return findBasicRoute(this, target, maxStepsCount, true);
	}

	public long getLastTimeHit() {
		return lastTimeHit;
	}

	public void setLastTimeHit(long lastTimeHit) {
		this.lastTimeHit = lastTimeHit;
	}

	public Region getRegion() {
		return World.getRegion(getRegionId());
	}

}
