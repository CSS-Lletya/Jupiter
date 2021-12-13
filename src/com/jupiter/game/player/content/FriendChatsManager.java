package com.jupiter.game.player.content;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.jupiter.Settings;
import com.jupiter.cache.io.OutputStream;
import com.jupiter.game.map.World;
import com.jupiter.game.player.AccountCreation;
import com.jupiter.game.player.FriendsIgnores;
import com.jupiter.game.player.Player;
import com.jupiter.utility.Utility;

public class FriendChatsManager {

	private String owner;
	private String ownerDisplayName;
	private FriendsIgnores settings;
	private CopyOnWriteArrayList<Player> players;
	private ConcurrentHashMap<String, Long> bannedPlayers;
	private byte[] dataBlock;

	private static HashMap<String, FriendChatsManager> cachedFriendChats;

	public static void init() {
		cachedFriendChats = new HashMap<String, FriendChatsManager>();
	}

	public int getRank(int rights, String username) {
		if (rights == 2)
			return 127;
		if (username.equals(owner))
			return 7;
		return settings.getRank(username);
	}

	public CopyOnWriteArrayList<Player> getPlayers() {
		return players;
	}

	public int getWhoCanKickOnChat() {
		return settings.getWhoCanKickOnChat();
	}

	public String getOwnerDisplayName() {
		return ownerDisplayName;
	}

	public String getOwnerName() {
		return owner;
	}

	public String getChannelName() {
		return settings.getChatName().replaceAll("<img=", "");
	}

	private void joinChat(Player player) {
		synchronized (this) {
			if (!player.getPlayerDetails().getUsername().equals(owner) && !settings.hasRankToJoin(player.getPlayerDetails().getUsername())
					&& !player.getPlayerDetails().getRights().isStaff()) {
				player.getPackets().sendGameMessage("You do not have a enough rank to join this friends chat channel.");
				return;
			}
			if (players.size() >= 100) {
				player.getPackets().sendGameMessage("This chat is full.");
				return;
			}
			Long bannedSince = bannedPlayers.get(player.getPlayerDetails().getUsername());
			if (bannedSince != null) {
				if (bannedSince + 3600000 > Utility.currentTimeMillis()) {
					player.getPackets().sendGameMessage("You have been banned from this channel.");
					return;
				}
				bannedPlayers.remove(player.getPlayerDetails().getUsername());
			}
			joinChatNoCheck(player);
		}
	}

	public void leaveChat(Player player, boolean logout) {
		synchronized (this) {
			player.setCurrentFriendChat(null);
			players.remove(player);
			if (players.size() == 0) { // no1 at chat so uncache it
				synchronized (cachedFriendChats) {
					cachedFriendChats.remove(owner);
				}
			} else
				refreshChannel();
			if (!logout) {
				player.getPlayerDetails().setCurrentFriendChatOwner(null);
				player.getPackets().sendGameMessage("You have left the channel.");
				player.getPackets().sendFriendsChatChannel();
			}
		}
	}

	public Player getPlayerByDisplayName(String username) {
		String formatedUsername = Utility.formatPlayerNameForProtocol(username);
		for (Player player : players) {
			if (player.getPlayerDetails().getUsername().equals(formatedUsername) || player.getDisplayName().equals(username))
				return player;
		}
		return null;
	}

	public void kickPlayerFromChat(Player player, String username) {
		String name = "";
		for (char character : username.toCharArray()) {
			name += Utility.containsInvalidCharacter(character) ? " " : character;
		}
		synchronized (this) {
			int rank = getRank(player.getPlayerDetails().getRights().getValue(), player.getPlayerDetails().getUsername());
			if (rank < getWhoCanKickOnChat())
				return;
			Player kicked = getPlayerByDisplayName(name);
			if (kicked == null) {
				player.getPackets().sendGameMessage("This player is not this channel.");
				return;
			}
			if (rank <= getRank(kicked.getPlayerDetails().getRights().getValue(), kicked.getPlayerDetails().getUsername()))
				return;
			kicked.setCurrentFriendChat(null);
			kicked.getPlayerDetails().setCurrentFriendChatOwner(null);
			players.remove(kicked);
			bannedPlayers.put(kicked.getPlayerDetails().getUsername(), Utility.currentTimeMillis());
			kicked.getPackets().sendFriendsChatChannel();
			kicked.getPackets().sendGameMessage("You have been kicked from the friends chat channel.");
			player.getPackets()
					.sendGameMessage("You have kicked " + kicked.getPlayerDetails().getUsername() + " from friends chat channel.");
			refreshChannel();

		}
	}

