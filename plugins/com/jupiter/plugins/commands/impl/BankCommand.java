package com.jupiter.plugins.commands.impl;

import com.jupiter.game.player.Player;
import com.jupiter.game.player.Rights;
import com.jupiter.plugins.commands.Command;
import com.jupiter.plugins.commands.CommandSignature;

@CommandSignature(alias = {"Bank"}, rights = {Rights.ADMINISTRATOR}, syntax = "Opens Bank")
public final class BankCommand implements Command {
    @Override
    public void execute(Player player, String[] cmd, String command) {
        player.getBank().openBank();
    }
}