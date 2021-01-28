package com.runescape.game.content;

import com.runescape.cache.loaders.ClientScriptMap;
import com.runescape.cache.loaders.GeneralRequirementMap;
import com.runescape.game.interaction.controllers.impl.tutorial.StartTutorial;
import com.runescape.game.interaction.dialogues.impl.npc.MakeOverMage;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AchievementHandler;
import com.runescape.game.world.entity.player.achievements.easy.Personal_Groomer;

public final class PlayerLook {

	public static void openCharacterCustomizing(Player player) {
		player.getPackets().sendWindowsPane(1028, 0);
		player.getPackets().sendUnlockIComponentOptionSlots(1028, 65, 0, 11, 0);
		player.getPackets().sendUnlockIComponentOptionSlots(1028, 128, 0, 50, 0);
		player.getPackets().sendUnlockIComponentOptionSlots(1028, 132, 0, 250, 0);
		player.getVarsManager().sendVarBit(8093, player.getAppearence().isMale() ? 0 : 1);
	}

	public static void handleCharacterCustomizingButtons(Player player, int buttonId, int slotId) {
		if (buttonId == 138) { // confirm
			player.getPackets().sendWindowsPane(player.getInterfaceManager().onResizable() ? 746 : 548, 0);
			player.getAttributes().remove("SelectWearDesignD");
			player.getAttributes().remove("ViewWearDesign");
			player.getAttributes().remove("ViewWearDesignD");
			player.getAppearence().generateAppearenceData();
			AchievementHandler.incrementProgress(player, Personal_Groomer.class, 1);
		} else if (buttonId >= 68 && buttonId <= 74) {
			player.getAttributes().put("ViewWearDesign", (buttonId - 68));
			player.getAttributes().put("ViewWearDesignD", 0);
			setDesign(player, buttonId - 68, 0);
		} else if (buttonId >= 103 && buttonId <= 105) {
			Integer index = (Integer) player.getAttributes().get("ViewWearDesign");
			if (index == null) { return; }
			player.getAttributes().put("ViewWearDesignD", (buttonId - 103));
			setDesign(player, index, buttonId - 103);
		} else if (buttonId == 62 || buttonId == 63) {
			setGender(player, buttonId == 62);
		} else if (buttonId == 65) {
			setSkin(player, slotId);
		} else if (buttonId >= 116 && buttonId <= 121) {
			player.getAttributes().put("SelectWearDesignD", (buttonId - 116));
		} else if (buttonId == 128) {
			Integer index = (Integer) player.getAttributes().get("SelectWearDesignD");
			if (index == null || index == 1) {
				boolean male = player.getAppearence().isMale();
				int map1 = ClientScriptMap.getMap(male ? 3304 : 3302).getIntValue(slotId);
				if (map1 == 0) { return; }
				GeneralRequirementMap map = GeneralRequirementMap.getMap(map1);
				player.getAppearence().setHairStyle(map.getIntValue(788));
				if (!male) { player.getAppearence().setBeardStyle(player.getAppearence().getHairStyle()); }
			} else if (index == 2) {
				player.getAppearence().setTopStyle(ClientScriptMap.getMap(player.getAppearence().isMale() ? 3287 : 1591).getIntValue(slotId));
				player.getAppearence().setArmsStyle(player.getAppearence().isMale() ? 26 : 65); // default
				player.getAppearence().setWristsStyle(player.getAppearence().isMale() ? 34 : 68); // default
				player.getAppearence().generateAppearenceData();
			} else if (index == 3) {
				player.getAppearence().setLegsStyle(ClientScriptMap.getMap(player.getAppearence().isMale() ? 3289 : 1607).getIntValue(slotId));
			} else if (index == 4) {
				player.getAppearence().setBootsStyle(ClientScriptMap.getMap(player.getAppearence().isMale() ? 1136 : 1137).getIntValue(slotId));
			} else if (player.getAppearence().isMale()) {
				player.getAppearence().setBeardStyle(ClientScriptMap.getMap(3307).getIntValue(slotId));
			}
		} else if (buttonId == 132) {
			Integer index = (Integer) player.getAttributes().get("SelectWearDesignD");
			if (index == null || index == 0) { setSkin(player, slotId); } else {
				if (index == 1 || index == 5) {
					player.getAppearence().setHairColor(ClientScriptMap.getMap(2345).getIntValue(slotId));
				} else if (index == 2) {
					player.getAppearence().setTopColor(ClientScriptMap.getMap(3283).getIntValue(slotId));
				} else if (index == 3) {
					player.getAppearence().setLegsColor(ClientScriptMap.getMap(3283).getIntValue(slotId));
				} else { player.getAppearence().setBootsColor(ClientScriptMap.getMap(3297).getIntValue(slotId)); }
			}
		}
	}

