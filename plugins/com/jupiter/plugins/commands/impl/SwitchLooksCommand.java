package com.jupiter.plugins.commands.impl;

import com.jupiter.game.player.Player;
import com.jupiter.game.player.Rights;
import com.jupiter.plugins.listener.Command;
import com.jupiter.plugins.wrapper.CommandSignature;

@CommandSignature(alias = {"switchlooks"}, rights = {Rights.ADMINISTRATOR}, syntax = "switches item looks")
public final class SwitchLooksCommand implements Command {

    @Override
    public void execute(Player player, String[] cmd, String command) throws Exception {
        player.switchItemsLook();
    }
}
