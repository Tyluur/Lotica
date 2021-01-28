package com.runescape.game.interaction.cutscenes;

import com.runescape.game.interaction.cutscenes.actions.CutsceneAction;
import com.runescape.game.interaction.cutscenes.actions.LookCameraAction;
import com.runescape.game.interaction.cutscenes.actions.PosCameraAction;
import com.runescape.game.world.entity.player.Player;

import java.util.ArrayList;

public class TowersPkCutscene extends Cutscene {

	@Override
	public CutsceneAction[] getActions(Player player) {
		ArrayList<CutsceneAction> actionsList = new ArrayList<CutsceneAction>();

		actionsList.add(new PosCameraAction(getX(player, player.getX() - 5),
				getY(player, player.getY() + 7), 8000, 6, 6, -1));
		actionsList.add(new LookCameraAction(getX(player, player.getX()), getY(
				player, player.getY() + 7), 6000, 6, 6, 10));

		return actionsList.toArray(new CutsceneAction[actionsList.size()]);
	}

	@Override
	public boolean hiddenMinimap() {
		return true;
	}

}
