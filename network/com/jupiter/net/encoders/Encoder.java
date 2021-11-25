package com.jupiter.net.encoders;

import com.jupiter.net.Session;

public abstract class Encoder {

	protected Session session;

	public Encoder(Session session) {
		this.session = session;
	}

}
