package com.jupiter.game.player;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The enumerated type whose elements represent the types of authority a player
 * can have.
 * @author lare96 <http://github.com/lare96>
 * @author Dennis
 */
@AllArgsConstructor
public enum Rights {
	PLAYER(0),
	MODERATOR(1),
	ADMINISTRATOR(2);
	
	/**
	 * The value of this rank as seen by the server. This value will be used to
	 * determine which of the elements are greater than each other.
	 */
	@Getter
	private final int value;
	
	/**
	 * Determines if this right is greater than the argued right. Please note
	 * that this method <b>does not</b> compare the Objects themselves, but
	 * instead compares the value behind them as specified by {@code value} in
	 * the enumerated type.
	 * @param other the argued right to compare.
	 * @return {@code true} if this right is greater, {@code false} otherwise.
	 */
	public final boolean greater(Rights other) {
		return value > other.value;
	}
	
	/**
	 * Determines if this right is lesser than the argued right. Please note
	 * that this method <b>does not</b> compare the Objects themselves, but
	 * instead compares the value behind them as specified by {@code value} in
	 * the enumerated type.
	 * @param other the argued right to compare.
	 * @return {@code true} if this right is lesser, {@code false} otherwise.
	 */
	public final boolean less(Rights other) {
		return value < other.value;
	}
	
	/**
	 * Determines if this right is equal in power to the argued right. Please
	 * note that this method <b>does not</b> compare the Objects themselves, but
	 * instead compares the value behind them as specified by {@code value} in
	 * the enumerated type.
	 * @param other the argued right to compare.
	 * @return {@code true} if this right is equal, {@code false} otherwise.
	 */
	public final boolean equal(Rights other) {
		return value == other.value;
	}
	
	public final boolean equals(Rights... rights) {
		return Arrays.stream(rights).anyMatch(right -> this == right);
	}
	
	public final boolean isStaff() {
		return this.equals(MODERATOR) || this.equals(ADMINISTRATOR);
	}
}