package com.jupiter.game.player.content;

import com.jupiter.game.player.Player;

public final class FadingScreen {

	public static void fadeWithDelay(final Player player, int ticks, final Runnable event) {
		unfade(player, fade(player, ticks), event);
	}

	public static void fade(final Player player, final Runnable event) {
		unfade(player, fade(player), event);
	}

	public static void unfade(final Player player, int ticks, final Runnable event) {
		unfade(player, 4, ticks, event);
	}

	public static void unfade(final Player player, int startDelay, int delay, final Runnable event) {
		if (startDelay > 0)
			player.task((startDelay + delay), p -> unfade(player, event));
		else
			unfade(player, event);
	}

	public static void unfade(final Player player, Runnable event) {
		event.run();
		player.getInterfaceManager().sendFadingInterface(170);
		player.task(4, p -> p.getInterfaceManager().closeFadingInterface());
	}

	public static int fade(Player player, int fadeTicks) {
		player.getInterfaceManager().sendFadingInterface(115);
		return fadeTicks;
	}

	public static int fade(Player player) {
		return fade(player, 0);
	}
}