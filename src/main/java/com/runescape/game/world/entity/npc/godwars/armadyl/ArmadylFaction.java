package com.runescape.game.world.entity.npc.godwars.armadyl;

import com.runescape.game.interaction.controllers.impl.GodWars;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.combat.NPCCombatDefinitions;
import com.runescape.game.world.entity.npc.godwars.Bosses;
import com.runescape.game.world.entity.npc.godwars.zammorak.ZamorakFaction;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.utility.Utils;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class ArmadylFaction extends NPC {

	public ArmadylFaction(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
	}

	@Override
	public void drop() {
		super.drop();
		Player killer = getMostDamageReceivedSourcePlayer();
		if (killer == null) {
			return;
		}
		killer.getControllerManager().verifyControlerForOperation(GodWars.class).ifPresent(c -> {
			killer.getFacade().increaseKillCount(Bosses.ARMADYL, killer.isAnyDonator() ? 2 : 1);
			c.updateInterface();
		});
	}
	
	@Override
	public boolean checkAggressivity() {
		NPCCombatDefinitions defs = getCombatDefinitions();
		if (defs.getAggressivenessType() == NPCCombatDefinitions.PASSIVE)
			return false;
		ArrayList<Entity> possibleTarget = new ArrayList<>();
		for (int regionId : getMapRegionsIds()) {
			List<Integer> playerIndexes = World.getRegion(regionId).getPlayerIndexes();
			if (playerIndexes != null) {
				for (int npcIndex : playerIndexes) {
					Player player = World.getPlayers().get(npcIndex);
					if (player == null || player.isDead() || !player.isRunning() || !player.withinDistance(this, getCombatDefinitions().getAttackStyle() == NPCCombatDefinitions.MELEE ? 4 : getCombatDefinitions().getAttackStyle() == NPCCombatDefinitions.SPECIAL ? 16 : 8) || ((!isAtMultiArea() || !player.isAtMultiArea()) && player.getAttackedBy() != this && player.getAttackedByDelay() > Utils.currentTimeMillis()) || !clipedProjectile(player, false) || !hasGodItem(player))
						continue;
					possibleTarget.add(player);
				}
			}
			List<Integer> npcsIndexes = World.getRegion(regionId).getNPCsIndexes();
			if (npcsIndexes != null) {
				for (int npcIndex : npcsIndexes) {
					NPC npc = World.getNPCs().get(npcIndex);
					if (npc == null || npc == this || npc instanceof ZamorakFaction || npc.isDead() || npc.hasFinished() || !npc.withinDistance(this, getCombatDefinitions().getAttackStyle() == NPCCombatDefinitions.MELEE ? 4 : getCombatDefinitions().getAttackStyle() == NPCCombatDefinitions.SPECIAL ? 16 : 8) || !npc.getDefinitions().hasAttackOption() || ((!isAtMultiArea() || !npc.isAtMultiArea()) && npc.getAttackedBy() != this && npc.getAttackedByDelay() > Utils.currentTimeMillis()) || !clipedProjectile(npc, false))
						continue;
					possibleTarget.add(npc);
				}
			}
		}
		if (!possibleTarget.isEmpty()) {
			setTarget(possibleTarget.get(Utils.getRandom(possibleTarget.size() - 1)));
			return true;
		}
		return false;
	}

	private boolean hasGodItem(Player player) {
		for (Item item : player.getEquipment().getItems().getItems()) {
			if (item == null)
				continue; // shouldn't happen
			String name = item.getDefinitions().getName();
			// using else as only one item should count
			if (name.contains("Armadyl Helmet") || name.contains("Armadyl mitre") || name.contains("Armadyl full helm") || name.contains("Armadyl coif") || name.contains("Torva full helm") || name.contains("Pernix cowl") || name.contains("Virtus mask"))
				return true;
			else if (name.contains("Armadyl cloak"))
				return true;
			else if (name.contains("Armadyl pendant") || name.contains("Armadyl stole"))
				return true;
			else if (name.contains("Armadyl godsword") || name.contains("Armadyl crozier") || name.contains("Zaryte Bow"))
				return true;
			else if (name.contains("Armadyl body") || name.contains("Armadyl robe top") || name.contains("Armadyl chestplate") || name.contains("Armadyl platebody") || name.contains("Torva platebody") || name.contains("Pernix body") || name.contains("Virtus robe top"))
				return true;
			else if (name.contains("Illuminated book of law") || name.contains("Book of law") || name.contains("Armadyl kiteshield"))
				return true;
			else if (name.contains("Armadyl robe legs") || name.contains("Armadyl plateskirt") || name.contains("Armadyl chaps") || name.contains("Armadyl platelegs") || name.contains("Armadyl Chainskirt") || name.contains("Torva platelegs") || name.contains("Pernix chaps") || name.contains("Virtus robe legs"))
				return true;
			else if (name.contains("Armadyl vambraces"))
				return true;
		}
		return false;
	}
}
