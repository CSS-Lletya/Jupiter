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

@Data
public class AccountCreation {

	public static Player loadPlayer(String username) {
		return (Player) GsonLoader.load("data/characters/" + username + ".json", Player.class);
	}

	public static void savePlayer(Player player) {
		GsonLoader.save(player, "data/characters/" + player.getDisplayName() + ".json", Player.class);
	}

	public static boolean exists(String username) {
		return new File("data/characters/" + username + ".json").exists();
	}
	
	public static void login(Player player, Session session, String username, byte displayMode, short screenWidth, short screenHeight, IsaacKeyPair isaacKeyPair) {
		player.setSession(session);
		player.getPlayerDetails().setUsername(username);
		player.setDisplayMode(displayMode);
		player.setScreenWidth(screenWidth);
		player.setScreenHeight(screenHeight);
		player.setIsaacKeyPair(isaacKeyPair);
		if (player.attributes == null)
			player.attributes = new AttributeMap<>(Attribute.class);
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
		if (player.prayer == null)
			player.prayer = new PrayerManager();
		if (player.bank == null)
			player.bank = new Bank();
		if (player.musicsManager == null)
			player.musicsManager = new MusicsManager();
		if (player.friendsIgnores == null)
			player.friendsIgnores = new FriendsIgnores();
		if (player.auraManager == null)
			player.auraManager = new AuraManager();
		if(player.toolbelt == null)
			player.toolbelt = new Toolbelt();
		if (player.lodeStone == null)
			player.lodeStone = new LodeStone();
		if (player.getPlayerDetails().getActivatedLodestones() == null)
			player.getPlayerDetails().setActivatedLodestones(new boolean[16]);
		if (player.getCurrentActivity() == null)
			player.setCurrentActivity(Optional.empty());
		if (player.getPlayerDetails().getOwnedObjectsManagerKeys() == null) // temporary
			player.getPlayerDetails().setOwnedObjectsManagerKeys(new LinkedList<String>());
		if (player.varsManager == null)
			player.varsManager = new VarManager(player);
		player.setInterfaceManager(new InterfaceManager(player));
		player.setHintIconsManager(new HintIconsManager(player));
		player.setPriceCheckManager(new PriceCheckManager(player));
		player.localPlayerUpdate = new LocalPlayerUpdate(player);
		player.localNPCUpdate = new LocalNPCUpdate(player);
		player.setActionManager(new ActionManager(player));
		player.setTrade(new Trade(player));
		
		// loads player on saved instances
		player.lodeStone.setPlayer(player);
		player.getAppearance().setPlayer(player);
		player.getInventory().setPlayer(player);
		player.getEquipment().setPlayer(player);
		player.getSkills().setPlayer(player);
		player.toolbelt.setPlayer(player);
		player.getCombatDefinitions().setPlayer(player);
		player.prayer.setPlayer(player);
		player.bank.setPlayer(player);
		player.musicsManager.setPlayer(player);
		player.friendsIgnores.setPlayer(player);
		player.auraManager.setPlayer(player);
		player.setTemporaryMovementType((byte) -1);
		player.logicPackets = new ConcurrentLinkedQueue<LogicPacket>();
		player.setSwitchItemCache(Collections.synchronizedList(new ArrayList<Byte>()));
		
		player.initEntity();
		World.addPlayer(player);
		player.updateEntityRegion(player);

		if (player.getPlayerDetails().getPasswordList() == null)
			player.getPlayerDetails().setPasswordList(new ArrayList<String>());
		if (player.getPlayerDetails().getIpList() == null)
			player.getPlayerDetails().setIpList(new ArrayList<String>());
		player.getSession().updateIPnPass(player);
	}
}