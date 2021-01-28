package com.runescape.workers.tasks.impl;

import com.runescape.game.content.global.wilderness.WildernessActivityManager;
import com.runescape.game.content.skills.DXPAlgorithms;
import com.runescape.game.interaction.controllers.impl.Wilderness;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.utility.ChatColors;
import com.runescape.utility.ServerInformation;
import com.runescape.utility.Utils;
import com.runescape.utility.world.player.DailyEvents;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 19, 2015
 */
public class InformationTabTick extends WorldTask {

	/**
	 * The shooting star
	 */
	public static ShootingStarTick shootingStarTick;

	@Override
	public void run() {
		int interfaceId = 930;
		Stream<Player> players = World.players();
		players.forEach(player -> {
			StringBuilder bldr = new StringBuilder();
			bldr.append("Online: <col=" + ChatColors.WHITE + ">").append(Utils.getFakePlayerCount()).append("<br>");
			bldr.append("Wilderness: <col=" + ChatColors.WHITE + ">").append(findWildernessActivity()).append("<br>");
			bldr.append("Uptime: <col=" + ChatColors.WHITE + ">").append(ServerInformation.getGameUptime()).append("<br>");
			if (player.isStaff()) {
				bldr.append("Lag: <col=" + ChatColors.WHITE + ">").append(Utils.getLagPercentage()).append("%<br>");
			}

			bldr.append("<br><col=FF0000>Events<br><br>");
			int skillOfTheDay = DailyEvents.getSkillOfTheDay();
			if (skillOfTheDay != -1) {
				bldr.append("SOTD: <col=" + ChatColors.WHITE + ">").append(Skills.SKILL_NAME[skillOfTheDay]).append(" (").append(DailyEvents.getTimeTillNext()).append(")<br>");
			}

			String wildActivity = WildernessActivityManager.getSingleton().getActivityDescription();
			bldr.append("Wildy: <col=" + ChatColors.WHITE + ">").append(wildActivity == null ? "No Event" : wildActivity).append("<br>");

			if (shootingStarTick == null) {
				shootingStarTick = WorldTasksManager.getTask(ShootingStarTick.class);
			}
			if (shootingStarTick != null) {
				bldr.append(shootingStarTick.getStarInformation()).append("<br>");
			}

			bldr.append("<br><col=FF0000>Player<br><br>");

			bldr.append("Rank: <col=" + ChatColors.WHITE + ">").append(Utils.formatPlayerNameForDisplay(player.getPrimaryRight().getName())).append("<br>");

			if (player.isAnyDonator() && player.getFacade().hasDonatorTimeRemaining()) {
				long difference = player.getFacade().getDonatorExpirationTime() - System.currentTimeMillis();
				long days = TimeUnit.MILLISECONDS.toDays(difference);
				long hours = TimeUnit.MILLISECONDS.toHours(difference);
				boolean daysLimited = days <= 0;
				bldr.append("Membership Left: <col=" + ChatColors.WHITE + ">").append(daysLimited ? hours : days).append(" ").append(daysLimited ? "hours" : "days").append("<br>");
			}

			bldr.append("Playtime: <col=" + ChatColors.WHITE + ">").append(player.getFacade().getPlaytime()).append("<br>");
			if (DXPAlgorithms.enabledHourlyBonus(player)) {
				long minutes = TimeUnit.MILLISECONDS.toMinutes(player.getFacade().getDoubleExperienceOverAt() - System.currentTimeMillis());
				long seconds = TimeUnit.MILLISECONDS.toSeconds(player.getFacade().getDoubleExperienceOverAt() - System.currentTimeMillis());
				bldr.append("DXP: <col=" + ChatColors.WHITE + ">").append(minutes > 1 ? minutes + " minutes" : seconds + " seconds").append(" left<br>");
			}

			bldr.append("<br><col=FF0000>Wilderness<br><br>");
			bldr.append("Kills: <col=" + ChatColors.WHITE + ">").append(Utils.format(player.getKillCount())).append("<br>");
			bldr.append("Deaths: <col=" + ChatColors.WHITE + ">").append(Utils.format(player.getDeathCount())).append("<br>");
			bldr.append("Streak: <col=" + ChatColors.WHITE + ">").append(Utils.format(player.getFacade().getKillstreak())).append("<br>");
			bldr.append("KDR: <col=" + ChatColors.WHITE + ">").append(Utils.round(player.getDeathCount() == 0 ? 1 : (double) player.getKillCount() / (double) player.getDeathCount(), 2)).append("<br>");

			if (player.getFacade().hasSlayerTask()) {
				bldr.append("Slayer: <col=" + ChatColors.WHITE + ">").append(player.getFacade().getSlayerTask().getAmountToKill()).append("x ").append(player.getFacade().getSlayerTask().getName()).append("<br>");
			}

			StringBuilder staffOnlineDetails = new StringBuilder();

			// the list of staff online
			List<Player> staffOnline = getStaffOnline();

			// the index for the staff member online
			int index = 1;

			// writing details to the builder
			for (Player p : staffOnline) {
				if (p == null || p.getPrimaryRight() == null) {
					continue;
				}
				staffOnlineDetails.append(index).append(". <img=").append(p.getPrimaryRight().getCrownIcon()).append(">").append(p.getDisplayName()).append("<br>");
				index++;
			}

			if (!staffOnline.isEmpty()) {
				String staffText = ("<br><col=FF0000>Staff</col><br><br>");
				bldr.append(staffText);
				bldr.append(staffOnlineDetails);
			}

			// sends all the text in the stringbuilder
			player.getPackets().sendIComponentText(interfaceId, 16, bldr.toString());
		});
	}

	/**
	 * Gets a sorted list of staff members online. This list is sorted by the rank of the staff members
	 *
	 * @return A {@code List} {@code Object}
	 */
	private List<Player> getStaffOnline() {
		List<Player> staffOnline = new ArrayList<>();
		World.players().filter(Player::isStaff).forEach(staffOnline::add);
		Collections.sort(staffOnline, (o1, o2) -> o1.getPrimaryRight().compareTo(o2.getPrimaryRight()));
		return staffOnline;
	}

	/**
	 * This method finds how many players are in the wilderness
	 */
	private int findWildernessActivity() {
		int count = 0;
		for (Player player : World.getPlayers()) {
			if (player == null) {
				continue;
			}
			if (Wilderness.isAtWild(player)) {
				count++;
			}
		}
		return count;
	}

}
