package com.bukkit.mcteam.factions.commands;

import java.util.Comparator;

import com.bukkit.mcteam.factions.FPlayer;

public class FPlayerBountyComparator implements Comparator<FPlayer>  {

	@Override
	public int compare(FPlayer arg0, FPlayer arg1) {
		return arg0.getBounty() < arg1.getBounty() ? 1 : 0;
	}
}
