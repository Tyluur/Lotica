package com.runescape.network.stream.incoming.impl;

import com.runescape.cache.loaders.IComponentDefinitions;
import com.runescape.game.world.entity.player.Player;
import com.runescape.network.stream.InputStream;
import com.runescape.network.stream.incoming.IncomingStreamDecoder;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 3/27/2016
 */
public class FrameInteractionStreamDecoder extends IncomingStreamDecoder {

    private final static int WINDOW_SWITCH_PACKET = 93;

    private final static int KEY_TYPED_PACKET = 68;

    @Override
    public int[] getKeys() {
        return new int[]{84, 29, 68, 75, 93};
    }

    @Override
    public void decode(Player player, InputStream stream, int packetId, int length) {
        // click = 84, move mouse = 29, type = 68
        if (packetId == WINDOW_SWITCH_PACKET) {
            int active = stream.readByte();
            player.getInterfaceManager().setClientActive(active == 1);
        } else if (packetId == KEY_TYPED_PACKET) {
            int keyCode = stream.readByte();
            //				int unknown = packet.readByte();
            //				int unknown2 = packet.readUnsignedShort();

            switch (keyCode) {
                case 16: { // 1
                    IComponentDefinitions optionComponent = player.getInterfaceManager().getDialogueInterfaceDefinitions("option1");
                    if (optionComponent == null) {
                        break;
                    }
                    player.getDialogueManager().continueDialogue(player.getInterfaceManager().getChatboxInterface(), optionComponent.getWidgetId());
                    break;
                }
                case 17: { // 2
                    IComponentDefinitions optionComponent = player.getInterfaceManager().getDialogueInterfaceDefinitions("option2");
                    if (optionComponent == null) {
                        break;
                    }
                    player.getDialogueManager().continueDialogue(player.getInterfaceManager().getChatboxInterface(), optionComponent.getWidgetId());
                    break;
                }
                case 18: {// 3
                    IComponentDefinitions optionComponent = player.getInterfaceManager().getDialogueInterfaceDefinitions("option3");
                    if (optionComponent == null) {
                        break;
                    }
                    player.getDialogueManager().continueDialogue(player.getInterfaceManager().getChatboxInterface(), optionComponent.getWidgetId());
                    break;
                }
                case 19: {// 4
                    IComponentDefinitions optionComponent = player.getInterfaceManager().getDialogueInterfaceDefinitions("option4");
                    if (optionComponent == null) {
                        break;
                    }
                    player.getDialogueManager().continueDialogue(player.getInterfaceManager().getChatboxInterface(), optionComponent.getWidgetId());
                    break;
                }
                case 20: {// 5
                    IComponentDefinitions optionComponent = player.getInterfaceManager().getDialogueInterfaceDefinitions("option5");
                    if (optionComponent == null) {
                        break;
                    }
                    player.getDialogueManager().continueDialogue(player.getInterfaceManager().getChatboxInterface(), optionComponent.getWidgetId());
                    break;
                }
                case 13: // esc
                    player.closeInterfaces();
                    break;
                case 83: // space
                    IComponentDefinitions continueComponent = player.getInterfaceManager().getDialogueInterfaceDefinitions("Click here to continue");
                    if (continueComponent == null) {
                        break;
                    }
                    player.getDialogueManager().continueDialogue(player.getInterfaceManager().getChatboxInterface(), continueComponent.getWidgetId());
                    break;
            }
        }
    }
}
