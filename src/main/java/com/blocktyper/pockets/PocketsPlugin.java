package com.blocktyper.pockets;

import java.util.ResourceBundle;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.blocktyper.plugin.BlockTyperPlugin;
import com.blocktyper.pockets.utils.PocketsUtils;

public class PocketsPlugin extends BlockTyperPlugin implements CommandExecutor {

	public static final String RESOURCE_NAME = "com.blocktyper.pockets.resources.PocketsMessages";

	private String pocketName;
	private InventoryClickListener inventoryClickListener;

	public PocketsPlugin() {
		super();
	}

	public void onEnable() {
		super.onEnable();
		inventoryClickListener = new InventoryClickListener(this);
		new BlockPlaceListener(this);
		pocketName = getConfig().getString(ConfigKeyEnum.POCKET_NAME.getKey());
		PocketsUtils.registerPocketRecipes(this);
		this.getCommand("pockets-test").setExecutor(this);
	}

	@Override
	public void onDisable() {
		super.onDisable();
		debugInfo("PocketsPlugin onDisable");
		inventoryClickListener.saveAllOpenPocketInventories();
	}

	// recipes

	public String getPocketName() {
		return pocketName;
	}

	// begin localization
	private ResourceBundle bundle = null;

	public ResourceBundle getBundle() {
		if (bundle == null)
			bundle = ResourceBundle.getBundle(RESOURCE_NAME, locale);
		return bundle;
	}

	// end localization

	// pockets-test command
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player))
			return false;

		Player player = (Player) sender;

		if (!player.isOp())
			return false;

		ItemStack[] contents = PocketsUtils.getTestItems(this);
		
		int inventorySize = (contents.length + 1)/9 + 1;
		
		Inventory testInventory = Bukkit.createInventory(null, inventorySize*9, "Testing items for Pockets");
		testInventory.setContents(contents);

		player.openInventory(testInventory);
		return true;
	}

	public InventoryClickListener getInventoryClickListener() {
		return inventoryClickListener;
	}

}
