package com.jupiter.game.player.controlers;

import java.util.Optional;

import com.jupiter.game.map.WorldObject;
import com.jupiter.game.map.WorldTile;
import com.jupiter.game.player.Player;
import com.jupiter.game.task.LinkedTaskSequence;
import com.jupiter.net.encoders.other.Animation;
import com.jupiter.utils.Utils;

public class JailControler extends Controler {

	@Override
	public void start() {
		if (player.getPlayerDetails().getJailed() > Utils.currentTimeMillis())
			player.sendRandomJail(player);
	}

	@Override
	public void process() {
		if (player.getPlayerDetails().getJailed() <= Utils.currentTimeMillis()) {
			player.getControlerManager().getControler().removeControler();
			player.getMovement().move(Optional.empty(), new WorldTile(2677, 10379, 0), Optional.of("Your account has been unjailed."));
		}
	}

	public static void stopControler(Player p) {
		p.getControlerManager().getControler().removeControler();
	}

	@Override
	public boolean sendDeath() {
		LinkedTaskSequence seq = new LinkedTaskSequence();
		seq.connect(1, () -> player.setNextAnimation(new Animation(836)));
		seq.connect(3, () -> {
			player.getPackets().sendGameMessage("Oh dear, you have died.");
			player.setNextAnimation(new Animation(-1));
			player.reset();
			player.setCanPvp(false);
			player.sendRandomJail(player);
		});
		seq.start();
		return false;
	}

	@Override
	public boolean login() {

		return false;
	}

	@Override
	public boolean logout() {

		return false;
	}

	@Override
	public boolean processMagicTeleport(WorldTile toTile) {
		player.getPackets().sendGameMessage("You are currently jailed for your delinquent acts.");
		return false;
	}

	@Override
	public boolean processItemTeleport(WorldTile toTile) {
		player.getPackets().sendGameMessage("You are currently jailed for your delinquent acts.");
		return false;
	}

	@Override
	public boolean processObjectClick1(WorldObject object) {
		player.getPackets().sendGameMessage("You cannot do any activities while being jailed.");
		return false;
	}

}
