package com.runescape.game.event.interaction.button;

import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.game.content.skills.Combat;
import com.runescape.game.content.skills.SkillCapeCustomizer;
import com.runescape.game.content.skills.magic.Magic;
import com.runescape.game.event.interaction.type.InterfaceInteractionEvent;
import com.runescape.game.interaction.dialogues.impl.item.Transportation;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.masks.Hit;
import com.runescape.game.world.entity.masks.Hit.HitLook;
import com.runescape.game.world.entity.player.Equipment;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.actions.PlayerCombat;
import com.runescape.game.world.item.Item;
import com.runescape.network.codec.decoders.WorldPacketsDecoder;
import com.runescape.network.codec.decoders.handlers.ButtonHandler;
import com.runescape.utility.Utils;
import com.runescape.utility.external.gson.loaders.ItemInformationLoader;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.runescape.network.codec.decoders.WorldPacketsDecoder.*;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/17/2016
 */
public class WornEquipmentInteractionEvent extends InterfaceInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] { 387, 667 };
	}

	@Override
	public boolean handleInterfaceInteraction(Player player, int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
		if (interfaceId == 667) {
			if (componentId != 7) {
				return true;
			}
			if (slotId > 14) { return true; }
			Item item = player.getEquipment().getItem(slotId);
			if (item == null) { return true; }
			if (packetId == WorldPacketsDecoder.ACTION_BUTTON8_PACKET) {
				player.getPackets().sendGameMessage(ItemInformationLoader.getExamine(item.getId()));
			} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
				ButtonHandler.sendRemove(player, slotId);
				ButtonHandler.refreshEquipBonuses(player);
			}
		} else {
			if (player.getInterfaceManager().containsInventoryInter()) {
				return true;
			}
			boolean opened = false;
			if (componentId == 39) {
				ButtonHandler.openEquipmentBonuses(player, false);
				opened = true;
			}
			if (componentId == 45) {
				player.stopAll();
				Map<Integer, List<Item>> items = player.getItemsOnDeath(player.findContainedItems());

				List<Item> keptItems = new ArrayList<>();
				keptItems.addAll(items.get(1));
				keptItems.addAll(items.get(2));

				int interfaceId1 = 478;
				player.getPackets().sendIComponentText(interfaceId1, 12, "Items Kept on Death");
				player.getPackets().sendIComponentText(interfaceId1, 13, "" + (player.isAnyDonator() ? "Untradeables are kept in your inventory as a donator." : "Untradeables shown here must be claimed from bankers."));

				// sends the items over the interface, key 93 = inventory
				player.getPackets().sendItems(93, keptItems.toArray(new Item[keptItems.size()]));
				player.getInterfaceManager().sendInterface(interfaceId1);

				// so the player doesn't see the 'fake' items in their inventory
				player.getInterfaceManager().sendInventoryInterface(303);

				// when the interface is closed, we can see the inventory again
				player.setCloseInterfacesEvent(() -> player.getInventory().init());
				opened = true;
			}
			if (componentId == 42) {
				if (player.getInterfaceManager().containsScreenInterface()) {
					player.getPackets().sendGameMessage("Please finish what you're doing before opening the price checker.");
					return true;
				}
				player.stopAll();
				player.getPriceCheckManager().openPriceCheck();
				opened = true;
			}
			if (opened) {
				return true;
			}
			Optional<SlotActions> optional = SlotActions.getSlotAction(componentId);
			if (!optional.isPresent()) {
				Thread.dumpStack();
				return true;
			}
			SlotActions action = optional.get();
			if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
				ButtonHandler.sendRemove(player, action.equipmentSlot);
			} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON8_PACKET) {
				player.getEquipment().sendExamine(action.equipmentSlot);
			} else {
				if (!action.handleOtherOption(player, slotId2, packetId)) {
					player.sendMessage("This action was not handled - please report it on forums.");
					return true;
				}
			}
			if (player.getInterfaceManager().containsInterface(667)) {
				ButtonHandler.refreshEquipBonuses(player);
			}
		}

		return true;
	}

	private enum SlotActions {
		HAT(8, Equipment.SLOT_HAT) {
			@Override
			public boolean handleOtherOption(Player player, int itemId, int packetId) {
				if (packetId == ACTION_BUTTON1_PACKET) {
					ButtonHandler.sendRemove(player, Equipment.SLOT_HAT);
					return true;
				} else if (packetId == ACTION_BUTTON8_PACKET) {
					player.getEquipment().sendExamine(Equipment.SLOT_HAT);
					return true;
				}
				return false;
			}
		},
		CAPE(11, Equipment.SLOT_CAPE) {
			@Override
			public boolean handleOtherOption(Player player, int itemId, int packetId) {
				if (packetId == ACTION_BUTTON5_PACKET) {
					int capeId = player.getEquipment().getCapeId();
					if (capeId == 20769 || capeId == 20771) {
						SkillCapeCustomizer.startCustomizing(player, capeId);
						return true;
					}
				} else if (packetId == ACTION_BUTTON2_PACKET) {
					int capeId = player.getEquipment().getCapeId();
					if (capeId == 20767) {
						SkillCapeCustomizer.startCustomizing(player, capeId);
						return true;
					}
				} else if (packetId == ACTION_BUTTON1_PACKET) {
					ButtonHandler.sendRemove(player, Equipment.SLOT_CAPE);
					return true;
				} else if (packetId == ACTION_BUTTON8_PACKET) {
					player.getEquipment().sendExamine(Equipment.SLOT_CAPE);
					return true;
				}
				return false;
			}
		},
		AMULET(14, Equipment.SLOT_AMULET) {
			@Override
			public boolean handleOtherOption(Player player, int itemId, int packetId) {
				if (packetId == ACTION_BUTTON2_PACKET) {
					int amuletId = player.getEquipment().getAmuletId();
					if (amuletId <= 1712 && amuletId >= 1706 || amuletId >= 10354 && amuletId <= 10361) {
						if (Magic.sendItemTeleportSpell(player, true, Transportation.EMOTE, Transportation.GFX, 4, new WorldTile(3087, 3496, 0))) {
							Item amulet = player.getEquipment().getItem(Equipment.SLOT_AMULET);
							if (amulet != null) {
								amulet.setId(amulet.getId() - 2);
								player.getEquipment().refresh(Equipment.SLOT_AMULET);
							}
						}
					} else if (amuletId == 1704 || amuletId == 10352) {
						player.getPackets().sendGameMessage("The amulet has ran out of charges. You need to recharge it if you wish it use it once more.");
					}
					return true;
				} else if (packetId == ACTION_BUTTON3_PACKET) {
					int amuletId = player.getEquipment().getAmuletId();
					if (amuletId <= 1712 && amuletId >= 1706 || amuletId >= 10354 && amuletId <= 10361) {
						if (Magic.sendItemTeleportSpell(player, true, Transportation.EMOTE, Transportation.GFX, 4, new WorldTile(2918, 3176, 0))) {
							Item amulet = player.getEquipment().getItem(Equipment.SLOT_AMULET);
							if (amulet != null) {
								amulet.setId(amulet.getId() - 2);
								player.getEquipment().refresh(Equipment.SLOT_AMULET);
							}
						}
					}
					return true;
				} else if (packetId == ACTION_BUTTON4_PACKET) {
					int amuletId = player.getEquipment().getAmuletId();
					if (amuletId <= 1712 && amuletId >= 1706 || amuletId >= 10354 && amuletId <= 10361) {
						if (Magic.sendItemTeleportSpell(player, true, Transportation.EMOTE, Transportation.GFX, 4, new WorldTile(3105, 3251, 0))) {
							Item amulet = player.getEquipment().getItem(Equipment.SLOT_AMULET);
							if (amulet != null) {
								amulet.setId(amulet.getId() - 2);
								player.getEquipment().refresh(Equipment.SLOT_AMULET);
							}
						}
					}
					return true;
				} else if (packetId == ACTION_BUTTON5_PACKET) {
					int amuletId = player.getEquipment().getAmuletId();
					if (amuletId <= 1712 && amuletId >= 1706 || amuletId >= 10354 && amuletId <= 10361) {
						if (Magic.sendItemTeleportSpell(player, true, Transportation.EMOTE, Transportation.GFX, 4, new WorldTile(3293, 3163, 0))) {
							Item amulet = player.getEquipment().getItem(Equipment.SLOT_AMULET);
							if (amulet != null) {
								amulet.setId(amulet.getId() - 2);
								player.getEquipment().refresh(Equipment.SLOT_AMULET);
							}
						}
					}
					return true;
				}
				return false;
			}
		},
		ARROWS(38, Equipment.SLOT_ARROWS),
		WEAPON(17, Equipment.SLOT_WEAPON) {
			@Override
			public boolean handleOtherOption(Player player, int itemId, int packetId) {
				if (packetId == ACTION_BUTTON2_PACKET) {
					int weaponId = player.getEquipment().getWeaponId();
					if (weaponId == 15484) {
						player.getInterfaceManager().gazeOrbOfOculus();
					} else if (ItemDefinitions.getItemDefinitions(itemId).containsOption(2, "Check-charges")) {
						Item item = player.getEquipment().getItem(Equipment.SLOT_WEAPON);
						if (item == null) {
							return true;
						}
						player.getDegradeManager().sendTimeLeft(item);
					}
					return true;
				}
				return false;
			}
		},
		CHEST(20, Equipment.SLOT_CHEST),
		SHIELD(23, Equipment.SLOT_SHIELD) {
			@Override
			public boolean handleOtherOption(Player player, int itemId, int packetId) {
				if (packetId == ACTION_BUTTON2_PACKET) {
					if (itemId == 11284) {
						if (player.getFacade().getDragonFireCharges() > 0) {
							Entity target = null;
							if (player.getActionManager().getAction() instanceof PlayerCombat) {
								target = ((PlayerCombat) player.getActionManager().getAction()).getTarget();
							}
							if (target == null) {
								return true;
							}
							if (!Utils.timeHasPassed(player.getAttribute("last_dfs_time_used", -1L), TimeUnit.SECONDS.toMillis(30))) {
								player.sendMessage("You can only use the shields effect every 30 seconds.");
								return true;
							}
							player.setNextGraphics(new Graphics(1165));
							player.setNextAnimation(new Animation(6696));
							int delay = 2;
							if (target instanceof Player) {
								Player p2 = target.player();
								if (p2.getFireImmune() >= Utils.currentTimeMillis()) {
									p2.sendMessage("Your antifire potion absorbs most of the firey attack!");
									World.sendProjectile(player, p2, 1166, 41, 16, 31, 35, 16, 0);
									p2.applyDelayedHit(new Hit(player, Utils.random(100), HitLook.REGULAR_DAMAGE), delay);
									return true;
								} else if (Combat.hasAntiDragProtection(p2)) {
									p2.sendMessage("Your Dragonfire shield absorbs most of the firey attack!");
									World.sendProjectile(player, p2, 1166, 41, 16, 31, 35, 16, 0);
									p2.applyDelayedHit(new Hit(player, Utils.random(100), HitLook.REGULAR_DAMAGE), delay);
									return true;
								} else if (p2.getFireImmune() >= Utils.currentTimeMillis() && Combat.hasAntiDragProtection(p2)) {
									p2.sendMessage("Due to the nature of your shield and potion you take no damage.");
									World.sendProjectile(player, p2, 1166, 41, 16, 31, 35, 16, 0);
									p2.applyDelayedHit(new Hit(player, 0, HitLook.REGULAR_DAMAGE), delay);
									return true;
								}
							}
							if (target instanceof Player) {
								target.player().sendMessage("You are horribly burnt by the firey attack!");
							}
							World.sendProjectile(player, target, 1166, 41, 16, 31, 35, 16, 0);
							target.applyDelayedHit(new Hit(player, 200 + Utils.random(100), HitLook.REGULAR_DAMAGE), delay);
							player.getFacade().setDragonFireCharges(player.getFacade().getDragonFireCharges() - 1);
							if (player.getFacade().getDragonFireCharges() <= 0) {
								player.sendMessage("As you use the last of its charges, your dragon fire shield cools off.");
								player.getEquipment().getItems().set(Equipment.SLOT_SHIELD, new Item(11283));
								player.getEquipment().refreshAll();
							}
							player.putAttribute("last_dfs_time_used", System.currentTimeMillis());
						} else {
							player.sendMessage("Your shield does not have enough charges to be activated.");
						}
					}
					return true;
				}
				return false;
			}
		},
		LEGS(26, Equipment.SLOT_LEGS),
		HANDS(29, Equipment.SLOT_HANDS),
		FEET(32, Equipment.SLOT_FEET),
		RING(35, Equipment.SLOT_RING) {
			@Override
			public boolean handleOtherOption(Player player, int itemId, int packetId) {
				if (packetId == ACTION_BUTTON2_PACKET) {
					if (itemId == 2572) {
						player.sendMessage("You have " + player.getFacade().getRowCharges() + " ring of wealth charges left.");
						return true;
					}
				}
				return false;
			}
		},
		AURA(50, Equipment.SLOT_AURA) {
			@Override
			public boolean handleOtherOption(Player player, int itemId, int packetId) {
				if (packetId == ACTION_BUTTON4_PACKET) {
					ButtonHandler.sendRemove(player, Equipment.SLOT_AURA);
					player.getAuraManager().removeAura();
					return true;
				} else if (packetId == ACTION_BUTTON2_PACKET) {
					player.getAuraManager().activate();
					return true;
				} else if (packetId == ACTION_BUTTON3_PACKET) {
					player.getAuraManager().sendAuraRemainingTime();
					return true;
				}
				return false;
			}
		};

		private final int buttonId;

		private final int equipmentSlot;

		SlotActions(int buttonId, int equipmentSlot) {
			this.buttonId = buttonId;
			this.equipmentSlot = equipmentSlot;
		}

		/**
		 * Gets a slot action for the button clicked
		 *
		 * @param buttonId
		 * 		The button
		 */
		public static Optional<SlotActions> getSlotAction(int buttonId) {
			return Arrays.stream(SlotActions.values()).filter(p -> p.buttonId == buttonId).findFirst();
		}

		/**
		 * Handles other packet options
		 *
		 * @param player
		 * 		The player
		 * @param itemId
		 * 		The id of the item clicked
		 * @param packetId
		 * 		The id of the packet
		 */
		public boolean handleOtherOption(Player player, int itemId, int packetId) {
			return false;
		}

        public int getButtonId() {
            return this.buttonId;
        }

        public int getEquipmentSlot() {
            return this.equipmentSlot;
        }
    }
}
