package com.runescape.game.content.global.commands.player;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.event.interaction.button.RewardPetInterfaceInteractionEvent;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 8/24/2016
 */
public class Pets extends CommandSkeleton<String> {

	@Override
	public String getIdentifiers() {
		return "pets";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		RewardPetInterfaceInteractionEvent.display(player);
	}
}
