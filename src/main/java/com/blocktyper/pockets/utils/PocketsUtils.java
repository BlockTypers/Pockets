package com.blocktyper.pockets.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.blocktyper.pockets.ConfigKeyEnum;
import com.blocktyper.pockets.PocketsPlugin;
import com.blocktyper.pockets.data.Pocket;
import com.blocktyper.pockets.listeners.PocketsListenerBase;
import com.blocktyper.v1_2_2.IBlockTyperPlugin;
import com.blocktyper.v1_2_2.helpers.ComplexMaterial;
import com.blocktyper.v1_2_2.helpers.InvisibleLoreHelper;
import com.blocktyper.v1_2_2.recipes.AbstractBlockTyperRecipe;
import com.blocktyper.v1_2_2.recipes.IRecipe;

public class PocketsUtils {

	public static void registerPocketRecipes(PocketsPlugin plugin) {
		List<String> mats = plugin.getConfig().getStringList(ConfigKeyEnum.MATERIALS_WHICH_CAN_HAVE_POCKETS.getKey());

		Map<String, Object> nbtObjectData = new HashMap<>();
		nbtObjectData.put(PocketsListenerBase.POCKET_NBT_JSON_KEY, new Pocket());

		if (mats != null && !mats.isEmpty()) {
			mats.forEach(m -> registerPocketRecipe(m, plugin, nbtObjectData));
		} else {
			plugin.debugWarning("No MATERIALS_WHICH_CAN_HAVE_POCKETS");
		}

		// recipeRegistrar().registerRecipe(recipeKey, recipeName, lore,
		// outputMaterial, amount, opOnly, materialMatrix, itemStartsWithMatrix,
		// recipeKeepMatrix, plugin, listenersList);
	}

	public static String getPocketWithPocketRecipeKey(String materialName) {
		String recipeKey = materialName + "-with-pocket";
		return recipeKey;
	}

	private static void registerPocketRecipe(String materialName, PocketsPlugin plugin,
			Map<String, Object> nbtObjectData) {

		ComplexMaterial outputMaterial = ComplexMaterial.fromString(materialName);

		IRecipe pocketRecipe = plugin.recipeRegistrar().getRecipeFromKey(PocketsPlugin.POCKET_RECIPE_KEY);
		if (pocketRecipe == null)
			return;

		String defaultPocketName = pocketRecipe.getName();

		if (outputMaterial == null)
			return;

		String pocketMaterialName = plugin.getConfig().getString(ConfigKeyEnum.POCKET_MATERIAL.getKey());
		ComplexMaterial pocketMaterial = ComplexMaterial.fromString(pocketMaterialName);

		if (pocketMaterial == null)
			return;

		String recipeKey = getPocketWithPocketRecipeKey(materialName);

		Integer transferSourceNameSlot = 7;

		// SPS
		// SSS
		// SMS
		List<Material> materialMatrix = new ArrayList<>();
		materialMatrix.add(0, Material.STRING);
		materialMatrix.add(1, pocketMaterial.getMaterial());
		materialMatrix.add(2, Material.STRING);
		materialMatrix.add(3, Material.STRING);
		materialMatrix.add(4, Material.STRING);
		materialMatrix.add(5, Material.STRING);
		materialMatrix.add(6, Material.STRING);
		materialMatrix.add(transferSourceNameSlot, outputMaterial.getMaterial());
		materialMatrix.add(8, Material.STRING);
		
		
		List<Byte> materialDataMatrix = new ArrayList<>();
		materialDataMatrix.add(0, null);
		materialDataMatrix.add(1, pocketMaterial.getData());
		materialDataMatrix.add(2, null);
		materialDataMatrix.add(3, null);
		materialDataMatrix.add(4, null);
		materialDataMatrix.add(5, null);
		materialDataMatrix.add(6, null);
		materialDataMatrix.add(transferSourceNameSlot, outputMaterial.getData());
		materialDataMatrix.add(8, null);

		plugin.debugWarning("Pocket material: " + materialName);

		AbstractBlockTyperRecipe recipe = new PocketsRecipe(recipeKey, materialMatrix, materialDataMatrix, outputMaterial.getMaterial(), plugin);
		
		recipe.setOutputData(outputMaterial.getData());

		List<Integer> transferSourceLoreAndEnchantmentMatrix = new ArrayList<>();
		transferSourceLoreAndEnchantmentMatrix.add(transferSourceNameSlot);

		recipe.setTransferSourceLoreMatrix(transferSourceLoreAndEnchantmentMatrix);
		recipe.setTransferSourceEnchantmentMatrix(transferSourceLoreAndEnchantmentMatrix);
		recipe.setTransferSourceNameSlot(transferSourceNameSlot);

		Map<Integer, String> itemHasNbtKeyMatrix = new HashMap<>();
		itemHasNbtKeyMatrix.put(1, PocketsPlugin.POCKET_RECIPE_KEY);
		recipe.setItemHasNbtKeyMatrix(itemHasNbtKeyMatrix);

		recipe.setNbtObjectData(nbtObjectData);

		List<String> defaultInitialLore = new ArrayList<>();
		addPocketNameToLoreFirstLine(defaultInitialLore, defaultPocketName, plugin);
		recipe.setInitialLore(defaultInitialLore);

		List<String> locales = pocketRecipe.getLocales();
		if (locales != null && !locales.isEmpty()) {
			for (String locale : locales) {
				List<String> localeInitialPocketLore = new ArrayList<>();
				String localePocketName = null;
				if (pocketRecipe.getLocaleNameMap().containsKey(locale)) {
					localePocketName = pocketRecipe.getLocaleNameMap().get(locale);
				} else {
					localePocketName = defaultPocketName;
				}

				localePocketName = localePocketName == null ? defaultPocketName : localePocketName;

				addPocketNameToLoreFirstLine(localeInitialPocketLore, localePocketName, plugin);
				recipe.getLocaleInitialLoreMap().put(locale, localeInitialPocketLore);
			}
		}

		recipe.setNonStacking(true);

		plugin.recipeRegistrar().registerRecipe(recipe);

	}

