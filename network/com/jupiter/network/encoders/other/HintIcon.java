package com.jupiter.network.encoders.other;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a Hint Icon marker to display on a specific tile
 * @author Dennis
 *
 */
@Getter
@Setter
public final class HintIcon {

	/**
	 * The X coordinate
	 */
	private int coordX;
	
	/**
	 * The Y coordinate
	 */
	private int coordY;
	
	/**
	 * The Plane (Height leveL) of the coordinate
	 */
	private int plane;
	
	/**
	 * Represents the distance height from Ground to X height
	 */
	private int distanceFromFloor;
	
	/**
	 * Represents the Target Type
	 */
	private int targetType;
	
	/**
	 * Represents the Target Index
	 */
	private int targetIndex;
	
	/**
	 * Represents the Arrow Type (Color, etc..)
	 */
	private int arrowType;
	
	/**
	 * Represents the Hint Icon model ID
	 */
	private int modelId;
	
	/**
	 * Represents the base index
	 */
	private int index;

	/**
	 * Constructs a new Hint Icon
	 */
	public HintIcon() {
		this.setIndex(7);
	}

	/**
	 * Constructs a new Hint Icon
	 * @param targetType
	 * @param modelId
	 * @param index
	 */
	public HintIcon(int targetType, int modelId, int index) {
		this.setTargetType(targetType);
		this.setModelId(modelId);
		this.setIndex(index);
	}

	/**
	 * Constructs a new Hint Icon
	 * @param targetIndex
	 * @param targetType
	 * @param arrowType
	 * @param modelId
	 * @param index
	 */
	public HintIcon(int targetIndex, int targetType, int arrowType, int modelId, int index) {
		this.setTargetType(targetType);
		this.setTargetIndex(targetIndex);
		this.setArrowType(arrowType);
		this.setModelId(modelId);
		this.setIndex(index);
	}

	/**
	 * Constructs a new Hint Icon
	 * @param coordX
	 * @param coordY
	 * @param height
	 * @param distanceFromFloor
	 * @param targetType
	 * @param arrowType
	 * @param modelId
	 * @param index
	 */
	public HintIcon(int coordX, int coordY, int height, int distanceFromFloor, int targetType, int arrowType,
			int modelId, int index) {
		this.setCoordX(coordX);
		this.setCoordY(coordY);
		this.setPlane(height);
		this.setDistanceFromFloor(distanceFromFloor);
		this.setTargetType(targetType);
		this.setArrowType(arrowType);
		this.setModelId(modelId);
		this.setIndex(index);
	}
}