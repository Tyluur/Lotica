package com.runescape.network.codec;

import com.runescape.network.Session;
import com.runescape.network.stream.InputStream;

public abstract class Decoder {

	protected Session session;

	public Decoder(Session session) {
		this.session = session;
	}

	public abstract int decode(InputStream stream);

}
