package com.runescape.game.world.entity.npc.godwars.bandos;

import com.runescape.game.interaction.controllers.impl.GodWars;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.combat.NPCCombatDefinitions;
import com.runescape.game.world.entity.npc.godwars.Bosses;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.utility.Utils;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class BandosFaction extends NPC {

	public BandosFaction(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setBonuses(new int[] { 150, 150, 150, 150, 150, 80, 80, 80, 80, 80 });
	}

	@Override
	public void drop() {
		super.drop();
		Player killer = getMostDamageReceivedSourcePlayer();
		if (killer == null) {
			return;
		}
		killer.getControllerManager().verifyControlerForOperation(GodWars.class).ifPresent(c -> {
			killer.getFacade().increaseKillCount(Bosses.BANDOS, killer.isAnyDonator() ? 2 : 1);
			c.updateInterface();
		});
	}

	@Override
	public boolean checkAggressivity() {
		NPCCombatDefinitions defs = getCombatDefinitions();
		if (defs.getAggressivenessType() == NPCCombatDefinitions.PASSIVE)
			return false;
		ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
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
					if (npc == null || npc == this || npc instanceof BandosFaction || npc.isDead() || npc.hasFinished() || !npc.withinDistance(this, getCombatDefinitions().getAttackStyle() == NPCCombatDefinitions.MELEE ? 4 : getCombatDefinitions().getAttackStyle() == NPCCombatDefinitions.SPECIAL ? 16 : 8) || !npc.getDefinitions().hasAttackOption() || ((!isAtMultiArea() || !npc.isAtMultiArea()) && npc.getAttackedBy() != this && npc.getAttackedByDelay() > Utils.currentTimeMillis()) || !clipedProjectile(npc, false))
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
			if (name.contains("Bandos mitre") || name.contains("Bandos Full helm") || name.contains("Bandos coif") || name.contains("Torva full helm") || name.contains("Pernix cowl") || name.contains("Vitus mask"))
				return true;
			else if (name.contains("Bandos cloak"))
				return true;
			else if (name.contains("Bandos stole"))
				return true;
			else if (name.contains("Ancient mace") || name.contains("Granite mace") || name.contains("Bandos godsword") || name.contains("Bandos crozier") || name.contains("Zaryte Bow"))
				return true;
			else if (name.contains("Bandos body") || name.contains("Bandos robe top") || name.contains("Bandos chestplate") || name.contains("Bandos platebody") || name.contains("Torva platebody") || name.contains("Pernix body") || name.contains("Virtus robe top"))
				return true;
			else if (name.contains("Illuminated book of war") || name.contains("Book of war") || name.contains("Bandos kiteshield"))
				return true;
			else if (name.contains("Bandos robe legs") || name.contains("Bandos tassets") || name.contains("Bandos chaps") || name.contains("Bandos platelegs") || name.contains("Bandos plateskirt") || name.contains("Torva platelegs") || name.contains("Pernix chaps") || name.contains("Virtus robe legs"))
				return true;
			else if (name.contains("Bandos vambraces"))
				return true;
			else if (name.contains("Bandos boots"))
				return true;
		}
		return false;
	}
}
