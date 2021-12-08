package com.jupiter.game.dialogue.type;

import com.jupiter.cache.loaders.NPCDefinitions;
import com.jupiter.game.dialogue.Mood;
import com.jupiter.game.player.Player;

public class NPCStatement implements Statement {

    private int npcId;
    private Mood emote;
    private String[] texts;

    public NPCStatement(int npcId, Mood emote, String... texts) {
        this.npcId = npcId;
        this.emote = emote;
        this.texts = texts;
    }

    @Override
    public void send(Player player) {
        StringBuilder builder = new StringBuilder();
        for (int line = 0; line < texts.length; line++)
            builder.append(" " + texts[line]);
        String text = builder.toString();
        player.getInterfaceManager().sendChatBoxInterface(1184);
        player.getPackets().sendIComponentText(1184, 17, NPCDefinitions.getNPCDefinitions(npcId).name);
        player.getPackets().sendIComponentText(1184, 13, text);
        player.getPackets().sendNPCOnIComponent(1184, 11, npcId);
        if (emote != null && emote.getEmoteId() != -1)
            player.getPackets().sendIComponentAnimation(emote.getEmoteId(), 1184, 11);
    }

	@Override
	public int getOptionId(int componentId) {
		return 0;
	}
}
