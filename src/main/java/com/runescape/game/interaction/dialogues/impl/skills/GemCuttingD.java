package com.runescape.game.interaction.dialogues.impl.skills;

import com.runescape.game.content.skills.SkillsDialogue;
import com.runescape.game.content.skills.crafting.GemCutting;
import com.runescape.game.content.skills.crafting.GemCutting.Gem;
import com.runescape.game.interaction.dialogues.Dialogue;

public class GemCuttingD extends Dialogue {

	private Gem gem;

	@Override
	public void start() {
		this.gem = (Gem) parameters[0];
		SkillsDialogue
				.sendSkillsDialogue(
						player,
						SkillsDialogue.CUT,
						"Choose how many you wish to cut,<br>then click on the item to begin.",
						player.getInventory().getItems()
								.getNumberOf(gem.getUncut()),
						new int[] { gem.getUncut() }, null);

	}

	@Override
	public void run(int interfaceId, int componentId) {
		player.getActionManager().setAction(
				new GemCutting(gem, SkillsDialogue.getQuantity(player)));
		end();
	}

	@Override
	public void finish() {

	}

}
