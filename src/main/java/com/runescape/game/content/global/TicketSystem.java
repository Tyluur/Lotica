package com.runescape.game.content.global;

import com.runescape.game.world.World;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.ForceTalk;
import com.runescape.game.world.entity.player.Player;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

import java.util.ArrayList;
import java.util.Iterator;

public class TicketSystem {

	/**
	 * The list of tickets
	 */
	public static final ArrayList<TicketEntry> tickets = new ArrayList<>();

	/**
	 * The amount of tickets processed
	 */
	private static int TICKET_COUNT;

	/**
	 * Answers the first open ticket
	 *
	 * @param player
	 * 		The player answering
	 */
	public static void answerTicket(Player player) {
		removeTicket(player);
		filterTickets();
		if (tickets.isEmpty()) {
			player.getPackets().sendGameMessage("There are no tickets open, congratulations!");
			return;
		} else if (player.getAttributes().get("ticketTarget") != null) {
			removeTicket(player);
		}
		while (tickets.size() > 0) {
			TicketEntry ticket = tickets.get(0);// next in line
			Player target = ticket.player;
			if (target == null) {
				tickets.remove(0);
				continue; // shouldn't happen but k
			}
			if (target.getInterfaceManager().containsChatBoxInter() || target.getControllerManager().getController() != null || target.getInterfaceManager().containsInventoryInter() || target.getInterfaceManager().containsScreenInterface()) {
				tickets.remove(0);
				continue;
			}
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					player.setNextForceTalk(new ForceTalk("Hello there, I'm prepared to answer your questions."));
					player.getAttributes().put("ticketTarget", ticket);
					target.useStairs(-1, new WorldTile(player, 1), 1, 2);
				}
			});
			tickets.remove(ticket);
			player.getPackets().sendGameMessage("There is " + tickets.size() + " tickets left, this is ticket # " + ((TICKET_COUNT++) + 1) + ".");
			player.getPackets().sendGameMessage("The ticket was issued for the following reason: " + ticket.getReason() + ".");
			break;
		}
	}
	
	public static void removeTicket(Player player) {
		Object att = player.getAttributes().get("ticketTarget");
		if (att == null) {
			return;
		}
		TicketEntry ticket = (TicketEntry) att;
		Player target = ticket.getPlayer();
		target.setNextWorldTile(ticket.getTile());
		target.getAttributes().remove("ticketRequest");
		player.getAttributes().remove("ticketTarget");
	}
	
	public static void filterTickets() {
		for (Iterator<TicketEntry> it = tickets.iterator(); it.hasNext(); ) {
			TicketEntry entry = it.next();
			if (entry.player.hasFinished()) {
				it.remove();
			}
		}
	}
	
	public static boolean requestTicket(Player player, TicketEntry ticket) {
		if (player.getInterfaceManager().containsInventoryInter() || player.getInterfaceManager().containsScreenInterface()) {
			player.getPackets().sendGameMessage("Please finish what you're doing before requesting assistance.");
			return false;
		}
		if (!canSubmitTicket() || player.getAttributes().get("ticketRequest") != null || player.getControllerManager().getController() != null) {
			player.getPackets().sendGameMessage("You cannot submit an assistance request at this time.");
			return false;
		}
		player.getAttributes().put("ticketRequest", true);
		tickets.add(ticket);
		int indexOfTicket = 0;
		for (TicketEntry entry : tickets) {
			if (entry.equals(ticket)) {
				break;
			}
			indexOfTicket++;
		}
		for (Player mod : World.getPlayers()) {
			if (mod == null || mod.hasFinished() || !mod.hasStarted() || (!mod.isStaff())) {
				continue;
			}
			mod.getPackets().sendGameMessage("A ticket has been submitted by " + player.getDisplayName() + ". (" + (indexOfTicket + 1) + "/" + tickets.size() + ")");
		}
		return true;
	}
	
	public static boolean canSubmitTicket() {
		filterTickets();
		return true;
	}
	
	public static class TicketEntry {

		private final Player player;

		private final String reason;

		private final WorldTile tile;
		
		public TicketEntry(Player player, String reason) {
			this.player = player;
			this.reason = reason;
			this.tile = player;
		}

        public Player getPlayer() {
            return this.player;
        }

        public String getReason() {
            return this.reason;
        }

        public WorldTile getTile() {
            return this.tile;
        }
    }
}
