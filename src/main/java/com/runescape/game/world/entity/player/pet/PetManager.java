package com.runescape.game.world.entity.player.pet;

import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleNPCMessage;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.ForceTalk;
import com.runescape.game.world.entity.npc.pet.Pet;
import com.runescape.game.world.entity.npc.pet.RewardPet;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.quests.impl.GertrudesCat;
import com.runescape.game.world.item.Item;
import com.runescape.game.world.item.ItemConstants;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * The pet manager.
 *
 * @author Emperor
 */
public final class PetManager implements Serializable {

	/**
	 * The serial UID.
	 */
	private static final long serialVersionUID = -3379270918966667109L;

	/**
	 * The pet details mapping, sorted by item id.
	 */
	private final Map<Integer, PetDetails> petDetails = new HashMap<>();

	/**
	 * The current NPC id.
	 */
	private int npcId;

	/**
	 * The current item id.
	 */
	private int itemId;

	/**
	 * The troll baby's name (if any).
	 */
	private String trollBabyName;

	/**
	 * The player.
	 */
	private transient Player player;

	/**
	 * Constructs a new {@code PetManager} {@code Object}.
	 */
	public PetManager() {
		/*
		 * empty.
		 */
	}

	/**
	 * Initializes the pet manager.
	 */
	public void init() {
		if (npcId > 0 && itemId > 0) {
			spawnPet(itemId, false);
		}
	}

	/**
	 * Spawns a pet.
	 *
	 * @param itemId
	 * 		The item id.
	 * @param deleteItem
	 * 		If the item should be removed.
	 * @return {@code True} if we were dealing with a pet item id.
	 */
	public boolean spawnPet(int itemId, boolean deleteItem) {
		Pets pets = Pets.forId(itemId);
		if (pets == null) {
			return false;
		}
		if (cantSpawnPet(pets)) { return true; }
		int baseItemId = pets.getBabyItemId();
		PetDetails details = petDetails.get(baseItemId);
		if (details == null) {
			details = new PetDetails(pets.getGrowthRate() == 0.0 ? 100.0 : 0.0);
			petDetails.put(baseItemId, details);
		}
		int id = pets.getItemId(details.getStage());
		if (itemId != id) {
			player.getPackets().sendGameMessage("This is not the right pet, grow the pet correctly.");
			return true;
		}
		int npcId = pets.getNpcId(details.getStage());

		RewardPet rewardPet = RewardPet.getPetInstance(npcId);
		if (rewardPet != null && !player.getFacade().getRewardPets().contains(rewardPet)) {
			player.sendMessage("You must unlock this pet first. Check out ::pets for more information.");
			return true;
		}
		if (npcId > 0) {
			Pet pet = new Pet(npcId, itemId, player, player, details);
			this.npcId = npcId;
			this.itemId = itemId;
			pet.setGrowthRate(pets.getGrowthRate());
			player.setPet(pet);
			if (deleteItem) {
				player.setNextAnimation(new Animation(827));
				player.getInventory().deleteItem(itemId, 1);
			}
			return true;
		}
		return true;
	}

	public boolean cantSpawnPet(Pets pets) {
		if (!player.getQuestManager().isFinished(GertrudesCat.class)) {
			player.getDialogueManager().startDialogue(SimpleNPCMessage.class, 780, "You must finish the \"Gertrude's Cat\" quest", "in order to have a pet.");
			return true;
		}
		if (player.getPet() != null || player.getFamiliar() != null) {
			player.getPackets().sendGameMessage("You already have a follower.");
			return true;
		}
		if (!hasRequirements(pets)) {
			return true;
		}
		return false;
	}

	/**
	 * Checks if the player has the requirements for the pet.
	 *
	 * @param pet
	 * 		The pet.
	 * @return {@code True} if so.
	 */
	private boolean hasRequirements(Pets pet) {
		switch (pet) {
			case TZREK_JAD:
				if (!player.isCompletedFightCaves()) {
					player.getPackets().sendGameMessage("You need to complete at least one fight cave minigame to use this pet.");
					return false;
				}
				if (!player.isWonFightPits()) {
					player.getPackets().sendGameMessage("You need to win at least one fight pits minigame to use this pet.");
					return false;
				}
				return true;
			case FERRET:
			case GIANT_WOLPERTINGER:
				return true;
			default:
				break;
		}
		return true;
	}

	/**
	 * Makes the pet eat.
	 *
	 * @param foodId
	 * 		The food item id.
	 * @param npc
	 * 		The pet NPC.
	 */
	public void eat(int foodId, Pet npc) {
		if (npc != player.getPet()) {
			player.getPackets().sendGameMessage("This isn't your pet!");
			return;
		}
		Pets pets = Pets.forId(itemId);
		if (pets == null) {
			return;
		}
		if (pets == Pets.TROLL_BABY) {
			if (!ItemConstants.isTradeable(new Item(foodId))) {
				player.getPackets().sendGameMessage("Your troll baby won't eat this item.");
				return;
			}
			if (trollBabyName == null) {
				trollBabyName = ItemDefinitions.forId(foodId).getName();
				npc.setName(trollBabyName);
				npc.setNextForceTalk(new ForceTalk("YUM! Me likes " + trollBabyName + "!"));
			}
			player.getInventory().deleteItem(foodId, 1);
			player.getPackets().sendGameMessage("Your pet happily eats the " + ItemDefinitions.forId(foodId).getName() + ".");
			return;
		}
		for (int food : pets.getFood()) {
			if (food == foodId) {
				player.getInventory().deleteItem(food, 1);
				player.getPackets().sendGameMessage("Your pet happily eats the " + ItemDefinitions.forId(food).getName() + ".");
				player.setNextAnimation(new Animation(827));
				npc.getDetails().updateHunger(-15.0);
				return;
			}
		}
		player.getPackets().sendGameMessage("Nothing interesting happens.");
	}

	/**
	 * Removes the details for this pet.
	 *
	 * @param itemId
	 * 		The item id of the pet.
	 */
	public void removeDetails(int itemId) {
		Pets pets = Pets.forId(itemId);
		if (pets == null) {
			return;
		}
		petDetails.remove(pets.getBabyItemId());
	}

	/**
	 * Gets the player.
	 *
	 * @return The player.
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * Sets the player.
	 *
	 * @param player
	 * 		The player to set.
	 */
	public void setPlayer(Player player) {
		this.player = player;
	}

	/**
	 * Gets the npcId.
	 *
	 * @return The npcId.
	 */
	public int getNpcId() {
		return npcId;
	}

	/**
	 * Sets the npcId.
	 *
	 * @param npcId
	 * 		The npcId to set.
	 */
	public void setNpcId(int npcId) {
		this.npcId = npcId;
	}

	/**
	 * Gets the itemId.
	 *
	 * @return The itemId.
	 */
	public int getItemId() {
		return itemId;
	}

	/**
	 * Sets the itemId.
	 *
	 * @param itemId
	 * 		The itemId to set.
	 */
	public void setItemId(int itemId) {
		this.itemId = itemId;
	}

	/**
	 * Gets the trollBabyName.
	 *
	 * @return The trollBabyName.
	 */
	public String getTrollBabyName() {
		return trollBabyName;
	}

	/**
	 * Sets the trollBabyName.
	 *
	 * @param trollBabyName
	 * 		The trollBabyName to set.
	 */
	public void setTrollBabyName(String trollBabyName) {
		this.trollBabyName = trollBabyName;
	}

    public Map<Integer, PetDetails> getPetDetails() {
        return this.petDetails;
    }
}