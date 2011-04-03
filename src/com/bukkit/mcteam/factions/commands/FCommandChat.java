package com.bukkit.mcteam.factions.commands;

public class FCommandChat extends FBaseCommand {
	
	public FCommandChat() {
		aliases.add("fchat");
		aliases.add("fc");
		
		helpDescription = "Switch faction only chat on and off";
	}
	
	public void perform() {
		if ( ! assertHasFaction()) {
			return;
		}
		
		if ( ! me.isFactionChatting()) {
			// Turn on
			me.setFactionChatting(true);
			sendMessage("Faction-only chat ENABLED.");
			
			if (me.isAllyChatting()) {
				me.setAllyChatting(false);
				sendMessage("Faction-only chat DISABLED.");
			}
			
		} else {
			// Turn off
			me.setFactionChatting(false);
			sendMessage("Ally-only chat DISABLED.");
		}
	}
	
}
