package com.bukkit.mcteam.factions.commands;

public class FCommandAllyChat extends FBaseCommand {
	
	public FCommandAllyChat() {
		aliases.add("achat");
		aliases.add("ac");
		
		helpDescription = "Switch faction only chat on and off";
	}
	
	public void perform() {
		if (!assertHasFaction()) {
			return;
		}
		
		if (!me.isAllyChatting()) {
			// Turn on
			me.setAllyChatting(true);
			
			sendMessage("Ally-only chat ENABLED.");
			
			if (me.isFactionChatting()) {
				me.setFactionChatting(false);
				sendMessage("Faction-only chat DISABLED.");
			}
			
		} else {
			// Turn off
			me.setAllyChatting(false);
			sendMessage("Ally-only chat DISABLED.");
		}
	}

}
