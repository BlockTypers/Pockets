package com.blocktyper.pockets.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.blocktyper.v1_2_5.helpers.InvisHelper;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.blocktyper.pockets.PocketsPlugin;
import com.blocktyper.pockets.data.Pocket;
import com.blocktyper.pockets.listeners.PocketsListenerBase;
import com.blocktyper.v1_2_5.recipes.AbstractBlockTyperRecipe;
import com.blocktyper.v1_2_5.recipes.IRecipe;
import com.blocktyper.v1_2_5.serialization.CardboardBox;

public class OldPocketHelper {

	PocketsPlugin plugin;

	public OldPocketHelper(PocketsPlugin plugin) {
		this.plugin = plugin;
	}

	public Pocket getPocketOld(ItemStack item, HumanEntity player) {

		Pocket pocket = plugin.getInvisHelper().getObjectFromInvisisibleLore(item,
				PocketsListenerBase.POCKETS_HIDDEN_LORE_KEY, Pocket.class);

		if (pocket != null && pocket.getContents() != null && !pocket.getContents().isEmpty()) {
			List<CardboardBox> newContents = new ArrayList<>();
			for (CardboardBox box : pocket.getContents()) {
				ItemStack unboxedItem = box != null ? box.unbox() : null;
				if (unboxedItem != null) {

					if (unboxedItem.getItemMeta() != null && unboxedItem.getItemMeta().getLore() != null) {
						List<String> lore = new ArrayList<>();
						for (String loreLine : unboxedItem.getItemMeta().getLore()) {
							if (AbstractBlockTyperRecipe.isHiddenRecipeKey(loreLine)) {
								lore.add(InvisHelper.convertToInvisibleString(loreLine));
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

	public static void setLegacyPocketJson(ItemStack itemWithPocket, List<ItemStack> itemsInPocket, HumanEntity player,
			boolean includePrefix, PocketsPlugin plugin) {

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

		IRecipe recipe = plugin.recipeRegistrar().getRecipeFromKey(PocketsPlugin.POCKET_RECIPE_KEY);
		String pocketName = plugin.recipeRegistrar().getNameConsiderLocalization(recipe, player);

		String visiblePrefix = includePrefix ? pocketName + " [" + itemCount + "]" : null;

		plugin.getInvisHelper().setInvisisbleJson(pocket, itemWithPocket,
				PocketsListenerBase.POCKETS_HIDDEN_LORE_KEY, visiblePrefix);
	}

}
