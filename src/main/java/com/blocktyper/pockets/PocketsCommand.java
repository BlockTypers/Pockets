package com.blocktyper.pockets;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.blocktyper.serialization.CardboardBox;

public class PocketsCommand implements CommandExecutor {

	private static String COMMAND_POCKETS = "pockets";
	private static String COMMAND_PANTS = "pockets-pants";

	private PocketsPlugin plugin;

	public PocketsCommand(PocketsPlugin plugin) {
		this.plugin = plugin;
		plugin.getCommand(COMMAND_POCKETS).setExecutor(this);
		plugin.info("'/" + COMMAND_POCKETS + "' registered");

		plugin.getCommand(COMMAND_PANTS).setExecutor(this);
		plugin.info("'/" + COMMAND_PANTS + "' registered");
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			return false;
		}

		Player player = (Player) sender;

		if (label.toLowerCase().equals(COMMAND_PANTS)) {
			return givePants(player);
		} else if (label.toLowerCase().equals(COMMAND_POCKETS)) {
		}

		return true;
	}

	protected boolean givePants(Player player) {
		ItemStack pants = new ItemStack(Material.DIAMOND_LEGGINGS);
		ItemMeta meta = pants.getItemMeta();

		List<String> lore = new ArrayList<>();

		ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);

		CardboardBox contents = new CardboardBox(sword);

		Pocket pocket = new Pocket();
		pocket.getContents().add(contents);

		String pocketsJson = PocketsPlugin.JSON_HELPER.toJson(pocket);

		String pocketsText = PocketsPlugin.POCKETS_KEY + pocketsJson;

		lore.add(convertToInvisibleString(pocketsText));

		meta.setLore(lore);
		pants.setItemMeta(meta);

		player.getWorld().dropItem(player.getLocation(), pants);

		return true;
	}

	private String convertToInvisibleString(String s) {
		String hidden = "";
		for (char c : s.toCharArray())
			hidden += ChatColor.COLOR_CHAR + "" + c;
		return hidden;
	}

}
