package com.bukkit.mcteam.factions.commands;

import com.bukkit.mcteam.factions.Board;
import com.bukkit.mcteam.factions.Conf;
import com.bukkit.mcteam.factions.FLocation;
import com.bukkit.mcteam.factions.Faction;
import com.bukkit.mcteam.factions.struct.Role;

public class FCommandHome extends FBaseCommand {
	
	public FCommandHome() {
		aliases.add("home");
		
		helpDescription = "Teleport to the faction home";
	}
	
	public void perform() {
	
		if ( ! assertHasFaction()) {
			return;
		}
		
		if ( ! Conf.homesEnabled) {
			me.sendMessage("Sorry, Faction homes are disabled on this server.");
			return;
		}
		
		Faction myFaction = me.getFaction();
		
		if ( ! myFaction.hasHome()) {
			me.sendMessage("You faction does not have a home. " + (me.getRole().value < Role.MODERATOR.value ? " Ask your leader to:" : "You should:"));
			me.sendMessage(new FCommandSethome().getUseageTemplate(true, true));
			return;
		}
		
		FLocation flocation = new FLocation(me);
		Faction otherFaction = Board.getFactionAt(flocation);
		if (otherFaction.isSafeZone()) {
			FLocation homeLocation = new FLocation(myFaction.getHome());
			if (Board.getFactionAt(homeLocation) == myFaction) {
				player.teleport(myFaction.getHome());
			} else {
				me.sendMessage("Your home location is outside your base therefore you cant use it.");
			}
		} else {
			me.sendMessage("You can only use home from spawn.");
		}
	}
	
}
