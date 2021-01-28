package com.runescape.game.world.entity.player;

import com.runescape.game.content.unique.quickchat.QuickChatType;

public class QuickChatMessage extends PublicChatMessage {

	private QuickChatType type;
	
	private int[] params;

	public QuickChatMessage(QuickChatType type, int[] params) {
		super("", 0x8000);
		this.type = type;
		this.params = params;
	}

	public QuickChatType getType() {
		return type;
	}
	
	public void setType (QuickChatType type) {
		this.type = type;
	}
	
	public int[] getParams () {
		return params;
	}
	
	public void setParams (int[] params) {
		this.params = params;
	}
}