package com.runescape.game.world.entity.npc.combat.impl;

import com.runescape.game.content.skills.magic.Magic;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.combat.CombatScript;
import com.runescape.game.world.entity.npc.combat.NPCCombatDefinitions;
import com.runescape.game.world.entity.npc.nomad.FlameVortex;
import com.runescape.game.world.entity.npc.nomad.Nomad;
import com.runescape.game.world.entity.player.Player;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

public class NomadCombat extends CombatScript {

	@Override
	public Object[] getKeys() {
	
		return new Object[] {8528};
	}

	
	private void spawnFlameVortex(WorldTile tile) {
		if(!World.isNotCliped(tile.getPlane(), tile.getX(), tile.getY(), 1))
			return;
		new FlameVortex(tile);
	}
	@Override
	public int attack(final NPC npc, final Entity target) {
		final NPCCombatDefinitions defs = npc.getCombatDefinitions();
		final Nomad nomad = (Nomad) npc;
		if(target instanceof Player) {
			if(!nomad.isMeleeMode() && nomad.getHitpoints() < nomad.getMaxHitpoints() * 0.25) {
				if(!nomad.isHealed()) {
					nomad.setNextAnimation(new Animation(12700));
					nomad.heal(2500);
					nomad.setHealed(true);
					final Player player = (Player) target;
					Dialogue.sendNPCDialogueNoContinue(player, nomad.getId(), 9790, "You're thougher than I thought, time to even things up!");
					player.getPackets().sendVoice(8019);
					WorldTasksManager.schedule(new WorldTask() {
						@Override
						public void run() {
							Dialogue.closeNoContinueDialogue(player);
						}
					}, 9);
					return defs.getAttackDelay();
				}else{
					nomad.setMeleeMode();
					final Player player = (Player) target;
					Dialogue.sendNPCDialogueNoContinue(player, nomad.getId(), 9790, "Enough! THIS..ENDS..NOW!");
					player.getPackets().sendVoice(7964);
					WorldTasksManager.schedule(new WorldTask() {
						@Override
						public void run() {
							Dialogue.closeNoContinueDialogue(player);
						}
					}, 9);
				}
			}
		}
		if(nomad.isMeleeMode()) {
			int distanceX = target.getX() - npc.getX();
			int distanceY = target.getY() - npc.getY();
			int size = npc.getSize();
			if (distanceX > size || distanceX < -1 || distanceY > size|| distanceY < -1) 
				return 0;
			npc.setNextAnimation(new Animation(12696));
			delayHit(npc, 0, target, getRegularHit(npc, getRandomMaxHit(npc, 322, NPCCombatDefinitions.MELEE, target)));
			return 2;
		}else{
			if(target instanceof Player && nomad.useSpecialSpecialMove()) {
				final Player player = (Player) target;
				switch(nomad.getNextMove()) {
				case 0:
					nomad.setNextMovePerform();
					npc.setNextAnimation(new Animation(12701));
					Dialogue.sendNPCDialogueNoContinue(player, nomad.getId(), 9842, "Let's make things interesting!");
					player.getPackets().sendVoice(8039);
					final WorldTile middle = new WorldTile(player);
					WorldTasksManager.schedule(new WorldTask() {
						int count;
						@Override
						public void run() {
							switch(count) {
							case 0:
								spawnFlameVortex(middle.transform(2, 2, 0));
								break;
							case 1:
								spawnFlameVortex(middle.transform(2, 0, 0));
								break;
							case 2:
								spawnFlameVortex(middle.transform(2, -2, 0));
								break;
							case 3:
								spawnFlameVortex(middle.transform(-2, -2, 0));
								break;
							case 4:
								spawnFlameVortex(middle.transform(-2, 0, 0));
								break;
							case 5:
								spawnFlameVortex(middle.transform(-2, 2, 0));
								break;
							case 6:
								spawnFlameVortex(middle.transform(3, 1, 0));
								break;
							case 7:
								spawnFlameVortex(middle.transform(3, -1, 0));
								break;
							case 8:
								spawnFlameVortex(middle.transform(1, -3, 0));
								break;
							case 9:
								spawnFlameVortex(middle.transform(-1, -3, 0));
								break;
							case 10:
								spawnFlameVortex(middle.transform(-3, -1, 0));
								break;
							case 11:
								spawnFlameVortex(middle.transform(-3, 1, 0));
								break;
							case 12:
								Dialogue.closeNoContinueDialogue(player);
								stop();
							break;
							}
							count++;
						}
						
					}, 0, 0);
				break;
				case 1:
					nomad.setCantFollowUnderCombat(true);
					WorldTile throne = nomad.getThroneTile();
					if(nomad.getX() != throne.getX() || nomad.getY() != throne.getY())
						nomad.sendTeleport(nomad.getThroneTile());
					WorldTasksManager.schedule(new WorldTask() {

						private boolean secondLoop;
						
						@Override
						public void run() {
							if(!secondLoop) {
								npc.setNextAnimation(new Animation(12698));
								npc.setNextGraphics(new Graphics(2281));
								Dialogue.sendNPCDialogueNoContinue(player, nomad.getId(), 9790, "You cannot hide from my wrath!");
								player.getPackets().sendVoice(7960);
								secondLoop = true;
							}else{
								if(npc.clipedProjectile(target, false)) {
									delayHit(npc, 2, target, getRegularHit(npc, 750));
									World.sendProjectile(npc, target, 1658, 30, 30, 75, 25, 0, 0);
								}
								nomad.setCantFollowUnderCombat(false);
								Dialogue.closeNoContinueDialogue(player);
								nomad.setNextMovePerform();
								stop();
							}
							
						}
						
					}, 7, 10);
					return 25;
				case 2:
					Dialogue.sendNPCDialogueNoContinue(player, nomad.getId(), 9842, "Let's see how well you senses serve you!");
					player.getActionManager().forceStop();
					nomad.createCopies(player);
					WorldTasksManager.schedule(new WorldTask() {
						@Override
						public void run() {
							Dialogue.closeNoContinueDialogue(player);
						}
					}, 9);
					return 7;
				case 3:
					nomad.setCantFollowUnderCombat(true);
					throne = nomad.getThroneTile();
					nomad.sendTeleport(nomad.getThroneTile());
					Magic.sendObjectTeleportSpell(player, false, throne.transform(1, -3, 0));
					player.getLockManagement().lockAll();
					WorldTasksManager.schedule(new WorldTask() {
						private boolean secondLoop;
						
						@Override
						public void run() {
							if(!secondLoop) {
								npc.setNextAnimation(new Animation(12699));
								npc.setNextGraphics(new Graphics(2280));
								player.addFreezeDelay(17000);
								Dialogue.sendNPCDialogueNoContinue(player, nomad.getId(), 9790, "Let's see how much punishment you can take!");
								player.getPackets().sendVoice(8001);
								player.setNextFaceWorldTile(new WorldTile(player.getX(), player.getY()+1, 0));
								player.setNextGraphics(new Graphics(369));
								player.getLockManagement().unlockAll();
								secondLoop = true;
								WorldTasksManager.schedule(new WorldTask() {
									@Override
									public void run() {
										Dialogue.closeNoContinueDialogue(player);
									}
								}, 9);
							}else{
								delayHit(npc, 2, target, getRegularHit(npc, player.getMaxHitpoints()-1));
								World.sendProjectile(npc, target, 2280, 30, 30, 5, 25, 0, 0);
								nomad.setCantFollowUnderCombat(false);
								nomad.setNextMovePerform();
								stop();
							}
							
						}
						
					}, 7, 23);
					
				return 40;
				}
			}else{
				npc.setNextAnimation(new Animation(12697));
				int damage = getRandomMaxHit(npc, 322, NPCCombatDefinitions.MAGE, target);
				delayHit(npc, 2, target, getRegularHit(npc, damage));
				if(damage == 0) {
					WorldTasksManager.schedule(new WorldTask() {
						@Override
						public void run() {
							target.setNextGraphics(new Graphics(85, 0, 100));
						}
					}, 1);
				}
				World.sendProjectile(npc, target, 1657, 30, 30, 75, 25, 0, 0);
			}
		}
		
		return defs.getAttackDelay();
	}

}
