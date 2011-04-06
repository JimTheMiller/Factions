package com.bukkit.mcteam.factions.listeners;

import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;

import com.bukkit.mcteam.factions.Factions;
import com.nijiko.coelho.iConomy.iConomy;


public class FactionsIConomyListener extends ServerListener {

	@Override
	public void onPluginEnable(PluginEnableEvent event) {
		
		if(Factions.getIConomy() == null) {
            Plugin iConomy = Factions.instance.getServer().getPluginManager().getPlugin("iConomy");

            if (iConomy != null) {
                if(iConomy.isEnabled()) {
                	Factions.setIConomy((iConomy)iConomy);
                    System.out.println("[Factions] Successfully linked with iConomy.");
                }
            }
        }

	}
	
}
