package com.runescape.game.interaction.cutscenes.actions;

import com.runescape.game.world.entity.player.Player;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

/**
 * Handles an interface showing up cutscene action.
 * @author Emperor
 *
 */
public final class InterfaceAction extends CutsceneAction {

	/**
	 * The interface id.
	 */
	private final int interfaceId;
	
	/**
	 * The delay.
	 */
	private final int delay;
	
	/**
	 * Constructs a new {@code InterfaceAction} {@code Object}.
	 * @param interfaceId The interface id.
	 * @param actionDelay The action delay.
	 */
	public InterfaceAction(int interfaceId, int actionDelay) {
		super(-1, actionDelay);
		this.interfaceId = interfaceId;
		this.delay = actionDelay;
	}

	@Override
	public void process(final Player player, Object[] cache) {
		player.getInterfaceManager().sendInterface(interfaceId);
		WorldTasksManager.schedule(new WorldTask() {
			@Override
			public void run() {
				player.getInterfaceManager().closeScreenInterface();
			}			
		}, delay);
	}

}