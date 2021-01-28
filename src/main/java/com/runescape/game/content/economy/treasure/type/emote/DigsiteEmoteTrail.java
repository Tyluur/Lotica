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
public class DigsiteEmoteTrail extends AbstractEmoteTrail {

	@Override
	public String[] information() {
		return new String[] { "Beckon in the Digsite, near the eastern winch. Beware of double agents!", "Equip iron platelegs, and an iron pickaxe. " };
	}

	@Override
	public boolean passedRequirements(Player player) {
		return player.getEquipment().getLegsId() == 1067 && player.getEquipment().getWeaponId() == 1267;
	}

	@Override
	public Emotes emote() {
		return Emotes.BECKON;
	}

	@Override
	public WorldTile coordinates() {
		return new WorldTile(3370, 3425, 0);
	}

	@Override
	public TreasureTrailNPC fighterNPC(Player target) {
		return new TreasureTrailNPC(target, doubleAgentId, new WorldTile(3370, 3422, 0));
	}

}
