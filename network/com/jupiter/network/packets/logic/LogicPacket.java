package com.jupiter.network.packets.logic;

import com.jupiter.cache.io.InputStream;
import com.jupiter.game.player.Player;

/**
 * Represents an Outgoing Packet
 * @author Dennis
 *
 */
public interface LogicPacket {
	
	/**
	 * Executes the Packet
	 * @param player
	 * @param entity
	 * @throws Exception
	 */
	public void execute(Player player, InputStream stream);
}