package com.runescape.workers.tasks.impl;

import com.runescape.game.world.World;
import com.runescape.game.world.item.FloorItem;
import com.runescape.game.world.item.Item;
import com.runescape.game.world.item.WorldFloorItems;
import com.runescape.game.world.item.WorldFloorItems.TimedAppearanceItem;
import com.runescape.workers.tasks.WorldTask;

import java.util.List;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Jun 17, 2015
 */
public class TimedItemsTick extends WorldTask {

	@Override
	public void run() {
		List<TimedAppearanceItem> items = WorldFloorItems.getTimedItems();
		for (TimedAppearanceItem item : items) {
			FloorItem floorItem = World.getRegion(item.getTile().getRegionId()).getFloorItem(item.getItemId(), item.getTile(), null);
			if (floorItem == null) {
				World.addGroundItem(new Item(item.getItemId(), item.getAmount()), item.getTile(), null, false, item.getDelay(), 2, -1);
			}
		}
	}

}
