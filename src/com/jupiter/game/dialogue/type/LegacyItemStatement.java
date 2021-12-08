package com.jupiter.game.dialogue.type;

import com.jupiter.game.player.Player;

public class LegacyItemStatement implements Statement {

    private int[] itemIds;
    private String title;
    private String[] text;

    public LegacyItemStatement(int item, String title, String... text) {
        this.itemIds = new int[] { item };
        this.title = title;
        this.text = text;
    }

    public LegacyItemStatement(int item1, int item2, String title, String... text) {
        this.itemIds = new int[] { item1, item2 };
        this.title = title;
        this.text = text;
    }

    @Override
    public void send(Player player) {
        int interfaceId = itemIds.length > 1 ? 131 : 519;
        String text = "";
        text += title+"<br>";
        for (String s : this.text) {
            text += s + "<br>";
        }
        player.getInterfaceManager().sendChatBoxInterface(interfaceId);
        player.getPackets().sendIComponentText(interfaceId, 1, text);
        player.getPackets().sendItemOnIComponent(interfaceId, 0, itemIds[0], 1);
        if (itemIds.length > 1)
            player.getPackets().sendItemOnIComponent(interfaceId, 2, itemIds[1], 1);
    }

	@Override
	public int getOptionId(int componentId) {
		return 0;
	}
}
