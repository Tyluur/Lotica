package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/19/2015
 */
public class DebugShopItems extends CommandSkeleton<String> {
	
	@Override
	public String getIdentifiers() {
		return "dsi";
	}
	
	@Override
	public void handleCommand(Player player, String[] cmd) {
		String key = "debugging_shop_items";

		if (player.getAttribute(key) == null) {
			boolean append = cmd.length == 2 && Boolean.parseBoolean(cmd[1]);
			player.putAttribute(key, append);
			Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
			clpbrd.setContents(new StringSelection(""), null);
		} else {
			player.removeAttribute(key);
		}

		boolean flag = player.getAttribute(key, false);
		player.sendMessage(player.getAttribute(key) == null ? "You are on regular shop item viewing mode." : (flag ? "You are debugging shop items and the ids will be appended in your clipboard." : "You are debugging shop items and the id will be stored in clipboard."));
	}
}