	public static void setGender(Player player, boolean male) {
		if (male == player.getAppearence().isMale()) { return; }
		if (!male) { player.getAppearence().female(); } else { player.getAppearence().male(); }
		Integer index1 = (Integer) player.getAttributes().get("ViewWearDesign");
		Integer index2 = (Integer) player.getAttributes().get("ViewWearDesignD");
		setDesign(player, index1 != null ? index1 : 0, index2 != null ? index2 : 0);
		player.getAppearence().generateAppearenceData();
		player.getVarsManager().sendVarBit(8093, male ? 0 : 1);
	}

	public static void setSkin(Player player, int index) {
		player.getAppearence().setSkinColor(ClientScriptMap.getMap(748).getIntValue(index));
	}

	public static void setDesign(Player player, int index1, int index2) {
		int map1 = ClientScriptMap.getMap(3278).getIntValue(index1);
		if (map1 == 0) { return; }
		boolean male = player.getAppearence().isMale();
		int map2Id = GeneralRequirementMap.getMap(map1).getIntValue((male ? 1169 : 1175) + index2);
		if (map2Id == 0) { return; }
		GeneralRequirementMap map = GeneralRequirementMap.getMap(map2Id);
		for (int i = 1182; i <= 1186; i++) {
			int value = map.getIntValue(i);
			if (value == -1) { continue; }
			player.getAppearence().setLook(i - 1180, value);
		}
		for (int i = 1187; i <= 1190; i++) {
			int value = map.getIntValue(i);
			if (value == -1) { continue; }
			player.getAppearence().setColor(i - 1186, value);
		}
		if (!player.getAppearence().isMale()) {
			player.getAppearence().setBeardStyle(player.getAppearence().getHairStyle());
		}

	}

	public static void handleMageMakeOverButtons(Player player, int buttonId) {
		if (buttonId == 14 || buttonId == 16 || buttonId == 15 || buttonId == 17) {
			player.getAttributes().put("MageMakeOverGender", buttonId == 14 || buttonId == 16);
		} else if (buttonId >= 20 && buttonId <= 31) {

			int skin;
			if (buttonId == 31) { skin = 11; } else if (buttonId == 30) { skin = 10; } else if (buttonId == 20) {
				skin = 9;
			} else if (buttonId == 21) {
				skin = 8;
			} else if (buttonId == 22) {
				skin = 7;
			} else if (buttonId == 29) {
				skin = 6;
			} else if (buttonId == 28) {
				skin = 5;
			} else if (buttonId == 27) {
				skin = 4;
			} else if (buttonId == 26) {
				skin = 3;
			} else if (buttonId == 25) {
				skin = 2;
			} else if (buttonId == 24) {
				skin = 1;
			} else { skin = 0; }
			player.getAttributes().put("MageMakeOverSkin", skin);
		} else if (buttonId == 33) {
			Boolean male = (Boolean) player.getAttributes().remove("MageMakeOverGender");
			Integer skin = (Integer) player.getAttributes().remove("MageMakeOverSkin");
			player.closeInterfaces();
			AchievementHandler.incrementProgress(player, Personal_Groomer.class, 1);
			if (male == null || skin == null) { return; }
			if (!player.getControllerManager().verifyControlerForOperation(StartTutorial.class).isPresent() && male == player.getAppearence().isMale() && skin == player.getAppearence().getSkinColor()) {
				player.getDialogueManager().startDialogue(MakeOverMage.class, 2676, 1);
			} else {
				player.getDialogueManager().startDialogue(MakeOverMage.class, 2676, 2);
				if (player.getAppearence().isMale() != male) {
					if (player.getEquipment().wearingArmour()) {
						player.getDialogueManager().startDialogue("SimpleMessage", "You cannot have armor on while changing your gender.");
						return;
					}
					if (male) { player.getAppearence().resetAppearence(); } else { player.getAppearence().female(); }
				}
				player.getAppearence().setSkinColor(skin);
				player.getAppearence().generateAppearenceData();
			}
		}
	}

