package com.runescape.network;

import com.runescape.game.world.entity.player.Player;
import com.runescape.network.codec.Decoder;
import com.runescape.network.codec.Encoder;
import com.runescape.network.codec.decoders.ClientPacketsDecoder;
import com.runescape.network.codec.decoders.GrabPacketsDecoder;
import com.runescape.network.codec.decoders.LoginPacketsDecoder;
import com.runescape.network.codec.decoders.WorldPacketsDecoder;
import com.runescape.network.codec.encoders.GrabPacketsEncoder;
import com.runescape.network.codec.encoders.LoginPacketsEncoder;
import com.runescape.network.codec.encoders.WorldPacketsEncoder;
import com.runescape.network.stream.OutputStream;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class Session {

	protected Channel channel;

	protected Decoder decoder;

	protected Encoder encoder;

	protected boolean masterSession;

	private Queue<OutputStream> outgoingQueue = new LinkedBlockingQueue<>();

	@Override
	public String toString() {
		Player player = null;
		if (decoder instanceof WorldPacketsDecoder) {
			if (getWorldPackets().getPlayer() != null) {
				player = getWorldPackets().getPlayer();
			}
		}
		if (player != null) {
			return player.toString() + "[" + getIP() + "]";
		} else {
			return getIP();
		}
	}

	public WorldPacketsEncoder getWorldPackets() {
		return (WorldPacketsEncoder) encoder;
	}

	public String getIP() {
		return channel == null ? "" : channel.getRemoteAddress().toString().split(":")[0].replace("/", "");
	}

	public Session(Channel channel) {
		this.channel = channel;
		setDecoder(0);
	}

	public void setDecoder(int stage, Object attachment) {
		switch (stage) {
			case 0:
				decoder = new ClientPacketsDecoder(this);
				break;
			case 1:
				decoder = new GrabPacketsDecoder(this);
				break;
			case 2:
				decoder = new LoginPacketsDecoder(this);
				break;
			case 3:
				decoder = new WorldPacketsDecoder(this, (Player) attachment);
				break;
			case -1:
			default:
				decoder = null;
				break;
		}
	}

	public void processOutgoingQueue() {
		try {
			OutputStream o;
			if (!outgoingQueue.isEmpty()) {
				while ((o = outgoingQueue.poll()) != null) {
					channel.write(ChannelBuffers.copiedBuffer(o.getBuffer(), 0, o.getOffset()));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public final ChannelFuture writeWithFuture(OutputStream outStream) {
		if (outStream == null || !channel.isOpen()) { return null; }
		return channel.write(ChannelBuffers.copiedBuffer(outStream.getBuffer(), 0, outStream.getOffset()));
	}

	public final void write(OutputStream outStream) {
		if (outStream == null || !channel.isOpen()) { return; }
		outgoingQueue.add(outStream);
	}

	public final ChannelFuture write(ChannelBuffer outStream) {
		if (outStream == null || !channel.isOpen()) { return null; }
		return channel.write(outStream);
	}

	public final Decoder getDecoder() {
		return decoder;
	}

	public final void setDecoder(int stage) {
		setDecoder(stage, null);
	}

	public final void setEncoder(int stage) {
		setEncoder(stage, null);
	}

	public final void setEncoder(int stage, Object attachment) {
		switch (stage) {
			case 0:
				encoder = new GrabPacketsEncoder(this);
				break;
			case 1:
				encoder = new LoginPacketsEncoder(this);
				break;
			case 2:
				encoder = new WorldPacketsEncoder(this, (Player) attachment);
				break;
			case -1:
			default:
				encoder = null;
				break;
		}
	}

	public LoginPacketsEncoder getLoginPackets() {
		return (LoginPacketsEncoder) encoder;
	}

	public GrabPacketsEncoder getGrabPackets() {
		return (GrabPacketsEncoder) encoder;
	}

    public Channel getChannel() {
        return this.channel;
    }

    public boolean isMasterSession() {
        return this.masterSession;
    }

    public void setMasterSession(boolean masterSession) {
        this.masterSession = masterSession;
    }
}
