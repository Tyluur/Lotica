package com.runescape.game.world.entity.npc.others;

import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.masks.ForceTalk;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.godwars.saradomin.SaradominFaction;
import com.runescape.game.world.entity.npc.godwars.zammorak.ZamorakFaction;
import com.runescape.game.world.entity.player.Player;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class BanditCampBandits extends NPC {

	public BanditCampBandits(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setForceAgressive(true); //to ignore combat lvl
		setForceTargetDistance(10);
	}

	@Override
	public ArrayList<Entity> getPossibleTargets() {
		ArrayList<Entity> targets = super.getPossibleTargets();
		ArrayList<Entity> targetsCleaned = new ArrayList<>();
		for (Entity t : targets) {
			if (!(t instanceof Player) || (!ZamorakFaction.hasGodItem((Player) t) && !SaradominFaction.hasGodItem((Player) t))) {
				continue;
			}
			targetsCleaned.add(t);
		}
		return targetsCleaned;
	}

	@Override
	public void setTarget(Entity entity) {
		if (entity instanceof Player && (ZamorakFaction.hasGodItem((Player) entity) || SaradominFaction.hasGodItem((Player) entity))) {
			setNextForceTalk(new ForceTalk(ZamorakFaction.hasGodItem((Player) entity) ? "Time to die, Zamorakian filth!" : "Prepare to suffer, Saradominist scum!"));
		}
		super.setTarget(entity);
	}

}
