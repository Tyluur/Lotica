package com.runescape.network.stream.incoming.impl;

import com.runescape.game.GameConstants;
import com.runescape.game.content.global.commands.CommandHandler;
import com.runescape.game.content.unique.quickchat.QuickChatType;
import com.runescape.game.interaction.dialogues.impl.misc.SimplePlayerMessage;
import com.runescape.game.world.entity.player.ChatMessage;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.PublicChatMessage;
import com.runescape.game.world.entity.player.QuickChatMessage;
import com.runescape.network.stream.InputStream;
import com.runescape.network.stream.incoming.IncomingStreamDecoder;
import com.runescape.utility.Utils;
import com.runescape.utility.cache.huffman.Huffman;
import com.runescape.utility.external.gson.GsonStartup;
import com.runescape.utility.external.gson.loaders.PunishmentLoader;
import com.runescape.utility.external.gson.resource.Punishment;
import com.runescape.utility.external.gson.resource.Punishment.PunishmentType;
import com.runescape.workers.game.core.CoresManager;
import com.runescape.workers.game.log.GameLog;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/27/2016
 */
public class ChatStreamDecoder extends IncomingStreamDecoder {

	private final static int CHAT_TYPE_PACKET = 23;
	private final static int CHAT_PACKET = 36;
	private final static int PUBLIC_QUICK_CHAT_PACKET = 30;

	@Override
	public int[] getKeys() {
		return new int[] { 23, 36, 30 };
	}

	@Override
	public void decode(Player player, InputStream stream, int packetId, int length) {
		switch (packetId) {
			case CHAT_TYPE_PACKET:
				int type = stream.readUnsignedByte();
				player.setChatType(type);
				break;
			case CHAT_PACKET:
				if (!player.hasStarted()) {
					return;
				}
				if (player.getLastPublicMessage() > Utils.currentTimeMillis()) {
					return;
				}
				player.setLastPublicMessage(Utils.currentTimeMillis() + 300);
				int colorEffect = stream.readUnsignedByte();
				int moveEffect = stream.readUnsignedByte();
				String message = Huffman.readEncryptedMessage(250, stream);
				if (message == null || message.replaceAll(" ", "").equals("")) {
					return;
				}
				if (colorEffect == 255 || moveEffect == 255) {
					System.out.println(player.getUsername() + " is attempting to exploit a chat bug (resolved).");
					return;
				}
				if (message.startsWith("::") || message.startsWith(";;")) {
					CommandHandler.handleIncomingCommand(player, message, false);
					return;
				}
				Punishment punishment = GsonStartup.getClass(PunishmentLoader.class).isPunished(new Object[][] { new Object[] { player.getMacAddress(), PunishmentType.MACMUTE }, new Object[] { player.getUsername(), PunishmentType.MUTE } });
				if (punishment != null) {
					player.getDialogueManager().startDialogue(SimplePlayerMessage.class, "Hmm... It seems like I have been muted.", "It will expire in " + punishment.getTimeLeft() + " though.");
					return;
				}
				CoresManager.LOG_PROCESSOR.appendLog(new GameLog("chat", player.getUsername(), "Said:\t" + message));
				int effects = (colorEffect << 8) | (moveEffect & 0xff) & ~0x8000;
				if (player.getChatType() == 1) {
					player.sendFriendsChannelMessage(player.setLastChatMessage(new ChatMessage(message)));
				} else if (player.getChatType() == 2) {
					player.sendClanChannelMessage(player.setLastChatMessage(new ChatMessage(message)));
				} else if (player.getChatType() == 3) {
					player.sendGuestClanChannelMessage(player.setLastChatMessage(new ChatMessage(message)));
				} else {
					player.sendPublicChatMessage(player.setLastChatMessage(new PublicChatMessage(message, effects)));
				}
				player.setLastMsg(message);
				if (GameConstants.DEBUG) {
					System.out.println("Chat type: " + player.getChatType() + ", effects:\t" + effects);
				}
				break;
			case PUBLIC_QUICK_CHAT_PACKET:
				decodePublicQuickChat(player, stream);
				break;
		}
	}

	private void decodePublicQuickChat(Player player, InputStream stream) {
		if (!player.hasStarted())
			return;
		if (player.getLastPublicMessage() > Utils.currentTimeMillis())
			return;
		player.setLastPublicMessage(Utils.currentTimeMillis() + 300);
		int chatMode = stream.readUnsignedByte();
		int fileId = stream.readUnsignedShort();
		QuickChatType type = QuickChatType.getQuickChatType(fileId);
		if (type == null) {
			return;
		}
		int[] params = type.unpack(stream);
		QuickChatMessage message = new QuickChatMessage(type, params);
		Utils.completeQuickMessage(player, message);
		if (chatMode == 0) {
			player.sendPublicChatMessage(message);
		} else if (chatMode == 1) {
			player.sendFriendsChannelQuickMessage(message);
		}
	}
}
