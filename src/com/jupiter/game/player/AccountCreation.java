package com.jupiter.game.player;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.jupiter.cache.loaders.VarManager;
import com.jupiter.combat.player.CombatDefinitions;
import com.jupiter.game.map.World;
import com.jupiter.game.player.actions.ActionManager;
import com.jupiter.game.player.attributes.AttributeMap;
import com.jupiter.game.player.attributes.PlayerAttribute;
import com.jupiter.game.player.content.AuraManager;
import com.jupiter.game.player.content.LodeStone;
import com.jupiter.game.player.content.MusicsManager;
import com.jupiter.game.player.content.PriceCheckManager;
import com.jupiter.game.player.content.Toolbelt;
import com.jupiter.game.player.controlers.ActivityManager;
import com.jupiter.json.GsonLoader;
import com.jupiter.net.Session;
import com.jupiter.net.decoders.LogicPacket;
import com.jupiter.net.encoders.other.HintIconsManager;
import com.jupiter.net.encoders.other.LocalNPCUpdate;
import com.jupiter.net.encoders.other.LocalPlayerUpdate;
import com.jupiter.skills.Skills;
import com.jupiter.skills.prayer.Prayer;
import com.jupiter.utils.IsaacKeyPair;
import com.jupiter.utils.Utils;

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
		player.session = session;
		player.username = username;
		player.displayMode = displayMode;
		player.screenWidth = screenWidth;
		player.screenHeight = screenHeight;
		player.isaacKeyPair = isaacKeyPair;
		if (player.getAttributes() == null)
			player.attributes = new AttributeMap<>(PlayerAttribute.class);
		if (player.appearence == null)
			player.appearence = new Appearance();
		if (player.inventory == null)
			player.inventory = new Inventory();
		if (player.equipment == null)
			player.equipment = new Equipment();
		if (player.skills == null)
			player.skills = new Skills();
		if (player.combatDefinitions == null)
			player.combatDefinitions = new CombatDefinitions();
		if (player.prayer == null)
			player.prayer = new Prayer();
		if (player.bank == null)
			player.bank = new Bank();
		if (player.controlerManager == null)
			player.controlerManager = new ActivityManager();
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
		if (player.varsManager == null)
			player.varsManager = new VarManager(player);
		player.interfaceManager = new InterfaceManager(player);
		player.hintIconsManager = new HintIconsManager(player);
		player.priceCheckManager = new PriceCheckManager(player);
		player.localPlayerUpdate = new LocalPlayerUpdate(player);
		player.localNPCUpdate = new LocalNPCUpdate(player);
		player.actionManager = new ActionManager(player);
		player.trade = new Trade(player);
		
		// loads player on saved instances
		player.lodeStone.setPlayer(player);
		player.appearence.setPlayer(player);
		player.inventory.setPlayer(player);
		player.equipment.setPlayer(player);
		player.skills.setPlayer(player);
		player.toolbelt.setPlayer(player);
		player.combatDefinitions.setPlayer(player);
		player.prayer.setPlayer(player);
		player.bank.setPlayer(player);
		player.controlerManager.setPlayer(player);
		player.musicsManager.setPlayer(player);
		player.friendsIgnores.setPlayer(player);
		player.auraManager.setPlayer(player);
		player.temporaryMovementType = -1;
		player.logicPackets = new ConcurrentLinkedQueue<LogicPacket>();
		player.switchItemCache = Collections.synchronizedList(new ArrayList<Byte>());
		player.initEntity();
		player.packetsDecoderPing = Utils.currentTimeMillis();
		World.addPlayer(player);
		player.updateEntityRegion(player);

		if (player.getPlayerDetails().getPasswordList() == null)
			player.getPlayerDetails().setPasswordList(new ArrayList<String>());
		if (player.getPlayerDetails().getIpList() == null)
			player.getPlayerDetails().setIpList(new ArrayList<String>());
		player.getSession().updateIPnPass(player);
	}
}