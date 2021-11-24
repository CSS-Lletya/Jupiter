package com.jupiter.plugins.commands.impl;

import com.jupiter.game.ForceTalk;
import com.jupiter.game.player.Player;
import com.jupiter.game.player.Rights;
import com.jupiter.game.task.LinkedTaskSequence;
import com.jupiter.plugins.listener.Command;
import com.jupiter.plugins.wrapper.CommandSignature;

@CommandSignature(alias = {"task"}, rights = {Rights.PLAYER}, syntax = "Task testing.")
public final class TestTaskCommand implements Command {
	
	@Override
	public void execute(Player player, String[] cmd, String command) throws Exception {
		LinkedTaskSequence seq = new LinkedTaskSequence();
		
		seq.connect(1, () -> player.setNextForceTalk(new ForceTalk("Taste vengeance!")));
		
		seq.connect(2, () -> player.setNextForceTalk(new ForceTalk("OR NOT")));
		
		seq.connect(3, () -> player.setNextForceTalk(new ForceTalk("OR MAYBE....")));
		
		seq.start();
	}
}