package com.runescape.game.content.skills.thieving;

import com.runescape.game.GameConstants;
import com.runescape.game.interaction.controllers.impl.Wilderness;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.ForceTalk;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.masks.Hit;
import com.runescape.game.world.entity.masks.Hit.HitLook;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Equipment;
import com.runescape.game.world.entity.player.LockManagement.LockType;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.game.world.entity.player.achievements.AchievementHandler;
import com.runescape.game.world.entity.player.achievements.medium.Man_Thief;
import com.runescape.game.world.entity.player.actions.Action;
import com.runescape.game.world.item.Item;
import com.runescape.utility.Utils;

/**
 * Handels the pick pocketing.
 *
 * @author Raghav/Own4g3
 */
public class PickPocketAction extends Action {

	/**
	 * The npc stun animation.
	 */
	private static final Animation STUN_ANIMATION = new Animation(422),

	/**
	 * The pick pocketing animation.
	 */
	PICKPOCKETING_ANIMATION = new Animation(881),

	/**
	 * The double loot animation.
	 */
	DOUBLE_LOOT_ANIMATION = new Animation(5074),

	/**
	 * The triple loot animation.
	 */
	TRIPLE_LOOT_ANIMATION = new Animation(5075),

	/**
	 * The quadruple loot animation.
	 */
	QUADRUPLE_LOOT_ANIMATION = new Animation(5078);

	/**
	 * The double loot gfx.
	 */
	private static final Graphics DOUBLE_LOOT_GFX = new Graphics(873),

	/**
	 * The triple loot gfx.
	 */
	TRIPLE_LOOT_GFX = new Graphics(874),

	/**
	 * The quadruple loot gfx.
	 */
	QUADRUPLE_LOOT_GFX = new Graphics(875);

	/**
	 * Pick pocketing npc.
	 */
	private NPC npc;

	/**
	 * Data of an npc.
	 */
	private PickPocketableNPC npcData;

	/**
	 * The index to use in the levels required arrays.
	 */
	private int index;

	@Override
	public boolean start(Player player) {
		if (checkAll(player)) {
			int thievingLevel = player.getSkills().getLevel(Skills.THIEVING);
			int agilityLevel = player.getSkills().getLevel(Skills.AGILITY);
			if (Utils.getRandom(50) < 5) {
				for (int i = 0; i < 4; i++) {
					if (npcData.getThievingLevels()[i] <= thievingLevel && npcData.getAgilityLevels()[i] <= agilityLevel) {
						index = i;
					}
				}
			}
			player.faceEntity(npc);
			player.setNextAnimation(getAnimation());
			player.setNextGraphics(getGraphics());
			String message = "You attempt to pick the " + npc.getDefinitions().getName().toLowerCase() + "'s pocket...";
			if (npcData.equals(PickPocketableNPC.DESERT_PHOENIX)) {
				message = "You attempt to grab the phoenix's tail-feather";
			}
			player.getPackets().sendGameMessage(message);
			setActionDelay(player, 3);
			return true;
		}
		return false;
	}

	@Override
	public boolean process(Player player) {
		return checkAll(player);
	}

	@Override
	public int processWithDelay(Player player) {
		if (!isSuccessful(player)) {
			String text = "You fail to pick the " + npc.getDefinitions().getName().toLowerCase() + "'s pocket.";
			if (npcData.equals(PickPocketableNPC.DESERT_PHOENIX)) {
				text = "You fail to grab the feather";
			}
			player.getPackets().sendGameMessage(text);
			if (!npcData.equals(PickPocketableNPC.DESERT_PHOENIX)) {
				npc.setNextAnimation(STUN_ANIMATION);
			}
			npc.faceEntity(player);
			player.applyHit(new Hit(player, npcData.getStunDamage(), HitLook.REGULAR_DAMAGE));
			player.setNextAnimation(new Animation(424));
			player.setNextGraphics(new Graphics(80, 5, 60));
			player.getPackets().sendGameMessage("You've been stunned.");
			if (npcData.equals(PickPocketableNPC.MASTER_FARMER) || npcData.equals(PickPocketableNPC.FARMER)) {
				npc.setNextForceTalk(new ForceTalk("Cor blimey mate, what are ye doing in me pockets?"));
			} else if (npcData.equals(PickPocketableNPC.DESERT_PHOENIX)) {
				npc.setNextForceTalk(new ForceTalk("Squawk!"));
			}else {
				npc.setNextForceTalk(new ForceTalk("What do you think you're doing?"));
			}
			player.getLockManagement().lockActions(npcData.getStunTime() * 1000, LockType.COMBAT, LockType.NPC_INTERACTION, LockType.WALKING, LockType.ITEM_INTERACTION, LockType.PLAYER_INTERACTION, LockType.INTERFACE_INTERACTION);
			stop(player);
		} else {
			String message = getMessage(player);
			if (npcData.equals(PickPocketableNPC.DESERT_PHOENIX)) {
				message = "You grab a tail-feather";
			}
			player.getPackets().sendGameMessage(message);
			double totalXp = npcData.getExperience();
			if (hasTheivingSuit(player)) { totalXp *= 1.025; }
			player.getSkills().addXp(Skills.THIEVING, totalXp);
			for (int i = 0; i <= index; i++) {
				Item item = npcData.getLoot()[Utils.random(npcData.getLoot().length)];
				if (npcData == PickPocketableNPC.THE_GUNS) {
					item.setAmount(Utils.random(3_000, 8_000));
				} else if (npcData == PickPocketableNPC.MAN && npc.getRegionId() == GameConstants.START_PLAYER_LOCATION.getRegionId()) {
					item.setAmount(Utils.random(600, 1_000));
				} else if (npcData == PickPocketableNPC.GUARD && (npc.getRegionId() == 11832 || npc.getRegionId() == 12088)) {
					item.setId(995);
					item.setAmount(Utils.random(1_000, 5_000));
				} else if (npc.getRegionId() == 12093) {
					if (npcData == PickPocketableNPC.HERO) {
						item.setId(995);
						item.setAmount(Utils.random(5_000, 15_000));
					} else if (npcData == PickPocketableNPC.PALADIN) {
						item.setId(995);
						item.setAmount(Utils.random(5_000, 11_000));
					}
				}
				player.getInventory().addItem(item.getId(), item.getAmount());
			}
			if (npcData == PickPocketableNPC.MAN) {
				AchievementHandler.incrementProgress(player, Man_Thief.class);
			}
			if (player.getControllerManager().verifyControlerForOperation(Wilderness.class).isPresent()) {
				player.getInventory().addItemDrop(Wilderness.WILDERNESS_TOKEN, 2);
			}
		}
		return -1;
	}

