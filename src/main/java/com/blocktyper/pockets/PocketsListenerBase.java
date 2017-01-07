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
import org.bukkit.event.Cancellable;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.blocktyper.serialization.CardboardBox;

public abstract class PocketsListenerBase implements Listener {

	protected PocketsPlugin plugin;
	private Map<String, ItemStack> openPocketMap = new HashMap<>();

	protected static final int INVENTORY_COLUMNS = 9;
	protected static final Material BLACKOUT_MATERIAL = Material.STAINED_GLASS_PANE;
	protected static final String BLACKOUT_TEXT = "---";
	protected static String POCKETS_KEY = "#PKT";

	public PocketsListenerBase(PocketsPlugin plugin) {
		this.plugin = plugin;
		this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	public void setPocketJson(ItemStack itemWithPocket, List<ItemStack> itemsInPocket) {

		if (itemWithPocket == null) {
			return;
		}

		List<CardboardBox> contents = null;

		if (itemsInPocket != null && !itemsInPocket.isEmpty()) {
			contents = itemsInPocket.stream().filter(i -> i != null).map(i -> new CardboardBox(i))
					.collect(Collectors.toList());
		} else {
			contents = new ArrayList<>();
		}

		Pocket pocket = new Pocket();
		pocket.setContents(contents);

		int itemCount = contents != null ? contents.size() : 0;

		String visiblePrefix = plugin.getPocketName() + " [" + itemCount + "]";

		plugin.getInvisibleLoreHelper().setInvisisbleJson(pocket, itemWithPocket, POCKETS_KEY, visiblePrefix);
	}

	protected String getMaterialSettingConfigKey(Material material, String suffix) {
		return ConfigKeyEnum.MATERIAL_SETTINGS.getKey() + "." + material.name() + "." + suffix;
	}

	protected boolean isUserPermitted(HumanEntity player, boolean isGeneral, boolean sendMessage) {

		boolean userHasPermission = true;

		ConfigKeyEnum requirePermissionKey = isGeneral ? ConfigKeyEnum.REQUIRE_PERMISSIONS_FOR_GENERAL_USE
				: ConfigKeyEnum.REQUIRE_PERMISSIONS_FOR_POCKET_IN_POCKET_USE;

		boolean requireUsePermissions = plugin.getConfig().getBoolean(requirePermissionKey.getKey(), false);

		if (requireUsePermissions) {
			String requiredTypeString = (isGeneral ? "General use" : "Pocket in pocket") + " permissions required";
			plugin.debugInfo(requiredTypeString);
			userHasPermission = false;

			ConfigKeyEnum permissionsKey = isGeneral ? ConfigKeyEnum.GENERAL_USE_PERMISSIONS
					: ConfigKeyEnum.POCKET_IN_POCKET_USE_USE_PERMISSIONS;
			String usePermissions = plugin.getConfig().getString(permissionsKey.getKey(), null);

			if (usePermissions == null) {
				plugin.warning(requiredTypeString + ", but no permission supplied.  Please set the value for the ["
						+ permissionsKey + "] configuration key.");
			} else {
				plugin.debugInfo("Checking use permissions: " + usePermissions);
				userHasPermission = plugin.getPlayerHelper().playerCanDoAction(player, Arrays.asList(usePermissions));
				plugin.debugInfo(userHasPermission ? "Permission granted" : "Permission denied");
			}
		}

		if (!userHasPermission && sendMessage) {
			String message = plugin.getLocalizedMessage(LocalizedMessageEnum.PERMISSION_DENIED.getKey(), player);
			player.sendMessage(message);
		}

		return userHasPermission;
	}

	protected void openInventory(ItemStack clickedItem, Player player, Cancellable event) {

		if (!isUserPermitted(player, true, true)) {
			return;
		}

		Pocket pocket = getPocket(clickedItem);
		if (pocket == null) {
			return;
		}

		List<ItemStack> items = getPocketContents(pocket);

		int pocketSizeLimit = plugin.getConfig().getInt(
				getMaterialSettingConfigKey(clickedItem.getType(), ConfigKeyEnum.MATERIAL_SETTING_LIMIT.getKey()));

		plugin.debugInfo("pocketSizeLimit [initial]: " + pocketSizeLimit);
		if (pocketSizeLimit <= 0) {
			pocketSizeLimit = plugin.getConfig().getInt(ConfigKeyEnum.DEFAULT_POCKET_SIZE_LIMIT.getKey(), 6);
			plugin.debugInfo("pocketSizeLimit [secondary]: " + pocketSizeLimit);
		}

		if (items.size() > pocketSizeLimit)
			pocketSizeLimit = items.size();

		plugin.debugInfo("pocketSizeLimit [final]: " + pocketSizeLimit);

		int rows = (pocketSizeLimit / INVENTORY_COLUMNS) + (pocketSizeLimit % INVENTORY_COLUMNS > 0 ? 1 : 0);

		Inventory pocketsInventory = Bukkit.createInventory(null, rows * INVENTORY_COLUMNS, plugin.getPocketName());

		int i = -1;
		boolean noPocketInPocketIssueLocated = true;

		for (ItemStack item : items) {
			if (item == null || item.getType().equals(Material.AIR))
				continue;

			if (pocketInPocketIssue(clickedItem, item, player, noPocketInPocketIssueLocated)) {
				noPocketInPocketIssueLocated = false;
				tryToFitItemInPlayerInventory(item, player);
				continue;
			}

			i++;
			pocketsInventory.setItem(i, item);
		}

		int availableSlotsOnLastRow = pocketSizeLimit >= INVENTORY_COLUMNS ? pocketSizeLimit % INVENTORY_COLUMNS
				: pocketSizeLimit;

		plugin.debugInfo("availableSlotsOnLastRow: " + availableSlotsOnLastRow);

		if (availableSlotsOnLastRow > 0) {
			// we must add black pane glass slots to the end of the inventory
			// which cannot be moved
			int blackedOutSlotsRequired = INVENTORY_COLUMNS - availableSlotsOnLastRow;

			plugin.debugInfo("blackedOutSlotsRequired: " + blackedOutSlotsRequired);
			for (i = pocketSizeLimit; i < pocketSizeLimit + blackedOutSlotsRequired; i++) {
				ItemStack blackOut = new ItemStack(BLACKOUT_MATERIAL);
				ItemMeta itemMeta = blackOut.getItemMeta();
				itemMeta.setDisplayName(BLACKOUT_TEXT);
				itemMeta.setLore(new ArrayList<>());
				itemMeta.getLore().add(plugin.getInvisibleLoreHelper().convertToInvisibleString(i + ""));
				blackOut.setItemMeta(itemMeta);
				pocketsInventory.setItem(i, blackOut);
			}
		}

		if (event != null)
			event.setCancelled(true);

		if (openPocketMap == null)
			openPocketMap = new HashMap<>();

		openPocketMap.put(player.getName(), clickedItem);

		if (!noPocketInPocketIssueLocated) {
			// if we had to change the contents of the inventory because had
			// invalid pocket-in-pocket items
			// then we need to re-save the item data before opening it.
			saveInventoryIntoItem(player, pocketsInventory, true);
		}

		PocketDelayOpener pocketDelayOpener = new PocketDelayOpener(plugin, player, pocketsInventory);
		pocketDelayOpener.runTaskLater(plugin, 5L * 1);
	}

	protected void saveInventoryIntoItem(HumanEntity player, Inventory inventory) {
		saveInventoryIntoItem(player, inventory, false);
	}

	protected void saveInventoryIntoItem(HumanEntity player, Inventory inventory, boolean isOnClose) {
		ItemStack itemWithPocket = getActivePocketItem(player);

		if (itemWithPocket == null) {
			plugin.debugInfo("itemWithPocket == null");
			return;
		}

		ItemStack[] items = inventory.getStorageContents();

		List<ItemStack> itemsInPocketTemp = items == null ? null
				: Arrays.asList(items).stream().filter(i -> !isBlackoutItem(i)).collect(Collectors.toList());

		List<ItemStack> itemsInPocket = null;
		boolean showPocketInPocketWarning = true;
		if (itemsInPocketTemp != null) {
			itemsInPocket = new ArrayList<>();
			for (ItemStack item : itemsInPocketTemp) {
				if (item != null && pocketInPocketIssue(itemWithPocket, item, player, showPocketInPocketWarning)) {
					showPocketInPocketWarning = false;
					tryToFitItemInPlayerInventory(item, player);
					continue;
				}
				itemsInPocket.add(item);
			}
		}

		if (isOnClose) {
			plugin.debugInfo("SAVING on inventory close");
			setPocketJson(itemWithPocket, itemsInPocket);
		} else {
			plugin.debugInfo("SAVING after inventory action");
			setPocketJson(itemWithPocket, itemsInPocket);
		}
	}

	protected boolean isBlackoutItem(ItemStack item) {
		if (item == null)
			return false;
		if (!item.getType().equals(BLACKOUT_MATERIAL))
			return false;
		if (item.getItemMeta() == null || item.getItemMeta().getDisplayName() == null)
			return false;
		if (!item.getItemMeta().getDisplayName().equals(BLACKOUT_TEXT))
			return false;

		return true;
	}

	protected void tryToFitItemInPlayerInventory(ItemStack item, HumanEntity player) {
		HashMap<Integer, ItemStack> remaining = player.getInventory().addItem(item);
		plugin.debugWarning("tryToFitItemInPlayerInventory: " + item.getType() + "[" + item.getAmount() + "]");
		if (remaining == null || remaining.values() == null || remaining.values().isEmpty()) {
			remaining.values().forEach(i -> player.getWorld().dropItemNaturally(player.getLocation(), i));
		}
	}

	protected boolean pocketInPocketIssue(ItemStack itemWithPocket, ItemStack itemInPocket, HumanEntity player) {
		return pocketInPocketIssue(itemWithPocket, itemInPocket, player, true);
	}

	protected boolean pocketInPocketIssue(ItemStack itemWithPocket, ItemStack itemInPocket, HumanEntity player,
			boolean showWarning) {
		boolean defaultAllowPocketsInPocket = plugin.getConfig().getBoolean(getMaterialSettingConfigKey(
				itemWithPocket.getType(), ConfigKeyEnum.DEFAULT_ALLOW_POCKET_IN_POCKET.getKey()), true);
		boolean allowPocketsInPocket = plugin.getConfig()
				.getBoolean(
						getMaterialSettingConfigKey(itemWithPocket.getType(),
								ConfigKeyEnum.MATERIAL_SETTING_ALLOW_POCKET_IN_POCKET.getKey()),
						defaultAllowPocketsInPocket);

		Pocket pocket = getPocket(itemInPocket);
		if (pocket != null && pocket.getContents() != null && !pocket.getContents().isEmpty()) {
			if (!allowPocketsInPocket) {
				if (showWarning) {
					String message = plugin
							.getLocalizedMessage(LocalizedMessageEnum.POCKETS_IN_POCKETS_NOT_ALLOWED.getKey(), player);
					player.sendMessage(ChatColor.RED + message);
				}
				return true;
			} else if (!isUserPermitted(player, false, showWarning)) {
				return true;
			}
		}

		return false;
	}

	protected Pocket getPocket(ItemStack item) {
		return getPocket(item, true);
	}

	protected Pocket getPocket(ItemStack item, boolean hideChildPockets) {

		Pocket pocket = plugin.getInvisibleLoreHelper().getObjectFromInvisisibleLore(item, POCKETS_KEY, Pocket.class);

		if (pocket != null) {
			if (hideChildPockets && pocket.getContents() != null && !pocket.getContents().isEmpty()) {
				List<ItemStack> items = pocket.getContents().stream().map(c -> getPocketItemsHidden(c))
						.collect(Collectors.toList());
				List<CardboardBox> newContents = items == null ? null
						: items.stream().filter(i -> i != null).map(i -> new CardboardBox(i))
								.collect(Collectors.toList());
				pocket.setContents(newContents);
			}
		}

		return pocket;
	}

	protected ItemStack getPocketItemsHidden(CardboardBox box) {
		ItemStack item = box != null ? box.unbox() : null;
		if (item != null) {
			Pocket pocket = getPocket(item, false);
			if (pocket != null) {
				List<ItemStack> itemsInPocket = pocket.getContents() == null ? null
						: pocket.getContents().stream().map(c -> c.unbox()).collect(Collectors.toList());
				setPocketJson(item, itemsInPocket);
			}
		}
		return item;
	}

	protected List<ItemStack> getPocketContents(ItemStack item, boolean hideChildPockets) {
		return getPocketContents(getPocket(item, hideChildPockets));
	}

	protected List<ItemStack> getPocketContents(Pocket pocket) {
		return pocket == null || pocket.getContents() == null ? null
				: pocket.getContents().stream().filter(c -> c != null).map(c -> c.unbox()).collect(Collectors.toList());
	}

	protected ItemStack getActivePocketItem(HumanEntity player) {
		if (openPocketMap == null)
			openPocketMap = new HashMap<>();

		return openPocketMap.containsKey(player.getName()) ? openPocketMap.get(player.getName()) : null;
	}

}
