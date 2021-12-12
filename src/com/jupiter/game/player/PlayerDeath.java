package com.jupiter.game.player;

import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import com.jupiter.Settings;
import com.jupiter.game.item.FloorItem;
import com.jupiter.game.item.Item;
import com.jupiter.game.player.activity.Activity.ActivitySafety;
import com.jupiter.game.player.activity.ActivityHandler;
import com.jupiter.game.task.impl.ActorDeathTask;
import com.jupiter.network.encoders.other.Animation;
import com.jupiter.network.host.HostManager;
import com.jupiter.skills.Skills;
import com.jupiter.skills.prayer.Prayer;

public class PlayerDeath extends ActorDeathTask<Player> {

	public PlayerDeath(Player actor) {
		super(actor);
	}

	@Override
	public void preDeath() {
		if (!ActivityHandler.execute(getActor(), activity -> activity.sendDeath(getActor())))
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
		getActor().getAttributes().stopAll(getActor());
	}

	@Override
	public void postDeath() {
		getActor().getPackets().sendMusicEffect(90);
		getActor().getPackets().sendGameMessage("Oh dear, you have died.");
		getActor().setNextWorldTile(Settings.RESPAWN_PLAYER_LOCATION);
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
			getActor().getCurrentActivity().ifPresent(p -> {
				if (getActor().getCurrentActivity().get().getSafety() == ActivitySafety.DANGEROUS) {
					sendItemsOnDeath(getActor(), killer);
				}
			});
		}
	}
	
	public void sendItemsOnDeath(Player player, Player killer) {
//		if (getRights().isStaff())
//			return;
		player.getAuraManager().removeAura();
		CopyOnWriteArrayList<Item> containedItems = new CopyOnWriteArrayList<Item>();
		for (int i = 0; i < 14; i++) {
			if (player.getEquipment().getItem(i) != null && player.getEquipment().getItem(i).getId() != -1
					&& player.getEquipment().getItem(i).getAmount() != -1)
				containedItems.add(new Item(player.getEquipment().getItem(i).getId(), player.getEquipment().getItem(i).getAmount()));
		}
		for (int i = 0; i < 28; i++) {
			if (player.getInventory().getItem(i) != null && player.getInventory().getItem(i).getId() != -1
					&& player.getInventory().getItem(i).getAmount() != -1)
				containedItems.add(new Item(player.getInventory().getItem(i).getId(), player.getInventory().getItem(i).getAmount()));
		}
		if (containedItems.isEmpty())
			return;
		int keptAmount = 0;
//		if (!(getControlerManager().getControler() instanceof CorpBeastControler)) {
			keptAmount = player.getCombatDefinitions().hasSkull() ? 0 : 3;
			if (player.getPrayer().active(Prayer.PROTECT_ITEM_C) || player.getPrayer().active(Prayer.PROTECT_ITEM_N))
				keptAmount++;
//		}
		CopyOnWriteArrayList<Item> keptItems = new CopyOnWriteArrayList<Item>();
		Item lastItem = new Item(1, 1);
		for (int i = 0; i < keptAmount; i++) {
			for (Item item : containedItems) {
				int price = item.getDefinitions().getValue();
				if (price >= lastItem.getDefinitions().getValue()) {
					lastItem = item;
				}
			}
			keptItems.add(lastItem);
			containedItems.remove(lastItem);
			lastItem = new Item(1, 1);
		}
		player.getInventory().reset();
		player.getEquipment().reset();
		for (Item item : keptItems) {
			player.getInventory().addItem(item);
		}
		/** This Checks which items that is listed in the 'PROTECT_ON_DEATH' **/
		for (Item item : containedItems) {	// This checks the items you had in your inventory or equipped
			for (String string : Settings.PROTECT_ON_DEATH) {	//	This checks the matched items from the list 'PROTECT_ON_DEATH'
				if (item.getDefinitions().getName().toLowerCase().contains(string) || item.getDefinitions().exchangableItem) {
					player.getInventory().addItem(item);	//	This adds the items that is matched and listed in 'PROTECT_ON_DEATH'
					containedItems.remove(item);	//	This remove the whole list of the contained items that is matched
				}
			}
		}

		/** This to avoid items to be dropped in the list 'PROTECT_ON_DEATH' **/
		for (Item item : containedItems) {	//	This checks the items you had in your inventory or equipped
			for (String string : Settings.PROTECT_ON_DEATH) {	//	This checks the matched items from the list 'PROTECT_ON_DEATH'
				if (item.getDefinitions().getName().toLowerCase().contains(string)) {
					containedItems.remove(item);	//	This remove the whole list of the contained items that is matched
				}
			}
			FloorItem.createGroundItem(item, player.getLastWorldTile(), killer == null ? player : killer, false, 180, true, true);	//	This dropps the items to the killer, and is showed for 180 seconds
		}
		for (Item item : containedItems) {
			FloorItem.createGroundItem(item, player.getLastWorldTile(), killer == null ? player : killer, false, 180, true, true);
		}
	}
}