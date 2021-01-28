package com.runescape.utility;

import com.runescape.cache.Cache;
import com.runescape.cache.loaders.ItemDefinitions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 4/14/2016
 */
public class ItemDefinitionLoader {

	private static final int SEPARATED_AMOUNT = 20;

	public static void main(String[] args) throws IOException {
		Cache.init();
		load();
	}

	public static void load() {
		long start = System.currentTimeMillis();

		int size = Utils.getItemDefinitionsSize();

		final List<Integer> parts = new ArrayList<>();


		parts.add(0);
		for (int i = 0; i < SEPARATED_AMOUNT; i++) {
			int div = size / SEPARATED_AMOUNT;
			parts.add(div * (i + 1));
		}

//		System.out.println(parts.size() + " parts: " + parts);

		final CountDownLatch latch = new CountDownLatch(parts.size());
		for (int i = 0; i < parts.size(); i++) {
			final Counter c = new Counter() {
				@Override
				public void run() {
					for (int i = getStartId(); i <= getEndId(); i++) {
						ItemDefinitions.getItemDefinitions(i);
					}
					latch.countDown();
				}
			};

			boolean last = (i == parts.size() - 1);
			c.setStartId(parts.get(i));
			c.setEndId(last ? size : parts.get(i + 1));
			Thread thread = new Thread(c);
			thread.setName("Thread " + i + " ");
			thread.start();
		}

		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("Cached all item definitions in " + (System.currentTimeMillis() - start));
	}

	private abstract static class Counter implements Runnable {

		private int startId, endId;

        public int getStartId() {
            return this.startId;
        }

        public int getEndId() {
            return this.endId;
        }

        public void setStartId(int startId) {
            this.startId = startId;
        }

        public void setEndId(int endId) {
            this.endId = endId;
        }
    }
}
