package com.jupiter.combat.npc.combat.scripts.impl;

import com.jupiter.combat.npc.NPC;
import com.jupiter.combat.npc.combat.NPCCombatDefinitions;
import com.jupiter.combat.npc.combat.scripts.MobCombatInterface;
import com.jupiter.combat.npc.combat.scripts.MobCombatSignature;
import com.jupiter.game.map.World;
import com.jupiter.game.map.WorldTile;
import com.jupiter.game.player.Player;
import com.jupiter.game.task.Task;
import com.jupiter.network.encoders.other.Animation;
import com.jupiter.network.encoders.other.Graphics;
import com.jupiter.utility.RandomUtility;

@MobCombatSignature(mobId = {}, mobName = {"Giant Mole"})
public class GiantMoleCombat extends MobCombatInterface {

	private static final WorldTile[] COORDS = { new WorldTile(1737, 5228, 0), new WorldTile(1751, 5233, 0),
			new WorldTile(1778, 5237, 0), new WorldTile(1736, 5227, 0), new WorldTile(1780, 5152, 0),
			new WorldTile(1758, 5162, 0), new WorldTile(1745, 5169, 0), new WorldTile(1760, 5183, 0) };
	
	@Override
	public int execute(Player target, NPC npc) throws Exception {
		NPCCombatDefinitions defs = npc.getCombatDefinitions();
		if (RandomUtility.random(5) == 0) { // bury
			npc.setNextAnimation(new Animation(3314));
			npc.setCantInteract(true);
			npc.getCombat().removeTarget();
			final Player player = (Player) (target instanceof Player ? target : null);
			if (player != null)
				player.getInterfaceManager().sendTab(player.getInterfaceManager().hasRezizableScreen() ? 1 : 11, 226);
			final WorldTile middle = npc.getMiddleWorldTile();
			World.get().submit(new Task(2) {
				@Override
				protected void execute() {
					if (player != null)
						player.getPackets().closeInterface(player.getInterfaceManager().hasRezizableScreen() ? 1 : 11);
					npc.setCantInteract(false);
					if (npc.isDead())
						return;
					World.sendGraphics(npc, new Graphics(572), middle);
					World.sendGraphics(npc, new Graphics(571),
							new WorldTile(middle.getX(), middle.getY() - 1, middle.getPlane()));
					World.sendGraphics(npc, new Graphics(571),
							new WorldTile(middle.getX(), middle.getY() + 1, middle.getPlane()));
					World.sendGraphics(npc, new Graphics(571),
							new WorldTile(middle.getX() - 1, middle.getY() - 1, middle.getPlane()));
					World.sendGraphics(npc, new Graphics(571),
							new WorldTile(middle.getX() - 1, middle.getY() + 1, middle.getPlane()));
					World.sendGraphics(npc, new Graphics(571),
							new WorldTile(middle.getX() + 1, middle.getY() - 1, middle.getPlane()));
					World.sendGraphics(npc, new Graphics(571),
							new WorldTile(middle.getX() + 1, middle.getY() + 1, middle.getPlane()));
					World.sendGraphics(npc, new Graphics(571),
							new WorldTile(middle.getX() - 1, middle.getY(), middle.getPlane()));
					World.sendGraphics(npc, new Graphics(571),
							new WorldTile(middle.getX() + 1, middle.getY(), middle.getPlane()));
					npc.setNextWorldTile(new WorldTile(COORDS[RandomUtility.random(COORDS.length)]));
					npc.setNextAnimation(new Animation(3315));
					this.cancel();
				}
			});
		} else {
			npc.setNextAnimation(new Animation(defs.getAttackEmote()));
			delayHit(npc, 0, target,
					getMeleeHit(npc, getRandomMaxHit(npc, defs.getMaxHit(), NPCCombatDefinitions.MELEE, target)));
		}
		return defs.getAttackDelay();
	}
}