package com.runescape.game.world.entity.npc.others;

import com.runescape.game.content.global.minigames.warriors.WarriorsGuild;
import com.runescape.game.content.global.minigames.warriors.WarriorsGuildData.WarriorSet;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.combat.NPCCombatDefinitions;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

public class AnimatedArmor extends NPC {

	/**
	 *
	 */
	private static final long serialVersionUID = -3490465937456559584L;
	private transient Player player;

	public AnimatedArmor(Player player, int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
		this.player = player;
	}

	@Override
	public void processNPC() {
		super.processNPC();
		if (!getCombat().underCombat()) {
			finish();
		}
	}

	@Override
	public void sendDeath(final Entity source) {
		final NPCCombatDefinitions defs = getCombatDefinitions();
		resetWalkSteps();
		getCombat().removeTarget();
		setNextAnimation(null);
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					setNextAnimation(new Animation(defs.getDeathEmote()));
				} else if (loop >= defs.getDeathDelay()) {
					givePoints(source);
					finish();
					stop();
				}
				loop++;
			}
		}, 0, 1);
	}

	private void givePoints(Entity source) {
		if (source instanceof Player) {
			Player player = (Player) source;
			for (Integer items : getDroppedItems()) {
				if (items == -1) {
					continue;
				}
				World.addGroundItem(new Item(items), new WorldTile(getCoordFaceX(getSize()), getCoordFaceY(getSize()), getPlane()), player, true, 60);
			}
			player.setWarriorPoints(3, WarriorsGuild.ARMOR_POINTS[getId() - 4278]);
		}
	}

	public int[] getDroppedItems() {
		return WarriorSet.values()[getId() - 4278].getArmourIds();
	}

	@Override
	public void finish() {
		if (hasFinished()) {
			return;
		}
		super.finish();
		if (player != null) {
			player.removeAttribute("animator_spawned");
			if (!isDead()) {
				for (int item : getDroppedItems()) {
					if (item == -1) {
						continue;
					}
					player.getInventory().addItemDrop(item, 1);
				}
			}
		}
	}
}
