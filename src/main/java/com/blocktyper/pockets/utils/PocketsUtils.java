package com.blocktyper.pockets.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.blocktyper.plugin.IBlockTyperPlugin;
import com.blocktyper.pockets.ConfigKeyEnum;
import com.blocktyper.pockets.PocketsListenerBase;
import com.blocktyper.pockets.PocketsPlugin;
import com.blocktyper.recipes.BlockTyperRecipe;
import com.blocktyper.recipes.IRecipe;

public class PocketsUtils {

	public static void registerPocketRecipes(PocketsPlugin plugin) {
		List<String> mats = plugin.getConfig().getStringList(ConfigKeyEnum.MATERIALS_WHICH_CAN_HAVE_POCKETS.getKey());

		if (mats != null && !mats.isEmpty()) {
			mats.forEach(m -> registerPocketRecipe(m, plugin, true));
			mats.forEach(m -> registerPocketRecipe(m, plugin, false));
		} else {
			plugin.debugWarning("No MATERIALS_WHICH_CAN_HAVE_POCKETS");
		}

		// recipeRegistrar().registerRecipe(recipeKey, recipeName, lore,
		// outputMaterial, amount, opOnly, materialMatrix, itemStartsWithMatrix,
		// recipeKeepMatrix, plugin, listenersList);
	}
	

	private static void registerPocketRecipe(String materialName, PocketsPlugin plugin, boolean useHidenRecipeKeys) {

		Material outputMaterial = Material.matchMaterial(materialName);
		
		IRecipe pocketRecipe = plugin.recipeRegistrar().getRecipeFromKey(PocketsPlugin.POCKET_RECIPE_KEY);
		if (pocketRecipe == null)
			return;
		
		String defaultPocketName = pocketRecipe.getName();

		if (outputMaterial == null)
			return;

		String pocketMaterialName = plugin.getConfig().getString(ConfigKeyEnum.POCKET_MATERIAL.getKey());
		Material pocketMaterial = Material.matchMaterial(pocketMaterialName);

		if (pocketMaterial == null)
			return;

		

		String recipeKey = outputMaterial.name() + "-with-pocket";

		ItemStack outputItem = new ItemStack(outputMaterial);
		plugin.getInventoryClickListener().setPocketJson(outputItem, new ArrayList<>(), null, false);

		List<String> lore = outputItem.getItemMeta().getLore();

		Integer transferSourceNameSlot = 7;
		
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
		materialMatrix.add(transferSourceNameSlot, outputMaterial);
		materialMatrix.add(8, Material.STRING);
		

		plugin.debugWarning("Pocket material: " + materialName);

		BlockTyperRecipe recipe = new BlockTyperRecipe(recipeKey, materialMatrix, outputMaterial, plugin);
		
		List<Integer> transferSourceLoreAndEnchantmentMatrix = new ArrayList<>();
		transferSourceLoreAndEnchantmentMatrix.add(transferSourceNameSlot);
		
		recipe.setTransferSourceLoreMatrix(transferSourceLoreAndEnchantmentMatrix);
		recipe.setTransferSourceEnchantmentMatrix(transferSourceLoreAndEnchantmentMatrix);
		recipe.setTransferSourceNameSlot(transferSourceNameSlot);
		
		if(useHidenRecipeKeys){
			Map<Integer, String> itemHasHiddenKeyMatrix = new HashMap<>();
			itemHasHiddenKeyMatrix.put(1, PocketsPlugin.POCKET_RECIPE_KEY);
			recipe.setItemHasHiddenKeyMatrix(itemHasHiddenKeyMatrix);
		}else{
			Map<Integer, String> itemHasHiddenKeyMatrix = new HashMap<>();
			itemHasHiddenKeyMatrix.put(1, defaultPocketName);
			recipe.setItemStartsWithMatrix(itemHasHiddenKeyMatrix);
		}
		
		
		
		List<String> defaultLore = new ArrayList<>(lore);
		addPocketNameToLoreFirstLine(defaultLore, defaultPocketName, plugin);
		recipe.setLore(defaultLore);

		List<String> locales = pocketRecipe.getLocales();
		if (locales != null && !locales.isEmpty()) {
			for (String locale : locales) {
				List<String> localePocketLore = new ArrayList<>(lore);
				String localePocketName = null;
				if (pocketRecipe.getLocaleNameMap().containsKey(locale)) {
					localePocketName = pocketRecipe.getLocaleNameMap().get(locale);
				} else {
					localePocketName = defaultPocketName;
				}
				
				localePocketName = localePocketName == null ? defaultPocketName : localePocketName;
				
				addPocketNameToLoreFirstLine(localePocketLore, localePocketName, plugin);
				recipe.getLocaleLoreMap().put(locale, localePocketLore);
			}
		}

		plugin.recipeRegistrar().registerRecipe(recipe);

	}
	
	private static void addPocketNameToLoreFirstLine(List<String> lore, String pocketName, IBlockTyperPlugin plugin){
		lore.add(0, pocketName + plugin.getInvisibleLoreHelper()
		.convertToInvisibleString(PocketsListenerBase.POCKETS_HIDDEN_LORE_KEY));
	}

