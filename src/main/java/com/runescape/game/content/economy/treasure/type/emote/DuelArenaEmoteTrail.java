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
public class DuelArenaEmoteTrail extends AbstractEmoteTrail {

	@Override
	public String[] information() {
		return new String[] { "Bow or curtsy in the lobby of the Duel Arena.", "Equip an iron chainbody, leather chaps and a coif." };
	}

	@Override
	public boolean passedRequirements(Player player) {
		return player.getEquipment().getChestId() == 1101 && player.getEquipment().getHatId() == 1169 && player.getEquipment().getLegsId() == 1095;
	}

	@Override
	public Emotes emote() {
		return Emotes.BOW;
	}

	@Override
	public WorldTile coordinates() {
		return new WorldTile(3314, 3243, 0);
	}

	@Override
	public TreasureTrailNPC fighterNPC(Player target) {
		return null;
	}

}
