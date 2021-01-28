package com.runescape.network.stream.incoming.impl;

import com.runescape.game.GameConstants;
import com.runescape.game.content.global.clans.ClansManager;
import com.runescape.game.content.skills.SkillCapeCustomizer;
import com.runescape.game.content.skills.magic.Magic;
import com.runescape.game.content.skills.slayer.SlayerManagement;
import com.runescape.game.content.skills.summoning.Summoning;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.familiar.impl.Familiar;
import com.runescape.game.world.entity.npc.familiar.impl.Familiar.SpecialAttack;
import com.runescape.game.world.entity.player.Bank;
import com.runescape.game.world.entity.player.Inventory;
import com.runescape.game.world.entity.player.LockManagement.LockType;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.actions.PlayerCombat;
import com.runescape.game.world.item.Item;
import com.runescape.network.codec.decoders.handlers.InventoryOptionsHandler;
import com.runescape.network.stream.InputStream;
import com.runescape.network.stream.incoming.IncomingStreamDecoder;
import com.runescape.utility.Utils;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/27/2016
 */
public class InterfaceStreamDecoder extends IncomingStreamDecoder {

	private final static int INTERFACE_ON_PLAYER = 40;

	private final static int INTERFACE_ON_NPC = 65;

	private final static int COLOR_ID_PACKET = 22;

	private final static int SWITCH_INTERFACE_ITEM_PACKET = 26;

	private final static int DIALOGUE_CONTINUE_PACKET = 54;

	@Override
	public int[] getKeys() {
		return new int[] { 40, 65, 22, 26, 54 };
	}

	@Override
	public void decode(Player player, InputStream stream, int packetId, int length) {
		switch (packetId) {
			case DIALOGUE_CONTINUE_PACKET:
				handleDialogue(player, packetId, length, stream);
				break;
			case SWITCH_INTERFACE_ITEM_PACKET:
				stream.readUnsignedShort();
				int fromSlot = stream.readUnsignedShortLE();
				stream.readUnsignedShort128();
				int interface1Hash = stream.readIntV1();
				int toSlot = stream.readUnsignedShortLE();
				int interface2Hash = stream.readIntV2();

				int fromInterfaceId = interface1Hash >> 16;
				int fromComponentId = interface1Hash - (fromInterfaceId << 16);

				int toInterfaceId = interface2Hash >> 16;
				int toComponentId = interface2Hash - (toInterfaceId << 16);

				if (Utils.getInterfaceDefinitionsSize() <= fromInterfaceId || Utils.getInterfaceDefinitionsSize() <= toInterfaceId) {
					return;
				}
				if (!player.getInterfaceManager().containsInterface(fromInterfaceId) || !player.getInterfaceManager().containsInterface(toInterfaceId)) {
					return;
				}
				if (fromComponentId != -1 && Utils.getInterfaceDefinitionsComponentsSize(fromInterfaceId) <= fromComponentId) {
					return;
				}
				if (toComponentId != -1 && Utils.getInterfaceDefinitionsComponentsSize(toInterfaceId) <= toComponentId) {
					return;
				}
//				System.out.println(fromInterfaceId + ", " + toInterfaceId);
				if (fromInterfaceId == Inventory.INVENTORY_INTERFACE && fromComponentId == 0 && toInterfaceId == Inventory.INVENTORY_INTERFACE && toComponentId == 0) {
					toSlot -= 28;
					if (toSlot < 0 || toSlot >= player.getInventory().getItemsContainerSize() || fromSlot >= player.getInventory().getItemsContainerSize()) {
						return;
					}
					player.getInventory().switchItem(fromSlot, toSlot);
				} else if (fromInterfaceId == 763 && fromComponentId == 0 && toInterfaceId == 763 && toComponentId == 0) {
					if (toSlot >= player.getInventory().getItemsContainerSize() || fromSlot >= player.getInventory().getItemsContainerSize()) {
						return;
					}
					player.getInventory().switchItem(fromSlot, toSlot);
				} else if (fromInterfaceId == 762 && toInterfaceId == 762) {
					player.getBank().switchItem(fromSlot, toSlot, fromComponentId, toComponentId);
				}
				if (GameConstants.DEBUG) {
					System.out.println("Switch item " + fromInterfaceId + ", " + fromSlot + ", " + toSlot);
				}
				break;
			case INTERFACE_ON_PLAYER:
				handleInterfaceOnPlayer(player, packetId, length, stream);
				break;
			case INTERFACE_ON_NPC:
				handleInterfaceOnNPC(player, packetId, length, stream);
				break;
			case COLOR_ID_PACKET:
				if (!player.hasStarted()) {
					return;
				}
				int colorId = stream.readUnsignedShort();
				if (player.getAttributes().get("SkillcapeCustomize") != null) {
					SkillCapeCustomizer.handleSkillCapeCustomizerColor(player, colorId);
				}
				break;
		}
	}

