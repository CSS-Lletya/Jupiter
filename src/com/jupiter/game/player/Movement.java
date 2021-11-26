package com.jupiter.game.player;

import java.util.function.Consumer;

import com.jupiter.game.map.World;
import com.jupiter.game.map.WorldTile;
import com.jupiter.game.task.Task;
import com.jupiter.net.encoders.other.Animation;
import com.jupiter.utils.Utils;

import lombok.Data;

@Data
public class Movement {
	
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
	
	public void useStairs(int emoteId, final WorldTile dest) {
		useStairs(emoteId, dest, null);
	}

	public void useStairs(int emoteId, final WorldTile dest,final String message) {
		lockUntil(p -> {
			p.stopAll();
			if (emoteId != -1)
				p.setNextAnimation(new Animation(emoteId));
			World.get().submit(new Task(1) {
				@Override
				protected void execute() {
					p.setNextWorldTile(dest);
					p.getMovement().unlock();
					if (message != null)
						p.getPackets().sendGameMessage(message);
					this.cancel();
				}
			});
		});
	}
}