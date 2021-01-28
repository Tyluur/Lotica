package com.runescape.game.world.entity.player.rights;

import com.runescape.game.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/25/2016
 */
public class Right {

	private String name;

	private int forumGroupId;

	/**
	 * The message icon used in sending packets
	 */
	private int messageIcon;

	/**
	 * The crown icon used in sending images
	 */
	private int crownIcon;

	private int clientRight;

	private String chatColour;

	/**
	 * If the player has the rights of these groups, they have access to this right
	 */
	private List<String> rightsWithAccessTo;

	private transient int index;

	/**
	 * Checks if this right is available for the player, based on the {@link #rightsWithAccessTo} list
	 *
	 * @param player
	 * 		The player
	 */
	public boolean isAvailableFor(Player player) {
		// player rights are always available to everyone
		if (equals(RightManager.PLAYER)) {
			return true;
		}
		List<Right> rights = new ArrayList<>();
		for (String name : rightsWithAccessTo) {
			Optional<Right> rightOptional = RightManager.getRight(name);
			if (!rightOptional.isPresent()) {
				continue;
			}
			rights.add(rightOptional.get());
		}
		if (!rights.contains(this)) { rights.add(this); }
		return player.hasPrivilegesOf(rights.toArray(new Right[rights.size()]));
	}

	/**
	 * Compares a right with another right
	 *
	 * @param other
	 * 		The right
	 */
	public int compareTo(Right other) {
		return Integer.compare(getIndex(), other.getIndex());
	}

	/**
	 * Adds a right that has access to this right
	 *
	 * @param rights
	 * 		The rights
	 */
	public boolean addRightsWithAccessTo(Right... rights) {
		for (Right right : rights) {
			if (rightsWithAccessTo.contains(right.getName())) { continue; }
			if (!rightsWithAccessTo.add(right.getName())) { return false; }
		}
		return true;
	}

	/**
	 * Sets the index and initializes the right variables
	 *
	 * @param index
	 * 		The index
	 */
	public void setIndexAndInitialize(int index) {
		this.index = index;
		if (rightsWithAccessTo == null) {
			rightsWithAccessTo = new ArrayList<>();
		}
	}

	/**
	 * If the right is a donator right
	 */
	public boolean isDonator() {
		return name.toLowerCase().contains("donator");
	}

	@Override
	public String toString() {
		return name + "[" + index + "]";
	}

	/**
	 * Gets the proper name
	 */
	public String getProperName() {
		return name.substring(0, 1) + name.substring(1, name.length()).toLowerCase().replaceAll("_", " ");
	}

    public String getName() {
        return this.name;
    }

    public int getForumGroupId() {
        return this.forumGroupId;
    }

    public int getMessageIcon() {
        return this.messageIcon;
    }

    public int getCrownIcon() {
        return this.crownIcon;
    }

    public int getClientRight() {
        return this.clientRight;
    }

    public String getChatColour() {
        return this.chatColour;
    }

    public List<String> getRightsWithAccessTo() {
        return this.rightsWithAccessTo;
    }

    public int getIndex() {
        return this.index;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setForumGroupId(int forumGroupId) {
        this.forumGroupId = forumGroupId;
    }

    public void setMessageIcon(int messageIcon) {
        this.messageIcon = messageIcon;
    }

    public void setCrownIcon(int crownIcon) {
        this.crownIcon = crownIcon;
    }

    public void setClientRight(int clientRight) {
        this.clientRight = clientRight;
    }

    public void setChatColour(String chatColour) {
        this.chatColour = chatColour;
    }

    public void setRightsWithAccessTo(List<String> rightsWithAccessTo) {
        this.rightsWithAccessTo = rightsWithAccessTo;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
