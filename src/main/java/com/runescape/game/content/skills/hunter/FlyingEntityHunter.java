package com.runescape.game.content.skills.hunter;

import com.runescape.game.world.World;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.ForceTalk;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.masks.Hit;
import com.runescape.game.world.entity.masks.Hit.HitLook;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.game.world.item.Item;
import com.runescape.utility.Utils;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

import java.util.HashMap;
import java.util.Map;

public class FlyingEntityHunter {

	public static final Animation CAPTURE_ANIMATION = new Animation(6606);

	public static final Item[] CHARMS = { new Item(12158, 1) /* Gold charm */, new Item(12159, 1) /*
																* Green
																* charm
																*/, new Item(12160, 1) /*
																						* Crimson
																						* charm
																						*/, new Item(12163, 1) /*
																												* Blue
																												* charm
																												*/ };

	public static void captureFlyingEntity(final Player player, final NPC npc) {
		final String name = npc.getDefinitions().getName().toUpperCase();
		final FlyingEntities instance = FlyingEntities.forNPCId(npc.getId());
		if (instance == null) { return; }
		final boolean isImpling = name.toLowerCase().contains("impling");
		if (!player.getInventory().containsItem(isImpling ? 11260 : 10012, 1)) {
			player.getPackets().sendGameMessage("You don't have an empty " + (isImpling ? "impling jar" : "butterfly jar") + " in which to keep " + (isImpling ? "an impling" : "a butterfly") + ".");
			return;
		}
		int weaponId = player.getEquipment().getWeaponId();
		if (weaponId != 11259 && weaponId != 10010 && isImpling) {
			player.getPackets().sendGameMessage("You need to have a butterfly net equipped in order to capture an impling.");
			return;
		}
		if (player.getSkills().getLevel(Skills.HUNTER) < instance.getLevel()) {
			player.getPackets().sendGameMessage("You need a hunter level of " + instance.getLevel() + " to capture a " + name.toLowerCase() + ".");
			return;
		}
		player.getLockManagement().lockAll(2);
		player.getPackets().sendGameMessage("You swing your net...");
		player.setNextAnimation(CAPTURE_ANIMATION);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				if (isSuccessful(player, instance.getLevel(), player1 -> {
					if (player1.getEquipment().getItem(3).getId() == 11259) {
						return 3;// magic net
					} else if (player1.getEquipment().getItem(3).getId() == 10010) {
						return 2;// regular net
					}
					return 1;// hands
				})) {
					if (player.getInventory().addItem(new Item(instance.getReward(), 1))) {
						player.getInventory().deleteItem(new Item(11260, 1));
						player.getSkills().addXp(Skills.HUNTER, instance.getPuroExperience());
						npc.setRespawnTask(); //sets loc and finishes auto
						player.getPackets().sendGameMessage("You manage to catch the " + name.toLowerCase() + " and squeeze it into a jar.");
						return;
					}
				} else {
					if (isImpling) {
						npc.setNextForceTalk(new ForceTalk("Tehee, you missed me!"));
						WorldTasksManager.schedule(new WorldTask() {
							@Override
							public void run() {
								WorldTile teleTile = npc;
								for (int trycount = 0; trycount < 10; trycount++) {
									teleTile = new WorldTile(npc, 3);
									if (World.isTileFree(npc.getPlane(), teleTile.getX(), teleTile.getY(), player.getSize())) {
										break;
									}
									teleTile = npc;
								}
								npc.setNextWorldTile(teleTile);
							}
						}, 2);
					}
					player.getPackets().sendGameMessage("...you stumble and miss the " + name.toLowerCase());
				}
			}
		});
	}

	public static boolean isSuccessful(Player player, int dataLevel, DynamicFormula formula) {
/*	int hunterlevel = player.getSkills().getLevel(Skills.HUNTER);
	int increasedProbability = formula == null ? 1 : formula.getExtraProbablity(player);
	int level = Utils.random(hunterlevel + increasedProbability) + 1;

	int chance = level * 100 / (dataLevel * 2);

	if (Utils.random(100) > chance)
	    return false;*/

		return Utils.random(4) != 0;
	}

	public static void openJar(Player player, FlyingEntities instance, int slot) {
		boolean isImpling = instance.toString().toLowerCase().contains("impling");
		if (isImpling) {
			double chance = Utils.randomDouble() * 100;
			Item item = null;
			Item[] list = null;
			if (chance <= 60) { list = instance.getRarleyCommon(); } else if (chance <= 90) {
				list = instance.getCommon();
			} else if (chance <= 99.8) {
				list = instance.getRare();
			} else
			//3% of extreme rare
			{ list = instance.getExtremelyRare(); }
			if (list != null) { item = list[Utils.random(list.length)]; }
			if (item == null) { return; }
			player.getInventory().getItem(slot).setId(isImpling ? 11260 : 11);
			player.getInventory().refresh(slot);
			if (item != null) { player.getInventory().addItemDrop(item.getId(), Utils.random(item.getAmount()) + 1); }

		}
		if (instance != null) { instance.effect(player); }
		if (Utils.random(4) == 0) {
			player.getInventory().deleteItem(new Item(isImpling ? 11260 : 11));
			player.getPackets().sendGameMessage("You press too hard on the jar and the glass shatters in your hands.");
			player.applyHit(new Hit(player, 10, HitLook.REGULAR_DAMAGE));
		}
	}

	public enum FlyingEntities {

		BABY_IMPLING(1028, 11238, 20, 25, 17, new Item[] { new Item(946, 1), new Item(1755, 1), new Item(1734, 1), new Item(1733, 1), new Item(2347, 1), new Item(1985, 1) }, new Item[] { new Item(1927, 1), new Item(319, 1), new Item(2007, 1), new Item(1779, 1), new Item(7170, 1), new Item(401, 1), new Item(1438, 1) }, new Item[] { new Item(2355), new Item(1607), new Item(1743), new Item(379), new Item(1761) }, null),

		YOUNG_IMPLING(1029, 11240, 48, 65, 22, new Item[] { new Item(361, 1), new Item(1901, 1), new Item(1539, 5), new Item(1784, 4), new Item(1523, 1), new Item(7936, 1), new Item(5970, 1) }, new Item[] { new Item(855, 1), new Item(1353, 1), new Item(2293, 1), new Item(7178, 1), new Item(247, 1), new Item(453, 1), new Item(1777, 1), new Item(231, 1), new Item(2347, 1) }, new Item[] { new Item(1097, 1), new Item(1157, 1), new Item(8778, 1), new Item(133, 1), new Item(2359, 1), }, null),

		GOURMET_IMPLING(1030, 11242, 82, 113, 28, new Item[] { new Item(361, 1), new Item(365, 1), new Item(1897, 1), new Item(2007, 1), new Item(2011, 1), new Item(2293, 1), new Item(2327, 1), new Item(5970, 1) }, new Item[] { new Item(247, 1), new Item(379, 1), new Item(385, 1), new Item(1883, 1), new Item(1885, 1), new Item(5755, 1), new Item(6969, 1), new Item(7170, 1), new Item(7178, 1), new Item(7188, 1), new Item(7754, 1), new Item(8244, 1), new Item(8526, 1) }, new Item[] { new Item(373, 1), new Item(5406, 1), new Item(5755, 1), new Item(7170, 1), new Item(7178, 1), new Item(7178, 1), new Item(7188, 1), new Item(7754, 1), new Item(8244, 1), new Item(8526, 1), new Item(10136, 1), new Item(14831, 1), new Item(10137, 5) }, null),

		EARTH_IMPLING(1031, 11244, 126, 177, 36, new Item[] { new Item(444, 1), new Item(557, 32), new Item(1441, 6), new Item(1442, 1), new Item(2353, 1), new Item(5104, 1), new Item(5535, 1), new Item(6032, 1), new Item(20904, 1), new Item(20914, 1), new Item(20942, 1), new Item(24216, 1), new Item(24767, 1) }, new Item[] { new Item(237, 1), new Item(447, 1), new Item(1273, 1), new Item(454, 6), new Item(1487, 1), new Item(5311, 2), new Item(5294, 2) }, new Item[] { new Item(448, 1), new Item(1606, 2), new Item(6035, 2), }, new Item[] { new Item(1603, 1), new Item(5303, 1) }),

		ESSENCE_IMPLING(1032, 11246, 160, 225, 42, new Item[] { new Item(562, 4), new Item(554, 50) /*
												* Fire
												* rune
												*/, new Item(555, 30) /*
																		* Water
																		* rune
																		*/, new Item(556, 60) /*
																								* Air
																								* rune
																								*/, new Item(559, 30) /*
																														* Body
																														* rune
																														*/, new Item(1448, 1), new Item(7937, 20) }, new Item[] { new Item(564, 4), new Item(4694, 4) /*
												* Steam
												* rune
												*/, new Item(4696, 4) /*
																		* Dust
																		* rune
																		*/, new Item(4698, 4), }, new Item[] { new Item(560, 13) /*
							* Death
							* rune
							*/, new Item(561, 13) /*
													* Nature
													* rune
													*/, new Item(563, 13) /*
																			* Law
																			* rune
																			*/, new Item(566, 13), new Item(1442, 1) }, null),

		ECLECTIC_IMPLING(1033, 11248, 205, 289, 50, new Item[] { new Item(1273, 1) /*
							* Mithril
							* pickaxe
							*/, new Item(5970, 1), new Item(231, 1) /*
																	* Snape
																	* grass
																	*/, new Item(556, 41) /*
																							* Air
																							* rune
																							*/, new Item(8779, 4) /*
																													* Oak
																													* plank
																													*/, new Item(12111, 1) }, new Item[] { new Item(2358, 5) /*
							* Gold
							* bar
							*/, new Item(444, 1) /*
													* Gold
													* ore
													*/, new Item(4527, 1), new Item(237, 1) /*
																							* Unicorn
																							* horn
																							*/, new Item(7937, 25) /*
																													* Pure
																													* essence
																													*/, new Item(1199, 1) /*
																																			* Adamant
																																			* kiteshield
																																			*/, new Item(2349, 1) /*
																																									* Bronze
																																									* bar
																																									*/, new Item(2351, 1) /*
																																															* Iron
																																															* bar
																																															*/, new Item(2353, 1), new Item(1199, 1) /*
													* Adamant
													* kiteshield
													*/ }, new Item[] { new Item(2493, 1) /*
							* Blue
							* d
							* 'hide
							* chaps
							*/, new Item(10083, 1) /*
													* Red
													* spiky
													* vambraces
													*/, new Item(1213, 1) /*
																			* Rune
																			* dagger
																			*/, new Item(450, 10) /*
																									* Adamantite
																									* ore
																									*/, new Item(7208, 1) /*
																															* Wild
																															* pie
																															*/, new Item(5760, 2), new Item(5321, 3) /*
																																										* Watermelon
																																										* seed
																																										*/, }, new Item[] { new Item(1391, 1), new Item(1601, 1) }),

		SPIRIT_IMPLING(7866, 15513, 227, 321, 54, new Item[] { new Item(12155, 25), new Item(12109, 1) /*
													* Cockatrice
													* egg
													*/, new Item(12113, 1) /*
																			* Saratrice
																			* egg
																			*/, new Item(12121, 1) /*
																									* Vulatrice
																									* egg
																									*/, new Item(12115, 1) /*
																															* Zamatrice
																															* egg
																															*/, new Item(12117, 1) /*
																																					* Pengatrice
																																					* egg
																																					*/, new Item(12111, 1) /*
																																											* Guthatrice
																																											* egg
																																											*/, new Item(2350, 6) /*
																																																	* Bronze
																																																	* bar
																																																	*/, new Item(2351, 5) /*
								* Iron
								* bar
								*/, new Item(2354, 2) /*
														* Steel
														* bar
														*/, new Item(2359, 1), new Item(2361, 1) /*
																									* Adamant
																									* bar
																									*/, new Item(2363, 1) /*
																															* Rune
																															* bar
																															*/, new Item(1115, 1), new Item(1120, 2)
			/* Steel platebody */, new Item(1636, 10), new Item(2135, 25) /*
																			* Raw rat
																			* meat
																			*/, new Item(2139, 25), new Item(9979, 15) /*
																														* Raw
																														* bird
																														* meat
																														*/, new Item(3363, 5), new Item(10095, 1) /*
																																									* Larupia
																																									* fur
																																									*/, new Item(10103, 1), new Item(10819, 7) /*
								* Yak
								* -
								* hide
								*/, new Item(6155, 1), new Item(7939, 1) /*
																			* Tortoise
																			* shell
																			*/, new Item(6291, 1), new Item(6319, 1) /* Proboscis */, new Item(2860, 3), new Item(237, 1) /*
																																											* Unicorn
																																											* horn
																																											*/, new Item(10149, 1), new Item(2151, 5) /*
								* Swamp
								* toad
								*/, new Item(12156, 1) /* Honeycomb */, new Item(1934, 14), new Item(6033, 12) /* Compost */, new Item(6010, 1) /* Marigolds */, new Item(12148, 3), new Item(12134, 1) /*
								* Evil
								* turnip
								*/, new Item(12153, 1), new Item(1520, 65) /*
																			* Willow
																			* logs
																			*/, new Item(5934, 2), new Item(1964, 25) /* Banana */, new Item(8431, 2), new Item(3138, 1) /*
																																											* Potato
																																											* cactus
																																											*/, new Item(2462, 1), new Item(249, 4) /*
								* Clean
								* guam
								*/, new Item(951, 18) /* Tinderbox */, new Item(311, 14) /* Harpoon */, new Item(6980, 5), new Item(1442, 1), new Item(1438, 1) /*
																																									* Air
																																									* talisman
																																									*/, new Item(572, 2) /*
																																															* Water
																																															* orb
																																															*/, new Item(9737, 2), new Item(9976, 4) /* Chinchompa */, new Item(10117, 1), new Item(383, 1) /*
																											* Raw
																											* shark
																											*/, new Item(1444, 1), new Item(1444, 1) /*
																																						* Water
																																						* talisman
																																						*/, }, null, null, null) {
			@Override
			public void effect(Player player) {
				if (Utils.random(2) == 0) {
					Item charm = CHARMS[Utils.random(CHARMS.length)];
					int charmAmount = Utils.random(charm.getAmount());
					player.getDialogueManager().startDialogue("ItemMessage", "The impling was carrying a" + charm.getName().toLowerCase() + ".", charm.getId());
					player.getInventory().addItemDrop(charm.getId(), charmAmount);
				}
			}
		},

		NATURE_IMPLING(1034, 11250, 250, 353, 58, new Item[] { new Item(5100, 1) /*
							* Limpwurt
							* seed
							*/, new Item(5104, 1), new Item(5281, 1) /*
																		* Belladonna
																		* seed
																		*/, new Item(5294, 1), new Item(6016, 1) /*
																													* Cactus
																													* spine
																													*/, new Item(1513, 1), new Item(253, 4) /*
																																							* Clean
																																							* tarromin
																																							*/, }, new Item[] { new Item(5298, 5) /*
							* Avantoe
							* seed
							*/, new Item(5299, 1), new Item(5297, 1) /*
																		* Irit
																		* seed
																		*/, new Item(3051, 1), new Item(5285, 1) /*
																													* Orange
																													* tree
																													* seed
																													*/, new Item(5286, 1), new Item(5313, 1) /*
																																								* Willow
																																								* seed
																																								*/, new Item(5974, 1) /* Coconut */, }, new Item[] { new Item(5304, 1) /*
							* Torstol
							* seed
							*/, new Item(5295, 1) }, new Item[] { new Item(5303, 1) /*
							* Dwarf
							* weed
							* seed
							*/, new Item(219, 1) /*
													* Grimy
													* torstol
													*/, new Item(269, 1) }),

		MAGPIE_IMPLING(1035, 11252, 289, 409, 65, new Item[] { new Item(1682, 3) /*
							* Diamond
							* amulet
							*/, new Item(1732, 3), new Item(2569, 3) /*
																		* Ring
																		* of
																		* forging
																		*/, new Item(3391, 1), new Item(5541, 1) /*
																													* Nature
																													* tiara
																													*/, new Item(1748, 6), }, new Item[] { new Item(1333, 1) /*
							* Rune
							* scimitar
							*/, new Item(1347, 1) /*
													* Rune
													* warhammer
													*/, new Item(2571, 5) /*
																			* Ring
																			* of
																			* life
																			*/, new Item(4097, 1) /*
																									* Mystic
																									* boots
																									*/, new Item(4095, 1) /*
																															* Mystic
																															* gloves
																															*/, new Item(2364, 2) /*
																																					* Rune
																																					* bar
																																					*/, new Item(1603, 1) /* Ruby */, }, new Item[] { new Item(1215, 1) /*
							* Dragon
							* dagger
							*/, new Item(1185, 1) /*
													* Rune
													* sq
													* shield
													*/, new Item(1601, 1) /* Diamond */, new Item(5287, 1), new Item(987, 1) /*
																																* Loop
																																* half
																																* of
																																* a
																																* key
																																*/, new Item(985, 1) /*
																																						* Tooth
																																						* half
																																						* of
																																						* a
																																						* key
																																						*/, }, new Item[] { new Item(5300, 1) /*
							* Snapdragon
							* seed
							*/, new Item(12121, 1), new Item(993, 1) }),

		NINJA_IMPLING(6053, 11254, 339, 481, 74, new Item[] { new Item(6328, 1) /*
							* Snakeskin
							* boots
							*/, new Item(3385, 1), new Item(3391, 1) /*
																		* Splitbark
																		* gauntlets
																		*/, new Item(4097, 1) /*
																								* Mystic
																								* boots
																								*/, new Item(4095, 1), new Item(3101, 1) /*
																																			* Rune
																																			* claws
																																			*/, new Item(1333, 1), new Item(1347, 1) /*
																																														* Rune
																																														* warhammer
																																														*/, new Item(1215, 1), new Item(6313, 1) /*
													* Opal
													* machete
													*/, new Item(892, 70), new Item(811, 70) /*
																								* Rune
																								* dart
																								*/, new Item(868, 40), new Item(1747, 16), new Item(140, 4), new Item(805, 40), new Item(25496, 4), }, new Item[] { new Item(9342, 1), new Item(6155, 1) /*
												* Dagannoth
												* hide
												*/ }, new Item[] { new Item(2363, 1) /*
							* Rune
							* bar
							*/, new Item(9194, 1) }, null),

		KINGLY_IMPLING(7903, 15517, 434, 617, 91, new Item[] { new Item(1705, Utils.random(3, 11)), new Item(1684, 3), new Item(1618, Utils.random(17, 34)), new Item(990, 2) }, new Item[] { new Item(1631, 1), new Item(1615, 1), new Item(9341, 40 + Utils.random(30)), new Item(9342, 57), new Item(15511, 1), new Item(15509, 1), new Item(15505, 1), new Item(15507, 1), new Item(15503, 1), new Item(11212, 40 + Utils.random(104)), new Item(9193, 62 + Utils.random(8)), new Item(11230, Utils.random(182, 319)), new Item(11232, 70), new Item(1306, Utils.random(1, 2)), new Item(1249, 1) }, new Item[] { new Item(9194, Utils.random(1, 71)) }, new Item[] { new Item(7158, 1), new Item(2366, 1), new Item(6571, 1) }),

		;

		static final Map<Short, FlyingEntities> flyingEntities = new HashMap<>();

		static {
			for (FlyingEntities impling : FlyingEntities.values()) {
				flyingEntities.put((short) impling.reward, impling);
			}
		}

		private int npcId, level, reward;

		private double puroExperience, rsExperience;

		private Item[] rarleyCommon, common, rare, extremelyRare;

		private Graphics graphics;

		FlyingEntities(int npcId, int reward, double puroExperience, double rsExperience, int level, Graphics graphics, Item[] rarleyCommon, Item[] common, Item[] rare, Item[] extremelyRare) {
			this.npcId = npcId;
			this.reward = reward;
			this.puroExperience = puroExperience;
			this.rsExperience = rsExperience;
			this.level = level;
			this.rarleyCommon = rarleyCommon;
			this.common = common;
			this.rare = rare;
			this.extremelyRare = extremelyRare;
			this.graphics = graphics;
		}

		FlyingEntities(int npcId, int reward, double puroExperience, double rsExperience, int level, Item[] rarleyCommon, Item[] common, Item[] rare, Item[] extremelyRare) {
			this.npcId = npcId;
			this.reward = reward;
			this.puroExperience = puroExperience;
			this.rsExperience = rsExperience;
			this.level = level;
			this.rarleyCommon = rarleyCommon;
			this.common = common;
			this.rare = rare;
			this.extremelyRare = extremelyRare;
		}

		public static FlyingEntities forItem(short reward) {
			return flyingEntities.get(reward);
		}

		public static FlyingEntities forId(int itemId) {
			for (FlyingEntities entity : FlyingEntities.values()) {
				if (itemId == entity.getReward()) { return entity; }
			}
			return null;
		}

		public int getReward() {
			return reward;
		}

		public static FlyingEntities forNPCId(int npcId) {
			for (FlyingEntities entity : FlyingEntities.values()) {
				if (npcId == entity.getNpcId()) { return entity; }
			}
			return null;
		}

		public int getNpcId() {
			return npcId;
		}

		public int getLevel() {
			return level;
		}

		public double getPuroExperience() {
			return puroExperience;
		}

		public double getRsExperience() {
			return rsExperience;
		}

		public Item[] getRarleyCommon() {
			return rarleyCommon;
		}

		public Item[] getCommon() {
			return common;
		}

		public Item[] getRare() {
			return rare;
		}

		public Item[] getExtremelyRare() {
			return extremelyRare;
		}

		public Graphics getGraphics() {
			return graphics;
		}

		public void effect(Player player) {

		}
	}

	public interface DynamicFormula {

		int getExtraProbablity(Player player);

	}
}