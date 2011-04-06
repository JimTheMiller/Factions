package com.bukkit.mcteam.factions.commands;

import com.bukkit.mcteam.factions.FPlayer;

public class FCommandBounty extends FBaseCommand {
	
	public FCommandBounty() {
		aliases.add("b");
		optionalParameters.add("name");
		optionalParameters.add("amount");
		helpDescription = "*NEW* view and place bounties";
	}
	
	public void perform() {
		
		if (!assertHasFaction()) {
			return;
		}
		
		if (parameters.size() == 2)
		{
			FPlayer.getAllOnline();
		}
	}
}
