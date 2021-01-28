/*
package com.runescape.game.world.entity.player.rights;

import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.ChatColors;

*/
/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 18, 2015
 *//*

public enum Rights {

	OWNER(4) {
		@Override
		public boolean availableFor(Player player) {
			return player.hasPrivilegesOf(this);
		}

		@Override
		public int getMessageIcon() {
			return 15;
		}

		@Override
		public int getCrownIcon() {
			return 15;
		}

		@Override
		public int getClientRight() {
			return 2;
		}

		@Override
		public String colour() {
			return ChatColors.RED;
		}

	},

	ADMINISTRATOR(7) {
		@Override
		public boolean availableFor(Player player) {
			return player.hasPrivilegesOf(OWNER, this);
		}

		@Override
		public int getMessageIcon() {
			return 2;
		}

		@Override
		public int getCrownIcon() {
			return 1;
		}

		@Override
		public int getClientRight() {
			return 2;
		}

		@Override
		public String colour() {
			return ChatColors.YELLOW;
		}
	},

	SERVER_MODERATOR(6) {
		@Override
		public boolean availableFor(Player player) {
			return player.hasPrivilegesOf(OWNER, ADMINISTRATOR, this);
		}

		@Override
		public int getMessageIcon() {
			return 1;
		}

		@Override
		public int getClientRight() {
			return 0;
		}

		@Override
		public int getCrownIcon() {
			return 0;
		}

		@Override
		public String colour() {
			return ChatColors.WHITE;
		}
	},

	FORUM_MODERATOR(15) {
		@Override
		public boolean availableFor(Player player) {
			return player.hasPrivilegesOf(this);
		}

		@Override
		public int getMessageIcon() {
			return -1;
		}

		@Override
		public int getClientRight() {
			return 0;
		}

		@Override
		public int getCrownIcon() {
			return -1;
		}

		@Override
		public String colour() {
			return ChatColors.GREEN;
		}
	},

	SUPPORT(8) {
		@Override
		public boolean availableFor(Player player) {
			return player.hasPrivilegesOf(OWNER, ADMINISTRATOR, SERVER_MODERATOR, this);
		}

		@Override
		public int getMessageIcon() {
			return 14;
		}

		@Override
		public int getClientRight() {
			return 0;
		}

		@Override
		public int getCrownIcon() {
			return 14;
		}

		@Override
		public String colour() {
			return ChatColors.BLUE;
		}
	},

	ELITE_DONATOR(14) {
		@Override
		public boolean availableFor(Player player) {
			return player.hasPrivilegesOf(this);
		}

		@Override
		public int getMessageIcon() {
			return 12;
		}

		@Override
		public int getClientRight() {
			return 0;
		}

		@Override
		public int getCrownIcon() {
			return 12;
		}

		@Override
		public String colour() {
			return "9208c5";
		}

	},

	LEGENDARY_DONATOR(13) {
		@Override
		public boolean availableFor(Player player) {
			return player.hasPrivilegesOf(ELITE_DONATOR, this);
		}

		@Override
		public int getMessageIcon() {
			return 11;
		}

		@Override
		public int getClientRight() {
			return 0;
		}

		@Override
		public int getCrownIcon() {
			return 11;
		}

		@Override
		public String colour() {
			return "d95a00";
		}
	},

	EXTREME_DONATOR(12) {
		@Override
		public boolean availableFor(Player player) {
			return player.hasPrivilegesOf(ELITE_DONATOR, LEGENDARY_DONATOR, this);
		}

		@Override
		public int getMessageIcon() {
			return 10;
		}

		@Override
		public int getClientRight() {
			return 0;
		}

		@Override
		public int getCrownIcon() {
			return 10;
		}

		@Override
		public String colour() {
			return "2fba21";
		}
	},

	SUPREME_DONATOR(11) {
		@Override
		public boolean availableFor(Player player) {
			return player.hasPrivilegesOf(ELITE_DONATOR, LEGENDARY_DONATOR, EXTREME_DONATOR, this);
		}

		@Override
		public int getMessageIcon() {
			return 9;
		}

		@Override
		public int getClientRight() {
			return 0;
		}

		@Override
		public int getCrownIcon() {
			return 9;
		}

		@Override
		public String colour() {
			return "166ca7";
		}
	},

	DONATOR(10) {
		@Override
		public boolean availableFor(Player player) {
			return player.hasPrivilegesOf(OWNER, ELITE_DONATOR, LEGENDARY_DONATOR, EXTREME_DONATOR, SUPREME_DONATOR, this);
		}

		@Override
		public int getMessageIcon() {
			return 8;
		}

		@Override
		public int getClientRight() {
			return 0;
		}

		@Override
		public int getCrownIcon() {
			return 8;
		}

		@Override
		public String colour() {
			return "cb2f2d";
		}
	},

