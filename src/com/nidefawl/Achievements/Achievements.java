package com.nidefawl.Achievements;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.File;
import java.util.logging.Logger;
import java.util.logging.Level;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import com.ensifera.animosity.craftirc.CraftIRC;
import com.nidefawl.Achievements.Messaging.AchLister;
import com.nidefawl.Achievements.Messaging.AchMessaging;
import com.nidefawl.Stats.Stats;
import com.nidefawl.Stats.StatsSettings;
import com.nidefawl.Stats.datasource.StatsSQLConnectionManager;

public class Achievements extends JavaPlugin {
	public final static Logger log = Logger.getLogger("Minecraft");
	public String color = "&b";
	public String obtainedColor = "&a";
	public boolean enabled = false;
	public boolean useSQL = false;
	public boolean useTreasurehuntCommand = false;
	public boolean useDiscoveryMode = false;
	public static int RangeDiscoveryMode = 3;
	public boolean useCraftIRC = false;
	public final static Double version = 1.0;
	public static String logprefix = "[Achievements 2.0.1]";
	private final static Yaml yaml = new Yaml(new SafeConstructor());
	//private String name = "Achievements";
	private String listLocation = "achievements.txt";
	private long delay = 300;
	private Stats statsInstance = null;
	private AchievementsListener listener = null;
	public HashMap<String, AchievementListData> achievementList = new HashMap<String, AchievementListData>();
	public HashMap<String, PlayerAchievement> playerAchievements = new HashMap<String, PlayerAchievement>();
	public List<String> formatAchDetail = new ArrayList<String>();
	public List<String> formatAchList = new ArrayList<String>();
	public String formatAchNotifyBroadcast = "+playername has been awarded +achname!";
	public String formatAchNotifyPlayer = null;
	public ArrayList<String> ircTags = new ArrayList<String>();
	protected final Object achsLock = new Object();
	public int achsPerPage = 8;
	public static String dbPlayerAchievementsTable;
	public static String dbAchievementsTable;
	private boolean allowConsoleCommands = false;
	public static boolean useSQLDefinitions = false;
	public final static Double getVersion() {
		return version;
	}
	
	public Permission permission = null;
	public Economy economy = null;

	private AchievementPlayerListener playerListener = null;
	public Location playerDistOld = null;
	public Location playerDistNew = null;
	public World playerWorldOld = null;
	public World playerWorldNew = null;
	
