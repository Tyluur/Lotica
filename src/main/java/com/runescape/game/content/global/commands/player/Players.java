package com.runescape.game.content.global.commands.player;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 18, 2015
 */
public class Players extends CommandSkeleton<String[]> {

	@Override
	public String[] getIdentifiers() {
		return new String[] { "players", "playersonline" };
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		player.sendMessage("There are currently " + Utils.getFakePlayerCount() + " players online.");
		/*// converting the player list to a local list
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
			slot++;
		}
		// sending the data
		Scrollable.sendQuestScroll(player, playerList.size() + " players online", players);
		player.getPackets().sendGameMessage("There are currently " + playerList.size() + " players online.", false);*/
	}

}
