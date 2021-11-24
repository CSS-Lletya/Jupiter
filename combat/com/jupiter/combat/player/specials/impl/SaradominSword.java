package com.jupiter.combat.player.specials.impl;

import java.util.Optional;

import com.jupiter.combat.player.PlayerCombat;
import com.jupiter.combat.player.specials.WeaponSpecialSignature;
import com.jupiter.combat.player.specials.WeaponSpecials;
import com.jupiter.game.Animation;
import com.jupiter.game.Entity;
import com.jupiter.game.Graphics;
import com.jupiter.game.item.ItemNames;
import com.jupiter.game.player.Player;
import com.jupiter.game.player.Rights;
import com.jupiter.utils.Utils;

@WeaponSpecialSignature(weapons = { ItemNames.SARADOMIN_SWORD, 23690 }, specAmount = 100)
public class SaradominSword implements WeaponSpecials {

	/**
     *Hits two lightning bolts, the first hit is a regular hit which grants experience in the chosen melee skill. The second hit is magic based, and can
     * hit 50-180, giving magic experience. (Note: the special attack does not give defence experience unless defensive casting is enabled).
     *
     */
    @Override
    public void execute(Player player, Entity target, PlayerCombat combat) throws Exception {
        target.setNextGraphics(new Graphics(1194));

        if(player.getRights() == Rights.ADMINISTRATOR)
			player.getPackets().sendGameMessage(this.getClass().getName() + " Unfinished special, needs testing!");


        int weaponId = player.getEquipment().getWeaponId();
        int attackStyle = player.getCombatDefinitions().getAttackStyle();

        //implementation
        int regularDmg = combat.getRandomMaxHit(player, weaponId, attackStyle, false, true, 1, true);
        int magicDamage = 50 + Utils.getRandom(180);
        combat.delayNormalHit(weaponId, attackStyle,
                combat.getMeleeHit(player, regularDmg),
                combat.getMagicHit(player, magicDamage));
    }

    @Override
    public Optional<Animation> getAnimation() {
        return Optional.of(new Animation(11993));
    }

    @Override
    public Optional<Graphics> getGraphics() {
        return Optional.empty();
    }

    @Override
    public Optional<Integer> getSound() {
        return Optional.of(3853);
    }
}