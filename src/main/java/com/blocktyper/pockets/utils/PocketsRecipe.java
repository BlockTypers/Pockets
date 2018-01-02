package com.blocktyper.pockets.utils;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;

import com.blocktyper.v1_2_5.IBlockTyperPlugin;
import com.blocktyper.v1_2_5.recipes.AbstractBlockTyperRecipe;
import com.blocktyper.v1_2_5.recipes.IRecipe;

public class PocketsRecipe extends AbstractBlockTyperRecipe {

	public PocketsRecipe(IRecipe recipe, IBlockTyperPlugin plugin) {
		super(recipe, plugin);
	}
	
	

	public PocketsRecipe(String key, List<Material> materialMatrix, List<Byte> materialDataMatrix, Material output,
			IBlockTyperPlugin plugin) {
		super(key, materialMatrix, materialDataMatrix, output, plugin);
	}



	@Override
	public List<String> getLocalizedLoreForPlugin(IRecipe recipe, HumanEntity player) {
		return null;
	}

}
