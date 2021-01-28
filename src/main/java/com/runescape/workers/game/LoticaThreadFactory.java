package com.runescape.workers.game;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 5/1/2016
 */
public class LoticaThreadFactory implements ThreadFactory {

	private static final AtomicInteger poolNumber = new AtomicInteger(1);

	private final ThreadGroup group;

	private final AtomicInteger threadNumber = new AtomicInteger(1);

	private final String namePrefix;

	public LoticaThreadFactory(String name) {
		SecurityManager s = System.getSecurityManager();
		group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
		namePrefix = "Lotica-" + name + "-thread-";
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
		if (t.isDaemon()) { t.setDaemon(false); }
		if (t.getPriority() != Thread.MIN_PRIORITY) { t.setPriority(Thread.MIN_PRIORITY); }
		return t;
	}

}
