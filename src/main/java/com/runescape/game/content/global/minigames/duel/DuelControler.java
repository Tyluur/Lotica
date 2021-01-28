package com.runescape.game.content.global.minigames.duel;

import com.runescape.game.interaction.controllers.Controller;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.RouteEvent;
import com.runescape.game.world.entity.player.actions.PlayerFollow;

public class DuelControler extends Controller {

	@Override
	public void start() {
		sendInterfaces();
		player.getAppearence().generateAppearenceData();
		player.getPackets().sendPlayerOption("Challenge", 1, false);
		moved();
	}

	@Override
	public boolean login() {
		start();
		return false;
	}

	@Override
	public boolean logout() {
		return false;
	}

	@Override
	public void forceClose() {
		remove();
	}

	@Override
	public boolean processMagicTeleport(WorldTile toTile) {
		return true;
	}

	@Override
	public boolean processItemTeleport(WorldTile toTile) {
		return true;
	}

	@Override
	public void magicTeleported(int type) {
		removeController();
		remove();
	}

	@Override
	public void moved() {
		if (!isAtDuelArena(player)) {
			removeController();
			remove();
		}
	}

	@Override
	public boolean canPlayerOption1(final Player target) {
		PlayerFollow.followPathTo(player, target.getWorldTile());
		player.setRouteEvent(new RouteEvent(target, () -> {
			player.stopAll();
			if (target.getInterfaceManager().containsScreenInterface()) {
				player.getPackets().sendGameMessage("The other player is busy.");
				return;
			}
			if (target.getAttributes().get("DuelChallenged") == player) {
				player.getControllerManager().removeController();
				target.getControllerManager().removeController();
				target.getAttributes().remove("DuelChallenged");
				player.setLastDuelRules(new DuelRules(player, target));
				target.setLastDuelRules(new DuelRules(target, player));
				player.getControllerManager().startController("DuelArena", target, target.getAttributes().get("DuelFriendly"));
				target.getControllerManager().startController("DuelArena", player, target.getAttributes().remove("DuelFriendly"));
				return;
			}
			player.getInterfaceManager().sendInterface(640);
			player.setNextFaceEntity(target);
			player.getAttributes().put("DuelTarget", target);
			player.getAttributes().put("WillDuelFriendly", true);
			player.getPackets().sendConfig(283, 67108864);
		}));
		return false;
	}

	public static void challenge(Player player) {
		player.closeInterfaces();
		Boolean friendly = (Boolean) player.getAttributes().remove("WillDuelFriendly");
		if (friendly == null)
			return;
		Player target = (Player) player.getAttributes().remove("DuelTarget");
		if (target == null || target.hasFinished() || !target.withinDistance(player, 14) || !(target.getControllerManager().getController() instanceof DuelControler)) {
			player.getPackets().sendGameMessage("Unable to find " + (target == null ? "your target" : target.getDisplayName()));
			return;
		}
		player.getAttributes().put("DuelChallenged", target);
		player.getAttributes().put("DuelFriendly", friendly);
		player.getPackets().sendGameMessage("Sending " + target.getDisplayName() + " a request...");
		target.getPackets().sendDuelChallengeRequestMessage(player, friendly);
	}

	public void remove() {
		player.getInterfaceManager().closeOverlay();
		player.getAppearence().generateAppearenceData();
		player.getPackets().sendPlayerOption("null", 1, false);
	}

	@Override
	public void sendInterfaces() {
		if (isAtDuelArena(player)) {
			player.getInterfaceManager().sendOverlay(638);
		}
	}

	public static boolean isAtDuelArena(WorldTile tile) {
		return tile.withinArea(3341, 3265, 3387, 3281);
	}
}
