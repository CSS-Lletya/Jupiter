package com.jupiter.game;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import com.jupiter.game.map.WorldTile;
import com.jupiter.game.player.Player;
import com.jupiter.net.encoders.other.Animation;
import com.jupiter.utils.Utils;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class Movement {
	
	private transient ConcurrentLinkedQueue<Object[]> walkSteps;
	
	/**
	 * Represents the Entity
	 */
	private transient Entity entity;
	
	/**
	 * Constructs a new Movement event
	 * @param entity
	 */
	public Movement(Entity entity) {
		this.entity = entity;
		walkSteps = new ConcurrentLinkedQueue<Object[]>();
	}
	
	/**
	 * Represents the Lock delay time
	 */
	private transient long lockDelay; // used for doors and stuff like that

	/**
	 * Checks if the {@link #entity} is locked
	 * @return
	 */
	public boolean isLocked() {
		return lockDelay >= Utils.currentTimeMillis();
	}

	/**
	 * Locks the {@link #entity} indefinitely 
	 */
	public void lock() {
		lockDelay = Long.MAX_VALUE;
	}

	/**
	 * Locks the {@link #entity} until the consumer event passes
	 * Note: Required to use {@link #unlock()} method to release the lock state in the consumer event
	 * @param entity
	 */
	public void lockUntil(Consumer<Entity> entity) {
		lock();
		entity.accept(this.entity);
	}
	
	/**
	 * Locks the {@link #entity} for a specific amount of Seconds.
	 * @param time
	 */
	public void lock(long time) {
		lockDelay = Utils.currentTimeMillis() + (time * 600);
	}

	/**
	 * Unlocks the {@link #entity} indefinitely
	 */
	public void unlock() {
		lockDelay = 0;
	}
	
	/**
	 * Moves the {@link #entity} to a specified position with optional parameters.
	 * @param emoteId
	 * @param dest
	 */
	public void move(Optional<Animation> emoteId, final WorldTile dest) {
		move(emoteId, dest, Optional.empty());
	}

	/**
	 * Moves the {@link #entity} to a specified position with optional parameters.
	 * @param emoteId
	 * @param dest
	 */
	public void move(Optional<Animation> emoteId, final WorldTile dest, Optional<String> message) {
		lockUntil(p -> {
			p.toPlayer().stopAll();
			emoteId.ifPresent(p::setNextAnimation);
			p.toPlayer().task(1, event -> {
				event.setNextWorldTile(dest);
				event.getMovement().unlock();
				message.ifPresent(event.getPackets()::sendGameMessage);
			});
		});
	}
	
	public void drainRunEnergy() {
		setRunEnergy(entity.toPlayer().getPlayerDetails().getRunEnergy() - 1);
	}

	public void setRunEnergy(double runEnergy) {
		entity.toPlayer().getPlayerDetails().setRunEnergy(runEnergy);
		entity.toPlayer().getPackets().sendRunEnergy();
	}

	public void setRestingMode(boolean resting) {
		setRestingMode(resting);
		sendRunButtonConfig();
	}
	
	public void toogleRun(boolean update) {
		entity.setRun(!isRun());
		entity.toPlayer().setUpdateMovementType(update);
		if (update)
			sendRunButtonConfig();
	}

	public void setRunHidden(boolean run) {
		entity.setRun(run);
		entity.toPlayer().setUpdateMovementType(run);
	}
	
	public void sendRunButtonConfig() {
		entity.toPlayer().getPackets().sendConfig(173, isResting() ? 3 : isRun() ? 1 : 0);
	}
	
	@Getter
	@Setter
	private transient boolean resting;
	
	public transient final byte TELE_MOVE_TYPE = 127, WALK_MOVE_TYPE = 1, RUN_MOVE_TYPE = 2;
	
	public byte getMovementType(Player player) {
		if (player.getTemporaryMovementType() != -1)
			return player.getTemporaryMovementType();
		return isRun() ? RUN_MOVE_TYPE : WALK_MOVE_TYPE;
	}
	
	@Getter
	@Setter
	private transient boolean run;
}