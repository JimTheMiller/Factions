package com.bukkit.mcteam.factions.commands;

import com.bukkit.mcteam.factions.Board;
import com.bukkit.mcteam.factions.Conf;
import com.bukkit.mcteam.factions.FLocation;
import com.bukkit.mcteam.factions.Faction;
import com.bukkit.mcteam.factions.struct.Role;

public class FCommandClaim extends FBaseCommand {
	
	public FCommandClaim() {
		aliases.add("claim");
		
		helpDescription = "Claim the land where you are standing";
	}
	
	public void perform() {
		
		if (!assertHasFaction()) {
			return;
		}
		
		Faction myFaction = me.getFaction();
		FLocation flocation = new FLocation(me);
		Faction otherFaction = Board.getFactionAt(flocation);
		
		if (!assertMinRole(Role.MODERATOR)) {
			return;
		}

		if (myFaction == otherFaction) {
			sendMessage("You already own this land.");
			return;
		}

		if (!otherFaction.isNone()) {
			sendMessage("This land is already claimed. Try /f unclaim");
			return;
		}
		
		if (myFaction.getLandRounded() >= myFaction.getPowerRounded()) {
			sendMessage("You can't claim more land! You need more power!");
			return;
		}
	
		if (otherFaction.isSafeZone()) {
			sendMessage("You can not claim a SafeZone.");
			return;
		}
		
		myFaction.sendMessage(me.getNameAndRelevant(myFaction) + Conf.colorSystem + " claimed some new land :D");
		Board.setFactionAt(myFaction, flocation);
	}
	
}
