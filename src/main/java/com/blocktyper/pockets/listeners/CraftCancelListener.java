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

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void prepareItemCraft(PrepareItemCraftEvent event) {
		ItemStack[] craftingMatrix = event.getInventory().getMatrix();
		
		ItemStack itemWithPocket = craftingMatrix[7];
		
		Pocket pocket = getPocket(itemWithPocket, event.getViewers().get(0));
		
		if(pocket != null){
			event.getInventory().setResult(null);
		}
		
	}

}
