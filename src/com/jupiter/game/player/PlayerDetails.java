package com.jupiter.game.player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.jupiter.utility.Utility;

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
		rights = Rights.PLAYER;
		ownedObjectsManagerKeys = new LinkedList<String>();
		passwordList = new ArrayList<String>();
		ipList = new ArrayList<String>();
		runEnergy = 100D;
		allowChatEffects = true;
		mouseButtons = true;
		activatedLodestones = new boolean[16];
	}

	/**
	 * The players raw Username (non-formatted)
	 */
	private transient String username;
	/**
	 * The players personal password for login
	 */
	private String password;
	
	/**
	 * The amount of authority this player has over others.
	 */
	private Rights rights = Rights.PLAYER;
	
	// game bar status
	private byte publicStatus;
	private byte clanStatus;
	private byte tradeStatus;
	private boolean[] activatedLodestones;
	
	private double runEnergy;
	private boolean allowChatEffects;
	private boolean mouseButtons;
	private byte privateChatSetup;
	private byte friendChatSetup;

	// Used for storing recent ips and password
	private ArrayList<String> passwordList = new ArrayList<String>();
	private ArrayList<String> ipList = new ArrayList<String>();


	private String currentFriendChatOwner;
	private byte summoningLeftClickOption;
	private List<String> ownedObjectsManagerKeys;
	
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
		this.displayTime = i + Utility.currentTimeMillis();
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