package com.jupiter.network.encoders;

import com.jupiter.network.Session;

public abstract class Encoder {

	protected Session session;

	public Encoder(Session session) {
		this.session = session;
	}

}
