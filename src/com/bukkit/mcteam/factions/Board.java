package com.bukkit.mcteam.factions;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.bukkit.ChatColor;

import com.bukkit.mcteam.factions.struct.Relation;
import com.bukkit.mcteam.factions.util.TextUtil;
import com.bukkit.mcteam.gson.reflect.TypeToken;
import com.bukkit.mcteam.util.AsciiCompass;
import com.bukkit.mcteam.util.DiscUtil;

public class Board {
	private static transient File file = new File(Factions.instance.getDataFolder(), "board.json");
	private static transient HashMap<FLocation, Claim> flocationIds = new HashMap<FLocation, Claim>();
	
	//----------------------------------------------//
	// Get and Set
	//----------------------------------------------//
	public static int getIdAt(FLocation flocation) {
		if ( ! flocationIds.containsKey(flocation)) {
			return 0;
		}
		
		Claim c = flocationIds.get(flocation);
		return c.factionId;
	}
	
	public static Faction getFactionAt(FLocation flocation) {
		return Faction.get(getIdAt(flocation));
	}
	
	public static Claim getClaimAt(FLocation flocation) {
		return flocationIds.get(flocation);
	}
	
	public static void setIdAt(int id, FLocation flocation) {
		if (id == 0) {
			removeAt(flocation);
		}
		
		flocationIds.put(flocation, new Claim(id, ClaimAccess.FACTION));
	}
	
	public static void setAccessAt(FLocation flocation, ClaimAccess access) {
		Claim claim = flocationIds.get(flocation);
		claim.access = access;
	}
	
	public static void setFactionAt(Faction faction, FLocation flocation) {
		setIdAt(faction.getId(), flocation);
	}
	
	public static void removeAt(FLocation flocation) {
		flocationIds.remove(flocation);
	}

	// Is this coord NOT completely surrounded by coords claimed by the same faction?
	// Simpler: Is there any nearby coord with a faction other than the faction here?
	public static boolean isBorderLocation(FLocation flocation) {
		int orthogonallyOwned = 0;
		Faction faction = getFactionAt(flocation);
		FLocation a = flocation.getRelative(1, 0);
		FLocation b = flocation.getRelative(-1, 0);
		FLocation c = flocation.getRelative(0, 1);
		FLocation d = flocation.getRelative(0, -1);
		if (faction == getFactionAt(a)) {
			orthogonallyOwned += 1;
		}
		if (faction == getFactionAt(b)) {
			orthogonallyOwned += 1;
		}
		if (faction == getFactionAt(c)) {
			orthogonallyOwned += 1;
		}
		if (faction == getFactionAt(d)) {
			orthogonallyOwned += 1;
		}
		if (orthogonallyOwned < 3) {
			return true;
		} else {
			return false;
		}
	}
	
	public static int numberSurroundingPlots(FLocation flocation) {
		int orthogonallyOwned = 0;
		Faction faction = getFactionAt(flocation);
		FLocation a = flocation.getRelative(1, 0);
		FLocation b = flocation.getRelative(-1, 0);
		FLocation c = flocation.getRelative(0, 1);
		FLocation d = flocation.getRelative(0, -1);
		if (faction == getFactionAt(a)) {
			orthogonallyOwned += 1;
		}
		if (faction == getFactionAt(b)) {
			orthogonallyOwned += 1;
		}
		if (faction == getFactionAt(c)) {
			orthogonallyOwned += 1;
		}
		if (faction == getFactionAt(d)) {
			orthogonallyOwned += 1;
		}
		return orthogonallyOwned;
	}
	
	//----------------------------------------------//
	// Cleaner. Remove orphaned foreign keys
	//----------------------------------------------//
	
