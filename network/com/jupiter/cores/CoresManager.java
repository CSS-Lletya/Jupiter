package com.jupiter.cores;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.jupiter.Settings;
import com.jupiter.network.decoders.WorldPacketsDecoder;
import com.jupiter.utility.CatchExceptionRunnable;

public final class CoresManager {

	public static volatile boolean SHUTDOWN;
	
	private static ScheduledExecutorService worldExecutor;
	private static ScheduledExecutorService worldExemptExecutor;
	private static List<Future<?>> PENDING_FUTURES = new ArrayList<Future<?>>();
	
	public static void startThreads() {
		worldExemptExecutor = Executors.newSingleThreadScheduledExecutor(new SlowThreadFactory());
		worldExecutor = Executors.newSingleThreadScheduledExecutor(new WorldThreadFactory());
		fastExecutor = new Timer("Fast Executor");
		WorldPacketsDecoder.loadPacketSizes();
	}
	
	public static void execute(Runnable command) {
		synchronized(PENDING_FUTURES) {
			Future<?> future = worldExemptExecutor.submit(new CatchExceptionRunnable(command));
			PENDING_FUTURES.add(future);
			List<Future<?>> finished = new ArrayList<>();
			PENDING_FUTURES.parallelStream().filter(f -> f.isDone()).forEach(pending -> finished.add(pending));
			finished.forEach(done -> PENDING_FUTURES.remove(done));
		}
	}
	
	public static void schedule(Runnable command, int delay) {
		worldExemptExecutor.schedule(new CatchExceptionRunnable(command), delay*Settings.WORLD_CYCLE_MS, TimeUnit.MILLISECONDS);
	}
	
	public static void schedule(Runnable command, int startDelay, int delay) {
		worldExemptExecutor.scheduleWithFixedDelay(new CatchExceptionRunnable(command), startDelay*Settings.WORLD_CYCLE_MS, delay*Settings.WORLD_CYCLE_MS, TimeUnit.MILLISECONDS);
	}
	
	public static boolean pendingTasks() {
		synchronized(PENDING_FUTURES) {
			List<Future<?>> finished = new ArrayList<>();
			for (Future<?> f : PENDING_FUTURES) {
				if (f.isDone())
					finished.add(f);
			}
			for (Future<?> f : finished)
				PENDING_FUTURES.remove(f);
			
			boolean allDone = true;
			for(Future<?> future : PENDING_FUTURES) {
				if (!future.isDone())
					allDone = false;
			}
			return !allDone;
		}
	}
	
	
	public Timer fastExecutor() {
		return fastExecutor;
	}

	public static void shutdown() {
		worldExemptExecutor.shutdown();
		fastExecutor.cancel();
		SHUTDOWN = true;
	}

	public static ScheduledExecutorService getWorldExecutor() {
		return worldExecutor;
	}
	
	public static Timer fastExecutor;
}
