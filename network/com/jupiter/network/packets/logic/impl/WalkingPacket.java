package com.jupiter.network.packets.logic.impl;

import com.jupiter.cache.io.InputStream;
import com.jupiter.game.map.WorldTile;
import com.jupiter.game.player.Player;
import com.jupiter.game.route.RouteFinder;
import com.jupiter.game.route.strategy.FixedTileStrategy;
import com.jupiter.network.packets.logic.LogicPacket;
import com.jupiter.network.packets.logic.LogicPacketSignature;
import com.jupiter.utility.Utility;

@LogicPacketSignature(packetId = 33, packetSize = 5, description = "Basic Walking packet")
public class WalkingPacket implements LogicPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		if (!player.isStarted() || !player.isClientLoadedMapRegion() || player.isDead())
			return;
		long currentTime = Utility.currentTimeMillis();
		if (player.getMovement().getLockDelay() > currentTime)
			return;
		if (player.getFreezeDelay() >= currentTime) {
			player.getPackets().sendGameMessage("A magical force prevents you from moving.");
			return;
		}
		@SuppressWarnings("unused")
		int length = stream.getLength();
		/*
		 * if (packetId == MINI_WALKING_PACKET) length -= 13;
		 */
		
		boolean forceRun = stream.readUnsignedByte() == 1;
		if (forceRun)
			player.setRun(forceRun);
		int baseX = stream.readUnsignedShort();
		int baseY = stream.readUnsignedShortLE();

        int steps = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, player.getX(), player.getY(), player.getPlane(), player.getSize(), new FixedTileStrategy(baseX, baseY), true);
		if (steps > 25)
            steps = 25;

        player.getAttributes().stopAll(player);

        player.setNextFaceEntity(null);
        
        player.getAction().ifPresent(skill -> skill.cancel());

        if (steps > 0) {

            @SuppressWarnings("unused")
			int x = 0, y = 0;
            for (int step = 0; step < steps; step++) {
                x = baseX + stream.readUnsignedByte();
                y = baseY + stream.readUnsignedByte();
            }
            
            int[] bufferX = RouteFinder.getLastPathBufferX();
            int[] bufferY = RouteFinder.getLastPathBufferY();
            int last = -1;
            for (int i = steps - 1; i >= 0; i--) {
                if (!player.addWalkSteps(bufferX[i], bufferY[i], 25, true))
                    break;
                last = i;
            }

            if (last != -1) {
                WorldTile tile = new WorldTile(bufferX[last], bufferY[last], player.getPlane());
                player.getPackets().sendMinimapFlag(tile.getLocalX(player.getLastLoadedMapRegionTile(), player.getMapSize()), tile.getLocalY(player.getLastLoadedMapRegionTile(), player.getMapSize()));
            } else {
                player.getPackets().sendResetMinimapFlag();
            }
        }
	}
}