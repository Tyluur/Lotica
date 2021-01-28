package com.runescape.network.stream.incoming.impl;

import com.runescape.game.world.entity.player.Player;
import com.runescape.network.stream.InputStream;
import com.runescape.network.stream.incoming.IncomingStreamDecoder;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/27/2016
 */
public class CommunicationStreamDecoder extends IncomingStreamDecoder {

	private final static int ADD_FRIEND_PACKET = 51;

	private final static int ADD_IGNORE_PACKET = 17;

	private final static int REMOVE_IGNORE_PACKET = 38;

	private final static int REMOVE_FRIEND_PACKET = 8;

	@Override
	public int[] getKeys() {
		return new int[] { 51, 17, 38, 8 };
	}

	@Override
	public void decode(Player player, InputStream stream, int packetId, int length) {
		switch (packetId) {
			case ADD_FRIEND_PACKET:
				if (!player.hasStarted()) {
					return;
				}
				player.getFriendsIgnores().addFriend(stream.readString());
				break;
			case ADD_IGNORE_PACKET:
				if (!player.hasStarted()) {
					return;
				}
				player.getFriendsIgnores().addIgnore(stream.readString(), stream.readUnsignedByte() == 1);
				break;
			case REMOVE_FRIEND_PACKET:
				if (!player.hasStarted()) {
					return;
				}
				player.getFriendsIgnores().removeFriend(stream.readString());
				break;
			case REMOVE_IGNORE_PACKET:
				if (!player.hasStarted()) {
					return;
				}
				player.getFriendsIgnores().removeIgnore(stream.readString());
				break;
		}
	}
}
