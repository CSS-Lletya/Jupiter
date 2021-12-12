package com.jupiter.skills.prayer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Leech {
	ATTACK(Prayer.LEECH_ATTACK, 2231, 2232),
	STRENGTH(Prayer.LEECH_STRENGTH, 2248, 2250),
	DEFENSE(Prayer.LEECH_DEFENSE, 2244, 2246),
	RANGED(Prayer.LEECH_RANGE, 2236, 2238),
	MAGIC(Prayer.LEECH_MAGIC, 2240, 2242),
	SPECIAL(Prayer.LEECH_SPECIAL, 2256, 2258),
	ENERGY(Prayer.LEECH_ENERGY, 2252, 2254);
	
	private Prayer prayer;

	private int projAnim, spotAnimHit;
}