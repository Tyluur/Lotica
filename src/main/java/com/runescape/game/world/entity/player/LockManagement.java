package com.runescape.game.world.entity.player;

/**
 * This class handles the locking of player actions
 *
 * @author Tyluur<itstyluur@gmail.com>
 * @since 9/30/15
 */
public class LockManagement {

	/**
	 * This array contains data about the time each {@link com.runescape.game.world.entity.player.LockManagement.LockType}
	 * is locked for
	 */
	private final long[] lockDelays = new long[LockType.values().length];

	/**
	 * This method locks several {@link com.runescape.game.world.entity.player.LockManagement.LockType}s for the given
	 * delay
	 *
	 * @param delay
	 * 		The amount of time (in milliseconds) that we should lock the actions
	 * @param types
	 * 		The type of actions to lock
	 */
	public void lockActions(long delay, LockType... types) {
		for (LockType type : types) {
			lockDelays[type.ordinal()] = System.currentTimeMillis() + delay;
		}
	}

	/**
	 * This method locks every {@link com.runescape.game.world.entity.player.LockManagement.LockType} indefinitely
	 *
	 * @param time
	 * 		The amount of time in milliseconds to lock all actions for. If this param is blank, we lock indefinitely
	 */
	public void lockAll(long... time) {
		for (LockType type : LockType.values()) {
			lockDelays[type.ordinal()] = time.length > 0 ? System.currentTimeMillis() + time[0] : Long.MAX_VALUE;
		}
	}

	public void debug() {
		for (LockType type : LockType.values()) {
			if (lockDelays[type.ordinal()] > 0) {
				System.out.println(type + "\t is locked until: " + lockDelays[type.ordinal()] + " ms...\t" + (lockDelays[type.ordinal()] - System.currentTimeMillis()) + " more ms");
			}
		}
	}

	/**
	 * This method unlocks every action
	 */
	public void unlockAll() {
		for (LockType type : LockType.values()) {
			lockDelays[type.ordinal()] = 0;
		}
	}

	/**
	 * Gets the lock delay for the lock type
	 *
	 * @param type
	 * 		The lock type
	 */
	public long getLockDelay(LockType type) {
		return lockDelays[type.ordinal()];
	}

	/**
	 * Sets a lock delay
	 *
	 * @param type
	 * 		The type
	 * @param delay
	 * 		The delay
	 */
	public void setLockDelay(LockType type, long delay) {
		lockDelays[type.ordinal()] = delay;
	}

	/**
	 * This method checks if there is any value in the {@link #lockDelays} array that has a time greater than the
	 * current time in millis. If any of the indexes aren't locked, we return false
	 *
	 * @return {@code true} if all indexes are locked
	 */
	public boolean isAllLocked() {
		for (long lockDelay : lockDelays) {
			if (lockDelay < System.currentTimeMillis()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * This method checks if there is any {@code #lockDelays} index that is locked
	 *
	 * @return {@code True} if there is any index locked
	 */
	public boolean isAnyLocked() {
		for (long lockDelay : lockDelays) {
			if (lockDelay > System.currentTimeMillis()) {
				return true;
			}
		}
		return false;
	}

	public String debugLogInformation() {
		String info = "";
		for (LockType type : LockType.values()) {
			if (isLocked(type)) {
				info += type + ":(" + lockDelays[type.ordinal()] + ")\n";
			}
		}
		if (info.equalsIgnoreCase("")) {
			info = "NONE";
		}
		return info;
	}

	/**
	 * Checks if a type of action is locked
	 *
	 * @param type
	 * 		The type of action
	 */
	public boolean isLocked(LockType type) {
		return lockDelays[type.ordinal()] > System.currentTimeMillis();
	}

	public enum LockType {
		WALKING,
		EMOTES,
		COMBAT,
		FOOD,
		BONE_BURIAL,
		POTIONS,
		DAMAGE,
		ACTION,
		INTERFACE_INTERACTION,
		PLAYER_INTERACTION,
		NPC_INTERACTION,
		OBJECT_INTERACTION,
		ITEM_INTERACTION,
	}
}