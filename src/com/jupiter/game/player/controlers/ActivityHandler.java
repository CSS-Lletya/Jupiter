package com.jupiter.game.player.controlers;

import java.util.HashMap;

import com.jupiter.utils.Utils;

import io.vavr.collection.Array;
import io.vavr.control.Try;
import lombok.SneakyThrows;

public class ActivityHandler {

	private static final HashMap<Object, Class<Activity>> handledActivities = new HashMap<Object, Class<Activity>>();

	@SuppressWarnings("unchecked")
	public static final void init() {
		Try.run(() -> {
			Class<Activity>[] regular = Utils.getClasses("com.rs.game.player.activites.impl");
			Array.of(regular).filter(c -> c.isAnonymousClass()).forEach(activity -> handledActivities.put(activity.getSimpleName(), activity));
		}).onFailure(fail -> fail.printStackTrace());
	}

	@SneakyThrows(Throwable.class)
	public static final Activity getAcitivity(Object key) {
		if (key instanceof Activity)
			return (Activity) key;
		Class<Activity> classC = handledActivities.get(key);
		if (classC == null)
			return null;
		return classC.newInstance();
	}
}