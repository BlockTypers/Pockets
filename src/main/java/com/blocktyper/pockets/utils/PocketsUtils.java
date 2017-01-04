package com.blocktyper.pockets.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.blocktyper.pockets.ConfigKeyEnum;
import com.blocktyper.pockets.PocketsPlugin;

public class PocketsUtils {

	public static void registerPocketRecipes(PocketsPlugin plugin) {
		List<String> mats = plugin.getConfig().getStringList(ConfigKeyEnum.MATERIALS_WHICH_CAN_HAVE_POCKETS.getKey());

		if (mats != null && !mats.isEmpty()) {
			mats.forEach(m -> registerPocketRecipe(m, plugin));
		} else {
			plugin.debugWarning("No MATERIALS_WHICH_CAN_HAVE_POCKETS");
		}

		// recipeRegistrar().registerRecipe(recipeKey, recipeName, lore,
		// outputMaterial, amount, opOnly, materialMatrix, itemStartsWithMatrix,
		// recipeKeepMatrix, plugin, listenersList);
	}

	private static void registerPocketRecipe(String materialName, PocketsPlugin plugin) {

		Material outputMaterial = Material.matchMaterial(materialName);

		if (outputMaterial == null)
			return;

		String pocketMaterialName = plugin.getConfig().getString(ConfigKeyEnum.POCKET_MATERIAL.getKey());
		Material pocketMaterial = Material.matchMaterial(pocketMaterialName);

		if (pocketMaterial == null)
			return;

		if (plugin.getPocketName() == null || plugin.getPocketName().isEmpty())
			return;

		String recipeKey = outputMaterial.name() + "-with-pocket";

		ItemStack outputItem = new ItemStack(outputMaterial);
		plugin.getInventoryClickListener().setPocketJson(outputItem, new ArrayList<>());

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
		itemStartsWithMatrix.add("1=" + plugin.getPocketName());

		List<String> recipeKeepMatrix = null;
		List<String> listenersList = null;

		plugin.debugWarning("Pocket material: " + materialName);

		plugin.recipeRegistrar().registerRecipe(recipeKey, recipeName, lore, outputMaterial, amount, opOnly,
				materialMatrix, itemStartsWithMatrix, recipeKeepMatrix, plugin, listenersList);

	}

	public static ItemStack[] getTestItems(PocketsPlugin plugin) {
		List<ItemStack> contentsList = new ArrayList<>();

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

		
				
		// DIAMOND HELM
		ItemStack diamondHelm = new ItemStack(Material.DIAMOND_HELMET);
		ItemMeta diamondHelmMeta = diamondHelm.getItemMeta();
		List<String> diamondHelmLore = new ArrayList<>();
		diamondHelmMeta.setDisplayName("Diamond Helm");
		diamondHelmLore.add("Diamond Lore");
		diamondHelmMeta.setLore(diamondHelmLore);
		diamondHelmMeta.addEnchant(Enchantment.THORNS, 2, true);
		diamondHelm.setItemMeta(diamondHelmMeta);
		plugin.getInventoryClickListener().setPocketJson(diamondHelm, null);
		contentsList.add(diamondHelm);
		
		// GOLD HELM
		ItemStack goldHelm = new ItemStack(Material.GOLD_HELMET);
		ItemMeta goldHelmMeta = goldHelm.getItemMeta();
		List<String> goldHelmLore = new ArrayList<>();
		goldHelmMeta.setDisplayName("Gold Helm");
		goldHelmLore.add("Gold Lore");
		goldHelmMeta.setLore(goldHelmLore);
		goldHelm.setItemMeta(goldHelmMeta);
		plugin.getInventoryClickListener().setPocketJson(goldHelm, null);
		contentsList.add(goldHelm);
		
		
		// IRON HELM
		ItemStack ironHelm = new ItemStack(Material.IRON_HELMET);
		plugin.getInventoryClickListener().setPocketJson(ironHelm, null);
		contentsList.add(ironHelm);

		// PUMPKIN
		ItemStack pumpkin = new ItemStack(Material.PUMPKIN);
		plugin.getInventoryClickListener().setPocketJson(pumpkin, null);
		contentsList.add(pumpkin);

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
		Material pocketMaterial = Material
				.matchMaterial(plugin.getConfig().getString(ConfigKeyEnum.POCKET_MATERIAL.getKey()));
		String pocketName = plugin.getConfig().getString(ConfigKeyEnum.POCKET_NAME.getKey());
		ItemStack pocket = new ItemStack(pocketMaterial, pocketMaterial.getMaxStackSize());
		ItemMeta pocketMeta = pocket.getItemMeta();
		pocketMeta.setDisplayName(pocketName);
		pocket.setItemMeta(pocketMeta);
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
