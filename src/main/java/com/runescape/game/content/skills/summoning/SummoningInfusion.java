package com.runescape.game.content.skills.summoning;

import com.runescape.cache.loaders.ClientScriptMap;
import com.runescape.cache.loaders.ItemDefinitions;
import com.runescape.game.GameConstants;
import com.runescape.game.world.entity.masks.Animation;
import com.runescape.game.world.entity.masks.Graphics;
import com.runescape.game.world.entity.player.Player;
import com.runescape.game.world.entity.player.Skills;
import com.runescape.game.world.entity.player.achievements.AchievementHandler;
import com.runescape.game.world.entity.player.achievements.easy.Novice_Summoner;
import com.runescape.game.world.item.Item;

import java.util.List;
import java.util.ListIterator;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since Apr 20, 2015
 */
public class SummoningInfusion {

    public static final int POUCHES_INTERFACE = 672, SCROLLS_INTERFACE = 666;
    private static final Animation SCROLL_INFUSIN_ANIMATION = new Animation(723);
    private static final Animation POUCH_INFUSION_ANIMATION = new Animation(725);
    private static final Graphics POUCH_INFUSION_GRAPHICS = new Graphics(1207);

    public static int getScrollId(int id) {
        return ClientScriptMap.getMap(1283).getIntValue(id);
    }

    public static int getRequiredLevel(int id) {
        return ClientScriptMap.getMap(1185).getIntValue(id);
    }

    public static int getNPCId(int id) {
        return ClientScriptMap.getMap(1320).getIntValue(id);
    }

    public static String getRequirementsMessage(int id) {
        return ClientScriptMap.getMap(1186).getStringValue(id);
    }

    public static void openInfusionInterface(Player player) {
        player.getInterfaceManager().sendInterface(POUCHES_INTERFACE);
        player.getPackets().sendPouchInfusionOptionsScript(POUCHES_INTERFACE, 16, 78, 8, 10, "Infuse<col=FF9040>", "Infuse-5<col=FF9040>", "Infuse-10<col=FF9040>", "Infuse-X<col=FF9040>", "Infuse-All<col=FF9040>", "List<col=FF9040>");
        player.getPackets().sendIComponentSettings(POUCHES_INTERFACE, 16, 0, 462, 190);
        player.getAttributes().put("infusing_scroll", false);
    }

    public static void openScrollInfusionInterface(Player player) {
        player.getInterfaceManager().sendInterface(SCROLLS_INTERFACE);
        player.getPackets().sendScrollInfusionOptionsScript(SCROLLS_INTERFACE, 16, 78, 8, 10, "Transform<col=FF9040>", "Transform-5<col=FF9040>", "Transform-10<col=FF9040>", "Transform-All<col=FF9040>", "Transform-X<col=FF9040>");
        player.getPackets().sendIComponentSettings(SCROLLS_INTERFACE, 16, 0, 462, 126);
        player.getAttributes().put("infusing_scroll", true);
    }

    public static void handlePouchInfusion(Player player, int slotId, int creationCount) {
        if (creationCount <= 0) {
            return;
        }
        int slotValue = (slotId - 2) / 5;
        Pouches pouch = Pouches.values()[slotValue];
        if (pouch == null) {
            return;
        }
        boolean infusingScroll = (boolean) player.getAttributes().remove("infusing_scroll"), hasRequirements = false;
        ItemDefinitions def = ItemDefinitions.forId(pouch.getRealPouchId());
        List<Item> itemReq = def.getCreateItemRequirements(infusingScroll);
        ListIterator<Item> it$ = itemReq.listIterator();
        while (it$.hasNext()) {
            Item item = it$.next();
            if (item.getId() == 1) {
                it$.remove();
                continue;
            }
            if ((item.getId() == 12155 && item.getAmount() == 3226) || (item.getId() == 12160 && item.getAmount() == 165)) {
                item.setAmount(1);
            }
        }
        int level = getRequiredLevel(pouch.getRealPouchId());
	    for (int i = 0; i < creationCount; i++) {
		    if (!player.getInventory().containsItems(itemReq)) {
			    if (GameConstants.DEBUG) {
				    System.out.println(itemReq + " needed.");
			    }
			    sendItemList(player, infusingScroll, creationCount, slotId);
			    break;
		    } else if (player.getSkills().getLevelForXp(Skills.SUMMONING) < level) {
			    player.getPackets().sendGameMessage("You need a summoning level of " + level + " to create this pouch.");
			    break;
		    }
		    hasRequirements = true;
		    player.getInventory().removeItems(itemReq);
		    player.getInventory().addItem(new Item(infusingScroll ? getScrollId(pouch.getRealPouchId()) : pouch.getRealPouchId(), infusingScroll ? 10 : 1));
		    player.getSkills().addXp(Skills.SUMMONING, infusingScroll ? pouch.getMinorExperience() : pouch.getExperience());
		    if (pouch == Pouches.SPIRIT_WOLF) {
			    AchievementHandler.incrementProgress(player, Novice_Summoner.class, 1);
		    }
	    }
	    if (!hasRequirements) {
            player.getAttributes().put("infusing_scroll", infusingScroll);
            return;
        }
        player.closeInterfaces();
        player.setNextAnimation(infusingScroll ? SCROLL_INFUSIN_ANIMATION : POUCH_INFUSION_ANIMATION);
        player.setNextGraphics(POUCH_INFUSION_GRAPHICS);
    }

    public static void switchInfusionOption(Player player) {
        boolean infusingScroll = (boolean) player.getAttributes().get("infusing_scroll");
        if (infusingScroll) {
            openInfusionInterface(player);
        } else {
            openScrollInfusionInterface(player);
        }
    }

    public static void sendItemList(Player player, boolean infusingScroll, int count, int slotId) {
        int slotValue = (slotId - 2) / 5;
        Pouches pouch = Pouches.values()[slotValue];
        if (pouch == null) {
            return;
        }
        if (infusingScroll) {
            player.getPackets().sendGameMessage("This scroll requires 1 " + ItemDefinitions.forId(pouch.getRealPouchId()).name.toLowerCase() + ".");
        } else {
            player.getPackets().sendGameMessage(getRequirementsMessage(pouch.getRealPouchId()));
        }
    }
}
