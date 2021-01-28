package com.runescape.game.content.unique.quickchat;

import com.runescape.cache.Cache;
import com.runescape.network.stream.InputStream;
import com.runescape.network.stream.OutputStream;

import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class QuickChatType {
	
	private static final ConcurrentHashMap<Integer, QuickChatType> quickChatTypes = new ConcurrentHashMap<>();
	
	public int[] responses;
	
	public boolean aBool11355 = true;

	ChatPhraseEncodingType[] variablePartTypes;

	int[][] variablePartConfigKeys;

	String[] text;

	private int myid;
	QuickChatType(int id) {
		this.myid = id;
	}

	public static QuickChatType getQuickChatType(int id) {
		QuickChatType qcType = quickChatTypes.get(id);
		if (qcType == null) {
			qcType = new QuickChatType(id);
			byte[] data = Cache.STORE.getIndexes()[id >= 0x8000 ? 25 : 24].getFile(1, id & 0x7fff);
			if (data != null) {
				qcType.decode(new InputStream(data));
			}
			if (id >= 0x8000) {
				qcType.setGlobal();
			}
			quickChatTypes.put(id, qcType);
		}
		return qcType;
	}

	void decode(InputStream buffer) {
		for (;;) {
			int code = buffer.readUnsignedByte();
			if (0 == code) {
				break;
			}
			decode(buffer, code);
		}
	}

	void setGlobal() {
		if (responses != null) {
			for (int pos = 0; pos < responses.length; pos++) {
				responses[pos] |= 0x8000;
			}
		}
	}

	void decode(InputStream buffer, int code) {
		if (1 == code) {
			text = Pattern.compile("<", Pattern.LITERAL).split(buffer.readString());
		} else if (code == 2) {
			int i_2_ = buffer.readUnsignedByte();
			responses = new int[i_2_];
			for (int i_3_ = 0; i_3_ < i_2_; i_3_++) {
				responses[i_3_] = buffer.readUnsignedShort();
			}
		} else if (3 == code) {
			int typeCount = buffer.readUnsignedByte();
			variablePartTypes = new ChatPhraseEncodingType[typeCount];
			variablePartConfigKeys = new int[typeCount][];
			for (int slot = 0; slot < typeCount; slot++) {
				int i_6_ = buffer.readUnsignedShort();
				ChatPhraseEncodingType type = ChatPhraseEncodingType.getByID(i_6_);
				if (null != type) {
					variablePartTypes[slot] = type;
					variablePartConfigKeys[slot] = new int[type.configKeyCount];
					for (int pos = 0; pos < type.configKeyCount; pos++) {
						variablePartConfigKeys[slot][pos] = buffer.readUnsignedShort();
					}
				}
			}
		} else if (4 == code) {
			aBool11355 = false;
		}
	}

	public int getParamCount() {
		if (variablePartTypes == null) {
			return 0;
		}
		return variablePartTypes.length;
	}
	
	public ChatPhraseEncodingType getParamType (int pos) {
		return variablePartTypes[pos];
	}

	public int getParamKey(int slot, int i_12_) {
		if (null == variablePartTypes || slot < 0
				    || slot > variablePartTypes.length) {
			return -1;
		}
		if (variablePartConfigKeys[slot] == null || i_12_ < 0
				    || i_12_ > variablePartConfigKeys[slot].length) {
			return -1;
		}
		return variablePartConfigKeys[slot][i_12_];
	}
	
	public int getId () {
		return myid;
	}
	
	public int[] unpack(InputStream buffer) {
		int[] params = new int[0];
		if (variablePartTypes != null) {
			params = new int[variablePartTypes.length];
			for (int pos = 0; pos < variablePartTypes.length; pos++) {
				int size = variablePartTypes[pos].clientTransmitSize;
				if (size > 0) {
					params[pos] = (int) buffer.readQuickchatParam(size);
				}
			}
		}
		return params;
	}
	
	public void pack(OutputStream buffer, int[] params) {
		if (variablePartTypes != null) {
			for (int pos = 0; pos < variablePartTypes.length && pos < params.length; pos++) {
				int size = variablePartTypes[pos].serverTransmitSize;
				if (size > 0) {
					buffer.writeQuickchatParam((long) params[pos], size);
				}
			}
		}
	}

	public enum ChatPhraseEncodingType {
		LISTDIALOG(0, 2, 2, 1),
		OBJDIALOG(1, 2, 2, 0),
		COUNTDIALOG(2, 4, 4, 0),
		STAT_BASE(4, 1, 1, 1),
		ENUM_STRING(6, 0, 4, 2),
		ENUM_STRING_CLAN(7, 0, 1, 1),
		TOSTRING_VARP(8, 0, 4, 1),
		TOSTRING_VARBIT(9, 0, 4, 1),
		OBJTRADEDIALOG(10, 2, 2, 0),
		ENUM_STRING_STATBASE(11, 0, 1, 2),
		ACC_GETCOUNT_WORLD(12, 0, 1, 0),
		ACC_GETMEANCOMBATLEVEL(13, 0, 1, 0),
		TOSTRING_SHARED(14, 0, 4, 1),
		ACTIVECOMBATLEVEL(15, 0, 1, 0);


		public int serialID;
		public int clientTransmitSize;
		public int serverTransmitSize;
		public int configKeyCount;

		ChatPhraseEncodingType(int id, int clientSize, int serverSize, int keyCount) {
			serialID = id;
			clientTransmitSize = clientSize;
			serverTransmitSize = serverSize;
			configKeyCount = keyCount;
		}

		public static ChatPhraseEncodingType getByID(int id) {
			ChatPhraseEncodingType[] values = values();
			for (ChatPhraseEncodingType value : values) {
				if (id == value.serialID) {
					return value;
				}
			}
			return null;
		}
	}
}