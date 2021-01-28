package com.runescape.game.content.global.commands.owner;

import com.runescape.game.content.global.commands.CommandSkeleton;
import com.runescape.game.world.World;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 19, 2015
 */
public class ListLocalObjects extends CommandSkeleton<String[]> {

	@Override
	public String[] getIdentifiers() {
		return new String[] { "listobjs" };
	}

	@Override
	public void handleCommand(Player player, String[] cmd) {
		List<WorldObject> objects = World.getRegion(player.getRegionId()).getObjects();
		if (objects == null) {
			throw new IllegalStateException();
		}
		String nameFilter = cmd.length == 2 ? cmd[1] : null;
		Collections.sort(objects, (o1, o2) -> Integer.compare(Utils.getDistance(player, o1), Utils.getDistance(player, o2)));
		objects = objects.stream().filter(p -> Utils.getDistance(player, p) < 5).collect(Collectors.toList());
		if (nameFilter != null) {
			objects = objects.stream().filter(p -> p.getDefinitions().name.contains(nameFilter)).collect(Collectors.toList());
		}
		objects.stream().forEach(System.out::println);
	}

}
