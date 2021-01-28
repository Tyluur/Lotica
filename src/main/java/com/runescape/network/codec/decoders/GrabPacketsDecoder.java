package com.runescape.network.codec.decoders;

import com.runescape.cache.Cache;
import com.runescape.network.Session;
import com.runescape.network.codec.Decoder;
import com.runescape.network.stream.InputStream;
import com.runescape.workers.game.ConcurrentThreadFactory;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Jonathan
 */
public final class GrabPacketsDecoder extends Decoder {

	/**
	 * The executor service used for the worker
	 */
	private static final ExecutorService worker = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new ConcurrentThreadFactory("JS5-Worker"));

	private LinkedList<String> requests = new LinkedList<>();

	@Override
	public int decode(InputStream stream) {
		while (stream.getRemaining() > 0 && session.getChannel().isOpen()) {
			int packetId = stream.readUnsignedByte();
			decodeRequestCacheContainer(stream, packetId);
		}
		requests.clear();
		return stream.getOffset();
	}

	private void decodeRequestCacheContainer(InputStream stream, final int priority) {
		final int indexId = stream.readUnsignedByte();
		final int archiveId = stream.readInt();// on original 667
		if (archiveId < 0) { return; }
		if (indexId != 255) {
			if (Cache.STORE.getIndexes().length <= indexId || Cache.STORE.getIndexes()[indexId] == null || !Cache.STORE.getIndexes()[indexId].archiveExists(archiveId)) {
				return;
			}
		} else if (archiveId != 255) {
			if (Cache.STORE.getIndexes().length <= archiveId || Cache.STORE.getIndexes()[archiveId] == null) { return; }
		}
		switch (priority) {
			case 0:
				requests.add(indexId + "," + archiveId);
				break;
			case 1:
				worker.submit((Runnable) () -> session.getGrabPackets().sendCacheArchive(indexId, archiveId, true));
				break;
			case 2:
			case 3:
				requests.clear();
				break;
			default:
				break;
		}
		while (requests.size() > 0) {
			String[] request = requests.removeFirst().split(",");
			session.getGrabPackets().sendCacheArchive(Integer.parseInt(request[0]), Integer.parseInt(request[1]), false);
		}
	}

	public GrabPacketsDecoder(Session connection) {
		super(connection);
	}

}
