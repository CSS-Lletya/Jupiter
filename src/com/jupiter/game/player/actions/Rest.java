package com.jupiter.game.player.actions;

import com.jupiter.game.player.Player;
import com.jupiter.game.player.content.Emotes;
import com.jupiter.network.encoders.other.Animation;
import com.jupiter.utility.RandomUtility;
import com.jupiter.utility.Utility;

public class Rest extends Action {

	private static int[][] REST_DEFS = { { 5713, 1549, 5748 }, { 11786, 1550, 11788 }, { 5713, 1551, 2921 } // TODO First emote

	};

	private int index;

	@Override
	public boolean start(Player player) {
		if (!process(player))
			return false;
		index = RandomUtility.random(REST_DEFS.length);
		player.setResting(true);
		player.setNextAnimation(new Animation(REST_DEFS[index][0]));
		player.getAppearance().setRenderEmote((short) REST_DEFS[index][1]);
		return true;
	}

	@Override
	public boolean process(Player player) {
		if (player.isPoisoned()) {
			player.getPackets().sendGameMessage("You can't rest while you're poisoned.");
			return false;
		}
		if (player.getAttackedByDelay() + 10000 > Utility.currentTimeMillis()) {
			player.getPackets().sendGameMessage("You can't rest until 10 seconds after the end of combat.");
			return false;
		}
		return true;
	}

	@Override
	public int processWithDelay(Player player) {
		return 0;
	}

	@Override
	public void stop(Player player) {
		player.setResting(false);
		player.setNextAnimation(new Animation(REST_DEFS[index][2]));
		Emotes.setNextEmoteEnd(player);
		player.getAppearance().setRenderEmote((short) -1);
	}

}
