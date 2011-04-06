package com.bukkit.mcteam.factions.commands;

import com.bukkit.mcteam.factions.Board;
import com.bukkit.mcteam.factions.Conf;
import com.bukkit.mcteam.factions.FLocation;
import com.bukkit.mcteam.factions.Faction;
import com.bukkit.mcteam.factions.Factions;
import com.bukkit.mcteam.factions.struct.Role;

public class FCommandUnclaim extends FBaseCommand {
	
	public FCommandUnclaim() {
		aliases.add("unclaim");
		aliases.add("declaim");
		
		helpDescription = "Unclaim the land where you are standing";
	}
	
	public void perform() {
		FLocation flocation = new FLocation(me);
		Faction otherFaction = Board.getFactionAt(flocation);
		
		if (otherFaction.isSafeZone()) {
			if (Factions.hasPermManageSafeZone(sender)) {
				Board.removeAt(flocation);
				sendMessage("Safe zone was unclaimed.");
			} else {
				sendMessage("This is a safe zone. You lack permissions to unclaim.");
			}
			return;
		}
		
		if ( ! assertHasFaction()) {
			return;
		}
		
		if ( ! assertMinRole(Role.MODERATOR)) {
			return;
		}
		
		Faction myFaction = me.getFaction();
		
		
		if ( myFaction != otherFaction) {
			if ( ! otherFaction.hasLandInflation()) {
				 // TODO more messages WARN current faction most importantly
				sendMessage(me.getRelationColor(otherFaction)+otherFaction.getTag()+Conf.colorSystem+" owns this land and is strong enough to keep it.");
				return;
			}
			
			int plotsAround = Board.numberSurroundingPlots(flocation);
			if (plotsAround > 2) {
				sendMessage("There are still " + plotsAround + " surrounding plots.");
				return;
			}
			Board.removeAt(flocation);
			
			otherFaction.sendMessage(me.getNameAndRelevant(otherFaction)+Conf.colorSystem+" unclaimed some of your land :O");
			myFaction.sendMessage(me.getNameAndRelevant(myFaction)+Conf.colorSystem+" unclaimed some land from "+otherFaction.getTag(myFaction));
			
		} else {
			
			Board.removeAt(flocation);

			myFaction.sendMessage(me.getNameAndRelevant(myFaction)+Conf.colorSystem+" unclaimed some land.");
		}
		
		return;
	}
	
}
