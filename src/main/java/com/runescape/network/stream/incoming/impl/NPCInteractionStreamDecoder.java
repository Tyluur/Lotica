package com.runescape.network.stream.incoming.impl;

import com.runescape.game.world.World;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.familiar.impl.Familiar;
import com.runescape.game.world.entity.npc.glacor.Glacyte;
import com.runescape.game.world.entity.player.LockManagement.LockType;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.actions.PlayerCombat;
import com.runescape.network.codec.decoders.handlers.NPCHandler;
import com.runescape.network.stream.InputStream;
import com.runescape.network.stream.incoming.IncomingStreamDecoder;
import com.runescape.utility.Utils;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/27/2016
 */
public class NPCInteractionStreamDecoder extends IncomingStreamDecoder {

	private final static int NPC_CLICK1_PACKET = 9;

	private final static int NPC_CLICK2_PACKET = 31;

	private final static int NPC_CLICK3_PACKET = 28;

	private final static int NPC_CLICK4_PACKET = 67;

	private final static int NPC_EXAMINE_PACKET = 92;

	private final static int ATTACK_NPC = 66;

	@Override
	public int[] getKeys() {
		return new int[] { 9, 31, 28, 67, 92, 66 };
	}

	@Override
	public void decode(Player player, InputStream stream, int packetId, int length) {
		switch (packetId) {
			case ATTACK_NPC:
				if (!player.hasStarted() || !player.clientHasLoadedMapRegion() || player.isDead()) {
					return;
				}
				if (player.getLockManagement().isLocked(LockType.NPC_INTERACTION)) {
					return;
				}
				stream.readByte128();
				int npcIndex = stream.readUnsignedShort128();
				NPC npc = World.getNPCs().get(npcIndex);
				if (npc == null || npc.isDead() || npc.hasFinished() || !player.getMapRegionsIds().contains(npc.getRegionId()) || !npc.getDefinitions().hasAttackOption()) {
					return;
				}
				if (!player.getControllerManager().canAttack(npc)) {
					return;
				}
				if (npc instanceof Familiar) {
					Familiar familiar = (Familiar) npc;
					if (familiar == player.getFamiliar()) {
						player.getPackets().sendGameMessage("You can't attack your own familiar.");
						return;
					}
					if (!familiar.canAttack(player)) {
						player.getPackets().sendGameMessage("You can't attack this npc.");
						return;
					}
				} else if (!npc.isForceMultiAttacked()) {
					if (!npc.isAtMultiArea() || !player.isAtMultiArea()) {
						if (!(npc instanceof Glacyte)) {
							if (player.getAttackedBy() != npc && player.getAttackedByDelay() > Utils.currentTimeMillis()) {
								player.getPackets().sendGameMessage("You are already in combat.");
								return;
							}
							if (npc.getAttackedBy() != player && npc.getAttackedByDelay() > Utils.currentTimeMillis()) {
								player.getPackets().sendGameMessage("This npc is already in combat.");
								return;
							}
						}
					}
				}
				if (!npc.canBeAttacked(player)) {
					return;
				}
				player.stopAll(false);
				player.getActionManager().setAction(new PlayerCombat(npc));
				break;
			case NPC_CLICK1_PACKET:
				NPCHandler.handleNPCInteraction(player, 1, stream);
				break;
			case NPC_CLICK2_PACKET:
				NPCHandler.handleNPCInteraction(player, 2, stream);
				break;
			case NPC_CLICK3_PACKET:
				NPCHandler.handleNPCInteraction(player, 3, stream);
				break;
			case NPC_CLICK4_PACKET:
				NPCHandler.handleNPCInteraction(player, 4, stream);
				break;
			case NPC_EXAMINE_PACKET:
				NPCHandler.handleNPCInteraction(player, 5, stream);
				break;
		}
	}
}