	public static void handleHairdresserSalonButtons(Player player, int buttonId, int slotId) {
		if (buttonId == 6) {
			player.getAttributes().put("hairSaloon", true);
		} else if (buttonId == 7) {
			player.getAttributes().put("hairSaloon", false);
		} else if (buttonId == 18) {
			player.closeInterfaces();
			player.getAppearence().generateAppearenceData();
			AchievementHandler.incrementProgress(player, Personal_Groomer.class, 1);
		} else if (buttonId == 10) {
			Boolean hairSalon = player.getAttribute("hairSaloon");
			if (hairSalon != null && hairSalon) {
				player.getAppearence().setHairStyle((int) ClientScriptMap.getMap(player.getAppearence().isMale() ? 2339 : 2342).getKeyForValue(slotId / 2));
			} else if (player.getAppearence().isMale()) {
				player.getAppearence().setBeardStyle(ClientScriptMap.getMap(703).getIntValue(slotId / 2));
			}
		} else if (buttonId == 16) {
			player.getAppearence().setHairColor(ClientScriptMap.getMap(2345).getIntValue(slotId / 2));
		}
	}

	public static void openGenderSelection(Player player) {
		player.getInterfaceManager().sendInterface(900);
		player.getPackets().sendIComponentText(900, 33, "Confirm");
		player.getVarsManager().sendVarBit(6098, player.getAppearence().isMale() ? 0 : 1);
		player.getVarsManager().sendVarBit(6099, player.getAppearence().getSkinColor());
		player.getAttributes().put("MageMakeOverGender", player.getAppearence().isMale());
		player.getAttributes().put("MageMakeOverSkin", player.getAppearence().getSkinColor());
	}

	public static void handleThessaliasMakeOverButtons(Player player, int buttonId, int slotId) {
		if (buttonId == 6) { player.getAttributes().put("ThessaliasMakeOver", 0); } else if (buttonId == 7) {
			if (ClientScriptMap.getMap(player.getAppearence().isMale() ? 690 : 1591).getKeyForValue(player.getAppearence().getTopStyle()) >= 32) {
				player.getAttributes().put("ThessaliasMakeOver", 1);
			} else { player.getPackets().sendGameMessage("You can't select different arms to go with that top."); }
		} else if (buttonId == 8) {
			if (ClientScriptMap.getMap(player.getAppearence().isMale() ? 690 : 1591).getKeyForValue(player.getAppearence().getTopStyle()) >= 32) {
				player.getAttributes().put("ThessaliasMakeOver", 2);
			} else { player.getPackets().sendGameMessage("You can't select different wrists to go with that top."); }
		} else if (buttonId == 9) {
			player.getAttributes().put("ThessaliasMakeOver", 3);
		} else if (buttonId == 19) { // confirm
			player.closeInterfaces();
			AchievementHandler.incrementProgress(player, Personal_Groomer.class, 1);
		} else if (buttonId == 12) { // set part
			Integer stage = (Integer) player.getAttributes().get("ThessaliasMakeOver");
			if (stage == null || stage == 0) {
				player.getAppearence().setTopStyle(ClientScriptMap.getMap(player.getAppearence().isMale() ? 690 : 1591).getIntValue(slotId / 2));
				player.getAppearence().setArmsStyle(player.getAppearence().isMale() ? 26 : 65); // default
				player.getAppearence().setWristsStyle(player.getAppearence().isMale() ? 34 : 68); // default
			} else if (stage == 1) // arms
			{ player.getAppearence().setArmsStyle(ClientScriptMap.getMap(player.getAppearence().isMale() ? 711 : 693).getIntValue(slotId / 2)); } else if (stage == 2) // wrists
			{ player.getAppearence().setWristsStyle(ClientScriptMap.getMap(751).getIntValue(slotId / 2)); } else {
				player.getAppearence().setLegsStyle(ClientScriptMap.getMap(player.getAppearence().isMale() ? 1586 : 1607).getIntValue(slotId / 2));
			}

		} else if (buttonId == 17) {// color
			Integer stage = (Integer) player.getAttributes().get("ThessaliasMakeOver");
			if (stage == null || stage == 0 || stage == 1) {
				player.getAppearence().setTopColor(ClientScriptMap.getMap(3282).getIntValue(slotId / 2));
			} else if (stage == 3) {
				player.getAppearence().setLegsColor(ClientScriptMap.getMap(3284).getIntValue(slotId / 2));
			}
		}
	}

