package com.jupiter.game.player.activity;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.ImmutableSet;
import com.jupiter.game.player.Player;
import com.jupiter.game.player.activity.impl.TestActivity;
import com.jupiter.game.player.activity.impl.WildernessActivity;

/**
 * TODO: Item on object, process npc death
 * @author Dennis
 * @author lare96 <http://github.com/lare96>
 * @author <a href="http://www.rune-server.org/members/stand+up/">Stand Up</a>
 */
public class ActivityHandler {

	private static ImmutableSet<Activity> ACTIVITIES = ImmutableSet.of(new TestActivity(), new WildernessActivity());
	
	/**
	 * The method that executes {@code action} for {@code player}.
	 * @param player the player to execute the action for.
	 * @param action the backed activity action to execute.
	 */
	public static void executeVoid(Player player, Consumer<Activity> action) {
		if (ACTIVITIES.contains(player.getCurrentActivity().get()))
			action.accept(player.getCurrentActivity().get());
	}
	
	/**
	 * The method that executes {@code function} for {@code player} that returns
	 * a result.
	 * @param player the player to execute the function for.
	 * @param defaultValue the default value to return if the player isn't in a activity.
	 * @param function the function to execute that returns a result.
	 */
	public static boolean execute(Player player, Function<Activity, Boolean> function) {
		return ACTIVITIES.contains(player.getCurrentActivity().get()) ? function.apply(player.getCurrentActivity().get()) : false;
	}
	
	/**
	 * Sets and Initiates the Activity for the target player
	 * @param player
	 * @param activity
	 */
	public static void startActivity(Player player, Activity activity) {
		player.setCurrentActivity(Optional.of(activity));
		player.getCurrentActivity().get().start(player);
	}
}