package com.nidefawl.Achievements.Commands;

import java.util.logging.Logger;
import java.util.ArrayList;

import org.bukkit.entity.Player;
import com.nidefawl.Achievements.Achievements;
import com.nidefawl.Achievements.Messaging.AchMessaging;
import com.nidefawl.Stats.StatsSettings;

public class AchCommandHandler {
	private boolean empty;
	private ArrayList<String[]> commandList = new ArrayList<String[]>();

	static final Logger log = Logger.getLogger("Minecraft");
	public Achievements plugin = null;

	public AchCommandHandler(Achievements plugin, String commands) {
		this.empty = true;
		if (commands == null)
			return;
		this.plugin = plugin;
		String[] split = commands.split(";");
		for (String c : split) {
			c = c.replaceAll("<semicolon>", ":");
			if (c.length() <= 1)
				continue;
			String[] s = c.split(" ");
			if (s.length < 2) {
				Achievements.LogError("Invalid command " + c);
				continue;
			}
			this.commandList.add(s);
			this.empty = false;
		}
	}

	protected static boolean checkInv(Achievements plugin, Player player, String[] s) {
		if (s.length < 3) {
			Achievements.LogError("Bad command '" + s[0] + "' (not enough arguments) correct is: item <itemname> <amount>");
			return false;
		}
		if (plugin.Stats() == null) {
			Achievements.LogError("Cannot resolve item names!");
			return false;
		}
		int item = plugin.Stats().getItems().getItem(s[1]);
		if (item == 0) {
			Achievements.LogError("Bad command '" + s[0] + " " + s[1] + " " + s[2] + "' (invalid item) correct is: item <itemname> <amount>");
			return false;
		}
		try {
			Integer.parseInt(s[2]);
		} catch (NumberFormatException ex) {
			Achievements.LogError("Bad command '" + s[0] + " " + s[1] + " " + s[2] + "'(amount is not a number) correct is: item <itemname> <amount>");
			return false;
		}
		if (player.getInventory().firstEmpty() == -1) {
			AchMessaging.send(player, plugin.color + "An achievement reward item is waiting for you!");
			AchMessaging.send(player, plugin.color + "You should get some free space in your inventory!");
			return false;
		}
		return true;
	}
	
	public boolean preCheck(Player player) {

		if (isEmpty())
			return true;
		for (String[] s : commandList) {
			if (s[0].equalsIgnoreCase("item")) {
				if (!checkInv(plugin, player, s))
					return false;
			}
		}
		return true;
	}

	public boolean preCheck() {
		if (isEmpty())
			return true;
		for (String[] s : commandList) {
			if (s[0].equalsIgnoreCase("item")) {
				if (s.length < 3) {
					Achievements.LogError("Bad command '" + s[0] + "' (not enough arguments) correct is: item <itemname> <amount>");
					return false;
				}
				if (plugin.Stats() == null) {
					Achievements.LogError("Cannot resolve item names!");
					return false;
				}
				int item = plugin.Stats().getItems().getItem(s[1]);
				if (item == 0) {
					Achievements.LogError("Bad command '" + s[0] + " " + s[1] + " " + s[2] + "' (invalid item) correct is: item <itemname> <amount>");
					return false;
				}
				try {
					Integer.parseInt(s[2]);
				} catch (NumberFormatException ex) {
					Achievements.LogError("Bad command '" + s[0] + " " + s[1] + " " + s[2] + "'(amount is not a number) correct is: item <itemname> <amount>");
					return false;
				}
			}
		}
		return true;
	}

	public void run(Player player) {
		if (isEmpty())
			return;

		for (String[] s : commandList) {
			if (s[0].startsWith("/")) {
				if(plugin.consoleCommandsAllowed()) { 
					if (StatsSettings.debugOutput)
						Achievements.LogInfo("Executing command: " + s[0] + "");
					Achievements.LogInfo("Executing command: " + s[0] + "");
					AchCommandNativeCommand.handleCommand(plugin, player, s);
				} else {
					Achievements.LogInfo("Could not execute "+s[0]);
					Achievements.LogInfo("Console commands are disabled. check achievements.properties!");
				}
				continue;
			}
			Achievements.LogInfo("Could not execute "+s[0]);
			Achievements.LogInfo("Unsupported command. Check achievements.properties!");
		}
	}

	public boolean isEmpty() {
		return this.empty;
	}

	@Override
	public String toString() {
		boolean second = false;

		if (isEmpty())
			return "";

		String ret = "";
		for (String[] s : commandList) {
			if (second)
				ret = ret + ";";
			for (String c : s) {
				ret = ret + c + " ";
			}
			second = true;
		}
		return ret;
	}

}
