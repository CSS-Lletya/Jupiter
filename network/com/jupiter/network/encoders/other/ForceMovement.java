package com.jupiter.network.encoders.other;

import com.jupiter.game.map.WorldTile;
import com.jupiter.utility.Utility;

import lombok.Data;
import lombok.Getter;

/**
 * Represents a Forced Movement event for an Entity (Typically the Player)
 * @author Dennis
 */
@Getter
@Data
public class ForceMovement {

	/**
	 * The Directions of the movements
	 */
	public static final int NORTH = 0, EAST = 1, SOUTH = 2, WEST = 3;

	/**
	 * The First tile for the event
	 */
	private final WorldTile toFirstTile;
	
	/**
	 * The First tile delay for the event
	 */
	private final int firstTileTicketDelay;
	
	/**
	 * The Second tile for the event
	 */
	private final WorldTile toSecondTile;
	
	/**
	 * The First tile delay for the event
	 */
	private final int secondTileTicketDelay;
	
	/**
	 * An undefined direction value for the event
	 */
	private final int direction;

	public int getDirection() {
		switch (direction) {
		case NORTH:
			return Utility.getFaceDirection(0, 1);
		case EAST:
			return Utility.getFaceDirection(1, 0);
		case SOUTH:
			return Utility.getFaceDirection(0, -1);
		case WEST:
		default:
			return Utility.getFaceDirection(-1, 0);
		}
	}
}