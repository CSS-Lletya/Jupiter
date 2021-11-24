package com.jupiter.plugins.rsinterface.impl;

import com.jupiter.game.player.Player;
import com.jupiter.plugins.listener.RSInterface;
import com.jupiter.plugins.wrapper.RSInterfaceSignature;

@RSInterfaceSignature(interfaceId = {1092})
public class LodestoneNetworkInterfacePlugin implements RSInterface {
    @Override
    public void execute(Player player, int interfaceId, int componentId, int packetId, byte slotId, int slotId2) throws Exception {
        player.getLodeStones().handleButtons(componentId);
    }
}
