package com.runescape.workers.game.core;

import com.runescape.game.world.World;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.workers.tasks.WorldTasksManager;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/22/2016
 */
public class GameUpdateSequence {

	/**
	 * The player updating executor
	 */
	private final Executor executor = Executors.newFixedThreadPool(CoresManager.AVAILABLE_PROCESSORS);

	/**
	 * The start of the update sequence
	 *
	 * @param start
	 * 		The start long time
	 * @param print
	 * 		If debug messages should be printed
	 * @param debugMessages
	 * 		The list of debug messages that will be populated/referenced
	 */
	public void start(long start, boolean print, List<String> debugMessages) {
		WorldTasksManager.processTasks();
		if (print) {
			debugMessages.add("Took " + (System.currentTimeMillis() - start) + " ms to loop tasks");
			start = System.currentTimeMillis();
		}
	}

	/**
	 * The core of the update sequence
	 *
	 * @param start
	 * 		The start long time
	 * @param print
	 * 		If debug messages should be printed
	 * @param debugMessages
	 * 		The list of debug messages that will be populated/referenced
	 */
	public void run(long start, boolean print, List<String> debugMessages) {
		/** Players and npcs are then processed */
		processPlayers();
		if (print) {
			debugMessages.add("Took " + (System.currentTimeMillis() - start) + " ms to process players");
			start = System.currentTimeMillis();
		}
		processNpcs();
		if (print) {
			debugMessages.add("Took " + (System.currentTimeMillis() - start) + " ms to process npcs");
			start = System.currentTimeMillis();
		}
		
		/** Player updating is then handled */
		sendUpdating();
		if (print) {
			debugMessages.add("Took " + (System.currentTimeMillis() - start) + " ms to send updating");
			start = System.currentTimeMillis();
		}
	}

	/**
	 * The reset stage of the update sequence
	 *
	 * @param start
	 * 		The start long time
	 * @param print
	 * 		If debug messages should be printed
	 * @param debugMessages
	 * 		The list of debug messages that will be populated/referenced
	 */
	public void reset(long start, boolean print, List<String> debugMessages) {
		/** Masks are then reset */
		resetPlayerMasks();
		if (print) {
			debugMessages.add("Took " + (System.currentTimeMillis() - start) + " ms to reset player masks");
			start = System.currentTimeMillis();
		}
		resetNpcMasks();
		if (print) {
			debugMessages.add("Took " + (System.currentTimeMillis() - start) + " ms to reset npc masks");
			start = System.currentTimeMillis();
		}
	}
	
	/**
	 * Processes all players.
	 */
	private void processPlayers() {
		long timeTaken = 0;
		for (Player p : World.getPlayers()) {
			if ( p != null && p.hasStarted() && !p.hasFinished()) {
				long ours = p.processEntity();
				if (ours > 50) {
					timeTaken += ours;
				}
				if (timeTaken > 200) {
					Map<String, Long> data = p.getProcessData();
					System.out.println("\t" + p.getUsername() + " took long to process [ " + timeTaken + "]!");
					data.entrySet().stream().forEach(entry -> System.out.println("\t\tkey=" + entry.getKey() + ", val=" + entry.getValue()));
				}
			}
		}
	}
	
	/**
	 * Processes all npcs
	 */
	private void processNpcs() {
		for (NPC npc : World.getNPCs()) {
			if (npc == null || npc.hasFinished()) {
				continue;
			}
			npc.processEntity();
		}
	}
	
	/**
	 * Sends player and npc updating for all players
	 */
	private void sendUpdating() {
//		final CountDownLatch latch = new CountDownLatch(World.getPlayers().size());
		World.players().forEach(player -> {
			player.getPackets().sendLocalPlayersUpdate();
			player.getPackets().sendLocalNPCsUpdate();
//			latch.countDown();
		});
		/*try {
			latch.await(600L, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
	}

	/**
	 * Resets all masks for all players
	 */
	private void resetPlayerMasks() {
		World.players().forEach(Player::resetMasks);
	}
	
	/**
	 * Resets all masks for all npcs
	 */
	private void resetNpcMasks() {
		for (NPC npc : World.getNPCs()) {
			if (npc == null || npc.hasFinished()) {
				continue;
			}
			npc.resetMasks();
		}
	}
}

