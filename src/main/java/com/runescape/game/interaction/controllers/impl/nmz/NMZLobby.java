package com.runescape.game.interaction.controllers.impl.nmz;

import com.runescape.game.content.FadingScreen;
import com.runescape.game.content.FadingScreen.FadeTypes;
import com.runescape.game.interaction.controllers.Controller;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.interaction.dialogues.impl.minigame.NMZHostD;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleMessage;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleNPCMessage;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.ChatColors;
import com.runescape.utility.Utils;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 12/27/2015
 */
public class NMZLobby extends Controller {

	/**
	 * The id of the npc that guides people when they're in the lobby
	 */
	private static final int LOBBY_GUIDE_ID = 4516;

	/**
	 * The names of the members in your session.
	 */
	private List<String> sessionMembers;

	/**
	 * If we are the leader of the session we're in
	 */
	private String sessionLeader;

	@Override
	public void start() {
		enterRoom();
		generateSessionVariables();
		refreshPartyInterface();
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				player.getPackets().sendStopCameraShake();
			}
		}, 10);
	}

	@Override
	public void process() {
		if (!inRoom()) {
			player.sendMessage("Your nightmare zone session has ended because you left the lobby.");
			leave();
			return;
		}
		if (player.getAttribute("viewing_store") != null) {
			if (player.getInterfaceManager().containsInterface(256)) {
				player.getInterfaceManager().closeOverlay();
			}
			return;
		}
		if (!player.getInterfaceManager().containsInterface(256)) {
			player.getInterfaceManager().sendOverlay(256);
			refreshPartyInterface();
		}
	}

	@Override
	public boolean processNPCClick1(NPC npc) {
		if (npc.getId() == LOBBY_GUIDE_ID) {
			player.getDialogueManager().startDialogue(NMZHostD.class, npc.getId());
			return false;
		}
		return true;
	}

	@Override
	public boolean sendDeath() {
		player.getLockManagement().lockAll(7000);
		player.stopAll();
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					player.setNextAnimation(new Animation(836));
				} else if (loop == 1) {
					player.getPackets().sendGameMessage("Oh dear, you have died.");
				} else if (loop == 3) {
					player.reset();
					player.setNextAnimation(new Animation(-1));
					leave();
				} else if (loop == 4) {
					player.getPackets().sendMusicEffect(90);
					stop();
				}
				loop++;
			}
		}, 0, 1);
		return false;
	}

	@Override
	public boolean login() {
		return false;
	}

	@Override
	public boolean logout() {
		leave();
		return true;
	}

	@Override
	public void forceClose() {
		leave();
	}

	@Override
	public boolean handlePlayerOption5(Player p2) {
		if (leadingParty()) {
			p2.getControllerManager().verifyControlerForOperation(NMZLobby.class).ifPresent(nmzLobby -> nmzLobby.inviteMember(player, p2));
		} else {
			player.sendMessage("Only the leader can invite members to the party; you must message <col=" + ChatColors.MAROON + ">" + Utils.formatPlayerNameForDisplay(sessionLeader) + "</col>.");
		}
		return false;
	}

	/**
	 * Invites a member to our session
	 *
	 * @param recipient
	 * 		The member we're inviting
	 */
	public void inviteMember(Player inviter, Player recipient) {
		if (!recipient.getControllerManager().verifyControlerForOperation(NMZLobby.class).isPresent()) {
			inviter.sendMessage("That player is not in the lobby.");
			return;
		}
		// we know they are in the controller, so we must send a dialogue asking if they wish to join our party
		// they can only join our party if
		recipient.getControllerManager().verifyControlerForOperation(NMZLobby.class).ifPresent(c -> {
			inviter.sendMessage("You have just invited " + recipient.getDisplayName() + " to your party.");
			sendInvitationDialogue(inviter, recipient);
		});
	}

	/**
	 * Sends the dialogue inviting a player to our party
	 *
	 * @param recipient
	 * 		The player who is receinving the invitation
	 */
	private void sendInvitationDialogue(final Player leader, final Player recipient) {
		recipient.getDialogueManager().startDialogue(new Dialogue() {
			@Override
			public void start() {
				sendDialogue("You have been invited to " + leader.getDisplayName() + "'s party.", "If you accept, you must be ready to enter the nightmare zone.", "However, this is a safe minigame; all your items will be kept on death.");
			}

			@Override
			public void run(int interfaceId, int option) {
				if (stage == -1) {
					sendOptionsDialogue(DEFAULT_OPTIONS, "Join party", "Decline invitation");
					stage = 0;
				} else {
					if (option == FIRST) {
						joinSession(leader, recipient);
					}
					end();
				}
			}

			@Override
			public void finish() {

			}
		});
	}

	/**
	 * @param leader
	 * 		The leader of the session
	 * @param joiner
	 * 		The player who has joined the session
	 */
	private static void joinSession(Player leader, Player joiner) {
		joiner.getControllerManager().verifyControlerForOperation(NMZLobby.class).ifPresent(joinerLobby -> {
			NMZLobby leaderLobby = getLobbyInstance(leader);
			if (leaderLobby == null) {
				joiner.sendMessage("An error occurred; please try again.");
				return;
			}
			if (leaderLobby.sessionMembers.size() == 4) {
				joiner.sendMessage("The leader cannot have any more members in their party.");
				leader.sendMessage("Your party is full; " + joiner.getDisplayName() + " cannot join.");
				return;
			}
			if (leaderLobby.sessionMembers.contains(joiner.getUsername())) {
				leader.sendMessage(joiner.getDisplayName() + " is already in your party.");
				return;
			}
			leaderLobby.sessionMembers.add(joiner.getUsername());
			joinerLobby.sessionLeader = leader.getUsername();
			// we tell all the members their leader has left and assign them as their own session leaders again

			for (String memberName : joinerLobby.sessionMembers) {
				Player sessionPlayer = World.getPlayer(memberName);
				// if there is no player or the player is the player joining, they dont need to know the leader left
				if (sessionPlayer == null || sessionPlayer.equals(joiner)) {
					continue;
				}
				sessionPlayer.sendMessage("Your party leader (" + joiner.getDisplayName() + ") has joined another party; you must find a new leader.");
				handleLeaderLeave(sessionPlayer);
				sessionPlayer.getControllerManager().verifyControlerForOperation(NMZLobby.class).ifPresent(NMZLobby::refreshPartyInterface);
			}
			joinerLobby.sessionMembers.clear();

			leader.sendMessage(joiner.getDisplayName() + " has just joined your party.");
			joiner.sendMessage("You have just joined " + leader.getDisplayName() + "'s party; they are now the leader of this session.");

			leaderLobby.refreshPartyInterface();
			joinerLobby.refreshPartyInterface();
		});
	}

	/**
	 * When a member leaves a session, they become the leader of their session again
	 *
	 * @param member
	 * 		The member whose party has been left by the leader
	 */
	private static void handleLeaderLeave(Player member) {
		member.getControllerManager().verifyControlerForOperation(NMZLobby.class).ifPresent(NMZLobby::generateSessionVariables);
	}

	/**
	 * The player leaves the session formally via this.
	 */
	public void leave() {
		player.getPackets().sendPlayerOption("null", 5, false);
//		player.getPackets().sendBlackOut(0);
		player.getInterfaceManager().closeOverlay();
		// if we are leading the party, all our members must leave our party and lead their own
		if (leadingParty()) {
			for (String memberName : sessionMembers) {
				Player memberPlayer = World.getPlayer(memberName);
				if (memberPlayer == null) {
					continue;
				}
				memberPlayer.getControllerManager().verifyControlerForOperation(NMZLobby.class).ifPresent(memberLobby -> {
					memberLobby.generateSessionVariables();
					memberLobby.refreshPartyInterface();
				});
				memberPlayer.sendMessage("Your leader has left the lobby; you are forced to become the leader of your party.");
			}
		} else {
			// otherwise, if we are a member in someone's party, their list must be updated
			Player leaderPlayer = World.getPlayer(sessionLeader);
			if (leaderPlayer == null) {
				System.out.println("Attempted to remove player from leader's party but leader did not exist.[sessionLeader=" + sessionLeader + "]");
				return;
			}
			leaderPlayer.getControllerManager().verifyControlerForOperation(NMZLobby.class).ifPresent(leaderLobby -> {
				leaderLobby.sessionMembers.remove(player.getUsername());
				leaderLobby.refreshPartyInterface();
				leaderLobby.refreshInterfaceForMembers();
			});
			leaderPlayer.sendMessage(player.getDisplayName() + " has just left your party.");
		}
		removeController();
	}

	/**
	 * If we are leading the party we're in
	 */
	private boolean leadingParty() {return sessionLeader != null && sessionLeader.equals(player.getUsername());}

	/**
	 * Refreshes the interface for all members in our party
	 */
	private void refreshInterfaceForMembers() {
		for (String memberName : sessionMembers) {
			Player memberPlayer = World.getPlayer(memberName);
			if (memberPlayer == null) {
				continue;
			}
			memberPlayer.getControllerManager().verifyControlerForOperation(NMZLobby.class).ifPresent(NMZLobby::refreshPartyInterface);
		}
	}

	/**
	 * Enters the room and adds some effects
	 */
	private void enterRoom() {
		player.getDialogueManager().startDialogue(SimpleMessage.class, "Your nightmare has started.", "Invite players to your party by right-clicking them.", "Speak to the Dream Host when you are ready to enter the nightmare.");
		player.getPackets().sendPlayerOption("Invite", 5, false);
		player.getPackets().sendCameraShake(3, 12, 25, 12, 25);
	}

	/**
	 * Generates default session variables, these are set and symbolize us leading our own party
	 */
	private void generateSessionVariables() {
		sessionLeader = player.getUsername();
		sessionMembers = new ArrayList<>();
	}

	/**
	 * Refreshes the party interface with all information necessary for the lobby
	 */
	private void refreshPartyInterface() {
		int interfaceId = 256;
		int startMemberNameLine = 7;
		int[] slots = { 12, startMemberNameLine };
		int index = 1;
		// therefore, we have a max of 5 members in the party
		Player leaderPlayer = World.getPlayer(sessionLeader);
		if (leaderPlayer == null) {
			System.out.println(player.getUsername() + " had no session leader so the interface did not update");
			return;
		}
		NMZLobby lobbyInstance = getLobbyInstance(leaderPlayer);
		if (lobbyInstance == null) {
			System.out.println("Leader had no lobby instance.");
			return;
		}
		List<String> localMembersList = lobbyInstance.sessionMembers;

		int partySize = localMembersList == null ? 0 : localMembersList.size();

		// removes the text on the interface
		Utils.clearInterface(player, interfaceId);

		// sends the leader information & party members size
		player.getPackets().sendIComponentText(interfaceId, 5, "Party Members (" + (partySize + 1) + ")");
		player.getPackets().sendIComponentText(interfaceId, 6, leaderPlayer.getDisplayName());
		player.getPackets().sendIComponentText(interfaceId, 11, "Leader:");

		// we send the player information first to be blank
		// so it is only overriden on a need-to basis
		for (int i = 0; i < 4; i++) {
			player.getPackets().sendIComponentText(interfaceId, 12 + i, "Player " + (i + 1) + ":");
			player.getPackets().sendIComponentText(interfaceId, 7 + i, "-----");
		}

		// party is empty so we dont need to refresh the names
		if (partySize == 0) {
			return;
		}

		//sends the members list over the interface
		for (String member : localMembersList) {
			Player memberPlayer = World.getPlayer(member);
			if (memberPlayer == null) {
				continue;
			}
			player.getPackets().sendIComponentText(interfaceId, slots[0], "Player " + index + ": ");
			player.getPackets().sendIComponentText(interfaceId, slots[1], memberPlayer.getDisplayName());
			slots[0]++;
			slots[1]++;
			index++;
		}
	}

	/**
	 * Gets the lobby instance for the player
	 *
	 * @param player
	 * 		The player
	 */
	private static NMZLobby getLobbyInstance(Player player) {
		return player.getControllerManager().verifyControlerForOperation(NMZLobby.class).get();
	}

	/**
	 * If the player is in the lobby room
	 */
	private boolean inRoom() {
		int topLeftX = 3091, topLeftY = 3513;
		int bottomRightX = 3100, bottomRightY = 3507;

		return player.getX() >= topLeftX && player.getX() <= bottomRightX && player.getY() >= bottomRightY && player.getY() <= topLeftY;
	}

	/**
	 * Transfers everyone to the nightmare zone
	 */
	public void transfer(NMZModes mode) {
		if (!leadingParty()) {
			player.getDialogueManager().startDialogue(SimpleNPCMessage.class, LOBBY_GUIDE_ID, "Your leader is the only eligible player to transfer the party to the nightmare zone.", "Tell them you are ready so you can battle.");
			return;
		}
		List<Player> team = getTeam();
		if (team.size() > 0) {
			// creates a new nmz instance
			NMZInstance instance = new NMZInstance(team, mode);
			// teleports all members in the instance to the room after generation
			instance.generateAndDeliverRoom();
			team.forEach(teamMember -> {
				// removes the fade & blackout once we've arrived (the interface has been closed)
				teamMember.setCloseInterfacesEvent(() -> {
					// removes blackout
//					teamMember.getPackets().sendBlackOut(0);
					// remove fade
					FadingScreen.removeFade(teamMember);
				});
				// starts the controller
				teamMember.getControllerManager().startController("NMZController", instance);
				// blacks out minimap
//				teamMember.getPackets().sendBlackOut(2);
				// sends the all black fade screen
				FadingScreen.displayFade(player, FadeTypes.ALL_BLACK, -1);
			});
		}
	}

	/**
	 * Gets a list of all members in our team
	 */
	private List<Player> getTeam() {
		List<Player> team = new ArrayList<>();
		for (String name : sessionMembers) {
			if (name == null) {
				continue;
			}
			Player teamPlayer = World.getPlayer(name);
			if (teamPlayer == null) {
				continue;
			}
			team.add(teamPlayer);
		}
		team.add(player);
		return team;
	}
}
