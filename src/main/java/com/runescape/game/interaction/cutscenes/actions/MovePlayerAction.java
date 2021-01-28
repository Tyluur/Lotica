package com.runescape.game.interaction.cutscenes.actions;

import com.runescape.game.interaction.cutscenes.Cutscene;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.player.Player;

public class MovePlayerAction extends CutsceneAction {

	private int x, y, plane, movementType;

	public MovePlayerAction(int x, int y, boolean run, int actionDelay) {
		this(x, y, -1, run ? Player.RUN_MOVE_TYPE : Player.WALK_MOVE_TYPE,
				actionDelay);
	}

	public MovePlayerAction(int x, int y, int plane, int movementType,
			int actionDelay) {
		super(-1, actionDelay);
		this.x = x;
		this.y = y;
		this.plane = plane;
		this.movementType = movementType;
	}

	@Override
	public void process(Player player, Object[] cache) {
		Cutscene scene = (Cutscene) cache[0];
		if (movementType == Player.TELE_MOVE_TYPE) {
			player.setNextWorldTile(new WorldTile(scene.getBaseX() + x, scene
					.getBaseY() + y, plane));
			return;
		}
		player.setRun(movementType == Player.RUN_MOVE_TYPE);
		player.addWalkSteps(scene.getBaseX() + x, scene.getBaseY() + y);
	}

}
