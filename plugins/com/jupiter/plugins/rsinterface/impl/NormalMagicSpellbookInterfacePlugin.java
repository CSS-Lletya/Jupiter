package com.jupiter.plugins.rsinterface.impl;

import com.jupiter.game.player.Player;
import com.jupiter.plugins.rsinterface.RSInterface;
import com.jupiter.plugins.rsinterface.RSInterfaceSignature;
import com.jupiter.skills.magic.Magic;

@RSInterfaceSignature(interfaceId = {192})
public class NormalMagicSpellbookInterfacePlugin implements RSInterface {

	@Override
	public void execute(Player player, int interfaceId, int componentId, int packetId, byte slotId, int slotId2) throws Exception {
		if (componentId == 2)
			player.getCombatDefinitions().switchDefensiveCasting();
		else if (componentId == 7)
			player.getCombatDefinitions().switchShowCombatSpells();
		else if (componentId == 9)
			player.getCombatDefinitions().switchShowTeleportSkillSpells();
		else if (componentId == 11)
			player.getCombatDefinitions().switchShowMiscallaneousSpells();
		else if (componentId == 13)
			player.getCombatDefinitions().switchShowSkillSpells();
		else if (componentId >= 15 & componentId <= 17)
			player.getCombatDefinitions().setSortSpellBook(componentId - 15);
		else
			Magic.processNormalSpell(player, componentId, packetId);
	}
}