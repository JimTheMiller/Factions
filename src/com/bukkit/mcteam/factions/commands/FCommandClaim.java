package com.bukkit.mcteam.factions.commands;

import com.bukkit.mcteam.factions.Board;
import com.bukkit.mcteam.factions.Conf;
import com.bukkit.mcteam.factions.FLocation;
import com.bukkit.mcteam.factions.Faction;
import com.bukkit.mcteam.factions.struct.Relation;
import com.bukkit.mcteam.factions.struct.Role;

public class FCommandClaim extends FBaseCommand {
	
	public FCommandClaim() {
		aliases.add("claim");
		
		helpDescription = "Claim the land where you are standing";
	}
	
	public void perform() {
		if ( ! assertHasFaction()) {
			return;
		}
		
		Faction myFaction = me.getFaction();
		FLocation flocation = new FLocation(me);
		Faction otherFaction = Board.getFactionAt(flocation);
		
		if (myFaction == otherFaction) {
			sendMessage("You already own this land.");
			return;
		}
		
		if ( ! assertMinRole(Role.MODERATOR)) {
			return;
		}
		
		
		if (myFaction.getLandRounded() >= myFaction.getPowerRounded()) {
			sendMessage("You can't claim more land! You need more power!");
			return;
		}
		
		if (otherFaction.getRelation(me) == Relation.ALLY) {
			sendMessage("You can't claim the land of your allies.");
			return;
		}
		
		if (otherFaction.isSafeZone()) {
			sendMessage("You can not claim a SafeZone.");
			return;
		}
		
		if (otherFaction.isNone()) {
			myFaction.sendMessage(me.getNameAndRelevant(myFaction)+Conf.colorSystem+" claimed some new land :D");
		} else { //if (otherFaction.isNormal()) {
			
			if ( ! otherFaction.hasLandInflation()) {
				 // TODO more messages WARN current faction most importantly
				sendMessage(me.getRelationColor(otherFaction)+otherFaction.getTag()+Conf.colorSystem+" owns this land and is strong enough to keep it.");
				return;
			}
			
			int plotsAround = Board.numberSurroundingPlots(flocation);
			if (plotsAround > 1) {
				sendMessage("There are still " + plotsAround + " surrounding plots.");
				return;
			}
			
			// ASDF claimed some of your land 450 blocks NNW of you.
			// ASDf claimed some land from FACTION NAME
			otherFaction.sendMessage(me.getNameAndRelevant(otherFaction)+Conf.colorSystem+" stole some of your land :O");
			myFaction.sendMessage(me.getNameAndRelevant(myFaction)+Conf.colorSystem+" claimed some land from "+otherFaction.getTag(myFaction));
		}
		
		Board.setFactionAt(myFaction, flocation);
	}
	
}