	private Boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }
	
	private Boolean setupPermissions()
    {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }
	
	protected class AchievementsServerListener extends ServerListener {
		Achievements plugin = null;

		public AchievementsServerListener(Achievements plugin) {
			this.plugin = plugin;
		}

		@Override
		public void onPluginEnable(PluginEnableEvent event) {
			if (event.getPlugin().getDescription().getName().equals("Stats")) {
				if (this.plugin.enabled) {
					this.plugin.Disable();
				}
				if(this.plugin.checkStatsPlugin()) {
					this.plugin.Enable();	
				}
			}
		}
	}
	public boolean checkStatsPlugin() {
		if (statsInstance != null)
			return true;
		Plugin plug = getServer().getPluginManager().getPlugin("Stats");
		if (plug == null) {
			return false;
		}
		if (!plug.isEnabled()) {
			return false;
		}
		statsInstance = (Stats) plug;
		if(!statsInstance.enabled||statsInstance.updated) {
			return false;
		}
		LogInfo("Found required plugin: " + plug.getDescription().getName());
		return true;
	}

	public Stats Stats() {
		checkStatsPlugin();
		return statsInstance;
	}

	public boolean loadConfig() {
		this.enabled 									= false;
		AchPropertiesFile properties 					= new AchPropertiesFile(new File(getDataFolder(), "achievements.properties"));
		
		this.listLocation 								= properties.getString("achievements-list", "achievements.txt", "The name of your achievements file; default: achievements.txt");
		this.delay 										= (long) properties.getInt("achievements-delay", 60, "Delay in seconds, Achivements will check for new achievements.");
		this.allowConsoleCommands 						= properties.getBoolean("achievements-console-commands", true, "Important: this needs to be true to use commands! Use * for players name: /give * stone 16");
		this.useSQL 									= properties.getBoolean("achievements-use-sql", false, "Should Achievements use SQL for storage?");
		this.useTreasurehuntCommand 					= properties.getBoolean("treasurehunt-command", true, "Needs to be true if you want to use /treasure command.");
		this.useDiscoveryMode 							= properties.getBoolean("discovery-mode", true, "Needs to be true if players should be allowed to discover playes automatically.");
		Achievements.RangeDiscoveryMode 				= properties.getInt("discovery-mode-range", 3, "After this number of Blocks (Range) Achievements will start a discovery-check. 3 is default.");
		Achievements.useSQLDefinitions 					= properties.getBoolean("achievements-definitions-sql", false, "Should Achievements use SQL for Definitions?");
		Achievements.dbAchievementsTable 				= properties.getString("sql-table-achievements", "achievements", "The name of your definitions table.");
		Achievements.dbPlayerAchievementsTable 			= properties.getString("sql-table-playerachievements", "playerachievements", "The name of your playerachievements table.");
		this.color 										= ("&" + properties.getString("achievements-color", "b", ""));
		this.obtainedColor 								= ("&" + properties.getString("achievements-obtainedcolor", "a", ""));
		this.achsPerPage 								= properties.getInt("achievements-list-perpage", 8, "");
		this.formatAchNotifyBroadcast 					= properties.getString("achievements-format-notifybroadcast", "&b+playername has been awarded +achname!", "check documentation for details");
		this.formatAchNotifyPlayer 						= properties.getString("achievements-format-notifyplayer", "(+description)", "");
		this.formatAchList.add(properties.getString("achievements-format-list", "+id +shortenedachname &6[&f+category&6 - &f+key&6:&f +value&6]", "check documentation for details"));
		this.formatAchList.add(properties.getString("achievements-format-list2", "", ""));
		this.formatAchDetail.add(properties.getString("achievements-format-detail", "+id Name: +achname", ""));
		this.formatAchDetail.add(properties.getString("achievements-format-detail2", "Desc: +description", ""));
		this.formatAchDetail.add(properties.getString("achievements-format-detail3", "Requirement: &6[&f+category&6 - &f+key&6:&f +value&6]", ""));
		this.formatAchDetail.add(properties.getString("achievements-format-detail4", "Reward: &6[&f+reward&6]", ""));
		this.formatAchDetail.add(properties.getString("achievements-format-detail5", "", ""));
		this.formatAchDetail.add(properties.getString("achievements-format-detail6", "", ""));
		this.formatAchDetail.add(properties.getString("achievements-format-detail7", "", ""));
		this.formatAchDetail.add(properties.getString("achievements-format-detail8", "", ""));
		this.useCraftIRC 								= properties.getBoolean("achievements-craftirc", checkCraftIRC(), "");
		ircTags.clear();
		for (String tag : properties.getString("achievements-craftirc-tags", "default", "by comma seperated CraftIRC tags").split(",")) {
			ircTags.add(tag.trim());
		}
		properties.save();

		if (!checkStatsPlugin()) {
			Disable();
			return false;
		}
		if (useCraftIRC && getServer().getPluginManager().getPlugin("CraftIRC") == null) {
			LogInfo("CraftIRC not found. Disabling this feature");
		}
		return true;
	}


	public int getIndexOfAch(String achName) {
		int index = 0;
		for (String a : achievementList.keySet()) {
			if (a.equals(achName))
				return index;
			index++;
		}
		return -1;
	}

	public void onEnable() {
		
		if (!setupPermissions()) {
            System.out.println("Null perm");
           //use these if you require econ
          //getServer().getPluginManager().disablePlugin(this);
          //return;
        }
		
		if (!setupEconomy()) {
            System.out.println("Null econ");
           //use these if you require econ
          //getServer().getPluginManager().disablePlugin(this);
          //return;
        }
		
		
		getDataFolder().mkdirs();
		File achDirectory = new File("achievements");
		if (achDirectory.exists() && achDirectory.isDirectory()) {
			LogInfo("Moving ./achievements/ directory to " + getDataFolder().getPath());
			if (!achDirectory.renameTo(new File(getDataFolder().getPath()))) {
				LogError("Moving ./achievements/ directory to " + getDataFolder().getPath() + " failed");
				LogError("Please move your files manually and delete the old 'achievements' directory. Thanks");
				LogError("Disabling Achievements");
				getServer().getPluginManager().disablePlugin(this);
				return;
			}
		}
		if (!checkStatsPlugin()) {
			AchievementsServerListener srvListener = new AchievementsServerListener(this);
			getServer().getPluginManager().registerEvent(Event.Type.PLUGIN_ENABLE, srvListener, Priority.Normal, this);
		} else {
			Enable();
		}
		
		if (useDiscoveryMode == true) {
			playerListener = new AchievementPlayerListener(this);
			getServer().getPluginManager().registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Monitor, this);
			LogInfo("Using Discovery Mode!");
		}
		
		if (useTreasurehuntCommand == true) {
			LogInfo("Using Treasurehunt Mode!");
		}
	}

	public void Enable() {
		formatAchDetail = new ArrayList<String>();
		formatAchList = new ArrayList<String>();
		achievementList = new HashMap<String, AchievementListData>();
		playerAchievements = new HashMap<String, PlayerAchievement>();
		checkStatsPlugin();
		checkCraftIRC();
		
		if (!loadConfig())
			return;

		if (useSQL) {
			if (StatsSQLConnectionManager.getConnection(true) == null) {
				LogError("MySQL-Connection could not be established");
				return;
			}
			if (!checkSchema()) {
				LogError("Could not create tables. Achievements is disabled");
				return;
			}
		}
		reloadList();
		listener = new AchievementsListener();
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, listener, Priority.Normal, this);
		getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, listener, Priority.Normal, this);
		LogInfo(logprefix + " Plugin Enabled");


		getServer().getScheduler().scheduleSyncRepeatingTask(this, new AchievementsCheckerTask(this), delay * 20, delay * 20);
	}
	public void reloadList() {
		this.enabled = false;
		achievementList = AchievementsLoader.LoadAchievementsList(this, getDataFolder().getPath(), listLocation);
		if (achievementList == null || achievementList.isEmpty()) {
			LogInfo((useSQLDefinitions ? "table " + dbAchievementsTable : listLocation) + " is empty");
			Disable();
			return;
		}
		LogInfo("loaded " + achievementList.size() + " achievements definitions");
		playerAchievements = new HashMap<String, PlayerAchievement>();
		for (Player p : getServer().getOnlinePlayers()) {
			load(p);
		}
		this.enabled = true;
	}


	public void Disable() {

		if (enabled) {
			enabled = false;
			getServer().getScheduler().cancelTasks(this);
			playerAchievements = null;
		}
		LogInfo(logprefix + " Plugin Disabled");
	}

	public void onDisable() {
		if(checkStatsPlugin()) {
			Disable();
		}
	}

	public CraftIRC craftirc = null;

	public boolean checkCraftIRC() {
		if (craftirc != null)
			return true;
		Plugin plug = this.getServer().getPluginManager().getPlugin("CraftIRC");
		if (plug != null && plug.isEnabled()) {
			craftirc = (CraftIRC) plug;

			LogInfo("Found supported plugin: " + plug.getDescription().getName());
			return true;
		}
		return false;
	}

	public AchievementListData getAchievement(String name) {
		return achievementList.get(name);
	}

	public Achievements() {
	}
	
	public void checkAchievements() {
		if (!enabled ||!checkStatsPlugin() || Stats().enabled == false)
			return;

		if (StatsSettings.debugOutput)
			LogInfo("checking achievements...");
		for (Player p : getServer().getOnlinePlayers()) {
			if (!Stats().permission.has(p, "achievements.check"))
				continue;
			if (!playerAchievements.containsKey(p.getName()))
				load(p);
			PlayerAchievement pa = playerAchievements.get(p.getName());
			for (String name2 : achievementList.keySet()) {
				AchievementListData ach = achievementList.get(name2);
				if (ach == null || !ach.isEnabled())
					continue;
				if (!ach.conditions.meets(this, p, pa))
					continue;
				int playerValue = Stats().get(p.getName(), ach.getCategory(), ach.getKey());

				if (playerValue < ach.getValue()) // doesn't meet requirements,
					// skip
					continue;
				// award achievement
				if (pa.hasAchievement(ach)) {
					Achievement pad = pa.get(ach.getName());

					// already awarded
					if (pad.getCount() >= ach.getMaxawards())
						continue;

					if (pad.getCount() > 0 && playerValue < ((pad.getCount() + 1) * ach.getValue()))
						continue;

					// check for inventory space
					// player will get rewarded next check if there is no free
					// space
					if (!ach.commands.preCheck(p))
						continue;
					pad.incrementCount();
				} else {

					// check for inventory space
					// player will get rewarded next check if there is no free
					// space
					if (!ach.commands.preCheck(p))
						continue;
					pa.award(ach.getName());
				}

				AchLister.sendAchievementMessage(this, p, ach);
				pa.save();

				ach.commands.run(p);
			}
		}
	}
	
	//public void checkDiscover(Player player, double LocX, double LocY, double LocZ, Location playerLoc) {
	public void checkDiscover(Player player) {
		
		Location playerLoc = player.getLocation();
			
		if (!enabled ||!checkStatsPlugin() || Stats().enabled == false)
			return;

		if (StatsSettings.debugOutput) {
			LogInfo("Check " + player + " at location " + playerLoc);
		}
		for (String name2 : achievementList.keySet()) {

			AchievementListData ach = achievementList.get(name2);
			if (ach == null || !ach.isEnabled())
				continue;
			if (!ach.category.contains("discover"))
				continue;
			String DestLoc = ach.key;
			String[] split = DestLoc.split(",");
			double DestX = Integer.parseInt (split[0]);
			double DestY = Integer.parseInt (split[1]);
			double DestZ = Integer.parseInt (split[2]);
			
			Location DestLocDist = new Location(player.getWorld(), DestX, DestY, DestZ);
			//DestLocDist.setX(DestX);
			//DestLocDist.setY(DestY);
			//DestLocDist.setZ(DestZ);
			//DestLocDist.add(DestX, DestY, DestZ);
			
			double playerDist = playerLoc.distance(DestLocDist);
			
			if (playerDist > ach.value) {// doesn't meet requirements,
				// skip
				continue;
			}
			
			PlayerAchievement pa = playerAchievements.get(player.getName());
			// award achievement
			if (pa.hasAchievement(ach)) {
				
				Achievement pad = pa.get(ach.getName());

				// already awarded
				if (pad.getCount() >= ach.getMaxawards())
					continue;

				// check for inventory space
				// player will get rewarded next check if there is no free
				// space
				if (!ach.commands.preCheck(player))
					continue;
				pad.incrementCount();
			} else {

				// check for inventory space
				// player will get rewarded next check if there is no free
				// space
				if (!ach.commands.preCheck(player))
					continue;
				pa.award(ach.getName());
			}

			AchLister.sendAchievementMessage(this, player, ach);
			pa.save();

			ach.commands.run(player);
		}
	}
	
	public void checkTreasure(Player player) {
		
		Location playerLoc = player.getLocation();
			
		if (!enabled ||!checkStatsPlugin() || Stats().enabled == false)
			return;

		if (StatsSettings.debugOutput) {
			LogInfo("Check " + player + " at location " + playerLoc);
		}
		for (String name2 : achievementList.keySet()) {

			AchievementListData ach = achievementList.get(name2);
			if (ach == null || !ach.isEnabled())
				continue;
			if (!ach.category.contains("treasurehunt"))
				continue;
			String DestLoc = ach.key;
			String[] split = DestLoc.split(",");
			double DestX = Integer.parseInt (split[0]);
			double DestY = Integer.parseInt (split[1]);
			double DestZ = Integer.parseInt (split[2]);
			
			Location DestLocDist = new Location(player.getWorld(), DestX, DestY, DestZ);
			//DestLocDist.setX(DestX);
			//DestLocDist.setY(DestY);
			//DestLocDist.setZ(DestZ);
			//DestLocDist.add(DestX, DestY, DestZ);
			
			double playerDist = playerLoc.distance(DestLocDist);
			
			if (playerDist > ach.value) {// doesn't meet requirements,
				// skip
				AchMessaging.send(player, ChatColor.LIGHT_PURPLE + "You didn't find a treasure here.");
				continue;
			}
			
			PlayerAchievement pa = playerAchievements.get(player.getName());
			// award achievement
			if (pa.hasAchievement(ach)) {
				
				Achievement pad = pa.get(ach.getName());

				// already awarded
				if (pad.getCount() >= ach.getMaxawards())
					continue;

				// check for inventory space
				// player will get rewarded next check if there is no free
				// space
				if (!ach.commands.preCheck(player))
					continue;
				pad.incrementCount();
			} else {

				// check for inventory space
				// player will get rewarded next check if there is no free
				// space
				if (!ach.commands.preCheck(player))
					continue;
				pa.award(ach.getName());
			}

			AchLister.sendAchievementMessage(this, player, ach);
			pa.save();

			ach.commands.run(player);
		}
	}

	public void load(Player player) {
		if (!Stats().permission.has(player, "achievements.check")) {
			if (StatsSettings.debugOutput)
				LogInfo("player " + player.getName() + " has no achievements.check permission. Not loading");
			return;
		}
		if (playerAchievements.containsKey(player.getName())) {
			if (StatsSettings.debugOutput)
				LogInfo(player.getName() + " is already in list. skipping");
			return;
		}
		PlayerAchievement pa;
		if (useSQL) {
			String location = getDataFolder().getPath() + File.separator + player.getName() + ".txt";
			File fold = new File(location);
			pa = new PlayerAchievementSQL(player.getName());
			if (fold.exists()) {
				PlayerAchievement paold = new PlayerAchievementFile(getDataFolder().getPath(), player.getName());
				paold.load();
				File fnew = new File(location + ".old");
				fold.renameTo(fnew);
				pa.copy(paold);
				pa.save();
			}
		} else
			pa = new PlayerAchievementFile(getDataFolder().getPath(), player.getName());
		pa.load();
		playerAchievements.put(player.getName(), pa);
		if (StatsSettings.debugOutput)
			LogInfo("Loaded Player '" + player.getName() + "'");

	}

	public void unload(String player) {
		if (!playerAchievements.containsKey(player)) {
			return;
		}
		PlayerAchievement pa = playerAchievements.get(player);
		pa.save();
		playerAchievements.remove(player);
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		final Player player = (Player)sender;
		if(sender instanceof Player) {
			load(player);
			if (cmd.getName().equals("achievements") && Stats().permission.has(player, "achievements.view.own")) {
				if (playerAchievements == null || playerAchievements.get(player.getName()) == null) {
					AchMessaging.send(player, ChatColor.LIGHT_PURPLE + "You have no achievements.");
					return true;
				}
				AchLister.SendDoneAchList(this, player, args);
				return true;
			}

			if (cmd.getName().equals("listachievements") && Stats().permission.has(player, "achievements.view.list")) {
				AchLister.SendAchList(this, player, args);
				return true;
			}
			
			if (useTreasurehuntCommand == true){
				if (commandLabel.equals("treasure") && permission.has(player, "achievements.treasurehunt")) {
					checkTreasure(player);
					return true;
				}
				else if (commandLabel.equals("treasure") && !permission.has(player, "achievements.treasurehunt")){
					AchMessaging.send(player, ChatColor.RED + "You aren't allowed to join the treasure hunt!");
				}				
			}
			else if(useTreasurehuntCommand != true){
				AchMessaging.send(player, ChatColor.RED + "The Treasure Hunt Feature is not enabled!");
			}
		}
		if (cmd.getName().equals("checkachievements") && Stats().permission.has(player, "achievements.admin.check")) {
			checkAchievements();
			sender.sendMessage(ChatColor.LIGHT_PURPLE + "Achievements updated.");
			return true;
		} else if (cmd.getName().equals("reloadachievements") && Stats().permission.has(player, "achievements.admin.reload")) {
			reloadList();
			sender.sendMessage(ChatColor.LIGHT_PURPLE + "Achievements reloaded.");
			return true;
		} 
		return false;
	}

	public static Yaml getYaml() {
		return yaml;
	}

	public class AchievementsListener extends PlayerListener {
		public void onPlayerJoin(PlayerJoinEvent event) {
			load(event.getPlayer());
		}

		public void onPlayerQuit(PlayerQuitEvent event) {
			unload(event.getPlayer().getName());
		}
	}

	private boolean checkSchema() {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean result = false;
		try {
			conn = StatsSQLConnectionManager.getConnection(true);
			DatabaseMetaData dbm = conn.getMetaData();
			rs = dbm.getTables(null, null, dbPlayerAchievementsTable, null);
			if (!rs.next()) {
				ps = conn.prepareStatement("CREATE TABLE `" + dbPlayerAchievementsTable + "` (`player` varchar(32) NOT NULL DEFAULT '-',`achievement` varchar(128) NOT NULL DEFAULT '-',`count` int(11) NOT NULL DEFAULT '0',PRIMARY KEY (`player`,`achievement`));");
				ps.executeUpdate();
				LogInfo("created table '" + dbPlayerAchievementsTable + "'.");
				result = true;
			} else {
				try {
					rs = dbm.getColumns(null, null, dbPlayerAchievementsTable, "achievement");

					if (rs.next() && rs.getInt("COLUMN_SIZE") != 128) {
						LogError("Outdated database!");
						LogError("UPGRADING FROM v0.16 TO v0.20");
						try {
							Statement statement = conn.createStatement();
							statement.executeUpdate("ALTER TABLE  `" + dbPlayerAchievementsTable + "` CHANGE  `achievement`  `achievement` VARCHAR( 128 ) NOT NULL DEFAULT  '-'");
							statement.close();
							LogError("UPGRADING SUCCESSFUL");

							result = true;
						} catch (SQLException e_) {
							LogError("UPGRADING FAILED");
							LogError(e_.getMessage() + e_.getSQLState());
							result = false;
						}
					} else {
						result = true;
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			rs.close();
			if(useSQLDefinitions && result) {
				rs = dbm.getTables(null, null, dbAchievementsTable, null);
				if (!rs.next()) {
					ps = conn.prepareStatement("CREATE TABLE `" + dbAchievementsTable + "` (`id` int(11) NOT NULL auto_increment, `enabled` tinyint(1) NOT NULL, `name` varchar(128) NOT NULL, `description` text NOT NULL, `category` varchar(32) NOT NULL, `stat` varchar(32) NOT NULL, `value` int(11) NOT NULL, `maxawards` int(11) NOT NULL, `commands` text NOT NULL, `conditions` text NOT NULL,  PRIMARY KEY  (`id`) ) ENGINE=MyISAM  DEFAULT CHARSET=latin1;");
					ps.executeUpdate();
					ps.close();
					ps = conn.prepareStatement("ALTER TABLE  `achievements` ADD UNIQUE (`name`);");
					ps.executeUpdate();
					LogInfo("created table '" + dbAchievementsTable + "'.");
					result = true;
				} else {
					result = true;
				}
			}
		} catch (SQLException ex) {
			LogError("SQL exception" + ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (rs != null)
					rs.close();
				if (conn != null)
					conn.close();
			} catch (SQLException ex) {
				LogError("SQL exception on close" + ex);
			}
		}
		return result;
	}



	public static void LogError(String string) {
		Achievements.log.log(Level.SEVERE, Achievements.logprefix + " " + string);
	}

	public static void LogInfo(String string) {
		Achievements.log.log(Level.INFO, Achievements.logprefix + " " + string);
	}

	public void onLoad() {

	}

	/**
	 * @return the allowNativeCommands
	 */
	public boolean consoleCommandsAllowed() {
		return allowConsoleCommands;
	}
	
}
