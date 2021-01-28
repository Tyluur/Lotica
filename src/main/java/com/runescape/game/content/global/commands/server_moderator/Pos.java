package com.runescape.game.content.global.commands.server_moderator;

import com.runescape.game.GameConstants;
import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 18, 2015
 */
public class Pos extends CommandSkeleton<String[]> {

	@Override
	public String[] getIdentifiers() {
		return new String[] { "pos" };
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		boolean toClipboard = cmd.length == 2 && Boolean.parseBoolean(cmd[1]);
		player.getPackets().sendGameMessage("Coords: " + player.getX() + ", " + player.getY() + ", " + player.getPlane() + ", regionId: " + player.getRegionId() + ", rx: " + player.getChunkX() + ", ry: " + player.getChunkY(), true);
		String text = "new WorldTile(" + player.getX() + ", " + player.getY() + ", " + player.getPlane() + ")";

		if (GameConstants.DEBUG) { System.out.println(text + ", " + toClipboard + ", regionId=" + player.getRegionId()); }

		if (toClipboard) {
			StringSelection stringSelection = new StringSelection(text);
			Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
			clpbrd.setContents(stringSelection, null);
		}

		//System.out.println("new WorldTile(" + (player.getX() - 64) + ", " + (player.getY() - 64) + ", " + player.getPlane() + ")");
		//System.out.println("Coords: " + player.getX() + ", " + player.getY() + ", " + player.getPlane() + ", regionId: " + player.getRegionId() + ", rx: " + player.getChunkX() + ", ry: " + player.getChunkY());
	}

}
