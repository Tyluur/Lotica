package com.runescape.workers.boot;

import com.runescape.workers.game.core.CoresManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 10/24/2015
 */
public class BootHandler {

	/**
	 * The amount of threads we can construct
	 */
	private static final int THREAD_SIZE = CoresManager.AVAILABLE_PROCESSORS;

	/**
	 * The list of work we must complete
	 */
	private static final List<Runnable> WORK_TO_COMPLETE = new ArrayList<>();

	/**
	 * The list of boot workers
	 */
	private static final CopyOnWriteArrayList<BootWorker> BOOT_WORKERS = new CopyOnWriteArrayList<>();

	/**
	 * The amount of work that must be complete
	 */
	private static CountDownLatch countDownLatch;

	/**
	 * Adds all of the runnables to the {@link #WORK_TO_COMPLETE} list
	 *
	 * @param work
	 * 		The work we must complete later
	 */
	public static void addWork(Runnable... work) {
		Collections.addAll(WORK_TO_COMPLETE, work);
		prepareAll();
		executeWorkers();
	}

	/**
	 * Prepares all essentials for work to be done. We first construct the {@link #countDownLatch}, then create {@code
	 * BootWorker}s into the {@link #BOOT_WORKERS} list, then the {@link #prepareBootWorkers()} method is ran
	 */
	public static void prepareAll() {
		countDownLatch = new CountDownLatch(WORK_TO_COMPLETE.size());
		for (int i = 0; i < THREAD_SIZE; i++) {
			BOOT_WORKERS.add(new BootWorker(i));
		}
		prepareBootWorkers();
	}

	/**
	 * Prepares the workers by populating them with workload from the {@link #WORK_TO_COMPLETE}
	 */
	public static void prepareBootWorkers() {
		int index = 0;
		for (Iterator<Runnable> it$ = WORK_TO_COMPLETE.iterator(); it$.hasNext(); ) {
			getBestWorker().addToWorkLoad(it$.next(), index);
			it$.remove();
			index++;
		}
	}

	/**
	 * Executes the workers
	 */
	public static void executeWorkers() {
		ExecutorService service = Executors.newFixedThreadPool(CoresManager.AVAILABLE_PROCESSORS);
		//System.out.println("submitted to " + service.toString());
		BOOT_WORKERS.forEach(service::execute);
//		BOOT_WORKERS.forEach(worker -> new Thread(worker).start());
	}

	/**
	 * Getting the best worker to use for the upcoming workload. This is dependent on the amount of work the worker
	 * currently has to do
	 */
	private static BootWorker getBestWorker() {
		int leastWorkDone = -1;
		BootWorker bestWorker = null;
		for (BootWorker worker : BOOT_WORKERS) {
			if (worker.getWorkLoadSize() < leastWorkDone || leastWorkDone == -1) {
				leastWorkDone = worker.getWorkLoadSize();
				bestWorker = worker;
			}
		}
		return bestWorker;
	}

	/**
	 * Awaits the completion of the countdown
	 */
	public static void await() {
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Performs the finishing operations on the threads after we have completed
	 */
	public static void finish() {
		BOOT_WORKERS.forEach(com.runescape.workers.boot.BootWorker::interrupt);
		BOOT_WORKERS.clear();
	}

	/**
	 * Gets the worker numbers left in a list
	 */
	private static List<Integer> workerNumbersLeft() {
		List<Integer> result = new ArrayList<>();
		BOOT_WORKERS.forEach(worker -> worker.getWorkLoad().forEach(load -> result.add(load.getTaskNumber())));
		return result;
	}

	/**
	 * The details of the workers left
	 */
	public static String workersLeftDetails() {
		String details = "";
		List<Integer> numbersLeft = workerNumbersLeft();
		for (int i = 0; i < numbersLeft.size(); i++) {
			details = details + "" + numbersLeft.get(i) + "" + (i == numbersLeft.size() - 1 ? "" : ", ");
		}
		return details;
	}

	/**
	 * Gets the {@link #countDownLatch}
	 */
	public static CountDownLatch getCountDownLatch() {
		return countDownLatch;
	}

}
