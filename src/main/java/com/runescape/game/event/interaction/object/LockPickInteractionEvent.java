package com.runescape.game.event.interaction.object;

import com.runescape.game.event.interaction.type.ObjectInteractionEvent;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.Hit;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.utility.Utils;
import com.runescape.utility.world.ClickOption;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 4/28/2016
 */
public class LockPickInteractionEvent extends ObjectInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] { 2558 };
	}

	@Override
	public boolean handleObjectInteraction(Player player, WorldObject object, ClickOption option) {
		Doors door = Doors.getDoor(object);
		if (door == null) {
			return false;
		}
		if (option == ClickOption.FIRST) {
			return true;
		} else if (option == ClickOption.SECOND) {
			WorldTile toTile = player.matches(object) ? door.outside : door.inside;
			if (toTile.equals(door.inside) && !player.getInventory().containsItem(1523, 1)) {
				player.sendMessage("You need a lock pick to enter this room.", false);
				return true;
			}
			if (player.getSkills().getLevel(Skills.THIEVING) < 70) {
				player.sendMessage("You need a thieving level of 70 to pick this lock.");
				return true;
			}
			if (Utils.random(5) != 1) {
				player.applyHit(new Hit(player, 1));
				return true;
			}
			player.setNextWorldTile(toTile);
			player.getInventory().deleteItem(1523, 1);
		}
		return true;
	}

	private enum Doors {

		PIRATE_HUT_WEST(new WorldTile(3038, 3956, 0), new WorldTile(3038, 3956, 0), new WorldTile(3037, 3956, 0)),
		PIRATE_HUT_NORTH(new WorldTile(3041, 3959, 0), new WorldTile(3041, 3959, 0), new WorldTile(3041, 3960, 0)),
		PIRATE_HUT_EAST(new WorldTile(3044, 3956, 0), new WorldTile(3044, 3956, 0), new WorldTile(3045, 3956, 0)),;

		private final WorldTile spawnTile, inside, outside;

		Doors(WorldTile spawnTile, WorldTile inside, WorldTile outside) {
			this.spawnTile = spawnTile;
			this.inside = inside;
			this.outside = outside;
		}

		public static Doors getDoor(WorldTile object) {
			for (Doors door : values()) {
				if (door.spawnTile.matches(object)) {
					return door;
				}
			}
			return null;
		}

	}
}
