package com.runescape.game.interaction.dialogues.impl.skills;

import com.runescape.game.content.skills.AncientEffigies;
import com.runescape.game.interaction.dialogues.Dialogue;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.utility.Utils;

/**
 * Ancient effifies dialogue handling.
 *
 * @author Raghav/Own4g3 <Raghav_ftw@hotmail.com>
 */
public class AncientEffigiesD extends Dialogue {
	
	int itemId;

	int skill1; // this might needs to be saved

	int skill2;
	
	@Override
	public void start() {
		itemId = (Integer) parameters[0];
		sendDialogue("As you inspect the ancient effigy, you begin to feel a", "strange sensation of the relic searching your mind,", "drawing on your knowledge.");
		int random = Utils.getRandom(7);
		skill1 = AncientEffigies.SKILL_1[random];
		skill2 = AncientEffigies.SKILL_2[random];
	}
	
	@Override
	public void run(int interfaceId, int componentId) {
		if (getStage() == -1) {
			sendDialogue("Images from your experiences of " + AncientEffigies.getMessage(skill1), "fill your mind.");
			stage = (0);
		} else if (getStage() == 0) {
			player.getAttributes().put("skill1", skill1);
			player.getAttributes().put("skill2", skill2);
			sendOptionsDialogue("Which images do you wish<br> to focus on?", "" + Skills.SKILL_NAME[skill1], "" + Skills.SKILL_NAME[skill2]);
			stage = (1);
		} else if (getStage() == 1 && componentId == FIRST) {
			if (player.getSkills().getLevel((Integer) player.getAttributes().get("skill1")) < AncientEffigies.getRequiredLevel(itemId)) {
				sendDialogue("The images in your mind fade; the ancient effigy seems", "to desire knowledge of experiences you have not yet", "had.");
				player.getPackets().sendGameMessage("You require at least level " + AncientEffigies.getRequiredLevel(itemId) + " " + Skills.SKILL_NAME[(Integer) player.getAttributes().get("skill1")] + " to investigate the ancient effigy further.");
			} else {
				player.getAttributes().put("skill", skill1);
				sendDialogue("As you focus on your memories, you can almost hear a", "voice in the back of your mind whispering to you...");
				stage = (2);
			}
		} else if (getStage() == 1 && componentId == SECOND) {
			if (player.getSkills().getLevel((Integer) player.getAttributes().get("skill2")) < AncientEffigies.getRequiredLevel(itemId)) {
				sendDialogue("The images in your mind fade; the ancient effigy seems", "to desire knowledge of experiences you have not yet", "had.");
				player.getPackets().sendGameMessage("You require at least level " + AncientEffigies.getRequiredLevel(itemId) + " " + Skills.SKILL_NAME[(Integer) player.getAttributes().get("skill1")] + " to investigate the ancient effigy further.");
			} else {
				player.getAttributes().put("skill", skill2);
				sendDialogue("As you focus on your memories, you can almost hear a", "voice in the back of your mind whispering to you...");
				stage = (2);
			}
		} else if (getStage() == 2) {
			player.getSkills().addXpNoModifier((Integer) player.getAttributes().get("skill"), AncientEffigies.getExp(itemId));
			player.getPackets().sendGameMessage("You have gained " + Utils.format(AncientEffigies.getExp(itemId)) + " " + Skills.SKILL_NAME[(Integer) player.getAttributes().get("skill")] + " experience!");
			AncientEffigies.effigyInvestigation(player, itemId);
			sendDialogue("The ancient effigy glows briefly; it seems changed", "somehow and no longer responds to the same memories", "as before.");
			stage = 3;
		} else if (getStage() == 3) {
			sendDialogue("A sudden bolt of inspiration flashes through your mind,", "revealing new insight into your experiences!");
			stage = (-2);
		} else {
			end();
		}
	}
	
	@Override
	public void finish() {
		
	}
	
}
