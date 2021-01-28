package com.runescape.game.interaction.dialogues.impl;

import com.runescape.game.interaction.dialogues.Dialogue;

/**
 * @author Tyluur<itstyluur@gmail.com>
 * @since May 7, 2015
 */
public abstract class ConfirmationDialogue extends Dialogue {
	
	@Override
	public void start() {
		this.header = (String) parameters[0];
		this.itemDescription = (String) parameters[1];
		this.bottom = (String) parameters[2];
		this.itemId = (Integer) parameters[3];
		player.getInterfaceManager().sendChatBoxInterface(94);
		player.getPackets().sendIComponentText(94, 8, itemDescription);
		player.getPackets().sendIComponentText(94, 2, header);
		player.getPackets().sendIComponentText(94, 7, bottom);
		player.getPackets().sendItemOnIComponent(94, 9, itemId, 1);
	}
	

	@Override
	public void run(int interfaceId, int option) {
		end();
		if (interfaceId == 94 && option == 3) {
			onConfirm();
			player.getPackets().sendSound(4500, 0, 1);
		}
	}

	@Override
	public void finish() {

	}
	
	/**
	 * What to do when we confirm the dialogue 
	 */
	public abstract void onConfirm();
	
	private String header, itemDescription, bottom;
	private int itemId;

}
