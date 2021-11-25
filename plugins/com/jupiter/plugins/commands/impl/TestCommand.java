package com.jupiter.plugins.commands.impl;

import com.jupiter.game.dialogue.Conversation;
import com.jupiter.game.dialogue.Mood;
import com.jupiter.game.dialogue.Options;
import com.jupiter.game.player.Player;
import com.jupiter.game.player.Rights;
import com.jupiter.plugins.listener.Command;
import com.jupiter.plugins.wrapper.CommandSignature;

/**
 * This is just a dummy command to re-use for whatever testing needed.
 * 
 * @author Dennis
 *
 */
@CommandSignature(alias = { "test" }, rights = { Rights.PLAYER }, syntax = "Test a Command")
public final class TestCommand implements Command {

	@Override
	public void execute(Player player, String[] cmd, String command) throws Exception {
		player.startConversation(new Conversation(player)
				.addSimple("Hey there")
				.addPlayer(Mood.AMAZED, "HAAAY")
				.addNPC(3, Mood.SAD, "i'm sad..take my ags")
				.addItem(11694, "tada!")
				.addOptions("options time!", new Options() {

					@Override
					public void create() {
						option("option 1", () -> System.out.println("option 1"));
						option("option 2", () -> System.out.println("option 2"));
					}
				}));
	}
}