package com.jupiter.game.player;

import java.util.Optional;

import com.jupiter.Settings;
import com.jupiter.game.map.WorldTile;
import com.jupiter.game.player.controlers.Wilderness;
import com.jupiter.game.task.impl.ActorDeathTask;
import com.jupiter.net.encoders.other.Animation;
import com.jupiter.net.host.HostManager;
import com.jupiter.skills.Skills;

public class PlayerDeath extends ActorDeathTask<Player> {

	public PlayerDeath(Player actor) {
		super(actor);
	}

	@Override
	public void preDeath() {
		if (!getActor().getControlerManager().sendDeath())
			return;
		getActor().getMovement().lock();
		getActor().setNextAnimation(new Animation(836));
	}

	@Override
	public void death() {
		if (getActor().getPoisonDamage().get() > 0) {
			getActor().getPoisonDamage().set(0);
			getActor().getPackets().sendConfig(102, 0);
		}
		getActor().setAntifireDetails(Optional.empty());	
		getActor().getSkullTimer().set(0);
		getActor().stopAll();
//		if (getActor().getFamiliar() != null)
//			getActor().getFamiliar().sendDeath(getActor());
	}

	@Override
	public void postDeath() {
		getActor().getPackets().sendMusicEffect(90);
		getActor().getPackets().sendGameMessage("Oh dear, you have died.");
		getActor().getMovement().move(Optional.empty(), Settings.RESPAWN_PLAYER_LOCATION);
		getActor().setNextAnimation(new Animation(-1));
		getActor().heal(getActor().getMaxHitpoints());
		final int maxPrayer = getActor().getSkills().getLevelForXp(Skills.PRAYER) * 10;
		getActor().getPrayer().restorePrayer(maxPrayer);
		getActor().setNextAnimation(new Animation(Animation.RESET_ANIMATION));
		getActor().getMovement().unlock();
		getActor().getCombatDefinitions().resetSpecialAttack();
		getActor().getPrayer().closeAllPrayers();
		getActor().getMovement().setRunEnergy(100);
		
		if (getActor() instanceof Player) {
			Player killer = (Player) getActor();
			killer.setAttackedByDelay(4);
			if(HostManager.same(getActor(), killer)) {
				killer.getPackets().sendGameMessage("You don't receive any points because you and " + getActor().getDisplayName() + " are connected from the same network.");
				return;
			}
			if (getActor().getControlerManager().getControler() instanceof Wilderness) {
				if (getActor().getControlerManager().getControler() != null) {
					getActor().sendItemsOnDeath(killer);
				}
			}
		}
	}
}