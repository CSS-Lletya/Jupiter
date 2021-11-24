package com.jupiter.plugins.rsinterface.impl;

import com.jupiter.game.player.Player;
import com.jupiter.plugins.commands.impl.PlayerDesign;
import com.jupiter.plugins.listener.RSInterface;
import com.jupiter.plugins.wrapper.RSInterfaceSignature;

@RSInterfaceSignature(interfaceId = {1028})
public class WardrobeInterfacePlugin implements RSInterface {
    @Override
    public void execute(Player player, int interfaceId, int componentId, int packetId, byte slotId, int slotId2) throws Exception {
        if(interfaceId == 1028)
            PlayerDesign.handle(player, componentId, slotId);
    }
}