	private static void addPocketNameToLoreFirstLine(List<String> lore, String pocketName, IBlockTyperPlugin plugin) {
		String invisiblePrefix = InvisibleLoreHelper
				.convertToInvisibleString(PocketsListenerBase.POCKETS_SIZE_HIDDEN_LORE_KEY);
		lore.add(0, invisiblePrefix + plugin.getConfig().getString(ConfigKeyEnum.DEFAULT_POCKET_COLOR.getKey(),
				PocketsPlugin.DEFAULT_POCKET_COLOR) + pocketName + " [0]");
	}

	public static ItemStack[] getTestItems(PocketsPlugin plugin, HumanEntity player) {
		List<ItemStack> contentsList = new ArrayList<>();

		// Existing Chest
		ItemStack exisitingChest = new ItemStack(Material.GOLD_CHESTPLATE);
		ItemMeta exisitingChestMeta = exisitingChest.getItemMeta();
		exisitingChestMeta.setDisplayName("Exisiting Chest No Damage");
		List<String> exisitingChestLore = new ArrayList<>();
		exisitingChestLore.add("Gold Chest Lore");
		exisitingChestMeta.setLore(exisitingChestLore);
		exisitingChestMeta.addEnchant(Enchantment.KNOCKBACK, 2, true);
		exisitingChest.setItemMeta(exisitingChestMeta);
		contentsList.add(exisitingChest);

		// EXCALIBER
		ItemStack excaliber = new ItemStack(Material.DIAMOND_SWORD);
		short durability = 800;
		excaliber.setDurability(durability);
		ItemMeta excaliberMeta = excaliber.getItemMeta();
		excaliberMeta.setDisplayName("Excaliber");
		List<String> excaliberLore = new ArrayList<>();
		excaliberLore.add("Mighty Lore");
		excaliberMeta.setLore(excaliberLore);
		excaliberMeta.addEnchant(Enchantment.KNOCKBACK, 2, true);
		excaliber.setItemMeta(excaliberMeta);
		contentsList.add(excaliber);

		// OLD POCKET
		ItemStack oldPocketedItem = new ItemStack(Material.DIAMOND_HELMET);
		ItemMeta oldPocketedItemMeta = oldPocketedItem.getItemMeta();
		List<String> oldPocketedItemLore = new ArrayList<>();
		oldPocketedItemMeta.setDisplayName("Old Pocket");
		oldPocketedItemLore.add("Hidden Json below");
		oldPocketedItemMeta.setLore(oldPocketedItemLore);
		oldPocketedItemMeta.addEnchant(Enchantment.THORNS, 2, true);
		oldPocketedItem.setItemMeta(oldPocketedItemMeta);
		OldPocketHelper.setLegacyPocketJson(oldPocketedItem, Arrays.asList(new ItemStack(Material.STONE)), player, true,
				plugin);
		contentsList.add(oldPocketedItem);

		// IRON HELM
		String ironHelmKey = getPocketWithPocketRecipeKey(Material.IRON_HELMET.name());
		ItemStack ironHelm = plugin.recipeRegistrar().getItemFromRecipe(ironHelmKey, player, null, 1);
		contentsList.add(ironHelm);

		// PUMPKINS
		String pumpkinKey = getPocketWithPocketRecipeKey(Material.PUMPKIN.name());
		ItemStack pumpkin = plugin.recipeRegistrar().getItemFromRecipe(pumpkinKey, player, null, 1);
		contentsList.add(pumpkin);
		ItemStack pumpkin2 = plugin.recipeRegistrar().getItemFromRecipe(pumpkinKey, player, null, 1);
		contentsList.add(pumpkin2);

		// STRING
		ItemStack string = new ItemStack(Material.STRING, 64);
		contentsList.add(string);

		// RABBIT_HIDE
		ItemStack rabbitHide = new ItemStack(Material.RABBIT_HIDE, 64);
		contentsList.add(rabbitHide);

		// IRON_INGOT
		ItemStack ironIngot = new ItemStack(Material.IRON_INGOT, 64);
		contentsList.add(ironIngot);

		// POCKETS
		ItemStack pocket = plugin.recipeRegistrar().getItemFromRecipe(PocketsPlugin.POCKET_RECIPE_KEY, player, null,
				-1);
		contentsList.add(pocket);

		// Original Pocket
		IRecipe pocketRecipe = plugin.recipeRegistrar().getRecipeFromKey(PocketsPlugin.POCKET_RECIPE_KEY);
		ItemStack flowerPocket = new ItemStack(pocket.getType(), 1);
		ItemMeta flowerPocketMeta = flowerPocket.getItemMeta();
		flowerPocketMeta.setDisplayName(pocketRecipe.getName());
		flowerPocket.setItemMeta(flowerPocketMeta);
		contentsList.add(flowerPocket);

		// SADDLE
		ItemStack saddle = new ItemStack(Material.SADDLE);
		contentsList.add(saddle);

		// DIAMOND_BARDING
		ItemStack horseArmor = new ItemStack(Material.DIAMOND_BARDING);
		contentsList.add(horseArmor);

		// CARPET
		ItemStack carpet = new ItemStack(Material.CARPET);
		contentsList.add(carpet);

		// ITEM_FRAME
		ItemStack itemFrame = new ItemStack(Material.ITEM_FRAME);
		contentsList.add(itemFrame);

		// ENCHANTING TABLE
		ItemStack enchantmentTable = new ItemStack(Material.ENCHANTMENT_TABLE);
		contentsList.add(enchantmentTable);

		// LAPIS_ORE
		ItemStack lapisOre = new ItemStack(Material.LAPIS_BLOCK, 1);
		contentsList.add(lapisOre);

		// ANVIL
		ItemStack anvil = new ItemStack(Material.ANVIL);
		contentsList.add(anvil);

		// KNOCKBACK BOOK
		ItemStack knockbackBook = new ItemStack(Material.ENCHANTED_BOOK);
		EnchantmentStorageMeta knockbackMeta = (EnchantmentStorageMeta) knockbackBook.getItemMeta();
		knockbackMeta.addStoredEnchant(Enchantment.KNOCKBACK, 1, true);
		knockbackBook.setItemMeta(knockbackMeta);
		contentsList.add(knockbackBook);

		// PROT BOOK
		ItemStack protectionFireBook = new ItemStack(Material.ENCHANTED_BOOK);
		EnchantmentStorageMeta protectionFireMeta = (EnchantmentStorageMeta) protectionFireBook.getItemMeta();
		protectionFireMeta.addStoredEnchant(Enchantment.PROTECTION_FIRE, 1, true);
		protectionFireBook.setItemMeta(protectionFireMeta);
		contentsList.add(protectionFireBook);

		ItemStack expBottle = new ItemStack(Material.EXP_BOTTLE, Material.EXP_BOTTLE.getMaxStackSize());
		contentsList.add(expBottle);

		ItemStack[] contents = contentsList.toArray(new ItemStack[contentsList.size()]);
		return contents;
	}
}
