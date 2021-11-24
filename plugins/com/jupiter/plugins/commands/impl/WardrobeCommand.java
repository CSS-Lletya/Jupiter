package com.jupiter.plugins.commands.impl;

import com.jupiter.game.player.Player;
import com.jupiter.game.player.Rights;
import com.jupiter.plugins.listener.Command;
import com.jupiter.plugins.wrapper.CommandSignature;

@CommandSignature(alias = {"wardrobe"}, rights = {Rights.ADMINISTRATOR}, syntax = "Shows player appearence options")
public final class WardrobeCommand implements Command {

    @Override
    public void execute(Player player, String[] cmd, String command) throws Exception {
        if (cmd.length > 1) {
            player.getPackets().sendPanelBoxMessage("Use: ;;wardrobe");
            return;
        }
        PlayerDesign.open(player);

    }
}
