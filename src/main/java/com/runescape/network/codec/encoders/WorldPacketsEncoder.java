package com.runescape.network.codec.encoders;

import com.runescape.game.GameConstants;
import com.runescape.game.content.FriendChatsManager;
import com.runescape.game.content.economy.exchange.ExchangeConfiguration.Progress;
import com.runescape.game.content.global.clans.ClansManager;
import com.runescape.game.event.InputEvent;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.*;
import com.runescape.game.world.item.FloorItem;
import com.runescape.game.world.item.Item;
import com.runescape.game.world.item.ItemsContainer;
import com.runescape.game.world.region.DynamicRegion;
import com.runescape.game.world.region.Region;
import com.runescape.network.Session;
import com.runescape.network.codec.Encoder;
import com.runescape.network.stream.OutputStream;
import com.runescape.utility.Utils;
import com.runescape.utility.cache.huffman.Huffman;
import com.runescape.utility.world.map.MapArchiveKeys;
import com.runescape.workers.game.core.CoresManager;
import com.runescape.workers.game.log.GameLog;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

public class WorldPacketsEncoder extends Encoder {

	private Player player;

	public WorldPacketsEncoder(Session session, Player player) {
		super(session);
		this.player = player;
	}

	public Player getPlayer() {
		return player;
	}

	public void sendResetMinimapFlag() {
		OutputStream stream = new OutputStream(3);
		stream.writePacket(player, 55);
		stream.writeByte(255);
		stream.writeByte128(255);
		session.write(stream);
	}

	/**
	 * Send a runtime message to the client
	 *
	 * @param message
	 * 		The message to execute on the client's computer
	 */
	public void sendRuntimeMessage(String message) {
		sendMessage(1337, message, null);
	}

	public void sendMessage(int type, String text, Player p) {
		int maskData = 0;
		if (p != null) {
			maskData |= 0x1;
			if (p.hasDisplayName()) {
				maskData |= 0x2;
			}
		}
		OutputStream stream = new OutputStream();
		stream.writePacketVarByte(player, 102);
		stream.writeSmart(type);
		stream.writeInt(0); // junk, not used by client
		stream.writeByte(maskData);
		if ((maskData & 0x1) != 0) {
			stream.writeString(Utils.formatPlayerNameForDisplay(p.getUsername()));
			if (p.hasDisplayName()) { stream.writeString(p.getDisplayName()); }
		}
		stream.writeString(text);
		stream.endPacketVarByte();
		session.write(stream);
	}

	/**
	 * Sends the URL open request to the client
	 *
	 * @param website
	 * 		The website to open on the client's computer
	 */
	public void sendOpenURL(String website) {
		sendMessage(1338, website, null);
	}

	public void sendMinimapFlag(int x, int y) {
		OutputStream stream = new OutputStream(3);
		stream.writePacket(player, 55);
		stream.writeByte(x);
		stream.writeByte128(y);
		session.write(stream);
	}

	public void sendNPCMessage(int border, NPC npc, String message) {
		sendGameMessage(message);
	}

	public void sendGameMessage(String text) {
		sendGameMessage(text, false);
	}

	public void sendGameMessage(String text, boolean... filter) {
		sendMessage(filter.length == 1 && filter[0] ? 109 : 0, text, null);
	}

	public void sendItems(int key, ItemsContainer<Item> items) {
		sendItems(key, key < 0, items);
	}

	public void sendItems(int key, boolean keyLessIntegerSize, ItemsContainer<Item> items) {
		sendItems(key, keyLessIntegerSize, items.getItems());
	}

