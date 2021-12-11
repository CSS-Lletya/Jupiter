package com.jupiter.game.player.activity;

import com.jupiter.combat.npc.NPC;
import com.jupiter.game.Entity;
import com.jupiter.game.item.FloorItem;
import com.jupiter.game.item.Item;
import com.jupiter.game.map.WorldObject;
import com.jupiter.game.map.WorldTile;
import com.jupiter.game.player.Player;
import com.jupiter.skills.cooking.Foods.Food;

import lombok.Data;

@Data
public abstract class Activity {

	/**
	 * The string which defines the current activity.
	 */
	private final String activity;

	/**
	 * The current name of this activity.
	 */
	private final ActivitySafety safety;

	/**
	 * The current type of this activity.
	 */
	private final ActivityType type;

	public void start(Player player) {
	}

	public boolean canEat(Player player, Food food) {
		return true;
	}

	// TODO: This condition
//	public boolean canPot(Player player, Pot pot) {
//		return true;
//	}

	public boolean canTakeItem(Player player, FloorItem item) {
		return true;
	}

	public boolean keepCombating(Player player, Entity target) {
		return true;
	}

	public boolean canEquip(Player player, int slotId, int itemId) {
		return true;
	}

	public boolean canAttack(Player player, Entity target) {
		return true;
	}

	public boolean canDeleteInventoryItem(Player player, int itemId, int amount) {
		return true;
	}

	public boolean canUseItemOnItem(Player player, Item itemUsed, Item usedWith) {
		return true;
	}

	public boolean canAddInventoryItem(Player player, int itemId, int amount) {
		return true;
	}

	public boolean canPlayerOption1(Player player, Player target) {
		return true;
	}

	public boolean canPlayerOption2(Player player, Player target) {
		return true;
	}

	public boolean canPlayerOption3(Player target) {
		return true;
	}

	public boolean canPlayerOption4(Player target) {
		return true;
	}

	public boolean canHit(Player player, Entity entity) {
		return true;
	}

	/**
	 * processes every game ticket, usualy not used
	 */
	public void process(Player player) {

	}

	public void moved(Player player) {

	}

	/**
	 * called once teleport is performed
	 */
	public void magicTeleported(Player player, int type) {

	}

	public void sendInterfaces(Player player) {

	}

	public boolean useDialogueScript(Player player, Object key) {
		return true;
	}

	public boolean processMagicTeleport(Player player, WorldTile toTile) {
		return true;
	}

	public boolean processItemTeleport(Player player, WorldTile toTile) {
		return true;
	}

	public boolean processObjectTeleport(Player player, WorldTile toTile) {
		return true;
	}

	public boolean processObjectClick1(Player player, WorldObject object) {
		return true;
	}

	public boolean processButtonClick(Player player, int interfaceId, int componentId, int slotId, int slotId2,
			int packetId) {
		return true;
	}

	public boolean processNPCClick1(Player player, NPC npc) {
		return true;
	}

	public boolean processNPCClick2(Player player, NPC npc) {
		return true;
	}

	public boolean processNPCClick3(Player player, NPC npc) {
		return true;
	}

	public boolean processNPCClick4(Player player, NPC npc) {
		return true;
	}

	public boolean processObjectClick2(Player player, WorldObject object) {
		return true;
	}

	public boolean processObjectClick3(Player player, WorldObject object) {
		return true;
	}

	public boolean processObjectClick4(Player player, WorldObject object) {
		return true;
	}

	public boolean processObjectClick5(Player player, WorldObject object) {
		return true;
	}

	public boolean handleItemOnObject(Player player, WorldObject object, Item item) {
		return true;
	}

	public boolean sendDeath(Player player) {
		return true;
	}

	public boolean canMove(Player player, int dir) {
		return true;
	}

	public boolean checkWalkStep(Player player, int lastX, int lastY, int nextX, int nextY) {
		return true;
	}

	public void login(Player player) {
	}

	public boolean logout(Player player) {
		return false;
	}

	public void forceClose(Player player) {
	}

	public boolean processItemOnNPC(Player player, NPC npc, Item item) {
		return true;
	}

	public boolean canDropItem(Player player, Item item) {
		return true;
	}

	public boolean canSummonFamiliar(Player player) {
		return true;
	}

	public boolean processItemOnPlayer(Player player, Player target, Item item) {
		return true;
	}

	public void processNPCDeath(Player player, int id) {

	}

	/**
	 * Determines if {@code player} is in this activity.
	 * 
	 * @param player the player to determine this for.
	 * @return <true> if this activity contains the player, <false> otherwise.
	 */
	public boolean contains(Player player) {
		return false;
	}

	/**
	 * The enumerated type whose elements represent the activity types.
	 * 
	 * @author Dennis
	 * @author lare96 <http://github.com/lare96>
	 */
	public enum ActivityType {
		// A standard activity
		NORMAL,
		// A sequenced activity (warriors guild cyclops room)
		SEQUENCED
	}

	/**
	 * The enumerated type whose elements represent the item safety of a player who
	 * is playing the activity.
	 * 
	 * @author Dennis
	 * @author <a href="http://www.rune-server.org/members/stand+up/">Stand Up</a>
	 */
	public enum ActivitySafety {
		/**
		 * This safety is similar to when a player dies while they are skulled.
		 */
		DANGEROUS,
		/**
		 * Indicates the activity is fully safe and no items will be lost on death.
		 */
		SAFE
	}
}