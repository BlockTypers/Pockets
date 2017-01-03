package com.blocktyper.pockets;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import com.blocktyper.plugin.BlockTyperPlugin;

public class PocketsPlugin extends BlockTyperPlugin {

	public static final String RESOURCE_NAME = "com.blocktyper.pockets.resources.PocketsMessages";

	private String pocketName;
	private PocketsListenerBase pocketsListenerBase;

	public PocketsPlugin() {
		super();
	}

	public void onEnable() {
		super.onEnable();
		new PocketsCommand(this);
		pocketsListenerBase = new InventoryClickListener(this);
		new BlockPlaceListener(this);
		pocketName = getConfig().getString(ConfigKeyEnum.POCKET_NAME.getKey());
		registerPocketRecipes();
	}

	// recipes
	private void registerPocketRecipes() {
		List<String> mats = getConfig().getStringList(ConfigKeyEnum.MATERIALS_WHICH_CAN_HAVE_POCKETS.getKey());

		if (mats != null && !mats.isEmpty()) {
			mats.forEach(m -> registerPocketRecipe(m));
		} else {
			debugWarning("No MATERIALS_WHICH_CAN_HAVE_POCKETS");
		}

		// recipeRegistrar().registerRecipe(recipeKey, recipeName, lore,
		// outputMaterial, amount, opOnly, materialMatrix, itemStartsWithMatrix,
		// recipeKeepMatrix, plugin, listenersList);
	}

	private void registerPocketRecipe(String materialName) {

		Material outputMaterial = Material.matchMaterial(materialName);

		if (outputMaterial == null)
			return;

		String pocketMaterialName = getConfig().getString(ConfigKeyEnum.POCKET_MATERIAL.getKey());
		Material pocketMaterial = Material.matchMaterial(pocketMaterialName);

		if (pocketMaterial == null)
			return;

		if (pocketName == null || pocketName.isEmpty())
			return;

		String recipeKey = outputMaterial.name() + "-with-pocket";

		ItemStack outputItem = new ItemStack(outputMaterial);
		pocketsListenerBase.setPocketJson(outputItem, new ArrayList<>());

		List<String> lore = outputItem.getItemMeta().getLore();
		int amount = 1;
		boolean opOnly = false;
		String recipeName = null;

		// SPS
		// SSS
		// SMS
		List<Material> materialMatrix = new ArrayList<>();
		materialMatrix.add(0, Material.STRING);
		materialMatrix.add(1, pocketMaterial);
		materialMatrix.add(2, Material.STRING);
		materialMatrix.add(3, Material.STRING);
		materialMatrix.add(4, Material.STRING);
		materialMatrix.add(5, Material.STRING);
		materialMatrix.add(6, Material.STRING);
		materialMatrix.add(7, outputMaterial);
		materialMatrix.add(8, Material.STRING);

		List<String> itemStartsWithMatrix = new ArrayList<>();
		itemStartsWithMatrix.add("1=" + pocketName);

		List<String> recipeKeepMatrix = null;
		List<String> listenersList = null;

		debugWarning("Pocket material: " + materialName);

		recipeRegistrar().registerRecipe(recipeKey, recipeName, lore, outputMaterial, amount, opOnly, materialMatrix,
				itemStartsWithMatrix, recipeKeepMatrix, this, listenersList);

	}

	public String getPocketName() {
		return pocketName;
	}

	// begin localization
	private ResourceBundle bundle = null;

	public ResourceBundle getBundle() {
		if (bundle == null)
			bundle = ResourceBundle.getBundle(RESOURCE_NAME, locale);
		return bundle;
	}

	// end localization

}
