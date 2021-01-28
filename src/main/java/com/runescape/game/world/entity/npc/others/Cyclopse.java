package com.runescape.game.world.entity.npc.others;

import com.runescape.game.content.global.minigames.warriors.WarriorsGuild;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.godwars.bandos.BandosFaction;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.utility.Utils;

public class Cyclopse extends BandosFaction {

	/**
	 *
	 */
	private static final long serialVersionUID = -348753458086327348L;

	public Cyclopse(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, false);
	}

	@Override
	public void sendDeath(Entity source) {
		super.sendDeath(source);
		if (source instanceof Player) {
			WarriorsGuild.killedCyclopses++;
			final NPC npc = this;
			final Player player = (Player) source;
			if (player.getControllerManager().getController() == null || !(player.getControllerManager().getController() instanceof WarriorsGuild) || Utils.random(player.isAnyDonator() ? 7 : 10) != 0) {
				return;
			}
			WarriorsGuild controler = (WarriorsGuild) player.getControllerManager().getController();
			if (!controler.isInCyclopse()) {
				System.out.println("Player tried to receive drops but wasnt in the cyclopse room");
				return;
			}
			player.sendMessage("You receive a defender drop...");
			sendDrop(player, new Item(WarriorsGuild.getBestDefender(player)), false);
		}
	}
}
