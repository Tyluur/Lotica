package com.runescape.game.event.interaction.item;

import com.runescape.game.event.interaction.type.ItemInteractionEvent;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.interaction.dialogues.impl.item.NewcomerCombatLamp;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.utility.Utils;
import com.runescape.utility.world.ClickOption;

import java.util.ArrayList;
import java.util.List;

import static com.runescape.game.world.entity.player.Skills.DEFENCE;
import static com.runescape.game.world.entity.player.Skills.getLevelByExperience;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 8/23/2016
 */
public class NewcomerLampInteractionEvent extends ItemInteractionEvent {


	@Override
	public int[] getKeys() {
		return new int[] { 11137, 11139 };
	}

	@Override
	public boolean handleItemInteraction(Player player, Item item, ClickOption option) {
		switch (item.getId()) {
			case 11137:
				player.getDialogueManager().startDialogue(new NewcomerCombatLamp());
				break;
			case 11139:
				player.getDialogueManager().startDialogue(new Dialogue() {


					int[] experienceOptions = { 4470, 13363, 61512, };

					@Override
					public void start() {
						sendDialogue("This lamp can help you with the build of your account.", "Select the amount of defense experience you wish added.");
					}

					@Override
					public void run(int interfaceId, int option) {
						switch(stage) {
							case -1:
								List<String> options = new ArrayList<>();
								options.add(DEFAULT_OPTIONS);
								for (int exp : experienceOptions) {
									options.add(Utils.format(exp) + " (Level: " + getLevelByExperience(exp, DEFENCE) + ")");
								}
								sendOptionsDialogue(options.toArray(new String[options.size()]));
								stage = 0;
								break;
							case 0:
								break;
						}
					}

					@Override
					public void finish() {

					}
				});
				break;
		}
		return true;
	}
}