	PLAYER(3) {
		@Override
		public boolean availableFor(Player player) {
			return true;
		}

		@Override
		public int getMessageIcon() {
			return -1;
		}

		@Override
		public int getClientRight() {
			return 0;
		}

		@Override
		public int getCrownIcon() {
			return -1;
		}

		@Override
		public String colour() {
			return ChatColors.BLACK;
		}
	},

	VETERAN(18) {
		@Override
		public boolean availableFor(Player player) {
			return player.hasPrivilegesOf(this);
		}

		@Override
		public int getMessageIcon() {
			return 18;
		}

		@Override
		public int getCrownIcon() {
			return 18;
		}

		@Override
		public int getClientRight() {
			return 0;
		}

		@Override
		public String colour() {
			return "B68418";
		}
	},

	ULTIMATE_IRONMAN(17) {
		@Override
		public boolean availableFor(Player player) {
			return player.hasPrivilegesOf(this);
		}

		@Override
		public int getMessageIcon() {
			return 16;
		}

		@Override
		public int getCrownIcon() {
			return 16;
		}

		@Override
		public int getClientRight() {
			return 0;
		}

		@Override
		public String colour() {
			return ChatColors.WHITE;
		}
	},

	IRONMAN(16) {
		@Override
		public boolean availableFor(Player player) {
			return player.hasPrivilegesOf(this);
		}

		@Override
		public int getMessageIcon() {
			return 13;
		}

		@Override
		public int getCrownIcon() {
			return 13;
		}

		@Override
		public int getClientRight() {
			return 0;
		}

		@Override
		public String colour() {
			return "666666";
		}
	},

	CHALLENGER(19) {
		@Override
		public boolean availableFor(Player player) {
			return player.hasPrivilegesOf(this);
		}

		@Override
		public int getMessageIcon() {
			return -1;
		}

		@Override
		public int getCrownIcon() {
			return -1;
		}

		@Override
		public int getClientRight() {
			return 0;
		}

		@Override
		public String colour() {
			return "0099FF";
		}
	},

	BANNED(5) {
		@Override
		public boolean availableFor(Player player) {
			return false;
		}

		@Override
		public int getMessageIcon() {
			return -1;
		}

		@Override
		public int getCrownIcon() {
			return -1;
		}

		@Override
		public int getClientRight() {
			return 0;
		}

		@Override
		public String colour() {
			return ChatColors.BLACK;
		}
	};

	*/
/**
	 * The forum group id of the right
	 *//*

	private final int forumGroupId;

	Rights(int forumGroupId) {
		this.forumGroupId = forumGroupId;
	}

	*/
/**
	 * This method finds out if the group if a membership group.
	 *
	 * @param groupId
	 * 		The id of the group
	 * @return {@code True} if it is a membership group
	 *//*

	public static boolean isMembershipGroup(int groupId) {
		Rights right = findRight(groupId);
		if (right == null) {
			System.err.println("Could not find forum group by id:" + groupId);
			return false;
		}
		switch (right) {
			case DONATOR:
			case SUPREME_DONATOR:
			case EXTREME_DONATOR:
			case LEGENDARY_DONATOR:
			case ELITE_DONATOR:
				return true;
			default:
				return false;
		}
	}

	*/
/**
	 * Finding a right with the group id
	 *
	 * @param forumGroupId
	 * 		The group id to find
	 *//*

	public static Rights findRight(int forumGroupId) {
		for (Rights right : Rights.values()) {
			if (right.forumGroupId == forumGroupId) {
				return right;
			}
		}
		return null;
	}

	*/
/**
	 * This method finds out if the right is a prioritized one and should not be overriden by any donation subgroups
	 *
	 * @param right
	 * 		The right to check for
	 * @return {@code True} if the right is prioritized
	 *//*

	public static boolean isPrioritized(Rights right) {
		switch (right) {
			case OWNER:
			case ADMINISTRATOR:
			case SERVER_MODERATOR:
			case FORUM_MODERATOR:
			case SUPPORT:
			case CHALLENGER:
			case IRONMAN:
			case ULTIMATE_IRONMAN:
			case VETERAN:
				return true;
			default:
				return false;
		}
	}

	*/
/**
	 * If the command is available for the player
	 *
	 * @param player
	 * 		The player
	 *//*

	public abstract boolean availableFor(Player player);

	*/
/**
	 * The icon that will be sent on messages
	 *
	 * @return An {@code Integer} {@code Object}
	 *//*

	public abstract int getMessageIcon();

	*/
/**
	 * Gets the crown icon id to be sent over interfaces
	 *//*

	public abstract int getCrownIcon();

	*/
/**
	 * The rights that will be sent to the client
	 *//*

	public abstract int getClientRight();

	*/
/**
	 * The colour of this right, shown on yell and other places.
	 *//*

	public abstract String colour();

	*/
/**
	 * @return the forumGroupId
	 *//*

	public int getForumGroupId() {
		return forumGroupId;
	}
}
*/
