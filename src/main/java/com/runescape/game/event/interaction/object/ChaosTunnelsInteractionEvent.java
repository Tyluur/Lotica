package com.runescape.game.event.interaction.object;

import com.runescape.game.event.interaction.type.ObjectInteractionEvent;
import com.runescape.game.world.WorldObject;
import com.runescape.game.world.WorldTile;
import com.runescape.game.world.entity.player.Player;
import com.runescape.utility.Utils;
import com.runescape.utility.world.ClickOption;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since 7/6/2015
 */
public class ChaosTunnelsInteractionEvent extends ObjectInteractionEvent {
	
	@Override
	public int[] getKeys() {
		return new int[] { 77745, 28779, 65203, 28782 };
	}

	@Override
	public boolean handleObjectInteraction(Player player, WorldObject object, ClickOption option) {
		int x = object.getX();
		int y = object.getY();
		switch (object.getId()) {
			case 77745:
			case 28779:
				if (x == 3254 && y == 5451) {
					movePlayer(player, 3250, 5448, 0);
				}
				if (x == 3250 && y == 5448) {
					movePlayer(player, 3254, 5451, 0);
				}
				if (x == 3241 && y == 5445) {
					movePlayer(player, 3233, 5445, 0);
				}
				if (x == 3233 && y == 5445) {
					movePlayer(player, 3241, 5445, 0);
				}
				if (x == 3259 && y == 5446) {
					movePlayer(player, 3265, 5491, 0);
				}
				if (x == 3265 && y == 5491) {
					movePlayer(player, 3259, 5446, 0);
				}
				if (x == 3260 && y == 5491) {
					movePlayer(player, 3266, 5446, 0);
				}
				if (x == 3266 && y == 5446) {
					movePlayer(player, 3260, 5491, 0);
				}
				if (x == 3241 && y == 5469) {
					movePlayer(player, 3233, 5470, 0);
				}
				if (x == 3233 && y == 5470) {
					movePlayer(player, 3241, 5469, 0);
				}
				if (x == 3235 && y == 5457) {
					movePlayer(player, 3229, 5454, 0);
				}
				if (x == 3229 && y == 5454) {
					movePlayer(player, 3235, 5457, 0);
				}
				if (x == 3280 && y == 5460) {
					movePlayer(player, 3273, 5460, 0);
				}
				if (x == 3273 && y == 5460) {
					movePlayer(player, 3280, 5460, 0);
				}
				if (x == 3283 && y == 5448) {
					movePlayer(player, 3287, 5448, 0);
				}
				if (x == 3287 && y == 5448) {
					movePlayer(player, 3283, 5448, 0);
				}
				if (x == 3244 && y == 5495) {
					movePlayer(player, 3239, 5498, 0);
				}
				if (x == 3239 && y == 5498) {
					movePlayer(player, 3244, 5495, 0);
				}
				if (x == 3232 && y == 5501) {
					movePlayer(player, 3238, 5507, 0);
				}
				if (x == 3238 && y == 5507) {
					movePlayer(player, 3232, 5501, 0);
				}
				if (x == 3218 && y == 5497) {
					movePlayer(player, 3222, 5488, 0);
				}
				if (x == 3222 && y == 5488) {
					movePlayer(player, 3218, 5497, 0);
				}
				if (x == 3218 && y == 5478) {
					movePlayer(player, 3215, 5475, 0);
				}
				if (x == 3215 && y == 5475) {
					movePlayer(player, 3218, 5478, 0);
				}
				if (x == 3224 && y == 5479) {
					movePlayer(player, 3222, 5474, 0);
				}
				if (x == 3222 && y == 5474) {
					movePlayer(player, 3224, 5479, 0);
				}
				if (x == 3208 && y == 5471) {
					movePlayer(player, 3210, 5477, 0);
				}
				if (x == 3210 && y == 5477) {
					movePlayer(player, 3208, 5471, 0);
				}
				if (x == 3214 && y == 5456) {
					movePlayer(player, 3212, 5452, 0);
				}
				if (x == 3212 && y == 5452) {
					movePlayer(player, 3214, 5456, 0);
				}
				if (x == 3204 && y == 5445) {
					movePlayer(player, 3197, 5448, 0);
				}
				if (x == 3197 && y == 5448) {
					movePlayer(player, 3204, 5445, 0);
				}
				if (x == 3189 && y == 5444) {
					movePlayer(player, 3187, 5460, 0);
				}
				if (x == 3187 && y == 5460) {
					movePlayer(player, 3189, 5444, 0);
				}
				if (x == 3192 && y == 5472) {
					movePlayer(player, 3186, 5472, 0);
				}
				if (x == 3186 && y == 5472) {
					movePlayer(player, 3192, 5472, 0);
				}
				if (x == 3185 && y == 5478) {
					movePlayer(player, 3191, 5482, 0);
				}
				if (x == 3191 && y == 5482) {
					movePlayer(player, 3185, 5478, 0);
				}
				if (x == 3171 && y == 5473) {
					movePlayer(player, 3167, 5471, 0);
				}
				if (x == 3167 && y == 5471) {
					movePlayer(player, 3171, 5473, 0);
				}
				if (x == 3171 && y == 5478) {
					movePlayer(player, 3167, 5478, 0);
				}
				if (x == 3167 && y == 5478) {
					movePlayer(player, 3171, 5478, 0);
				}
				if (x == 3168 && y == 5456) {
					movePlayer(player, 3178, 5460, 0);
				}
				if (x == 3178 && y == 5460) {
					movePlayer(player, 3168, 5456, 0);
				}
				if (x == 3191 && y == 5495) {
					movePlayer(player, 3194, 5490, 0);
				}
				if (x == 3194 && y == 5490) {
					movePlayer(player, 3191, 5495, 0);
				}
				if (x == 3141 && y == 5480) {
					movePlayer(player, 3142, 5489, 0);
				}
				if (x == 3142 && y == 5489) {
					movePlayer(player, 3141, 5480, 0);
				}
				if (x == 3142 && y == 5462) {
					movePlayer(player, 3154, 5462, 0);
				}
				if (x == 3154 && y == 5462) {
					movePlayer(player, 3142, 5462, 0);
				}
				if (x == 3143 && y == 5443) {
					movePlayer(player, 3155, 5449, 0);
				}
				if (x == 3155 && y == 5449) {
					movePlayer(player, 3143, 5443, 0);
				}
				if (x == 3307 && y == 5496) {
					movePlayer(player, 3317, 5496, 0);
				}
				if (x == 3317 && y == 5496) {
					movePlayer(player, 3307, 5496, 0);
				}
				if (x == 3318 && y == 5481) {
					movePlayer(player, 3322, 5480, 0);
				}
				if (x == 3322 && y == 5480) {
					movePlayer(player, 3318, 5481, 0);
				}
				if (x == 3299 && y == 5484) {
					movePlayer(player, 3303, 5477, 0);
				}
				if (x == 3303 && y == 5477) {
					movePlayer(player, 3299, 5484, 0);
				}
				if (x == 3286 && y == 5470) {
					movePlayer(player, 3285, 5474, 0);
				}
				if (x == 3285 && y == 5474) {
					movePlayer(player, 3286, 5470, 0);
				}
				if (x == 3290 && y == 5463) {
					movePlayer(player, 3302, 5469, 0);
				}
				if (x == 3302 && y == 5469) {
					movePlayer(player, 3290, 5463, 0);
				}
				if (x == 3296 && y == 5455) {
					movePlayer(player, 3299, 5450, 0);
				}
				if (x == 3299 && y == 5450) {
					movePlayer(player, 3296, 5455, 0);
				}
				if (x == 3280 && y == 5501) {
					movePlayer(player, 3285, 5508, 0);
				}
				if (x == 3285 && y == 5508) {
					movePlayer(player, 3280, 5501, 0);
				}
				if (x == 3300 && y == 5514) {
					movePlayer(player, 3297, 5510, 0);
				}
				if (x == 3297 && y == 5510) {
					movePlayer(player, 3300, 5514, 0);
				}
				if (x == 3289 && y == 5533) {
					movePlayer(player, 3288, 5536, 0);
				}
				if (x == 3288 && y == 5536) {
					movePlayer(player, 3289, 5533, 0);
				}
				if (x == 3285 && y == 5527) {
					movePlayer(player, 3282, 5531, 0);
				}
				if (x == 3282 && y == 5531) {
					movePlayer(player, 3285, 5527, 0);
				}
				if (x == 3325 && y == 5518) {
					movePlayer(player, 3323, 5531, 0);
				}
				if (x == 3323 && y == 5531) {
					movePlayer(player, 3325, 5518, 0);
				}
				if (x == 3299 && y == 5533) {
					movePlayer(player, 3297, 5536, 0);
				}
				if (x == 3297 && y == 5538) {
					movePlayer(player, 3299, 5533, 0);
				}
				if (x == 3321 && y == 5554) {
					movePlayer(player, 3315, 5552, 0);
				}
				if (x == 3315 && y == 5552) {
					movePlayer(player, 3321, 5554, 0);
				}
				if (x == 3291 && y == 5555) {
					movePlayer(player, 3285, 5556, 0);
				}
				if (x == 3285 && y == 5556) {
					movePlayer(player, 3291, 5555, 0);
				}
				if (x == 3266 && y == 5552) {
					movePlayer(player, 3262, 5552, 0);
				}
				if (x == 3262 && y == 5552) {
					movePlayer(player, 3266, 5552, 0);
				}
				if (x == 3256 && y == 5561) {
					movePlayer(player, 3253, 5561, 0);
				}
				if (x == 3253 && y == 5561) {
					movePlayer(player, 3256, 5561, 0);
				}
				if (x == 3249 && y == 5546) {
					movePlayer(player, 3252, 5543, 0);
				}
				if (x == 3252 && y == 5543) {
					movePlayer(player, 3249, 5546, 0);
				}
				if (x == 3261 && y == 5536) {
					movePlayer(player, 3268, 5534, 0);
				}
				if (x == 3268 && y == 5534) {
					movePlayer(player, 3261, 5536, 0);
				}
				if (x == 3243 && y == 5526) {
					movePlayer(player, 3241, 5529, 0);
				}
				if (x == 3241 && y == 5529) {
					movePlayer(player, 3243, 5526, 0);
				}
				if (x == 3230 && y == 5547) {
					movePlayer(player, 3226, 5553, 0);
				}
				if (x == 3226 && y == 5553) {
					movePlayer(player, 3230, 5547, 0);
				}
				if (x == 3206 && y == 5553) {
					movePlayer(player, 3204, 5546, 0);
				}
				if (x == 3204 && y == 5546) {
					movePlayer(player, 3206, 5553, 0);
				}
				if (x == 3211 && y == 5533) {
					movePlayer(player, 3214, 5533, 0);
				}
				if (x == 3214 && y == 5533) {
					movePlayer(player, 3211, 5533, 0);
				}
				if (x == 3208 && y == 5527) {
					movePlayer(player, 3211, 5523, 0);
				}
				if (x == 3211 && y == 5523) {
					movePlayer(player, 3208, 5527, 0);
				}
				if (x == 3201 && y == 5531) {
					movePlayer(player, 3197, 5529, 0);
				}
				if (x == 3197 && y == 5529) {
					movePlayer(player, 3201, 5531, 0);
				}
				if (x == 3202 && y == 5515) {
					movePlayer(player, 3196, 5512, 0);
				}
				if (x == 3196 && y == 5512) {
					movePlayer(player, 3202, 5515, 0);
				}
				if (x == 3190 && y == 5515) {
					movePlayer(player, 3190, 5519, 0);
				}
				if (x == 3190 && y == 5519) {
					movePlayer(player, 3190, 5515, 0);
				}
				if (x == 3185 && y == 5518) {
					movePlayer(player, 3181, 5517, 0);
				}
				if (x == 3181 && y == 5517) {
					movePlayer(player, 3185, 5518, 0);
				}
				if (x == 3187 && y == 5531) {
					movePlayer(player, 3182, 5530, 0);
				}
				if (x == 3182 && y == 5530) {
					movePlayer(player, 3187, 5531, 0);
				}
				if (x == 3169 && y == 5510) {
					movePlayer(player, 3159, 5501, 0);
				}
				if (x == 3159 && y == 5501) {
					movePlayer(player, 3169, 5510, 0);
				}
				if (x == 3165 && y == 5515) {
					movePlayer(player, 3173, 5530, 0);
				}
				if (x == 3173 && y == 5530) {
					movePlayer(player, 3165, 5515, 0);
				}
				if (x == 3156 && y == 5523) {
					movePlayer(player, 3152, 5520, 0);
				}
				if (x == 3152 && y == 5520) {
					movePlayer(player, 3156, 5523, 0);
				}
				if (x == 3148 && y == 5533) {
					movePlayer(player, 3153, 5537, 0);
				}
				if (x == 3153 && y == 5537) {
					movePlayer(player, 3148, 5533, 0);
				}
				if (x == 3143 && y == 5535) {
					movePlayer(player, 3147, 5541, 0);
				}
				if (x == 3147 && y == 5541) {
					movePlayer(player, 3143, 5535, 0);
				}
				if (x == 3168 && y == 5541) {
					movePlayer(player, 3171, 5542, 0);
				}
				if (x == 3171 && y == 5542) {
					movePlayer(player, 3168, 5541, 0);
				}
				if (x == 3190 && y == 5549) {
					movePlayer(player, 3190, 5554, 0);
				}
				if (x == 3190 && y == 5554) {
					movePlayer(player, 3190, 5549, 0);
				}
				if (x == 3180 && y == 5557) {
					movePlayer(player, 3174, 5558, 0);
				}
				if (x == 3174 && y == 5558) {
					movePlayer(player, 3180, 5557, 0);
				}
				if (x == 3162 && y == 5557) {
					movePlayer(player, 3158, 5561, 0);
				}
				if (x == 3158 && y == 5561) {
					movePlayer(player, 3162, 5557, 0);
				}
				if (x == 3166 && y == 5553) {
					movePlayer(player, 3162, 5545, 0);
				}
				if (x == 3162 && y == 5545) {
					movePlayer(player, 3166, 5553, 0);
				}
				if (x == 3142 && y == 5545) {
					movePlayer(player, 3115, 5528, 0);
				}
				if (x == 3115 && y == 5528) {
					movePlayer(player, 3142, 5545, 0);
				}
				break;
			case 65203:
				long inCombatTimer = Utils.currentTimeMillis();
				if (player.getAttackedByDelay() + 10000 > inCombatTimer) {
					gameMessage(player, "You cannot enter the rift while you're under attack.");
					return true;
				}
				movePlayer(player, 3183, 5470, 0);
				if (x == 3165 && y == 3561) {
					movePlayer(player, 3292, 5479, 0);
				}
				if (x == 3165 && y == 3618) {
					movePlayer(player, 3291, 5538, 0);
				}
				if (x == 3119 && y == 3571) {
					movePlayer(player, 3248, 5490, 0);
				}
				if (x == 3107 && y == 3639) {
					movePlayer(player, 3234, 5559, 0);
				}
				break;
			case 28782:
				if (x == 3183 && y == 5470) {
					movePlayer(player, 3059, 3549, 0);
				}
				if (x == 3248 && y == 5490) {
					movePlayer(player, 3120, 3571, 0);
				}
				if (x == 3292 && y == 5479) {
					movePlayer(player, 3166, 3561, 0);
				}
				if (x == 3291 && y == 5538) {
					movePlayer(player, 3166, 3618, 0);
				}
				if (x == 3234 && y == 5559) {
					movePlayer(player, 3107, 3640, 0);
				}
				break;
		}
		return true;
	}

	/**
	 * Moving a player to the specified coordinates
	 *
	 * @param player
	 * 		The player
	 * @param x
	 * 		The x coord
	 * @param y
	 * 		The y coord
	 * @param z
	 * 		The plane
	 */
	private void movePlayer(Player player, final int x, final int y, final int z) {
		player.setNextWorldTile(new WorldTile(x, y, z));
	}

	/**
	 * Sending the player a message
	 *
	 * @param player
	 * 		The player
	 * @param message
	 * 		The message
	 */
	private void gameMessage(Player player, final String message) {
		player.getPackets().sendGameMessage(message);
	}
}
