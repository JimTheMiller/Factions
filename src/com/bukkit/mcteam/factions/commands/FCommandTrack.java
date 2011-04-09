package com.bukkit.mcteam.factions.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;

import com.bukkit.mcteam.factions.FPlayer;
import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;

public class FCommandTrack extends FBaseCommand {
	
	public FCommandTrack() {
		aliases.add("track");
		requiredParameters.add("player name");
		helpDescription = "Tracks the last known location of a player.";
	}
	
	public void perform() {
		
		if (parameters.size() <= 0) {
			me.sendMessage(ChatColor.GREEN + "[track] You must enter a players name.");
			return;
		}
		
		Account myAccount = iConomy.getBank()
			.getAccount(me.getName());
		
		if (myAccount.getBalance() <= 50) {
			me.sendMessage(ChatColor.GREEN + "[track] tracking a player costs 50 you dont have enough.");
			return;
		}
		
		FPlayer target = FPlayer.find(parameters.get(0));
		
		if (target == null) {
			me.sendMessage(ChatColor.GREEN + "[track] no player called "
					+ ChatColor.WHITE + parameters.get(0) 
					+ ChatColor.GREEN + " found.");
			return;
		}
			
		myAccount.subtract(50);
		
		Location location = target.getPlayer().getLocation();
		
		me.getPlayer().getServer().broadcastMessage("x: " + location.getX() + ", z: " + location.getZ());
		
		me.getPlayer().setCompassTarget(location);
		
		me.sendMessage(ChatColor.GREEN + "[track] Your compass has been set to "
				+ ChatColor.WHITE + target.getName() 
				+ ChatColor.GREEN + " location.");
		me.sendMessage(ChatColor.GREEN + "[track] x: " + (int)location.getX() + ", z: " + (int)location.getZ());
		me.sendMessage(ChatColor.GREEN + "[track] compasses are broken in 1.4 (you can still use 1.3 on this server)");
	}
	
}
