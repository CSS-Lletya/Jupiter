package com.jupiter.network.encoders.other;

import lombok.Getter;

/**
 * Represents a Graphic in the Game world
 * @author Dennis
 */
@Getter
public final class Graphics {

	/**
	 * The Id of the graphic
	 * The Height of the Graphic
	 * The Speed of the Graphic
	 * The Rotation of the Graphic
	 */
	private int id, height, speed, rotation;

	/**
	 * Constructs a new Graphic
	 * @param id
	 */
	public Graphics(int id) {
		this(id, 0, 0, 0);
	}

	/**
	 * Constructs a new Graphic
	 * @param id
	 * @param speed
	 * @param height
	 */
	public Graphics(int id, int speed, int height) {
		this(id, speed, height, 0);
	}

	/**
	 * Constructs a new Graphic
	 * @param id
	 * @param speed
	 * @param height
	 * @param rotation
	 */
	public Graphics(int id, int speed, int height, int rotation) {
		this.id = id;
		this.speed = speed;
		this.height = height;
		this.rotation = rotation;
	}

	/**
	 * Represents the Hash Code id for the Graphic (Ignore)
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + height;
		result = prime * result + id;
		result = prime * result + rotation;
		result = prime * result + speed;
		return result;
	}

	/**
	 * Checks value of the Graphic (Ignore)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Graphics other = (Graphics) obj;
		if (height != other.height)
			return false;
		if (id != other.id)
			return false;
		if (rotation != other.rotation)
			return false;
		if (speed != other.speed)
			return false;
		return true;
	}

	/**
	 * Gets the Settings Hash mask for the Graphic
	 * @return hash
	 */
	public int getSettingsHash() {
		return (speed & 0xffff) | (height << 16);
	}

	/**
	 * Gets the Settings Hash mask for the Graphic
	 * @return hash
	 */
	public int getSettings2Hash() {
		int hash = 0;
		return hash |= rotation & 0x7;
	}
}