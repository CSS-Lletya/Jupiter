package com.jupiter.plugins.rsinterface.impl;

import java.util.HashMap;

import com.jupiter.combat.player.Bonus;
import com.jupiter.game.item.Item;
import com.jupiter.game.item.ItemConstants;
import com.jupiter.game.player.Equipment;
import com.jupiter.game.player.Player;
import com.jupiter.game.player.Rights;
import com.jupiter.game.player.activity.ActivityHandler;
import com.jupiter.network.decoders.WorldPacketsDecoder;
import com.jupiter.plugins.rsinterface.RSInterface;
import com.jupiter.plugins.rsinterface.RSInterfaceSignature;
import com.jupiter.skills.Skills;
import com.jupiter.utility.ChatColors;
import com.jupiter.utility.ItemExamines;
import com.jupiter.utility.Utility;

@RSInterfaceSignature(interfaceId = { 667, 670 })
public class CombatBonusesInterfacePlugin implements RSInterface {
	@Override
	public void execute(Player player, int interfaceId, int componentId, int packetId, byte slotId, int slotId2) throws Exception {
		if(interfaceId == 670)
			if (componentId == 0) {
				if (slotId >= player.getInventory().getItemsContainerSize())
					return;
				Item item = player.getInventory().getItem(slotId);
				if (item == null)
					return;
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
					if (sendWear(player, slotId, item.getId()))
						refreshEquipBonuses(player);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET)
					player.getInventory().sendExamine(slotId);
//				else if (packetId == 27) 
//					sendItemStats(player, item);
				else if (packetId == 68) {
					player.getPackets().sendGameMessage(ItemExamines.getExamine(item));
					if (item.getDefinitions().getValue() <= 1) {
						return;
					}
					player.getPackets().sendGameMessage(ChatColors.blue + "x" + Utility.format(item.getAmount()) + " "
							+ item.getName() + " value: "
							+ Utility.format(item.getDefinitions().getValue() * item.getAmount()) + "gp (HA:"
							+ Utility.format(item.getDefinitions().getHighAlchPrice() * item.getAmount()) + "gp)");
				}
			}

