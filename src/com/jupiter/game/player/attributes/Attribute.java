package com.jupiter.game.player.attributes;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Attribute implements AttributeType {
	BUSY(false), IN_COMBAT(false), AUTO_RETALIATE(true), RUN_ENERGY(100), PLAYERS_KILLED(0);

	private final Object defaultValue;

	@Override
	public Object defaultValue() {
		return defaultValue;
	}
}