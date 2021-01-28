package com.runescape.game.content;

import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.game.content.global.cannon.CannonAlgorithms;
import com.runescape.game.interaction.dialogues.impl.item.SimpleItemMessage;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleMessage;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.game.world.entity.player.achievements.AchievementHandler;
import com.runescape.game.world.entity.player.achievements.hard.Vine_Whippy;
import com.runescape.game.world.entity.player.actions.DFSCreateAction;
import com.runescape.game.world.item.Item;
import com.runescape.utility.world.item.KeyItemDefinitions;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Jun 17, 2015
 */
public class ItemOnTypeHandler implements KeyItemDefinitions {

	/**
	 * Handles the item on the other item
	 *
	 * @param player
	 * 		The player
	 * @param used
	 * 		The item used
	 * @param with
	 * 		The item used with
	 * @return {@code Boolean.TRUE} if successful
	 */
	public static boolean handleItemOnItem(Player player, Item used, Item with) {
		for (ItemTypes type : ItemTypes.values()) {
			for (int typeUse : type.used) {
				for (int typeWith : type.with) {
					if (typeUse == used.getId() && with.getId() == typeWith || (typeUse == with.getId() && typeWith == used.getId())) {
						type.useTogether(player, used.getId(), with.getId());
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Handles what to do when used together
	 *
	 * @param player
	 * 		The player
	 * @param object
	 * 		The object
	 * @param item
	 * 		The item
	 */
	public static boolean handleItemOnObject(Player player, WorldObject object, Item item) {
		for (ObjectTypes type : ObjectTypes.values()) {
			String objName = object.getDefinitions().name;
			if (objName.toLowerCase().equals(type.name.toLowerCase()) && type.itemId == item.getId()) {
				type.useTogether(player, object, item);
				return true;
			}
		}
		return false;
	}

	private enum ItemTypes {

		LOOP_KEYS(985, 987) {
			@Override
			public void useTogether(Player player, int used, int with) {
				player.getInventory().deleteItem(used, 1);
				player.getInventory().deleteItem(with, 1);
				player.getInventory().addItem(989, 1);
			}
		},

		STUDDED_BODY(1129, 2370) {
			@Override
			public void useTogether(Player player, int used, int with) {
				player.getInventory().deleteItem(used, 1);
				player.getInventory().deleteItem(with, 1);
				player.getInventory().addItem(1133, 1);
			}
		},

		STUDDED_CHAPS(1095, 2370) {
			@Override
			public void useTogether(Player player, int used, int with) {
				player.getInventory().deleteItem(used, 1);
				player.getInventory().deleteItem(with, 1);
				player.getInventory().addItem(1097, 1);
			}
		},

		MAGIC_IMBUENT(new int[] { 14061 }, new int[] { 6575, 6737, 6731, 6733, 6735 }) {
			@Override
			public void useTogether(Player player, int used, int with) {
				int imbued = getImbuedRing(with);
				if (imbued == -1) {
					return;
				}
				player.getInventory().deleteItem(with, 1);
				player.getInventory().deleteItem(used, 1);
				player.getInventory().addItem(imbued, 1);
				player.getDialogueManager().startDialogue(SimpleItemMessage.class, imbued, "Your ring beams with light and power, it must be magic...");
			}

			public int getImbuedRing(int baseRing) {
				switch (baseRing) {
					case 6575:
						return 15017;
					case 6737:
						return 15220;
					case 6731:
						return 15018;
					case 6733:
						return 15019;
					case 6735:
						return 15020;
				}
				return -1;
			}

		},

		DRAGON_PLATEBODY(new int[] { 14472, 14474, 14476 }, new int[] { 14472, 14474, 14476 }) {
			@Override
			public void useTogether(Player player, int used, int with) {
				int[] items = new int[] { 14472, 14474, 14476 };
				if (player.getInventory().containsItems(items, new int[] { 1, 1, 1})) {
					for (int item : items) {
						player.getInventory().deleteItem(item, 1);
					}
					player.getInventory().addItem(14479, 1);
					player.getDialogueManager().startDialogue(SimpleItemMessage.class, 14479, "You put all the pieces together and make", "a brand new dragon platebody.");
				} else {
					player.getDialogueManager().startDialogue(SimpleItemMessage.class, 14479, "You don't have all the items required to make a dragon platebody.");
				}
			}

		},

		DRAGON_SQ(2366, 2368) {
			@Override
			public void useTogether(Player player, int used, int with) {
				player.getInventory().deleteItem(used, 1);
				player.getInventory().deleteItem(with, 1);
				player.getInventory().addItem(1187, 1);
			}
		},

		VINE(21369, 4151) {
			@Override
			public void useTogether(Player player, int used, int with) {
				player.getInventory().deleteItem(used, 1);
				player.getInventory().deleteItem(with, 1);
				player.getInventory().addItem(21371, 1);
				AchievementHandler.incrementProgress(player, Vine_Whippy.class, 1);
			}
		},

		GODSWORD_MAKING(new int[] { ARMADYL_HILT, BANDOS_HILT, SARADOMIN_HILT, ZAMORAK_HILT }, new int[] { 11690 }) {
			@Override
			public void useTogether(Player player, int used, int with) {
				int hiltId = with == 11690 ? used : with;
				if (!player.getInventory().contains(hiltId)) {
					return;
				}
				player.getInventory().deleteItem(hiltId, 1);
				player.getInventory().deleteItem(11690, 1);
				player.getInventory().addItem(getSwordByHilt(hiltId));
			}
		},

		GODSWORD_BLADE_MAKING(new int[] { SHARD_1, SHARD_2, SHARD_3 }, new int[] { SHARD_1, SHARD_2, SHARD_3 }) {
			@Override
			public void useTogether(Player player, int used, int with) {
				if (!player.getInventory().containsItems(new int[] { SHARD_1, SHARD_2, SHARD_3 }, new int[] { 1, 1,
						                                                                                            1 })) {
					player.sendMessage("You don't have all the godsword shards necessary to make the blade");
					return;
				}
				player.getInventory().deleteItem(SHARD_1, 1);
				player.getInventory().deleteItem(SHARD_2, 1);
				player.getInventory().deleteItem(SHARD_3, 1);
				player.getInventory().addItem(11690, 1);
			}
		},

		BLESSED_MAKING(13754, 13734) {
			@Override
			public void useTogether(Player player, int used, int with) {
				player.getInventory().deleteItem(used, 1);
				player.getInventory().deleteItem(with, 1);
				player.getInventory().addItem(BLESSED_SPIRIT_SHIELD, 1);
			}
		},
		
		DOOGLE_SARDINE(1573, 327) {
			@Override
			public void useTogether(Player player, int used, int with) {
				player.getInventory().deleteItem(used, 1);
				player.getInventory().deleteItem(with, 1);
				player.getInventory().addItem(1552, 1);
			}
			
		},

		SPIRIT_SHIELD_MAKING(new int[] { ARCANE_SIGIL, DIVINE_SIGIL, ELYSIAN_SIGIL,
				                               SPECTRAL_SIGIL }, new int[] { BLESSED_SPIRIT_SHIELD }) {
			@Override
			public void useTogether(Player player, int used, int with) {
				int sigilId = (with == BLESSED_SPIRIT_SHIELD ? used : with);
				int shieldId = getShieldBySigil(sigilId);
				if (!player.getInventory().contains(sigilId)) {
					return;
				}
				if (player.getSkills().getLevelForXp(Skills.PRAYER) < 90) {
					player.sendMessage("You need a prayer level of 90 to create this spirit shield.");
					return;
				}
				if (player.getSkills().getLevelForXp(Skills.SMITHING) < 85) {
					player.sendMessage("You need a smithing level of 85 to create this sprit shield.");
					return;
				}
				player.getInventory().deleteItem(sigilId, 1);
				player.getInventory().deleteItem(BLESSED_SPIRIT_SHIELD, 1);
				player.getInventory().addItem(new Item(shieldId, 1));
				player.getDialogueManager().startDialogue(SimpleMessage.class, "You successfully create a " + ItemDefinitions.forId(shieldId).getName() + ".");
			}
		};

		ItemTypes(int[] used, int[] with) {
			this.used = used;
			this.with = with;
		}

		ItemTypes(int used, int with) {
			this.used = new int[] { used };
			this.with = new int[] { with };
		}

		private final int[] used;

		private final int[] with;

		public abstract void useTogether(Player player, int used, int with);
	}

	private enum ObjectTypes {

		CANNON("Dwarf Multicannon", 2) {
			@Override
			public void useTogether(Player player, WorldObject object, Item item) {
				if (object.getId() == 6) {
					CannonAlgorithms.addCannonBalls(player, object, item.getId());
				}
			}
		},


		DFS_MAKING("Anvil", 11286) {
			@Override
			public void useTogether(Player player, WorldObject object, Item item) {
				player.getActionManager().setAction(new DFSCreateAction());
			}
		};

		ObjectTypes(String name, int itemId) {
			this.name = name;
			this.itemId = itemId;
		}

		private final String name;

		private final int itemId;

		/**
		 * Handles what to do when used together
		 *
		 * @param player
		 * 		The player
		 * @param object
		 * 		The object
		 * @param item
		 * 		The item
		 */
		public abstract void useTogether(Player player, WorldObject object, Item item);
	}


	private static Item getSwordByHilt(int used) {
		switch (used) {
			case ARMADYL_HILT:
				return new Item(11694);
			case BANDOS_HILT:
				return new Item(11696);
			case SARADOMIN_HILT:
				return new Item(11698);
			case ZAMORAK_HILT:
				return new Item(11700);
		}
		return null;
	}

	/**
	 * Gets the id of the spirit shield by the sigil
	 *
	 * @param sigil
	 * 		The id of the sigil
	 */
	private static int getShieldBySigil(int sigil) {
		switch (sigil) {
			case ARCANE_SIGIL:
				return ARCANE_SPIRIT_SHIELD;
			case DIVINE_SIGIL:
				return DIVINE_SPIRIT_SHIELD;
			case ELYSIAN_SIGIL:
				return ELYSIAN_SPIRIT_SHIELD;
			case SPECTRAL_SIGIL:
				return SPECTRAL_SPIRIT_SHIELD;
		}
		return -1;
	}

}
