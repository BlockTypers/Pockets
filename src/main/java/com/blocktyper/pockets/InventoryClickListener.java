package com.blocktyper.pockets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.blocktyper.recipes.IRecipe;

public class InventoryClickListener extends PocketsListenerBase {
	
	static final ClickType DEFAULT_CLICK_TYPE = ClickType.RIGHT;

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
		InventoryClickListener.removePlayerWithPocketInventoryOpen(event.getPlayer());
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
		
		ItemStack item = event.getCurrentItem() != null && !event.getCurrentItem().getType().equals(Material.AIR) ? event.getCurrentItem() : event.getCursor();

		if (item == null || item.getType().equals(Material.AIR)) {
			plugin.debugInfo("item == null || item.getType().equals(Material.AIR)");
			return;
		}
		
		Pocket pocket = getPocket(item, player);
		List<ItemStack> contents = null;
		if (pocket != null) {
			contents = getPocketContents(pocket);
			setPocketJson(item, contents, player, true);
		}
		
		

		IRecipe recipe = plugin.recipeRegistrar().getRecipeFromKey(PocketsPlugin.POCKET_RECIPE_KEY);
		String pocketName = plugin.recipeRegistrar().getNameConsiderLocalization(recipe, player);

		if (event.getInventory().getName() != null && event.getInventory().getName().equals(pocketName)) {
			// We are in a pocket inventory and must handle pocket transfers
			handlePocketClick(event, player);
			return;
		}
		
		if (pocket == null) {
			return;
		}
		
		if (contents == null) {
			plugin.debugInfo("Contents were null but pockt was not!");
		}

		// if we got this far, then we are not in a pocket inventory and can open pockets
		ClickType clickType = null;
		
		try {
			clickType = ClickType.valueOf(plugin.getConfig().getString(ConfigKeyEnum.OPEN_POCKET_ACTION.getKey(),DEFAULT_CLICK_TYPE.name()));
		} catch (Exception e) {
			plugin.warning("Issue getting default click type: " + e.getMessage());
			clickType = null;
		}
		
		clickType = clickType != null ? clickType : DEFAULT_CLICK_TYPE;
		
		if(!clickType.equals(event.getClick())){
			plugin.debugInfo("Not a pocket action click");
			return;
		}
		
