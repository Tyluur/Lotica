package com.runescape.utility.tools;

import com.alex.store.Store;
import com.runescape.game.GameConstants;

import java.io.IOException;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 1/16/2016
 */
public class CacheTool {

	public static void main(String[] args) {
		try {
			Store oldCache = new Store(System.getProperty("user.home") + "/Desktop/cache/");
			Store currentCache = new Store(GameConstants.CACHE_PATH);

			System.out.println(currentCache.getIndexes()[23].packIndex(oldCache));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
