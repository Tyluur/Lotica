package com.runescape.game.world.entity.npc.others;

import com.runescape.game.world.World;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.ChatColors;
import com.runescape.workers.game.core.CoresManager;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

import java.util.concurrent.TimeUnit;

@SuppressWarnings("serial")
public class Bork extends NPC {
	
	public Bork(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
		super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
		setLureDelay(0);
		setForceAgressive(true);
	}

	@Override
	public void drop() {
		super.drop();

		getPossibleTargets().stream().filter(e -> e instanceof Player).forEach(e -> {
			final Player player = (Player) e;
			player.getInterfaceManager().sendInterface(693);
			player.getDialogueManager().startDialogue("DagonHai", 7137, player, 1);
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					player.stopAll();
				}
			}, 8);
		});
	}

	@Override
	public void spawn() {
		super.spawn();
		World.sendWorldMessage("<img=6><col=" + ChatColors.MAROON + ">Global</col>: Bork has just been respawned!", false);
	}

	@Override
	public void setRespawnTask() {
		if (!hasFinished()) {
			reset();
			setLocation(getRespawnTile());
			finish();
		}
		CoresManager.schedule((Runnable) () -> {
			try {
				spawn();
			} catch (Exception | Error e) {
				e.printStackTrace();
			}
		}, 1, TimeUnit.HOURS);
	}
	
	public static boolean atBork(WorldTile tile) {
		return (tile.getX() >= 3083 && tile.getX() <= 3120) && (tile.getY() >= 5522 && tile.getY() <= 5550);
	}
}
