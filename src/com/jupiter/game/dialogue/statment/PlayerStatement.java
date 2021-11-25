package com.jupiter.game.dialogue.statment;

import com.jupiter.game.dialogue.Mood;
import com.jupiter.game.player.Player;

public class PlayerStatement implements Statement {

    private Mood emote;
    private String[] texts;

    public PlayerStatement(Mood emote, String... texts) {
        this.emote = emote;
        this.texts = texts;
    }

    @Override
    public void send(Player player) {
        StringBuilder builder = new StringBuilder();
        for (int line = 0; line < texts.length; line++)
            builder.append(" " + texts[line]);
        String text = builder.toString();
        player.getInterfaceManager().sendChatBoxInterface(1191);
        player.getPackets().sendIComponentText(1191, 8, player.getDisplayName());
        player.getPackets().sendIComponentText(1191, 17, text);
        player.getPackets().sendPlayerOnIComponent(1191, 15);
        if (emote != null && emote.getEmoteId() != -1)
            player.getPackets().sendIComponentAnimation(emote.getEmoteId(), 1191, 15);
    }

	@Override
	public int getOptionId(int componentId) {
		return 0;
	}
}
