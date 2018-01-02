package com.blocktyper.pockets.listeners;

import java.util.List;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;

import com.blocktyper.pockets.PocketsPlugin;
import com.blocktyper.pockets.data.Pocket;

public class InventoryOpenListener extends PocketsListenerBase {

	static final ClickType DEFAULT_CLICK_TYPE = ClickType.RIGHT;

	public InventoryOpenListener(PocketsPlugin plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
	public void inventoryOpenEvent(InventoryOpenEvent event) {

		if (event.getInventory() == null || event.getInventory().getContents() == null) {
			return;
		}

		HumanEntity player = event.getPlayer();

		if (player == null && event.getInventory().getViewers() != null
				&& !event.getInventory().getViewers().isEmpty()) {
			player = event.getInventory().getViewers().get(0);
		}

		if (player == null) {
			return;
		}

		for (ItemStack item : event.getInventory().getContents()) {
			if (item != null) {
				Pocket pocket = getPocket(item);
				if (pocket == null) {
					continue;
				}

				List<ItemStack> contents = getPocketContents(pocket);
				setPocketNbt(item, contents, event.getInventory().getViewers().get(0), true);
			}
		}
	}

}
