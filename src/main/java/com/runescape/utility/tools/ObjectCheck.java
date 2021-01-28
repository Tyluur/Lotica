package com.runescape.utility.tools;

import com.runescape.cache.Cache;
import com.runescape.cache.loaders.ObjectDefinitions;
import com.runescape.utility.Utils;

import java.io.IOException;

public class ObjectCheck {

	public static void main(String[] args) throws IOException {
		Cache.init();
		for (int i = 0; i < Utils.getObjectDefinitionsSize(); i++) {
			ObjectDefinitions def = ObjectDefinitions.getObjectDefinitions(i);
			if ( def.containsOption("Steal-from")) {
				System.out.println(def.id+" - "+def.name);
			}
		}
	}

}
