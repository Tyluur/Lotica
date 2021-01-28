package com.runescape.network.codec.encoders;

import com.runescape.cache.Cache;
import com.runescape.game.GameConstants;
import com.runescape.network.Session;
import com.runescape.network.codec.Encoder;
import com.runescape.network.stream.OutputStream;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

public final class GrabPacketsEncoder extends Encoder {

	private static byte[] UKEYS_FILE;

	private int encryptionValue;

	public GrabPacketsEncoder(Session connection) {
		super(connection);
	}

	public final void sendOutdatedClientPacket() {
		OutputStream stream = new OutputStream(1);
		stream.writeByte(6);
		ChannelFuture future = session.writeWithFuture(stream);
		if (future != null) { future.addListener(ChannelFutureListener.CLOSE); } else { session.getChannel().close(); }
	}

	public final void sendStartUpPacket() {
		OutputStream stream = new OutputStream(1 + GameConstants.GRAB_SERVER_KEYS.length * 4);
		stream.writeByte(0);
		for (int key : GameConstants.GRAB_SERVER_KEYS) { stream.writeInt(key); }
		session.write(stream);
	}

	public final void sendCacheArchive(int indexId, int containerId, boolean priority) {
		if (indexId == 255 && containerId == 255) { session.write(getUkeysFile()); } else {
			session.write(getArchivePacketData(indexId, containerId, priority));
		}
	}

	public static OutputStream getUkeysFile() {
		if (UKEYS_FILE == null) { UKEYS_FILE = Cache.generateUkeysFile(); }
		return getContainerPacketData(255, 255, UKEYS_FILE);
	}

	public final ChannelBuffer getArchivePacketData(int indexId, int archiveId, boolean priority) {
		byte[] archive = indexId == 255 ? Cache.STORE.getIndex255().getArchiveData(archiveId) : Cache.STORE.getIndexes()[indexId].getMainFile().getArchiveData(archiveId);
		if (archive == null || !priority) {
			return null;
		}
		int compression = archive[0] & 0xff;
		int length = ((archive[1] & 0xff) << 24) + ((archive[2] & 0xff) << 16) + ((archive[3] & 0xff) << 8) + (archive[4] & 0xff);
		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
		buffer.writeByte(indexId);
		buffer.writeInt(archiveId);
		buffer.writeByte(compression);
		buffer.writeInt(length);
		int realLength = compression != 0 ? length + 4 : length;
		for (int index = 5; index < realLength + 5; index++) {
			if (buffer.writerIndex() % 512 == 0) {
				buffer.writeByte(255);
			}
			buffer.writeByte(archive[index]);
		}
		int v = encryptionValue;
		if (v != 0) {
			for (int i = 0; i < buffer.arrayOffset(); i++) {
				buffer.setByte(i, buffer.getByte(i) ^ v);
			}
		}
		return buffer;
	}

	public static OutputStream getContainerPacketData(int indexFileId, int containerId, byte[] archive) {
		OutputStream stream = new OutputStream(archive.length + 4);
		stream.writeByte(indexFileId);
		stream.writeInt(containerId);
		stream.writeByte(0);
		stream.writeInt(archive.length);
		int offset = 10;
		for (byte element : archive) {
			if (offset == 512) {
				stream.writeByte(255);
				offset = 1;
			}
			stream.writeByte(element);
			offset++;
		}
		return stream;
	}

	public void setEncryptionValue(int encryptionValue) {
		this.encryptionValue = encryptionValue;
	}

}
