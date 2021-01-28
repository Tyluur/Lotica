package com.runescape.utility.tools;

import com.runescape.cache.Cache;
import com.runescape.cache.loaders.RenderAnimDefinitions;

import java.io.IOException;

public class RenderEmotes {
	public static void main(String[] args) throws IOException {
		Cache.init();
		int emoteId = 16652;
		for(int i  = 0; i < 2000; i++) {
			RenderAnimDefinitions defs = RenderAnimDefinitions.getRenderAnimDefinitions(i);
			if(defs.anInt972 == emoteId ||  defs.anInt963 == emoteId)
				System.out.println("RenderID: "+i);
		}
	}
}
