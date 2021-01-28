package com.runescape.network.stream.incoming.impl;

import com.runescape.game.content.global.clans.ClansManager;
import com.runescape.game.content.skills.slayer.SlayerManagement;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.LockManagement.LockType;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.RouteEvent;
import com.runescape.game.world.entity.player.actions.PlayerCombat;
import com.runescape.game.world.entity.player.actions.PlayerFollow;
import com.runescape.network.stream.InputStream;
import com.runescape.network.stream.incoming.IncomingStreamDecoder;
import com.runescape.utility.Utils;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/27/2016
 */
public class PlayerInteractionStreamDecoder extends IncomingStreamDecoder {

	private final static int PLAYER_OPTION_1 = 14;

	private final static int PLAYER_OPTION_2 = 53;

	private final static int PLAYER_OPTION_4 = 77;

	private final static int PLAYER_OPTION_5 = 50;

	private final static int PLAYER_INVITE_OPTION = 43;

	private final static int ACCEPT_TRADE_CHAT_PACKET = 46;

	// TODO player option 5 for nmz

	@Override
	public int[] getKeys() {
		return new int[] { 14, 53, 43, 77, 46, 50 };
	}

	@Override
	public void decode(Player player, InputStream stream, int packetId, int length) {
		switch (packetId) {
			case PLAYER_OPTION_1:
				handlePlayerOption1(player, packetId, length, stream);
				break;
			case PLAYER_OPTION_2:
				handlePlayerOption2(player, packetId, length, stream);
				break;
			case ACCEPT_TRADE_CHAT_PACKET:
			case PLAYER_OPTION_4:
				handlePlayerOption4(player, packetId, length, stream);
				break;
			case PLAYER_INVITE_OPTION:
				handlePlayerInvite(player, packetId, length, stream);
				break;
			case PLAYER_OPTION_5:
				stream.readByte();
				int playerIndex = stream.readUnsignedShort();
				Player p2 = World.getPlayers().get(playerIndex);
				if (p2 == null || p2.isDead() || p2.hasFinished() || !player.getMapRegionsIds().contains(p2.getRegionId())) {
					return;
				}
				if (player.getLockManagement().isLocked(LockType.PLAYER_INTERACTION)) {
					return;
				}
				player.setRouteEvent(new RouteEvent(p2, () -> {
					player.stopAll(false);
					player.getControllerManager().handlePlayerOption5(p2);
				}));
				break;
		}
	}

	private void handlePlayerOption1(Player player, Integer packetId, Integer length, InputStream stream) {
		if (!player.hasStarted() || !player.clientHasLoadedMapRegion() || player.isDead()) {
			return;
		}
		boolean forceRun = stream.readByte() == 1;
		int playerIndex = stream.readUnsignedShort();
		Player p2 = World.getPlayers().get(playerIndex);
		if (p2 == null || p2.isDead() || p2.hasFinished() || !player.getMapRegionsIds().contains(p2.getRegionId())) {
			return;
		}
		player.stopAll(true);
		if (player.getLockManagement().isLocked(LockType.PLAYER_INTERACTION) || !player.getControllerManager().canPlayerOption1(p2)) {
			return;
		}
		if (!player.isCanPvp()) {
			return;
		}
		if (!player.getControllerManager().canAttack(p2)) {
			return;
		}
		if (!player.isCanPvp() || !p2.isCanPvp()) {
			player.getPackets().sendGameMessage("You can only attack players in a player-vs-player area.");
			return;
		}
		if (!p2.isAtMultiArea() || !player.isAtMultiArea()) {
			if (player.getAttackedBy() != p2 && player.getAttackedByDelay() > Utils.currentTimeMillis()) {
				player.getPackets().sendGameMessage("You are already in combat.");
				return;
			}
			if (p2.getAttackedBy() != player && p2.getAttackedByDelay() > Utils.currentTimeMillis()) {
				if (p2.getAttackedBy() instanceof NPC) {
					p2.setAttackedBy(player);
				} else {
					player.getPackets().sendGameMessage("That player is already in combat.");
					return;
				}
			}
		}
		player.getActionManager().setAction(new PlayerCombat(p2));
	}

	private void handlePlayerOption2(Player player, Integer packetId, Integer length, InputStream stream) {
		if (!player.hasStarted() || !player.clientHasLoadedMapRegion() || player.isDead()) {
			return;
		}
		stream.readByte();
		int playerIndex = stream.readUnsignedShort();
		Player p2 = World.getPlayers().get(playerIndex);
		if (p2 == null || p2.isDead() || p2.hasFinished() || !player.getMapRegionsIds().contains(p2.getRegionId())) {
			return;
		}
		if (player.getLockManagement().isLocked(LockType.PLAYER_INTERACTION)) {
			return;
		}
		player.stopAll(false);
		player.getActionManager().setAction(new PlayerFollow(p2));
	}

	private void handlePlayerOption4(final Player player, Integer packetId, Integer length, InputStream stream) {
		stream.readByte();
		int playerIndex = stream.readUnsignedShort();
		final Player p2 = World.getPlayers().get(playerIndex);
		if (p2 == null || p2.isDead() || p2.hasFinished() || !player.getMapRegionsIds().contains(p2.getRegionId())) {
			return;
		}
		if (player.getLockManagement().isLocked(LockType.PLAYER_INTERACTION)) {
			return;
		}
		player.setRouteEvent(new RouteEvent(p2, () -> {
			player.setNextFaceEntity(p2);
			if (player.isCantTrade()) {
				player.getPackets().sendGameMessage("You are busy.");
				return;
			}
			if (p2.getInterfaceManager().containsScreenInterface() || p2.isCantTrade()) {
				player.getPackets().sendGameMessage("The other player is busy.");
				return;
			}
			if (!p2.withinDistance(player, 14)) {
				player.getPackets().sendGameMessage("Unable to find target " + p2.getDisplayName());
				return;
			}
			if (p2.getAttributes().get("TradeTarget") == player) {
				p2.getAttributes().remove("TradeTarget");
				player.getTrade().openTrade(p2);
				p2.getTrade().openTrade(player);
				return;
			}
			if (player.isAnyIronman() || p2.isAnyIronman()) {
				player.sendMessage("Ironmen cannot trade.");
				return;
			}
			if (!p2.getControllerManager().canTrade()) {
				return;
			}
			player.setNextFaceWorldTile(p2);
			player.getAttributes().put("TradeTarget", p2);
			player.getPackets().sendGameMessage("Sending " + p2.getDisplayName() + " a request...");
			p2.getPackets().sendTradeRequestMessage(player);
		}));
	}

	private void handlePlayerInvite(Player player, Integer packetId, Integer length, InputStream stream) {
		boolean forceRun = stream.readByte() == 1;
		int playerIndex = stream.readUnsignedShort();
		Player p2 = World.getPlayers().get(playerIndex);
		if (p2 == null || p2 == player || p2.isDead() || p2.hasFinished() || !player.getMapRegionsIds().contains(p2.getRegionId()) || player.getLockManagement().isLocked(LockType.PLAYER_INTERACTION)) {
			return;
		}
		if (forceRun) {
			player.setRun(forceRun);
		}
		player.stopAll();
		player.setNextFaceWorldTile(p2);
		if (ClansManager.viewInvite(player, p2)) {
		} else if (SlayerManagement.viewInvite(player, p2)) {

		}
	}
}
