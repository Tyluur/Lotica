package com.runescape.game.content.global.cannon;

import com.runescape.game.world.World;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.Hit;
import com.runescape.game.world.entity.masks.Hit.HitLook;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.familiar.impl.Familiar;
import com.runescape.game.world.entity.player.OwnedObjectManager;
import com.runescape.game.world.entity.player.OwnedObjectManager.ProcessEvent;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.game.world.entity.player.achievements.AchievementHandler;
import com.runescape.game.world.entity.player.achievements.elite.Cannonballer;
import com.runescape.utility.Utils;

import java.io.Serializable;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since Nov 30, 2013
 */
public class DwarfCannon implements Serializable {

	/**
	 * A new {@code DwarfCannon} {@code Object}
	 *
	 * @param owner
	 * 		The owner of the dwarf cannon
	 * @param object
	 * 		The object
	 */
	public DwarfCannon(String owner, WorldObject object) {
		this.owner = owner;
		this.object = object;
		this.direction = CannonDirection.NORTH;
		OwnedObjectManager.addOwnedObjectManager(World.getPlayerByDisplayName(owner), new WorldObject[] { object }, new long[] { 0 }, new ProcessEvent() {

			@Override
			public void spawnObject(Player player, WorldObject object) {
			}

			@Override
			public void process(Player player, WorldObject currentObject) {
				tick(player, currentObject);
			}
		});
	}

	/**
	 * Handles the ticking of the current game object. The object is set to face the proper direction here, it will also
	 * attack the best target here as well.
	 *
	 * @param player
	 * 		The player who owns the object
	 * @param currentObject
	 * 		The dwarf cannon world object
	 */
	private void tick(Player player, final WorldObject currentObject) {
		final int endHeight = 38;
		final int startHeight = 50;
		final int speed = 30;
		final int delay = 0;
		final int curve = 10;

		int amountHit = 0;

		if (getBalls() <= 0) {
			if (isFiring()) {
				player.sendMessage("Your cannon has ran out of balls!");
				setFiring(false);
			}
			return;
		}

		setNextDirection();
		World.sendObjectAnimation(currentObject, new Animation(getDirection().getAnimation()));

		if (isFiring()) {
			for (NPC npc : CannonAlgorithms.getAffectedNPCS(player, currentObject, direction)) {
				if (amountHit > 3) {
					continue;
				}
				if (npc == null || npc == player.getFamiliar() || npc.isDead() || npc.hasFinished() || !npc.getDefinitions().hasAttackOption() || npc instanceof Familiar && !player.isAtMultiArea() || ((!player.isAtMultiArea() || !npc.isAtMultiArea()) && npc.getAttackedBy() != player && npc.getAttackedByDelay() > Utils.currentTimeMillis()) || !player.getControllerManager().canHit(npc)) {
					continue;
				}
				if (!player.getControllerManager().canAttack(npc)) {
					continue;
				}
				if (npc instanceof Familiar) {
					Familiar familiar = (Familiar) npc;
					if (familiar == player.getFamiliar() || !familiar.canAttack(player)) {
						continue;
					}
				} else if (!npc.isForceMultiAttacked()) {
					if (!npc.isAtMultiArea() || !player.isAtMultiArea()) {
						if ((player.getAttackedBy() != npc && player.getAttackedByDelay() > Utils.currentTimeMillis()) || (npc.getAttackedBy() != player && npc.getAttackedByDelay() > Utils.currentTimeMillis())) {
							continue;
						}
					}
				}
				if (!npc.clipedProjectile(currentObject, false)) {
					continue;
				}

				int damage = Utils.random(300);
				World.sendProjectile(currentObject, currentObject.getWorldTile(), npc.getWorldTile(), 53, startHeight, endHeight, speed, delay, curve, 0);

				npc.applyHit(new Hit(player, damage, HitLook.CANNON_DAMAGE));
				npc.setTarget(player);

				if (npc instanceof Familiar) {
					player.setWildernessSkull();
				}

				double combatXp = 10 * damage;
				if (combatXp > 0) {
					player.getSkills().addXpNoModifier(Skills.RANGE, combatXp);
					balls--;
				}
				AchievementHandler.incrementProgress(player, Cannonballer.class, 1);
				amountHit++;
			}
		}
	}

	/**
	 * Sets the next direction for the cannon to face
	 */
	private void setNextDirection() {
		direction = (direction.ordinal() == 8 ? CannonDirection.NORTH_EAST : CannonDirection.values()[direction.ordinal() + 1]);
	}

	/**
	 * The owner of the cannon
	 *
	 * @return A {@code String} {@code Object}
	 */
	public String getOwner() {
		return owner;
	}

	/**
	 * If the cannon is firing or not.
	 */
	public boolean isFiring() {
		return firing;
	}

	/**
	 * Sets the cannon to fire or to stop firing
	 *
	 * @param firing
	 * 		If it should fire
	 */
	public void setFiring(boolean firing) {
		this.firing = firing;
	}

	/**
	 * The amount of balls in the cannon
	 *
	 * @return A {@code Integer} {@code Object} with the amount of balls in the cannon
	 */
	public int getBalls() {
		return balls;
	}

	public void setBalls(int balls) {
		this.balls = balls;
	}

	public CannonDirection getDirection() {
		return direction;
	}

	public void setDirection(CannonDirection direction) {
		this.direction = direction;
	}

	/**
	 * This is what happens when the cannon is picked up or the player sets up a new cannon or logs out.
	 *
	 * @param logout
	 * 		If this is true, the cannon pieces go to the players bank, if it's false, it goes to the players inventory as a
	 * 		droppable.
	 */
	public void finish(boolean logout) {
		Player player = World.getPlayerByDisplayName(getOwner());
		if (player != null) {
			if (logout) {
				for (int item : CANNON_ITEMS) {
					player.getBank().addItem(item, 1, true);
				}
				if (getBalls() > 0) {
					player.getBank().addItem(2, getBalls(), true);
				}
			} else {
				for (int item : CANNON_ITEMS) {
					player.getInventory().addItemDrop(item, 1);
				}
				if (getBalls() > 0) {
					player.getInventory().addItemDrop(2, getBalls());
				}
			}
			OwnedObjectManager.removeObject(player, object);
			player.setDwarfCannon(null);
		}
	}

	private final String owner;

	private final WorldObject object;

	private boolean firing;

	private int balls;

	private CannonDirection direction;

	public static final int[] CANNON_ITEMS = new int[] { 6, 8, 10, 12 };

	/**
	 *
	 */
	private static final long serialVersionUID = 2881293458319092111L;
}
