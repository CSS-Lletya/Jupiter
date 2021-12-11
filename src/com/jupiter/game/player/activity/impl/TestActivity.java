package com.jupiter.game.player.activity.impl;

import com.jupiter.game.player.Player;
import com.jupiter.game.player.activity.Activity;

public class TestActivity extends Activity {

	public TestActivity() {
		super("TEST", ActivitySafety.SAFE, ActivityType.NORMAL);
	}

	@Override
	public void login(Player player) {
		System.out.println("HEY THERE ZED");
	}
	
	@Override
	public void start(Player player) {
		System.out.println("Started");
	}
}