		if(interfaceId == 667) {
			if (componentId == 14) {
				if (slotId >= 14)
					return;
				Item item = player.getEquipment().getItem(slotId);
				if (item == null)
					return;

				if (packetId == 3) {
					player.getPackets().sendGameMessage(ItemExamines.getExamine(item));
					if (item.getDefinitions().getValue() <= 1) {
						return;
					}
					player.getPackets().sendGameMessage(ChatColors.blue + "x" + Utility.format(item.getAmount()) + " "
							+ item.getName() + " value: "
							+ Utility.format(item.getDefinitions().getValue() * item.getAmount()) + "gp (HA:"
							+ Utility.format(item.getDefinitions().getHighAlchPrice() * item.getAmount()) + "gp)");
				} else if (packetId == 216) {
					player.getEquipment().sendRemoveEquipment(slotId);
					refreshEquipBonuses(player);
				}
			}
			if (componentId == 87) {
				player.getAttributes().stopAll(player);
			}
			if (componentId == 9) {
				if (slotId >= 14)
					return;
				Item item = player.getEquipment().getItem(slotId);
				if (item == null)
					return;
				if (packetId == 81) {
					player.getPackets().sendGameMessage(ItemExamines.getExamine(item));
					if (item.getDefinitions().getValue() <= 1) {
						return;
					}
					player.getPackets().sendGameMessage(ChatColors.blue + "x" + Utility.format(item.getAmount()) + " "
							+ item.getName() + " value: "
							+ Utility.format(item.getDefinitions().getValue() * item.getAmount()) + "gp (HA:"
							+ Utility.format(item.getDefinitions().getHighAlchPrice() * item.getAmount()) + "gp)");
				}
//				if (packetId == 22) {
//					sendItemStats(player, item);
				 else if (packetId == 96) {
					EquipmentInterfacePlugin.sendRemove(player, slotId);
					player.getPackets().sendGlobalConfig(779, player.getEquipment().getWeaponRenderEmote());
					refreshEquipBonuses(player);
				}
			} else if (componentId == 46) {
				player.getBank().openBank();
//				player.getPackets().sendIComponentText(762, 47,
//						"Bank Value: " + Utils.format(player.getBank().getBankValue()) + "gp");
			}
		}
	}

	public static void refreshEquipBonuses(Player player) {
		player.getPackets().sendIComponentText(667, 28, "Stab: " + player.getCombatDefinitions().getBonus(Bonus.STAB_ATT));
		player.getPackets().sendIComponentText(667, 29, "Slash: " + player.getCombatDefinitions().getBonus(Bonus.SLASH_ATT));
		player.getPackets().sendIComponentText(667, 30, "Crush: " + player.getCombatDefinitions().getBonus(Bonus.CRUSH_ATT));
		player.getPackets().sendIComponentText(667, 31, "Magic: " + player.getCombatDefinitions().getBonus(Bonus.MAGIC_ATT));
		player.getPackets().sendIComponentText(667, 32, "Range: " + player.getCombatDefinitions().getBonus(Bonus.RANGE_ATT));
		player.getPackets().sendIComponentText(667, 33, "Stab: " + player.getCombatDefinitions().getBonus(Bonus.STAB_DEF));
		player.getPackets().sendIComponentText(667, 34, "Slash: " + player.getCombatDefinitions().getBonus(Bonus.SLASH_DEF));
		player.getPackets().sendIComponentText(667, 35, "Crush: " + player.getCombatDefinitions().getBonus(Bonus.CRUSH_DEF));
		player.getPackets().sendIComponentText(667, 36, "Magic: " + player.getCombatDefinitions().getBonus(Bonus.MAGIC_DEF));
		player.getPackets().sendIComponentText(667, 37, "Range: " + player.getCombatDefinitions().getBonus(Bonus.RANGE_DEF));
		player.getPackets().sendIComponentText(667, 38, "Summoning: " + player.getCombatDefinitions().getBonus(Bonus.SUMM_DEF));
		player.getPackets().sendIComponentText(667, 39, "Absorb Melee: " + player.getCombatDefinitions().getBonus(Bonus.ABSORB_MELEE) + "%");
		player.getPackets().sendIComponentText(667, 40, "Absorb Magic: " + player.getCombatDefinitions().getBonus(Bonus.ABSORB_MAGIC) + "%");
		player.getPackets().sendIComponentText(667, 41, "Absorb Ranged: " + player.getCombatDefinitions().getBonus(Bonus.ABSORB_RANGE) + "%");
		player.getPackets().sendIComponentText(667, 42, "Strength: " + player.getCombatDefinitions().getBonus(Bonus.MELEE_STR));
		player.getPackets().sendIComponentText(667, 43, "Ranged Str: " + player.getCombatDefinitions().getBonus(Bonus.RANGE_STR));
		player.getPackets().sendIComponentText(667, 44, "Prayer: " + player.getCombatDefinitions().getBonus(Bonus.PRAYER));
		player.getPackets().sendIComponentText(667, 45, "Magic Damage: " + player.getCombatDefinitions().getBonus(Bonus.MAGIC_STR) + "%");
	}

	@SuppressWarnings("unused")
	public static boolean sendWear(Player player, byte slotId, int itemId) {

		if (player.hasFinished() || player.isDead())
			return false;
		player.getAttributes().stopAll(player, false, false);
		Item item = player.getInventory().getItem(slotId);
		String itemName = item.getDefinitions() == null ? "" : item.getDefinitions().getName().toLowerCase();
		if (item == null || item.getId() != itemId)
			return false;
		if (item.getDefinitions().isNoted() || !item.getDefinitions().isWearItem(player.getAppearance().isMale())) {
			player.getPackets().sendGameMessage("You can't wear that.");
			return true;
		}
		byte targetSlot = Equipment.getItemSlot(itemId);
		if (targetSlot == -1) {
			player.getPackets().sendGameMessage("You can't wear that.");
			return true;
		}
		if (!ItemConstants.canWear(item, player))
			return true;
		boolean isTwoHandedWeapon = targetSlot == 3 && Equipment.isTwoHandedWeapon(item);
		if (isTwoHandedWeapon && !player.getInventory().hasFreeSlots() && player.getEquipment().hasShield()) {
			player.getPackets().sendGameMessage("Not enough free space in your inventory.");
			return true;
		}
		HashMap<Integer, Integer> requiriments = item.getDefinitions().getWearingSkillRequiriments();
		boolean hasRequiriments = true;
		if (requiriments != null) {
			for (int skillId : requiriments.keySet()) {
				if (skillId > 24 || skillId < 0)
					continue;
				int level = requiriments.get(skillId);
				if (level < 0 || level > 120)
					continue;
				if (player.getSkills().getLevelForXp(skillId) < level) {
					if (hasRequiriments) {
						player.getPackets().sendGameMessage("You are not high enough level to use this item.");
					}
					hasRequiriments = false;
					String name = Skills.SKILL_NAME[skillId].toLowerCase();
					player.getPackets().sendGameMessage("You need to have a" + (name.startsWith("a") ? "n" : "") + " "
							+ name + " level of " + level + ".");
				}

			}
		}
		if (!hasRequiriments)
			return true;
		if (ActivityHandler.execute(player, activity -> !activity.canEquip(player, targetSlot, itemId)))
			return false;
		player.getAttributes().stopAll(player, false, false);
		player.getInventory().deleteItem(slotId, item);
		if (targetSlot == 3) {
			if (isTwoHandedWeapon && player.getEquipment().getItem(5) != null) {
				if (!player.getInventory().addItem(player.getEquipment().getItem(5).getId(),
						player.getEquipment().getItem(5).getAmount())) {
					player.getInventory().getItems().set(slotId, item);
					player.getInventory().refresh(slotId);
					return true;
				}
				player.getEquipment().getItems().set(5, null);
			}
		} else if (targetSlot == 5) {
			if (player.getEquipment().getItem(3) != null
					&& Equipment.isTwoHandedWeapon(player.getEquipment().getItem(3))) {
				if (!player.getInventory().addItem(player.getEquipment().getItem(3).getId(),
						player.getEquipment().getItem(3).getAmount())) {
					player.getInventory().getItems().set(slotId, item);
					player.getInventory().refresh(slotId);
					return true;
				}
				player.getEquipment().getItems().set(3, null);
			}

		}
		if (player.getEquipment().getItem(targetSlot) != null
				&& (itemId != player.getEquipment().getItem(targetSlot).getId()
				|| !item.getDefinitions().isStackable())) {
			if (player.getInventory().getItems().get(slotId) == null) {
				player.getInventory().getItems().set(slotId, new Item(player.getEquipment().getItem(targetSlot).getId(),
						player.getEquipment().getItem(targetSlot).getAmount()));
				player.getInventory().refresh(slotId);
			} else
				player.getInventory().addItem(new Item(player.getEquipment().getItem(targetSlot).getId(),
						player.getEquipment().getItem(targetSlot).getAmount()));
			player.getEquipment().getItems().set(targetSlot, null);
		}
		if (targetSlot == Equipment.SLOT_AURA)
			player.getAuraManager().removeAura();
		int oldAmt = 0;
		if (player.getEquipment().getItem(targetSlot) != null) {
			oldAmt = player.getEquipment().getItem(targetSlot).getAmount();
		}
		Item item2 = new Item(itemId, oldAmt + item.getAmount());
		player.getEquipment().getItems().set(targetSlot, item2);
		player.getEquipment().refresh(targetSlot, targetSlot == 3 ? (byte) 5 : targetSlot == 3 ? (byte) 0 : (byte) 3);
		player.getAppearance().generateAppearenceData();
		player.getPackets().sendSound(2240, 0, 1);
		return true;
	}

	static int finalSlot;
	public static boolean sendWear2(Player player, int slotId, int itemId) {
		if (player.hasFinished() || player.isDead())
			return false;
		player.getAttributes().stopAll(player, false, false);
		Item item = player.getInventory().getItem(slotId);
		if (item == null || item.getId() != itemId)
			return false;
		if ((itemId == 4565 || itemId == 4084) && !player.getPlayerDetails().getRights().equal(Rights.ADMINISTRATOR)) {
			player.getPackets().sendGameMessage("You've to be a administrator to wear this item.");
			return true;
		}
		if (item.getDefinitions().isNoted()
				|| !item.getDefinitions().isWearItem(player.getAppearance().isMale()) && itemId != 4084) {
			player.getPackets().sendGameMessage("You can't wear that.");
			return false;
		}
		byte targetSlot = Equipment.getItemSlot(itemId);
		finalSlot = targetSlot;
		if (itemId == 4084)
			targetSlot = 3;
		if (targetSlot == -1) {
			player.getPackets().sendGameMessage("You can't wear that.");
			return false;
		}
		if (!ItemConstants.canWear(item, player))
			return false;
		boolean isTwoHandedWeapon = targetSlot == 3 && Equipment.isTwoHandedWeapon(item);
		if (isTwoHandedWeapon && !player.getInventory().hasFreeSlots() && player.getEquipment().hasShield()) {
			player.getPackets().sendGameMessage("Not enough free space in your inventory.");
			return false;
		}
		HashMap<Integer, Integer> requiriments = item.getDefinitions().getWearingSkillRequiriments();
		boolean hasRequiriments = true;
		if (requiriments != null) {
			for (int skillId : requiriments.keySet()) {
				if (skillId > 24 || skillId < 0)
					continue;
				int level = requiriments.get(skillId);
				if (level < 0 || level > 120)
					continue;
				if (player.getSkills().getLevelForXp(skillId) < level) {
					if (hasRequiriments)
						player.getPackets().sendGameMessage("You are not high enough level to use this item.");
					hasRequiriments = false;
					String name = Skills.SKILL_NAME[skillId].toLowerCase();
					player.getPackets().sendGameMessage("You need to have a" + (name.startsWith("a") ? "n" : "") + " "
							+ name + " level of " + level + ".");
				}

			}
		}
		if (!hasRequiriments)
			return false;
		if (ActivityHandler.execute(player, activity -> !activity.canEquip(player, finalSlot, itemId)))
			return false;
		player.getInventory().getItems().remove(slotId, item);
		if (targetSlot == 3) {
			if (isTwoHandedWeapon && player.getEquipment().getItem(5) != null) {
				if (!player.getInventory().getItems().add(player.getEquipment().getItem(5))) {
					player.getInventory().getItems().set(slotId, item);
					return false;
				}
				player.getEquipment().getItems().set(5, null);
			}
		} else if (targetSlot == 5) {
			if (player.getEquipment().getItem(3) != null
					&& Equipment.isTwoHandedWeapon(player.getEquipment().getItem(3))) {
				if (!player.getInventory().getItems().add(player.getEquipment().getItem(3))) {
					player.getInventory().getItems().set(slotId, item);
					return false;
				}
				player.getEquipment().getItems().set(3, null);
			}

		}
		if (player.getEquipment().getItem(targetSlot) != null
				&& (itemId != player.getEquipment().getItem(targetSlot).getId()
				|| !item.getDefinitions().isStackable())) {
			if (player.getInventory().getItems().get(slotId) == null) {
				player.getInventory().getItems().set(slotId, new Item(player.getEquipment().getItem(targetSlot).getId(),
						player.getEquipment().getItem(targetSlot).getAmount()));
			} else
				player.getInventory().getItems().add(new Item(player.getEquipment().getItem(targetSlot).getId(),
						player.getEquipment().getItem(targetSlot).getAmount()));
			player.getEquipment().getItems().set(targetSlot, null);
		}
		if (targetSlot == Equipment.SLOT_AURA)
			player.getAuraManager().removeAura();
		int oldAmt = 0;
		if (player.getEquipment().getItem(targetSlot) != null) {
			oldAmt = player.getEquipment().getItem(targetSlot).getAmount();
		}
		Item item2 = new Item(itemId, oldAmt + item.getAmount());
		player.getEquipment().getItems().set(targetSlot, item2);
		player.getEquipment().refresh(targetSlot, targetSlot == 3 ? (byte) 5 : targetSlot == 3 ? (byte) 0 : (byte) 3);
		return true;
	}

	public static void sendWear(Player player, int[] slotIds) {
		if (player.hasFinished() || player.isDead())
			return;
		boolean worn = false;
		Item[] copy = player.getInventory().getItems().getItemsCopy();
		for (int slotId : slotIds) {
			Item item = player.getInventory().getItem(slotId);
			if (item == null)
				continue;
			if (sendWear2(player, slotId, item.getId()))
				worn = true;
		}
		player.getInventory().refreshItems(copy);
		if (worn) {
			player.getAppearance().generateAppearenceData();
			player.getPackets().sendSound(2240, 0, 1);
		}
	}

