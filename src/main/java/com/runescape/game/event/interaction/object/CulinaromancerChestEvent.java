package com.runescape.game.event.interaction.object;

import com.runescape.game.event.interaction.type.ObjectInteractionEvent;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.external.gson.GsonStartup;
import com.runescape.utility.external.gson.loaders.StoreLoader;
import com.runescape.utility.world.ClickOption;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 25, 2015
 */
public class CulinaromancerChestEvent extends ObjectInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] { 12309 };
	}

	@Override
	public boolean handleObjectInteraction(Player player, WorldObject object, ClickOption option) {
		switch(option) {
		case FIRST:
			player.getBank().openBank();
			break;
		case SECOND:
			GsonStartup.getOptional(StoreLoader.class).ifPresent(loader -> loader.openStore(player, "Culinaromancer Food"));
			break;
		case THIRD:
			GsonStartup.getOptional(StoreLoader.class).ifPresent(loader -> loader.openStore(player, "Culinaromancer Items"));
			break;
		default:
			break;
		}
		return true;
	}

}
