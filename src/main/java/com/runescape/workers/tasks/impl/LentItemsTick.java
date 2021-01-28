package com.runescape.workers.tasks.impl;

import com.runescape.utility.external.gson.loaders.LentItemsLoader;
import com.runescape.workers.tasks.WorldTask;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/20/2015
 */
public class LentItemsTick extends WorldTask {
	
	@Override
	public void run() {
		LentItemsLoader.pulse();
	}
}