	public static void openClothingSelection(final Player player) {
		if (player.getEquipment().wearingArmour()) {
			player.getDialogueManager().startDialogue("SimpleNPCMessage", 548, "You're not able to try on my clothes with all that armour. Take it off and then speak to me again.");
			return;
		}
		player.setNextAnimation(new Animation(11623));
		player.getInterfaceManager().sendInterface(729);
		player.getPackets().sendIComponentText(729, 21, "Free!");
		player.getAttributes().put("ThessaliasMakeOver", 0);
		player.getPackets().sendUnlockIComponentOptionSlots(729, 12, 0, 100, 0);
		player.getPackets().sendUnlockIComponentOptionSlots(729, 17, 0, ClientScriptMap.getMap(3282).getSize() * 2, 0);
		player.setCloseInterfacesEvent(() -> {
			player.getDialogueManager().startDialogue("SimpleNPCMessage", 548, "A marvellous choise. You look splendid!");
			player.setNextAnimation(new Animation(-1));
			player.getAppearence().getAppeareanceData();
			player.getAttributes().remove("ThessaliasMakeOver");
		});
	}

	public static void openHairSelection(final Player player) {
		if (player.getEquipment().getHatId() != -1) {
			player.getDialogueManager().startDialogue("SimpleNPCMessage", 598, "I'm afraid I can't see your head at the moment. Please remove your headgear first.");
			return;
		}
		if (player.getEquipment().getWeaponId() != -1 || player.getEquipment().getShieldId() != -1) {
			player.getDialogueManager().startDialogue("SimpleNPCMessage", 598, "I don't feel comfortable cutting hair when you are wielding something. Please remove what you are holding first.");
			return;
		}
		player.setNextAnimation(new Animation(11623));
		player.getInterfaceManager().sendInterface(309);
		player.getPackets().sendUnlockIComponentOptionSlots(309, 10, 0, ClientScriptMap.getMap(player.getAppearence().isMale() ? 2339 : 2342).getSize() * 2, 0);
		player.getPackets().sendUnlockIComponentOptionSlots(309, 16, 0, ClientScriptMap.getMap(2345).getSize() * 2, 0);
		player.getPackets().sendIComponentText(309, 20, "Free!");
		player.getAttributes().put("hairSaloon", true);
		player.setCloseInterfacesEvent(() -> {
			player.setNextAnimation(new Animation(-1));
			player.getAppearence().getAppeareanceData();
			player.getAttributes().put("hairSaloon", true);
		});
	}

	public static void openShoeSelection(final Player player) {
		if (player.getEquipment().getBootsId() != -1) {
			player.getDialogueManager().startDialogue("SimpleNPCMessage", 1301, "I don't feel comfortable helping you try on new boots when you are wearing some already.", "Please remove your boots first.");
			return;
		}
		player.setNextAnimation(new Animation(11623));
		player.getInterfaceManager().sendInterface(728);
		player.getAttributes().put("YrsaBoot", 0);
		player.getPackets().sendUnlockIComponentOptionSlots(728, 12, 0, 500, 0);
		player.getPackets().sendUnlockIComponentOptionSlots(728, 7, 0, ClientScriptMap.getMap(3297).getSize() * 2, 0);
		player.getPackets().sendIComponentText(728, 16, "Free");
		player.setCloseInterfacesEvent(() -> {
			player.getDialogueManager().startDialogue("SimpleNPCMessage", 548, "Hey, They look great!");
			player.setNextAnimation(new Animation(-1));
			player.getAppearence().getAppeareanceData();
			player.getAttributes().remove("YrsaBoot");
		});
	}

	public static void handleYrsaShoes(Player player, int componentId, int slotId) {
		if (componentId == 14) {
			player.closeInterfaces();
			AchievementHandler.incrementProgress(player, Personal_Groomer.class, 1);
		} else if (componentId == 12) {// setting the colors.
			player.getAppearence().setBootsColor(ClientScriptMap.getMap(3297).getIntValue(slotId / 2));
			player.getAppearence().generateAppearenceData();
		} else if (componentId == 7) {// /boot style
			player.getAppearence().setBootsStyle(ClientScriptMap.getMap(player.getAppearence().isMale() ? 3290 : 3293).getIntValue(slotId / 2));
			player.getAppearence().generateAppearenceData();
		}
	}

	private PlayerLook() {

	}

}
