package com.runescape.game.event.interaction.object;

import com.runescape.game.content.skills.Woodcutting;
import com.runescape.game.content.skills.Woodcutting.HatchetDefinitions;
import com.runescape.game.event.interaction.type.ObjectInteractionEvent;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.world.ClickOption;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/4/2015
 */
public class BrimhavenVineInteractionEvent extends ObjectInteractionEvent {
	
	@Override
	public int[] getKeys() {
		return new int[] { 5107 };
	}

	@Override
	public boolean handleObjectInteraction(Player player, WorldObject object, ClickOption option) {
		HatchetDefinitions hatchet = Woodcutting.getHatchet(player);
		if (hatchet == null) {
			player.sendMessage("You must have a hatchet to chop these vines.");
			return true;
		}
		player.setNextWorldTile(object.getWorldTile());
		player.setNextAnimation(new Animation(hatchet.getEmoteId()));
		World.removeObjectTemporary(object, 5_000);
		return true;
	}
}
