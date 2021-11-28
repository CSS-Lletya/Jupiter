package com.jupiter.plugins.regions;

import com.jupiter.plugin.annotations.PluginEventHandler;
import com.jupiter.plugin.events.ObjectClickEvent;
import com.jupiter.plugin.handlers.ObjectClickHandler;

@PluginEventHandler
public class Home {

	public static ObjectClickHandler bank = new ObjectClickHandler(new Object[] {"Counter"}) {
		
		@Override
		public void handle(ObjectClickEvent e) {
			e.getPlayer().getBank().openBank();
		}
	};
}