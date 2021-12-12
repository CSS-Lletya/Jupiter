package com.jupiter.utility;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

public class KeyGeneration {

	private final static AtomicInteger nextKey = new AtomicInteger(400);
	private static final int key = nextKey.getAndDecrement();
	
	public static final int generateKey() {
		IntStream.range(1, 99).forEach(key -> nextKey.set(-1));
		return key;
	}
}