	private void joinChatNoCheck(Player player) {
		synchronized (this) {
			players.add(player);
			player.setCurrentFriendChat(this);
			player.getPlayerDetails().setCurrentFriendChatOwner(owner);
			player.getPackets()
					.sendGameMessage("You are now talking in the friends chat channel " + settings.getChatName());
			refreshChannel();
		}
	}

	public void destroyChat() {
		synchronized (this) {
			for (Player player : players) {
				player.setCurrentFriendChat(null);
				player.getPlayerDetails().setCurrentFriendChatOwner(null);
				player.getPackets().sendFriendsChatChannel();
				player.getPackets().sendGameMessage("You have been removed from this channel!");
			}
		}
		synchronized (cachedFriendChats) {
			cachedFriendChats.remove(owner);
		}

	}

//	public void sendQuickMessage(Player player, QuickChatMessage message) {
//		synchronized (this) {
//			if (!player.getPlayerDetails().getUsername().equals(owner) && !settings.canTalk(player) && player.getRights() < 2) {
//				player.getPackets()
//						.sendGameMessage("You do not have a enough rank to talk on this friends chat channel.");
//				return;
//			}
//			String formatedName = Utils.formatPlayerNameForDisplay(player.getPlayerDetails().getUsername());
//			String displayName = player.getDisplayName();
//			int rights = player.getMessageIcon();
//			for (Player p2 : players)
//				p2.getPackets().receiveFriendChatQuickMessage(formatedName, displayName, rights, settings.getChatName(),
//						message);
//		}
//	}

	public void sendMessage(Player player, String message) {
		synchronized (this) {
			if (!player.getPlayerDetails().getUsername().equals(owner) && !settings.canTalk(player)
					&& !player.getPlayerDetails().getRights().isStaff()) {
				player.getPackets()
						.sendGameMessage("You do not have a enough rank to talk on this friends chat channel.");
				return;
			}
			String formatedName = Utility.formatPlayerNameForDisplay(player.getPlayerDetails().getUsername());
			String displayName = player.getDisplayName();
			int rights = player.getMessageIcon();
			for (Player p2 : players)
				p2.getPackets().receiveFriendChatMessage(formatedName, displayName, rights, settings.getChatName(),
						message);
		}
	}

	public void sendDiceMessage(Player player, String message) {
		synchronized (this) {
			if (!player.getPlayerDetails().getUsername().equals(owner) && !settings.canTalk(player)
					&& !player.getPlayerDetails().getRights().isStaff()) {
				player.getPackets()
						.sendGameMessage("You do not have a enough rank to talk on this friends chat channel.");
				return;
			}
			for (Player p2 : players) {
				p2.getPackets().sendGameMessage(message);
			}
		}
	}

