package com.bukkit.mcteam.factions;

public enum ClaimAccess {
	ALLY(3, "ally"),
	MOD(2, "mod"),
	FACTION(1, "faction");
	
	public final int value;
	public final String nicename;
	
	private ClaimAccess(final int value, final String nicename) {
        this.value = value;
        this.nicename = nicename;
    }
}