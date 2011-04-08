package com.bukkit.mcteam.factions.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import org.bukkit.ChatColor;

import com.bukkit.mcteam.factions.FPlayer;
import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;

public class FCommandBounty extends FBaseCommand {
	
	public FCommandBounty() {
		aliases.add("bounty");
		helpDescription = "*NEW* /f bounty [name] [amount]";
	}
	
	public void perform() {

		if (parameters.size() == 0)
			showList();

		if (parameters.size() == 2) 
			addBounty();
	}
	
	private void showList() {
		
		try
		{
		List<FPlayer> players = new ArrayList<FPlayer>(FPlayer.getAllOnline());
		Collections.sort(players, new FPlayerBountyComparator());
		
		for (int i = 0; i < (players.size() >= 10 ? 10 : players.size()); i++) {
			FPlayer target = players.get(i);
			me.sendMessage(
					ChatColor.GREEN + "[bounty] " + (i+1) + ". " 
					+ ChatColor.WHITE + target.getName() 
					+ ChatColor.GREEN + " " + iConomy.getBank().format(target.getBounty()));
			
		}
		} catch (Exception ex) 		{
			me.sendMessage(ex.toString());
		}
	}
	
	private void addBounty() {
		
		String playerName = parameters.get(0);
		FPlayer target = FPlayer.find(playerName);
		
		if (target == null) {
			me.sendMessage(ChatColor.GREEN + "[bounty] " 
					+ ChatColor.WHITE + playerName  
					+ ChatColor.GREEN + " does not exist.");
			return;
		}
		
		int amount = 0;
		try
		{
			if (parameters.size() > 1)
				amount = Math.abs(Integer.parseInt(parameters.get(1)));
		} catch (NumberFormatException ex) {
		}
		
		if (amount <= 0) {
			me.sendMessage(
					ChatColor.GREEN + "[bounty] "
					+ ChatColor.RED + "Invalid amount.");
			return;
		}
		
		Account myAccount = iConomy.getBank()
			.getAccount(me.getName());

		if (amount > myAccount.getBalance()) 
			amount = (int)myAccount.getBalance();
						
		myAccount.subtract(amount);
		
		target.addBounty(amount);
		
		me.getPlayer().getServer()
			.broadcastMessage(
					ChatColor.GREEN + "[bounty] " 
					+ ChatColor.WHITE + me.getName() 
					+ ChatColor.GREEN + " added " + amount + " to " 
					+ ChatColor.WHITE + target.getName() 
					+ ChatColor.GREEN + " bounty.");
	}
	
}
