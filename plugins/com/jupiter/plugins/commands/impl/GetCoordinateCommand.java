package com.jupiter.plugins.commands.impl;

import com.jupiter.game.player.Player;
import com.jupiter.game.player.Rights;
import com.jupiter.plugins.listener.Command;
import com.jupiter.plugins.wrapper.CommandSignature;

@CommandSignature(alias = {"coordinate"}, rights = {Rights.ADMINISTRATOR}, syntax = "Get current player tile")
public final class GetCoordinateCommand implements Command {

    @Override
    public void execute(Player player, String[] cmd, String command) throws Exception {
        player.getPackets().sendGameMessage("X: " + Integer.toString(player.getX()) + " Y: " + Integer.toString(player.getY())
                + " Plane: " + Integer.toString(player.getHeight()));
    }
}
