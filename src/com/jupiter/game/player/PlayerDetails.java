package com.jupiter.game.player;

import java.util.concurrent.ConcurrentHashMap;

import com.jupiter.utils.Utils;

import lombok.Data;

/**
 * All personal variables of the Player shall be stored here for easier access.
 * 
 * @author Dennis
 *
 */
@Data
public final class PlayerDetails {

	/**
	 * Constructs a new Player's details
	 */
	public PlayerDetails() {
		pouches = new byte[4];
		maxedCapeCustomized = new int[4];
		completionistCapeCustomized = new int[4];
		varBitList = new ConcurrentHashMap<Integer, Integer>();
	}

	/**
	 * An array of Runecrafting pouches that possibly contain values.
	 */
	private byte[] pouches;


	private long displayTime;

	/**
	 * The length of a Player being Muted (Unable to chat)
	 */
	private long muted;

	/**
	 * Length of the Player being Jailed (stuck in a remote area)
	 */
	private long jailed;

	/**
	 * Adds Players display time
	 * 
	 * @param i
	 */
	public void addDisplayTime(long i) {
		this.displayTime = i + Utils.currentTimeMillis();
	}

	/**
	 * Represents the last known IP from the Player
	 */
	private String lastIP;

	/**
	 * Represents if a Player is filtering out their chatbox messages
	 */
	private boolean filterGame;
	/**
	 * Represents if the Player has their experience locked
	 */
	private boolean xpLocked;

	/**
	 * An array of possible changes to the Max Cape customization
	 */
	private int[] maxedCapeCustomized;
	/**
	 * An array of possible changes to the Completionist Cape customization
	 */
	private int[] completionistCapeCustomized;

	/**
	 * Represents if the Player should be using older item models to display
	 */
	public boolean oldItemsLook;
	
	/**
	 * Represents the default Yell color for a Player
	 */
	public String yellColor = "ff0000";

	public int skullId;

	/**
	 * @varpbit
	 */

	public ConcurrentHashMap<Integer, Integer> varBitList = new ConcurrentHashMap<>();
	public transient ConcurrentHashMap<Integer, Integer> temporaryVarBits = new ConcurrentHashMap<>();
}