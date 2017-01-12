package com.blocktyper.pockets.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.blocktyper.helpers.InvisibleLoreHelper;
import com.blocktyper.pockets.PocketsPlugin;
import com.blocktyper.pockets.data.Pocket;
import com.blocktyper.pockets.listeners.PocketsListenerBase;
import com.blocktyper.recipes.BlockTyperRecipe;
import com.blocktyper.serialization.CardboardBox;

public class OldPocketHelper {
	
	PocketsPlugin plugin;

	public OldPocketHelper(PocketsPlugin plugin) {
		this.plugin = plugin;
	}

	public Pocket getPocketOld(ItemStack item, HumanEntity player) {

		Pocket pocket = plugin.getInvisibleLoreHelper().getObjectFromInvisisibleLore(item, PocketsListenerBase.POCKETS_HIDDEN_LORE_KEY,
				Pocket.class);

		if (pocket != null && pocket.getContents() != null && !pocket.getContents().isEmpty()) {
			List<CardboardBox> newContents = new ArrayList<>();
			for (CardboardBox box : pocket.getContents()) {
				ItemStack unboxedItem = box != null ? box.unbox() : null;
				if (unboxedItem != null) {

					if (unboxedItem.getItemMeta() != null && unboxedItem.getItemMeta().getLore() != null) {
						List<String> lore = new ArrayList<>();
						for (String loreLine : unboxedItem.getItemMeta().getLore()) {
							if (BlockTyperRecipe.isHiddenRecipeKey(loreLine)) {
								lore.add(InvisibleLoreHelper.convertToInvisibleString(loreLine));
							} else {
								lore.add(loreLine);
							}
						}

						ItemMeta itemMeta = unboxedItem.getItemMeta();
						itemMeta.setLore(lore);
						unboxedItem.setItemMeta(itemMeta);
					}

					CardboardBox newBox = new CardboardBox(unboxedItem);
					newContents.add(newBox);
				} else {
					newContents.add(box);
				}
			}
			pocket.setContents(newContents);
		}

		return pocket;
	}

}
