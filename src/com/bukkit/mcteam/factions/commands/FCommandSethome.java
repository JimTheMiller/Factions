package com.bukkit.mcteam.factions.commands;

import com.bukkit.mcteam.factions.Board;
import com.bukkit.mcteam.factions.Conf;
import com.bukkit.mcteam.factions.FLocation;
import com.bukkit.mcteam.factions.Faction;
import com.bukkit.mcteam.factions.struct.Role;

public class FCommandSethome extends FBaseCommand {
	
	public FCommandSethome() {
		aliases.add("sethome");
		helpDescription = "Set the faction home";
	}
	
	public void perform() {
		
		if (!assertHasFaction()) {
			return;
		}
		
		if (!assertMinRole(Role.MODERATOR)) {
			return;
		}
		
		if (!Conf.homesEnabled) {
			me.sendMessage("Sorry, Faction homes are disabled on this server.");
			return;
		}
				
		Faction myFaction = me.getFaction();
		FLocation flocation = new FLocation(me);
		Faction otherFaction = Board.getFactionAt(flocation);
		
		if (myFaction == otherFaction) {
			myFaction.setHome(player.getLocation());
			myFaction.sendMessage(me.getNameAndRelevant(myFaction)+Conf.colorSystem+" set the home for your faction. You can now use:");
			myFaction.sendMessage(new FCommandHome().getUseageTemplate(true, true));
		} else {
			me.sendMessage("You can only set home within your factions claims");
		}
	}
	
}
