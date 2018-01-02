package com.blocktyper.pockets.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

import com.blocktyper.pockets.PocketsPlugin;
import com.blocktyper.pockets.data.Pocket;

public class CraftCancelListener extends PocketsListenerBase {

	public CraftCancelListener(PocketsPlugin plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void prepareItemCraft(PrepareItemCraftEvent event) {
		ItemStack[] craftingMatrix = event.getInventory().getMatrix();
		ItemStack itemWithPocket = craftingMatrix[7];
		if(itemWithPocket != null) {
			Pocket pocket = getPocket(itemWithPocket);
			if(pocket != null){
				event.getInventory().setResult(null);
			}
		}
	}

}