	public static ItemStack[] getTestItems(PocketsPlugin plugin, HumanEntity player) {
		List<ItemStack> contentsList = new ArrayList<>();

		// EXCALIBER
		ItemStack exisitingChest = new ItemStack(Material.GOLD_CHESTPLATE);
		ItemMeta exisitingChestMeta = exisitingChest.getItemMeta();
		exisitingChestMeta.setDisplayName("Exisiting Chest No Damage");
		List<String> exisitingChestLore = new ArrayList<>();
		exisitingChestLore.add("Gold Chest Lore");
		exisitingChestMeta.setLore(exisitingChestLore);
		exisitingChestMeta.addEnchant(Enchantment.KNOCKBACK, 2, true);
		exisitingChest.setItemMeta(exisitingChestMeta);
		contentsList.add(exisitingChest);
		
		ItemStack exisitingChestWithDamage = exisitingChest.clone();
		short durability = 40;
		exisitingChestWithDamage.setDurability(durability);
		ItemMeta exisitingChestWithDamageMeta = exisitingChestWithDamage.getItemMeta();
		exisitingChestWithDamageMeta.setDisplayName("Exisiting Chest With Damage");
		exisitingChestWithDamage.setItemMeta(exisitingChestWithDamageMeta);
		contentsList.add(exisitingChestWithDamage);
		
		// EXCALIBER
		ItemStack excaliber = new ItemStack(Material.DIAMOND_SWORD);
		durability = 800;
		excaliber.setDurability(durability);
		ItemMeta excaliberMeta = excaliber.getItemMeta();
		excaliberMeta.setDisplayName("Excaliber");
		List<String> excaliberLore = new ArrayList<>();
		excaliberLore.add("Mighty Lore");
		excaliberMeta.setLore(excaliberLore);
		excaliberMeta.addEnchant(Enchantment.KNOCKBACK, 2, true);
		excaliber.setItemMeta(excaliberMeta);
		contentsList.add(excaliber);
		
		

		// DIAMOND HELM
		ItemStack diamondHelm = new ItemStack(Material.DIAMOND_HELMET);
		ItemMeta diamondHelmMeta = diamondHelm.getItemMeta();
		List<String> diamondHelmLore = new ArrayList<>();
		diamondHelmMeta.setDisplayName("Diamond Helm");
		diamondHelmLore.add("Diamond Lore");
		diamondHelmMeta.setLore(diamondHelmLore);
		diamondHelmMeta.addEnchant(Enchantment.THORNS, 2, true);
		diamondHelm.setItemMeta(diamondHelmMeta);
		plugin.getInventoryClickListener().setPocketJson(diamondHelm, null, player, true);
		contentsList.add(diamondHelm);

		// GOLD HELM
		ItemStack goldHelm = new ItemStack(Material.GOLD_HELMET);
		ItemMeta goldHelmMeta = goldHelm.getItemMeta();
		List<String> goldHelmLore = new ArrayList<>();
		goldHelmMeta.setDisplayName("Gold Helm");
		goldHelmLore.add("Gold Lore");
		goldHelmMeta.setLore(goldHelmLore);
		goldHelm.setItemMeta(goldHelmMeta);
		contentsList.add(goldHelm);
		
		// FOREIGN INVIS IN NAME GOLD_BOOTS
		ItemStack goldBoots = new ItemStack(Material.GOLD_BOOTS);
		ItemMeta goldBootsMeta = goldBoots.getItemMeta();
		goldBootsMeta.setDisplayName("Foreign Invisible Text In Name" + plugin.getInvisibleLoreHelper().convertToInvisibleString("Invisible Name"));
		goldBoots.setItemMeta(goldBootsMeta);
		contentsList.add(goldBoots);
				
		// FOREIGN INVIS IN LORE DIAMOND_BOOTS
		ItemStack diamondBoots = new ItemStack(Material.DIAMOND_BOOTS);
		ItemMeta diamondBootsMeta = diamondBoots.getItemMeta();
		List<String> diamondBootsLore = new ArrayList<>();
		diamondBootsMeta.setDisplayName("Foreign Invisible Lore");
		diamondBootsLore.add(plugin.getInvisibleLoreHelper().convertToInvisibleString("Invisible Lore"));
		diamondBootsMeta.setLore(diamondBootsLore);
		diamondBoots.setItemMeta(diamondBootsMeta);
		contentsList.add(diamondBoots);

		// IRON HELM
		ItemStack ironHelm = new ItemStack(Material.IRON_HELMET);
		plugin.getInventoryClickListener().setPocketJson(ironHelm, null, player, true);
		contentsList.add(ironHelm);

		// PUMPKINS
		ItemStack pumpkin = new ItemStack(Material.PUMPKIN);
		plugin.getInventoryClickListener().setPocketJson(pumpkin, null, player, true);
		contentsList.add(pumpkin);
		contentsList.add(pumpkin.clone());

		// STRING
		ItemStack string = new ItemStack(Material.STRING, 64);
		contentsList.add(string);

		// RABBIT_HIDE
		ItemStack rabbitHide = new ItemStack(Material.RABBIT_HIDE, 64);
		contentsList.add(rabbitHide);

		// IRON_INGOT
		ItemStack ironIngot = new ItemStack(Material.IRON_INGOT, 64);
		contentsList.add(ironIngot);

		// POCKET
		ItemStack pocket = plugin.recipeRegistrar().getItemFromRecipe(PocketsPlugin.POCKET_RECIPE_KEY, player, null, -1);
		contentsList.add(pocket);

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
