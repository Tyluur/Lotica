package com.runescape.game.event.interaction.object;

import com.runescape.game.content.skills.runecrafting.AbyssObstacles;
import com.runescape.game.content.skills.runecrafting.Runecrafting;
import com.runescape.game.event.interaction.type.ObjectInteractionEvent;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.world.ClickOption;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 6/19/2015
 */
public class AbyssalObjectInteractionEvent extends ObjectInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] { 7143, 7153, 7152, 7144, 7150, 7146, 7147, 7148, 7149, 7139, 7151, 7145, 7137, 7140, 7131, 713,
				                 7129, 7133, 7132, 7141, 7134, 7138, 7136 };
	}

	@Override
	public boolean handleObjectInteraction(Player player, WorldObject object, ClickOption option) {
		int id = object.getId();
		if (id == 7143 || id == 7153) {
			AbyssObstacles.clearRocks(player, object);
		} else if (id == 7152 || id == 7144) {
			AbyssObstacles.clearTendrills(player, object, new WorldTile(id == 7144 ? 3028 : 3051, 4824, 0));
		} else if (id == 7150 || id == 7146) {
			AbyssObstacles.clearEyes(player, object, new WorldTile(object.getX() == 3021 ? 3028 : 3050, 4839, 0));
		} else if (id == 7147) {
			AbyssObstacles.clearGap(player, object, new WorldTile(3030, 4843, 0), false);
		} else if (id == 7148) {
			AbyssObstacles.clearGap(player, object, new WorldTile(3040, 4845, 0), true);
		} else if (id == 7149) {
			AbyssObstacles.clearGap(player, object, new WorldTile(3048, 4842, 0), false);
		} else if (id == 7151) {
			AbyssObstacles.burnGout(player, object, new WorldTile(3053, 4831, 0));
		} else if (id == 7145) {
			AbyssObstacles.burnGout(player, object, new WorldTile(3024, 4834, 0));
		} else if (id == 7137) {
			Runecrafting.enterWaterAltar(player);
		} else if (id == 7139) {
			Runecrafting.enterAirAltar(player);
		} else if (id == 7140) {
			Runecrafting.enterMindAltar(player);
		} else if (id == 7131) {
			Runecrafting.enterBodyAltar(player);
		} else if (id == 7130) {
			Runecrafting.enterEarthAltar(player);
		} else if (id == 7129) {
			Runecrafting.enterFireAltar(player);
		} else if (id == 7133) {
			Runecrafting.enterNatureAltar(player);
		} else if (id == 7132) {
			Runecrafting.enterCosmicAltar(player);
		} else if (id == 7141) {
			Runecrafting.enterBloodAltar(player);
		} else if (id == 7134) {
			Runecrafting.enterChoasAltar(player);
		} else if (id == 7136) {
			Runecrafting.enterDeathAltar(player);
		} else if (id == 7138) {
			player.getPackets().sendGameMessage("A strange power blocks your exit..");
		}
		return true;
	}
}
