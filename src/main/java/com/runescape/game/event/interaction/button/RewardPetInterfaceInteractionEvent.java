package com.runescape.game.event.interaction.button;

import com.runescape.game.event.interaction.type.InterfaceInteractionEvent;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.world.entity.npc.pet.RewardPet;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.pet.PetDetails;
import com.runescape.utility.ChatColors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 8/24/2016
 */
public class RewardPetInterfaceInteractionEvent extends InterfaceInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] { 156 };
	}

	@Override
	public boolean handleInterfaceInteraction(Player player, int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
		if (!player.getAttribute("pet_selection_opened", false)) {
			return false;
		}
		int index = componentId - 8;
		RewardPet[] rewardPets = RewardPet.values();
		if (index < 0 || index >= rewardPets.length) {
			return true;
		}
		RewardPet pet = rewardPets[index];
		List<String> messages = new ArrayList<>();

		messages.add(pet.getName());
		boolean unlocked = player.getFacade().getRewardPets().contains(pet);
		if (!unlocked) {
			Collections.addAll(messages, pet.getDescription());
		} else {
			messages.add("You have unlocked this pet. Do you wish to spawn it?");
		}
		player.getDialogueManager().startDialogue(new Dialogue() {
			@Override
			public void start() {
				sendDialogue(messages.toArray(new String[messages.size()]));
			}

			@Override
			public void run(int interfaceId, int option) {
				switch (stage) {
					case -1:
						if (unlocked) {
							sendOptionsDialogue("Spawn " + pet.getName() + "?", "Yes", "No");
							stage = 0;
						} else {
							end();
							player.putAttribute("pet_selection_opened", true);
							player.setCloseInterfacesEvent(() -> player.removeAttribute("pet_selection_opened"));
						}
						break;
					case 0:
						end();
						if (option == FIRST) {
							if (player.getPetManager().cantSpawnPet(pet.getPet())) {
								return;
							}
							int baseItemId = pet.getPet().getBabyItemId();
							PetDetails details = player.getPetManager().getPetDetails().get(baseItemId);
							if (details == null) {
								details = new PetDetails(pet.getPet().getGrowthRate() == 0.0 ? 100.0 : 0.0);
								player.getPetManager().getPetDetails().put(baseItemId, details);
							}
							int itemId = pet.getPet().getItemId(details.getStage());
							player.getPetManager().spawnPet(itemId, false);
							player.closeInterfaces();
						} else {
							player.putAttribute("pet_selection_opened", true);
							player.setCloseInterfacesEvent(() -> player.removeAttribute("pet_selection_opened"));
						}
						break;
				}
			}

			@Override
			public void finish() {

			}

		});
		return true;
	}

	/**
	 * Displays the selection
	 *
	 * @param player
	 * 		The player
	 */
	public static void display(Player player) {
		player.closeInterfaces();

		int interfaceId = 156;
		int start = 8;

		for (int i = 7; i < 108; i++) {
			player.getPackets().sendIComponentText(interfaceId, i, "");
		}
		for (RewardPet pet : RewardPet.values()) {
			player.getPackets().sendIComponentText(interfaceId, start, "<col=" + ChatColors.BLUE + ">" + pet.getName());
			start++;
		}
		player.putAttribute("pet_selection_opened", true);
		player.setCloseInterfacesEvent(() -> player.removeAttribute("pet_selection_opened"));
		player.getPackets().sendRunScript(677, start - 8);
		player.getPackets().sendGlobalString(211, "Choose your pet");
		player.getInterfaceManager().sendInterface(interfaceId);
	}
}
