package com.jupiter.game.player.activity.impl;

import java.util.Optional;

import com.jupiter.combat.npc.NPC;
import com.jupiter.combat.player.Combat;
import com.jupiter.combat.player.type.CombatEffectType;
import com.jupiter.game.Entity;
import com.jupiter.game.map.WorldObject;
import com.jupiter.game.map.WorldTile;
import com.jupiter.game.player.Player;
import com.jupiter.game.player.activity.Activity;
import com.jupiter.skills.Skills;
import com.jupiter.utility.Utility;

public class WildernessActivity extends Activity {

	public WildernessActivity() {
		super("WILDERNESS", ActivitySafety.DANGEROUS, ActivityType.NORMAL);
	}

	@Override
	public void start(Player player) {
		checkBoosts(player);
		sendInterfaces(player);
		moved(player);
	}

	@Override
	public boolean contains(Player player) {
		return true;
	}

	@Override
	public void login(Player player) {
		moved(player);
	}

	@Override
	public boolean keepCombating(Player player,Entity target) {
		if (target instanceof NPC)
			return true;
		if (!canAttack(player, target))
			return false;
		if (target.getAttackedBy() != player && player.getAttackedBy() != target)
			Combat.effect(player, CombatEffectType.SKULL);
		if (player.getCombatDefinitions().getSpellId() <= 0
				&& Utility.inCircle(new WorldTile(3105, 3933, 0), target, 24)) {
			player.getPackets().sendGameMessage("You can only use magic in the arena.");
			return false;
		}
		return true;
	}

	@Override
	public boolean canAttack(Player player, Entity target) {
		if (target instanceof Player) {
			Player p2 = (Player) target;
			if (player.isCanPvp() && !p2.isCanPvp()) {
				player.getPackets().sendGameMessage("That player is not in the wilderness.");
				return false;
			}
			if (canHit(player, target))
				return true;
			return false;
		}
		return true;
	}

	@Override
	public boolean canHit(Player player, Entity target) {
		if (target instanceof NPC)
			return true;
		Player p2 = (Player) target;
		if (Math.abs(player.getSkills().getCombatLevel() - p2.getSkills().getCombatLevel()) > getWildLevel(player))
			return false;
		return true;
	}

	@Override
	public boolean processMagicTeleport(Player player, WorldTile toTile) {
		if (getWildLevel(player) > 20 || player.getTeleBlockDelay() > 0) {
			player.getPackets().sendGameMessage("A mysterious force prevents you from teleporting.");
			return false;
		}
		return true;

	}

	@Override
	public boolean processItemTeleport(Player player, WorldTile toTile) {
		if (getWildLevel(player) > 30 || player.getTeleBlockDelay() > 0) {
			player.getPackets().sendGameMessage("A mysterious force prevents you from teleporting.");
			return false;
		}
		return true;
	}

	@Override
	public boolean processObjectTeleport(Player player, WorldTile toTile) {
		if (player.getTeleBlockDelay() > 0) {
			player.getPackets().sendGameMessage("A mysterious force prevents you from teleporting.");
			return false;
		}
		return true;
	}

	@Override
	public boolean processObjectClick1(Player player, final WorldObject object) {
		if (object.getId() == 2557 || object.getId() == 65717) {
			player.getPackets().sendGameMessage("It seems it is locked, maybe you should try something else.");
			return false;
		}
		return true;
	}

	@Override
	public void sendInterfaces(Player player) {
		if (isAtWild(player))
			showSkull(player);
	}

	@Override
	public void magicTeleported(Player player, int teleType) {
		if (!isAtWild(player.getNextWorldTile())) {
			player.setCanPvp(false);
			removeIcon(player);
			player.setCurrentActivity(Optional.empty());
		}
		player.getInterfaceManager().closeFixedOverlay();
	}

	@Override
	public void moved(Player player) {
		boolean isAtWild = isAtWild(player);
		boolean isAtWildSafe = isAtWildSafe(player);
		if (!showingSkull && isAtWild && !isAtWildSafe) {
			showingSkull = true;
			player.setCanPvp(true);
			showSkull(player);
			player.getAppearance().generateAppearenceData();
		} else if (showingSkull && (isAtWildSafe || !isAtWild)) {
			removeIcon(player);
		} else if (!isAtWildSafe && !isAtWild) {
			player.setCanPvp(false);
			removeIcon(player);
			player.setCurrentActivity(Optional.empty());
		}
	}

