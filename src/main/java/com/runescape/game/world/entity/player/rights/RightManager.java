package com.runescape.game.world.entity.player.rights;

import com.google.gson.reflect.TypeToken;
import com.runescape.cache.Cache;
import com.runescape.utility.Utils;
import com.runescape.utility.external.gson.GsonCollections;
import com.runescape.utility.external.gson.GsonStartup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/25/2016
 */
public class RightManager extends GsonCollections<Right> {

	/**
	 * The list of all possible rights
	 */
	private static final List<Right> RIGHT_LIST = new ArrayList<>();

	/**
	 * The static rights
	 */
	public static Right OWNER, ADMINISTRATOR, SERVER_MODERATOR, FORUM_MODERATOR, SUPPORT, IRONMAN, ULTIMATE_IRONMAN, VETERAN, DONATOR, SUPREME_DONATOR, EXTREME_DONATOR, ELITE_DONATOR, LEGENDARY_DONATOR, PLAYER;

	@Override
	public void initialize() {
		RIGHT_LIST.clear();
		List<Right> rightList = new ArrayList<>(generateList());
		for (int index = 0; index < rightList.size(); index++) {
			Right right = rightList.get(index);
			right.setIndexAndInitialize(index);
			RIGHT_LIST.add(right);
		}
		setVars();
	}

	@Override
	public String getFileLocation() {
		return "data/resource/rights.json";
	}

	@Override
	public List<Right> loadList() {
		return gson.fromJson(Utils.getText(getFileLocation()), new TypeToken<List<Right>>() {}.getType());
	}

	/**
	 * Sets the default variables
	 */
	private void setVars() {
		OWNER = getRight("OWNER").orElseThrow(() -> new RuntimeException("Couldn't find right!"));
		ADMINISTRATOR = getRight("ADMINISTRATOR").orElseThrow(() -> new RuntimeException("Couldn't find right!"));
		SERVER_MODERATOR = getRight("SERVER_MODERATOR").orElseThrow(() -> new RuntimeException("Couldn't find right!"));
		SUPPORT = getRight("SUPPORT").orElseThrow(() -> new RuntimeException("Couldn't find right!"));
		IRONMAN = getRight("IRONMAN").orElseThrow(() -> new RuntimeException("Couldn't find right!"));
		ULTIMATE_IRONMAN= getRight("ULTIMATE_IRONMAN").orElseThrow(() -> new RuntimeException("Couldn't find right!"));
		VETERAN = getRight("VETERAN").orElseThrow(() -> new RuntimeException("Couldn't find right!"));
		DONATOR = getRight("DONATOR").orElseThrow(() -> new RuntimeException("Couldn't find right!"));
		SUPREME_DONATOR = getRight("SUPREME_DONATOR").orElseThrow(() -> new RuntimeException("Couldn't find right!"));
		EXTREME_DONATOR = getRight("EXTREME_DONATOR").orElseThrow(() -> new RuntimeException("Couldn't find right!"));
		ELITE_DONATOR = getRight("ELITE_DONATOR").orElseThrow(() -> new RuntimeException("Couldn't find right!"));
		LEGENDARY_DONATOR = getRight("LEGENDARY_DONATOR").orElseThrow(() -> new RuntimeException("Couldn't find right!"));
		PLAYER = getRight("PLAYER").orElseThrow(() -> new RuntimeException("Couldn't find right!"));
	}

	/**
	 * Gets the right by the name
	 *
	 * @param name
	 * 		The name
	 */
	public static Optional<Right> getRight(String name) {
		return RIGHT_LIST.stream().filter(p -> p.getName().equalsIgnoreCase(name)).findFirst();
	}

	/**
	 * This method finds out if the group if a membership group.
	 *
	 * @param groupId
	 * 		The id of the group
	 * @return {@code True} if it is a membership group
	 */
	public static boolean isMembershipGroup(int groupId) {
		Optional<Right> rightOptional = findRight(groupId);
		if (!rightOptional.isPresent()) {
			System.err.println("Could not find forum group by id:" + groupId);
			return false;
		}
		Right right = rightOptional.get();
		return right.equals(DONATOR) || right.equals(SUPREME_DONATOR) || right.equals(EXTREME_DONATOR) || right.equals(ELITE_DONATOR) || right.equals(LEGENDARY_DONATOR);
	}

	/**
	 * Finds the right by a group id
	 *
	 * @param groupId
	 * 		The group id
	 */
	public static Optional<Right> findRight(int groupId) {
		return RIGHT_LIST.stream().filter(p -> p.getForumGroupId() == groupId).findAny();
	}

	public static void main(String[] args) throws IOException {
		Cache.init();
		GsonStartup.loadAll();
		RightManager manager = GsonStartup.getClass(RightManager.class);

		SUPPORT.addRightsWithAccessTo(OWNER, ADMINISTRATOR, SERVER_MODERATOR);

		manager.save(RIGHT_LIST);
	}

	/**
	 * Adds a new right to the {@link #RIGHT_LIST}
	 *
	 * @param name
	 * 		The name of the right
	 * @param forumGroupId
	 * 		The group of the right
	 * @param messageIcon
	 * 		The message icon of the right
	 * @param crownIcon
	 * 		The crown icon of the right
	 * @param clientRight
	 * 		The client right of the right
	 * @param chatColor
	 * 		The chat color of the right
	 */
	public boolean addRight(String name, int forumGroupId, int messageIcon, int crownIcon, int clientRight, String chatColor) {
		for (Right right : RIGHT_LIST) {
			if (right.getForumGroupId() == forumGroupId) {
				throw new IllegalStateException("Already existed a right for forumGroupId=" + forumGroupId + "\t" + right);
			}
		}
		Right right = new Right();
		right.setName(name);
		right.setForumGroupId(forumGroupId);
		right.setMessageIcon(messageIcon);
		right.setCrownIcon(crownIcon);
		right.setClientRight(clientRight);
		right.setChatColour(chatColor);
		return RIGHT_LIST.add(right);
	}

	/**
	 * Gets the right list
	 */
	public static List<Right> getRightList() {
		return RIGHT_LIST;
	}
}
