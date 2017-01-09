package com.blocktyper.pockets;

import java.util.List;

import org.bukkit.Achievement;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerAchievementAwardedEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PlayerInventoryOpenListener extends PocketsListenerBase {

	static final ClickType DEFAULT_CLICK_TYPE = ClickType.RIGHT;

	public PlayerInventoryOpenListener(PocketsPlugin plugin) {
		super(plugin);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		event.getPlayer().removeAchievement(Achievement.OPEN_INVENTORY);
	}

	@EventHandler
	public void onInventoryOpenEvent(PlayerAchievementAwardedEvent event) {
		if (event.getAchievement().equals(Achievement.OPEN_INVENTORY)) {
			event.setCancelled(true);
			namePockets(event.getPlayer(), event.getPlayer().getInventory());
		}
	}

	private void namePockets(HumanEntity player, Inventory inventory) {
		if (player == null) {
			return;
		}

		if (inventory == null || inventory.getContents() == null) {
			return;
		}

		for (ItemStack item : player.getInventory().getContents()) {
			if (item != null) {
				Pocket pocket = getPocket(item, player);
				if (pocket == null) {
					continue;
				}

				List<ItemStack> contents = getPocketContents(pocket);
				setPocketJson(item, contents, player, true);
			}
		}
	}

}