	@Override
	public void forceClose(Player player) {
		removeIcon(player);
	}

	public static final boolean isAtWild(WorldTile tile) {// TODO fix this
		return (tile.getX() >= 3011 && tile.getX() <= 3132 && tile.getY() >= 10052 && tile.getY() <= 10175) // fortihrny
				// dungeon
				|| (tile.getX() >= 2940 && tile.getX() <= 3395 && tile.getY() >= 3525 && tile.getY() <= 4000)
				|| (tile.getX() >= 3264 && tile.getX() <= 3279 && tile.getY() >= 3279 && tile.getY() <= 3672)
				|| (tile.getX() >= 2756 && tile.getX() <= 2875 && tile.getY() >= 5512 && tile.getY() <= 5627)
				|| (tile.getX() >= 3158 && tile.getX() <= 3181 && tile.getY() >= 3679 && tile.getY() <= 3697)
				|| (tile.getX() >= 3280 && tile.getX() <= 3183 && tile.getY() >= 3885 && tile.getY() <= 3888)
				|| (tile.getX() >= 3012 && tile.getX() <= 3059 && tile.getY() >= 10303 && tile.getY() <= 10351);
	}

	public static boolean isAtWildSafe(Player player) {
		player.getInterfaceManager().closeFixedOverlay();
		return (player.getX() >= 2940 && player.getX() <= 3395 && player.getY() <= 3524 && player.getY() >= 3523);
	}
	
	public static boolean isInideWilderness(Player player) {
		return WildernessActivity.isAtWild(player) && !WildernessActivity.isAtWildSafe(player);
	}

	public int getWildLevel(Player player) {
		if (player.getY() > 9900)
			return (player.getY() - 9920) / 8 + 1;
		return (player.getY() - 3520) / 8 + 1;
	}
	
	private transient boolean showingSkull;
	
	public static void checkBoosts(Player player) {
		boolean changed = false;
		int level = player.getSkills().getLevelForXp(Skills.ATTACK);
		int maxLevel = (int) (level + 5 + (level * 0.15));
		if (maxLevel < player.getSkills().getLevel(Skills.ATTACK)) {
			player.getSkills().set(Skills.ATTACK, maxLevel);
			changed = true;
		}
		level = player.getSkills().getLevelForXp(Skills.STRENGTH);
		maxLevel = (int) (level + 5 + (level * 0.15));
		if (maxLevel < player.getSkills().getLevel(Skills.STRENGTH)) {
			player.getSkills().set(Skills.STRENGTH, maxLevel);
			changed = true;
		}
		level = player.getSkills().getLevelForXp(Skills.DEFENCE);
		maxLevel = (int) (level + 5 + (level * 0.15));
		if (maxLevel < player.getSkills().getLevel(Skills.DEFENCE)) {
			player.getSkills().set(Skills.DEFENCE, maxLevel);
			changed = true;
		}
		level = player.getSkills().getLevelForXp(Skills.RANGE);
		maxLevel = (int) (level + 5 + (level * 0.1));
		if (maxLevel < player.getSkills().getLevel(Skills.RANGE)) {
			player.getSkills().set(Skills.RANGE, maxLevel);
			changed = true;
		}
		level = player.getSkills().getLevelForXp(Skills.MAGIC);
		maxLevel = level + 5;
		if (maxLevel < player.getSkills().getLevel(Skills.MAGIC)) {
			player.getSkills().set(Skills.MAGIC, maxLevel);
			changed = true;
		}
		if (changed)
			player.getPackets().sendGameMessage("Your extreme potion bonus has been reduced.");
	}
	
	public void removeIcon(Player player) {
		if (showingSkull) {
			showingSkull = false;
			player.setCanPvp(false);
			player.setCurrentActivity(Optional.empty());
			player.getAppearance().generateAppearenceData();
			player.getEquipment().refresh(null);
		}
	}
	
	public void showSkull(Player player) {
		player.getInterfaceManager().sendOverlay(381, false);
	}

	public static boolean isDitch(int id) {
		return id >= 1440 && id <= 1444 || id >= 65076 && id <= 65087;
	}
	
	
	public void setCanPvp(Player player, boolean canPvp) {
		player.setCanPvp(canPvp);
		player.getAppearance().getAppeareanceBlocks();
		player.getPackets().sendPlayerOption(canPvp ? "Attack" : "null", 1, true);
		player.getPackets().sendPlayerUnderNPCPriority(canPvp);
	}
}