	public static void clean() {
		Iterator<Entry<FLocation, Claim>> iter = flocationIds.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<FLocation, Claim> entry = iter.next();
			if (!Faction.exists(entry.getValue().factionId)) {
				Factions.log("Board cleaner removed " + entry.getValue() + " from " + entry.getKey());
				iter.remove();
			}
		}
	}	
	
	//----------------------------------------------//
	// Coord count
	//----------------------------------------------//
	
	public static int getFactionCoordCount(int factionId) {
		int ret = 0;
		for (Claim thatClaim : flocationIds.values()) {
			if(thatClaim.factionId == factionId) {
				ret += 1;
			}
		}
		return ret;
	}
	
	public static int getFactionCoordCount(Faction faction) {
		return getFactionCoordCount(faction.getId());
	}
	
	//----------------------------------------------//
	// Map generation
	//----------------------------------------------//
	
	/**
	 * The map is relative to a coord and a faction
	 * north is in the direction of decreasing x
	 * east is in the direction of decreasing z
	 */
	public static ArrayList<String> getMap(Faction faction, FLocation flocation, double inDegrees) {
		ArrayList<String> ret = new ArrayList<String>();
		ret.add(TextUtil.titleize("("+flocation.getCoordString()+") "+getFactionAt(flocation).getTag(faction)));
		
		int halfWidth = Conf.mapWidth / 2;
		int halfHeight = Conf.mapHeight / 2;
		FLocation topLeft = flocation.getRelative(-halfHeight, halfWidth);
		int width = halfWidth * 2 + 1;
		int height = halfHeight * 2 + 1;
		
		// For each row
		for (int dx = 0; dx < height; dx++) {
			// Draw and add that row
			String row = "";
			for (int dz = 0; dz > -width; dz--) {
				if(dz == -(halfWidth) && dx == halfHeight) {
					row += ChatColor.AQUA+"P";
				} else {
					FLocation flocationHere = topLeft.getRelative(dx, dz);
					Faction factionHere = getFactionAt(flocationHere);
					if (factionHere.isNone()) {
						row += ChatColor.GRAY+"-";
					} else if (factionHere.isSafeZone()) {
						row += ChatColor.GOLD+"S";
					} else {
						Relation relation = factionHere.getRelation(faction);
						row += relation.getColor() + relation.getSymbol();
					}
				}
			}
			ret.add(row);
		}
		
		// Get the compass
		ArrayList<String> asciiCompass = AsciiCompass.getAsciiCompass(inDegrees, ChatColor.RED, Conf.colorChrome);

		// Add the compass
		ret.set(1, asciiCompass.get(0)+ret.get(1).substring(3*3));
		ret.set(2, asciiCompass.get(1)+ret.get(2).substring(3*3));
		ret.set(3, asciiCompass.get(2)+ret.get(3).substring(3*3));
		
		return ret;
	}
	
	
	// -------------------------------------------- //
	// Persistance
	// -------------------------------------------- //
	
	public static Map<String,Map<String,Claim>> dumpAsSaveFormat() {
		Map<String,Map<String,Claim>> worldCoordIds = new HashMap<String,Map<String,Claim>>(); 
		
		for (Entry<FLocation, Claim> entry : flocationIds.entrySet()) {
			String worldName = entry.getKey().getWorldName();
			String coords = entry.getKey().getCoordString();
			Claim claim = entry.getValue();
			if ( ! worldCoordIds.containsKey(worldName)) {
				worldCoordIds.put(worldName, new TreeMap<String, Claim>());
			}
			
			worldCoordIds.get(worldName).put(coords, claim);
		}
		
		return worldCoordIds;
	}
	
	public static void loadFromSaveFormat(Map<String,Map<String,Claim>> worldCoordIds) {
		flocationIds.clear();
		
		for (Entry<String,Map<String,Claim>> entry : worldCoordIds.entrySet()) {
			String worldName = entry.getKey();
			for (Entry<String, Claim> entry2 : entry.getValue().entrySet()) {
				String[] coords = entry2.getKey().trim().split("[,\\s]+");
				int x = Integer.parseInt(coords[0]);
				int z = Integer.parseInt(coords[1]);
				Claim claim = entry2.getValue();
				flocationIds.put(new FLocation(worldName, x, z), claim);
			}
		}
	}
	
	public static boolean save() {
		//Factions.log("Saving board to disk");
		
		try {
			DiscUtil.write(file, Factions.gson.toJson(dumpAsSaveFormat()));
		} catch (IOException e) {
			Factions.log("Failed to save the board to disk.");
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	public static boolean load() {
		Factions.log("Loading board from disk");
		
		/*
		if ( ! file.exists()) {
			if ( ! loadOld())
				Factions.log("No board to load from disk. Creating new file.");
			save();
			return true;
		}*/
		
		try {
			Type type = new TypeToken<Map<String,Map<String,Claim>>>(){}.getType();
			Map<String,Map<String,Claim>> worldCoordIds = Factions.gson.fromJson(DiscUtil.read(file), type);
			loadFromSaveFormat(worldCoordIds);
		} catch (IOException e) {
			Factions.log("Failed to load the board from disk.");
			e.printStackTrace();
			return false;
		}
			
		return true;
	}
/*
	private static boolean loadOld() {
		File folderBoard = new File(Factions.instance.getDataFolder(), "board");

		if ( ! folderBoard.isDirectory())
			return false;

		Factions.log("Board file doesn't exist, attempting to load old pre-1.1 data.");

		String ext = ".json";

		class jsonFileFilter implements FileFilter {
			@Override
			public boolean accept(File file) {
				return (file.getName().toLowerCase().endsWith(".json") && file.isFile());
			}
		}

		File[] jsonFiles = folderBoard.listFiles(new jsonFileFilter());
		for (File jsonFile : jsonFiles) {
			// Extract the name from the filename. The name is filename minus ".json"
			String name = jsonFile.getName();
			name = name.substring(0, name.length() - ext.length());
			try {
				JsonParser parser = new JsonParser();
				JsonObject json = (JsonObject) parser.parse(DiscUtil.read(jsonFile));
				JsonArray coords = json.getAsJsonArray("coordFactionIds");
				Iterator<JsonElement> coordSet = coords.iterator();
				while(coordSet.hasNext()) {
					JsonArray coordDat = (JsonArray) coordSet.next();
					JsonObject coord = coordDat.get(0).getAsJsonObject();
					int coordX = coord.get("x").getAsInt();
					int coordZ = coord.get("z").getAsInt();
					
					parser.
					
					JsonObject claim = coordDat.get(1).
					
					int factionId = claim.get("factionId").getAsInt();
					ClaimAccess access = claim.get("access").getAsInt();
					
					flocationIds.put(new FLocation(name, coordX, coordZ), new Claim(factionId, access));
				}
				Factions.log("loaded pre-1.1 board "+name);
			} catch (Exception e) {
				e.printStackTrace();
				Factions.log(Level.WARNING, "failed to load board "+name);
			}
		}
		return true;
	}
	*/

}



















