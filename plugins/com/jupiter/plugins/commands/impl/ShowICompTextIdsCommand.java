package com.jupiter.plugins.commands.impl;

import com.jupiter.cache.utility.CacheUtility;
import com.jupiter.game.player.Player;
import com.jupiter.game.player.Rights;
import com.jupiter.plugins.commands.Command;
import com.jupiter.plugins.commands.CommandSignature;

@CommandSignature(alias = {"icomptextids"}, rights = {Rights.ADMINISTRATOR}, syntax = "Shows all text components in an interface")
public final class ShowICompTextIdsCommand implements Command {

    @Override
    public void execute(Player player, String[] cmd, String command) throws Exception {
        if (cmd.length < 2) {
            player.getPackets().sendPanelBoxMessage("Use: ;;icomptextids [interfaceId]");
            return;
        }

        try {
            int interId = Integer.valueOf(cmd[1]);
            for (int componentId = 0; componentId < CacheUtility.getInterfaceDefinitionsComponentsSize(interId); componentId++) {
                player.getPackets().sendIComponentText(interId,	componentId, "id: " + componentId);
            }
        } catch (NumberFormatException e) {
            player.getPackets().sendPanelBoxMessage("Use: ;;icomptextids [interfaceId]");
        }
    }
}
