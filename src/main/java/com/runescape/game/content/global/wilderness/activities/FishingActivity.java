package com.runescape.game.content.global.wilderness.activities;

import com.runescape.game.content.global.wilderness.WildernessActivity;
import com.runescape.game.content.skills.fishing.Fishing;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Dec 31, 2014
 */
public class FishingActivity extends WildernessActivity {

	@Override
	public String getDescription() {
		return "Fishing spots prepared below east dragons";
	}

	@Override
	public String getServerAnnouncement() {
		return "Fishing spots with rocktails have spawned below east dragons!";
	}

	@Override
	public void onCreate() {
		for (FishingSpots spot : FishingSpots.values()) {
			for (WorldTile tile : spot.spawnTiles) {
				NPC fish = new NPC(spot.npcId, tile, -1, true);
				fish.getAttributes().put("fishing_activity_npc", true);
			}
		}
	}

	@Override
	public void process() {
		
	}

	@Override
	public void onFinish() {
		for (NPC npc : World.getNPCs()) {
			if (npc == null) {
				continue;
			}
			for (FishingSpots spot : FishingSpots.values()) {
				for (WorldTile tile : spot.spawnTiles) {
					if (npc.getId() == spot.npcId) {
						if (npc.getRespawnTile().equals(tile)) {
							npc.finish();
						}
					}
				}
			}
		}
		for (Player player : World.getPlayers()) {
			if (player == null || !(player.getActionManager().getAction() instanceof Fishing)) {
				continue;
			}
			player.getActionManager().forceStop();
		}
	}

	@Override
	public long getActivityTime() {
		return 0;
	}

	private enum FishingSpots {

		SHRIMP(327, new WorldTile(3351, 3634, 0)),
		TROUT_SALMON(328, new WorldTile(3353, 3633, 0)),
		LOBSTER(312, new WorldTile(3355, 3633, 0)),
		SHARK(313, new WorldTile[] { new WorldTile(3370, 3637, 0), new WorldTile(3368, 3635, 0) }),
		ROCKTAILS(8842, new WorldTile[] { new WorldTile(3365, 3635, 0), new WorldTile(3360, 3635, 0) });

		FishingSpots(int npcId, WorldTile[] spawnTiles) {
			this.npcId = npcId;
			this.spawnTiles = spawnTiles;
		}

		FishingSpots(int npcId, WorldTile spawnTile) {
			this.npcId = npcId;
			this.spawnTiles = new WorldTile[] { spawnTile };
		}

		/**
		 * The id of the fishing npc
		 */
		private final int npcId;

		/**
		 * The spawn tiles of the npc
		 */
		private final WorldTile[] spawnTiles;
	}

	@Override
	public boolean receivesBonus(Player player, Object... params) {
		NPC fish = (NPC) params[0];
		return fish != null && fish.getAttribute("fishing_activity_npc") != null && player.getRegionId() == 13368;
	}

	@Override
	public Integer getBonusPoints(Player player) {
		if (player.isAnyDonator()) {
			return 2;
		}
		return 1;
	}

	@Override
	public Integer getPointChance(Player player) {
		if (player.isAnyDonator()) {
			return 75;
		}
		return 50;
	}

}
