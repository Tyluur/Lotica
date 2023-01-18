package com.runescape

import com.runescape.cache.Cache
import com.runescape.cache.loaders.ItemsEquipIds
import com.runescape.game.GameConstants
import com.runescape.game.content.FishingSpotsHandler
import com.runescape.game.content.FriendChatsManager
import com.runescape.game.content.GlobalPlayers
import com.runescape.game.content.RandomEventManager
import com.runescape.game.content.bot.BotInitializer
import com.runescape.game.content.economy.treasure.TreasureTrailData
import com.runescape.game.content.economy.treasure.TreasureTrailHandler
import com.runescape.game.content.global.clans.ClansManager
import com.runescape.game.content.global.commands.CommandHandler
import com.runescape.game.content.global.lottery.Lottery
import com.runescape.game.content.global.wilderness.WildernessActivityManager
import com.runescape.game.content.skills.mining.Mining
import com.runescape.game.event.interaction.InteractionEventManager
import com.runescape.game.interaction.controllers.ControllerHandler
import com.runescape.game.interaction.controllers.impl.nmz.powerup.PowerupGenerator
import com.runescape.game.interaction.cutscenes.CutscenesHandler
import com.runescape.game.interaction.dialogues.DialogueHandler
import com.runescape.game.world.World
import com.runescape.game.world.entity.npc.combat.CombatScriptsHandler
import com.runescape.game.world.entity.player.QuestManager
import com.runescape.game.world.entity.player.achievements.AchievementHandler
import com.runescape.game.world.item.ItemProperties
import com.runescape.game.world.item.WorldFloorItems
import com.runescape.game.world.region.MapMerging
import com.runescape.game.world.region.RegionBuilder
import com.runescape.network.LoticaChannelHandler
import com.runescape.network.stream.incoming.IncomingStreamHandler
import com.runescape.utility.ConfigurationParser
import com.runescape.utility.ItemDefinitionLoader
import com.runescape.utility.applications.console.listener.ConsoleListener
import com.runescape.utility.cache.huffman.Huffman
import com.runescape.utility.external.gson.GsonStartup
import com.runescape.utility.logging.ErrLogger
import com.runescape.utility.logging.OutLogger
import com.runescape.utility.world.Censor
import com.runescape.utility.world.map.MapArchiveKeys
import com.runescape.utility.world.map.MapAreas
import com.runescape.utility.world.map.MusicHints
import com.runescape.utility.world.npc.NPCWalkingData
import com.runescape.utility.world.`object`.ObjectRemoval
import com.runescape.utility.world.`object`.ObjectSpawns
import com.runescape.utility.world.player.DTRank
import com.runescape.utility.world.player.PkRank
import com.runescape.utility.world.player.StarterList
import com.runescape.workers.boot.BootHandler
import com.runescape.workers.game.core.CoresManager
import com.runescape.workers.game.log.SystemOutLogger
import com.runescape.workers.game.login.LoginRequestProcessor
import java.io.IOException
import kotlin.system.exitProcess

class Main private constructor() {
    /**
     * @return the startTime
     */
    /**
     * The time at which this class was started
     */
    @JvmField
    val startTime: Long = System.currentTimeMillis()

    /**
     * If the server was started
     */
    private var started = false

    /**
     * This method will initialize all necessary data to the server from external locations.
     */
    @Throws(IOException::class)
    private fun initialize() {
        // cache is started before all else
        Cache.init()
        // work is then prepared
        BootHandler.addWork(
            Runnable {
                try {
                    CoresManager.init()
                    World.init()
                    ControllerHandler.init()
                    WildernessActivityManager.getSingleton().load()
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: ClassNotFoundException) {
                    e.printStackTrace()
                } catch (e: InstantiationException) {
                    e.printStackTrace()
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }
            },
            Runnable {
                PkRank.init()
                DTRank.init()
            },
            Runnable {
                MapArchiveKeys.init()
                MapAreas.init()
                Mining.load()
            },
            Runnable {
                ObjectRemoval.initialize()
                ObjectSpawns.init()
                FishingSpotsHandler.init()
                TreasureTrailData.TreasureTrailTier.dumpRewardIds()
            },
            Runnable {
                TreasureTrailHandler.loadAll()
                AchievementHandler.loadAll(false)
                IncomingStreamHandler.loadAll()
            },
            Runnable {
                WorldFloorItems.setup()
                ItemProperties.loadProperties()
                CombatScriptsHandler.init()
            },
            Runnable { RegionBuilder.init() },
            Runnable { MusicHints.init() },
            Runnable { ItemsEquipIds.init() },
            Runnable { Huffman.init() },
            Runnable {
                GsonStartup.loadAll()
                CommandHandler.initialize()
            },
            Runnable {
                try {
                    DialogueHandler.init()
                    GlobalPlayers.loadOnlineToday()
                    ConfigurationParser.loadConfiguration() // 10
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            },
            Runnable {
                Lottery.loadLottery()
                CutscenesHandler.init()
                FriendChatsManager.init()
                ClansManager.init()
                RandomEventManager.populateQuestionBank()
            },
            Runnable {
                Censor.init()
                PowerupGenerator.loadPowerups()
            },
            Runnable {
                StarterList.loadStarters()
                NPCWalkingData.loadList()
            },
            Runnable {
                QuestManager.initialize()
                InteractionEventManager.initialize()
            },
            Runnable { ItemDefinitionLoader.load() })
        BootHandler.await()
    }

    /**
     * Performs the tasks that take place after the server has been initialized
     */
    @Throws(InterruptedException::class)
    fun performCompletionTasks() {
        BootHandler.finish()
        MapMerging.start()
        ConsoleListener.initializeConsoleListener()
        LoginRequestProcessor.getSingleton().init()
        LoticaChannelHandler.init()
        BotInitializer.initializeBots()
        started = true
    }

    /**
     * Gets the started variable
     */
    fun hasStarted(): Boolean {
        return started
    }

    companion object {
        /**
         * The instance of this class
         */
        private var SINGLETON: Main? = null

        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            System.setProperty("user.timezone", "EST")
            System.setOut(OutLogger(System.out))
            System.setErr(ErrLogger(System.err))
            println(GameConstants.SERVER_NAME + " is running on the " + (if (GameConstants.HOSTED) "host" else "local") + " server.")
            val main = get()
            main!!.initialize()
            main.performCompletionTasks()
            println("Game Server prepared in " + (System.currentTimeMillis() - main.startTime) + " milliseconds.")
        }

        /**
         * Gets the [.SINGLETON]
         */
        @JvmStatic
        fun get(): Main? {
            if (SINGLETON == null) {
                SINGLETON = Main()
            }
            return SINGLETON
        }

        @JvmStatic
        fun shutdown() {
            closeServices()
            exitProcess(-1)
        }

        fun closeServices() {
            CoresManager.shutdown()
            SystemOutLogger.flushAll()
        }
    }
}