	private void handleDialogue(Player player, Integer packetId, Integer length, InputStream stream) {
		int interfaceHash = stream.readIntV2();
		stream.readShortLE128();
		int interfaceId = interfaceHash >> 16;
		if (Utils.getInterfaceDefinitionsSize() <= interfaceId) {
			return;
		}
		if (!player.isRunning() || !player.getInterfaceManager().containsInterface(interfaceId)) {
			return;
		}
		int componentId = interfaceHash - (interfaceId << 16);
		if (interfaceId == 14) {
			handlePinComponents(player, componentId);
			return;
		} else if (interfaceId == 13) {
			player.getPinManager().finishPin(componentId - 6);
			return;
		}
		player.getDialogueManager().continueDialogue(interfaceId, componentId);
	}

	private void handlePinComponents(Player player, int componentId) {
		switch (componentId) {
			case 18: // change your pin
				if (!player.getPinManager().hasPin()) {
					player.getPinManager().showConfirmSetPin();
					player.putAttribute("bank_pin_action", "set_pin");
				} else {
					if (!player.getPinManager().enteredPinDuringSession()) {
						player.getPinManager().openSettingsScreen();
						player.sendMessage("Please enter your bank pin before attempting to change it.");
					} else {
						player.getPinManager().showConfirmChangePin();
						player.putAttribute("bank_pin_action", "change_pin");
					}
				}
				break;
			case 19: // delete pin
				if (player.getPinManager().hasPin()) {
					player.getPinManager().showConfirmDeletePin();
					player.putAttribute("bank_pin_action", "delete_pin");
				} else {
					handlePinComponents(player, 20);
				}
				break;
			case 20: // switch delay
				player.getPinManager().switchRecoveryDelay();
				player.getPinManager().openSettingsScreen();
				player.getPinManager().sendRecoveryString();
				break;
			case 35:
				player.getInterfaceManager().closeScreenInterface();
				break;
			case 33:
				switch (player.getAttribute("bank_pin_action", "")) {
					case "set_pin":
					case "change_pin":
						player.getPinManager().showEnterPin(false);
						break;
					case "delete_pin":
						if (player.getPinManager().enteredPinDuringSession()) {
							player.getPinManager().cancelPin();
							player.getPackets().sendGlobalString(344, "Your Bank PIN has now been deleted.<br><br>This means that there is no PIN protection on your bank account.");
						} else {
							player.getPinManager().setPinCancelationTime(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(player.getPinManager().isSevenDayRecoveryDelay() ? 7 : 3));
							player.getPinManager().openSettingsScreen();
							player.getPackets().sendGlobalString(344, "Your bank pin will be deleted on: " + Bank.DATE_FORMAT.format(new Date(player.getPinManager().getPinCancelationTime())) + ".");
						}
						break;
				}
				break;
		}
	}

