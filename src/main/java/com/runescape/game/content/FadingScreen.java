package com.runescape.game.content;

import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;
import com.runescape.workers.game.core.CoresManager;
import com.runescape.workers.tasks.WorldTask;
import com.runescape.workers.tasks.WorldTasksManager;

import java.util.TimerTask;

public final class FadingScreen {

	private FadingScreen() {
		
	}
	
	public static void fade(final Player player, final Runnable event) {
		unfade(player, fade(player), event);
	}
	
	public static void unfade(final Player player, long startTime, final Runnable event) {
		long leftTime = 2500 - (Utils.currentTimeMillis() - startTime);
		if (leftTime > 0) {
			CoresManager.FAST_EXECUTOR.schedule(new TimerTask() {
				@Override
				public void run() {
					try {
						unfade(player, event);
					} catch (Throwable e) {
						e.printStackTrace();
					}
				}
				
			}, leftTime);
		} else { unfade(player, event); }
	}
	
	
	public static void unfade(final Player player, Runnable event) {
		event.run();
		WorldTasksManager.schedule(new WorldTask() {

			@Override
			public void run() {
				player.getInterfaceManager().sendFadingInterface(170);
				CoresManager.FAST_EXECUTOR.schedule(new TimerTask() {
					@Override
					public void run() {
						try {
							player.getInterfaceManager().closeFadingInterface();
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}
				}, 2000);
			}
			
		});
	}
	
	public static long fade(Player player) {
		player.getInterfaceManager().sendFadingInterface(115);
		return Utils.currentTimeMillis();
	}

	public static void displayFade(Player player, FadeTypes type, int ticks) {
		player.getInterfaceManager().sendFadingInterface(type.interfaceId);
		if (ticks != -1) {
			WorldTasksManager.schedule(new WorldTask() {
				@Override
				public void run() {
					player.getInterfaceManager().sendFadingInterface(FadeTypes.FADE_TO_REGULAR.interfaceId);
				}
			}, ticks);
		}
	}

	public static void removeFade(Player player) {
		player.getInterfaceManager().sendFadingInterface(FadeTypes.FADE_TO_REGULAR.interfaceId);
	}

	public enum FadeTypes {
		ALL_BLACK(85),
		FADE_TO_BLACK(115),
		FADE_TO_REGULAR(170);

		private final int interfaceId;

		FadeTypes(int interfaceId) {
			this.interfaceId = interfaceId;
		}

	}
}
