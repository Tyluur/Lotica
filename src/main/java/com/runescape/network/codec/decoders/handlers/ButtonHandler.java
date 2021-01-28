package com.runescape.network.codec.decoders.handlers;

import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.game.GameConstants;
import com.runescape.game.content.GreegreeHandler;
import com.runescape.game.content.PlayerLook;
import com.runescape.game.content.global.minigames.Crucible;
import com.runescape.game.content.global.minigames.duel.DuelControler;
import com.runescape.game.content.skills.SkillCapeCustomizer;
import com.runescape.game.content.skills.SkillsDialogue;
import com.runescape.game.content.skills.construction.House;
import com.runescape.game.content.skills.crafting.JewllerySmithing;
import com.runescape.game.content.skills.magic.Magic;
import com.runescape.game.content.skills.runecrafting.Runecrafting;
import com.runescape.game.content.skills.smithing.Smithing.ForgingInterface;
import com.runescape.game.content.skills.summoning.Summoning;
import com.runescape.game.event.InputEvent;
import com.runescape.game.event.InputEvent.InputEventType;
import com.runescape.game.event.interaction.InteractionEventManager;
import com.runescape.game.interaction.dialogues.impl.misc.MyAccountD;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleMessage;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.npc.familiar.impl.Familiar;
import com.runescape.game.world.entity.npc.familiar.impl.Familiar.SpecialAttack;
import com.runescape.game.world.entity.player.*;
import com.runescape.game.world.entity.player.LockManagement.LockType;
import com.runescape.game.world.entity.player.achievements.AchievementHandler;
import com.runescape.game.world.entity.player.achievements.medium.Furious;
import com.runescape.game.world.entity.player.actions.FightPitsViewingOrb;
import com.runescape.game.world.entity.player.actions.HomeTeleport;
import com.runescape.game.world.entity.player.actions.PlayerCombat;
import com.runescape.game.world.entity.player.actions.Rest;
import com.runescape.game.world.item.Item;
import com.runescape.game.world.item.ItemConstants;
import com.runescape.network.codec.decoders.WorldPacketsDecoder;
import com.runescape.network.stream.InputStream;
import com.runescape.utility.Utils;
import com.runescape.utility.external.gson.loaders.ItemInformationLoader;
import com.runescape.workers.game.core.CoresManager;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class ButtonHandler {

	private static final Object[][] info = new Object[][] { { 31, 0, "Stab" }, { 32, 1, "Slash" }, { 33, 2, "Crush" }, { 34, 3, "Magic" }, { 35, 4, "Range" }, { 36, 5, "Stab" }, { 37, 6, "Slash" }, { 38, 7, "Crush" }, { 39, 8, "Magic" }, { 40, 9, "Range" }, { 41, 10, "Summoning" }, { 42, CombatDefinitions.ABSORVE_MELEE_BONUS, "Absorb Melee" }, { 43, CombatDefinitions.ABSORVE_MAGE_BONUS, "Absorb Magic" }, { 44, CombatDefinitions.ABSORVE_RANGE_BONUS, "Absorb Range" }, { 45, 14, "Strength" }, { 46, 15, "Ranged Str" }, { 47, 16, "Prayer" }, { 48, 17, "Magic Damage" } };

	public static void handleButtons(final Player player, InputStream stream, int packetId) {
		int interfaceHash = stream.readIntV2();
		int interfaceId = interfaceHash >> 16;
		if (Utils.getInterfaceDefinitionsSize() <= interfaceId) {
			// hack, or server error or client error
			// player.getSession().getChannel().close();
			return;
		}
		if (player.isDead() || !player.getInterfaceManager().containsInterface(interfaceId)) {
			return;
		}
		final int componentId = interfaceHash - (interfaceId << 16);
		if (componentId != 65535 && Utils.getInterfaceDefinitionsComponentsSize(interfaceId) + 1 <= componentId) {
			// hack, or server error or client error
			// player.getSession().getChannel().close();
			return;
		}
		final int slotId2 = stream.readUnsignedShortLE128();
		final int slotId = stream.readUnsignedShort();
		if (!player.getControllerManager().processButtonClick(interfaceId, componentId, slotId, packetId)) {
			return;
		}
		if (player.getLockManagement().isLocked(LockType.INTERFACE_INTERACTION)) {
			return;
		}
		if (InteractionEventManager.handleInterfaceInteraction(player, interfaceId, componentId, slotId, slotId2, packetId)) {
			return;
		}
		if (player.getActionManager().getAction() != null && player.getActionManager().getAction().handleClick(player, interfaceId, componentId)) {
			return;
		}
		if (interfaceId == 182) {
			if (player.getInterfaceManager().containsInventoryInter()) {
				return;
			}
			if (componentId == 6 || componentId == 13) {
				if (!player.hasFinished()) {
					player.logout();
				}
			}
		} else if (interfaceId == 880) {
			if (componentId >= 7 && componentId <= 19) {
				Familiar.setLeftclickOption(player, (componentId - 7) / 2);
			} else if (componentId == 21) {
				Familiar.confirmLeftOption(player);
			} else if (componentId == 25) {
				Familiar.setLeftclickOption(player, 7);
			}
		} else if (interfaceId == 675) {
			JewllerySmithing.handleButtonClick(player, componentId, packetId == 14 ? 1 : packetId == 67 ? 5 : 10);
		} else if (interfaceId == 662) {
			if (player.getFamiliar() == null) {
				if (player.getPet() == null) {
					return;
				}
				if (componentId == 49) {
					player.getPet().call();
				} else if (componentId == 51) {
					player.getDialogueManager().startDialogue("DismissD");
				}
				return;
			}
			if (componentId == 49) {
				player.getFamiliar().call();
			} else if (componentId == 51) {
				player.getDialogueManager().startDialogue("DismissD");
			} else if (componentId == 67) {
				player.getFamiliar().takeBob();
			} else if (componentId == 69) {
				player.getFamiliar().renewFamiliar();
			} else if (componentId == 74) {
				if (!player.getControllerManager().canUseFamiliarSpecial()) {
					return;
				}
				if (player.getFamiliar().getSpecialAttack() == SpecialAttack.CLICK) {
					player.getFamiliar().setSpecial(true);
				}
				if (player.getFamiliar().hasSpecialOn()) {
					player.getFamiliar().submitSpecial(player);
				}
			}
		} else if (interfaceId == 747) {
			if (componentId == 7) {
				Familiar.selectLeftOption(player);
			} else if (player.getPet() != null) {
				if (componentId == 10 || componentId == 19) {
					player.getPet().call();
				} else if (componentId == 11 || componentId == 20) {
					player.getDialogueManager().startDialogue("DismissD");
				} else if (componentId == 18) {
					player.getPet().sendFollowerDetails();
				}
			} else if (player.getFamiliar() != null) {
				if (componentId == 10 || componentId == 19) {
					player.getFamiliar().call();
				} else if (componentId == 11 || componentId == 20) {
					player.getDialogueManager().startDialogue("DismissD");
				} else if (componentId == 12 || componentId == 21) {
					player.getFamiliar().takeBob();
				} else if (componentId == 13 || componentId == 22) {
					player.getFamiliar().renewFamiliar();
				} else if (componentId == 18 || componentId == 9) {
					player.getFamiliar().sendFollowerDetails();
				} else if (componentId == 17) {
					if (!player.getControllerManager().canUseFamiliarSpecial()) {
						return;
					}
					if (player.getFamiliar().getSpecialAttack() == SpecialAttack.CLICK) {
						player.getFamiliar().setSpecial(true);
					}
					if (player.getFamiliar().hasSpecialOn()) {
						player.getFamiliar().submitSpecial(player);
					}
				}
			}
		} else if (interfaceId == 309) {
			PlayerLook.handleHairdresserSalonButtons(player, componentId, slotId);
		} else if (interfaceId == 729) {
			PlayerLook.handleThessaliasMakeOverButtons(player, componentId, slotId);
		} else if (interfaceId == 187) {
			if (componentId == 1) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
					player.getMusicsManager().playAnotherMusic(slotId / 2);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
					player.getMusicsManager().sendHint(slotId / 2);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET) {
					player.getMusicsManager().addToPlayList(slotId / 2);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET) {
					player.getMusicsManager().removeFromPlayList(slotId / 2);
				}
			} else if (componentId == 4) {
				player.getMusicsManager().addPlayingMusicToPlayList();
			} else if (componentId == 10) {
				player.getMusicsManager().switchPlayListOn();
			} else if (componentId == 11) {
				player.getMusicsManager().clearPlayList();
			} else if (componentId == 13) {
				player.getMusicsManager().switchShuffleOn();
			}
		} else if ((interfaceId == 590 && componentId == 8) || interfaceId == 464) {
			player.getEmotesManager().useBookEmote(slotId, interfaceId == 464 ? componentId : EmotesManager.getId(slotId, packetId));
		} else if (interfaceId == 192) {
			if (componentId == 2) {
				player.getCombatDefinitions().switchDefensiveCasting();
			} else if (componentId == 7) {
				player.getCombatDefinitions().switchShowCombatSpells();
			} else if (componentId == 9) {
				player.getCombatDefinitions().switchShowTeleportSkillSpells();
			} else if (componentId == 11) {
				player.getCombatDefinitions().switchShowMiscallaneousSpells();
			} else if (componentId == 13) {
				player.getCombatDefinitions().switchShowSkillSpells();
			} else if (componentId >= 15 & componentId <= 17) {
				player.getCombatDefinitions().setSortSpellBook(componentId - 15);
			} else {
				Magic.processNormalSpell(player, componentId, packetId);
			}
		} else if (interfaceId == 334) {
			if (componentId == 22) {
				player.closeInterfaces();
			} else if (componentId == 21) {
				player.getTrade().accept(false);
			}
		} else if (interfaceId == 335) {
			if (componentId == 16) {
				player.getTrade().accept(true);
			} else if (componentId == 18) {
				player.closeInterfaces();
			} else if (componentId == 31) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
					player.getTrade().removeItem(slotId, 1);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
					player.getTrade().removeItem(slotId, 5);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET) {
					player.getTrade().removeItem(slotId, 10);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET) {
					player.getTrade().removeItem(slotId, Integer.MAX_VALUE);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET) {
					player.getAttributes().put("trade_item_X_Slot", slotId);
					player.getAttributes().put("trade_isRemove", Boolean.TRUE);
					player.getPackets().sendRunScript(108, "Enter Amount:");
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON9_PACKET) {
					player.getTrade().sendValue(slotId, false);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON8_PACKET) {
					player.getTrade().sendExamine(slotId, false);
				}
			} else if (componentId == 34) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
					player.getTrade().sendValue(slotId, true);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON8_PACKET) {
					player.getTrade().sendExamine(slotId, true);
				}
			} else if (componentId == 57) {
				player.getPackets().requestClientInput(new InputEvent("Enter Amount:", InputEventType.INTEGER) {
					@Override
					public void handleInput() {
						int hours = getInput();
						if (hours > 72) {
							player.getDialogueManager().startDialogue(SimpleMessage.class, "You cannot lend an item for more than 72 hours.");
							return;
						}
						if (hours == 0) {
							player.getTrade().setLentTillLogout(true);
						} else {
							player.getTrade().setLentTillLogout(false);
						}
						player.getTrade().setHoursLentFor(hours);
						player.getTrade().refreshLendHours();
					}
				});
			}
		} else if (interfaceId == 336) {
			if (componentId == 0) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
					player.getTrade().addItem(slotId, 1);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
					player.getTrade().addItem(slotId, 5);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET) {
					player.getTrade().addItem(slotId, 10);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET) {
					player.getTrade().addItem(slotId, Integer.MAX_VALUE);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET) {
					player.getAttributes().put("trade_item_X_Slot", slotId);
					player.getAttributes().remove("trade_isRemove");
					player.getPackets().sendRunScript(108, "Enter Amount:");
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON6_PACKET) {
					player.getTrade().addLendItem(slotId);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON9_PACKET) {
					player.getTrade().sendValue(slotId);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON8_PACKET) {
					player.getInventory().sendExamine(slotId);
				}
			}
		} else if (interfaceId == 300) {
			ForgingInterface.handleIComponents(player, componentId);
		} else if (interfaceId == 206) {
			if (componentId == 15) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
					player.getPriceCheckManager().removeItem(slotId, 1);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
					player.getPriceCheckManager().removeItem(slotId, 5);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET) {
					player.getPriceCheckManager().removeItem(slotId, 10);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET) {
					player.getPriceCheckManager().removeItem(slotId, Integer.MAX_VALUE);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET) {
					player.getAttributes().put("pc_item_X_Slot", slotId);
					player.getAttributes().put("pc_isRemove", Boolean.TRUE);
					player.getPackets().sendRunScript(108, "Enter Amount:");
				}
			}
		} else if (interfaceId == 672) {
			if (componentId == 16) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
					Summoning.sendCreatePouch(player, slotId2, 1);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
					Summoning.sendCreatePouch(player, slotId2, 5);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET) {
					Summoning.sendCreatePouch(player, slotId2, 10);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET) {
					Summoning.sendCreatePouch(player, slotId2, Integer.MAX_VALUE);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET) {
					Summoning.sendCreatePouch(player, slotId2, 28);// x
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON6_PACKET) {
					player.getPackets().sendGameMessage("You currently need " + ItemDefinitions.forId(slotId2).getCreateItemRequirements());
				}
			}
		} else if (interfaceId == 207) {
			if (componentId == 0) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
					player.getPriceCheckManager().addItem(slotId, 1);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
					player.getPriceCheckManager().addItem(slotId, 5);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET) {
					player.getPriceCheckManager().addItem(slotId, 10);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET) {
					player.getPriceCheckManager().addItem(slotId, Integer.MAX_VALUE);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET) {
					player.getAttributes().put("pc_item_X_Slot", slotId);
					player.getAttributes().remove("pc_isRemove");
					player.getPackets().sendRunScript(108, "Enter Amount:");
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON9_PACKET) {
					player.getInventory().sendExamine(slotId);
				}
			}
		} else if (interfaceId == 665) {
			if (player.getFamiliar() == null || player.getFamiliar().getBob() == null) {
				return;
			}
			if (componentId == 0) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
					player.getFamiliar().getBob().addItem(slotId, 1);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
					player.getFamiliar().getBob().addItem(slotId, 5);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET) {
					player.getFamiliar().getBob().addItem(slotId, 10);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET) {
					player.getFamiliar().getBob().addItem(slotId, Integer.MAX_VALUE);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET) {
					player.getAttributes().put("bob_item_X_Slot", slotId);
					player.getAttributes().remove("bob_isRemove");
					player.getPackets().sendRunScript(108, "Enter Amount:");
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON9_PACKET) {
					player.getInventory().sendExamine(slotId);
				}
			}
		} else if (interfaceId == 671) {
			if (player.getFamiliar() == null || player.getFamiliar().getBob() == null) {
				return;
			}
			if (componentId == 27) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
					player.getFamiliar().getBob().removeItem(slotId, 1);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
					player.getFamiliar().getBob().removeItem(slotId, 5);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET) {
					player.getFamiliar().getBob().removeItem(slotId, 10);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET) {
					player.getFamiliar().getBob().removeItem(slotId, Integer.MAX_VALUE);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET) {
					player.getAttributes().put("bob_item_X_Slot", slotId);
					player.getAttributes().put("bob_isRemove", Boolean.TRUE);
					player.getPackets().sendRunScript(108, "Enter Amount:");
				}
			} else if (componentId == 29) {
				player.getFamiliar().takeBob();
			}
		} else if (interfaceId == 916) {
			SkillsDialogue.handleSetQuantityButtons(player, componentId);
		} else if (interfaceId == 193) {
			if (componentId == 5) {
				player.getCombatDefinitions().switchShowCombatSpells();
			} else if (componentId == 7) {
				player.getCombatDefinitions().switchShowTeleportSkillSpells();
			} else if (componentId >= 9 && componentId <= 11) {
				player.getCombatDefinitions().setSortSpellBook(componentId - 9);
			} else if (componentId == 18) {
				player.getCombatDefinitions().switchDefensiveCasting();
			} else {
				Magic.processAncientSpell(player, componentId);
			}
		} else if (interfaceId == 430) {
			if (componentId == 5) {
				player.getCombatDefinitions().switchShowCombatSpells();
			} else if (componentId == 7) {
				player.getCombatDefinitions().switchShowTeleportSkillSpells();
			} else if (componentId == 9) {
				player.getCombatDefinitions().switchShowMiscallaneousSpells();
			} else if (componentId >= 11 & componentId <= 13) {
				player.getCombatDefinitions().setSortSpellBook(componentId - 11);
			} else if (componentId == 20) {
				player.getCombatDefinitions().switchDefensiveCasting();
			} else {
				Magic.processLunarSpell(player, componentId, packetId);
			}
		} else if (interfaceId == 261) {
			if (player.getInterfaceManager().containsInventoryInter()) {
				return;
			}
			if (componentId == 14) {
				if (player.getInterfaceManager().containsScreenInterface()) {
					player.getPackets().sendGameMessage("Please close the interface you have open before setting your graphic options.");
					return;
				}
				player.stopAll();
				player.getInterfaceManager().sendInterface(742);
			} else if (componentId == 4) {
				player.switchAllowChatEffects();
			} else if (componentId == 5) { // chat setup
				player.getInterfaceManager().sendSettings(982);
			} else if (componentId == 6) {
				player.switchMouseButtons();
			} else if (componentId == 16) // audio options
			{
				player.getInterfaceManager().sendInterface(743);
			} else if (componentId == 18) {
				MyAccountD.displayBossKillCounts(player);
			} else if (componentId == 8) {// house options
				player.getInterfaceManager().sendSettings(398);
			}
		} else if (interfaceId == 398) {
			if (componentId == 19) {
				player.getInterfaceManager().sendSettings();
			} else if (componentId == 15 || componentId == 1) {
				player.getHouse().setBuildMode(componentId == 15);
			} else if (componentId == 25 || componentId == 26) {
				player.getHouse().setArriveInPortal(componentId == 25);
			} else if (componentId == 27) {
				player.getHouse().expelGuests();
			} else if (componentId == 29) {
				House.leaveHouse(player);
			}
		} else if (interfaceId == 429) {
			if (componentId == 18) {
				player.getInterfaceManager().sendSettings();
			}
		} else if (interfaceId == 982) {
			if (componentId == 5) {
				player.getInterfaceManager().sendSettings();
			} else if (componentId == 41) {
				player.setPrivateChatSetup(player.getPrivateChatSetup() == 0 ? 1 : 0);
			} else if (componentId >= 49 && componentId <= 61) {
				player.setPrivateChatSetup(componentId - 48);
			} else if (componentId >= 72 && componentId <= 91) {
				player.setFriendChatSetup(componentId - 72);
			}
			player.refreshOtherChatsSetup();
		} else if (interfaceId == 271) {
			if (componentId == 8 || componentId == 42) {
				player.getPrayer().switchPrayer(slotId);
			} else if (componentId == 43 && player.getPrayer().isUsingQuickPrayer()) {
				player.getPrayer().switchSettingQuickPrayer();
			}
		} else if (interfaceId == 1218) {
			if ((componentId >= 33 && componentId <= 55) || componentId == 120 || componentId == 151 || componentId == 189) {
				player.getPackets().sendInterface(false, 1218, 1, 1217); // seems
			}
			// to
			// fix
		} else if (interfaceId == 499) {
			int skillMenu = -1;
			if (player.getAttributes().get("skillMenu") != null) {
				skillMenu = (Integer) player.getAttributes().get("skillMenu");
			}
			if (componentId >= 10 && componentId <= 25) {
				player.getPackets().sendConfig(965, ((componentId - 10) * 1024) + skillMenu);
			} else if (componentId == 29)
			// close inter
			{
				player.stopAll();
			}

		} else if (interfaceId == 640) {
			if (componentId == 18 || componentId == 22) {
				player.getAttributes().put("WillDuelFriendly", true);
				player.getPackets().sendConfig(283, 67108864);
			} else if (componentId == 19 || componentId == 21) {
				player.getAttributes().put("WillDuelFriendly", false);
				player.getPackets().sendConfig(283, 134217728);
			} else if (componentId == 20) {
				DuelControler.challenge(player);
			}
		} else if (interfaceId == 650) {
			if (componentId == 17) {
				player.stopAll();
				player.setNextWorldTile(new WorldTile(2974, 4384, player.getPlane()));
				player.getControllerManager().startController("CorpBeastControler");
			} else if (componentId == 18) {
				player.closeInterfaces();
			}
		} else if (interfaceId == 667) {
			if (componentId == 14) {
				if (slotId >= 14) {
					return;
				}
				Item item = player.getEquipment().getItem(slotId);
				if (item == null) {
					return;
				}
				if (packetId == 3) {
					player.getPackets().sendGameMessage(ItemInformationLoader.getExamine(item.getId()));
				} else if (packetId == 216) {
					sendRemove(player, slotId);
					ButtonHandler.refreshEquipBonuses(player);
				}
			} else if (componentId == 46 && player.getAttributes().remove("Banking") != null) {
				player.getBank().openBank();
			}
		} else if (interfaceId == 670) {
			if (componentId == 0) {
				if (slotId >= player.getInventory().getItemsContainerSize()) {
					return;
				}
				Item item = player.getInventory().getItem(slotId);
				if (item == null) {
					return;
				}
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
					if (sendWear(player, slotId, item.getId())) {
						ButtonHandler.refreshEquipBonuses(player);
					}
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET) {
					player.getInventory().sendExamine(slotId);
				}
			}
		} else if (interfaceId == Inventory.INVENTORY_INTERFACE) { // inventory
			if (componentId == 0) {
				if (slotId > 27 || player.getInterfaceManager().containsInventoryInter() || player.getLockManagement().isLocked(LockType.ITEM_INTERACTION)) {
					return;
				}
				Item item = player.getInventory().getItem(slotId);
				if (item == null || item.getId() != slotId2) {
					return;
				}
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
					InventoryOptionsHandler.handleItemOption1(player, slotId, slotId2, item);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
					InventoryOptionsHandler.handleItemOption2(player, slotId, slotId2, item);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET) {
					InventoryOptionsHandler.handleItemOption3(player, slotId, slotId2, item);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET) {
					InventoryOptionsHandler.handleItemOption4(player, slotId, slotId2, item);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET) {
					InventoryOptionsHandler.handleItemOption5(player, slotId, slotId2, item);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON6_PACKET) {
					InventoryOptionsHandler.handleItemOption6(player, slotId, slotId2, item);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON7_PACKET) {
					InventoryOptionsHandler.handleItemOption7(player, slotId, slotId2, item);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON8_PACKET) {
					InventoryOptionsHandler.handleItemOption8(player, slotId, slotId2, item);
				}
			}
		} else if (interfaceId == 742) {
			if (componentId == 46) // close
			{
				player.stopAll();
			}
		} else if (interfaceId == 743) {
			if (componentId == 20) // close
			{
				player.stopAll();
			}
		} else if (interfaceId == 741) {
			if (componentId == 9) // close
			{
				player.stopAll();
			}
		} else if (interfaceId == 749) {
			if (componentId == 1) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
					player.getPrayer().switchQuickPrayers();
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
					player.getPrayer().switchSettingQuickPrayer();
				}
			}
		} else if (interfaceId == 750) {
			if (componentId == 1) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
					player.toogleRun(!player.isResting());
					if (player.isResting()) {
						player.stopAll();
					}
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
					if (player.isResting()) {
						player.stopAll();
						return;
					}
					long currentTime = Utils.currentTimeMillis();
					if (player.getEmotesManager().getNextEmoteEnd() >= currentTime) {
						player.getPackets().sendGameMessage("You can't rest while perfoming an emote.");
						return;
					}
					if (player.getLockManagement().isLocked(LockType.INTERFACE_INTERACTION)) {
						player.getPackets().sendGameMessage("You can't rest while perfoming an action.");
						return;
					}
					player.stopAll();
					player.getActionManager().setAction(new Rest());
				}
			}
		} else if (interfaceId == 11) {
			if (componentId == 17) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
					player.getBank().depositItem(slotId, 1, true);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
					player.getBank().depositItem(slotId, 5, true);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET) {
					player.getBank().depositItem(slotId, 10, true);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET) {
					player.getBank().depositItem(slotId, Integer.MAX_VALUE, true);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET) {
					player.getPackets().requestClientInput(new InputEvent("Enter Amount:", InputEventType.INTEGER) {
						@Override
						public void handleInput() {
							player.getBank().depositItem(slotId, getInput(), true);
						}
					});
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON9_PACKET) {
					player.getInventory().sendExamine(slotId);
				}
			} else if (componentId == 18) {
				player.getBank().depositAllInventory(false);
			} else if (componentId == 20) {
				player.getBank().depositAllEquipment(false);
			}
		} else if (interfaceId == 762) {
			if (componentId == 15) {
				player.getBank().switchInsertItems();
			} else if (componentId == 19) {
				player.getBank().switchWithdrawNotes();
			} else if (componentId == 33) {
				player.getBank().depositAllInventory(true);
			} else if (componentId == 35) {
				player.getBank().depositAllEquipment(true);
			} else if (componentId == 37) {
				player.getBank().depositAllBob(true);
			} else if (componentId >= 44 && componentId <= 62) {
				int tabId = 9 - ((componentId - 44) / 2);
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
					player.getBank().setCurrentTab(tabId);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
					player.getBank().collapse(tabId);
				}
			} else if (componentId == 93) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
					player.getBank().withdrawItem(slotId, 1);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
					player.getBank().withdrawItem(slotId, 5);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET) {
					player.getBank().withdrawItem(slotId, 10);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET) {
					player.getBank().withdrawLastAmount(slotId);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET) {
					player.getPackets().requestClientInput(new InputEvent("Enter Amount", InputEventType.INTEGER) {
						@Override
						public void handleInput() {
							player.getBank().withdrawItem(slotId, getInput());
							player.getBank().setLastX(getInput());
							player.getBank().refreshLastX();
						}
					});
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON9_PACKET) {
					player.getBank().withdrawItem(slotId, Integer.MAX_VALUE);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON6_PACKET) {
					player.getBank().withdrawItemButOne(slotId);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON8_PACKET) {
					player.getBank().sendExamine(slotId);
				}
			}
		} else if (interfaceId == 763) {
			if (componentId == 0) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
					player.getBank().depositItem(slotId, 1, true);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
					player.getBank().depositItem(slotId, 5, true);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET) {
					player.getBank().depositItem(slotId, 10, true);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET) {
					player.getBank().depositLastAmount(slotId);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET) {
					player.getAttributes().put("bank_item_X_Slot", slotId);
					player.getAttributes().remove("bank_isWithdraw");
					player.getPackets().sendRunScript(108, "Enter Amount:");
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON9_PACKET) {
					player.getBank().depositItem(slotId, Integer.MAX_VALUE, true);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON8_PACKET) {
					player.getInventory().sendExamine(slotId);
				}
			}
		} else if (interfaceId == 767) {
			if (componentId == 10) {
				player.getBank().openBank();
			}
		} else if (interfaceId == 884) {
			if (componentId == 4) {
				int weaponId = player.getEquipment().getWeaponId();
				if (player.hasInstantSpecial(weaponId)) {
					player.performInstantSpecial(weaponId);
					return;
				}
				submitSpecialRequest(player);
			} else if (componentId >= 11 && componentId <= 14) {
				player.getCombatDefinitions().setAttackStyle(componentId - 11);
			} else if (componentId == 15) {
				player.getCombatDefinitions().switchAutoRetaliate();
			}
		} else if (interfaceId == 755) {
			if (componentId == 44) {
				player.getPackets().sendWindowsPane(player.getInterfaceManager().onResizable() ? 746 : 548, 2);
			} else if (componentId == 42) {
				player.getHintIconsManager().removeAll();
				player.getPackets().sendConfig(1159, 1);
			}
		} else if (interfaceId == 20) {
			SkillCapeCustomizer.handleSkillCapeCustomizer(player, componentId);
		} else if (interfaceId == 1056) {
			if (componentId == 173) {
				player.getInterfaceManager().sendInterface(917);
			}
		} else if (interfaceId == 751) {
			if (componentId == 26) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
					player.getFriendsIgnores().setPrivateStatus(0);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET) {
					player.getFriendsIgnores().setPrivateStatus(1);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET) {
					player.getFriendsIgnores().setPrivateStatus(2);
				}
			} else if (componentId == 32) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
					player.setFilterGame(false);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET) {
					player.setFilterGame(true);
				}
			} else if (componentId == 29) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
					player.setPublicStatus(0);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET) {
					player.setPublicStatus(1);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET) {
					player.setPublicStatus(2);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON5_PACKET) {
					player.setPublicStatus(3);
				}
			} else if (componentId == 0) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
					player.getFriendsIgnores().setFriendsChatStatus(0);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET) {
					player.getFriendsIgnores().setFriendsChatStatus(1);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET) {
					player.getFriendsIgnores().setFriendsChatStatus(2);
				}
			} else if (componentId == 23) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
					player.setClanStatus(0);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET) {
					player.setClanStatus(1);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET) {
					player.setClanStatus(2);
				}
			} else if (componentId == 20) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
					player.setTradeStatus(0);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET) {
					player.setTradeStatus(1);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET) {
					player.setTradeStatus(2);
				}
			} else if (componentId == 17) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
					player.setAssistStatus(0);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET) {
					player.setAssistStatus(1);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET) {
					player.setAssistStatus(2);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON9_PACKET) {
					// ASSIST XP Earned/Time
				}
			}
		} else if (interfaceId == 1163 || interfaceId == 1164 || interfaceId == 1168 || interfaceId == 1170 || interfaceId == 1173) {
			player.getDominionTower().handleButtons(interfaceId, componentId);
		} else if (interfaceId == 900) {
			PlayerLook.handleMageMakeOverButtons(player, componentId);
		} else if (interfaceId == 1028) {
			PlayerLook.handleCharacterCustomizingButtons(player, componentId, slotId);
		} else if (interfaceId == 1108 || interfaceId == 1109) {
			player.getFriendsIgnores().handleFriendChatButtons(interfaceId, componentId, packetId);
		} else if (interfaceId == 728) {
			PlayerLook.handleYrsaShoes(player, componentId, slotId);
		} else if (interfaceId == 1079) {
			player.closeInterfaces();
		} else if (interfaceId == 374) {
			if (componentId >= 5 && componentId <= 9) {
				player.setNextWorldTile(new WorldTile(FightPitsViewingOrb.ORB_TELEPORTS[componentId - 5]));
			} else if (componentId == 15) {
				player.stopAll();
			}
		} else if (interfaceId == 1092) {
			player.stopAll();
			WorldTile destTile = null;
			switch (componentId) {
				case 47:
					destTile = HomeTeleport.LUMBRIDGE_LODE_STONE;
					break;
				case 42:
					destTile = HomeTeleport.BURTHORPE_LODE_STONE;
					break;
				case 39:
					destTile = HomeTeleport.LUNAR_ISLE_LODE_STONE;
					break;
				case 7:
					destTile = HomeTeleport.BANDIT_CAMP_LODE_STONE;
					break;
				case 50:
					destTile = HomeTeleport.TAVERLY_LODE_STONE;
					break;
				case 40:
					destTile = HomeTeleport.ALKARID_LODE_STONE;
					break;
				case 51:
					destTile = HomeTeleport.VARROCK_LODE_STONE;
					break;
				case 45:
					destTile = HomeTeleport.EDGEVILLE_LODE_STONE;
					break;
				case 46:
					destTile = HomeTeleport.FALADOR_LODE_STONE;
					break;
				case 48:
					destTile = HomeTeleport.PORT_SARIM_LODE_STONE;
					break;
				case 44:
					destTile = HomeTeleport.DRAYNOR_VILLAGE_LODE_STONE;
					break;
				case 41:
					destTile = HomeTeleport.ARDOUGNE_LODE_STONE;
					break;
				case 43:
					destTile = HomeTeleport.CATHERBAY_LODE_STONE;
					break;
				case 52:
					destTile = HomeTeleport.YANILLE_LODE_STONE;
					break;
				case 49:
					destTile = HomeTeleport.SEERS_VILLAGE_LODE_STONE;
					break;
			}
			if (destTile != null) {
				player.getActionManager().setAction(new HomeTeleport(destTile));
			}
		}
		if (interfaceId == 1292) {
			if (componentId == 12) {
				Crucible.enterArena(player);
			} else if (componentId == 13) {
				player.closeInterfaces();
			}
		}
		if (GameConstants.DEBUG) {
			System.out.println("InterfaceId " + interfaceId + ", componentId " + componentId + ", slotId " + slotId + ", slotId2 " + slotId2 + ", PacketId: " + packetId);
		}
	}

	public static void sendRemove(Player player, int slotId) {
		if (slotId >= 15) {
			return;
		}
		player.stopAll(false, false);
		Item item = player.getEquipment().getItem(slotId);
		if (item == null || !player.getInventory().addItem(item.getId(), item.getAmount())) {
			return;
		}
		player.getEquipment().getItems().set(slotId, null);
		player.getEquipment().refresh(slotId);
		player.getAppearence().generateAppearenceData();
		if (Runecrafting.isTiara(item.getId())) {
			player.getPackets().sendConfig(491, 0);
		}
		if (slotId == 3) {
			player.getCombatDefinitions().desecreaseSpecialAttack(0);
		}
	}

	public static void refreshEquipBonuses(Player player) {
		final int interfaceId = 667;
		for (Object[] element : info) {
			int bonus = player.getCombatDefinitions().getBonuses()[(int) element[1]];
			String sign = bonus > 0 ? "+" : "";
			player.getPackets().sendIComponentText(interfaceId, (int) element[0], element[2] + ": " + sign + bonus);
		}
	}

	public static boolean sendWear(Player player, int slotId, int itemId) {
		if (player.hasFinished() || player.isDead()) {
			return false;
		}
		player.stopAll(false, false);
		Item item = player.getInventory().getItem(slotId);
		if (item == null || item.getId() != itemId) {
			return false;
		}
		if (item.getDefinitions().isNoted() || !item.getDefinitions().isWearItem(player.getAppearence().isMale())) {
			player.getPackets().sendGameMessage("You can't wear that.");
			return true;
		}
		int targetSlot = Equipment.getItemSlot(itemId);
		if (targetSlot == -1) {
			player.getPackets().sendGameMessage("You can't wear that.");
			return true;
		}
		if (!ItemConstants.canWear(player, item)) {
			return true;
		}
		if (item.getId() == 6585) {
			AchievementHandler.incrementProgress(player, Furious.class, 1);
		}
		boolean isTwoHandedWeapon = targetSlot == 3 && Equipment.isTwoHandedWeapon(item);
		if (isTwoHandedWeapon && !player.getInventory().hasFreeSlots() && player.getEquipment().hasShield()) {
			player.getPackets().sendGameMessage("Not enough free space in your inventory.");
			return true;
		}
		HashMap<Integer, Integer> requiriments = item.getDefinitions().getWearingSkillRequirements();
		boolean hasRequiriments = true;
		if (requiriments != null) {
			for (int skillId : requiriments.keySet()) {
				if (skillId > 24 || skillId < 0) {
					continue;
				}
				int level = requiriments.get(skillId);
				if (level < 0 || level > 120) {
					continue;
				}
				if (player.getSkills().getLevelForXp(skillId) < level) {
					if (hasRequiriments) {
						player.getPackets().sendGameMessage("You are not high enough level to use this item.");
					}
					hasRequiriments = false;
					String name = Skills.SKILL_NAME[skillId].toLowerCase();
					player.getPackets().sendGameMessage("You need to have a" + (name.startsWith("a") ? "n" : "") + " " + name + " level of " + level + ".");
				}

			}
		}
		if (!hasRequiriments) {
			return true;
		}
		if (!player.getControllerManager().canEquip(targetSlot, itemId)) {
			return false;
		}
		player.stopAll();
		player.getInventory().deleteItem(slotId, item);
		if (targetSlot == 3) {
			if (isTwoHandedWeapon && player.getEquipment().getItem(5) != null) {
				if (!player.getInventory().addItem(player.getEquipment().getItem(5).getId(), player.getEquipment().getItem(5).getAmount())) {
					player.getInventory().getItems().set(slotId, item);
					player.getInventory().refresh(slotId);
					return true;
				}
				player.getEquipment().getItems().set(5, null);
			}
		} else if (targetSlot == 5) {
			if (player.getEquipment().getItem(3) != null && Equipment.isTwoHandedWeapon(player.getEquipment().getItem(3))) {
				if (!player.getInventory().addItem(player.getEquipment().getItem(3).getId(), player.getEquipment().getItem(3).getAmount())) {
					player.getInventory().getItems().set(slotId, item);
					player.getInventory().refresh(slotId);
					return true;
				}
				player.getEquipment().getItems().set(3, null);
			}

		}
		if (player.getEquipment().getItem(targetSlot) != null && (itemId != player.getEquipment().getItem(targetSlot).getId() || !item.getDefinitions().isStackable())) {
			if (player.getInventory().getItems().get(slotId) == null) {
				player.getInventory().getItems().set(slotId, new Item(player.getEquipment().getItem(targetSlot).getId(), player.getEquipment().getItem(targetSlot).getAmount()));
				player.getInventory().refresh(slotId);
			} else {
				player.getInventory().addItem(new Item(player.getEquipment().getItem(targetSlot).getId(), player.getEquipment().getItem(targetSlot).getAmount()));
			}
			player.getEquipment().getItems().set(targetSlot, null);
		}
		if (targetSlot == Equipment.SLOT_AURA) {
			player.getAuraManager().removeAura();
		}
		int oldAmt = 0;
		if (player.getEquipment().getItem(targetSlot) != null) {
			oldAmt = player.getEquipment().getItem(targetSlot).getAmount();
		}
		Item item2 = new Item(itemId, oldAmt + item.getAmount());
		player.getEquipment().getItems().set(targetSlot, item2);
		player.getEquipment().refresh(targetSlot, targetSlot == 3 ? 5 : 3);
		player.getAppearence().generateAppearenceData();
		player.getPackets().sendSound(2240, 0, 1);
		if (targetSlot == 3) {
			player.getCombatDefinitions().desecreaseSpecialAttack(0);
		}
		return true;
	}

	public static void openEquipmentBonuses(final Player player, boolean banking) {
		player.getInterfaceManager().sendInterface(667);
		player.getPackets().sendIComponentSettings(667, 7, 0, 15, 1538);
		player.getPackets().sendGlobalConfig(779, player.getEquipment().getWeaponRenderEmote());
		refreshEquipBonuses(player);
	}

	public static void submitSpecialRequest(final Player player) {
		player.putAttribute("special_attack_toggled", true);
		CoresManager.schedule(() -> {
			try {
				if (player.isDead()) { return; }
				PlayerCombat.checkSpecialToggle(player);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}, 100, TimeUnit.MILLISECONDS);
	}

	public static void sendWear(Player player, int[] slotIds) {
		if (player.hasFinished() || player.isDead()) {
			return;
		}
		boolean worn = false;
		Item[] copy = player.getInventory().getItems().getItemsCopy();
		for (int slotId : slotIds) {
			Item item = player.getInventory().getItem(slotId);
			if (item == null) {
				continue;
			}
			if (sendWear2(player, slotId, item.getId())) {
				worn = true;
			}
		}
		player.getInventory().refreshItems(copy);
		if (worn) {
			player.getAppearence().generateAppearenceData();
			player.getPackets().sendSound(2240, 0, 1);
		}
	}

	public static boolean sendWear2(Player player, int slotId, int itemId) {
		if (player.hasFinished() || player.isDead()) {
			return false;
		}
		/*if (!player.hasInstantSpecial(itemId)) {
			player.stopAll(false, false);
		}*/
		Item item = player.getInventory().getItem(slotId);
		if (item == null || item.getId() != itemId) {
			return false;
		}
		if (item.getDefinitions().isNoted() || !item.getDefinitions().isWearItem(player.getAppearence().isMale()) && itemId != 4084) {
			player.getPackets().sendGameMessage("You can't wear that.");
			return false;
		}
		int targetSlot = Equipment.getItemSlot(itemId);
		if (itemId == 4084) {
			targetSlot = 3;
		}
		if (targetSlot == -1) {
			player.getPackets().sendGameMessage("You can't wear that.");
			return false;
		}
		if (!ItemConstants.canWear(player, item)) {
			return false;
		}
		if (item.getId() == 6585) {
			AchievementHandler.incrementProgress(player, Furious.class, 1);
		}
		boolean isTwoHandedWeapon = targetSlot == 3 && Equipment.isTwoHandedWeapon(item);
		if (isTwoHandedWeapon && !player.getInventory().hasFreeSlots() && player.getEquipment().hasShield()) {
			player.getPackets().sendGameMessage("Not enough free space in your inventory.");
			return false;
		}
		HashMap<Integer, Integer> requiriments = item.getDefinitions().getWearingSkillRequirements();
		boolean hasRequiriments = true;
		if (requiriments != null) {
			for (int skillId : requiriments.keySet()) {
				if (skillId > 24 || skillId < 0) {
					continue;
				}
				int level = requiriments.get(skillId);
				if (level < 0 || level > 120) {
					continue;
				}
				if (player.getSkills().getLevelForXp(skillId) < level) {
					if (hasRequiriments) {
						player.getPackets().sendGameMessage("You are not high enough level to use this item.");
					}
					hasRequiriments = false;
					String name = Skills.SKILL_NAME[skillId].toLowerCase();
					player.getPackets().sendGameMessage("You need to have a" + (name.startsWith("a") ? "n" : "") + " " + name + " level of " + level + ".");
				}
			}
		}
		if (!hasRequiriments) {
			return false;
		}
		if (!player.getControllerManager().canEquip(targetSlot, itemId)) {
			return false;
		}
		player.getInventory().getItems().remove(slotId, item);
		if (targetSlot == 3) {
			if (isTwoHandedWeapon && player.getEquipment().getItem(5) != null) {
				if (!player.getInventory().getItems().add(player.getEquipment().getItem(5))) {
					player.getInventory().getItems().set(slotId, item);
					return false;
				}
				player.getEquipment().getItems().set(5, null);
			}
		} else if (targetSlot == 5) {
			if (player.getEquipment().getItem(3) != null && Equipment.isTwoHandedWeapon(player.getEquipment().getItem(3))) {
				if (!player.getInventory().getItems().add(player.getEquipment().getItem(3))) {
					player.getInventory().getItems().set(slotId, item);
					return false;
				}
				player.getEquipment().getItems().set(3, null);
			}
		}
		if (player.getEquipment().getItem(targetSlot) != null && (itemId != player.getEquipment().getItem(targetSlot).getId() || !item.getDefinitions().isStackable())) {
			if (player.getInventory().getItems().get(slotId) == null) {
				player.getInventory().getItems().set(slotId, new Item(player.getEquipment().getItem(targetSlot).getId(), player.getEquipment().getItem(targetSlot).getAmount()));
			} else {
				player.getInventory().getItems().add(new Item(player.getEquipment().getItem(targetSlot).getId(), player.getEquipment().getItem(targetSlot).getAmount()));
			}
			player.getEquipment().getItems().set(targetSlot, null);
		}
		if (targetSlot == Equipment.SLOT_AURA) {
			player.getAuraManager().removeAura();
		}
		int oldAmt = 0;
		if (player.getEquipment().getItem(targetSlot) != null) {
			oldAmt = player.getEquipment().getItem(targetSlot).getAmount();
		}
		Item item2 = new Item(itemId, oldAmt + item.getAmount());
		player.getEquipment().getItems().set(targetSlot, item2);
		player.getEquipment().refresh(targetSlot, targetSlot == 3 ? 5 : 3);
		if (targetSlot == 3) {
			player.getCombatDefinitions().desecreaseSpecialAttack(0);
		}
		if (player.getInterfaceManager().containsInterface(667)) {
			refreshEquipBonuses(player);
		}
		if (!player.getInterfaceManager().containsInterface(667)) {
			player.closeInterfaces();
		}
		GreegreeHandler.handleGreegreeEquip(player, itemId);
		player.getDegradeManager().sendDegradeInformation(item2);
		return true;
	}

}


