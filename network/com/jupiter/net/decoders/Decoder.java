package com.jupiter.net.decoders;

import com.jupiter.cache.io.InputStream;
import com.jupiter.net.Session;

public abstract class Decoder {

	protected Session session;

	public Decoder(Session session) {
		this.session = session;
	}

	public abstract void decode(InputStream stream);

}
