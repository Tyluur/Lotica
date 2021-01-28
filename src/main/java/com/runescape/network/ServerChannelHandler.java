/*
package com.runescape.network;

import com.runescape.game.GameConstants;
import com.runescape.network.codec.decoders.WorldPacketsDecoder;
import com.runescape.network.stream.InputStream;
import com.runescape.workers.game.log.GameLog;
import com.runescape.workers.game.log.GameLogProcessor;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;

public class ServerChannelHandler extends SimpleChannelHandler {

	protected static ChannelGroup channels;

	private static ServerBootstrap bootstrap;

	private static ExecutorService workerExecutor, bossExecutor;

	public static void init() {
		new ServerChannelHandler();
	}

	public static int getConnectedChannelsSize() {
		return channels == null ? 0 : channels.size();
	}

*/
/*
	 * throws exeption so if cant handle channel server closes
	 *//*


	public ServerChannelHandler() {
		channels = new DefaultChannelGroup();
		workerExecutor = new OrderedMemoryAwareThreadPoolExecutor(2, 0, 0);
		bossExecutor = new OrderedMemoryAwareThreadPoolExecutor(1, 0, 0);
		bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(bossExecutor, workerExecutor, 2));
		bootstrap.getPipeline().addLast("handler", this);

		bootstrap.setOption("reuseAddress", true);
		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.sendBufferSize", GameConstants.WRITE_BUFFER_SIZE);
		bootstrap.setOption("child.receiveBufferSize", GameConstants.READ_BUFFER_SIZE);

		bootstrap.bind(new InetSocketAddress(GameConstants.GAME_PORT_ID));
	}

	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
		channels.add(e.getChannel());
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) {
		channels.remove(e.getChannel());
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
		ctx.setAttachment(new Session(e.getChannel()));
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
		Object sessionObject = ctx.getAttachment();
		if (sessionObject != null && sessionObject instanceof Session) {
			Session session = (Session) sessionObject;
			if (session.getDecoder() == null) { return; }
			if (session.getDecoder() instanceof WorldPacketsDecoder) {
				session.getWorldPackets().getPlayer().finish();
			}
		}
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		if (!(e.getMessage() instanceof ChannelBuffer)) {
			return;
		}
		Object sessionObject = ctx.getAttachment();
		if (sessionObject != null && sessionObject instanceof Session) {
			Session session = (Session) sessionObject;
			if (session.getDecoder() == null) {
				return;
			}
			ChannelBuffer buf = (ChannelBuffer) e.getMessage();
			buf.markReaderIndex();
			int avail = buf.readableBytes();
			if (avail < 1 || avail > GameConstants.RECEIVE_DATA_LIMIT) {
				return;
			}
			byte[] buffer = new byte[avail];
			buf.readBytes(buffer);
			try {
				session.getDecoder().decode(new InputStream(buffer));
			} catch (Throwable er) {
				er.printStackTrace();
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		Object sessionObject = ctx.getAttachment();
		if (sessionObject != null && sessionObject instanceof Session) {
			Session session = (Session) sessionObject;
			if (session.getDecoder() == null) { return; }
			if (session.getDecoder() instanceof WorldPacketsDecoder) {
				Throwable cause = e.getCause();
				GameLogProcessor.submitLog(new GameLog(session.getWorldPackets().getPlayer().getUsername(), "Cause of exception:\n\t" + cause.getMessage()));
				System.err.println("Exception caught!");
				cause.printStackTrace();
			}
		}
	}

	public static void shutdown() {
		channels.close().awaitUninterruptibly();
		bootstrap.releaseExternalResources();
	}

}
*/
