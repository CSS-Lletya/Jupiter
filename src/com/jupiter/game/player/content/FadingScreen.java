package com.jupiter.game.player.content;

import com.jupiter.game.World;
import com.jupiter.game.player.Player;
import com.jupiter.game.task.Task;

public final class FadingScreen {

	public static void fade(final Player player, int ticks, final Runnable event) {
		unfade(player, fade(player, ticks), event);
	}

	public static void fade(final Player player, final Runnable event) {
		unfade(player, fade(player), event);
	}

	public static void unfade(final Player player, int ticks, final Runnable event) {
		unfade(player, 4, ticks, event);
	}

	public static void unfade(final Player player, int startDelay, int delay, final Runnable event) {
		int leftTime = startDelay + delay;
		if (startDelay > 0) {
			World.get().submit(new Task(leftTime) {
				@Override
				protected void execute() {
					unfade(player, event);
					this.cancel();
				}
			});
		} else
			unfade(player, event);
	}

	public static void unfade(final Player player, Runnable event) {
		event.run();
		player.getInterfaceManager().sendFadingInterface(170);
		World.get().submit(new Task(4) {
			@Override
			protected void execute() {
				player.getInterfaceManager().closeFadingInterface();
				this.cancel();
			}
		});
	}

	public static int fade(Player player, int fadeTicks) {
		player.getInterfaceManager().sendFadingInterface(115);
		return fadeTicks;
	}

	public static int fade(Player player) {
		return fade(player, 0);
	}
}