	public void sendItems(int key, boolean negativeKey, Item[] items) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarShort(player, 37);
		stream.writeShort(negativeKey ? key : key);
		stream.writeByte(negativeKey ? 1 : 0);
		stream.writeShort(items.length);
		for (Item item : items) {
			int id = -1;
			int amount = 0;
			if (item != null) {
				id = item.getId();
				amount = item.getAmount();
			}
			stream.writeByte(amount >= 255 ? 255 : amount);
			if (amount >= 255) {
				stream.writeInt(amount);
			}
			stream.writeShortLE(id + 1);
		}
		stream.endPacketVarShort();
		session.write(stream);
	}

	public void sendItems(int key, Item[] items) {
		sendItems(key, key < 0, items);
	}

	public void sendPlayerUnderNPCPriority(boolean priority) {
		OutputStream stream = new OutputStream(2);
		stream.writePacket(player, 123);
		stream.write128Byte(priority ? 1 : 0);
		session.write(stream);
	}

	public void sendHintIcon(HintIcon icon) {
		OutputStream stream = new OutputStream(13);
		stream.writePacket(player, 81);
		stream.writeByte((icon.getTargetType() & 0x1f) | (icon.getIndex() << 5));
		if (icon.getTargetType() == 0) {
			stream.skip(11);
		} else {
			stream.writeByte(icon.getArrowType());
			if (icon.getTargetType() == 1 || icon.getTargetType() == 10) {
				stream.writeShort(icon.getTargetIndex());
				stream.writeShort(0); // unknown
				stream.skip(4);
			} else if ((icon.getTargetType() >= 2 && icon.getTargetType() <= 6)) { // directions
				stream.writeByte(0); // unknown
				stream.writeShort(icon.getCoordX());
				stream.writeShort(icon.getCoordY());
				stream.writeByte(icon.getDistanceFromFloor() * 4 >> 2);
				stream.writeShort(0); // unknown
			}
			stream.writeShort(icon.getModelId());
		}
		session.write(stream);

	}

	public void sendCameraShake(int slotId, int b, int c, int d, int e) {
		OutputStream stream = new OutputStream(7);
		stream.writePacket(player, 34);
		stream.write128Byte(b);
		stream.writeByte128(slotId);
		stream.writeShortLE128(e);
		stream.write128Byte(c);
		stream.write128Byte(d);
		session.write(stream);
	}

	public void sendStopCameraShake() {
		OutputStream stream = new OutputStream(1);
		stream.writePacket(player, 15);
		session.write(stream);
	}

	public void sendIComponentModel(int interfaceId, int componentId, int modelId) {
		OutputStream stream = new OutputStream(7);
		stream.writePacket(player, 58);
		stream.writeIntV1(interfaceId << 16 | componentId);
		stream.writeShort128(modelId);
		session.write(stream);
	}

	public void sendScrollIComponent(int interfaceId, int componentId, int value) {
		OutputStream stream = new OutputStream(7);
		stream.writePacket(player, 8);
		stream.writeShort128(value);
		stream.writeIntLE(interfaceId << 16 | componentId);
		session.write(stream);
	}

	public void sendHideIComponent(int interfaceId, int componentId, boolean hidden) {
		OutputStream stream = new OutputStream(6);
		stream.writePacket(player, 117);
		stream.writeIntV1(interfaceId << 16 | componentId);
		stream.writeByte128(hidden ? 1 : 0);
		session.write(stream);
	}

	public void sendRemoveGroundItem(FloorItem item) {
		sendWorldTile(item.getTile());
		int localX = item.getTile().getLocalX(player.getLastLoadedMapRegionTile(), player.getMapSize());
		int localY = item.getTile().getLocalY(player.getLastLoadedMapRegionTile(), player.getMapSize());
		int offsetX = localX - ((localX >> 3) << 3);
		int offsetY = localY - ((localY >> 3) << 3);
		OutputStream stream = new OutputStream(4);
		stream.writePacket(player, 16);
		stream.writeShort(item.getId());
		stream.writeByte((offsetX << 4) | offsetY);
		session.write(stream);

	}

	public void sendWorldTile(WorldTile tile) {
		OutputStream stream = new OutputStream(3);
		stream.writePacket(player, 46);
		stream.writeByte128(tile.getLocalX(player.getLastLoadedMapRegionTile(), player.getMapSize()) >> 3);
		stream.writeByte(tile.getLocalY(player.getLastLoadedMapRegionTile(), player.getMapSize()) >> 3);
		stream.write128Byte(tile.getPlane());
		session.write(stream);
	}

	public void sendGroundItem(FloorItem item) {
		sendWorldTile(item.getTile());
		int localX = item.getTile().getLocalX(player.getLastLoadedMapRegionTile(), player.getMapSize());
		int localY = item.getTile().getLocalY(player.getLastLoadedMapRegionTile(), player.getMapSize());
		int offsetX = localX - ((localX >> 3) << 3);
		int offsetY = localY - ((localY >> 3) << 3);
		OutputStream stream = new OutputStream(6);
		stream.writePacket(player, 48);
		stream.writeByteC((offsetX << 4) | offsetY);
		stream.writeShort128(item.getId());
		stream.writeShort(item.getAmount());
		session.write(stream);
	}

	public void sendProjectile(Entity receiver, WorldTile startTile, WorldTile endTile, int gfxId, int startHeight, int endHeight, int speed, int delay, int curve, int startDistanceOffset, int creatorSize) {
		sendWorldTile(startTile);
		OutputStream stream = new OutputStream(17);
		stream.writePacket(player, 62);
		int localX = startTile.getLocalX(player.getLastLoadedMapRegionTile(), player.getMapSize());
		int localY = startTile.getLocalY(player.getLastLoadedMapRegionTile(), player.getMapSize());
		int offsetX = localX - ((localX >> 3) << 3);
		int offsetY = localY - ((localY >> 3) << 3);
		stream.writeByte((offsetX << 3) | offsetY);
		stream.writeByte(endTile.getX() - startTile.getX());
		stream.writeByte(endTile.getY() - startTile.getY());
		stream.writeShort(receiver == null ? 0 : (receiver instanceof Player ? -(receiver.getIndex() + 1) : receiver.getIndex() + 1));
		stream.writeShort(gfxId);
		stream.writeByte(startHeight);
		stream.writeByte(endHeight);
		stream.writeShort(delay);
		int duration = (Utils.getDistance(startTile.getX(), startTile.getY(), endTile.getX(), endTile.getY()) * 30 / ((speed / 10) < 1 ? 1 : (speed / 10))) + delay;
		stream.writeShort(duration);
		stream.writeByte(curve);
		stream.writeShort(creatorSize * 64 + startDistanceOffset * 64);
		session.write(stream);

	}

	public void sendUnlockIComponentOptionSlots(int interfaceId, int componentId, int fromSlot, int toSlot, int... optionsSlots) {
		int settingsHash = 0;
		for (int slot : optionsSlots) {
			settingsHash |= 2 << slot;
		}
		sendIComponentSettings(interfaceId, componentId, fromSlot, toSlot, settingsHash);
	}

	public void sendIComponentSettings(int interfaceId, int componentId, int fromSlot, int toSlot, int settingsHash) {
		OutputStream stream = new OutputStream(13);
		stream.writePacket(player, 3);
		stream.writeShortLE(fromSlot);
		stream.writeIntV2(interfaceId << 16 | componentId);
		stream.writeShort128(toSlot);
		stream.writeIntLE(settingsHash);
		session.write(stream);
	}

	public void sendInterSetItemsOptionsScript(int interfaceId, int componentId, int key, int width, int height, String... options) {
		Object[] parameters = new Object[6 + options.length];
		int index = 0;
		for (int count = options.length - 1; count >= 0; count--) {
			parameters[index++] = options[count];
		}
		parameters[index++] = -1; // dunno but always this
		parameters[index++] = 0;// dunno but always this
		parameters[index++] = height;
		parameters[index++] = width;
		parameters[index++] = key;
		parameters[index++] = interfaceId << 16 | componentId;
		sendRunScript(150, parameters); // scriptid 150 does that the method
		// name says*/
	}

	public void sendRunScript(int scriptId, Object... params) {
		try {
			OutputStream stream = new OutputStream();
			stream.writePacketVarShort(player, 50);
			String parameterTypes = "";
			if (params != null) {
				for (int count = params.length - 1; count >= 0; count--) {
					if (params[count] instanceof String) {
						parameterTypes += "s"; // string
					} else {
						parameterTypes += "i"; // integer
					}
				}
			}
			stream.writeString(parameterTypes);
			if (params != null) {
				int index = 0;
				for (int count = parameterTypes.length() - 1; count >= 0; count--) {
					if (parameterTypes.charAt(count) == 's') {
						stream.writeString((String) params[index++]);
					} else {
						stream.writeInt((Integer) params[index++]);
					}
				}
			}
			stream.writeInt(scriptId);
			stream.endPacketVarShort();
			session.write(stream);
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendGlobalConfig(int id, int value) {
		if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
			sendGlobalConfig2(id, value);
		} else {
			sendGlobalConfig1(id, value);
		}
	}

	public void sendGlobalConfig2(int id, int value) {
		OutputStream stream = new OutputStream(7);
		stream.writePacket(player, 112);
		stream.writeShortLE(id);
		stream.writeInt(value);
		session.write(stream);
	}

	public void sendGlobalConfig1(int id, int value) {
		OutputStream stream = new OutputStream(4);
		stream.writePacket(player, 111);
		stream.writeShortLE128(id);
		stream.write128Byte(value);
		session.write(stream);
	}

	public void sendConfigByFile(int fileId, int value) {
		if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
			sendConfigByFile2(fileId, value);
		} else {
			sendConfigByFile1(fileId, value);
		}
	}

	public void sendConfigByFile2(int fileId, int value) {
		OutputStream stream = new OutputStream(7);
		stream.writePacket(player, 84);
		stream.writeInt(value);
		stream.writeShort(fileId);
		session.write(stream);
	}

	public void sendConfigByFile1(int fileId, int value) {
		OutputStream stream = new OutputStream(4);
		stream.writePacket(player, 14);
		stream.write128Byte(value);
		stream.writeShort128(fileId);
		session.write(stream);
	}

	public void sendRunEnergy() {
		OutputStream stream = new OutputStream(2);
		stream.writePacket(player, 13);
		stream.writeByte(player.getEnergyValue());
		session.write(stream);
	}

	public void sendIComponentText(int interfaceId, int componentId, String text) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarShort(player, 33);
		stream.writeInt(interfaceId << 16 | componentId);
		stream.writeString(text);
		stream.endPacketVarShort();
		session.write(stream);
	}

	public void sendIComponentAnimation(int emoteId, int interfaceId, int componentId) {

		OutputStream stream = new OutputStream(7);
		stream.writePacket(player, 23);
		stream.writeShortLE128(emoteId);
		stream.writeIntV1(interfaceId << 16 | componentId);
		session.write(stream);

	}

	public void sendItemOnIComponent(int interfaceid, int componentId, int id, int amount) {

		OutputStream stream = new OutputStream(11);
		stream.writePacket(player, 9);
		stream.writeShortLE(id);
		stream.writeInt(amount);
		stream.writeIntV2(interfaceid << 16 | componentId);
		session.write(stream);

	}

	public void sendEntityOnIComponent(boolean isPlayer, int entityId, int interfaceId, int componentId) {
		if (isPlayer) {
			sendPlayerOnIComponent(interfaceId, componentId);
		} else {
			sendNPCOnIComponent(interfaceId, componentId, entityId);
		}
	}

	public void sendPlayerOnIComponent(int interfaceId, int componentId) {
		OutputStream stream = new OutputStream(5);
		stream.writePacket(player, 114);
		stream.writeIntLE(interfaceId << 16 | componentId);
		session.write(stream);

	}

	public void sendNPCOnIComponent(int interfaceId, int componentId, int npcId) {
		OutputStream stream = new OutputStream(7);
		stream.writePacket(player, 98);
		stream.writeInt(interfaceId << 16 | componentId);
		stream.writeShortLE(npcId);
		session.write(stream);

	}

	public void sendObjectAnimation(WorldObject object, Animation animation) {
		OutputStream stream = new OutputStream(8);
		stream.writePacket(player, 96);
		stream.writeIntV2(object.getTileHash());
		stream.writeShort128(animation.getIds()[0]);
		stream.write128Byte((object.getType() << 2) + (object.getRotation() & 0x3));
		session.write(stream);
	}

	public void sendTileMessage(String message, WorldTile tile, int color) {
		sendTileMessage(message, tile, 5000, 255, color);
	}

	public void sendTileMessage(String message, WorldTile tile, int delay, int height, int color) {
		sendWorldTile(tile);
		OutputStream stream = new OutputStream();
		stream.writePacketVarByte(player, 32);
		stream.skip(1);
		int localX = tile.getLocalX(player.getLastLoadedMapRegionTile(), player.getMapSize());
		int localY = tile.getLocalY(player.getLastLoadedMapRegionTile(), player.getMapSize());
		int offsetX = localX - ((localX >> 3) << 3);
		int offsetY = localY - ((localY >> 3) << 3);
		stream.writeByte((offsetX << 4) | offsetY);
		stream.writeShort(delay / 30);
		stream.writeByte(height);
		stream.write24BitInteger(color);
		stream.writeString(message);
		stream.endPacketVarByte();
		session.write(stream);
	}

	public void sendSpawnedObject(WorldObject object) {
		sendWorldTile(object);
		int localX = object.getLocalX(player.getLastLoadedMapRegionTile(), player.getMapSize());
		int localY = object.getLocalY(player.getLastLoadedMapRegionTile(), player.getMapSize());
		int offsetX = localX - ((localX >> 3) << 3);
		int offsetY = localY - ((localY >> 3) << 3);
		OutputStream stream = new OutputStream(5);
		stream.writePacket(player, 28);
		stream.writeByte((offsetX << 4) | offsetY);
		stream.writeByte((object.getType() << 2) + (object.getRotation() & 0x3));
		stream.writeShort128(object.getId());
		session.write(stream);
	}

	public void sendDestroyObject(WorldObject object) {
		sendWorldTile(object);
		int localX = object.getLocalX(player.getLastLoadedMapRegionTile(), player.getMapSize());
		int localY = object.getLocalY(player.getLastLoadedMapRegionTile(), player.getMapSize());
		int offsetX = localX - ((localX >> 3) << 3);
		int offsetY = localY - ((localY >> 3) << 3);
		OutputStream stream = new OutputStream(3);
		stream.writePacket(player, 45);
		stream.writeByteC((offsetX << 4) | offsetY);
		stream.writeByte((object.getType() << 2) + (object.getRotation() & 0x3));
		session.write(stream);
	}

	public void sendFriendsChatChannel() {
		FriendChatsManager manager = player.getCurrentFriendChat();
		OutputStream stream = new OutputStream(manager == null ? 3 : manager.getDataBlock().length + 3);
		stream.writePacketVarShort(player, 12);
		if (manager != null) {
			stream.writeBytes(manager.getDataBlock());
		}
		stream.endPacketVarShort();
		session.write(stream);
	}

	public void sendFriend(String username, String displayName, int world, boolean putOnline, boolean warnMessage) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarShort(player, 85);
		stream.writeByte(warnMessage ? 0 : 1);
		stream.writeString(displayName);
		stream.writeString(displayName.equals(username) ? "" : username);
		stream.writeShort(putOnline ? world : 0);
		stream.writeByte(player.getFriendsIgnores().getRank(Utils.formatPlayerNameForProtocol(username)));
		stream.writeByte(0);
		if (putOnline) {
			stream.writeString(GameConstants.SERVER_NAME);
			stream.writeByte(0);
		}
		stream.endPacketVarShort();
		session.write(stream);
	}

	public void sendIgnore(String name, String display) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarByte(player, 105);
		stream.writeByte(0x2);
		if (display.equals(name)) {
			display = "";
		}
		stream.writeString(name);
		stream.writeString(display);
		stream.writeString(display);
		stream.writeString(name);
		stream.endPacketVarByte();
		session.write(stream);
	}

	public void sendPrivateMessage(String username, String message) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarShort(player, 77);
		stream.writeString(username);
		Huffman.sendEncryptMessage(stream, message);
		stream.endPacketVarShort();
		session.write(stream);
	}

	public void sendGameBarStages() {
		sendConfig(1054, 0); // clan on
		sendConfig(1055, 0); // assist on
		sendConfig(1056, player.isFilterGame() ? 1 : 0);
		sendConfig(2159, 0); // friends chat on
		OutputStream stream = new OutputStream(3);
		stream.writePacket(player, 72);
		stream.writeByte(0); // public on
		stream.writeByte(0); // trade on
		session.write(stream);
		sendPrivateGameBarStage();
	}

	public void sendConfig(int id, int value) {
		if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
			sendConfig2(id, value);
		} else {
			sendConfig1(id, value);
		}
	}

	public void sendPrivateGameBarStage() {
		OutputStream stream2 = new OutputStream(2);
		stream2.writePacket(player, 134);
		stream2.writeByte(player.getFriendsIgnores().getPrivateStatus());
		session.write(stream2);
	}

	// 131 clan chat quick message

	public void sendConfig2(int id, int value) {
		OutputStream stream = new OutputStream(7);
		stream.writePacket(player, 39);
		stream.writeIntV2(value);
		stream.writeShort128(id);
		session.write(stream);
	}

	public void sendConfig1(int id, int value) {
		OutputStream stream = new OutputStream(4);
		stream.writePacket(player, 101);
		stream.writeShort(id);
		stream.writeByte128(value);
		session.write(stream);
	}

	public void receivePrivateMessage(String name, String display, int rights, String message) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarShort(player, 120);
		stream.writeByte(name.equals(display) ? 0 : 1);
		stream.writeString(display);
		if (!name.equals(display)) {
			stream.writeString(name);
		}
		for (int i = 0; i < 5; i++) {
			stream.writeByte(Utils.getRandom(255));
		}
		stream.writeByte(rights);
		Huffman.sendEncryptMessage(stream, message);
		stream.endPacketVarShort();
		session.write(stream);
	}

	public void receivePrivateChatQuickMessage(String name, String display, int rights, QuickChatMessage message) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarByte(player, 42);
		stream.writeByte(name.equals(display) ? 0 : 1);
		stream.writeString(display);
		if (!name.equals(display)) {
			stream.writeString(name);
		}
		for (int i = 0; i < 5; i++) {
			stream.writeByte(Utils.getRandom(255));
		}
		stream.writeByte(rights);
		stream.writeShort(message.getType().getId());
		message.getType().pack(stream, message.getParams());
		stream.endPacketVarByte();
		session.write(stream);
	}

	public void sendPrivateQuickMessageMessage(String username, QuickChatMessage message) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarByte(player, 97);
		stream.writeString(username);
		stream.writeShort(message.getType().getId());
		message.getType().pack(stream, message.getParams());
		stream.endPacketVarByte();
		session.write(stream);
	}

	public void receiveFriendChatMessage(String name, String display, int rights, String chatName, ChatMessage message) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarByte(player, 40);
		stream.writeByte(name.equals(display) ? 0 : 1);
		stream.writeString(display);
		if (!name.equals(display)) {
			stream.writeString(name);
		}
		stream.writeLong(Utils.stringToLong(chatName));
		for (int i = 0; i < 5; i++) {
			stream.writeByte(Utils.getRandom(255));
		}
		stream.writeByte(rights);
		Huffman.sendEncryptMessage(stream, message.getMessage(player.isFilteringProfanity()));
		stream.endPacketVarByte();
		session.write(stream);
	}

	public void receiveFriendChatQuickMessage(String name, String display, int rights, String chatName, QuickChatMessage message) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarByte(player, 20);
		stream.writeByte(name.equals(display) ? 0 : 1);
		stream.writeString(display);
		if (!name.equals(display)) {
			stream.writeString(name);
		}
		stream.writeLong(Utils.stringToLong(chatName));
		for (int i = 0; i < 5; i++) {
			stream.writeByte(Utils.getRandom(255));
		}
		stream.writeByte(rights);
		stream.writeShort(message.getType().getId());
		message.getType().pack(stream, message.getParams());
		stream.endPacketVarByte();
		session.write(stream);
	}

	public void sendUnlockIgnoreList() {
		OutputStream stream = new OutputStream(1);
		stream.writePacket(player, 135);
		session.write(stream);
	}

	public void sendUnlockFriendList() {
		OutputStream stream = new OutputStream(1);
		stream.writePacketVarShort(player, 85);
		session.write(stream);
	}

	/*
	 * dynamic map region
	 */
	public void sendDynamicMapRegion(boolean wasAtDynamicRegion) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarShort(player, 128);
		int middleChunkX = player.getChunkX();
		int middleChunkY = player.getChunkY();
		stream.writeShort(middleChunkY);
		stream.writeByte(player.getMapSize());
		stream.write128Byte(player.isForceNextMapLoadRefresh() ? 1 : 0);
		stream.write128Byte(2);
		stream.writeShortLE(middleChunkX);
		stream.initBitAccess();

		int sceneLength = GameConstants.MAP_SIZES[player.getMapSize()] >> 4;
		// the regionids(maps files) that will be used to load this scene
		int[] regionIds = new int[4 * sceneLength * sceneLength];
		int newRegionIdsCount = 0;
		for (int plane = 0; plane < 4; plane++) {
			for (int realChunkX = (middleChunkX - sceneLength); realChunkX <= ((middleChunkX + sceneLength)); realChunkX++) {
				int regionX = realChunkX / 8;
				y:
				for (int realChunkY = (middleChunkY - sceneLength); realChunkY <= ((middleChunkY + sceneLength)); realChunkY++) {
					int regionY = realChunkY / 8;
					int regionId = (regionX << 8) + regionY;
					Region region = World.getRegions().get(regionId);
					int newChunkX;
					int newChunkY;
					int newPlane;
					int rotation;
					if (region instanceof DynamicRegion) { // generated map
						DynamicRegion dynamicRegion = (DynamicRegion) region;
						int[] pallete = dynamicRegion.getRegionCoords()[plane][realChunkX - (regionX * 8)][realChunkY - (regionY * 8)];
						newChunkX = pallete[0];
						newChunkY = pallete[1];
						newPlane = pallete[2];
						rotation = pallete[3];
					} else { // real map
						newChunkX = realChunkX;
						newChunkY = realChunkY;
						newPlane = plane;
						rotation = 0;// no rotation
					}
					// invalid chunk, not built chunk
					if (newChunkX == 0 || newChunkY == 0) { stream.writeBits(1, 0); } else {
						stream.writeBits(1, 1);
						// chunk encoding = (x << 14) | (y << 3) | (plane <<
						// 24), theres addition of two more bits for rotation
						stream.writeBits(26, (rotation << 1) | (newPlane << 24) | (newChunkX << 14) | (newChunkY << 3));
						int newRegionId = (((newChunkX / 8) << 8) + (newChunkY / 8));
						for (int index = 0; index < newRegionIdsCount; index++) {
							if (regionIds[index] == newRegionId) { continue y; }
						}
						regionIds[newRegionIdsCount++] = newRegionId;
					}

				}
			}
		}
		stream.finishBitAccess();
		for (int index = 0; index < newRegionIdsCount; index++) {
			int[] xteas = MapArchiveKeys.getKey(regionIds[index]);
			if (xteas == null) {
				xteas = new int[4];
			}
			for (int keyIndex = 0; keyIndex < 4; keyIndex++) {
				stream.writeInt(xteas[keyIndex]);
			}
		}
		stream.endPacketVarShort();
		session.write(stream);
	}

	/*
	 * normal map region
	 */
	public void sendMapRegion(boolean sendLswp) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarShort(player, 43);
		if (sendLswp) {
			player.getLocalPlayerUpdate().init(stream);
		}
		stream.writeByteC(player.getMapSize());
		stream.writeByte(player.isForceNextMapLoadRefresh() ? 1 : 0);
		stream.writeShortLE(player.getChunkX());
		stream.writeShort(player.getChunkY());
		for (int regionId : player.getMapRegionsIds()) {
			int[] xteas = MapArchiveKeys.getKey(regionId);
			if (xteas == null) {
				xteas = new int[4];
			}
			for (int index = 0; index < 4; index++) {
				stream.writeInt(xteas[index]);
			}
		}
		stream.endPacketVarShort();
		session.write(stream);
	}

	public void sendCutscene(int id) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarShort(player, 132);
		stream.writeShort(id);
		stream.writeShort(20); // xteas count
		for (int count = 0; count < 20; count++) {
			// xteas
			for (int i = 0; i < 4; i++) {
				stream.writeInt(0);
			}
		}
		byte[] appearence = player.getAppearence().getAppeareanceData();
		stream.writeByte(appearence.length);
		stream.writeBytes(appearence);
		stream.endPacketVarShort();
		session.write(stream);
	}

	/*
	 * sets the pane interface
	 */
	public void sendWindowsPane(int id, int type) {
		player.getInterfaceManager().setWindowsPane(id);
		OutputStream stream = new OutputStream(4);
		stream.writePacket(player, 67);
		stream.writeShortLE128(id);
		stream.write128Byte(type);
		session.write(stream);
	}

	public void sendPlayerOption(String option, int slot, boolean top) {
		sendPlayerOption(option, slot, top, -1);
	}

	public void sendPlayerOption(String option, int slot, boolean top, int cursor) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarByte(player, 1);
		stream.writeByte128(top ? 1 : 0);
		stream.writeShortLE(cursor);
		stream.writeString(option);
		stream.writeByteC(slot);
		stream.endPacketVarByte();
		session.write(stream);
	}

	public void sendPublicMessage(Player p, PublicChatMessage message) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarByte(player, 91);
		stream.writeShort(p.getIndex());
		stream.writeShort(message.getEffects());
		stream.writeByte(p.getMessageIcon());
		if (message instanceof QuickChatMessage) {
			QuickChatMessage qcMessage = (QuickChatMessage) message;
			stream.writeShort(qcMessage.getType().getId());
			qcMessage.getType().pack(stream, qcMessage.getParams());
		} else {
			byte[] chatStr = new byte[250];
			chatStr[0] = (byte) message.getMessage(player.isFilteringProfanity()).length();
			int offset = 1 + Huffman.encryptMessage(1, message.getMessage(player.isFilteringProfanity()).length(), chatStr, 0, message.getMessage(player.isFilteringProfanity()).getBytes());
			stream.writeBytes(chatStr, 0, offset);
		}
		stream.endPacketVarByte();
		session.write(stream);
	}

	/*
	 * sends local players update
	 */
	public void sendLocalPlayersUpdate() {
		session.write(player.getLocalPlayerUpdate().createPacketAndProcess());
	}

	/*
	 * sends local npcs update
	 */
	public void sendLocalNPCsUpdate() {
		session.write(player.getLocalNPCUpdate().createPacketAndProcess());
	}

	public void sendGraphics(Graphics graphics, Object target) {
		OutputStream stream = new OutputStream(13);
		int hash = 0;
		if (target instanceof WorldTile) {
			WorldTile tile = (WorldTile) target;
			hash = tile.getPlane() << 28 | tile.getX() << 14 | tile.getY() & 0x3fff | 1 << 30;
		} else if (target instanceof Player) {
			Player p = (Player) target;
			hash = p.getIndex() & 0xffff | 1 << 28;
		} else {
			NPC n = (NPC) target;
			hash = n.getIndex() & 0xffff;
		}
		stream.writePacket(player, 108);
		stream.writeShort128(graphics.getSpeed());
		stream.writeIntV2(hash);
		stream.writeByte128(0); // slot id used for entitys
		stream.writeByte128(graphics.getSettings2Hash());
		stream.writeShort(graphics.getHeight());
		stream.writeShortLE(graphics.getId());
		session.write(stream);

	}

	public void sendInterface(boolean walkable, int windowId, int windowComponentId, int interfaceId) {
		if (windowId != 752 || windowComponentId != 9 && windowComponentId != 12) {
			if (player.getInterfaceManager().containsInterface(windowComponentId, interfaceId)) {
				closeInterface(windowComponentId);
			}
			if (!player.getInterfaceManager().addInterface(windowId, windowComponentId, interfaceId)) {
				throw new IllegalStateException("Error adding interface: " + windowId + " , " + windowComponentId + " , " + interfaceId);
			}
		}
		OutputStream stream = new OutputStream(8);
		stream.writePacket(player, 5);
		stream.writeShortLE128(interfaceId);
		stream.writeIntLE(windowId << 16 | windowComponentId);
		stream.writeByte(walkable ? 1 : 0);
		session.write(stream);
	}

	public void closeInterface(int windowComponentId) {
		closeInterface(player.getInterfaceManager().getTabWindow(windowComponentId), windowComponentId);
		player.getInterfaceManager().removeTabInterface(windowComponentId);
	}

	public void closeInterface(int windowId, int windowComponentId) {
		OutputStream stream = new OutputStream(5);
		stream.writePacket(player, 73);
		stream.writeIntLE(windowId << 16 | windowComponentId);
		session.write(stream);
	}

	public void sendSystemUpdate(int delay) {
		OutputStream stream = new OutputStream(2);
		stream.writePacket(player, 125);
		stream.writeShort(delay * 50 / 30);
		session.write(stream);
	}

	public void sendUpdateItems(int key, ItemsContainer<Item> items, int... slots) {
		sendUpdateItems(key, items.getItems(), slots);
	}

	public void sendUpdateItems(int key, Item[] items, int... slots) {
		sendUpdateItems(key, key < 0, items, slots);
	}

	public void sendUpdateItems(int key, boolean negativeKey, Item[] items, int... slots) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarShort(player, 80);
		stream.writeShort(key);
		stream.writeByte(negativeKey ? 1 : 0);
		for (int slotId : slots) {
			if (slotId >= items.length) {
				continue;
			}
			stream.writeSmart(slotId);
			int id = -1;
			int amount = 0;
			Item item = items[slotId];
			if (item != null) {
				id = item.getId();
				amount = item.getAmount();
			}
			stream.writeShort(id + 1);
			if (id != -1) {
				stream.writeByte(amount >= 255 ? 255 : amount);
				if (amount >= 255) {
					stream.writeInt(amount);
				}
			}
		}
		stream.endPacketVarShort();
		session.write(stream);
	}

	public void sendUpdateItems(int key, boolean negativeKey, ItemsContainer<Item> items, int... slots) {
		sendUpdateItems(key, negativeKey, items.getItems(), slots);
	}

	public void sendGlobalString(int id, String string) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarByte(player, 54);
		stream.writeShortLE128(id);
		stream.writeString(string);
		stream.endPacketVarByte();
		session.write(stream);

	}

	public void sendLogout() {
		OutputStream stream = new OutputStream();
		stream.writePacket(player, 51);
		ChannelFuture future = session.writeWithFuture(stream);
		if (future != null) {
			future.addListener(ChannelFutureListener.CLOSE);
		} else {
			session.getChannel().close();
		}
	}

	public void sendPanelBoxMessage(String text) {
		sendMessage(99, text, null);
	}

	public void sendTradeRequestMessage(Player p) {
		sendMessage(100, "wishes to trade with you.", p);
	}

	public void sendClanWarsRequestMessage(Player p) {
		sendMessage(101, "wishes to challenge your clan to a clan war.", p);
	}

	public void sendDuelChallengeRequestMessage(Player p, boolean friendly) {
		sendMessage(101, "wishes to duel with you(" + (friendly ? "friendly" : "stake") + ").", p);
	}

	public void sendRebuildMap() {
		sendMessage(1339, "", player);
	}

	public void sendVoice(int id) {
		resetSounds();
		sendSound(id, 0, 2);
	}

	public void resetSounds() {
		OutputStream stream = new OutputStream(1);
		stream.writePacket(player, 142);
		session.write(stream);
	}

	// effect type 1 or 2(index4 or index14 format, index15 format unusused by
	// jagex for now)
	public void sendSound(int id, int delay, int effectType) {
		if (effectType == 1) {
			sendIndex14Sound(id, delay);
		} else if (effectType == 2) {
			sendIndex15Sound(id, delay);
		}
	}

	public void sendIndex14Sound(int id, int delay) {
		OutputStream stream = new OutputStream(9);
		stream.writePacket(player, 106);
		stream.writeShort(id);
		stream.writeByte(1);
		stream.writeShort(delay);
		stream.writeByte(255);
		stream.writeShort(256);
		session.write(stream);
	}

	public void sendIndex15Sound(int id, int delay) {
		OutputStream stream = new OutputStream(7);
		stream.writePacket(player, 121);
		stream.writeShort(id);
		stream.writeByte(1); // amt of times it repeats
		stream.writeShort(delay);
		stream.writeByte(0); // volume
		session.write(stream);
	}

	public void sendMusicEffect(int id) {
		OutputStream stream = new OutputStream(7);
		stream.writePacket(player, 0);
		stream.writeShort128(id);
		stream.write24BitInteger(0);
		stream.writeByteC(255); // volume
		session.write(stream);
	}

	public void sendMusic(int id) {
		sendMusic(id, 100, 255);
	}

	public void sendMusic(int id, int delay, int volume) {
		OutputStream stream = new OutputStream(5);
		stream.writePacket(player, 31);
		stream.write128Byte(delay);
		stream.writeShortLE(id);
		stream.writeByteC(volume);
		session.write(stream);
	}

	public void sendSkillLevel(int skill) {
		OutputStream stream = new OutputStream(7);
		stream.writePacket(player, 93);
		stream.write128Byte(player.getSkills().getLevel(skill));
		stream.writeByte128(skill);
		int experience = (int) player.getSkills().getXp(skill);
		stream.writeIntLE(experience);
		session.write(stream);
	}

	/**
	 * This will blackout specified area.
	 *
	 * @param area
	 * 		area = area which will be blackout (0 = unblackout; 1 = blackout orb; 2 = blackout map; 5 = blackout orb and
	 * 		map)
	 */
	public void sendBlackOut(int area) {
		OutputStream out = new OutputStream(2);
		out.writePacket(player, 68);
		out.writeByte(area);
		session.write(out);
	}

	// instant
	public void sendCameraLook(int viewLocalX, int viewLocalY, int viewZ) {
		sendCameraLook(viewLocalX, viewLocalY, viewZ, -1, -1);
	}

	public void sendCameraLook(int viewLocalX, int viewLocalY, int viewZ, int speed1, int speed2) {
		OutputStream stream = new OutputStream(7);
		stream.writePacket(player, 127);
		stream.writeByteC(viewLocalY);
		stream.writeShortLE128(viewZ >> 2);
		stream.write128Byte(viewLocalX);
		stream.writeByte(speed1);
		stream.write128Byte(speed2);
		session.write(stream);
	}

	public void sendResetCamera() {
		OutputStream stream = new OutputStream(1);
		stream.writePacket(player, 10);
		session.write(stream);
	}

	public void sendCameraRotation(int unknown1, int unknown2) {
		OutputStream stream = new OutputStream(5);
		stream.writeShortLE128(unknown1);
		stream.writeShort128(unknown1);
		stream.writePacket(player, 107);
		session.write(stream);
	}

	public void sendCameraPos(int moveLocalX, int moveLocalY, int moveZ) {
		sendCameraPos(moveLocalX, moveLocalY, moveZ, -1, -1);
	}

	public void sendCameraPos(int moveLocalX, int moveLocalY, int moveZ, int speed1, int speed2) {
		OutputStream stream = new OutputStream(7);
		stream.writePacket(player, 29);
		stream.write128Byte(speed1);
		stream.writeByteC(moveLocalY);
		stream.writeByteC(moveLocalX);
		stream.writeByteC(speed2);
		stream.writeShortLE128(moveZ >> 2);
		session.write(stream);
	}

	public void sendGrandExchangeBar(Player player, int slot, int item, Object progress, int price, int amountSold, int amountOffered) {
		OutputStream output = new OutputStream();
		output.writePacket(player, 61);
		output.writeByte(slot);
		output.writeByte(progress instanceof Progress ? ((Progress) progress).getValue() : ((Integer) progress));
		output.writeShort(item);
		output.writeInt(price);
		output.writeInt(amountOffered);
		output.writeInt(amountSold);
		output.writeInt(price * amountSold);
		session.write(output);
	}

	public void refreshWeight() {
		OutputStream stream = new OutputStream(3);
		stream.writePacket(player, 103);
		stream.writeShort((int) player.getWeight());
		session.write(stream);
	}

	public void sendInterFlashScript(int interfaceId, int componentId, int width, int height, int slot) {
		Object[] parameters = new Object[4];
		int index = 0;
		parameters[index++] = slot;
		parameters[index++] = height;
		parameters[index++] = width;
		parameters[index++] = interfaceId << 16 | componentId;
		sendRunScript(143, parameters);
	}

	public void sendInterSetItemsOptionsScript(int interfaceId, int componentId, int key, boolean negativeKey, int width, int height, String... options) {
		Object[] parameters = new Object[6 + options.length];
		int index = 0;
		for (int count = options.length - 1; count >= 0; count--) {
			parameters[index++] = options[count];
		}
		parameters[index++] = -1; // dunno but always this
		parameters[index++] = 0;// dunno but always this, maybe startslot?
		parameters[index++] = height;
		parameters[index++] = width;
		parameters[index++] = key;
		parameters[index++] = interfaceId << 16 | componentId;
		sendRunScript(negativeKey ? 695 : 150, parameters); // scriptid 150 does
		// that the method
		// name says*/
	}

	public void sendClanInviteMessage(Player p) {
		sendMessage(117, p.getDisplayName() + " is inviting you to join their clan.", p);
	}

	public void sendClanSettings(ClansManager manager, boolean myClan) {
		OutputStream stream = new OutputStream(manager == null ? 4 : manager.getClanSettingsDataBlock().length + 4);
		stream.writePacketVarShort(player, 118);
		stream.writeByte(myClan ? 1 : 0);
		if (manager != null) {
			stream.writeBytes(manager.getClanSettingsDataBlock());
		}
		stream.endPacketVarShort();
		session.write(stream);
	}

	public void sendClanChannel(ClansManager manager, boolean myClan) {
		OutputStream stream = new OutputStream(manager == null ? 4 : manager.getClanChannelDataBlock().length + 4);
		stream.writePacketVarShort(player, 7);
		stream.writeByte(myClan ? 1 : 0);
		if (manager != null) {
			stream.writeBytes(manager.getClanChannelDataBlock());
		}
		stream.endPacketVarShort();
		session.write(stream);
	}

	public void receiveClanChatMessage(boolean myClan, String display, int rights, ChatMessage message) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarByte(player, 138);
		stream.writeByte(myClan ? 1 : 0);
		stream.writeString(display);
		for (int i = 0; i < 5; i++) {
			stream.writeByte(Utils.getRandom(255));
		}
		stream.writeByte(rights);
		Huffman.sendEncryptMessage(stream, message.getMessage(player.isFilteringProfanity()));
		stream.endPacketVarByte();
		session.write(stream);
	}

	public void sendPouchInfusionOptionsScript(int interfaceId, int componentId, int slotLength, int width, int height, String... options) {
		Object[] parameters = new Object[5 + options.length];
		int index = 0;
		parameters[index++] = slotLength;
		parameters[index++] = 1; // dunno
		for (int count = options.length - 1; count >= 0; count--) {
			parameters[index++] = options[count];
		}
		parameters[index++] = height;
		parameters[index++] = width;
		parameters[index++] = interfaceId << 16 | componentId;
		sendRunScript(757, parameters);
	}

	public void sendScrollInfusionOptionsScript(int interfaceId, int componentId, int slotLength, int width, int height, String... options) {
		Object[] parameters = new Object[5 + options.length];
		int index = 0;
		parameters[index++] = slotLength;
		parameters[index++] = 1; // dunno are u sure it contains this 1? yeah
		for (int count = options.length - 1; count >= 0; count--) {
			parameters[index++] = options[count];
		}
		parameters[index++] = height;
		parameters[index++] = width;
		parameters[index++] = interfaceId << 16 | componentId;
		sendRunScript(763, parameters);
	}

	public void requestClientInput(InputEvent event) {
		CoresManager.LOG_PROCESSOR.appendLog(new GameLog("input_event", player.getUsername(), "Input event was started:\t[text=" + event.getText() + ", type=" + event.getType() + "]"));
		sendRunScript(event.getType().getScriptId(), event.getText());
		player.putAttribute("requested_client_input", System.currentTimeMillis());
		player.putAttribute("input_event", event);
	}
}
