package com.runescape.game.event.interaction.object;

import com.runescape.game.content.skills.mining.CrashedStarMining;
import com.runescape.game.event.interaction.type.ObjectInteractionEvent;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleMessage;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.game.world.entity.player.achievements.AchievementHandler;
import com.runescape.game.world.entity.player.achievements.medium.Star_Lord;
import com.runescape.utility.Utils;
import com.runescape.utility.world.ClickOption;
import com.runescape.workers.tasks.impl.ShootingStarTick;
import com.runescape.workers.tasks.impl.ShootingStarTick.ShootingStar;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 25, 2015
 */
public class CrashedStarObjectInteraction extends ObjectInteractionEvent {

	@Override
	public int[] getKeys() {
		return ShootingStarTick.STAR_IDS;
	}

	@Override
	public boolean handleObjectInteraction(Player player, WorldObject object, ClickOption option) {
		if (object instanceof ShootingStar) {
			ShootingStar star = (ShootingStar) object;
			switch (option) {
			case FIRST:
				if (!star.hasBeenTagged()) {
					player.getInventory().addItem(995, ShootingStarTick.COIN_SUM);
					int expReward = player.getSkills().getLevel(Skills.MINING) * 75;
					player.getSkills().addXp(Skills.MINING, expReward);
					player.getDialogueManager().startDialogue(SimpleMessage.class, "Congratulations, You were the first to reach the shooting star!", "You receive " + Utils.format(expReward) + " mining experience as a reward.");
					AchievementHandler.incrementProgress(player, Star_Lord.class);
					star.setBeenTagged(true);
				}
				player.getActionManager().setAction(new CrashedStarMining(star));
				break;
			default:
				int percent = ((star.getStage().getHealth() - star.getHealth()) * 100) / star.getStage().getHealth();
				player.getDialogueManager().startDialogue(SimpleMessage.class, "This is a size-" + (star.getStage().ordinal() + 1) + " star.", "A mining level of at least " + star.getStage().getLevelRequired() + " is required to mine this layer.", "It has been mined about " + percent + "% of the way to the next layer.");
				break;
			}
		}
		return true;
	}

}
