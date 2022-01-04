package com.jupiter.network.decoders;

import com.jupiter.cache.io.InputStream;
import com.jupiter.network.Session;

public abstract class Decoder {

	protected Session session;

	public Decoder(Session session) {
		this.session = session;
	}

	public abstract void decode(InputStream stream);

}
