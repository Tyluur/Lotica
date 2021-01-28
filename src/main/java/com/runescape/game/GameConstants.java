package com.runescape.game;

import com.runescape.game.world.WorldTile;

public final class GameConstants {

    /**
     * General client and server settings.
     */
    public static final String SERVER_NAME = "Lotica";

    /**
     * The path the cache will be loaded from
     */
    public static final String CACHE_PATH = "./data/cache/";

    /**
     * If we're hosted on linux
     */
    public static final boolean LINUX_HOST = System.getProperty("os.name").toLowerCase().contains("linux");

    /**
     * The server is on hosted mode if the main user name contains 'Administrator'
     */
    public static boolean HOSTED = System.getProperty("user.home").toLowerCase().contains("root") || LINUX_HOST;

    /**
     * The path for files to be saved at
     */
    public static final String FILES_PATH = HOSTED ? LINUX_HOST ? "/root/gamedata/" : "C:/gamedata/" : "data/";

    /**
     * We will always be on debug mode if the server isn't hosted
     */
    public static boolean DEBUG = true;

    /**
     * SQL will always be enabled if the server is hosted, otherwise we can toggle it on or off
     */
    public static boolean SQL_ENABLED = false;

    /**
     * If double votes are enabled
     */
    public static boolean DOUBLE_VOTES_ENABLED;

    /**
     * The times for double experieince
     */
    public static final long[] DOUBLE_EXPERIENCE_TIMES = new long[2];

    /**
     * The build of the client
     */
    public static final int CLIENT_BUILD = 667;

    /**
     * The custom build of the client, shows what version we're on
     */
    public static final int CUSTOM_CLIENT_BUILD = 3;

    /**
     * The port the server listens on
     */
    public static final int GAME_PORT_ID = 43594;

    /**
     * The port for the js5 server
     */
    public static final int JS5_PORT_ID = 43595;

    /**
     * The maximum size of the avail that can be received
     */
    public static final int RECEIVE_DATA_LIMIT = 7500;

    /**
     * The largest size of packet we can send
     */
    public static final int PACKET_SIZE_LIMIT = 7500;

    /**
     * If gambling is enabled in the server (dicing/flower poker)
     */
    public static final boolean GAMBLING_ENABLED = false;

    /**
     * Player settings
     */
    public static final int START_PLAYER_HITPOINTS = 100;

    public static final WorldTile START_PLAYER_LOCATION = new WorldTile(3086, 3502, 0);

    public static final String START_CONTROLER = "StartTutorial";

    public static final WorldTile RESPAWN_PLAYER_LOCATION = new WorldTile(3102, 3492, 0);

    /**
     * The chance multiplier for drops
     */
    public static final double DROP_CHANCE_MULTIPLIER = 1.0;

    /**
     * The experience rate multiplier for combat skills
     */
    public static final int COMBAT_EXP_RATE = 400, IRONMAN_COMBAT_EXP_RATE = 10;

    /**
     * The experience rate multiplier for non-combat skills
     */
    public static final int SKIll_EXP_RATE = 10, IRONMAN_SKILL_EXP_RATE = 5;

    /**
     * The ironman exp rate for prayer
     */
    public static final double IRONMAN_PRAYER_EXP_RATE = 5;

    /**
     * The regular exp rate for prayer
     */
    public static final double PRAYER_EXP_RATE = 10;

    /**
     * The bonus multiplier for daily skills
     */
    public static final double DAILY_EXP_BONUS = 1.15;

    /**
     * Music & Emote settings
     */
    public static final int AIR_GUITAR_MUSICS_COUNT = 50;

    /**
     * Memory settings
     */
    public static final int PLAYERS_LIMIT = 2048;

    public static final int NPCS_LIMIT = Short.MAX_VALUE;

    public static final int LOCAL_NPCS_LIMIT = 250;

    public static final int MIN_FREE_MEM_ALLOWED = 30000000; // 30mb

    /**
     * Game constants
     */
    public static final int[] MAP_SIZES = {104, 120, 136, 168};

    public static final int[] GRAB_SERVER_KEYS = {1393, 78700, 44880, 39771, 363186, 44375, 0, 16140, 6028, 263849, 778481, 209109, 372444, 444388, 892700, 20013, 24356, 16747, 1244, 1, 13271, 1321, 119, 853169, 1748783, 3963, 3323};

    public static final int READ_BUFFER_SIZE = 20 * 1024; // 20kb

    public static final int WRITE_BUFFER_SIZE = 20 * 1024; // 20kb

    public static final String SQL_FILE_PATH = "./data/sql.txt";

    private GameConstants() {

    }

}
