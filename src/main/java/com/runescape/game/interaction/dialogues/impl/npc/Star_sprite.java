package com.runescape.game.interaction.dialogues.impl.npc;

import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.world.item.Item;
import com.runescape.workers.tasks.impl.ShootingStarTick;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 8/29/2016
 */
public class Star_sprite extends Dialogue {

	int npcId;

	@Override
	public void start() {
		sendNPCDialogue(npcId = getParam(0), CALM, "Thanks for helping me out there.");
	}

	@Override
	public void run(int interfaceId, int option) {
		switch (stage) {
			case -1:
				if (player.removeAttribute("requires_star_reward", false)) {
					List<Item> receivedItems = ShootingStarTick.getJunkRewards(player);
					receivedItems.forEach(player.getInventory()::addItemDrop);
					List<String> converted = converted(receivedItems);
					List<String> messages = new ArrayList<>();
					messages.add("Here are some rewards for your help:");
					messages.addAll(converted);
					sendNPCDialogue(npcId, CALM, messages.toArray(new String[messages.size()]));
					stage = 0;
				} else {
					sendNPCDialogue(npcId, CALM, "Would you like to view the stardust shop?");
					stage = 1;
				}
				break;
			case 0:
				sendNPCDialogue(npcId, CALM, "Would you like to view the stardust shop?");
				stage = 1;
				break;
			case 1:
				sendOptionsDialogue(DEFAULT_OPTIONS, "Yes", "No");
				stage = 2;
				break;
			case 2:
				if (option == FIRST) {
					openStore("Stardust Exchange");
					stage = -2;
				} else {
					end();
				}
				break;
		}
	}

	@Override
	public void finish() {

	}

	private List<String> converted(List<Item> itemList) {
		String names = "";
		int amount = 1;
		for (int i = 0; i < itemList.size(); i++) {
			Item item = itemList.get(i);
			boolean lastItem = i == itemList.size() - 1;
			names += (lastItem ? "and " : "") + "" + item.getAmount() + " " + item.getName().toLowerCase() + "" + (lastItem ? "" : ", ");
			names += (amount % 3 == 0 ? "<br>" : "");
			amount++;
		}
		return Arrays.asList((String[]) names.split("<br>"));
	}
}
