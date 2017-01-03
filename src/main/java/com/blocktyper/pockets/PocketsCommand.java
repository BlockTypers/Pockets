package com.blocktyper.pockets;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PocketsCommand implements CommandExecutor {

	private static String COMMAND_POCKETS = "pockets";

	// private PocketsPlugin plugin;

	public PocketsCommand(PocketsPlugin plugin) {
		// this.plugin = plugin;
		plugin.getCommand(COMMAND_POCKETS).setExecutor(this);
		plugin.info("'/" + COMMAND_POCKETS + "' registered");
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}

		// Player player = (Player) sender;

		return true;
	}

}
