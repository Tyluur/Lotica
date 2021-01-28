package com.runescape.game.interaction.controllers.impl;

import com.runescape.game.content.skills.summoning.Summoning;
import com.runescape.game.interaction.controllers.Controller;
import com.runescape.game.interaction.dialogues.impl.minigame.FightCavesEntranceD;
import com.runescape.game.interaction.dialogues.impl.misc.SimpleNPCMessage;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.npc.fightcaves.FightCavesNPC;
import com.runescape.game.world.entity.npc.fightcaves.TzKekCaves;
import com.runescape.game.world.entity.npc.fightcaves.TzTok_Jad;
import com.runescape.game.world.entity.npc.fightcaves.Yt_HurKot;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.achievements.AchievementHandler;
import com.runescape.game.world.entity.player.achievements.elite.Cave_Addiction;
import com.runescape.game.world.entity.player.achievements.hard.Tztok_Me;
import com.runescape.game.world.entity.player.pet.Pets;
import com.runescape.game.world.entity.player.rights.RightManager;
import com.runescape.game.world.item.Item;
import com.runescape.game.world.region.RegionBuilder;
import com.runescape.utility.Utils;
import com.runescape.workers.game.core.CoresManager;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class FightCaves extends Controller {

	public static final WorldTile OUTSIDE = new WorldTile(2441, 5174, 0);

	public static final int[][] WAVES = { { 2734 }, { 2734, 2734 }, { 2736 }, { 2736, 2734 }, { 2736, 2734, 2734 }, { 2736, 2736 }, { 2739 }, { 2739, 2734 }, { 2739, 2734, 2734 }, { 2739, 2736 }, { 2739, 2736, 2734 }, { 2739, 2736, 2734, 2734 }, { 2739, 2736, 2736 }, { 2739, 2739 }, { 2741 }, { 2741, 2734 }, { 2741, 2734, 2734 }, { 2741, 2736 }, { 2741, 2736, 2734 }, { 2741, 2736, 2734, 2734 }, { 2741, 2736, 2736 }, { 2741, 2739 }, { 2741, 2739, 2734 }, { 2741, 2739, 2734, 2734 }, { 2741, 2739, 2736 }, { 2741, 2739, 2736, 2734 }, { 2741, 2739, 2736, 2734, 2734 }, { 2741, 2739, 2736, 2736 }, { 2741, 2739, 2739 }, { 2741, 2741 }, { 2743 }, { 2743, 2734 }, { 2743, 2734, 2734 }, { 2743, 2736 }, { 2743, 2736, 2734 }, { 2743, 2736, 2734, 2734 }, { 2743, 2736, 2736 }, { 2743, 2739 }, { 2743, 2739, 2734 }, { 2743, 2739, 2734, 2734 }, { 2743, 2739, 2736 }, { 2743, 2739, 2736, 2734 }, { 2743, 2739, 2736, 2734, 2734 }, { 2743, 2739, 2736, 2736 }, { 2743, 2739, 2739 }, { 2743, 2741 }, { 2743, 2741, 2734 }, { 2743, 2741, 2734, 2734 }, { 2743, 2741, 2736 }, { 2743, 2741, 2736, 2734 }, { 2743, 2741, 2736, 2734, 2734 }, { 2743, 2741, 2736, 2736 }, { 2743, 2741, 2739 }, { 2743, 2741, 2739, 2734 }, { 2743, 2741, 2739, 2734, 2734 }, { 2743, 2741, 2739, 2736 }, { 2743, 2741, 2739, 2736, 2734 }, { 2743, 2741, 2739, 2736, 2734, 2734 }, { 2743, 2741, 2739, 2736, 2736 }, { 2743, 2741, 2739, 2739 }, { 2743, 2741, 2741 }, { 2743, 2743 }, { 2745 } };

	private static final int THHAAR_MEJ_JAL = 2617;

	private static final int[] MUSICS = { 1088, 1082, 1086 };

	public boolean spawned;

	public int selectedMusic;

	private int[] boundChuncks;

	private Stages stage;

	private boolean logoutAtEnd;

	private boolean login;

	@Override
	public void start() {
		loadCave(false);
	}

	public void loadCave(final boolean login) {
		this.login = login;
		stage = Stages.LOADING;
		player.getLockManagement().lockAll();
		CoresManager.execute(() -> {
			// finds empty map bounds
			boundChuncks = RegionBuilder.findEmptyChunkBound(8, 8);
			// copys real map into the empty map
			// 552 640
			RegionBuilder.copyAllPlanesMap(302, 639, boundChuncks[0], boundChuncks[1], 64);
			RegionBuilder.copyAllPlanesMap(296, 632, boundChuncks[0], boundChuncks[1], 64);
			// selects a music
			selectedMusic = MUSICS[Utils.random(MUSICS.length)];
			player.setNextWorldTile(!login ? getWorldTile(46, 61) : getWorldTile(32, 32));
			// 1delay because player cant walk while teleing :p, + possible
			// issues avoid
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					if (!login) {
						WorldTile walkTo = getWorldTile(32, 32);
						player.addWalkSteps(walkTo.getX(), walkTo.getY());
					}
					player.getDialogueManager().startDialogue(SimpleNPCMessage.class, THHAAR_MEJ_JAL, "You're on your own now, JalYt.<br>Prepare to fight for your life!");
					player.setForceMultiArea(true);
					playMusic();
					player.getLockManagement().unlockAll();
					stage = Stages.RUNNING;
				}
			}, 1);
			if (!login) {
				CoresManager.FAST_EXECUTOR.schedule(new TimerTask() {

					@Override
					public void run() {
						try {
							if (stage != Stages.RUNNING) {
								return;
							}
							startWave();
						} catch (Throwable t) {
							t.printStackTrace();
						}
					}
				}, 6000);
			}
		});
	}

	/*
	 * gets worldtile inside the map
	 */
	public WorldTile getWorldTile(int mapX, int mapY) {
		return new WorldTile(boundChuncks[0] * 8 + mapX, boundChuncks[1] * 8 + mapY, 0);
	}

	public void playMusic() {
		player.getMusicsManager().playMusic(selectedMusic);
	}

	public void startWave() {
		int currentWave = getCurrentWave();
		if (currentWave > WAVES.length) {
			win();
			return;
		}
		player.getInterfaceManager().sendTab(player.getInterfaceManager().onResizable() ? 6 : 9, 316);
		player.sendMessage("You are now starting wave: " + currentWave);
		player.getPackets().sendConfig(639, currentWave);
		if (stage != Stages.RUNNING) {
			return;
		}
		for (int id : WAVES[currentWave - 1]) {
			if (id == 2736) {
				new TzKekCaves(id, getSpawnTile());
			} else if (id == 2745) {
				new TzTok_Jad(id, getSpawnTile(), this);
			} else {
				new FightCavesNPC(id, getSpawnTile());
			}
		}
		spawned = true;
	}

	public void win() {
		if (stage != Stages.RUNNING) {
			return;
		}
		exitCave(4);
	}

	public WorldTile getSpawnTile() {
		switch (Utils.random(5)) {
			case 0:
				return getWorldTile(11, 16);
			case 1:
				return getWorldTile(51, 25);
			case 2:
				return getWorldTile(10, 50);
			case 3:
				return getWorldTile(46, 49);
			case 4:
			default:
				return getWorldTile(32, 30);
		}
	}

	/*
	 * logout or not. if didnt logout means lost, 0 logout, 1, normal, 2 tele
	 */
	public void exitCave(int type) {
		stage = Stages.DESTROYING;
		WorldTile outside = OUTSIDE;
		if (type == 0 || type == 2) {
			player.setLocation(outside);
		} else {
			player.setForceMultiArea(false);
			player.getPackets().closeInterface(player.getInterfaceManager().onResizable() ? 6 : 9);
			if (type == 1 || type == 4) {
				player.setNextWorldTile(outside);
				if (type == 4) {
					AchievementHandler.incrementProgress(getPlayer(), Tztok_Me.class, 1);
					AchievementHandler.incrementProgress(player, Cave_Addiction.class, 1);
					player.setCompletedFightCaves();
					player.reset();
					player.getDialogueManager().startDialogue("SimpleNPCMessage", THHAAR_MEJ_JAL, "You even defeated Tz Tok-Jad, I am most impressed! Please accept this gift as a reward.");
					player.getPackets().sendGameMessage("You were victorious!!");
					player.getInventory().addItemDrop(6570, 1);
					player.getInventory().addItemDrop(6529, 16064);
				} else if (getCurrentWave() == 1) {
					player.getDialogueManager().startDialogue("SimpleNPCMessage", THHAAR_MEJ_JAL, "Well I suppose you tried... better luck next time.");
				} else {
					int tokkul = (getCurrentWave() - getInitialWave() * 8032 / WAVES.length);
					if (!player.getInventory().addItem(6529, tokkul)) {
						World.addGroundItem(new Item(6529, tokkul), new WorldTile(player), player, true, 60);
					}
					player.getDialogueManager().startDialogue("SimpleNPCMessage", THHAAR_MEJ_JAL, "Well done in the cave, here, take TokKul as reward.");
				}
			}
			removeController();
		}
		CoresManager.schedule(() -> RegionBuilder.destroyMap(boundChuncks[0], boundChuncks[1], 8, 8), 1200, TimeUnit.MILLISECONDS);
	}

	public int getCurrentWave() {
		if (getArguments() == null || getArguments().length == 0) { return 1; }
		Number numbArgument = getArgument(0);
		return numbArgument.intValue();
	}

	public void setCurrentWave(int wave) {
		if (getArguments() == null || getArguments().length == 0) {
			this.setArguments(new Object[1]);
		}
		getArguments()[0] = wave;
	}

	public int getInitialWave() {
		if (getArguments() == null || getArguments().length == 0) { return 1; }
		Number numbArgument = getArgument(1);
		return numbArgument.intValue();
	}

	@Override
	public void process() {
		if (spawned) {
			List<Integer> npcs = World.getRegion(player.getRegionId()).getNPCsIndexes();
			if (npcs == null || npcs.isEmpty()) {
				spawned = false;
				nextWave();
			}
		}
	}

	@Override
	public void moved() {
		if (stage != Stages.RUNNING || !login) {
			return;
		}
		login = false;
		setWaveEvent();
	}

	public void setWaveEvent() {
		if (getCurrentWave() == 63) {
			player.getDialogueManager().startDialogue("SimpleNPCMessage", THHAAR_MEJ_JAL, "Look out, here comes TzTok-Jad!");
		}
		CoresManager.FAST_EXECUTOR.schedule(new TimerTask() {

			@Override
			public void run() {
				try {
					if (stage != Stages.RUNNING) {
						return;
					}
					startWave();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}, 600);
	}

	@Override
	public void magicTeleported(int type) {
		exitCave(2);
	}

	@Override
	public boolean processMagicTeleport(WorldTile toTile) {
		player.getPackets().sendGameMessage("You can't teleport out of the arena!");
		return false;
	}

	@Override
	public boolean processItemTeleport(WorldTile toTile) {
		player.getPackets().sendGameMessage("You can't teleport out of the arena!");
		return false;
	}

	@Override
	public boolean processObjectTeleport(WorldTile toTile) {
		player.getPackets().sendGameMessage("You can't teleport out of the arena!");
		return false;
	}

	@Override
	public boolean processObjectClick1(WorldObject object) {
		if (object.getId() == 9357) {
			if (stage != Stages.RUNNING) {
				return false;
			}
			exitCave(1);
			return false;
		}
		return true;
	}

	@Override
	public boolean processButtonClick(int interfaceId, int componentId, int slotId, int packetId) {
		if (stage != Stages.RUNNING) {
			return false;
		}
		if (interfaceId == 182 && (componentId == 6 || componentId == 13)) {
			if (!logoutAtEnd) {
				logoutAtEnd = true;
				player.getPackets().sendGameMessage("<col=ff0000>You will be logged out automatically at the end of this wave.");
				player.getPackets().sendGameMessage("<col=ff0000>If you log out sooner, you will have to repeat this wave.");
			} else {
				player.logout();
			}
			return false;
		}
		return true;
	}

	@Override
	public boolean sendDeath() {
		player.getLockManagement().lockAll(7000);
		player.stopAll();
		WorldTasksManager.schedule(new WorldTask() {
			int loop;

			@Override
			public void run() {
				if (loop == 0) {
					player.setNextAnimation(new Animation(836));
				} else if (loop == 1) {
					player.getPackets().sendGameMessage("You have been defeated!");
				} else if (loop == 3) {
					player.reset();
					exitCave(1);
					player.setNextAnimation(new Animation(-1));
				} else if (loop == 4) {
					player.getPackets().sendMusicEffect(90);
					stop();
				}
				loop++;
			}
		}, 0, 1);
		return false;
	}

	/*
	 * return false so wont remove script
	 */
	@Override
	public boolean login() {
		loadCave(true);
		return false;
	}

	/*
	 * return false so wont remove script
	 */
	@Override
	public boolean logout() {
		/*
		 * only can happen if dungeon is loading and system update happens
		 */
		if (stage != Stages.RUNNING) {
			return false;
		}
		exitCave(0);
		return false;

	}

	@Override
	public void forceClose() {
		/*
		 * shouldnt happen
		 */
		if (stage != Stages.RUNNING) {
			return;
		}
		exitCave(2);
	}

	public void nextWave() {
		playMusic();
		setCurrentWave(getCurrentWave() + 1);
		if (logoutAtEnd) {
			player.forceLogout();
			return;
		}
		setWaveEvent();
	}

	public static void enterFightCaves(Player player, boolean dialogueFirst) {
		if (player.getFamiliar() != null || player.getPet() != null || Summoning.hasPouch(player) || Pets.hasPet(player)) {
			player.getDialogueManager().startDialogue("SimpleNPCMessage", THHAAR_MEJ_JAL, "Get rid of your familiar or pet before entering!");
			return;
		}
		if (dialogueFirst) {
			player.getDialogueManager().startDialogue(FightCavesEntranceD.class);
		} else {
			int initialWave = player.isAnyDonator() ? player.hasPrivilegesOf(RightManager.ELITE_DONATOR) ? 56 : 30 : 1;
			player.getControllerManager().startController("FightCavesControler", initialWave, initialWave);
		}
	}

	public void spawnHealers(TzTok_Jad jad) {
		if (stage != Stages.RUNNING) {
			return;
		}
		for (int i = 0; i < 4; i++) {
			new Yt_HurKot(jad, 2746, getSpawnTile());//Lets them actually heal jad
		}
	}

	private enum Stages {
		LOADING,
		RUNNING,
		DESTROYING
	}
}
