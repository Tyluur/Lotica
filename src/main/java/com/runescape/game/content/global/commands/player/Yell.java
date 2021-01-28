package com.runescape.game.content.global.commands.player;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.interaction.dialogues.impl.misc.SimplePlayerMessage;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.rights.RightManager;
import com.runescape.utility.Utils;
import com.runescape.utility.external.gson.GsonStartup;
import com.runescape.utility.external.gson.loaders.PunishmentLoader;
import com.runescape.utility.external.gson.resource.Punishment;
import com.runescape.utility.external.gson.resource.Punishment.PunishmentType;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 6/27/2015
 */
public class Yell extends CommandSkeleton<String> {
	
	@Override
	public String getIdentifiers() {
		return "yell";
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		Punishment punishment = GsonStartup.getClass(PunishmentLoader.class).isPunished(new Object[][] { new Object[] { player.getMacAddress(),
				                                                                                                              PunishmentType.MACMUTE },
				                                                                                               new Object[] { player.getUsername(),
						                                                                                                            PunishmentType.MUTE } });
		if (punishment != null) {
			player.getDialogueManager().startDialogue(SimplePlayerMessage.class, "Hmm... It seems like I have been muted.", "It will expire in " + punishment.getTimeLeft() + " though.");
			return;
		}
		if (!hasYellAccess(player)) {
			player.sendMessage("You don't have access to the yell channel.");
			return;
		}
		String message = getCompleted(cmd, 1);
		if (message == null || message.equalsIgnoreCase("null")) {
			return;
		}

		message = Utils.fixChatMessage(message.replaceAll("<", ""));

		StringBuilder tag = new StringBuilder();

		tag.append("[<img=").append(player.getPrimaryRight().getCrownIcon()).append("> ").append("<col=").append(player.getPrimaryRight().getChatColour()).append(">").append(player.getDisplayName()).append("</col>").append("]: ").append(message);
		for (Player pl : World.getPlayers()) {
			if (pl == null || pl.getFacade().isYellOff()) {
				continue;
			}
			pl.sendMessage(tag.toString());
		}
	}

	/**
	 * If the player has access to yell
	 *
	 * @param player
	 * 		The player
	 */
	private boolean hasYellAccess(Player player) {
		return player.isAnyDonator() || player.isStaff() || player.hasPrivilegesOf(RightManager.VETERAN);
	}
}
