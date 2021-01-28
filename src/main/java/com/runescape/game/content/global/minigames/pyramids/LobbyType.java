package com.runescape.game.content.global.minigames.pyramids;

import com.runescape.game.world.WorldTile;

public enum LobbyType {

	SINGLE {
		@Override
		public WorldTile[] getInformationTiles() {
			return new WorldTile[] { new WorldTile(1900, 3162, 0), new WorldTile(1899, 3162, 0) };
		}
	},

	TEAM {
		@Override
		public WorldTile[] getInformationTiles() {
			return new WorldTile[] { new WorldTile(1879, 3162, 0), new WorldTile(1880, 3162, 0) };
		}
	};

	public abstract WorldTile[] getInformationTiles();

	/**
	 * The index in the {@link #getInformationTiles()} array that stores the
	 * inside lobby coordinates
	 */
	public static final int INSIDE_LOBBY_IDX = 0;

	/**
	 * The index in the {@link #getInformationTiles()} array that stores the
	 * outside lobby coordinates
	 */
	public static final int OUTSIDE_LOBBY_IDX = 1;

}
