package com.runescape.cache.loaders;

import com.runescape.cache.Cache;
import com.runescape.network.stream.InputStream;

import java.util.concurrent.ConcurrentHashMap;

public final class ClientVarpBitDefinitions {

	private static final ConcurrentHashMap<Integer, ClientVarpBitDefinitions> VARPBIT_DEFS = new ConcurrentHashMap<>();

	public int varpId;

	public int baseVar;

	public int endBit;

	public int startBit;

	private ClientVarpBitDefinitions() {

	}

	public static ClientVarpBitDefinitions getClientVarpBitDefinitions(int id) {
		ClientVarpBitDefinitions script = VARPBIT_DEFS.get(id);
		if (script != null) {
			return script;
		}
		byte[] data = Cache.STORE.getIndexes()[22].getFile(id >>> 1416501898, id & 0x3ff);
		script = new ClientVarpBitDefinitions();
		script.varpId = id;
		if (data != null) {
			script.readValueLoop(new InputStream(data));
		}
		VARPBIT_DEFS.put(id, script);
		return script;

	}

	private void readValueLoop(InputStream stream) {
		for (; ; ) {
			int opcode = stream.readUnsignedByte();
			if (opcode == 0) {
				break;
			}
			readValues(stream, opcode);
		}
	}

	private void readValues(InputStream stream, int opcode) {
		if (opcode == 1) {
			baseVar = stream.readUnsignedShort();
			startBit = stream.readUnsignedByte();
			endBit = stream.readUnsignedByte();
		}
	}
}