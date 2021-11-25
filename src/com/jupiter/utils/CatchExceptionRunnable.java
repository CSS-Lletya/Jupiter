package com.jupiter.utils;

import lombok.Data;
import lombok.SneakyThrows;

@Data
public class CatchExceptionRunnable implements Runnable {

	private final Runnable runnable;
	
	@Override
	@SneakyThrows(Throwable.class)
	public void run() {
		runnable.run();
	}
}