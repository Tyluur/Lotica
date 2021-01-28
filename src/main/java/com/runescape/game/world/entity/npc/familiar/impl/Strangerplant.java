package com.runescape.game.world.entity.npc.familiar.impl;

import com.runescape.game.content.skills.summoning.Summoning.Pouches;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.masks.Hit;
import com.runescape.game.world.entity.masks.Hit.HitLook;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.game.world.item.Item;
import com.runescape.utility.Utils;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

public class Strangerplant extends Familiar {

	private static final long serialVersionUID = 2827958223981739176L;

	private int forageTicks;

	public Strangerplant(Player player, Pouches pouch, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
		super(player, pouch, tile, mapAreaNameHash, canBeAttackFromOutOfArea);
		int currentLevel = player.getSkills().getLevelForXp(Skills.FARMING);
		player.getSkills().setLevel(Skills.FARMING, (int) ((1 + (currentLevel * .04)) + currentLevel));
		player.getPackets().sendGameMessage("You feel a sudden urge to plant flowers.");
	}

	@Override
	public void processNPC() {
		super.processNPC();
		forageTicks++;
		if (forageTicks == 750) { addStrangeFruit(); }
	}

	private void addStrangeFruit() {
		getBob().getBeastItems().add(new Item(464, 1));
		forageTicks = 0;
	}

	@Override
	public String getSpecialName() {
		return "Poisonous Blast";
	}

	@Override
	public String getSpecialDescription() {
		return "Attack with 50% chance of poisoning your opponent and inflicting 20 damage";
	}

	@Override
	public int getBOBSize() {
		return 30;
	}

	@Override
	public int getSpecialAmount() {
		return 6;
	}

	@Override
	public SpecialAttack getSpecialAttack() {
		return SpecialAttack.ENTITY;
	}

	@Override
	public boolean submitSpecial(Object object) {
		final Entity target = (Entity) object;
		getOwner().setNextGraphics(new Graphics(1316));
		getOwner().setNextAnimation(new Animation(7660));
		setNextAnimation(new Animation(8211));
		World.sendProjectile(this, target, 1508, 34, 16, 30, 35, 16, 0);
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				target.applyHit(new Hit(getOwner(), Utils.random(20), HitLook.MAGIC_DAMAGE));
				if (Utils.random(1) == 0) { target.getPoison().makePoisoned(60); }
				target.setNextGraphics(new Graphics(1511));
			}
		}, 2);
		return true;
	}
}
