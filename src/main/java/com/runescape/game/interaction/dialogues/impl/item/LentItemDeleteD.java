package com.runescape.game.interaction.dialogues.impl.item;

import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.world.item.Item;
import com.runescape.utility.Utils;
import com.runescape.utility.external.gson.loaders.LentItemsLoader;
import com.runescape.utility.external.gson.resource.LentItem;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/20/2015
 */
public class LentItemDeleteD extends Dialogue {

	/** The item being dropped */
	private Item item;

	/** The lent item instance */
	private LentItem lentItem;

	@Override
	public void start() {
		lentItem = LentItemsLoader.getLentItem(player, item = getParam(0));
		if (lentItem == null) {
			sendDialogue("Error deleting this item, please ::empty it and report this on forums.");
			stage = -2;
			return;
		}
		sendItemDialogue(lentItem.getOriginalItemId(), 1, "Are you sure you wish to discard this lent item?", "Expires " + (lentItem.isLentTillLogout() ? "on logout." : "in " + Utils.convertMillisecondsToTime(lentItem.getHoursLeft())));
	}

	@Override
	public void run(int interfaceId, int option) {
		switch(stage) {
			case -1:
				sendOptionsDialogue("Return item to original owner?", "Yes", "No");
				stage = 0;
				break;
			case 0:
				if (option == FIRST) {
					if (player.getInventory().contains(item.getId())) {
						player.getInventory().deleteItem(item);
						LentItemsLoader.deleteLentItem(player, item);
						sendDialogue("The item has been returned to the original owner.");
						stage = -2;
					}
				} else {
					end();
				}
				break;
		}
	}

	@Override
	public void finish() {

	}
}
