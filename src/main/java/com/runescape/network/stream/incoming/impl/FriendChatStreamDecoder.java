package com.runescape.network.stream.incoming.impl;

import com.runescape.game.content.FriendChatsManager;
import com.runescape.game.content.unique.quickchat.QuickChatType;
import com.runescape.game.interaction.dialogues.impl.misc.SimplePlayerMessage;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.QuickChatMessage;
import com.runescape.network.stream.InputStream;
import com.runescape.network.stream.incoming.IncomingStreamDecoder;
import com.runescape.utility.Utils;
import com.runescape.utility.cache.huffman.Huffman;
import com.runescape.utility.external.gson.GsonStartup;
import com.runescape.utility.external.gson.loaders.PunishmentLoader;
import com.runescape.utility.external.gson.resource.Punishment;
import com.runescape.utility.external.gson.resource.Punishment.PunishmentType;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/27/2016
 */
public class FriendChatStreamDecoder extends IncomingStreamDecoder {

	private final static int JOIN_FRIEND_CHAT_PACKET = 1;

	private final static int CHANGE_FRIEND_RANK = 41;

	private final static int KICK_FRIEND_CHAT_PACKET = 32;

	private final static int SEND_FRIEND_MESSAGE_PACKET = 72;

	private final static int SEND_FRIEND_QUICK_CHAT_PACKET = 79;

	@Override
	public int[] getKeys() {
		return new int[] { 1, 41, 32, 72, 79 };
	}

	@Override
	public void decode(Player player, InputStream stream, int packetId, int length) {

		switch (packetId) {
			case JOIN_FRIEND_CHAT_PACKET:
				if (!player.hasStarted()) {
					return;
				}
				FriendChatsManager.joinChat(stream.readString(), player);
				break;
			case CHANGE_FRIEND_RANK:
				if (!player.hasStarted() || !player.getInterfaceManager().containsInterface(1108)) {
					return;
				}
				player.getFriendsIgnores().changeRank(stream.readString(), stream.readUnsignedByteC());
				break;
			case KICK_FRIEND_CHAT_PACKET:
				if (!player.hasStarted()) {
					return;
				}
				player.setLastPublicMessage(Utils.currentTimeMillis() + 1000); // avoids-message-appearing
				player.kickPlayerFromFriendsChannel(stream.readString());
				break;
			case SEND_FRIEND_MESSAGE_PACKET:
				if (!player.hasStarted()) {
					return;
				}
				Punishment punishment = GsonStartup.getClass(PunishmentLoader.class).isPunished(new Object[][] { new Object[] { player.getMacAddress(), PunishmentType.MACMUTE }, new Object[] { player.getUsername(), PunishmentType.MUTE } });
				if (punishment != null) {
					player.getDialogueManager().startDialogue(SimplePlayerMessage.class, "Hmm... It seems like I have been muted.", "It will expire in " + punishment.getTimeLeft() + " though.");
					return;
				}
				String username = stream.readString();
				Player p2 = World.getPlayerByDisplayName(username);
				if (p2 == null) {
					return;
				}
				player.getFriendsIgnores().sendMessage(p2, Utils.fixChatMessage(Huffman.readEncryptedMessage(150, stream)));
				break;
			case SEND_FRIEND_QUICK_CHAT_PACKET:
				handleFriendQuickChat(player, stream);
				break;
		}
	}

	private void handleFriendQuickChat(Player player, InputStream stream) {
		if (!player.hasStarted())
			return;
		String username = stream.readString();
		int fileId = stream.readUnsignedShort();
		QuickChatType type = QuickChatType.getQuickChatType(fileId);
		if (type == null) {
			return;
		}
		int[] params = type.unpack(stream);
		QuickChatMessage message = new QuickChatMessage(type, params);
		Utils.completeQuickMessage(player, message);
		Player p2 = World.getPlayerByDisplayName(username);
		if (p2 == null) {
			return;
		}
		player.getFriendsIgnores().sendQuickChatMessage(p2, message);
	}
}
