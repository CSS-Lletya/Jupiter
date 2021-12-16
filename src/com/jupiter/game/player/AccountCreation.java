package com.jupiter.game.player;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.jupiter.cache.loaders.VarManager;
import com.jupiter.combat.player.CombatDefinitions;
import com.jupiter.game.map.World;
import com.jupiter.game.player.actions.ActionManager;
import com.jupiter.game.player.attributes.Attribute;
import com.jupiter.game.player.attributes.AttributeMap;
import com.jupiter.game.player.content.AuraManager;
import com.jupiter.game.player.content.LodeStone;
import com.jupiter.game.player.content.MusicsManager;
import com.jupiter.game.player.content.PriceCheckManager;
import com.jupiter.game.player.content.Toolbelt;
import com.jupiter.json.GsonLoader;
import com.jupiter.network.Session;
import com.jupiter.network.decoders.LogicPacket;
import com.jupiter.network.encoders.other.HintIconsManager;
import com.jupiter.network.encoders.other.LocalNPCUpdate;
import com.jupiter.network.encoders.other.LocalPlayerUpdate;
import com.jupiter.network.utility.IsaacKeyPair;
import com.jupiter.skills.Skills;
import com.jupiter.skills.prayer.PrayerManager;

import lombok.Data;

/**
 * Represents a state of a Player (Loading, Creating, generating account details)
 * @author Dennis
 */
@Data
public class AccountCreation {

	/**
	 * Loads a Player via username
	 * @param username
	 * @return player
	 */
	public static Player loadPlayer(String username) {
		return (Player) GsonLoader.load("data/characters/" + username + ".json", Player.class);
	}

	/**
	 * Saves the target Player
	 * @param player
	 */
	public static void savePlayer(Player player) {
		GsonLoader.save(player, "data/characters/" + player.getDisplayName() + ".json", Player.class);
	}

	/**
	 * Checks if the target player username exists in the characters folder
	 * @param username
	 * @return
	 */
	public static boolean exists(String username) {
		return new File("data/characters/" + username + ".json").exists();
	}
	
	/**
	 * Registers and creates (if necessary) various player details & attributes
	 * @param player
	 * @param session
	 * @param username
	 * @param displayMode
	 * @param screenWidth
	 * @param screenHeight
	 * @param isaacKeyPair
	 */
	public static void login(Player player, Session session, String username, byte displayMode, short screenWidth, short screenHeight, IsaacKeyPair isaacKeyPair) {
		player.setSession(session);
		player.getPlayerDetails().setUsername(username);
		player.setDisplayMode(displayMode);
		player.setScreenWidth(screenWidth);
		player.setScreenHeight(screenHeight);
		player.setIsaacKeyPair(isaacKeyPair);
		if (player.getAttributes() == null)
			player.setAttributes( new AttributeMap<>(Attribute.class));
		if (player.getAppearance() == null)
			player.setAppearance(new Appearance());
		if (player.getInventory() == null)
			player.setInventory(new Inventory());
		if (player.getEquipment() == null)
			player.setEquipment(new Equipment());
		if (player.getSkills() == null)
			player.setSkills(new Skills());
		if (player.getCombatDefinitions() == null)
			player.setCombatDefinitions(new CombatDefinitions());
		if (player.getPrayer() == null)
			player.setPrayer(new PrayerManager());
		if (player.getBank() == null)
			player.setBank(new Bank());
		if (player.getMusicsManager() == null)
			player.setMusicsManager(new MusicsManager());
		if (player.getFriendsIgnores() == null)
			player.setFriendsIgnores(new FriendsIgnores());
		if (player.getAuraManager() == null)
			player.setAuraManager(new AuraManager());
		if(player.getToolbelt() == null)
			player.setToolbelt(new Toolbelt());
		if (player.getLodeStone() == null)
			player.setLodeStone(new LodeStone());
		if (player.getCurrentActivity() == null)
			player.setCurrentActivity(Optional.empty());
		if (player.getPlayerDetails().getOwnedObjectsManagerKeys() == null) // temporary
			player.getPlayerDetails().setOwnedObjectsManagerKeys(new LinkedList<String>());
		if (player.getVarsManager() == null)
			player.setVarsManager(new VarManager(player));
		if (player.getPlayerDetails().getPasswordList() == null)
			player.getPlayerDetails().setPasswordList(new ArrayList<String>());
		if (player.getPlayerDetails().getIpList() == null)
			player.getPlayerDetails().setIpList(new ArrayList<String>());
		player.setInterfaceManager(new InterfaceManager(player));
		player.setHintIconsManager(new HintIconsManager(player));
		player.setPriceCheckManager(new PriceCheckManager(player));
		player.setLocalPlayerUpdate(new LocalPlayerUpdate(player));
		player.setLocalNPCUpdate(new LocalNPCUpdate(player));
		player.setActionManager(new ActionManager(player));
		player.setTrade(new Trade(player));
		player.setTemporaryMovementType((byte) -1);
		player.setLogicPackets(new ConcurrentLinkedQueue<LogicPacket>());
		player.setSwitchItemCache(Collections.synchronizedList(new ArrayList<Byte>()));
		
		player.getLodeStone().setPlayer(player);
		player.getAppearance().setPlayer(player);
		player.getInventory().setPlayer(player);
		player.getEquipment().setPlayer(player);
		player.getSkills().setPlayer(player);
		player.getToolbelt().setPlayer(player);
		player.getCombatDefinitions().setPlayer(player);
		player.getPrayer().setPlayer(player);
		player.getBank().setPlayer(player);
		player.getMusicsManager().setPlayer(player);
		player.getFriendsIgnores().setPlayer(player);
		player.getAuraManager().setPlayer(player);
		
		player.initEntity();
		World.addPlayer(player);
		player.updateEntityRegion(player);
		player.getSession().updateIPnPass(player);
	}
}