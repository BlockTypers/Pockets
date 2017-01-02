package com.blocktyper.pockets;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

public class PocketDelayOpener extends BukkitRunnable {

	private Player player;
	private Inventory inventory;

	public PocketDelayOpener(PocketsPlugin plugin, Player player, Inventory inventory) {
		super();
		this.player = player;
		this.inventory = inventory;
	}

	public void run() {
		player.closeInventory();
		player.openInventory(inventory);
	}

}
