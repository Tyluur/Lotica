package com.runescape.utility.external.gson.resource;

import com.runescape.cache.Cache;
import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.game.GameConstants;
import com.runescape.game.content.global.wilderness.WildernessBoss;
import com.runescape.game.world.entity.npc.Drop;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.combat.NPCCombatDefinitions;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;
import com.runescape.utility.external.gson.loaders.NPCDataLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 24, 2015
 */
public class NPCData {

	public static void main(String[] args) throws IOException {
		Cache.init();
		File location = new File(NPCDataLoader.DATA_LOCATION);
		for (File file : location.listFiles()) {
			String npcName = file.getName().replaceAll(".json", "");
			NPCData data = NPCDataLoader.getData(npcName);
			if (data == null) {
				continue;
			}
			List<Drop> drops = data.getDrops();
			boolean changed = false;
			for (Drop drop : drops) {
				ItemDefinitions def = ItemDefinitions.forId(drop.getItemId());
				if ((drop.getMinAmount() > 1 || drop.getMaxAmount() > 1) && !def.isStackable()) {
					if (def.getCertId() != -1) {
						System.out.println("Set the drop of " + drop + " to its noted form");
						drop.setItemId(def.getCertId());
					} else {
						System.out.println("Set the drop of " + drop + " to drop 1");
						drop.setMinAmount(1);
						drop.setMaxAmount(drop.getMinAmount());
					}
					changed = true;
				}
			}
			if (changed) {
				NPCDataLoader.saveData(npcName, data);
			}
		}
	}
	
	/**
	 * The drops for this npc
	 */
	private final List<Drop> drops;

	private final Map<Integer, int[]> bonuses = new HashMap<>();

	private final Map<Integer, NPCCombatDefinitions> combatDefinitions = new HashMap<>();

	private List<Drop> charmDrops;

	public NPCData(List<Drop> dropList) {
		this.drops = dropList;
	}
	
	public NPCData() {
		this.drops = new ArrayList<>();
	}

	/**
	 * @return the drops
	 */
	public List<Drop> getDrops() {
		return drops;
	}

	/**
	 * Generates a list of drops
	 *
	 * @param killer
	 * 		The killer
	 * @param drops
	 * 		The drops list
	 * @param npc
	 * 		The NPC
	 */
	public List<Drop> generateDrops(Player killer, List<Drop> drops, NPC npc) {
		List<Drop> possibleDrops = new ArrayList<>();
		boolean equippingROW = killer.getEquipment().getRingId() == 2572;
		boolean wildernessBoss = npc != null && npc instanceof WildernessBoss;
		int possibleDropsCount = 0;
		for (Drop drop : drops) {
			if (drop.getRate() == 100) {
				possibleDrops.add(drop);
			} else {
				// challengers have increased chance at loots
				// just like ironmen accounts
				double chance = 99;
				if (npc != null && npc.getName().toLowerCase().contains("revenant")) {
					chance = 200;
				}
				chance = chance * (equippingROW && killer.getFacade().getRowCharges() > 0 ? 0.95 : 1);
				chance = chance * (wildernessBoss ? 0.90 : 1);
				double random = Utils.getRandomDouble(chance) + 1;
				if (random <= drop.getRate() * GameConstants.DROP_CHANCE_MULTIPLIER) {
					possibleDropsCount++;
					possibleDrops.add(drop);
				}
			}
		}
		if (possibleDropsCount > 0) {
			Drop random = possibleDrops.get(Utils.random(possibleDrops.size()));
			possibleDrops = possibleDrops.stream().filter(p -> p.getRate() == 100).collect(Collectors.toList());
			possibleDrops.add(random);
		}
		return possibleDrops;
	}

	public List<Drop> getCharmDrops() {
		if (charmDrops == null) {
			charmDrops = new ArrayList<>();
		}
		return charmDrops;
	}

    public Map<Integer, int[]> getBonuses() {
        return this.bonuses;
    }

    public Map<Integer, NPCCombatDefinitions> getCombatDefinitions() {
        return this.combatDefinitions;
    }
}
