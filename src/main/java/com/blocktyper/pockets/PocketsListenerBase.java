package com.blocktyper.pockets;

import org.bukkit.event.Listener;

public abstract class PocketsListenerBase implements Listener {

	protected PocketsPlugin plugin;

	public PocketsListenerBase(PocketsPlugin plugin) {
		this.plugin = plugin;
		this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
}
