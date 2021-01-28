package com.runescape.game.event.interaction.object;

import com.runescape.game.event.interaction.type.ObjectInteractionEvent;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.game.world.item.Item;
import com.runescape.utility.Utils;
import com.runescape.utility.world.ClickOption;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 20, 2015
 */
public class HomeObjectsInteractionEvent extends ObjectInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] {/* 4278, 4705, 4708,*/ 38698 };
	}

	@Override
	public boolean handleObjectInteraction(Player player, WorldObject object, ClickOption option) {
		if (object.getId() == 38698) {
			player.setNextWorldTile(new WorldTile(2815, 5511, 0));
			player.getControllerManager().startController("clan_wars_ffa", false);
			player.putAttribute("entered_portal_via_home", true);
			return true;
		}
		if (object.getRegionId() != 12342) {
			player.sendMessage("You can only thieve from the stalls at home.");
			return true;
		}
		Stall stall = Stall.getStall(object.getId());
		if (stall == null) {
			return true;
		}
		if (player.getInventory().getFreeSlots() == 0) {
			player.sendMessage("Your inventory is full, you cannot thieve from the stall.");
			return true;
		}
		if (player.getSkills().getLevelForXp(Skills.THIEVING) < stall.getLevelRequirement()) {
			player.sendMessage("You need a Thieving level of " + stall.getLevelRequirement() + " to steal from this stall.", false);
			return true;
		}
		final WorldObject emptyStall = new WorldObject(34381, object.getType(), object.getRotation(), object);
		Integer[] delayTicks = stall.getDisappearTicks();
		int min = delayTicks[0];
		int max = delayTicks[1];
		int delay = Utils.random(min, max);
		
		player.setNextAnimation(new Animation(881));
		player.getLockManagement().lockAll(2000);

		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				// if the object was already removed, we do not remove it
				/*if (!World.containsObjectWithId(object.getId(), object)) {
					player.sendMessage("Too late - it's gone!");
					stop();
					return;
				}*/
				player.getInventory().addItem(stall.getSteal());
				player.getSkills().addXp(Skills.THIEVING, stall.getExperienceGiven());

				World.removeObject(object);
				World.spawnObject(emptyStall);
				WorldTasksManager.schedule(new WorldTask() {

					@Override
					public void run() {
						World.removeObject(emptyStall);
						World.spawnObject(object);
						stop();
					}
				}, delay);
				stop();
			}
		}, 1);
		return true;
	}

	private enum Stall {
		FUR(4278, 1, 15) {
			@Override
			public Item getSteal() {
				return new Item(10378);
			}

			@Override
			public Integer[] getDisappearTicks() {
				return new Integer[] { 3, 6 };
			}
		},
		FISH(4705, 30, 40) {
			@Override
			public Item getSteal() {
				return new Item(10386);
			}

			@Override
			public Integer[] getDisappearTicks() {
				return new Integer[] { 2, 4 };
			}
		},
		VEG(4708, 60, 80) {
			@Override
			public Item getSteal() {
				return new Item(10370);
			}

			@Override
			public Integer[] getDisappearTicks() {
				return new Integer[] { 1, 2 };
			}
		};

		/**
		 * The stall object id
		 */
		private final int objectId;

		private final int levelRequirement;

		private final int experienceGiven;

		Stall(int objectId, int levelRequirement, int experienceGiven) {
			this.objectId = objectId;
			this.levelRequirement = levelRequirement;
			this.experienceGiven = experienceGiven;
		}

		/**
		 * @return the objectId
		 */
		public int getObjectId() {
			return objectId;
		}

		/**
		 * The steal the player gets
		 */
		public abstract Item getSteal();

		/**
		 * The time the stall will disappear for. [0] for min, [1] for max
		 */
		public abstract Integer[] getDisappearTicks();

		/**
		 * Finds a stall for the object id
		 *
		 * @param objectId
		 * 		The object id
		 */
		public static Stall getStall(int objectId) {
			for (Stall stall : Stall.values()) {
				if (stall.getObjectId() == objectId) {
					return stall;
				}
			}
			return null;
		}

		/**
		 * @return the levelRequirement
		 */
		public int getLevelRequirement() {
			return levelRequirement;
		}

		/**
		 * @return the experienceGiven
		 */
		public int getExperienceGiven() {
			return experienceGiven;
		}
	}

}
