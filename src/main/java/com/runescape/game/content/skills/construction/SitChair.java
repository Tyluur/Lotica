package com.runescape.game.content.skills.construction;

import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.actions.Action;

public class SitChair extends Action {

	private int chair;
	private WorldTile originalTile;
	private WorldTile chairTile;
	private boolean tped;

	public SitChair(Player player, int chair, WorldObject object) {
		this.chair = chair;
		this.originalTile = new WorldTile(player);
		chairTile = object;
		WorldTile face = new WorldTile(player);
		if (object.getType() == 10) {
			if (object.getRotation() == 0)
				face.moveLocation(0, -1, 0);
			else if (object.getRotation() == 2)
				face.moveLocation(0, 1, 0);
		} else if (object.getType() == 11) {
			if (object.getRotation() == 1)
				face.moveLocation(-1, 1, 0);
			else if (object.getRotation() == 2)
				face.moveLocation(1, 1, 0);
		}
		player.setNextFaceWorldTile(face);
	}

	@Override
	public boolean start(Player player) {
		setActionDelay(player, 1);
		return true;
	}

	@Override
	public boolean process(Player player) {
		return true;
	}

	@Override
	public int processWithDelay(Player player) {
		if (!tped) {
			player.setNextWorldTile(chairTile);
			tped = true;
		}
		player.setNextAnimation(new Animation(HouseConstants.CHAIR_EMOTES[chair]));
		return 0;
	}

	@Override
	public void stop(final Player player) {
		player.getLockManagement().lockAll(1000);
		player.setNextWorldTile(originalTile);
		player.setNextAnimation(new Animation(-1));
	}
}
