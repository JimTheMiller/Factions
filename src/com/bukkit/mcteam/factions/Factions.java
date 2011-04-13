package com.bukkit.mcteam.factions;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.bukkit.mcteam.factions.commands.FBaseCommand;
import com.bukkit.mcteam.factions.commands.FCommandAccess;
import com.bukkit.mcteam.factions.commands.FCommandAdmin;
import com.bukkit.mcteam.factions.commands.FCommandAllyChat;
import com.bukkit.mcteam.factions.commands.FCommandBounty;
import com.bukkit.mcteam.factions.commands.FCommandBuy;
import com.bukkit.mcteam.factions.commands.FCommandChat;
import com.bukkit.mcteam.factions.commands.FCommandClaim;
import com.bukkit.mcteam.factions.commands.FCommandCreate;
import com.bukkit.mcteam.factions.commands.FCommandDeinvite;
import com.bukkit.mcteam.factions.commands.FCommandDescription;
import com.bukkit.mcteam.factions.commands.FCommandHelp;
import com.bukkit.mcteam.factions.commands.FCommandHome;
import com.bukkit.mcteam.factions.commands.FCommandInvite;
import com.bukkit.mcteam.factions.commands.FCommandJoin;
import com.bukkit.mcteam.factions.commands.FCommandKick;
import com.bukkit.mcteam.factions.commands.FCommandLeave;
import com.bukkit.mcteam.factions.commands.FCommandList;
import com.bukkit.mcteam.factions.commands.FCommandMap;
import com.bukkit.mcteam.factions.commands.FCommandMod;
import com.bukkit.mcteam.factions.commands.FCommandOpen;
import com.bukkit.mcteam.factions.commands.FCommandRelationAlly;
import com.bukkit.mcteam.factions.commands.FCommandRelationEnemy;
import com.bukkit.mcteam.factions.commands.FCommandRelationNeutral;
import com.bukkit.mcteam.factions.commands.FCommandSafeclaim;
import com.bukkit.mcteam.factions.commands.FCommandSethome;
import com.bukkit.mcteam.factions.commands.FCommandShow;
import com.bukkit.mcteam.factions.commands.FCommandTag;
import com.bukkit.mcteam.factions.commands.FCommandTitle;
import com.bukkit.mcteam.factions.commands.FCommandTrack;
import com.bukkit.mcteam.factions.commands.FCommandUnclaim;
import com.bukkit.mcteam.factions.commands.FCommandVersion;
import com.bukkit.mcteam.factions.listeners.FBountyEntityListener;
import com.bukkit.mcteam.factions.listeners.FactionsBlockListener;
import com.bukkit.mcteam.factions.listeners.FactionsChunkListener;
import com.bukkit.mcteam.factions.listeners.FactionsEntityListener;
import com.bukkit.mcteam.factions.listeners.FactionsIConomyListener;
import com.bukkit.mcteam.factions.listeners.FactionsPlayerListener;
import com.bukkit.mcteam.gson.Gson;
import com.bukkit.mcteam.gson.GsonBuilder;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

import me.taylorkelly.help.Help;

/**
 * The data is saved to disk every 30min and on plugin disable.
 */
public class Factions extends JavaPlugin {

	public static Factions instance;
	private static iConomy iConomy = null;
	
	public boolean enabled = false;
	
