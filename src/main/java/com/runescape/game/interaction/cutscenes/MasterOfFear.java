package com.runescape.game.interaction.cutscenes;

import com.runescape.game.interaction.cutscenes.actions.CutsceneAction;
import com.runescape.game.interaction.cutscenes.actions.InterfaceAction;
import com.runescape.game.interaction.cutscenes.actions.LookCameraAction;
import com.runescape.game.interaction.cutscenes.actions.PosCameraAction;
import com.runescape.game.world.entity.player.Player;

import java.util.ArrayList;

public class MasterOfFear extends Cutscene {

	@Override
	public boolean hiddenMinimap() {
		return true;
	}

	@Override
	public CutsceneAction[] getActions(Player player) {
		ArrayList<CutsceneAction> actionsList = new ArrayList<CutsceneAction>();
		actionsList.add(new InterfaceAction(115, 2));
		actionsList.add(new PosCameraAction(getX(player, player.getX() + 5), getY(player, player.getY() + 3), 1500, -1));
		actionsList.add(new LookCameraAction(getX(player, player.getX() - 2), getY(player, player.getY()), 1500, 5));
		return actionsList.toArray(new CutsceneAction[0]);
	}
}
