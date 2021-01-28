package com.runescape.game.world.entity.npc.combat;

import com.runescape.game.world.entity.Entity;
import com.runescape.game.world.entity.npc.NPC;
import com.runescape.utility.Utils;

import java.util.HashMap;

public class CombatScriptsHandler {

	private static final HashMap<Object, CombatScript> cachedCombatScripts = new HashMap<>();

	private static final CombatScript DEFAULT_SCRIPT = new Default();

	public static void init() {
		cachedCombatScripts.clear();
		for (Object packet : Utils.getClassesInDirectory(CombatScriptsHandler.class.getPackage().getName() + ".impl")) {
			CombatScript handler = (CombatScript) packet;
			for (Object parameter : handler.getKeys()) {
				cachedCombatScripts.put(parameter, handler);
			}
		}
	}

	public static int specialAttack(final NPC npc, final Entity target) {
		CombatScript script = cachedCombatScripts.get(npc.getId());
		if (script == null) {
			script = cachedCombatScripts.get(npc.getDefinitions().getName());
			if (script == null) {
				script = DEFAULT_SCRIPT;
			}
		}
		return script.attack(npc, target);
	}
}