	@Override
	public void stop(Player player) {
		npc.setNextFaceEntity(null);
		setActionDelay(player, 3);
	}

	/**
	 * Checks if the player is succesfull to thiev or not.
	 *
	 * @param player
	 * 		The player.
	 * @return {@code True} if succesfull, {@code false} if not.
	 */
	private boolean isSuccessful(Player player) {
		/*int thievingLevel = player.getSkills().getLevel(Skills.THIEVING);
		int increasedChance = getIncreasedChance(player);
		int level = Utils.getRandom(thievingLevel + increasedChance);
		double ratio = level / npcData.getThievingLevels()[0];
		if (Math.round(ratio * thievingLevel) < npcData.getThievingLevels()[0] / player.getAuraManager().getThievingAccurayMultiplier()) {
			return false;
		}
		return true;*/
		return Utils.random(4) != 0;
	}

	/**
	 * Gets the message to send when finishing.
	 *
	 * @param player
	 * 		The player.
	 * @return The message.
	 */
	private String getMessage(Player player) {
		switch (index) {
			case 0:
				return "You successfully pick the " + npc.getDefinitions().getName().toLowerCase() + "'s pocket.";
			case 1:
				return "Your lighting-fast reactions allow you to steal double loot.";
			case 2:
				return "Your lighting-fast reactions allow you to steal triple loot.";
			case 3:
				return "Your lighting-fast reactions allow you to steal quadruple loot.";
		}
		return null;
	}

	private boolean hasTheivingSuit(Player player) {
		return player.getEquipment().getHatId() == 21482 && player.getEquipment().getChestId() == 21480 && player.getEquipment().getLegsId() == 21481 && player.getEquipment().getBootsId() == 21483;
	}

	/**
	 * Gets the increased chance for succesfully pickpocketing.
	 *
	 * @param player
	 * 		The player.
	 * @return The amount of increased chance.
	 */
	private int getIncreasedChance(Player player) {
		int chance = 0;
		if (Equipment.getItemSlot(Equipment.SLOT_HANDS) == 10075) { chance += 12; }
		player.getEquipment();
		if (Equipment.getItemSlot(Equipment.SLOT_CAPE) == 15349) { chance += 15; }
		if (npc.getDefinitions().getName().contains("H.A.M")) {
			for (Item item : player.getEquipment().getItems().getItems()) {
				if (item != null && item.getDefinitions().getName().contains("H.A.M")) {
					chance += 3;
				}
			}
		}
		return chance;
	}

	/**
	 * Checks everything before starting.
	 *
	 * @param player
	 * 		The player.
	 */
	private boolean checkAll(Player player) {
		if (player.getSkills().getLevel(Skills.THIEVING) < npcData.getThievingLevels()[0]) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You need a thieving level of " + npcData.getThievingLevels()[0] + " to steal from this npc.");
			return false;
		}
		if (player.getInventory().getFreeSlots() < 1) {
			player.getPackets().sendGameMessage("You don't have enough space in your inventory.");
			return false;
		}
		/*if (player.getAttackedBy() != null && player.getAttackedByDelay() > Utils.currentTimeMillis()) {
			player.getPackets().sendGameMessage("You can't do this while you're under combat.");
			return false;
		}*/
		if (npc.getAttackedBy() != null && npc.getAttackedByDelay() > Utils.currentTimeMillis()) {
			player.getPackets().sendGameMessage("The npc is under combat.");
			return false;
		}
		if (npc.isDead()) {
			player.getPackets().sendGameMessage("Too late, the npc is dead.");
			return false;
		}
		return true;

	}

	/**
	 * Gets the animation to perform.
	 *
	 * @return The animation.
	 */
	private Animation getAnimation() {
		switch (index) {
			case 0:
				return PICKPOCKETING_ANIMATION;
			case 1:
				return DOUBLE_LOOT_ANIMATION;
			case 2:
				return TRIPLE_LOOT_ANIMATION;
			case 3:
				return QUADRUPLE_LOOT_ANIMATION;
		}
		return null;
	}

	/**
	 * Gets the graphic to perform.
	 * @return The graphic.
	 */
	private Graphics getGraphics() {
		switch (index) {
			case 0:
				return null;
			case 1:
				return DOUBLE_LOOT_GFX;
			case 2:
				return TRIPLE_LOOT_GFX;
			case 3:
				return QUADRUPLE_LOOT_GFX;
		}
		return null;
	}

	/**
	 * Constructs a new {@code PickpocketAction} {@code Object}.
	 *
	 * @param npc
	 * 		The npc to whom the player is pickpocketing.
	 * @param npcData
	 * 		Data of an npc.
	 */
	public PickPocketAction(NPC npc, PickPocketableNPC npcData) {
		this.npc = npc;
		this.npcData = npcData;
	}

}
