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
public class LegendsGuideEmoteTrail extends AbstractEmoteTrail {

	@Override
	public String[] information() {
		return new String[] { "Bow or curtsy outside the entrance to the Legends' Guild.", "Equip iron platelegs, an amulet of strength and oak shortbow." };
	}

	@Override
	public boolean passedRequirements(Player player) {
		return player.getEquipment().getLegsId() == 1067 && player.getEquipment().getAmuletId() == 1725 && player.getEquipment().getWeaponId() == 843;
	}

	@Override
	public Emotes emote() {
		return Emotes.BOW;
	}

	@Override
	public WorldTile coordinates() {
		return new WorldTile(2729, 3348, 0);
	}

	@Override
	public TreasureTrailNPC fighterNPC(Player target) {
		return null;
	}

}
