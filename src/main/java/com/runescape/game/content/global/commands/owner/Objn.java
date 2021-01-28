package com.runescape.game.content.global.commands.owner;

import com.runescape.cache.loaders.ObjectDefinitions;
import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;

import java.util.Arrays;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 19, 2015
 */
public class Objn extends CommandSkeleton<String[]> {

	@Override
	public String[] getIdentifiers() {
		return new String[] { "objn" };
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		// if we wish to search with a filter on the object options,
		// we do so  such as: ;;objn [name] [option] [true]
		boolean searchingOptions = cmd.length >= 3;
		for (int i = 0; i < Utils.getObjectDefinitionsSize(); i++) {
			ObjectDefinitions definitions = ObjectDefinitions.getObjectDefinitions(i);
			if (definitions.name.toLowerCase().contains(cmd[1].replaceAll("_", " "))) {
				StringBuilder sbl = new StringBuilder();
				for (int j = 0; j < definitions.getOptions().length; j++) {
					String option = definitions.getOptions()[j];
					if (option == null) { continue; }
					sbl.append(option + ",");
				}
				ObjectDefinitions defs = ObjectDefinitions.getObjectDefinitions(i);
				if (searchingOptions) {
					String[] options = sbl.toString().split(",");
					for (String option : options) {
						if (option.equalsIgnoreCase(cmd[2])) {
							player.getPackets().sendMessage(99, "[OBJECT] " + defs.name + " - ID: " + i + " " + Arrays.toString(sbl.toString().split(",")) + ", SIZE: " + defs.sizeX + "/" + defs.sizeY, player);
						}
					}
				} else {
					player.getPackets().sendMessage(99, "[OBJECT] " + defs.name + " - ID: " + i + " " + Arrays.toString(sbl.toString().split(",")) + ", SIZE: " + defs.sizeX + "/" + defs.sizeY, player);
				}
			}
		}
	}

}