//	public static void sendItemStats(final Player player, Item item) {
//		StringBuilder b = new StringBuilder();
//		if (item.getId() == 772)
//			return;
//		boolean hasBonuses = ItemBonuses.getItemBonuses(item.getId()) != null;
//		for (int i = 0; i < 17; i++) {
//			int bonus = hasBonuses ? ItemBonuses.getItemBonuses(item.getId())[i] : 0;
//			String label = CombatDefinitions.BONUS_LABELS[i];
//			String sign = bonus > 0 ? "+" : "";
//			if (bonus == 16) {
//				continue;
//			}
//			b.append(label + ": " + (sign + bonus) + ((label == "Magic Damage" || label == "Absorb Melee"
//					|| label == "Absorb Magic" || label == "Absorb Ranged") ? "%" : "") + "<br>");
//		}
//		player.getPackets().sendGlobalString(321, "Stats for " + item.getName());
//		player.getPackets().sendGlobalString(324, b.toString());
//		player.getPackets().sendHideIComponent(667, 49, false);
//		player.setCloseInterfacesEvent(new Runnable() {
//			@Override
//			public void run() {
//				player.getPackets().sendGlobalString(321, "");
//				player.getPackets().sendGlobalString(324, "");
//				player.getPackets().sendHideIComponent(667, 49, true);
//			}
//		});
//	}
}