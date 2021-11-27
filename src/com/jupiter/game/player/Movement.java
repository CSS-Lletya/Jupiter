package com.jupiter.game.player;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import com.jupiter.game.map.WorldTile;
import com.jupiter.net.encoders.other.Animation;
import com.jupiter.utils.Utils;

import lombok.Data;

@Data
public class Movement {
	
	private transient ConcurrentLinkedQueue<Object[]> walkSteps;
	
	/**
	 * Represents the Player
	 */
	private transient Player player;
	
	/**
	 * Constructs a new Movement event
	 * @param player
	 */
	public Movement(Player player) {
		this.player = player;
		walkSteps = new ConcurrentLinkedQueue<Object[]>();
	}
	
	/**
	 * Represents the Lock delay time
	 */
	private transient long lockDelay; // used for doors and stuff like that

	/**
	 * Checks if the {@link #player} is locked
	 * @return
	 */
	public boolean isLocked() {
		return lockDelay >= Utils.currentTimeMillis();
	}

	/**
	 * Locks the {@link #player} indefinitely 
	 */
	public void lock() {
		lockDelay = Long.MAX_VALUE;
	}

	/**
	 * Locks the {@link #player} until the consumer event passes
	 * Note: Required to use {@link #unlock()} method to release the lock state in the consumer event
	 * @param player
	 */
	public void lockUntil(Consumer<Player> player) {
		lock();
		player.accept(this.player);
	}
	
	/**
	 * Locks the {@link #player} for a specific amount of Seconds.
	 * @param time
	 */
	public void lock(long time) {
		lockDelay = Utils.currentTimeMillis() + (time * 600);
	}

	/**
	 * Unlocks the {@link #player} indefinitely
	 */
	public void unlock() {
		lockDelay = 0;
	}
	
	/**
	 * Moves the {@link #player} to a specified position with optional parameters.
	 * @param emoteId
	 * @param dest
	 */
	public void move(Optional<Animation> emoteId, final WorldTile dest) {
		move(emoteId, dest, Optional.empty());
	}

	/**
	 * Moves the {@link #player} to a specified position with optional parameters.
	 * @param emoteId
	 * @param dest
	 */
	public void move(Optional<Animation> emoteId, final WorldTile dest, Optional<String> message) {
		lockUntil(p -> {
			p.stopAll();
			emoteId.ifPresent(p::setNextAnimation);
			p.task(1, event -> {
				event.setNextWorldTile(dest);
				event.getMovement().unlock();
				message.ifPresent(event.getPackets()::sendGameMessage);
			});
		});
	}
	
	public void drainRunEnergy() {
		setRunEnergy(player.getPlayerDetails().getRunEnergy() - 1);
	}

	public void setRunEnergy(double runEnergy) {
		player.getPlayerDetails().setRunEnergy(runEnergy);
		player.getPackets().sendRunEnergy();
	}

	public void setResting(boolean resting) {
		player.setResting(resting);
		sendRunButtonConfig();
	}
	
	public void toogleRun(boolean update) {
		player.setRun(!player.getRun());
		player.setUpdateMovementType(update);
		if (update)
			sendRunButtonConfig();
	}

	public void setRunHidden(boolean run) {
		player.setRun(run);
		player.setUpdateMovementType(run);
	}
	
	public void sendRunButtonConfig() {
		player.getPackets().sendConfig(173, player.isResting() ? 3 : player.getRun() ? 1 : 0);
	}
}