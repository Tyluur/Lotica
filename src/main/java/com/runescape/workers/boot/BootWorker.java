package com.runescape.workers.boot;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Tyluur<itstyluur @ gmail.com>
 * @since 10/24/2015
 */
public class BootWorker extends Thread {

    /**
     * The load of work we must complete
     */
    private final CopyOnWriteArrayList<BootTask> workLoad = new CopyOnWriteArrayList<>();

    private static final ExecutorService service = Executors.newSingleThreadScheduledExecutor();

    private static final boolean DEBUG_RUN = true;

    /**
     * The number of the book worker
     */
    private final int number;

    public BootWorker(int number) {
        setName("Boot Worker #" + (number + 1));
        this.number = number;
    }

    @Override
    public void run() {
        for (BootTask work : new ArrayList<>(workLoad)) {
            new Thread(() -> {
                try {
                    long start = System.currentTimeMillis();
                    work.getTask().run();
                    workLoad.remove(work);
                    BootHandler.getCountDownLatch().countDown();
                    long delay = System.currentTimeMillis() - start;

                    if (DEBUG_RUN) {
                        System.out.println("Worker #" + number + ":\t\tFinished job " + work.getTaskNumber() + " in " + delay + " ms\t\t\tqueue=[" + BootHandler.workersLeftDetails() + "]");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    /**
     * Adds more work to the {@link #workLoad}
     *
     * @param work  The work to add
     * @param index The index of the work
     */
    public void addToWorkLoad(Runnable work, int index) {
        workLoad.add(new BootTask(work, index));
    }

    /**
     * Gets the size of the workload
     */
    public int getWorkLoadSize() {
        return workLoad.size();
    }

    public CopyOnWriteArrayList<BootTask> getWorkLoad() {
        return this.workLoad;
    }
}
