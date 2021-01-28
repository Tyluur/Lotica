package com.runescape.game.content.economy.treasure.type.emote;

import com.runescape.game.content.economy.treasure.TreasureTrailNPC;
import com.runescape.game.content.economy.treasure.type.AbstractEmoteTrail;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.player.EmotesManager.Emotes;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 16, 2015
 */
public class ArdougneZooEmoteTrail extends AbstractEmoteTrail {

	@Override
	public String[] information() {
		return new String[] { "Blow a raspberry at the monkey cage in Ardougne Zoo.", "Equip a studded body and bronze platelegs." };
	}

	@Override
	public boolean passedRequirements(Player player) {
		return player.getEquipment().getChestId() == 1133 && player.getEquipment().getLegsId() == 1075;
	}

	@Override
	public Emotes emote() {
		return Emotes.RASPBERRY;
	}

	@Override
	public WorldTile coordinates() {
		return new WorldTile(2597, 3277, 0);
	}

	@Override
	public TreasureTrailNPC fighterNPC(Player target) {
		return null;
	}

}
