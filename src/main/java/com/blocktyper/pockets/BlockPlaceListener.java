package com.blocktyper.pockets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import com.blocktyper.serialization.CardboardBox;

import net.md_5.bungee.api.ChatColor;

public class BlockPlaceListener extends PocketsListenerBase {
	
	private static final String IS_EMPTY_POCKET = "POCKETS-IS_EMPTY_POCKET";

	public BlockPlaceListener(PocketsPlugin plugin) {
		super(plugin);
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onPocketBlockPlaceEvent(BlockPlaceEvent event) {

		ItemStack itemInHand = plugin.getPlayerHelper().getItemInHand(event.getPlayer());

		if (itemInHand == null) {
			plugin.debugInfo("[onPocketBlockPlaceEvent] itemInHand == null");
			return;
		}

		Pocket pocket = getPocket(itemInHand);

		if (pocket == null) {
			plugin.debugInfo("[onPocketBlockPlaceEvent] pocket == null");
			return;
		}

		if (pocket.getContents() == null || pocket.getContents().isEmpty()) {
			plugin.debugInfo(
					"[onPocketBlockPlaceEvent] pocket.getContents() == null || pocket.getContents().isEmpty()");
			return;
		}

		pocket.getContents().forEach(i -> dropItem(i, event.getBlock()));
		
		event.getPlayer().sendMessage(ChatColor.RED + "The item you just placed had a pocket attached. If you pick it back up soon, it might still be there.");

		MetadataValue mdv = new FixedMetadataValue(plugin, true); 
		event.getBlock().setMetadata(IS_EMPTY_POCKET, mdv);
	}

	private void dropItem(CardboardBox box, Block block) {
		if (block == null)
			return;
		if (box == null)
			return;
		ItemStack item = box.unbox();

		if (item == null)
			return;
		block.getWorld().dropItemNaturally(block.getLocation(), item);
	}
	
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onBlockBreakEvent(BlockBreakEvent event) {
		Collection<ItemStack> drops = event.getBlock()
				.getDrops(plugin.getPlayerHelper().getItemInHand(event.getPlayer()));

		if (drops == null || drops.isEmpty()) {
			plugin.debugInfo("[onBlockBreakEvent] drops == null || drops.isEmpty()");
			return;
		}

		List<MetadataValue> list = event.getBlock().getMetadata(IS_EMPTY_POCKET);
		
		if(list != null && !list.isEmpty() && list.get(0) != null && list.get(0).asBoolean()){
			plugin.debugInfo("[onBlockBreakEvent] empty pocket");

			for (ItemStack drop : drops) {
				if(drop.getType().equals(event.getBlock().getType())){
					plugin.debugInfo("[onBlockBreakEvent] drop type matches block type");
					setPocketJson(drop, new ArrayList<>());
				}else{
					plugin.debugInfo("[onBlockBreakEvent] drop type does not match block type");
				}
				event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), drop);
			}
			
			event.getBlock().setType(Material.AIR);
			event.setCancelled(true);
		}else{
			plugin.debugInfo("[onBlockBreakEvent] not empty pocket");
		}
	}

}
