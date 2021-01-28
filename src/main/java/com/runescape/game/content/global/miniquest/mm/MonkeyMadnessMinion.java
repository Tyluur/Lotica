package com.runescape.game.content.global.miniquest.mm;

import com.runescape.game.interaction.dialogues.impl.misc.SimpleNPCMessage;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.ForceTalk;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.masks.Hit;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.utility.Utils;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 7, 2015
 */
public class MonkeyMadnessMinion extends NPC {
	private static final long serialVersionUID = -6044069967056422704L;

	public MonkeyMadnessMinion(MonkeyMadnessBoss boss) {
		super(14211, boss.getWorldTile());
		this.setForceMultiArea(true);
		this.setSpawned(true);
		this.boss = boss;
	}

	@Override
	public void processNPC() {
		super.processNPC();
		if (boss.isEnraged()) {
			int random = Utils.random(1, 10);
			if (random >= 7) {
				heal();
			}
		} else {
			heal();
		}
	}
	
	@Override
	public void handleIngoingHit(final Hit hit) {
		super.handleIngoingHit(hit);
		if (!boss.isEnraged()) {
			hit.setDamage(0);
			if (hit.getSource().isPlayer()) {
				hit.getSource().player().getDialogueManager().startDialogue(SimpleNPCMessage.class, 1411, "Listen! You can't kill these minions until the demon has dropped under 50% health.", "He will get harder to kill at this point, so watch out.");
			}
		}
	}
	
	@Override
	public void sendDeath(Entity source) {
		super.sendDeath(source);
		boss.getMinions().remove(this);
	}

	/**
	 * Heals the {@link #boss} and sets the {@link #nextHealTime} to an upcoming
	 * time.
	 */
	private void heal() {
		if (nextHealTime == -1 || (System.currentTimeMillis() >= nextHealTime)) {
			boss.setNextGraphics(new Graphics(444));
			boss.heal(boss.isEnraged() ? Utils.random(70, 150) : Utils.random(10, 30));
			nextHealTime = System.currentTimeMillis() + Utils.random(2000, 6000);
			
			setNextForceTalk(new ForceTalk("Take some health, master!"));
			setNextFaceEntity(boss);
		}
	}

	/**
	 * The next time we should heal the boss
	 */
	private long nextHealTime = -1;

	/**
	 * @return the boss
	 */
	public MonkeyMadnessBoss getBoss() {
		return boss;
	}

	/**
	 * The boss we are the minion of
	 */
	private final MonkeyMadnessBoss boss;
}