	public final static Gson gson = new GsonBuilder()
	.setPrettyPrinting()
	.excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.VOLATILE)
	.registerTypeAdapter(Location.class, new MyLocationTypeAdapter())
	.create();
	
	private final FactionsPlayerListener playerListener = new FactionsPlayerListener();
	private final FactionsEntityListener entityListener = new FactionsEntityListener();
	private final FactionsBlockListener blockListener = new FactionsBlockListener();
	private final FactionsChunkListener chunkListener = new FactionsChunkListener();
	private final FactionsIConomyListener iConomyListener = new FactionsIConomyListener();
	public Integer givePlayersDaPowaReferenceInt = null;
	public Integer inactiveTimerReferenceInt = null;
	public static PermissionHandler Permissions;
	public static Help helpPlugin;

	// Commands
	public List<FBaseCommand> commands = new ArrayList<FBaseCommand>();

	private String baseCommand;
	
	public Factions() {
		Factions.instance = this;
	}
	
	public static iConomy getIConomy() 	{
		return iConomy;
	}
	
	public static boolean setIConomy(iConomy plugin) {
		if (iConomy == null)
			iConomy = plugin;
		else
			return false;
		return true;
	}
	
	@Override
	public void onEnable() {
		
		
		log("=== INIT START ===");
		long timeInitStart = System.currentTimeMillis();
		
		// Add the commands
		commands.add(new FCommandHelp());
		commands.add(new FCommandAccess());
		commands.add(new FCommandAdmin());
		commands.add(new FCommandBuy());
		commands.add(new FCommandBounty());
		commands.add(new FCommandChat());
		commands.add(new FCommandAllyChat());
		commands.add(new FCommandClaim());
		commands.add(new FCommandCreate());
		commands.add(new FCommandDeinvite());
		commands.add(new FCommandDescription());
		commands.add(new FCommandHome());
		commands.add(new FCommandInvite());
		commands.add(new FCommandJoin());
		commands.add(new FCommandKick());
		commands.add(new FCommandLeave());
		commands.add(new FCommandList());
		commands.add(new FCommandMap());
		commands.add(new FCommandMod());
		commands.add(new FCommandOpen());
		commands.add(new FCommandRelationAlly());
		commands.add(new FCommandRelationEnemy());
		commands.add(new FCommandRelationNeutral());
		commands.add(new FCommandSafeclaim());
		commands.add(new FCommandSethome());
		commands.add(new FCommandShow());
		commands.add(new FCommandTag());
		commands.add(new FCommandTitle());
		commands.add(new FCommandTrack());
		commands.add(new FCommandUnclaim());
		commands.add(new FCommandVersion());
		
		// Ensure basefolder exists!
		this.getDataFolder().mkdirs();
		
		Conf.load();
		FPlayer.load();
		Faction.load();
		Board.load();
		
		setupHelp();
		setupPermissions();
		
		// Register events
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_CHAT, this.playerListener, Event.Priority.Highest, this);
		pm.registerEvent(Event.Type.PLAYER_INTERACT, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_MOVE, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_JOIN, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_RESPAWN, this.playerListener, Event.Priority.High, this);
		pm.registerEvent(Event.Type.PLAYER_BUCKET_EMPTY, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_BUCKET_FILL, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_QUIT, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, this.playerListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.ENTITY_DEATH, this.entityListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.ENTITY_DAMAGE, this.entityListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.ENTITY_EXPLODE, this.entityListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.CREATURE_SPAWN, this.entityListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.ENTITY_TARGET, this.entityListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_BREAK, this.blockListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_DAMAGE, this.blockListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.BLOCK_PLACE, this.blockListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.CHUNK_LOAD, this.chunkListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.CHUNK_UNLOAD, this.chunkListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLUGIN_ENABLE, this.iConomyListener, Event.Priority.Monitor,this);
		
		pm.registerEvent(Event.Type.ENTITY_DEATH, new FBountyEntityListener(), Event.Priority.Normal, this);
		
		double redStoneRate = 0.005;
		double diamondRate = 0.01;
		//double lapisRate = 0.01;
		double goldRate = 0.025;
		double ironRate = 0.10;
		double clayRate = 0.05;
		double coalRate = 0.30;
		
		FactionsBlockListener.WoodDropRates.put(Material.COAL, coalRate);
		
		FactionsBlockListener.StoneDropRates.put(Material.IRON_ORE, ironRate);
		FactionsBlockListener.StoneDropRates.put(Material.COAL, coalRate);
		
		FactionsBlockListener.IronDropRates.put(Material.REDSTONE, redStoneRate);
		FactionsBlockListener.IronDropRates.put(Material.DIAMOND, diamondRate);
		//FactionsBlockListener.IronDropRates.put(Material.INK_SACK, lapisRate);
		FactionsBlockListener.IronDropRates.put(Material.GOLD_ORE, goldRate);
		FactionsBlockListener.IronDropRates.put(Material.IRON_ORE, ironRate);
		FactionsBlockListener.IronDropRates.put(Material.CLAY_BALL, clayRate);
		FactionsBlockListener.IronDropRates.put(Material.COAL, coalRate);	
		
		FactionsBlockListener.GoldDropRates.put(Material.REDSTONE, redStoneRate);
		FactionsBlockListener.GoldDropRates.put(Material.DIAMOND, diamondRate);
		//FactionsBlockListener.GoldDropRates.put(Material.INK_SACK, lapisRate);
		//FactionsBlockListener.GoldDropRates.put(Material.GOLD_ORE, goldRate);
		FactionsBlockListener.GoldDropRates.put(Material.IRON_ORE, ironRate);
		//FactionsBlockListener.GoldDropRates.put(Material.CLAY_BALL, clayRate);
		//FactionsBlockListener.GoldDropRates.put(Material.COAL, coalRate);	
		
		FactionsBlockListener.DiamondDropRates.put(Material.REDSTONE, redStoneRate);
		FactionsBlockListener.DiamondDropRates.put(Material.DIAMOND, diamondRate);
		//FactionsBlockListener.DiamondDropRates.put(Material.INK_SACK, lapisRate);
		FactionsBlockListener.DiamondDropRates.put(Material.GOLD_ORE, goldRate);
		FactionsBlockListener.DiamondDropRates.put(Material.IRON_ORE, ironRate);
		FactionsBlockListener.DiamondDropRates.put(Material.CLAY_BALL, clayRate);
		FactionsBlockListener.DiamondDropRates.put(Material.COAL, coalRate);	
		
		// Register recurring tasks
		long saveTicks = 20 * 60 * 30; // Approximately every 30 min
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new SaveTask(), saveTicks, saveTicks);
		
		if (givePlayersDaPowaReferenceInt == null) {
			givePlayersDaPowaReferenceInt = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
	            public void run() {
	            	log("updating power.");
	            	for (Player player : getServer().getOnlinePlayers()) {
	            		FPlayer.get(player).updatePower();
	            	}
	              }
	            }
	        , 30 * 21L, 120 * 21L);
		}
		
		if (inactiveTimerReferenceInt == null) {
			inactiveTimerReferenceInt = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
	            public void run() {
	            	log("removing inactive players");
	            	long fourDays = 4 * 24 * 60 * 60 * 1000;
	            	for (FPlayer fplayer : FPlayer.getAll()) {
	            		if (System.currentTimeMillis() - fplayer.getLastLoginTime() > fourDays) {
	            			fplayer.setPower(0);
	            		}
	            	}
	              }
	            }
	        , 20 * 20L, 24 * 60 * 60 * 20L);
		}
		/*
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
            	Random randomGenerator = new Random();
            	for (Player player : getServer().getOnlinePlayers()) {
            		World world = player.getWorld();
            		
            		int max = 120;
            		
            		int xOffset = randomGenerator.nextInt(max) - (max/2);
            		int zOffset = randomGenerator.nextInt(max) - (max/2);
            		
            		Location l = player.getLocation().clone();
            		l.setX(l.getX() + 10 + xOffset);
            		l.setZ(l.getZ() + 10 + zOffset);
            		l.setY(world.getHighestBlockYAt(l) - 1);
            		
            		Block bb = world.getBlockAt(l);
            		
            		TreeType type = TreeType.BIG_TREE; //TreeType.values()[randomGenerator.nextInt(TreeType.values().length)];            		
            		
            		Material m = bb.getType();
            		if (m == Material.DIRT || m == Material.GRASS)
            		{
            			l.setY(l.getY() + 1);
            			player.getWorld().generateTree(l, type);
            		}
            	}
              }
            }
        , 240 * 21L, 240 * 21L); */
			
		log("=== INIT DONE (Took "+(System.currentTimeMillis()-timeInitStart)+"ms) ===");
	}

	@Override
	public void onDisable() {
		
		saveAll();
		
		if (givePlayersDaPowaReferenceInt != null) {
			getServer().getScheduler().cancelTask(givePlayersDaPowaReferenceInt);
			givePlayersDaPowaReferenceInt = null;
		}
		if (inactiveTimerReferenceInt != null) {
			getServer().getScheduler().cancelTask(inactiveTimerReferenceInt);
			inactiveTimerReferenceInt = null;
		}
		
		log("Disabled");
	}

	// -------------------------------------------- //
	// Integration with other plugins
	// -------------------------------------------- //
	
	private void setupHelp() {
		if (helpPlugin != null) {
			return;
		}
		
		Plugin test = this.getServer().getPluginManager().getPlugin("Help");
		
		if (test != null) {
			helpPlugin = ((Help) test);
			Factions.log("Found and will use plugin "+helpPlugin.getDescription().getFullName());
			helpPlugin.registerCommand(this.getBaseCommand()+" help *[page]", "Factions plugin help.", this, false);
			helpPlugin.registerCommand("help factions", "instead use: /f help", helpPlugin, true);
		}
	}
	
	private void setupPermissions() {
		if (Permissions != null) {
			return;
		}
		
		Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
		
		if (test != null) {
			Permissions = ((Permissions)test).getHandler();
			Factions.log("Found and will use plugin "+((Permissions)test).getDescription().getFullName());
		} else {
			Factions.log("Permission system not detected, defaulting to OP");
		}
	}
	
	// -------------------------------------------- //
	// Test rights
	// -------------------------------------------- //
	
	public static boolean hasPermParticipate(CommandSender sender) {
		return hasPerm(sender, "factions.participate", false);
	}
	
	public static boolean hasPermCreate(CommandSender sender) {
		return hasPerm(sender, "factions.create", false);
	}
	
	public static boolean hasPermManageSafeZone(CommandSender sender) {
		return hasPerm(sender, "factions.manageSafeZone", true);
	}
	
	private static boolean hasPerm(CommandSender sender, String permNode, boolean fallbackOnlyOp) {
		if (Factions.Permissions == null || ! (sender instanceof Player)) {
			return fallbackOnlyOp == false || sender.isOp();
		}
		
		if (sender instanceof Player) {
			Player player = (Player)sender;
			return Factions.Permissions.has(player, permNode); 
		}
		
		return false;
	}
	
	// -------------------------------------------- //
	// Commands
	// -------------------------------------------- //
	
	@SuppressWarnings("unchecked")
	public String getBaseCommand() {
		if (this.baseCommand != null) {
			return this.baseCommand;
		}
		
		Map<String, Object> Commands = (Map<String, Object>)this.getDescription().getCommands();
		this.baseCommand = Commands.keySet().iterator().next();
		return this.baseCommand;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		List<String> parameters = new ArrayList<String>(Arrays.asList(args));
		this.handleCommand(sender, parameters);
		return true;
	}
	
	public void handleCommand(CommandSender sender, List<String> parameters) {
		if (parameters.size() == 0) {
			this.commands.get(0).execute(sender, parameters);
			return;
		}
		
		String commandName = parameters.get(0).toLowerCase();
		parameters.remove(0);
		
		for (FBaseCommand fcommand : this.commands) {
			if (fcommand.getAliases().contains(commandName)) {
				fcommand.execute(sender, parameters);
				return;
			}
		}
		
		sender.sendMessage(Conf.colorSystem+"Unknown faction command \""+commandName+"\". Try "+Conf.colorCommand+"/f help");
	}
	
	// -------------------------------------------- //
	// Logging
	// -------------------------------------------- //
	public static void log(String msg) {
		log(Level.INFO, msg);
	}
	
	public static void log(Level level, String msg) {
		Logger.getLogger("Minecraft").log(level, "["+instance.getDescription().getFullName()+"] "+msg);
	}
	
	// -------------------------------------------- //
	// Save all
	// -------------------------------------------- //
	
	public static void saveAll() {
		FPlayer.save();
		Faction.save();
		Board.save();
		Conf.save();
	}
	
	public int startLogOffTimer (String playerName) {
		final String pName = new String(playerName);
		if (givePlayersDaPowaReferenceInt != null) {
			return getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
	            public void run() {
	            	FPlayer.playerRanFromBattle(pName);
	              }
	            }
	        , 20 * 15L);
		};
		return 0;
	}
}
