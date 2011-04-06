package com.bukkit.mcteam.factions.commands;

import com.bukkit.mcteam.factions.Board;
import com.bukkit.mcteam.factions.ClaimAccess;
import com.bukkit.mcteam.factions.FLocation;
import com.bukkit.mcteam.factions.Faction;
import com.bukkit.mcteam.factions.struct.Role;

public class FCommandAccess extends FBaseCommand {
	
	public FCommandAccess() {
		aliases.add("access");
		optionalParameters.add("access");
		helpDescription = "*NEW* Change claim permissions (mod, faction, ally)";
	}
		
	public void perform() {
		
		if (!assertHasFaction()) {
			return;
		}
		
		FLocation flocation = new FLocation(me);
		Faction myFaction = me.getFaction();
		Faction ownerFaction = Board.getFactionAt(flocation);
		
		if (myFaction != ownerFaction)
		{
			me.sendMessage("You dont own this plot.");
			return;
		}
		
		if (!assertMinRole(Role.MODERATOR))
		{
			me.sendMessage("You are not a moderator/admin.");
			return;
		}
		
		ClaimAccess access = ClaimAccess.FACTION;
		
		if (parameters.size() > 0)
		{
			String rawAccess = parameters.get(0);
			
			if (rawAccess.contentEquals("faction")) {
				access = ClaimAccess.FACTION;
			} else if (rawAccess.contentEquals("mod")) {
				access = ClaimAccess.MOD;
			} else if (rawAccess.contentEquals("ally")) {
				access = ClaimAccess.ALLY;
			} else {
				me.sendMessage("You must enter one of [faction, mod, ally]");
			}
		}
		
		myFaction.sendMessage(me.getName() + " has changed claim access to " + access);
		Board.setAccessAt(flocation, access);
	}
}
