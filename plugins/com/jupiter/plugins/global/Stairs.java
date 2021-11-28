package com.jupiter.plugins.global;

import java.util.Optional;

import com.jupiter.plugin.annotations.PluginEventHandler;
import com.jupiter.plugin.events.ObjectClickEvent;
import com.jupiter.plugin.handlers.ObjectClickHandler;

@PluginEventHandler
public class Stairs {

	public static ObjectClickHandler stairs = new ObjectClickHandler(new Object[] {"Staircase"}) {
		
		@Override
		public void handle(ObjectClickEvent e) {
			e.getPlayer().getMovement().move(Optional.empty(), e.getPlayer(), Optional.of("HEY"));
		}
	};
}