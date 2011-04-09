package com.bukkit.mcteam.factions.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;

import com.bukkit.mcteam.factions.FPlayer;
import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;

public class FBountyEntityListener extends EntityListener {
	
	@Override
	public void onEntityDeath(EntityDeathEvent event) {
		
		if (!(event.getEntity() instanceof Player)) {
			return;
		}
		
		Player player = (Player)event.getEntity();
		FPlayer deadFPlayer = FPlayer.get(player);

		if (deadFPlayer.getLastDamangedBy() == null)
			return;

		Account deadPlayerAccount = iConomy.getBank()
			.getAccount(deadFPlayer.getName());
	
		Entity aliveEntity = deadFPlayer.getLastDamangedBy();
		
		if (aliveEntity == null)
			return;
	
		Player alivePlayer = (Player)aliveEntity;
		FPlayer aliveFPlayer = FPlayer.get(alivePlayer);
		
		Account alivePlayerAccount = iConomy.getBank()
			.getAccount(aliveFPlayer.getName());
	
		double deathCost = Math.abs(deadPlayerAccount.getBalance() * 0.05);
		
		deadPlayerAccount.subtract(deathCost * 3);
		alivePlayerAccount.add(deathCost);
		
		player.getServer().broadcastMessage(ChatColor.GREEN + "[bounty] " 
				+ ChatColor.WHITE + alivePlayer.getName() 
				+ ChatColor.GREEN + " stole " + iConomy.getBank().format(deathCost) + " from "
				+ ChatColor.WHITE + deadFPlayer.getName());
		
		aliveFPlayer.addBounty(deathCost * 2); // inflation
		
		player.getServer().broadcastMessage(ChatColor.GREEN + "[bounty] " 
				+ ChatColor.WHITE + alivePlayer.getName() 
				+ ChatColor.GREEN + " bounty has increased to " + iConomy.getBank().format(aliveFPlayer.getBounty()) + ".");
		
		if (deadFPlayer.getBounty() <= 0)
			return;
		
		double bounty = deadFPlayer.getBounty();
		
		player.getServer().broadcastMessage(ChatColor.GREEN + "[bounty] " 
				+ ChatColor.WHITE + alivePlayer.getName() 
				+ ChatColor.GREEN + " claimed " 
				+ iConomy.getBank().format(deadFPlayer.getBounty()) + " bounty for killing " 
				+ ChatColor.WHITE + deadFPlayer.getName() + ".");
		
		alivePlayerAccount.add(bounty);
		deadFPlayer.addBounty(-bounty);
	}
}