	private void handleInterfaceOnPlayer(final Player player, Integer packetId, Integer length, InputStream stream) {
		if (!player.hasStarted() || !player.clientHasLoadedMapRegion() || player.isDead()) {
			return;
		}
		if (player.getLockManagement().isLocked(LockType.INTERFACE_INTERACTION)) {
			return;
		}
		int playerIndex = stream.readUnsignedShortLE();
		int interfaceHash = stream.readIntLE();

		int itemId = stream.readUnsignedShort();

		final boolean forceRun = stream.read128Byte() == 1;

		int slotId = stream.readUnsignedShortLE128();
		int interfaceId = interfaceHash >> 16;
		int componentId = interfaceHash - (interfaceId << 16);
		if (Utils.getInterfaceDefinitionsSize() <= interfaceId) {
			return;
		}
		if (!player.getInterfaceManager().containsInterface(interfaceId)) {
			return;
		}
		if (componentId == 65535) {
			componentId = -1;
		}
		if (componentId != -1 && Utils.getInterfaceDefinitionsComponentsSize(interfaceId) <= componentId) {
			return;
		}
		final Player p2 = World.getPlayers().get(playerIndex);
		if (p2 == null || p2.isDead() || p2.hasFinished() || !player.getMapRegionsIds().contains(p2.getRegionId())) {
			return;
		}
		player.stopAll(true, true, true, false);
		if (forceRun) {
			player.setRun(forceRun);
		}
		switch (interfaceId) {
			case 1110:
				if (componentId == 87) {
					ClansManager.invite(player, p2);
				}
				break;
			case 662:
			case 747:
				if (player.getFamiliar() == null) {
					return;
				}
				player.resetWalkSteps();
				if ((interfaceId == 747 && componentId == 15) || (interfaceId == 662 && componentId == 65) || (interfaceId == 662 && componentId == 74) || interfaceId == 747 && componentId == 18) {
					if ((interfaceId == 662 && componentId == 74 || interfaceId == 747 && componentId == 24 || interfaceId == 747 && componentId == 18)) {
						if (player.getFamiliar().getSpecialAttack() != SpecialAttack.ENTITY) {
							return;
						}
					}
					if (!player.isCanPvp() || !p2.isCanPvp()) {
						player.getPackets().sendGameMessage("You can only attack players in a player-vs-player area.");
						return;
					}
					if (!player.getFamiliar().canAttack(p2)) {
						player.getPackets().sendGameMessage("You can only use your familiar in a multi-zone area.");
						return;
					} else {
						player.getFamiliar().setSpecial(interfaceId == 662 && componentId == 74 || interfaceId == 747 && componentId == 18);
						player.getFamiliar().setTarget(p2);
					}
				}
				break;
			case 193:
				switch (componentId) {
					case 28:
					case 32:
					case 24:
					case 20:
					case 30:
					case 34:
					case 26:
					case 22:
					case 29:
					case 33:
					case 25:
					case 21:
					case 31:
					case 35:
					case 27:
					case 23:
						if (Magic.checkCombatSpell(player, componentId, 1, false)) {
							player.setNextFaceWorldTile(new WorldTile(p2.getCoordFaceX(p2.getSize()), p2.getCoordFaceY(p2.getSize()), p2.getPlane()));
							if (!player.getControllerManager().canAttack(p2)) {
								return;
							}
							if (!player.isCanPvp() || !p2.isCanPvp()) {
								player.getPackets().sendGameMessage("You can only attack players in a player-vs-player area.");
								return;
							}
							if (!p2.isAtMultiArea() || !player.isAtMultiArea()) {
								if (player.getAttackedBy() != p2 && player.getAttackedByDelay() > Utils.currentTimeMillis()) {
									player.getPackets().sendGameMessage("That " + (player.getAttackedBy() instanceof Player ? "player" : "npc") + " is already in combat.");
									return;
								}
								if (p2.getAttackedBy() != player && p2.getAttackedByDelay() > Utils.currentTimeMillis()) {
									if (p2.getAttackedBy() instanceof NPC) {
										p2.setAttackedBy(player); // changes enemy
										// to player,
										// player has
										// priority over
										// npc on single
										// areas
									} else {
										player.getPackets().sendGameMessage("That player is already in combat.");
										return;
									}
								}
							}
							player.stopAll(true, true, true, false);
							player.getActionManager().setAction(new PlayerCombat(p2));
						}
						break;
				}
			case 192:
				switch (componentId) {
					case 25: // air strike
					case 28: // water strike
					case 30: // earth strike
					case 32: // fire strike
					case 34: // air bolt
					case 39: // water bolt
					case 42: // earth bolt
					case 45: // fire bolt
					case 49: // air blast
					case 52: // water blast
					case 58: // earth blast
					case 63: // fire blast
					case 70: // air wave
					case 73: // water wave
					case 77: // earth wave
					case 80: // fire wave
					case 86: // teleblock
					case 84: // air surge
					case 87: // water surge
					case 89: // earth surge
					case 91: // fire surge
					case 99: // storm of armadyl
					case 36: // bind
					case 66: // Sara Strike
					case 67: // Guthix Claws
					case 68: // Flame of Zammy
					case 55: // snare
					case 81: // entangle
						if (Magic.checkCombatSpell(player, componentId, 1, false)) {
							player.setNextFaceWorldTile(new WorldTile(p2.getCoordFaceX(p2.getSize()), p2.getCoordFaceY(p2.getSize()), p2.getPlane()));
							if (!player.getControllerManager().canAttack(p2)) {
								return;
							}
							if (!player.isCanPvp() || !p2.isCanPvp()) {
								player.getPackets().sendGameMessage("You can only attack players in a player-vs-player area.");
								return;
							}
							if (!p2.isAtMultiArea() || !player.isAtMultiArea()) {
								if (player.getAttackedBy() != p2 && player.getAttackedByDelay() > Utils.currentTimeMillis()) {
									player.getPackets().sendGameMessage("That " + (player.getAttackedBy() instanceof Player ? "player" : "npc") + " is already in combat.");
									return;
								}
								if (p2.getAttackedBy() != player && p2.getAttackedByDelay() > Utils.currentTimeMillis()) {
									if (p2.getAttackedBy() instanceof NPC) {
										p2.setAttackedBy(player); // changes enemy
										// to player,
										// player has
										// priority over
										// npc on single
										// areas
									} else {
										player.getPackets().sendGameMessage("That player is already in combat.");
										return;
									}
								}
							}
							player.stopAll(true, true, true, false);
							player.getActionManager().setAction(new PlayerCombat(p2));
						}
						break;
				}
				break;
			case 679:
				Item item = player.getInventory().getItem(slotId);
				if (item == null) {
					return;
				}
				player.setNextFaceWorldTile(p2);
				if (!player.getControllerManager().handleItemOnPlayer(p2, item)) {
					return;
				}
				if (item.getId() == 4155) {
					SlayerManagement.invitePlayer(player, p2);
				}
				break;
		}
		if (GameConstants.DEBUG) {
			System.out.println("Spell:" + componentId);
		}
	}

