package com.blocktyper.pockets.listeners;

import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.blocktyper.pockets.ConfigKeyEnum;
import com.blocktyper.pockets.LocalizedMessageEnum;
import com.blocktyper.pockets.PocketsPlugin;
import com.blocktyper.pockets.data.Pocket;
import com.blocktyper.v1_2_2.helpers.InvisibleLoreHelper;
import com.blocktyper.v1_2_2.nbt.NBTItem;
import com.blocktyper.v1_2_2.recipes.IRecipe;

public class InventoryClickListener extends PocketsListenerBase {

	static final ClickType DEFAULT_CLICK_TYPE = ClickType.RIGHT;

	private static boolean debugNbtTags = false;

	public InventoryClickListener(PocketsPlugin plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onInventoryCloseEvent(InventoryCloseEvent event) {
		IRecipe recipe = recipeRegistrar().getRecipeFromKey(PocketsPlugin.POCKET_RECIPE_KEY);
		String pocketName = recipeRegistrar().getNameConsiderLocalization(recipe, event.getPlayer());

		if (event.getInventory().getName() == null || !event.getInventory().getName().equals(pocketName)) {
			debugInfo("Not pocket inventory closing");
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

		if (!(event.getWhoClicked() instanceof Player)) {
			return;
		}
		
		if (event.getClickedInventory() == null) {
			debugInfo("clicked inventory was null");
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

		if (event.getClickedInventory().getName() != null && event.getClickedInventory().getName().equals(BLACKOUT_TEXT)) {
			event.setCancelled(true);
			return;
		}
		
		
		if(event.getInventory() != null && event.getInventory().getName() != null){
			String inventoryNameWithNoInvis = InvisibleLoreHelper.convertToVisibleString(event.getInventory().getName());
			if(inventoryNameWithNoInvis.startsWith(YOUR_POCKETS_HIDDEN_LORE_KEY)){
				event.setCancelled(true);
				if(!isBlackoutItem(item)){
					checkIfItemHasPocketAndOpen(event, item, player);
				}
				return;
			}
		}
		
		if (clickedItemIsOpenPocket(player, event.getCurrentItem(), true)) {
			event.setCancelled(true);
			saveLater(player);

			String goBackClickTypeString = plugin.getConfig().getString(
					ConfigKeyEnum.GO_BACK_TO_PLAYER_INVENTORY_CLICK_TYPE.getKey(), ClickType.SHIFT_RIGHT.name());
			if (goBackClickTypeString == null || goBackClickTypeString.isEmpty()
					|| ClickType.valueOf(goBackClickTypeString) == null) {
				goBackClickTypeString = ClickType.SHIFT_RIGHT.name();
			}
			goBackClickTypeString = goBackClickTypeString.toUpperCase();
			if (event.getClick().name().equals(goBackClickTypeString)) {
				player.closeInventory();
				Inventory blankInventory = Bukkit.createInventory(null, INVENTORY_COLUMNS, BLACKOUT_TEXT);
				fillWithBlackOutItems(blankInventory, 0, INVENTORY_COLUMNS, 0);
				player.openInventory(blankInventory);
			}

			return;
		}

		if (!itemFromCursor) {
			debugInfo("Starting oldPocketConverter");
			ItemStack convertedItem = oldPocketConverter(event.getCurrentItem(), player);
			if (convertedItem != null) {
				event.setCurrentItem(convertedItem);
				event.setCancelled(true);
				return;
			}
			debugInfo("Done with oldPocketConverter");
		}

		IRecipe recipe = recipeRegistrar().getRecipeFromKey(PocketsPlugin.POCKET_RECIPE_KEY);
		String pocketName = recipeRegistrar().getNameConsiderLocalization(recipe, player);

		if (event.getInventory().getName() != null && event.getInventory().getName().equals(pocketName)) {
			// We are in a pocket inventory and must handle pocket transfers
			handlePocketClick(event, player);
			return;
		}
		
		checkIfItemHasPocketAndOpen(event, item, player);
	}
	
	
	private void checkIfItemHasPocketAndOpen(InventoryClickEvent event, ItemStack item, Player player){
		Pocket pocket = getPocket(item, player);
		List<ItemStack> contents = null;
		if (pocket != null) {
			debugInfo("------------------------------------------");
			debugInfo("Pocket retreived");
			contents = getPocketContents(pocket);
			debugInfo("Contents: " + (contents != null ? contents.size() : "null"));
			debugInfo("------------------------------------------");
		} else {
			debugInfo("No Pocket retreived");
		}

		if (pocket == null)
			return;

		if (event.getAction().equals(InventoryAction.SWAP_WITH_CURSOR)) {
			if (pocketInPocketIssue(item, event.getCursor(), player, true)) {
				return;
			} else if (incompatibleIssue(event.getCursor(), player, true)) {
				return;
			}

			setActivePocketItemAndInventory(player, item, event.getClickedInventory());
			Inventory pocketInventory = getPocketInventory(item, contents, player);
			Map<Integer, ItemStack> remainingItems = pocketInventory.addItem(event.getCursor());
			ItemStack remainingStackForCursor = null;
			if (remainingItems != null && remainingItems.values() != null) {
				for(ItemStack remainingItem : remainingItems.values()){
					if(remainingStackForCursor == null){
						remainingStackForCursor = remainingItem;
					}else{
						tryToFitItemInPlayerInventory(remainingItem, player);
					}
				}
			}
			saveInventoryIntoItem(player, pocketInventory);
			removePlayerWithPocketInventoryOpen(event.getWhoClicked());
			player.setItemOnCursor(remainingStackForCursor);
			event.setCancelled(true);
			saveLater(player);
			return;
		}

		// if we got this far, then we are not in a pocket inventory and we
		// clicked a pocket
		ClickType clickType = null;

		try {
			clickType = ClickType.valueOf(
					plugin.getConfig().getString(ConfigKeyEnum.OPEN_POCKET_ACTION.getKey(), DEFAULT_CLICK_TYPE.name()));
		} catch (Exception e) {
			warning("Issue getting default click type: " + e.getMessage());
			clickType = null;
		}

		clickType = clickType != null ? clickType : DEFAULT_CLICK_TYPE;

		if (!clickType.equals(event.getClick())) {
			debugInfo("Not a pocket action click");
			return;
		}

		setActivePocketItemAndInventory(player, item, event.getClickedInventory());

		event.setCancelled(true);

		debugInfo("calling openInventory: " + item.getType().name());

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
				IRecipe recipe = recipeRegistrar().getRecipeFromKey(PocketsPlugin.POCKET_RECIPE_KEY);
				String pocketName = recipeRegistrar().getNameConsiderLocalization(recipe, player);
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

		IRecipe recipe = recipeRegistrar().getRecipeFromKey(PocketsPlugin.POCKET_RECIPE_KEY);
		String pocketName = recipeRegistrar().getNameConsiderLocalization(recipe, player);

		if (itemWithPocket == null) {
			String message = getLocalizedMessage(LocalizedMessageEnum.SERVER_RESTARTED_WHILE_POCKET_WAS_OPEN.getKey(), player);

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
	private void debugNbtTags(ItemStack item) {
		if (debugNbtTags) {
			debugInfo("()()()()()()()()()()()()()()()()()()()()()()()");
			debugInfo("()()()()()()()()()()()()()()()()()()()()()()()");
			debugInfo("()()()()()()()()()()()()()()()()()()()()()()()");
			debugInfo("NBT Tags:");
			NBTItem nbtItem = new NBTItem(item);
			if (nbtItem.getKeys() != null && !nbtItem.getKeys().isEmpty()) {
				for (String key : nbtItem.getKeys()) {
					boolean found = false;
					debugInfo("Key: " + key);
					String asString = nbtItem.getString(key);
					if (asString != null) {
						debugInfo("  -String (" + asString.length() + "):  " + asString);
						found = true;
					}

					Double asDouble = nbtItem.getDouble(key);
					if (asDouble != null) {
						debugInfo("  -Double:  " + asDouble);
						found = true;
					}

					Integer asInteger = nbtItem.getInteger(key);
					if (asInteger != null) {
						debugInfo("  -Integer: " + asInteger);
						found = true;
					}

					Boolean asBoolean = nbtItem.getBoolean(key);
					if (asBoolean != null) {
						debugInfo("  -Boolean: " + asBoolean);
						found = true;
					}

					if (!found)
						debugInfo("  -NULL");
				}
			}
			debugInfo("()()()()()()()()()()()()()()()()()()()()()()()");
			debugInfo("()()()()()()()()()()()()()()()()()()()()()()()");
			debugInfo("()()()()()()()()()()()()()()()()()()()()()()()");
		}
	}

}
