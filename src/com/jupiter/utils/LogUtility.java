package com.jupiter.utils;

import org.tinylog.Logger;

import com.jupiter.Settings;

/**
 * Represents a feature-rich Logging utility backed by TinyLog
 * <https://tinylog.org/v2/> <https://tinylog.org/v2/logging/>
 * 
 * @author Dennis
 *
 */
public final class LogUtility {

	/**
	 * Represents the type of the Log we're sending
	 * 
	 * @author Dennis
	 *
	 */
	public enum Type {
		INFO, TRACE, DEBUG, WARN, ERROR
	}

	/**
	 * Submits a specified log to the console.
	 * 
	 * @param logType
	 * @param message
	 */
	public static void log(Type logType, String className, String message) {
		if (!Settings.DEBUG)
			return;
		message = className + " - " + message;
		switch (logType) {
		case DEBUG:
			Logger.debug(message);
			break;
		case ERROR:
			Logger.error(message);
			break;
		case INFO:
			Logger.info(message);
			break;
		case TRACE:
			Logger.trace(message);
			break;
		case WARN:
			Logger.warn(message);
			break;
		}
	}
}