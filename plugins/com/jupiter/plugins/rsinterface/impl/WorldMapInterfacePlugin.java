package com.jupiter.plugins.rsinterface.impl;

import com.jupiter.game.player.Player;
import com.jupiter.game.player.actions.Rest;
import com.jupiter.network.decoders.WorldPacketsDecoder;
import com.jupiter.plugins.rsinterface.RSInterface;
import com.jupiter.plugins.rsinterface.RSInterfaceSignature;
import com.jupiter.utility.Utility;

@RSInterfaceSignature(interfaceId = { 548, 746, 750, 749, 755, 1214})
public class WorldMapInterfacePlugin implements RSInterface {

	@Override
	public void execute(Player player, int interfaceId, int componentId, int packetId, byte slotId, int slotId2) throws Exception {
		if ((interfaceId == 548 && componentId == 35) || (interfaceId == 746 && componentId == 55)) {
			if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
				player.getSkills().switchXPDisplay();
			else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
				player.getSkills().switchXPPopup();
			else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
				player.getSkills().setupXPCounter();
		} else if ((interfaceId == 746 && componentId == 208) || (interfaceId == 548 && componentId == 167)) {
			if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET) {
				if (player.getInterfaceManager().containsScreenInter()) {
					player.getPackets()
							.sendGameMessage("Please finish what you're doing before opening the price checker.");
					return;
				}
				player.getAttributes().stopAll(player);
				player.getPriceCheckManager().openPriceCheck();
			}
			if (packetId == 96) {
				System.out.println("Toggle money pouch");
			}
			if (packetId == 27) {
				System.out.println("Withdraw money pouch");
			}
			if (packetId == 68) {
				System.out.println("Examine money pouch");
			}
		}
		if (interfaceId == 548 || interfaceId == 746) {
			if ((interfaceId == 548 && componentId == 157) || (interfaceId == 746 && componentId == 200 && packetId == 96)) {
				if (player.getInterfaceManager().containsScreenInter()
						|| player.getInterfaceManager().containsInventoryInter()) {
					// TODO cant open sound
					player.getPackets()
							.sendGameMessage("Please finish what you're doing before opening the world map.");
					return;
				}
				// world map open
				player.getPackets().sendWindowsPane(755, 0);
				int posHash = player.getX() << 14 | player.getY();
				player.getPackets().sendGlobalConfig(622, posHash); // map open
				// center
				// pos
				player.getPackets().sendGlobalConfig(674, posHash); // player
				// position
			} 
			if (packetId == 27) {
				System.out.println("Clear World map marker");
			}
			else if (componentId == 217 && interfaceId == 548){
				player.getPackets().sendOpenURL("https://support.runescape.com/hc/en-gb");
				player.getPackets()
				.sendGameMessage("support.");
			}
			
			else if ((interfaceId == 548 && componentId == 35) || (interfaceId == 746 && componentId == 54)) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET)
					player.getSkills().switchXPDisplay();
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET)
					player.getSkills().switchXPPopup();
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON3_PACKET)
					player.getSkills().setupXPCounter();
			} else if ((interfaceId == 746 && componentId == 167) || (interfaceId == 548 && componentId == 159)) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON4_PACKET) {
					if (player.getInterfaceManager().containsScreenInter()) {
						player.getPackets()
								.sendGameMessage("Please finish what you're doing before opening the price checker.");
						return;
					}
					player.getAttributes().stopAll(player);
					player.getPriceCheckManager().openPriceCheck();
				}
			}
		}
		if (interfaceId == 755) {
			if (componentId == 44)
				player.getPackets().sendWindowsPane(player.getInterfaceManager().hasRezizableScreen() ? 746 : 548, 2);
			else if (componentId == 42) {
				player.getHintIconsManager().removeAll();// TODO find hintIcon index
				player.getPackets().sendConfig(1159, 1);
			}
		}
		if (interfaceId == 749) {
			if (componentId == 4) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) // activate
					player.getPrayer().switchQuickPrayers();
				else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) // switch
					player.getPrayer().switchSettingQuickPrayer();
			}
		} else if (interfaceId == 750) {
			if (componentId == 4) {
				if (packetId == WorldPacketsDecoder.ACTION_BUTTON1_PACKET) {
					player.getMovement().toogleRun(!player.isResting());
					if (player.isResting())
						player.getAttributes().stopAll(player);
				} else if (packetId == WorldPacketsDecoder.ACTION_BUTTON2_PACKET) {
					if (player.isResting()) {
						player.getAttributes().stopAll(player);
						return;
					}
					long currentTime = Utility.currentTimeMillis();
					if (player.getNextEmoteEnd() >= currentTime) {
						player.getPackets().sendGameMessage("You can't rest while perfoming an emote.");
						return;
					}
					if (player.getMovement().getLockDelay() >= currentTime) {
						player.getPackets().sendGameMessage("You can't rest while perfoming an action.");
						return;
					}
					player.getAttributes().stopAll(player);
					player.getActionManager().setAction(new Rest());
				}
			}
		}
		if (interfaceId == 1214) {
			player.getSkills().handleSetupXPCounter(componentId);
		}
	}
}