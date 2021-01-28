package com.runescape.network;

import com.runescape.game.GameConstants;
import com.runescape.network.codec.decoders.WorldPacketsDecoder;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;

import java.net.InetSocketAddress;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

/**
 * @author Tyluur <itstyluur@gmail.com>
 * @since 5/4/2016
 */
public class LoticaChannelHandler extends AbstractChannelHandler {

	protected static ChannelGroup channels;

	private static ServerBootstrap bootstrap;

	private static ExecutorService workerExecutor, bossExecutor;

	private static final CopyOnWriteArrayList<Session> connectedSessions = new CopyOnWriteArrayList<>();

	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
		channels.add(e.getChannel());
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
		Session session = new Session(e.getChannel());
		ctx.setAttachment(session);
		connectedSessions.add(session);
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) {
		Object sessionObject = ctx.getAttachment();
		if (sessionObject != null && sessionObject instanceof Session) {
			Session session = (Session) sessionObject;
			connectedSessions.remove(session);
			if (session.getDecoder() == null) { return; }
			if (session.getDecoder() instanceof WorldPacketsDecoder) {
				session.getWorldPackets().getPlayer().finish();
			}
		}
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) {
		channels.remove(e.getChannel());
	}

	public static void init() throws InterruptedException {
		channels = new DefaultChannelGroup();
		workerExecutor = new OrderedMemoryAwareThreadPoolExecutor(2, 0, 0);
		bossExecutor = new OrderedMemoryAwareThreadPoolExecutor(1, 0, 0);
		bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(bossExecutor, workerExecutor, 2));
		bootstrap.getPipeline().addLast("handler", new LoticaChannelHandler());

		bootstrap.setOption("reuseAddress", true);
		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.sendBufferSize", GameConstants.WRITE_BUFFER_SIZE);
		bootstrap.setOption("child.receiveBufferSize", GameConstants.READ_BUFFER_SIZE);

		bootstrap.bind(new InetSocketAddress(GameConstants.GAME_PORT_ID));
	}

	public static void processSessionQueue() {
		connectedSessions.forEach(Session::processOutgoingQueue);
	}

	public static void shutdown() {
		channels.close().awaitUninterruptibly();
		bootstrap.releaseExternalResources();
	}

}
