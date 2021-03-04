package com.runescape.game.content.bot.impl

import com.runescape.game.GameConstants
import com.runescape.game.content.bot.AbstractBot
import com.runescape.game.world.WorldTile
import com.runescape.game.world.entity.player.PublicChatMessage
import com.runescape.utility.Utils
import java.util.concurrent.TimeUnit

/**
 * @author Tyluur<itstyluur@icloud.com>
 * @since 1/18/2016
 */
class VoteSpammerBot : AbstractBot() {

    override fun botNames(): Array<String> {
        return arrayOf("Luke132")
    }

    override fun getBotAmounts(): Int {
        return 1
    }

    override fun hoverTiles(): Array<WorldTile> {
        return arrayOf(WorldTile(3087, 3497, 0))
    }

    override fun activityTime(): Long {
        return TimeUnit.MINUTES.toMillis(5)
    }

    override fun onPulse() {
        val effects = arrayOf(258, 512, 0, 515, 2, 1, 4)
        val messages = arrayOf(
            "Want cool cosmetics or exclusive rares? ::vote for " + GameConstants.SERVER_NAME + " for such.",
            "Help support " + GameConstants.SERVER_NAME + " and receive great rewards when you ::donate!"
        )
        val effect = Utils.randomArraySlot(effects)
        val message = Utils.randomArraySlot(messages)
        bot.sendPublicChatMessage(bot.setLastChatMessage(PublicChatMessage(message, effect)))
        bot.lastMsg = message
    }

}