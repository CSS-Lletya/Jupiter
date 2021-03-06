package com.jupiter.plugins.commands.impl;

import com.jupiter.game.dialogue.Conversation;
import com.jupiter.game.dialogue.Mood;
import com.jupiter.game.player.Player;
import com.jupiter.game.player.Rights;
import com.jupiter.plugins.commands.Command;
import com.jupiter.plugins.commands.CommandSignature;

/**
 * This is just a dummy command to re-use for whatever testing needed.
 * 
 * @author Dennis
 *
 */
@CommandSignature(alias = { "test" }, rights = { Rights.PLAYER }, syntax = "Test a Command")
public class TestCommand implements Command {

	@Override
	public void execute(Player player, String[] cmd, String command) throws Exception {
		player.startConversation(new Conversation(player)
		.addPlayer(Mood.HAPPY_TALKING, "HEY")
		.addItem(1050, "HOHO")
		);
		
	}
}