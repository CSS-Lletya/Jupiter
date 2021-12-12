package com.jupiter.combat.player.specials.impl;

import java.util.Optional;

import com.jupiter.combat.npc.NPC;
import com.jupiter.combat.player.PlayerCombat;
import com.jupiter.combat.player.specials.WeaponSpecialSignature;
import com.jupiter.combat.player.specials.WeaponSpecials;
import com.jupiter.game.Entity;
import com.jupiter.game.item.ItemNames;
import com.jupiter.game.map.World;
import com.jupiter.game.player.Player;
import com.jupiter.game.player.Rights;
import com.jupiter.game.task.Task;
import com.jupiter.network.encoders.other.Animation;
import com.jupiter.network.encoders.other.Graphics;


@WeaponSpecialSignature(weapons = { ItemNames.DRAGON_SPEAR }, specAmount = 25)
public class DragonSpear implements WeaponSpecials {

	/**
	 *When this special is used, the target is pushed back one square and stunned for three seconds. This attack does not inflict damage.
	 * Does not work on enemies that take up more than one space.
	 *
	 */
	@Override
	public void execute(Player player, Entity target, PlayerCombat combat) throws Exception {
		player.getAttributes().stopAll(player);
		target.setNextGraphics(new Graphics(80, 5, 60));
		if(player.getPlayerDetails().getRights() == Rights.ADMINISTRATOR)
			player.getPackets().sendGameMessage(this.getClass().getName() + " Unfinished special, Needs sound, and testing!");


		if (!target.addWalkSteps(target.getX() - player.getX() + target.getX(), target.getY() - player.getY() + target.getY(), 1))
			player.setNextFaceEntity(target);
		target.setNextFaceEntity(player);

		target.setNextFaceEntity(null);
		player.setNextFaceEntity(null);

		if (target instanceof Player) {
			final Player defendingPlayer = (Player) target;
			defendingPlayer.getMovement().lock();
			defendingPlayer.setDisableEquip(true);
			World.get().submit(new Task(5) {
				@Override
				protected void execute() {
					defendingPlayer.setDisableEquip(false);
					defendingPlayer.getMovement().unlock();
					this.cancel();
				}
			});
		} else {
			NPC n = (NPC) target;
			n.setFreezeDelay(3000);
			n.resetCombat();
			n.setRandomWalk(false);
		}
	}

	@Override
	public Optional<Animation> getAnimation() {
		return Optional.of(new Animation(12017));
	}

	@Override
	public Optional<Graphics> getGraphics() {
		return Optional.empty();
	}

	@Override
	public Optional<Integer> getSound() {
		return Optional.empty();
	}

}