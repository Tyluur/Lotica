package com.runescape.game.content.global.minigames.pyramids;

import com.runescape.game.content.Foods;
import com.runescape.game.content.Pots;
import com.runescape.game.content.global.clans.Clan;
import com.runescape.game.content.global.clans.ClanMember;
import com.runescape.game.interaction.controllers.Controller;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleMessage;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.utility.ChatColors;

import java.util.ArrayList;
import java.util.List;

public class PyramidHuntingLobby extends Controller implements PyramidHunterConstants {

	@Override
	public void start() {

	}

	@Override
	public boolean logout() {
		if (lobbyType != null) {
			player.setLocation(RESPAWN_LOBBY_COORDINATES);
		}
		return false;
	}

	@Override
	public void moved() {
		// if you teleport out or something messes up, this makes sure you're in the lobby
		// if you're not in this region its closed
		if (player.getRegionId() != 7473) {
			stop();
		}
	}

	@Override
	public boolean login() {
		return false;
	}

	private void stop() {
		player.getControllerManager().forceStop();
	}

	@Override
	public void magicTeleported(int type) {
		stop();
	}


	@Override
	public void forceClose() {

	}

	@Override
	public void process() {

	}

	@Override
	public boolean processObjectClick1(WorldObject object) {
		switch (object.getId()) {
			case SINGLE_MODE_OBJECT_ID:
				if (lobbyType == null) {
					sendPortalConfirmationDialogue(LobbyType.SINGLE, true);
				} else {
					sendPortalConfirmationDialogue(LobbyType.SINGLE, false);
				}
				return true;
			case TEAM_MODE_OBJECT_ID:
				if (lobbyType == null) {
					sendPortalConfirmationDialogue(LobbyType.TEAM, true);
				} else {
					sendPortalConfirmationDialogue(LobbyType.TEAM, false);
				}
				return true;
			case ENTER_GAME_PORTAL_ID:
				player.getDialogueManager().startDialogue(ENTER_GAME_DIALOGUE, lobbyType);
				return true;
		}
		return true;
	}

	/**
	 * Sends a dialogue that asks the user if they're sure they wish to enter the lobby
	 *
	 * @param type
	 * 		The type of lobby
	 * @param inside
	 * 		If they're going to go inside or outside the lobby
	 */
	private void sendPortalConfirmationDialogue(LobbyType type, boolean inside) {
		if (!inside) {
			handleLobby(type, false);
			return;
		}
		player.getDialogueManager().startDialogue(new Dialogue() {

			@Override
			public void start() {
				sendOptionsDialogue("Enter " + type.name().toLowerCase() + "-mode lobby?", "Yes", "No");
			}

			@Override
			public void run(int interfaceId, int option) {
				if (option == FIRST) {
					end();
					handleLobby(type, true);
				} else {
					end();
				}
			}

			@Override
			public void finish() {

			}

		});
	}

	/**
	 * This method prepares the user to enter the game. After they have passed the prerequisites to enter (no familiar,
	 * empty inventory), they are allowed in. The {@link #startGameSession(Object...)} method is called afterwards.
	 */
	public void prepareGameEntrance() {
		switch (lobbyType) {
			case SINGLE:
				if (hasConsumables(player)) {
					player.getDialogueManager().startDialogue(SimpleMessage.class, "You are not ready!", "Your inventory has consumables!");
					return;
				}
				if (player.getFamiliar() != null) {
					player.getDialogueManager().startDialogue(SimpleMessage.class, "You are not ready!", "Your familiar must be dismissed.");
					return;
				}
				startGameSession();
				break;
			case TEAM:
				if (player.getClanManager() == null) {
					player.getDialogueManager().startDialogue(SimpleMessage.class, "You must be in a clan to start this game!");
					return;
				}
				Clan clan = player.getClanManager().getClan();
				if (!player.isConnectedClanChannel() || clan == null) {
					player.getDialogueManager().startDialogue(SimpleMessage.class, "You must be in a clan to start this game!");
					return;
				}
				if (!player.getUsername().equals(clan.getClanLeaderUsername())) {
					player.getDialogueManager().startDialogue(SimpleMessage.class, "Only the clan leader can start the game!", "Message them and tell them you're ready.");
					return;
				}
				for (ClanMember cMember : clan.getMembers()) {
					Player member = World.getPlayer(cMember.getUsername());
					if (member == null) {
						continue;
					}
					if (!(member.getControllerManager().getController() instanceof PyramidHuntingLobby)) {
						continue;
					}
					PyramidHuntingLobby mLobby = (PyramidHuntingLobby) member.getControllerManager().getController();
					if (mLobby.lobbyType == null || mLobby.lobbyType != LobbyType.TEAM) {
						continue;
					}
					if (hasConsumables(member)) {
						player.getDialogueManager().startDialogue(SimpleMessage.class, member.getDisplayName() + " is not ready!", "Their inventory contains consumables.");
						return;
					}
					if (member.getFamiliar() != null) {
						player.getDialogueManager().startDialogue(SimpleMessage.class, member.getDisplayName() + " is not ready!", "Their familiar must be dismissed.");
						return;
					}
				}
				startGameSession(clan);
				break;
		}
	}

