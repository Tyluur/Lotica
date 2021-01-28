package com.runescape.network.codec.encoders;

import com.runescape.game.world.entity.player.Player;
import com.runescape.network.Session;
import com.runescape.network.codec.Encoder;
import com.runescape.network.stream.OutputStream;
import com.runescape.workers.game.login.LoginResponses;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;

public final class LoginPacketsEncoder extends Encoder {

	public LoginPacketsEncoder(Session connection) {
		super(connection);
	}

	public final void sendStartUpPacket() {
		OutputStream stream = new OutputStream(1);
		stream.writeByte(0);
		session.write(stream);
	}

	public final void sendClientPacket(Object param) {
		if (!(param instanceof Integer) && !(param instanceof LoginResponses)) {
			throw new IllegalStateException("Parameter can only be an integer or a LoginResponses value.");
		}
		int opcode = param instanceof Integer ? (int) param : ((LoginResponses) param).getOpcode();
		OutputStream stream = new OutputStream(1);
		stream.writeByte(opcode);
		ChannelFuture future = session.writeWithFuture(stream);
		if (future != null) {
			future.addListener(ChannelFutureListener.CLOSE);
		} else {
			session.getChannel().close();
		}
	}

	public final void sendLoginDetails(Player player) {
		OutputStream stream = new OutputStream();
		stream.writePacketVarByte(player, 2);
		stream.writeByte(player.getPrimaryRight().getClientRight());
		stream.writeByte(0);
		stream.writeByte(0);
		stream.writeByte(0);
		stream.writeByte(1);
		stream.writeByte(0);
		stream.writeShort(player.getIndex());
		stream.writeByte(1);
		stream.write24BitInteger(0);
		stream.writeByte(1);
		stream.writeString(player.getDisplayName());
		stream.endPacketVarByte();
		session.write(stream);
	}
}
