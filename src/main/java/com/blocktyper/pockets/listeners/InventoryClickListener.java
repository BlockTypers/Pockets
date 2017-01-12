package com.blocktyper.pockets.listeners;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import com.blocktyper.nbt.NBTItem;
import com.blocktyper.pockets.ConfigKeyEnum;
import com.blocktyper.pockets.LocalizedMessageEnum;
import com.blocktyper.pockets.PocketsPlugin;
import com.blocktyper.pockets.data.Pocket;
import com.blocktyper.recipes.IRecipe;

public class InventoryClickListener extends PocketsListenerBase {

	static final ClickType DEFAULT_CLICK_TYPE = ClickType.RIGHT;
	
	private static boolean debugNbtTags = false;

	public InventoryClickListener(PocketsPlugin plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onInventoryCloseEvent(InventoryCloseEvent event) {
		IRecipe recipe = plugin.recipeRegistrar().getRecipeFromKey(PocketsPlugin.POCKET_RECIPE_KEY);
		String pocketName = plugin.recipeRegistrar().getNameConsiderLocalization(recipe, event.getPlayer());

		if (event.getInventory().getName() == null || !event.getInventory().getName().equals(pocketName)) {
			plugin.debugInfo("Not pocket inventory closing");
			return;
		}
		saveInventoryIntoItem(event.getPlayer(), event.getInventory(), true);
		removePlayerWithPocketInventoryOpen(event.getPlayer());
	}

	private boolean itemIsInvalid(ItemStack item) {
		return item == null || item.getType().equals(Material.AIR);
	}

	/*
	 * ON INVENTORY CLICK
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onInventoryClickEvent(InventoryClickEvent event) {

		plugin.debugInfo("onInventoryClickEvent: " + event.getClick().name());

		if (!(event.getWhoClicked() instanceof Player)) {
			return;
		}

		Player player = ((Player) event.getWhoClicked());

		boolean itemFromCursor = false;

		ItemStack item = event.getCurrentItem();

		if (itemIsInvalid(item)) {
			item = event.getCursor();
			itemFromCursor = true;
		}

		if (itemIsInvalid(item))
			return;
		
		debugNbtTags(item);
		
		if (clickedItemIsOpenPocket(player, event.getCurrentItem(), true)) {
			event.setCancelled(true);
			return;
		}

		if (!itemFromCursor) {
			plugin.debugInfo("Starting oldPocketConverter");
			ItemStack convertedItem = oldPocketConverter(event.getCurrentItem(), player);
			if (convertedItem != null) {
				event.setCurrentItem(convertedItem);
				event.setCancelled(true);
				return;
			}
			plugin.debugInfo("Done with oldPocketConverter");
		}

		IRecipe recipe = plugin.recipeRegistrar().getRecipeFromKey(PocketsPlugin.POCKET_RECIPE_KEY);
		String pocketName = plugin.recipeRegistrar().getNameConsiderLocalization(recipe, player);

		if (event.getInventory().getName() != null && event.getInventory().getName().equals(pocketName)) {
			// We are in a pocket inventory and must handle pocket transfers
			handlePocketClick(event, player);
			return;
		}

		Pocket pocket = getPocket(item, player);
		List<ItemStack> contents = null;
		if (pocket != null) {
			plugin.debugInfo("------------------------------------------");
			plugin.debugInfo("Pocket retreived");
			contents = getPocketContents(pocket);
			plugin.debugInfo("Contents: " + (contents != null ? contents.size() : "null"));
			plugin.debugInfo("------------------------------------------");
		} else {
			plugin.debugInfo("No Pocket retreived");
		}

		if (pocket == null)
			return;

		// if we got this far, then we are not in a pocket inventory and we
		// clicked a pocket
		ClickType clickType = null;

		try {
			clickType = ClickType.valueOf(
					plugin.getConfig().getString(ConfigKeyEnum.OPEN_POCKET_ACTION.getKey(), DEFAULT_CLICK_TYPE.name()));
		} catch (Exception e) {
			plugin.warning("Issue getting default click type: " + e.getMessage());
			clickType = null;
		}

		clickType = clickType != null ? clickType : DEFAULT_CLICK_TYPE;

		if (!clickType.equals(event.getClick())) {
			plugin.debugInfo("Not a pocket action click");
			return;
		}

		setActivePocketItemAndInventory(player, item, event.getClickedInventory());

		event.setCancelled(true);

		plugin.debugInfo("calling openInventory: " + item.getType().name());

		openInventory(item, contents, player);
	}

	/**
	 * 
	 * @param player
	 * @param clickedItem
	 * @return
	 */
	protected boolean clickedItemIsOpenPocket(Player player, ItemStack clickedItem) {
		return clickedItemIsOpenPocket(player, clickedItem, false);
	}

	/**
	 * 
	 * @param player
	 * @param clickedItem
	 * @param sendMessage
	 * @return
	 */
	protected boolean clickedItemIsOpenPocket(Player player, ItemStack clickedItem, boolean sendMessage) {
		if (itemIsAnOpenPocket(clickedItem)) {
			if (sendMessage) {
				IRecipe recipe = plugin.recipeRegistrar().getRecipeFromKey(PocketsPlugin.POCKET_RECIPE_KEY);
				String pocketName = plugin.recipeRegistrar().getNameConsiderLocalization(recipe, player);
				player.sendMessage(ChatColor.RED + pocketName);
			}
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param event
	 * @param player
	 */
	private void handlePocketClick(InventoryClickEvent event, Player player) {

		ItemStack itemWithPocket = getActivePocketItem(player);

		IRecipe recipe = plugin.recipeRegistrar().getRecipeFromKey(PocketsPlugin.POCKET_RECIPE_KEY);
		String pocketName = plugin.recipeRegistrar().getNameConsiderLocalization(recipe, player);

		if (itemWithPocket == null) {
			String message = plugin
					.getLocalizedMessage(LocalizedMessageEnum.SERVER_RESTARTED_WHILE_POCKET_WAS_OPEN.getKey(), player);

			player.sendMessage(ChatColor.RED + message);
			event.setCancelled(true);
			player.closeInventory();
			return;
		}
		// do not let the player manipulate blackout items
		else if (isBlackoutItem(event.getCurrentItem())) {
			event.setCancelled(true);
			return;
		} else if (!isActionSupported(event.getAction())) {
			event.setCancelled(true);
			return;
		}

		boolean clickWasInPocketInventory = event.getClickedInventory() != null
				&& event.getClickedInventory().getName() != null
				&& event.getClickedInventory().getName().equals(pocketName);

		if (isAdditionAction(event.getAction())) {
			if (clickWasInPocketInventory) {

				ItemStack itemBeingAdded = event.getCursor();

				if (pocketInPocketIssue(itemWithPocket, itemBeingAdded, player)
						|| incompatibleIssue(itemBeingAdded, player, true)) {
					event.setCancelled(true);
					return;
				}
				saveLater(player);
			}
		} else if (event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {

			ItemStack itemBeingTransferred = event.getCurrentItem();

			if (!clickWasInPocketInventory) {
				if (pocketInPocketIssue(itemWithPocket, itemBeingTransferred, player)
						|| incompatibleIssue(itemBeingTransferred, player, true)) {
					event.setCancelled(true);
					return;
				}
			}

			saveLater(player);
		} else if (isRemovalAction(event.getAction())) {
			saveLater(player);
		}
	}

	/**
	 * 
	 * @param action
	 * @return
	 */
	private boolean isActionSupported(InventoryAction action) {
		return isAdditionAction(action) || isRemovalAction(action)
				|| action.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY);
	}

	/**
	 * 
	 * @param action
	 * @return
	 */
	private boolean isAdditionAction(InventoryAction action) {
		return action.equals(InventoryAction.PLACE_ALL) || action.equals(InventoryAction.PLACE_SOME)
				|| action.equals(InventoryAction.PLACE_ONE) || action.equals(InventoryAction.SWAP_WITH_CURSOR);
	}

	/**
	 * 
	 * @param action
	 * @return
	 */
	private boolean isRemovalAction(InventoryAction action) {
		return action.equals(InventoryAction.PICKUP_ALL) || action.equals(InventoryAction.PICKUP_HALF)
				|| action.equals(InventoryAction.PICKUP_ONE) || action.equals(InventoryAction.DROP_ONE_SLOT);
	}
	
	
	/**
	 * 
	 * @param item
	 */
	private void debugNbtTags(ItemStack item){
		if(debugNbtTags){
			plugin.debugInfo("()()()()()()()()()()()()()()()()()()()()()()()");
			plugin.debugInfo("()()()()()()()()()()()()()()()()()()()()()()()");
			plugin.debugInfo("()()()()()()()()()()()()()()()()()()()()()()()");
			plugin.debugInfo("NBT Tags:");
			NBTItem nbtItem = new NBTItem(item);
	        if(nbtItem.getKeys() != null && !nbtItem.getKeys().isEmpty()){
	        	for(String key : nbtItem.getKeys()){
	        		boolean found = false;
	        		plugin.debugInfo("Key: " + key) ;
	        		String asString = nbtItem.getString(key);
	        		if(asString != null){
	        			plugin.debugInfo("  -String ("+asString.length()+"):  " + asString);
	        			found = true;
	        		}
	        		
	        		Double asDouble = nbtItem.getDouble(key);
	        		if(asDouble != null){
	        			plugin.debugInfo("  -Double:  " + asDouble);
	        			found = true;
	        		}
	        		
	        		Integer asInteger = nbtItem.getInteger(key);
	        		if(asInteger != null){
	        			plugin.debugInfo("  -Integer: " + asInteger);
	        			found = true;
	        		}
	        		
	        		Boolean asBoolean = nbtItem.getBoolean(key);
	        		if(asBoolean != null){
	        			plugin.debugInfo("  -Boolean: " + asBoolean);
	        			found = true;
	        		}
	        		
	        		if(!found)
	        			plugin.debugInfo("  -NULL");
	        	}
	        }
	        plugin.debugInfo("()()()()()()()()()()()()()()()()()()()()()()()");
			plugin.debugInfo("()()()()()()()()()()()()()()()()()()()()()()()");
			plugin.debugInfo("()()()()()()()()()()()()()()()()()()()()()()()");
		}
	}

}
