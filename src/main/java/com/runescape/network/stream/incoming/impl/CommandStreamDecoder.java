package com.runescape.network.stream.incoming.impl;

import com.runescape.game.GameConstants;
import com.runescape.game.content.global.commands.CommandHandler;
import com.runescape.game.world.entity.player.Player;
import com.runescape.network.stream.InputStream;
import com.runescape.network.stream.incoming.IncomingStreamDecoder;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/27/2016
 */
public class CommandStreamDecoder extends IncomingStreamDecoder {

	@Override
	public int[] getKeys() {
		return new int[] { 70 };
	}

	@Override
	public void decode(Player player, InputStream stream, int packetId, int length) {
		if (!player.isRunning()) {
			return;
		}
		boolean clientCommand = stream.readUnsignedByte() == 1;
		@SuppressWarnings("unused") boolean unknown = stream.readUnsignedByte() == 1;
		String command = stream.readString();
		if (!CommandHandler.handleIncomingCommand(player, command, clientCommand) && GameConstants.DEBUG) {
			System.out.println("Command: " + command);
		}
	}
}
