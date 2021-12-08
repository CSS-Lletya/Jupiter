package com.jupiter.game.dialogue.type;

import java.security.InvalidParameterException;

import com.jupiter.game.player.Player;

public class OptionStatement implements Statement {

    private String title;
    private String[] options;

    public OptionStatement(String title, String... options) {
        this.title = title;
        if (options.length > 5) {
            throw new InvalidParameterException("The max options length is 5.");
        }
        this.options = options;
    }

    @Override
    public void send(Player player) {
        String[] optionArray = new String[5];
        for (int i = 0; i < 5; i++)
            optionArray[i] = "";
        int ptr = 0;
        for (String s : options) {
            if (s != null) {
                optionArray[ptr++] = s;
            }
        }
        player.getInterfaceManager().sendChatBoxInterface(1188);
        player.getPackets().sendIComponentText(1188, 20, title != null ? title : "Select an Option");
        player.getPackets().sendRunScript(5589, optionArray[4], optionArray[3], optionArray[2], optionArray[1], optionArray[0], options.length);
    }

	@Override
	public int getOptionId(int componentId) {
		return componentId == 11 ? 0 : componentId-12;
	}
}
