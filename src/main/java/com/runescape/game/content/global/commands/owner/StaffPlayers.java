package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.event.interaction.button.Scrollable;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Player;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 8/9/2016
 */
public class StaffPlayers extends CommandSkeleton<String> {

	@Override
	public String getIdentifiers() {
		return "players";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		List<String> ipAddressList = new ArrayList<>();

		// converting the player list to a local list
		List<Player> playerList = new ArrayList<>(World.players().collect(Collectors.toList()));
		// sorting the list alphabetically
		Collections.sort(playerList, (o1, o2) -> o1.getDisplayName().compareTo(o2.getDisplayName()));
		// creating an array, which will be used to store text
		String[] players = new String[playerList.size()];

		// the fake player index
		int slot = 1;

		for (int i = 0; i < playerList.size(); i++) {
			Player p = playerList.get(i);
			if (p == null || p.getPrimaryRight() == null || p.getDisplayName() == null) {
				continue;
			}
			// writing the text in the slot
			players[i] = slot + ". <img=" + p.getPrimaryRight().getCrownIcon() + ">" + p.getDisplayName();
			ipAddressList.add(p.getSession().getIP());
			slot++;
		}
		// sending the data
		Scrollable.sendQuestScroll(player, playerList.size() + " players online", players);
		player.getPackets().sendGameMessage("There are currently " + playerList.size() + " players online, " + getDoubleEntriesAmount(ipAddressList) + " of which are multi-entries.", false);
	}

	/**
	 * Gets the amount of times entries with the same value are in a list
	 *
	 * @param list
	 * 		The list to scan
	 */
	private int getDoubleEntriesAmount(List<String> list) {
		Map<String, Integer> map = new HashMap<>();

		for (String entry : list) {
			Integer amount = map.get(entry);
			if (amount == null) {
				amount = 0;
			} else {
				amount = amount + 1;
			}
			map.put(entry, amount);
		}

		int amount = 0;

		for (Entry<String, Integer> entry : map.entrySet()) {
			amount += entry.getValue();
		}
		return amount;
	}
}
