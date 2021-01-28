package com.runescape.game.event.interaction.object;

import com.runescape.game.event.interaction.type.ObjectInteractionEvent;
import com.runescape.game.interaction.controllers.impl.nmz.NMZLobby;
import com.runescape.game.interaction.dialogues.impl.minigame.NMZInitiation;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.world.ClickOption;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 12/27/2015
 */
public class NightmareZoneTransportationInteractionEvents extends ObjectInteractionEvent {

	@Override
	public int[] getKeys() {
		return new int[] {/* 2465, 102 */};
	}

	private static final WorldTile NEXT_TO_ROOM_PORTAL = new WorldTile(3123, 3494, 0);

	private static final WorldTile CLOSE_TO_BANK_PORTAL = new WorldTile(3108, 3491, 0);

	private static final WorldTile FIRST_PORTAL_TILE = new WorldTile(3108, 3490, 0);

	private static final WorldTile SECOND_PORTAL_TILE = new WorldTile(3123, 3495, 0);

	@Override
	public boolean handleObjectInteraction(Player player, WorldObject object, ClickOption option) {
		if (object.getId() == 2465) { // portals
			if (player.getX() < 3109) { // to the room
				stepThroughPortal(player, NEXT_TO_ROOM_PORTAL);
			} else {
				stepThroughPortal(player, CLOSE_TO_BANK_PORTAL);
			}
		} else if (object.getId() == 102) {
			if (player.getControllerManager().getController() == null) {
				player.getDialogueManager().startDialogue(NMZInitiation.class);
			} else {
				player.getControllerManager().verifyControlerForOperation(NMZLobby.class).ifPresent(NMZLobby::leave);
			}
		}
		return true;
	}

	/**
	 * Performs an animation that shows the player stepping through a portal
	 *
	 * @param player
	 * 		The player
	 * @param teleportLocation
	 * 		The location they appear in afterwards
	 */
	private static void stepThroughPortal(Player player, WorldTile teleportLocation) {
		player.setNextAnimation(new Animation(-1));
		player.getLockManagement().lockAll();
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				player.setNextAnimation(new Animation(2586));
				player.getAppearence().setRenderEmote(-1);
				WorldTasksManager.schedule(new WorldTask() {
					@Override
					public void run() {
						player.setNextWorldTile(teleportLocation);
						player.setNextAnimation(new Animation(2588));
						player.setNextFaceWorldTile(!teleportLocation.equals(CLOSE_TO_BANK_PORTAL) ? SECOND_PORTAL_TILE : FIRST_PORTAL_TILE);
						WorldTasksManager.schedule(new WorldTask() {
							@Override
							public void run() {
								player.getLockManagement().unlockAll();
							}
						});
						stop();
					}
				});
			}
		});
	}

}
