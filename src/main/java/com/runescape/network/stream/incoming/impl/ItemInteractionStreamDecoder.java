package com.runescape.network.stream.incoming.impl;

import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.game.content.economy.exchange.ExchangeManagement;
import com.runescape.game.content.skills.magic.Magic;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.player.LockManagement.LockType;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.RouteEvent;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.game.world.item.FloorItem;
import com.runescape.network.codec.decoders.handlers.InventoryOptionsHandler;
import com.runescape.network.codec.decoders.handlers.ObjectHandler;
import com.runescape.network.stream.InputStream;
import com.runescape.network.stream.incoming.IncomingStreamDecoder;
import com.runescape.utility.external.gson.loaders.ItemInformationLoader;
import com.runescape.workers.game.core.CoresManager;
import com.runescape.workers.game.log.GameLog;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/27/2016
 */
public class ItemInteractionStreamDecoder extends IncomingStreamDecoder {

	private static final int ITEM_ON_ITEM = 73;

	private final static int ITEM_EXAMINE_PACKET = 27;

	private final static int ITEM_ON_OBJECT_PACKET = 42;

	private final static int GRAND_EXCHANGE_SELECTION = 13;

	private final static int ITEM_TAKE_PACKET = 24;

	private static final int UNDEFINED = 21;

	@Override
	public int[] getKeys() {
		return new int[] { 73, 27, 42, 13, 24, 21 };
	}

	@Override
	public void decode(Player player, InputStream stream, int packetId, int length) {
		switch (packetId) {
			case ITEM_TAKE_PACKET:
				handleGroundItem(player, stream);
				break;
			case UNDEFINED:
				handleUndefined(player, stream);
				break;
			case ITEM_ON_ITEM:
				InventoryOptionsHandler.handleItemOnItem(player, stream);
				break;
			case ITEM_EXAMINE_PACKET:
				final int id = stream.readUnsignedShort128();
				stream.readByte();
				int y = stream.readUnsignedShort();
				int x = stream.readUnsignedShortLE();
				final WorldTile tile = new WorldTile(x, y, player.getPlane());
				final int regionId = tile.getRegionId();
				final FloorItem item = World.getRegion(regionId).getFloorItem(id, tile, player);
				if (item == null) {
					return;
				}
				player.getPackets().sendGameMessage(ItemInformationLoader.getExamine(item.getId()));
				break;
			case ITEM_ON_OBJECT_PACKET:
				ObjectHandler.handleItemOnObject(player, stream);
				break;
			case GRAND_EXCHANGE_SELECTION:
				int itemId = stream.readShort();
				if (!ItemDefinitions.getItemDefinitions(itemId).isExchangeable()) { return; }
				ExchangeManagement.chooseBuyItem(player, itemId);
				break;
		}
	}

	private void handleUndefined(Player player, InputStream stream) {
		int x = stream.readShort();
		int y = stream.readShort();
		stream.readShortLE128();
		stream.readIntV2();
		stream.readShortLE();
		stream.readByte();
		int id = stream.readShortLE();

		WorldTile tile = new WorldTile(x, y, player.getPlane());
		final int regionId = tile.getRegionId();
		final FloorItem item = World.getRegion(regionId).getFloorItem(id, tile, player);
		if (item == null) {
			return;
		}

		final int clientSpeed;
		int gfxDelay;
		if (player.withinDistance(tile, 1)) {
			clientSpeed = 70;
			gfxDelay = 80;
		} else if (player.withinDistance(tile, 5)) {
			clientSpeed = 90;
			gfxDelay = 100;
		} else if (player.withinDistance(tile, 8)) {
			clientSpeed = 110;
			gfxDelay = 120;
		} else {
			clientSpeed = 130;
			gfxDelay = 140;
		}
		if (player.getSkills().getLevel(Skills.MAGIC) < 33) {
			player.getPackets().sendGameMessage("You do not have the required level to cast this spell.");
			return;
		}
		if (!Magic.checkRunes(player, true, Magic.LAW_RUNE, 1, Magic.AIR_RUNE, 1)) {
			player.sendMessage("You do not have the required runes to cast this spell.", false);
			return;
		}
		final int delay = (gfxDelay / 20) - 1;
		player.stopAll();
		player.setNextAnimation(new Animation(710));
		player.setNextGraphics(new Graphics(142));
		World.sendProjectile(player, tile, 143, 18, 5, clientSpeed / 3, 50, 0 , 0);
		player.setNextFaceWorldTile(tile);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				final FloorItem floorItem = World.getRegion(regionId).getFloorItem(id, tile, player);
				if (floorItem == null) {
					player.sendMessage("Too late - it's gone.", false);
					return;
				}
				if (player.isAnyIronman() && (floorItem.getOwner() == null || (floorItem.getOwner() != null && !floorItem.getOwner().getUsername().equals(player.getUsername())))) {
					boolean isExclusion = false;
					if (floorItem.getId() == 1573 && tile.matches(new WorldTile(3152, 3401, 0))) {
						isExclusion = true;
					}
					if (!isExclusion) {
						player.sendMessage("As an ironman, you can't do this.");
						return;
					}
				}
				if (!player.getControllerManager().canPickupItem(floorItem)) {
					return;
				}
				World.removeGroundItem(player, floorItem);
			}
		}, delay);

	}

	private void handleGroundItem(final Player player, InputStream stream) {
		if (!player.hasStarted() || !player.clientHasLoadedMapRegion() || player.isDead()) {
			return;
		}
		if (player.getLockManagement().isLocked(LockType.ITEM_INTERACTION)) {
			return;
		}
		final int id = stream.readUnsignedShort128();
		stream.readByte();
		int y = stream.readUnsignedShort();
		int x = stream.readUnsignedShortLE();
		final WorldTile tile = new WorldTile(x, y, player.getPlane());
		final int regionId = tile.getRegionId();
		if (!player.getMapRegionsIds().contains(regionId)) {
			return;
		}
		final FloorItem item = World.getRegion(regionId).getFloorItem(id, tile, player);
		if (item == null) {
			return;
		}
		player.setRouteEvent(new RouteEvent(item, () -> {
			final FloorItem floorItem = World.getRegion(regionId).getFloorItem(id, tile, player);
			if (floorItem == null) {
				return;
			}
			if (player.isAnyIronman() && (floorItem.getOwner() == null || (floorItem.getOwner() != null && !floorItem.getOwner().getUsername().equals(player.getUsername())))) {
				boolean isExclusion = false;
				if (floorItem.getId() == 1573 && tile.matches(new WorldTile(3152, 3401, 0))) {
					isExclusion = true;
				}
				if (!isExclusion) {
					player.sendMessage("As an ironman, you can't do this.");
					return;
				}
			}
			if (!player.getControllerManager().canPickupItem(floorItem)) {
				return;
			}
			CoresManager.LOG_PROCESSOR.appendLog(new GameLog("item_interaction", player.getUsername(), "Picked up:\t" + floorItem + " at " + tile));
			player.addWalkSteps(tile.getX(), tile.getY(), 1);
			World.removeGroundItem(player, floorItem);
		}, true));
	}
}
