package com.runescape.game.world.entity.npc.pet;

import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.pet.Pets;
import com.runescape.utility.ChatColors;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 8/24/2016
 */
public enum RewardPet {

	ABYSSAL_MINION(Pets.ABYSSAL_MINION, "This small demon can be loyally yours after killing", "100 abyssal demons."),
	HATCHLING_DRAGON(Pets.BABY_DRAGON_1, "This cute dragon can call you its master after killing", "50 blue dragons.") {
		@Override
		public String getName() {
			return "Hatchling dragon (blue)";
		}
	},
	TZREK_JAD(Pets.TZREK_JAD, "In exchange for 5 fire capes, you will receive this rare pet.");

	private final Pets pet;

	private final String[] description;

	/**
	 * Gets the proper name of the pet
	 */
	public String getName() {
		String name = name();
		return name.substring(0, 1).toUpperCase() + name.substring(1, name.length()).toLowerCase().replaceAll("_", " ");
	}

	RewardPet(Pets pet, String... description) {
		this.pet = pet;
		this.description = description;
	}

	/**
	 * Gets the pet instance
	 *
	 * @param npcId
	 * 		The npc
	 */
	public static RewardPet getPetInstance(int npcId) {
		for (RewardPet pet : values()) {
			if (pet.getPet().getBabyNpcId() == npcId || pet.getPet().getGrownNpcId() == npcId || pet.getPet().getOvergrownNpcId() == npcId) {
				return pet;
			}
		}
		return null;
	}

	/**
	 * Gives the pet to the player
	 *
	 * @param player
	 * 		The player
	 * @param pet
	 * 		The pet
	 */
	public static void addPet(Player player, RewardPet pet) {
		if (player.getFacade().getRewardPets().contains(pet)) {
			return;
		}
		player.sendMessage("<col=" + ChatColors.BLUE + ">You have just received a " + pet.getName() + " pet! View ::pets for more information.");
		player.getFacade().addRewardPet(pet);
	}

    public Pets getPet() {
        return this.pet;
    }

    public String[] getDescription() {
        return this.description;
    }
}
