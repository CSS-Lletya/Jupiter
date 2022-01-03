package com.jupiter.network.packets.outgoing.impl;

import com.jupiter.cache.io.InputStream;
import com.jupiter.game.player.Player;
import com.jupiter.network.packets.outgoing.OutgoingPacket;
import com.jupiter.network.packets.outgoing.OutgoingPacketSignature;
import com.jupiter.utility.IntegerInputAction;

@OutgoingPacketSignature(packetId = 58, description = "Represents a Input state")
public class EnterIntegerPacket implements OutgoingPacket {

	@Override
	public void execute(Player player, InputStream stream) {
		if (!player.isActive() || player.isDead())
			return;
		int value = stream.readInt();
		if (player.getTemporaryAttributtes().get("integer_input_action") != null) {
			IntegerInputAction action = (IntegerInputAction) player.getTemporaryAttributtes()
					.remove("integer_input_action");
			action.handle(value);
			return;
		}
		if ((player.getInterfaceManager().containsInterface(762) && player.getInterfaceManager().containsInterface(763))
				|| player.getInterfaceManager().containsInterface(11)) {
			if (value < 0)
				return;
			Integer bank_item_X_Slot = (Integer) player.getTemporaryAttributtes().remove("bank_item_X_Slot");
			if (bank_item_X_Slot == null)
				return;
			player.getBank().setLastX(value);
			player.getBank().refreshLastX();
			if (player.getTemporaryAttributtes().remove("bank_isWithdraw") != null)
				player.getBank().withdrawItem(bank_item_X_Slot, value);
			else
				player.getBank().depositItem(bank_item_X_Slot, value,
						player.getInterfaceManager().containsInterface(11) ? false : true);
		} else if (player.getInterfaceManager().containsInterface(206)
				&& player.getInterfaceManager().containsInterface(207)) {
			if (value < 0)
				return;
			Integer pc_item_X_Slot = (Integer) player.getTemporaryAttributtes().remove("pc_item_X_Slot");
			if (pc_item_X_Slot == null)
				return;
			if (player.getTemporaryAttributtes().remove("pc_isRemove") != null)
				player.getPriceCheckManager().removeItem(pc_item_X_Slot, value);
			else
				player.getPriceCheckManager().addItem(pc_item_X_Slot, value);
		} else if (player.getInterfaceManager().containsInterface(671)
				&& player.getInterfaceManager().containsInterface(665)) {
//			if (player.getFamiliar() == null || player.getFamiliar().getBob() == null)
//				return;
//			if (value < 0)
//				return;
//			Integer bob_item_X_Slot = (Integer) player.getTemporaryAttributtes().remove("bob_item_X_Slot");
//			if (bob_item_X_Slot == null)
//				return;
//			if (player.getTemporaryAttributtes().remove("bob_isRemove") != null)
//				player.getFamiliar().getBob().removeItem(bob_item_X_Slot, value);
//			else
//				player.getFamiliar().getBob().addItem(bob_item_X_Slot, value);
		} else if (player.getInterfaceManager().containsInterface(335)
				&& player.getInterfaceManager().containsInterface(336)) {
			if (value < 0)
				return;
			Integer trade_item_X_Slot = (Integer) player.getTemporaryAttributtes().remove("trade_item_X_Slot");
			if (trade_item_X_Slot == null)
				return;
			if (player.getTemporaryAttributtes().remove("trade_isRemove") != null)
				player.getTrade().removeItem(trade_item_X_Slot, value);
			else
				player.getTrade().addItem(trade_item_X_Slot, value);
		}
	}
}