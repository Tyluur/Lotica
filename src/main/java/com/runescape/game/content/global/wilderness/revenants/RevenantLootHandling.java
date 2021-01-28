package com.runescape.game.content.global.wilderness.revenants;

import com.runescape.game.interaction.controllers.impl.Wilderness;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/26/2015
 */
public class RevenantLootHandling {

	/**
	 * Handles the death of npcs which should drop revenants
	 *
	 * @param npc
	 * 		The npc
	 * @param killer
	 * 		The killer of the npc
	 */
	public static void handleNPCDeath(NPC npc, Player killer) {
		boolean isRevenant = npc.getDefinitions().getName().contains("Revenant");
		if (isRevenant && Utils.percentageChance(30)) {
			double chance = Utils.random(300);

			// transforming the revenants to a list for easy modification
			List<RevenantLootTables> artefacts = new ArrayList<>(Arrays.asList(RevenantLootTables.values()));

			// shuffling the revenants so you dont always get them in the same order
			Collections.shuffle(artefacts);

			RevenantLootTables lootTable = null;

			// checking if we should drop an artefact
			for (RevenantLootTables artefact : artefacts) {
				if (chance <= artefact.getChanceDrop()) {
					lootTable = artefact;
					break;
				}
			}

			// if we weren't lucky enough
			if (lootTable == null) {
				return;
			}

			// giving the player the artefact wilderness points directly
			killer.getInventory().addItem(Wilderness.WILDERNESS_TOKEN, lootTable.getWildernessPointReward());
			killer.sendMessage("Lucky you - the revenant gives you " + lootTable.getWildernessPointReward() + " wilderness points for your kill.");
		}
	}

	public enum RevenantLootTables {

		ANCIENT(300, 1.5D),
		SEREN(150, 5D),
		ARMADYL(125, 5D),
		ZAMORAK(120, 5D),
		SARADOMIN(115, 7.5D),
		BANDOS(110, 7.5D),
		RUBY_CHALICE(100, 7.5D),
		GUTHIXIAN_BRAZIER(90, 10),
		ARMADYL_TOTEM(70, 10),
		ZAMORAK_MEDALLION(70, 10),
		SARADOMIN_CARVING(70, 10),
		BANDOS_SCRIMSHAW(70, 10),
		SARADOMIN_AMPHORA(50, 17.5D),
		ANCIENT_BRIDGE(50, 17.5D),
		BRONZED_DRAGON_CLAW(25, 17.5D),
		THIRD_AGE_CARAFE(25, 20),
		BROKEN_STATUE_HEADDRESS(25, 20);

		RevenantLootTables(int wildernessPointReward, double chanceDrop) {
			this.wildernessPointReward = wildernessPointReward;
			this.chanceDrop = chanceDrop;
		}

		/** The amount of wilderness points this loot gives */
		private final int wildernessPointReward;

		/** The chance the player has to receive this drop */
		private final double chanceDrop;

		/**
		 * Gets the chance for the drop
		 */
		public double getChanceDrop() {
			return chanceDrop;
		}

		/**
		 * Gets the point reward from this artefact
		 */
		public int getWildernessPointReward() {
			return wildernessPointReward;
		}

		/**
		 * Gets the item id of the artefact
		 */
		public int getItemId() {
			return 14876 + ordinal();
		}

	}
}
