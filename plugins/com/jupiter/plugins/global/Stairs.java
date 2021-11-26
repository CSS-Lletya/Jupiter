package com.jupiter.plugins.global;

import com.jupiter.game.map.WorldTile;
import com.jupiter.plugin.annotations.PluginEventHandler;
import com.jupiter.plugin.events.ObjectClickEvent;
import com.jupiter.plugin.handlers.ObjectClickHandler;

@PluginEventHandler
public class Stairs {

	public static ObjectClickHandler stairs = new ObjectClickHandler(new Object[] {"Staircase"}) {
		
		@Override
		public void handle(ObjectClickEvent e) {
			e.getPlayer().getMovement().useStairs(-1, new WorldTile(e.getPlayer().getX(), e.getPlayer().getY(), 1));
		}
	};
}
