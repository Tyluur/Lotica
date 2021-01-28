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
public class FishingGuildEmoteTrail extends AbstractEmoteTrail {

	@Override
	public String[] information() {
		return new String[] { "Blow a raspberry in the Fishing Guild bank. Beware of double agents!", "Equip an elemental shield, blue dragonhide chaps, and a rune warhammer." };
	}

	@Override
	public boolean passedRequirements(Player player) {
		return player.getEquipment().getShieldId() == 2890 && player.getEquipment().getWeaponId() == 1347 && player.getEquipment().getLegsId() == 2493;
	}

	@Override
	public Emotes emote() {
		return Emotes.RASPBERRY;
	}

	@Override
	public WorldTile coordinates() {
		return new WorldTile(2586, 3421, 0);
	}

	@Override
	public TreasureTrailNPC fighterNPC(Player target) {
		return new TreasureTrailNPC(target, doubleAgentId, new WorldTile(2586, 3423, 0));
	}

}
