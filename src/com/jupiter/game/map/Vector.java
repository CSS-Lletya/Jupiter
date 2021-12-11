package com.jupiter.game.map;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Vector {
	
	private float x, y;
	
	public Vector(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	public Vector(WorldTile tile) {
		this.x = tile.getX();
		this.y = tile.getY();
	}

	public Vector sub(Vector v2) {
		return new Vector(this.x - v2.x, this.y - v2.y);
	}
	
	public void norm() {
		float mag = (float) Math.sqrt(this.x*this.x + this.y*this.y);
		this.x = (float) (this.x / mag);
		this.y = (float) (this.y / mag);
	}
	
	public WorldTile toTile() {
		return toTile(0);
	}

	public WorldTile toTile(int plane) {
		return new WorldTile((int) Math.round(this.x), (int) Math.round(this.y), plane);
	}
	
	@Override
	public String toString() {
		return "[" + x +","+y+"]";
	}
}