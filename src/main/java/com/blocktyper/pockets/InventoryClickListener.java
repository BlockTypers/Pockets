package com.blocktyper.pockets;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InventoryClickListener extends PocketsListenerBase {

	Map<String, ItemStack> openPocketMap = new HashMap<>();

	public InventoryClickListener(PocketsPlugin plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onInventoryCloseEvent(InventoryCloseEvent event) {
		if (event.getInventory().getName() == null || !event.getInventory().getName().equals("(Pockets)")) {
			plugin.debugInfo("Not pocket inventory closing");
			return;
		}
		ItemStack itemWithPocket = openPocketMap.get(event.getPlayer().getName());

		if (itemWithPocket == null) {
			plugin.debugInfo("itemWithPocket == null");
			return;
		}

		ItemStack[] items = event.getInventory().getContents();

		List<ItemStack> itemsInPocket = items == null ? null : Arrays.asList(items);

		plugin.setPocketJson(itemWithPocket, itemsInPocket);
	}

	/*
	 * ON INVENTORY CLICK
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onInventoryClickEvent(InventoryClickEvent event) {

		ClickType ct = event.getClick();
		plugin.debugInfo("Click type: " + ct.name());

		if (!(event.getWhoClicked() instanceof Player)) {
			return;
		}

		if (event.getCurrentItem() == null || event.getCurrentItem().getType().equals(Material.AIR)) {
			return;
		}

		Player player = ((Player) event.getWhoClicked());
		
		if (event.getInventory().getName() != null && event.getInventory().getName().equals("(Pockets)")) {
			
			ItemStack itemWithPocket = openPocketMap.get(player.getName());
			
			if(itemWithPocket.equals(event.getCurrentItem())){
				player.sendMessage(ChatColor.RED + "You can not move an item while looking in its pockets.");
				event.setCancelled(true);
			}
			
			
			return;
		}

		if (!holdingHoe(player))
			return;

		Pocket pocket = plugin.getPocket(event.getCurrentItem());
		
		if (pocket == null) {
			player.sendMessage("No pockets");
			return;
		}
		
		List<ItemStack> items = plugin.getPocketContents(pocket);

		int rows = (items.size() / 9) + 1;

		Inventory pocketsInventory = Bukkit.createInventory(null, rows * 9, "(Pockets)");

		int i = -1;
		for (ItemStack item : items) {
			if (item == null || item.getType().equals(Material.AIR))
				continue;

			i++;
			pocketsInventory.setItem(i, item);
		}

		event.setCancelled(true);

		openPocketMap.put(player.getName(), event.getCurrentItem());

		PocketDelayOpener pocketDelayOpener = new PocketDelayOpener(plugin, player, pocketsInventory);
		pocketDelayOpener.runTaskLater(plugin, 5L * 1);
	}

	private boolean holdingHoe(Player player) {
		if (player.getEquipment().getItemInOffHand() == null)
			return false;

		ItemStack itemInOffHand = player.getEquipment().getItemInOffHand();

		if (!itemInOffHand.getType().equals(Material.WOOD_HOE) && !itemInOffHand.getType().equals(Material.STONE_HOE)
				&& !itemInOffHand.getType().equals(Material.IRON_HOE)
				&& !itemInOffHand.getType().equals(Material.GOLD_HOE)
				&& !itemInOffHand.getType().equals(Material.DIAMOND_HOE))
			return false;

		return true;
	}

}
