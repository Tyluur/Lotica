package com.runescape.game.content.global.minigames.pest;

import com.runescape.cache.loaders.NPCDefinitions;
import com.runescape.game.GameConstants;
import com.runescape.game.interaction.controllers.impl.pestcontrol.PestControlGame;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleNPCMessage;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.npc.pest.*;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.item.Item;
import com.runescape.game.world.region.RegionBuilder;
import com.runescape.utility.Utils;
import com.runescape.workers.game.core.CoresManager;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class PestControl {

	private final static int[][] PORTAL_LOCATIONS = { { 4, 56, 45, 21, 32 }, { 31, 28, 10, 9, 32 } };

	private final static int[] KNIGHT_IDS = { 3782, 3784, 3785 };

	private int[] boundChunks;

	private int[] pestCounts = new int[5];

	private List<Player> team;

	private List<NPC> brawlers = new LinkedList<>();

	private List<Integer> hiddenIds = new ArrayList<>();

	private PestPortal[] portals = new PestPortal[4];

	private VoidKnight knight;

	private PestData data;

	public PestControl(List<Player> team, PestData data) {
		this.team = Collections.synchronizedList(team);
		this.data = data;
	}

	public PestControl create() {
		final PestControl instance = this;
		CoresManager.execute(() -> {
			try {
				boundChunks = RegionBuilder.findEmptyChunkBound(8, 8);
				RegionBuilder.copyAllPlanesMap(328, 320, boundChunks[0], boundChunks[1], 8);
				sendBeginningWave();
				sendPortalInterfaces();
				for (Player player : team) {
					player.getControllerManager().removeController();
					player.useStairs(-1, getWorldTile(35 - Utils.random(4), 54 - (Utils.random(3))), 1, 2);
					player.getControllerManager().startController("PestControlGame", instance);
				}
				unlockRandomPortal(false);
				CoresManager.FAST_EXECUTOR.schedule(new PestGameTimer(), 1000, 1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		return instance;
	}

	private void sendBeginningWave() {
		knight = new VoidKnight(KNIGHT_IDS[Utils.random(KNIGHT_IDS.length)], true, getWorldTile(32, 32), this);
		knight.unlock();
		for (int index = 0; index < portals.length; index++) {
			PestPortal portal = portals[index] = new PestPortal(6146 + index, true, getWorldTile(PORTAL_LOCATIONS[0][index], PORTAL_LOCATIONS[1][index]), this);
			portal.setHitpoints(data.ordinal() == 0 ? 2000 : 2500);
		}
	}

	public void unlockRandomPortal(boolean wait) {
		try {
			List<Integer> badIndexes = new ArrayList<>();
			List<Integer> goodIndexes = new ArrayList<>();
			for (int i = 0; i < portals.length; i++) {
				if (portals[i].isDead() || portals[i] == null) {
					badIndexes.add(i);
				}
			}
			for (int i = 0; i < portals.length; i++) {
				if (badIndexes.contains(i)) {
					continue;
				}
				goodIndexes.add(i);
			}
			if (goodIndexes.size() > 0) {
				int random = goodIndexes.get(new Random().nextInt(goodIndexes.size()));
				final PestPortal portal = portals[random];
				if (GameConstants.DEBUG) {
					System.out.println("Going to unlock " + random + " ---- " + portal + ": SIZE " + goodIndexes.size());
				}
				if (wait) {
					WorldTasksManager.schedule(new WorldTask() {

						@Override
						public void run() {
							portal.unlock();
						}
					}, 25);
				} else {
					portal.unlock();
				}
				goodIndexes.clear();
				badIndexes.clear();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public WorldTile getWorldTile(int mapX, int mapY) {
		return new WorldTile(boundChunks[0] * 8 + mapX, boundChunks[1] * 8 + mapY, 0);
	}

	public boolean createPestNPC(int index) {
		if (pestCounts[index] >= (index == 4 ? 4 : (portals[index] != null && portals[index].isLocked()) ? 5 : 15)) {
			return false;
		}
		pestCounts[index]++;
		WorldTile baseTile = getWorldTile(PORTAL_LOCATIONS[0][index], PORTAL_LOCATIONS[1][index]);
		WorldTile teleTile = baseTile;
		int npcId = index == 4 ? data.getShifters()[Utils.random(data.getShifters().length)] : data.getPests()[Utils.random(data.getPests().length)];
		NPCDefinitions defs = NPCDefinitions.getNPCDefinitions(npcId);
		for (int trycount = 0; trycount < 10; trycount++) {
			teleTile = new WorldTile(baseTile, 5);
			if (World.isTileFree(baseTile.getPlane(), teleTile.getX(), teleTile.getY(), defs.size)) { break; }
			teleTile = baseTile;
		}
		String name = defs.getName().toLowerCase();
		if (name.contains("torcher") || name.contains("defiler")) {
			new ProjectilePest(npcId, teleTile, -1, true, true, index, this);
		} if (name.contains("shifter")) {
			new Shifter(npcId, teleTile, -1, true, true, index, this);
		} else if (name.contains("splatter")) {
			new Splatter(npcId, teleTile, -1, true, true, index, this);
		} else if (name.contains("spinner")) {
			new Spinner(npcId, teleTile, -1, true, true, index, this);
		} else if (name.contains("brawler")) {
			brawlers.add(new PestMonsters(npcId, teleTile, -1, true, true, index, this));
		} else { new PestMonsters(npcId, teleTile, -1, true, true, index, this); }
		sendPortalInterfaces();
		return true;
	}

	public void endGame() {
		final List<Player> team = new LinkedList<>();
		team.addAll(this.team);
		this.team.clear();
		for (final Player player : team) {
			final int knightZeal = (int) ((PestControlGame) player.getControllerManager().getController()).getPoints();
			player.getControllerManager().forceStop();
			WorldTasksManager.schedule(new WorldTask() {

				@Override
				public void run() {
					sendFinalReward(player, knightZeal);
				}
			}, 1);
		}
		CoresManager.schedule(() -> {
			try {
				if (boundChunks != null) {
					RegionBuilder.destroyMap(boundChunks[0], boundChunks[1], 8, 8);
					boundChunks = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}, 6000, TimeUnit.MILLISECONDS);
	}

	private void sendFinalReward(Player player, int knightZeal) {
		player.getLockManagement().lockAll(2000);
		if (knight.isDead()) {
			player.getDialogueManager().startDialogue(SimpleNPCMessage.class, 3786, "You failed to protect the void knight and have not been awarded any points.");
		} else if (knightZeal < 750) {
			player.getDialogueManager().startDialogue(SimpleNPCMessage.class, 3786, "The knights notice your lack of zeal in that battle and have not presented you with any points.");
		} else {
			int coinsAmount = player.getSkills().getCombatLevel() * 100;
			int pointsAmount = data.getReward();
			player.getDialogueManager().startDialogue(SimpleNPCMessage.class, 3786, "Congratulations! You have successfully kept the lander safe and have been awarded: " + coinsAmount + " gold coins and " + pointsAmount + " commendation points.");
			player.getInventory().addItem(new Item(995, coinsAmount));
			player.setPestPoints(player.getPestPoints() + pointsAmount);
		}
	}

	public void sendPortalInterfaces() {
		for (Player player : team) {
			for (int i = 13; i < 17; i++) {
				PestPortal npc = portals[i - 13];
				if (npc != null) {
					player.getPackets().sendIComponentText(408, i, npc.getHitpoints() + "");
				}
			}
			for (int i : hiddenIds) {
				player.getPackets().sendHideIComponent(408, i, true);
			}
			player.getPackets().sendIComponentText(408, 1, "" + knight.getHitpoints());
		}
	}

	public boolean isBrawlerAt(WorldTile tile) {
		for (Iterator<NPC> it = brawlers.iterator(); it.hasNext(); ) {
			NPC npc = it.next();
			if (npc.isDead() || npc.hasFinished()) {
				it.remove();
				continue;
			}
			if (npc.getX() == tile.getX() && npc.getY() == tile.getY() && tile.getPlane() == tile.getPlane()) {
				return true;
			}
		}
		return false;
	}

	private void updateTime(int minutes) {
		for (Player player : team) { player.getPackets().sendIComponentText(408, 0, minutes + " min"); }
	}

	public void sendTeamMessage(String message) {
		for (Player player : team) { player.getPackets().sendGameMessage(message, false); }
	}

	public boolean canFinish() {
		List<PestPortal> portalz = new ArrayList<>();
		for (PestPortal portal : portals) {
			if (portal == null || portal.isDead()) {
				continue;
			}
			portalz.add(portal);
		}
		return portalz.size() == 0 || knight.isDead();
	}

	public PestPortal[] getPortals() {
		return portals;
	}

	public List<Player> getPlayers() {
		return team;
	}

	public NPC getKnight() {
		return knight;
	}

	public int[] getPestCounts() {
		return pestCounts;
	}

	public PestData getPestData() {
		return data;
	}

	public void addDroppedPortal(int hiddenComponentId) {
		hiddenIds.add(hiddenComponentId);
	}

	public enum PestData {

		NOVICE(new int[] { /*Shifters*/3732, 3733, 3734, 3735, /*Ravagers*/3742, 3743, 3744, /*Brawler*/3772, 3773, /*Splatter*/3727, 3728, 3729, /*Spinner*/3747, 3748, 3749, /*Torcher*/3752, 3753, 3754, 3755, /*Defiler*/3762, 3763, 3764, 3765 }, new int[] { 3732, 3733, 3734, 3735 }, 3),

		INTERMEDIATE(new int[] { /*Shifters*/3734, 3735, 3736, 3737, 3738, 3739/*Ravagers*/, 3744, 3743, 3745, /*Brawler*/3773, 3775, 3776, /*Splatter*/3728, 3729, 3730, /*Spinner*/3748, 3749, 3750, 3751, /*Torcher*/3754, 3755, 3756, 3757, 3758, 3759, /*Defiler*/3764, 3765, 3766, 3768, 3769 }, new int[] { 3734, 3735, 3736, 3737, 3738, 3739 }, 5),

		VETERAN(new int[] { /*Shifters*/3736, 3737, 3738, 3739, 3740, 3741 /*Ravagers*/, 3744, 3745, 3746, /*Brawler*/3776, 3774,/*Splatter*/3729, 3730, 3731, /*Spinner*/3749, 3750, 3751, /*Torcher*/3758, 3759, 3760, 3761,/*Defiler*/3770, 3771 }, new int[] { 3736, 3737, 3738, 3739, 3740, 3741 }, 7);

		private int[] pests, shifters;

		private int reward;

		PestData(int[] pests, int[] shifters, int reward) {
			this.pests = pests;
			this.shifters = shifters;
			this.reward = reward;
		}

		public int[] getShifters() {
			return shifters;
		}

		public int[] getPests() {
			return pests;
		}

		public int getReward() {
			return reward;
		}
	}

	private class PestGameTimer extends TimerTask {

		int seconds = 1200;

		@Override
		public void run() {
			try {
				updateTime(seconds / 60);
				if (seconds == 0 || canFinish()) {
					endGame();
					cancel();
					return;
				}
				if (seconds % 10 == 0) { sendPortalInterfaces(); }
				seconds--;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
