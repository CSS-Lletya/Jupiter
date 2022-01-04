package com.jupiter.network.encoders.other;

import lombok.Data;

/**
 * Represents a Force Chat text from the Entity (NPC, Player)
 * @author Dennis
 */
@Data
public final class ForceTalk {

	/**
	 * The text desired to force chat from the Entity
	 */
	private final String text;
}