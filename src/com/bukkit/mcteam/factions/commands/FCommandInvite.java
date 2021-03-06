package com.bukkit.mcteam.factions.commands;

import com.bukkit.mcteam.factions.Conf;
import com.bukkit.mcteam.factions.FPlayer;
import com.bukkit.mcteam.factions.Faction;
import com.bukkit.mcteam.factions.struct.Role;

public class FCommandInvite extends FBaseCommand {
	
	public FCommandInvite() {
		aliases.add("invite");
		aliases.add("inv");
		
		requiredParameters.add("player name");
		
		helpDescription = "Invite a player";
	}
	
	public void perform() {
		if ( ! assertHasFaction()) {
			return;
		}
		
		if ( ! assertMinRole(Role.MODERATOR)) {
			return;
		}
		
		String playerName = parameters.get(0);
		
		FPlayer you = findFPlayer(playerName, false);
		if (you == null) {
			return;
		}
		
		Faction myFaction = me.getFaction();
		
		if (you.getFaction() == myFaction) {
			sendMessage(you.getName()+" is already a member of "+myFaction.getTag());
			sendMessage("You might want to: " + new FCommandKick().getUseageTemplate());
			return;
		}
		
		myFaction.invite(you);
		
		you.sendMessage(me.getNameAndRelevant(you)+Conf.colorSystem+" invited you to "+myFaction.getTag(you));
		myFaction.sendMessage(me.getNameAndRelevant(me)+Conf.colorSystem+" invited "+you.getNameAndRelevant(me)+Conf.colorSystem+" to your faction.");
	}
	
}
