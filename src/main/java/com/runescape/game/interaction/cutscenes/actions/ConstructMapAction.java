package com.runescape.game.interaction.cutscenes.actions;

import com.runescape.game.interaction.cutscenes.Cutscene;
import com.runescape.game.world.entity.player.Player;

public class ConstructMapAction extends CutsceneAction {

	private int baseChunkX, baseChunkY, widthChunks, heightChunks;

	public ConstructMapAction(int baseChunkX, int baseChunkY, int widthChunks,
			int heightChunks) {
		super(-1, -1);
		this.baseChunkX = baseChunkX;
		this.baseChunkY = baseChunkY;
		this.widthChunks = widthChunks;
		this.heightChunks = heightChunks;
	}

	@Override
	public void process(Player player, Object[] cache) {
		Cutscene scene = (Cutscene) cache[0];
		scene.constructArea(player, baseChunkX, baseChunkY, widthChunks,
				heightChunks);
	}
}
