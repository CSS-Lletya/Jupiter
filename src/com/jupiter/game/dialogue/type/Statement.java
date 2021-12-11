package com.jupiter.game.dialogue.type;

import com.jupiter.game.player.Player;

public interface Statement {
	
	void send(Player player);

	int getOptionId(int componentId);
}