	private void handleInterfaceOnNPC(Player player, Integer packetId, Integer length, InputStream stream) {
		if (!player.hasStarted() || !player.clientHasLoadedMapRegion() || player.isDead()) {
			return;
		}
		if (player.getLockManagement().isLocked(LockType.INTERFACE_INTERACTION)) {
			return;
		}
		int slot = stream.readUnsignedShortLE128();
		stream.readUnsignedShortLE();
		int npcIndex = stream.readUnsignedShortLE();
		int interfaceHash = stream.readIntV2();
		stream.readByte();
		int interfaceId = interfaceHash >> 16;
		int componentId = interfaceHash - (interfaceId << 16);

		if (Utils.getInterfaceDefinitionsSize() <= interfaceId) {
			return;
		}
		if (!player.getInterfaceManager().containsInterface(interfaceId)) {
			return;
		}
		if (componentId == 65535) {
			componentId = -1;
		}
		if (componentId != -1 && Utils.getInterfaceDefinitionsComponentsSize(interfaceId) <= componentId) {
			return;
		}
		NPC npc = World.getNPCs().get(npcIndex);
		if (npc == null || npc.isDead() || npc.hasFinished() || !player.getMapRegionsIds().contains(npc.getRegionId())) {
			return;
		}
		player.stopAll(true, true, true, false);
		switch (interfaceId) {
			case Inventory.INVENTORY_INTERFACE:
				Item item = player.getInventory().getItem(slot);
				if (item == null || !player.getControllerManager().processItemOnNPC(npc, item)) {
					return;
				}
				InventoryOptionsHandler.handleItemOnNPC(player, npc, item);
				break;
			case 1165:
				Summoning.attackDreadnipTarget(npc, player);
				break;
			case 662:
			case 747:
				if (player.getFamiliar() == null) {
					return;
				}
				player.resetWalkSteps();
				if ((interfaceId == 747 && componentId == 14) || (interfaceId == 662 && componentId == 65) || (interfaceId == 662 && componentId == 74) || interfaceId == 747 && componentId == 17 || interfaceId == 747 && componentId == 23) {
					if ((interfaceId == 662 && componentId == 74 || interfaceId == 747 && componentId == 17)) {
						if (player.getFamiliar().getSpecialAttack() != SpecialAttack.ENTITY) {
							return;
						}
					}
					if (npc == player.getFamiliar()) {
						player.getPackets().sendGameMessage("You can't attack your own familiar.");
						return;
					}
					if (!player.getFamiliar().canAttack(npc)) {
						player.getPackets().sendGameMessage("You can only use your familiar in a multi-zone area.");
						return;
					} else {
						player.getFamiliar().setSpecial(interfaceId == 662 && componentId == 74 || interfaceId == 747 && componentId == 17);
						player.getFamiliar().setTarget(npc);
					}
				}
				break;
			case 193:
				switch (componentId) {
					case 28:
					case 32:
					case 24:
					case 20:
					case 30:
					case 34:
					case 26:
					case 22:
					case 29:
					case 33:
					case 25:
					case 21:
					case 31:
					case 35:
					case 27:
					case 23:
						if (Magic.checkCombatSpell(player, componentId, 1, false)) {
							player.setNextFaceWorldTile(new WorldTile(npc.getCoordFaceX(npc.getSize()), npc.getCoordFaceY(npc.getSize()), npc.getPlane()));
							if (!player.getControllerManager().canAttack(npc)) {
								return;
							}
							if (npc instanceof Familiar) {
								Familiar familiar = (Familiar) npc;
								if (familiar == player.getFamiliar()) {
									player.getPackets().sendGameMessage("You can't attack your own familiar.");
									return;
								}
								if (!familiar.canAttack(player)) {
									player.getPackets().sendGameMessage("You can't attack this npc.");
									return;
								}
							} else if (!npc.isForceMultiAttacked()) {
								if (!npc.isAtMultiArea() || !player.isAtMultiArea()) {
									if (player.getAttackedBy() != npc && player.getAttackedByDelay() > Utils.currentTimeMillis()) {
										player.getPackets().sendGameMessage("You are already in combat.");
										return;
									}
									if (npc.getAttackedBy() != player && npc.getAttackedByDelay() > Utils.currentTimeMillis()) {
										player.getPackets().sendGameMessage("This npc is already in combat.");
										return;
									}
								}
							}
							player.stopAll(true, true, true, false);
							player.getActionManager().setAction(new PlayerCombat(npc));
						}
						break;
				}
				break;
			case 192:
				switch (componentId) {
					case 25: // air strike
					case 28: // water strike
					case 30: // earth strike
					case 32: // fire strike
					case 34: // air bolt
					case 39: // water bolt
					case 42: // earth bolt
					case 45: // fire bolt
					case 49: // air blast
					case 52: // water blast
					case 58: // earth blast
					case 63: // fire blast
					case 70: // air wave
					case 73: // water wave
					case 77: // earth wave
					case 80: // fire wave
					case 84: // air surge
					case 87: // water surge
					case 89: // earth surge
					case 66: // Sara Strike
					case 67: // Guthix Claws
					case 68: // Flame of Zammy
					case 93:
					case 91: // fire surge
					case 99: // storm of Armadyl
					case 36: // bind
					case 55: // snare
					case 81: // entangle
						if (Magic.checkCombatSpell(player, componentId, 1, false)) {
							player.setNextFaceWorldTile(new WorldTile(npc.getCoordFaceX(npc.getSize()), npc.getCoordFaceY(npc.getSize()), npc.getPlane()));
							if (!player.getControllerManager().canAttack(npc)) {
								return;
							}
							if (npc instanceof Familiar) {
								Familiar familiar = (Familiar) npc;
								if (familiar == player.getFamiliar()) {
									player.getPackets().sendGameMessage("You can't attack your own familiar.");
									return;
								}
								if (!familiar.canAttack(player)) {
									player.getPackets().sendGameMessage("You can't attack this npc.");
									return;
								}
							} else if (!npc.isForceMultiAttacked()) {
								if (!npc.isAtMultiArea() || !player.isAtMultiArea()) {
									if (player.getAttackedBy() != npc && player.getAttackedByDelay() > Utils.currentTimeMillis()) {
										player.getPackets().sendGameMessage("You are already in combat.");
										return;
									}
									if (npc.getAttackedBy() != player && npc.getAttackedByDelay() > Utils.currentTimeMillis()) {
										player.getPackets().sendGameMessage("This npc is already in combat.");
										return;
									}
								}
							}
							player.stopAll(true, true, true, false);
							player.getActionManager().setAction(new PlayerCombat(npc));
						}
						break;
				}
				break;
		}
		if (GameConstants.DEBUG) {
			System.out.println("Spell:" + componentId);
		}
	}
}
