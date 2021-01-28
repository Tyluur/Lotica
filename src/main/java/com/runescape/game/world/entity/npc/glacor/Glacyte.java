package com.runescape.game.world.entity.npc.glacor;

import com.runescape.game.world.World;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Hit;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since May 3, 2014
 */
@SuppressWarnings("serial")
public class Glacyte extends NPC {

	public Glacyte(Glacor glacor, int id, GlacyteType type, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
		setCapDamage(900);
		setSpawned(true);
		/** Setting final variables */
		this.glacor = glacor;
		this.type = type;
		if (type == null) {
			System.err.println("Glacor type is null!");
		}
		if (glacor.getCombat().getTarget() != null) {
			getCombat().setTarget(glacor.getCombat().getTarget());
		}
	}

	@Override
	public void handleIngoingHit(final Hit hit) {
		if (type != null) {
			type.handleIncomingHit(this, hit);
		}
		super.handleIngoingHit(hit);
	}

	@Override
	public void processNPC() {
		super.processNPC();
		if (type != null) {
			type.processGlacyte(this);
		}
		if (getCombat().getTarget() == null && targetName != null) {
			Entity target = World.getPlayer(targetName);
			getCombat().setTarget(target);
		}
	}

	@Override
	public void drop() {
		if (glacor != null) {
			glacor.getGlacytes().remove(this);
			Player player = getMostDamageReceivedSourcePlayer();
			if (player != null) {
				int amount = glacor.getGlacytes().size();
				player.sendMessage("I have " + amount + " more glacyte" + (amount == 1 ? "" : "s") + " to kill...");
			}
		}
	}

	/**
	 * @return the targetName
	 */
	public String getTargetName() {
		return targetName;
	}

	/**
	 * Checking if the target name is correct with the player name
	 *
	 * @param name
	 * 		The name to check for
	 */
	public boolean correctName(String name) {
		return targetName != null && name.equalsIgnoreCase(targetName);
	}

	/**
	 * @param targetName
	 * 		the targetName to set
	 */
	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	private String targetName;

	private final Glacor glacor;

	private final GlacyteType type;
}