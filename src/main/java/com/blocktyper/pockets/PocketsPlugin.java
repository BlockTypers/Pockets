package com.blocktyper.pockets;

import java.util.HashSet;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.blocktyper.pockets.listeners.CraftCancelListener;
import com.blocktyper.pockets.listeners.InventoryClickListener;
import com.blocktyper.pockets.listeners.InventoryOpenListener;
import com.blocktyper.pockets.listeners.PlayerInventoryOpenListener;
import com.blocktyper.pockets.utils.PocketsUtils;
import com.blocktyper.v1_2_2.BlockTyperBasePlugin;
import com.blocktyper.v1_2_2.nbt.ItemNBTIntegrationTest;
import com.blocktyper.v1_2_2.recipes.IRecipe;

public class PocketsPlugin extends BlockTyperBasePlugin implements CommandExecutor {

	private static final String RECIPES_KEY = "POCKETS_RECIPE_KEY";
	public static final String POCKET_RECIPE_KEY = "pocket";
	public static final String DEFAULT_POCKET_COLOR = "§5";

	public static final String RESOURCE_NAME = "com.blocktyper.pockets.resources.PocketsMessages";

	private InventoryClickListener inventoryClickListener;

	private boolean isNBTItemAPICompatible;
	private boolean isNBTItemAPIJsonCompatible;

	private static final String COMMAND_POCKETS_TEST = "pockets-test";

	private static Set<String> POCKETS_COMMANDS = new HashSet<>();

	static {
		POCKETS_COMMANDS.add("pockets");
		POCKETS_COMMANDS.add("taschen");
		POCKETS_COMMANDS.add("bolsillos");
		POCKETS_COMMANDS.add("poches");
		POCKETS_COMMANDS.add("карманы");
	}

	public PocketsPlugin() {
		super();
	}

	public void onEnable() {
		super.onEnable();
		registerListeners();
		PocketsUtils.registerPocketRecipes(this);
		this.getCommand(COMMAND_POCKETS_TEST).setExecutor(this);

		for (String pocketsCommand : POCKETS_COMMANDS) {
			this.getCommand(pocketsCommand).setExecutor(this);
		}

		ItemNBTIntegrationTest itemNBTIntegrationTest = new ItemNBTIntegrationTest(this);
		itemNBTIntegrationTest.test();
		isNBTItemAPICompatible = itemNBTIntegrationTest.isCompatible();
		isNBTItemAPIJsonCompatible = itemNBTIntegrationTest.isJsonCompatible();
	}

	private void registerListeners() {
		inventoryClickListener = new InventoryClickListener(this);
		new InventoryOpenListener(this);
		new BlockPlaceListener(this);
		new CraftCancelListener(this);

		if (getConfig().getBoolean(ConfigKeyEnum.RENAME_ITEMS_ON_INVENTORY_OPEN.getKey(), false)) {
			// this removes players OpenInventory achievement when they join and
			// prevents the achievement from occurring
			new PlayerInventoryOpenListener(this);
		}
	}

	@Override
	public void onDisable() {
		super.onDisable();
		debugInfo("PocketsPlugin onDisable");
		inventoryClickListener.saveAllOpenPocketInventories();
	}

	// recipes

	// begin localization
	public ResourceBundle getBundle(Locale locale) {
		return ResourceBundle.getBundle(RESOURCE_NAME, locale);
	}
	// end localization

	// pockets-test command
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player))
			return false;

		if (label == null) {
			return false;
		}

		Player player = (Player) sender;

		if (COMMAND_POCKETS_TEST.equalsIgnoreCase(label)) {
			if (!player.isOp()) {
				return false;
			}

			ItemStack[] contents = PocketsUtils.getTestItems(this, player);

			int inventorySize = (contents.length + 1) / 9 + 1;

			Inventory testInventory = Bukkit.createInventory(null, inventorySize * 9, "Testing items for Pockets");
			testInventory.setContents(contents);

			player.openInventory(testInventory);
			return true;
		} else if (POCKETS_COMMANDS.contains(label.toLowerCase())) {
			inventoryClickListener.openPlayersYourPocketsInventory(player);
			return true;
		}

		return false;
	}

	public InventoryClickListener getInventoryClickListener() {
		return inventoryClickListener;
	}

	public boolean isNbtItemAPICompatible() {
		return isNBTItemAPICompatible;
	}

	public boolean isNBTItemAPIJsonCompatible() {
		return isNBTItemAPIJsonCompatible;
	}

	@Override
	public String getRecipesNbtKey() {
		return RECIPES_KEY;
	}

	@Override
	public IRecipe bootstrapRecipe(IRecipe recipe) {
		return recipe;
	}

}
