package com.runescape.game.interaction.controllers.impl;

import com.runescape.game.content.skills.magic.Magic;
import com.runescape.game.interaction.controllers.Controller;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.ForceTalk;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.others.OrkLegion;
import com.runescape.game.world.entity.player.Player;

public class BorkControler extends Controller {

	public static int borkStage;
	public NPC bork;

	@Override
	public void start() {
		borkStage = (int) getArguments()[0];
		bork = (NPC) getArguments()[1];
		process();
	}

	int stage = 0;

	@Override
	public void process() {
		if (borkStage == 0) {
			if (stage == 0) {
				Magic.sendNormalTeleportSpell(player, 0, 0, new WorldTile(3114, 5528, 0));
			}
			if (stage == 5) {
				sendInterfaces();
			}
			if (stage == 18) {
				player.getPackets().closeInterface(player.getInterfaceManager().onResizable() ? 1 : 11);
				player.getDialogueManager().startDialogue("DagonHai", 7137, player, -1);
				player.getPackets().sendGameMessage("The choas teleporter transports you to an unknown portal.");
				removeController();
			}
		} else if (borkStage == 1) {
			if (stage == 4) {
				sendInterfaces();
				bork.setCantInteract(true);
			} else if (stage == 14) {
				for (int i = 0; i < 3; i++) {
					new OrkLegion(7135, new WorldTile(bork, 1), player);
				}
				player.getPackets().closeInterface(player.getInterfaceManager().onResizable() ? 1 : 11);
				bork.setCantInteract(false);
				bork.setNextForceTalk(new ForceTalk("Destroy the intruder, my Legions!"));
				removeController();
			}
		}
		stage++;
	}

	@Override
	public void sendInterfaces() {
		if (borkStage == 0) {
			player.getInterfaceManager().sendTab(player.getInterfaceManager().onResizable() ? 1 : 11, 692);
		} else if (borkStage == 1) {
			for (Entity t : bork.getPossibleTargets()) {
				Player pl = (Player) t;
				pl.getInterfaceManager().sendTab(pl.getInterfaceManager().onResizable() ? 1 : 11, 691);
			}
		}
	}

	@Override
	public boolean processMagicTeleport(WorldTile toTile) {
		return true;
	}

	@Override
	public boolean keepCombating(Entity target) {
		return !(borkStage == 1 && stage == 4);
	}

	@Override
	public boolean canEquip(int slotId, int itemId) {
		return !(borkStage == 1 && stage == 4);
	}

	@Override
	public boolean canAttack(Entity target) {
		return !(borkStage == 1 && stage == 4);
	}

	@Override
	public boolean canMove(int dir) {
		return !(borkStage == 1 && stage == 4);
	}

}
