package com.runescape.workers.game.core;

import com.runescape.Main;
import com.runescape.game.world.World;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.game.world.entity.player.Player;
import com.runescape.network.LoticaChannelHandler;
import com.runescape.workers.tasks.WorldTasksManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class GameUpdateWorker extends Thread {

    /**
     * If debug messages should be printed
     */
    public static AtomicBoolean shouldPrintDebug = new AtomicBoolean(true);

    /**
     * The time the last cycle took
     */
    private static long LAST_CYCLE_SPEED = 0;

    /**
     * The amount of times the server has lagged
     */
    private static int lagCounts = 0;

    private static int cycles;

    /**
     * The instance of the update structure
     */
    private final GameUpdateSequence sequence;

    private final Executor executor = Executors.newFixedThreadPool(CoresManager.AVAILABLE_PROCESSORS);

    @Override
    public void run() {
        while (!CoresManager.shutdown) {
            if (!Main.get().hasStarted()) {
                continue;
            }
            try {
                long initial = System.currentTimeMillis();
                long flag = System.currentTimeMillis();
                boolean print = shouldPrintDebug.get();
                List<String> debugMessages = new ArrayList<>();

                WorldTasksManager.processTasks();
                debugMessages.add("Took " + (System.currentTimeMillis() - flag) + " ms for 1");
                flag = System.currentTimeMillis();
                try {
                    // timeTaken = 0;
                    CountDownLatch latch = new CountDownLatch(World.getPlayers().size());
                    for (Player p : World.getPlayers()) {
                        if (p != null && p.hasStarted() && !p.hasFinished()) {
                            executor.execute(() -> {
                                try {
                                    long ours = p.processEntity();
                                    if (ours > 50) {
                                        Map<String, Long> data = p.getProcessData();
                                        System.out.println("\t" + p.getUsername() + " took long to process [ " + ours + "]!");
                                        data.entrySet().stream().forEach(entry -> System.out.println("\t\tkey=" + entry.getKey() + ", val=" + entry.getValue()));
                                    }
                                    latch.countDown();
                                } catch (Throwable e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    }
                    debugMessages.add("Took " + (System.currentTimeMillis() - flag) + " ms for 2");
                    flag = System.currentTimeMillis();
                    for (NPC npc : World.getNPCs()) {
                        if (npc == null || npc.hasFinished()) {
                            continue;
                        }
                        npc.processEntity();
                    }
                    debugMessages.add("Took " + (System.currentTimeMillis() - flag) + " ms for 3");
                    latch.await(600L, TimeUnit.MILLISECONDS);
                    flag = System.currentTimeMillis();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                try {
                    for (Player player : World.getPlayers()) {
                        if (!player.hasStarted() || player.hasFinished()) {
                            continue;
                        }
                        player.getPackets().sendLocalPlayersUpdate();
                        player.getPackets().sendLocalNPCsUpdate();
                    }
                    debugMessages.add("Took " + (System.currentTimeMillis() - flag) + " ms for 4");
                    flag = System.currentTimeMillis();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                try {
                    for (Player player : World.getPlayers()) {
                        if (!player.hasStarted() || player.hasFinished()) {
                            continue;
                        }
                        player.resetMasks();
                    }
                    debugMessages.add("Took " + (System.currentTimeMillis() - flag) + " ms for 5");
                    flag = System.currentTimeMillis();
                    for (NPC npc : World.getNPCs()) {
                        if (npc == null || npc.hasFinished()) {
                            continue;
                        }
                        npc.resetMasks();
                    }
                    debugMessages.add("Took " + (System.currentTimeMillis() - flag) + " ms for 6");
                    flag = System.currentTimeMillis();
                } catch (Throwable e) {
                    e.printStackTrace();
                }

                try {
                    for (Player player : World.getPlayers()) {
                        if (!player.hasStarted() || player.hasFinished()) {
                            continue;
                        }
                        if (player.getSession() == null || player.getSession().getChannel() == null || !player.getSession().getChannel().isOpen()) {
                            player.finish();
                            System.out.println("Finished player\t[1]=" + (player.getSession()) + ",[2]=" + (player.getSession().getChannel()) + ",[3]=" + player.getSession().getChannel().isOpen());
                        }
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                debugMessages.add("Took " + (System.currentTimeMillis() - flag) + " ms for 7");
                flag = System.currentTimeMillis();
                LoticaChannelHandler.processSessionQueue();
                debugMessages.add("Took " + (System.currentTimeMillis() - flag) + " ms for 8");

                /** We store the time we started processing the game world at for sleep calculations */

				/*
				sequence.start(start, print, debugMessages);
				sequence.run(start, print, debugMessages);
				sequence.reset(start, print, debugMessages);

				*//** The thread is set to sleep afterwards */
                setSleep(initial, print, debugMessages);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This method puts the thread to sleep for the amount of time necessary, based on the time taken to process
     * everything
     *
     * @param startTime     The time we started to process the thread
     * @param print         If we are printing debug results
     * @param debugMessages The list of debug messages
     */
    private void setSleep(long startTime, boolean print, List<String> debugMessages) throws InterruptedException {
        long sleepTime = LAST_CYCLE_SPEED = 600 + startTime - System.currentTimeMillis();

        //printing out when the server lags
        if (sleepTime < 500) {
            System.out.println("Cycle Time too high:\t" + sleepTime);
            lagCounts++;
            if (lagCounts >= 10 && !print) {
                System.out.println("Slow cycles will now be printed because we have lagged " + lagCounts + " times.");
                shouldPrintDebug.set(true);
                print = true;
                lagCounts = 0;
            }
            if (print) {
                /** Printing debug messages */
                debugMessages.forEach(System.out::println);
            }
        }

        if (sleepTime > 0) {
            Thread.sleep(sleepTime);
        }
        cycles++;
    }

    protected GameUpdateWorker() {
        setPriority(Thread.MAX_PRIORITY);
        setName("GameUpdateWorker");
        sequence = new GameUpdateSequence();
    }

    /**
     * Gets the last cycle speed
     */
    public static long getLastCycleSpeed() {
        return LAST_CYCLE_SPEED;
    }

    public static int getCycles() {
        return cycles;
    }

    public static void setCycles(int cycles) {
        GameUpdateWorker.cycles = cycles;
    }
}
