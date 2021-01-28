package com.runescape.game.world.entity.player.quests;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 18, 2015
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface QuestTag {
	Class<? extends Enum<?>> value();
}