		openInventory(item, contents, player, event);
	}

	private void handlePocketClick(InventoryClickEvent event, Player player) {

		plugin.debugInfo("-----------------------------------");
		plugin.debugInfo("Action: " + event.getAction().name());
		Inventory clickedInventory = event.getClickedInventory();
		plugin.debugInfo("clickedInventory: " + (clickedInventory != null
				? (clickedInventory.getName() != null ? clickedInventory.getName() : "no name") : "null"));
		plugin.debugInfo("cursor: " + (event.getCursor() != null ? event.getCursor().getType().name() : "null"));
		plugin.debugInfo(
				"current: " + (event.getCurrentItem() != null ? event.getCurrentItem().getType().name() : "null"));
		plugin.debugInfo("raw slot: " + event.getRawSlot());

		ItemStack itemWithPocket = getActivePocketItem(player);
		
		IRecipe recipe = plugin.recipeRegistrar().getRecipeFromKey(PocketsPlugin.POCKET_RECIPE_KEY);
		String pocketName = plugin.recipeRegistrar().getNameConsiderLocalization(recipe, player);

		if (itemWithPocket == null) {
			String message = plugin.getLocalizedMessage(LocalizedMessageEnum.SERVER_RESTARTED_WHILE_POCKET_WAS_OPEN.getKey(), player);
			
			player.sendMessage(ChatColor.RED + message);
			event.setCancelled(true);
			player.closeInventory();
			return;
		} else if (itemWithPocket.equals(event.getCurrentItem())) {
			player.sendMessage(ChatColor.RED + pocketName);
			event.setCancelled(true);
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

		plugin.debugInfo("clickWasInPocketInventory: " + clickWasInPocketInventory);

		if (isAdditionAction(event.getAction())) {
			if (clickWasInPocketInventory) {

				ItemStack itemBeingAdded = event.getCursor();
				
				if(foreignInvisIssue(itemBeingAdded, player)){
					event.setCancelled(true);
					return;
				}else if (pocketInPocketIssue(itemWithPocket, itemBeingAdded, player)) {
					event.setCancelled(true);
					return;
				}

				if (event.getAction().equals(InventoryAction.SWAP_WITH_CURSOR)) {
					ItemStack itemRemoved = event.getCurrentItem();
					saveInventoryAfterSwap(player, event.getClickedInventory(), itemRemoved, itemBeingAdded);
				} else if (event.getAction().equals(InventoryAction.PLACE_ONE)) {
					saveInventoryDrop(player, event.getClickedInventory(), itemBeingAdded, 1);
				} else {
					saveInventoryDrop(player, event.getClickedInventory(), itemBeingAdded, null);
				}
			}
		} else if (event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
			
			ItemStack itemBeingTransferred = event.getCurrentItem();
			
			if (!clickWasInPocketInventory) {
				if(foreignInvisIssue(itemBeingTransferred, player)){
					event.setCancelled(true);
					return;
				}else if (pocketInPocketIssue(itemWithPocket, itemBeingTransferred, player)) {
					event.setCancelled(true);
					return;
				}
			}

			Inventory toInventory = clickWasInPocketInventory ? player.getInventory() : event.getInventory();
			Inventory fromInventory = event.getClickedInventory();

			saveInventoryAfterTransfer(player, toInventory, fromInventory, !clickWasInPocketInventory, itemBeingTransferred);
		} else if (isRemovalAction(event.getAction())) {
			if (clickWasInPocketInventory) {
				if (event.getAction().equals(InventoryAction.DROP_ONE_SLOT)) {
					saveInventoryRemoval(player, event.getClickedInventory(), event.getCurrentItem(), 1);
				} else if (event.getAction().equals(InventoryAction.PICKUP_ONE)) {
					saveInventoryRemoval(player, event.getClickedInventory(), event.getCurrentItem(), 1);
				} else if (event.getAction().equals(InventoryAction.PICKUP_HALF)) {
					int half = event.getCurrentItem().getAmount() / 2;
					int remainder = event.getCurrentItem().getAmount() % 2;
					saveInventoryRemoval(player, event.getClickedInventory(), event.getCurrentItem(),
							(half + remainder));
				} else {
					saveInventoryRemoval(player, event.getClickedInventory(), event.getCurrentItem(), null);
				}
			}
		}
	}

	private void debugInventoryInfo(Inventory inventory, String localLabel) {
		String inventoryName = inventory.getName() != null ? inventory.getName() : "No name";
		plugin.debugInfo("existing " + localLabel + "[" + inventoryName + "] items: ");
		Arrays.asList(inventory.getContents()).forEach(c -> plugin.debugInfo("  -" + itemInfo(c)));
	}

	private String itemInfo(ItemStack item) {
		return (item != null ? item.getType() + "[" + item.getAmount() + "]" : "null");
	}

	private void saveInventoryAfterTransfer(Player player, Inventory toInventory, Inventory fromInventory,
			Boolean saveToInventoryOnTranser, ItemStack itemTranferred) {
		if (toInventory != null)
			debugInventoryInfo(toInventory, "toInventory");

		if (fromInventory != null)
			debugInventoryInfo(fromInventory, "fromInventory");

		plugin.debugInfo("itemTranfrerred: " + itemInfo(itemTranferred));

		Inventory tempFrom = getInventoryCopy(fromInventory);
		Inventory tempTo = getInventoryCopy(toInventory);

		// transfer the item removed into the tempTo inventory
		HashMap<Integer, ItemStack> notEverythingFitMap = tempTo.addItem(itemTranferred.clone());

		if (saveToInventoryOnTranser) {
			saveInventoryIntoItem(player, tempTo);
		} else {
			final Material typeRemoved = itemTranferred.getType();
			int amountToRemove = itemTranferred.getAmount();
			plugin.debugInfo("amountToRemove: " + amountToRemove);

			if (notEverythingFitMap != null && !notEverythingFitMap.isEmpty()) {
				int howMuchDidNotFit = notEverythingFitMap.values().stream()
						.filter(i -> i.getType().equals(typeRemoved)).map(i -> i.getAmount())
						.reduce(0, (a, b) -> a + b);
				plugin.debugInfo("howMuchDidNotFit: " + howMuchDidNotFit);
				amountToRemove = amountToRemove - howMuchDidNotFit;
				plugin.debugInfo("new amountToRemove: " + amountToRemove);
			}

			ItemStack[] contentsAfterRemoval = getContentsAfterRemoval(tempFrom, itemTranferred.clone(),
					amountToRemove);
			tempFrom.setStorageContents(contentsAfterRemoval);
			saveInventoryIntoItem(player, tempFrom);
		}
	}

	private void saveInventoryAfterSwap(Player player, Inventory inventoryWhereSwapOccurred, ItemStack itemRemoved,
			ItemStack itemAdded) {
		if (inventoryWhereSwapOccurred != null)
			debugInventoryInfo(inventoryWhereSwapOccurred, "inventoryWhereSwapOccurred");

		plugin.debugInfo("itemRemoved: " + itemInfo(itemRemoved));
		plugin.debugInfo("itemAdded: " + itemInfo(itemAdded));

		Inventory tempInventoryWhereSwapOccurred = getInventoryCopy(inventoryWhereSwapOccurred);
		tempInventoryWhereSwapOccurred.remove(itemRemoved);
		tempInventoryWhereSwapOccurred.addItem(itemAdded);
		saveInventoryIntoItem(player, tempInventoryWhereSwapOccurred);

	}

	private void saveInventoryDrop(Player player, Inventory dropToInventory, ItemStack itemAdded, Integer amount) {
		if (dropToInventory != null)
			debugInventoryInfo(dropToInventory, "dropToInventory");

		ItemStack itemAddedCopy = itemAdded.clone();

		if (amount != null) {
			itemAddedCopy.setAmount(amount);
		}

		plugin.debugInfo("itemAddedCopy: " + itemInfo(itemAddedCopy));

		Inventory tempDropToInventory = getInventoryCopy(dropToInventory);
		tempDropToInventory.addItem(itemAddedCopy);
		saveInventoryIntoItem(player, tempDropToInventory);
	}

	private void saveInventoryRemoval(Player player, Inventory removedFromInventory, ItemStack itemRemoved,
			Integer amountToRemove) {
		if (removedFromInventory != null)
			debugInventoryInfo(removedFromInventory, "removedFromInventory");

		plugin.debugInfo("itemRemoved: " + itemInfo(itemRemoved));

		Inventory tempRemovedFromInventory = getInventoryCopy(removedFromInventory);
		ItemStack[] contentsAfterRemoval = getContentsAfterRemoval(tempRemovedFromInventory, itemRemoved,
				amountToRemove == null ? itemRemoved.getAmount() : amountToRemove);
		tempRemovedFromInventory.setStorageContents(contentsAfterRemoval);
		saveInventoryIntoItem(player, tempRemovedFromInventory);
	}

	private ItemStack[] getContentsAfterRemoval(Inventory inventory, ItemStack itemRemoved, int amountRemoved) {
		List<ItemStack> contents = new ArrayList<>();
		boolean removalComplete = false;
		for (ItemStack item : inventory.getStorageContents()) {
			if (item == null) {
				contents.add(null);
				continue;
			}
			ItemStack copy = item.clone();
			if (!removalComplete && item.equals(itemRemoved)) {
				removalComplete = true;
				if (amountRemoved != item.getAmount()) {
					copy.setAmount(copy.getAmount() - amountRemoved);
					contents.add(copy);
				}
			} else {
				contents.add(copy);
			}
		}

		return contents.toArray(new ItemStack[contents.size()]);
	}

	private Inventory getInventoryCopy(Inventory inventoryToCopy) {
		int size = inventoryToCopy.getStorageContents() != null ? inventoryToCopy.getStorageContents().length
				: inventoryToCopy.getSize();
		size = size < 1 ? 1 : size;
		Inventory copy = Bukkit.createInventory(null, size, "copy-" + inventoryToCopy.getName());

		ItemStack[] copiedContents = null;
		if (inventoryToCopy.getStorageContents() != null) {
			List<ItemStack> contents = Arrays.asList(inventoryToCopy.getStorageContents()).stream()
					.map(i -> i != null ? i.clone() : null).collect(Collectors.toList());
			copiedContents = contents.toArray(new ItemStack[contents.size()]);
		}
		copy.setContents(copiedContents);
		return copy;
	}

	private boolean isActionSupported(InventoryAction action) {
		return isAdditionAction(action) || isRemovalAction(action)
				|| action.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY);
	}

	private boolean isAdditionAction(InventoryAction action) {
		return action.equals(InventoryAction.PLACE_ALL) || action.equals(InventoryAction.PLACE_SOME)
				|| action.equals(InventoryAction.PLACE_ONE) || action.equals(InventoryAction.SWAP_WITH_CURSOR);
	}

	private boolean isRemovalAction(InventoryAction action) {
		return action.equals(InventoryAction.PICKUP_ALL) || action.equals(InventoryAction.PICKUP_HALF)
				|| action.equals(InventoryAction.PICKUP_ONE) || action.equals(InventoryAction.DROP_ONE_SLOT);
	}
	
	
	//STATIC playersWithOpenInventories
	private static Map<HumanEntity, Inventory> playersWithOpenInventories = new HashMap<HumanEntity, Inventory>();
	
	public static void addPlayerWithPocketInventoryOpen(HumanEntity player, Inventory inventory) {
		playersWithOpenInventories.put(player, inventory);
	}

	public static void removePlayerWithPocketInventoryOpen(HumanEntity player) {
		playersWithOpenInventories.remove(player);
	}
	
	public void saveAllOpenPocketInventories() {
		if(playersWithOpenInventories != null && !playersWithOpenInventories.isEmpty()){
			playersWithOpenInventories.entrySet().forEach(e -> saveInventoryIntoItem(e.getKey(), e.getValue(), false));
		}
	}

}
