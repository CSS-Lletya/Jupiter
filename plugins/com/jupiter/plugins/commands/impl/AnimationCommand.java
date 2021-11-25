package com.jupiter.plugins.commands.impl;

import com.jupiter.game.player.Player;
import com.jupiter.game.player.Rights;
import com.jupiter.net.encoders.other.Animation;
import com.jupiter.plugins.commands.Command;
import com.jupiter.plugins.commands.CommandSignature;

@CommandSignature(alias = {"anim", "an", "animation"}, rights = {Rights.ADMINISTRATOR}, syntax = "Perform an animation")
public final class AnimationCommand implements Command {
	
	@Override
	public void execute(Player player, String[] cmd, String command) throws Exception {
		player.setNextAnimation(new Animation(Integer.valueOf(cmd[1])));
	}
}