	/**
	 * If the player has food in their inventory
	 *
	 * @param player
	 * 		The player
	 */
	public boolean hasConsumables(Player player) {
		for (Item item : player.getInventory().getItems().toArray()) {
			if (item == null) {
				continue;
			}
			Foods.Food food = Foods.Food.forId(item.getId());
			if (food != null) {
				return true;
			}
			Pots.Pot pot = Pots.getPot(item.getId());
			if (pot != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * /** Starts the game session. This is called after all users have passed the confirmation stage in the lobby and
	 * can officially enter the game.
	 *
	 * @param params
	 * 		Parameters that can be appended to this method
	 */
	public void startGameSession(Object... params) {
		PyramidFloor floor = new PyramidFloor();
		switch (lobbyType) {
			case SINGLE:
				floor.startSingleMode(player);
				break;
			case TEAM:
				Clan clan = (Clan) params[0];
				List<Player> players = new ArrayList<>();
				for (ClanMember cMember : clan.getMembers()) {
					Player member = World.getPlayer(cMember.getUsername());
					if (member == null) {
						continue;
					}
					if (!(member.getControllerManager().getController() instanceof PyramidHuntingLobby)) {
						continue;
					}
					PyramidHuntingLobby mLobby = (PyramidHuntingLobby) member.getControllerManager().getController();
					if (mLobby.lobbyType == null || mLobby.lobbyType != LobbyType.TEAM) {
						continue;
					}
					players.add(member);
				}
				floor.startTeamMode(players);
				break;
		}
	}

	/**
	 * Enters the lobby
	 *
	 * @param lobbyType
	 * 		The type of lobby to enter
	 * @param inside
	 * 		If we are to go inside the lobby or outside the lobby
	 */
	public void handleLobby(LobbyType lobbyType, boolean inside) {
		if (hasConsumables(player)) {
			player.getDialogueManager().startDialogue(SimpleMessage.class, "You are not ready!", "Your inventory has consumables.");
			return;
		}
		if (player.getFamiliar() != null) {
			player.getDialogueManager().startDialogue(SimpleMessage.class, "You are not ready!", "Your familiar must be dismissed.");
			return;
		}
		player.setNextWorldTile(lobbyType.getInformationTiles()[inside ? LobbyType.INSIDE_LOBBY_IDX : LobbyType.OUTSIDE_LOBBY_IDX]);
		if (inside) {
			this.lobbyType = lobbyType;
		} else {
			this.lobbyType = null;
		}
	}

	/**
	 * The type of lobby we're in
	 */
	private LobbyType lobbyType;

	/**
	 * The dialogue that is shown to the player when they are about to enter the game
	 */
	private static final Dialogue ENTER_GAME_DIALOGUE = new Dialogue() {

		@Override
		public void start() {
			stage = -1;
			lobby = (LobbyType) parameters[0];
			sendDialogue("<col=" + ChatColors.BLUE + ">You are about to start a <col=" + ChatColors.MAROON + ">" + lobby.name().toLowerCase() + "-mode</col><col=" + ChatColors.BLUE + "> pyramid hunting game.", "Are you sure you wish to continue?", "This minigame is <col=" + ChatColors.MAROON + ">safe</col>! You don't loose any items.");
		}

		@Override
		public void run(int interfaceId, int option) {
			switch (stage) {
				case -1:
					sendOptionsDialogue("Select an Option", "Start Game!", "Decline.");
					stage = 0;
					break;
				case 0:
					switch (option) {
						case FIRST:
							player.getControllerManager().verifyControlerForOperation(PyramidHuntingLobby.class).ifPresent(c -> {
								c.prepareGameEntrance();
								end();
							});
							break;
						case SECOND:
							end();
							break;
					}
					break;
			}
		}

		@Override
		public void finish() {

		}

		LobbyType lobby;

	};
}