	private void refreshChannel() {
		synchronized (this) {
			OutputStream stream = new OutputStream();
			stream.writeString(ownerDisplayName);
			String ownerName = Utility.formatPlayerNameForDisplay(owner);
			stream.writeByte(getOwnerDisplayName().equals(ownerName) ? 0 : 1);
			if (!getOwnerDisplayName().equals(ownerName))
				stream.writeString(ownerName);
			stream.writeLong(Utility.stringToLong(getChannelName()));
			int kickOffset = stream.getOffset();
			stream.writeByte(0);
			stream.writeByte(getPlayers().size());
			for (Player player : getPlayers()) {
				String displayName = player.getDisplayName();
				String name = Utility.formatPlayerNameForDisplay(player.getPlayerDetails().getUsername());
				stream.writeString(displayName);
				stream.writeByte(displayName.equals(name) ? 0 : 1);
				if (!displayName.equals(name))
					stream.writeString(name);
				stream.writeShort(1);
				int rank = getRank(player.getPlayerDetails().getRights().getValue(), player.getPlayerDetails().getUsername());
				stream.writeByte(rank);
				stream.writeString(Settings.SERVER_NAME);
			}
			dataBlock = new byte[stream.getOffset()];
			stream.setOffset(0);
			stream.getBytes(dataBlock, 0, dataBlock.length);
			for (Player player : players) {
				dataBlock[kickOffset] = (byte) (player.getPlayerDetails().getUsername().equals(owner) ? 0 : getWhoCanKickOnChat());
				player.getPackets().sendFriendsChatChannel();
			}
		}
	}

	public byte[] getDataBlock() {
		return dataBlock;
	}

	private FriendChatsManager(Player player) {
		owner = player.getPlayerDetails().getUsername();
		ownerDisplayName = player.getDisplayName();
		settings = player.getFriendsIgnores();
		players = new CopyOnWriteArrayList<Player>();
		bannedPlayers = new ConcurrentHashMap<String, Long>();
	}

	public static void destroyChat(Player player) {
		synchronized (cachedFriendChats) {
			FriendChatsManager chat = cachedFriendChats.get(player.getPlayerDetails().getUsername());
			if (chat == null)
				return;
			chat.destroyChat();
			player.getPackets().sendGameMessage("Your friends chat channel has now been disabled!");
		}
	}

	public static void linkSettings(Player player) {
		synchronized (cachedFriendChats) {
			FriendChatsManager chat = cachedFriendChats.get(player.getPlayerDetails().getUsername());
			if (chat == null)
				return;
			chat.settings = player.getFriendsIgnores();
		}
	}

	public static void refreshChat(Player player) {
		synchronized (cachedFriendChats) {
			FriendChatsManager chat = cachedFriendChats.get(player.getPlayerDetails().getUsername());
			if (chat == null)
				return;
			chat.refreshChannel();
		}
	}

	public static void joinChat(String ownerName, Player player) {
		synchronized (cachedFriendChats) {
			if (player.getCurrentFriendChat() != null)
				return;
			player.getPackets().sendGameMessage("Attempting to join channel...");
			String formatedName = Utility.formatPlayerNameForProtocol(ownerName);
			FriendChatsManager chat = cachedFriendChats.get(formatedName);
			if (chat == null) {
				Player owner = World.getPlayerByDisplayName(ownerName);
				if (owner == null) {
					if (!AccountCreation.exists(formatedName)) {
						player.getPackets().sendGameMessage("The channel you tried to join does not exist.");
						return;
					}
					owner = AccountCreation.loadPlayer(formatedName);
					if (owner == null) {
						player.getPackets().sendGameMessage("The channel you tried to join does not exist.");
						return;
					}
					owner.getPlayerDetails().setUsername(formatedName);
				}
				FriendsIgnores settings = owner.getFriendsIgnores();
				if (!settings.hasFriendChat()) {
					player.getPackets().sendGameMessage("The channel you tried to join does not exist.");
					return;
				}
				if (!player.getPlayerDetails().getUsername().equals(ownerName) && !settings.hasRankToJoin(player.getPlayerDetails().getUsername())
						&& !player.getPlayerDetails().getRights().isStaff()) {
					player.getPackets()
							.sendGameMessage("You do not have a enough rank to join this friends chat channel.");
					return;
				}
				chat = new FriendChatsManager(owner);
				cachedFriendChats.put(ownerName, chat);
				chat.joinChatNoCheck(player);
			} else
				chat.joinChat(player);
		}

	}

	public void kickPlayerFromFriendsChannel(Player player,String name) {
		if (player.getCurrentFriendChat() == null)
			return;
		kickPlayerFromChat(player, name);
	}

	public void sendFriendsChannelMessage(Player player,String message) {
		if (player.getCurrentFriendChat() == null)
			return;
		sendMessage(player, message);
	}
}