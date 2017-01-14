package com.blocktyper.pockets.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.blocktyper.helpers.InvisibleLoreHelper;
import com.blocktyper.nbt.NBTItem;
import com.blocktyper.plugin.IBlockTyperPlugin;
import com.blocktyper.pockets.ConfigKeyEnum;
import com.blocktyper.pockets.PocketsPlugin;
import com.blocktyper.pockets.data.Pocket;
import com.blocktyper.pockets.listeners.PocketsListenerBase;
import com.blocktyper.recipes.BlockTyperRecipe;
import com.blocktyper.recipes.IRecipe;
import com.blocktyper.serialization.CardboardBox;

public class PocketsUtils {

	public static void registerPocketRecipes(PocketsPlugin plugin) {
		List<String> mats = plugin.getConfig().getStringList(ConfigKeyEnum.MATERIALS_WHICH_CAN_HAVE_POCKETS.getKey());

		Map<String, String> nbtStringData = new HashMap<>();
		NBTItem nbtItem = new NBTItem(new ItemStack(Material.STONE));
		nbtItem.setObject(PocketsListenerBase.POCKET_NBT_JSON_KEY, new Pocket());
		String emptyPocketJsonString = nbtItem.getString(PocketsListenerBase.POCKET_NBT_JSON_KEY);
		nbtStringData.put(PocketsListenerBase.POCKET_NBT_JSON_KEY, emptyPocketJsonString);

		if (mats != null && !mats.isEmpty()) {
			mats.forEach(m -> registerPocketRecipe(m, plugin, nbtStringData));
		} else {
			plugin.debugWarning("No MATERIALS_WHICH_CAN_HAVE_POCKETS");
		}

		// recipeRegistrar().registerRecipe(recipeKey, recipeName, lore,
		// outputMaterial, amount, opOnly, materialMatrix, itemStartsWithMatrix,
		// recipeKeepMatrix, plugin, listenersList);
	}

	public static String getPocketWithPocketRecipeKey(Material material) {
		String recipeKey = material.name() + "-with-pocket";
		return recipeKey;
	}

	private static void registerPocketRecipe(String materialName, PocketsPlugin plugin,
			Map<String, String> nbtStringData) {

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

		String recipeKey = getPocketWithPocketRecipeKey(outputMaterial);

		ItemStack outputItem = new ItemStack(outputMaterial);
		outputItem = plugin.getInventoryClickListener().setPocketNbt(outputItem, new ArrayList<>(), null, false);

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

		Map<Integer, String> itemHasNbtKeyMatrix = new HashMap<>();
		itemHasNbtKeyMatrix.put(1, PocketsPlugin.POCKET_RECIPE_KEY);
		recipe.setItemHasNbtKeyMatrix(itemHasNbtKeyMatrix);

		recipe.setNbtStringData(nbtStringData);

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
		String invisiblePrefix = InvisibleLoreHelper.convertToInvisibleString(PocketsListenerBase.POCKETS_SIZE_HIDDEN_LORE_KEY);
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
		setLegacyPocketJson(oldPocketedItem, Arrays.asList(new ItemStack(Material.STONE)), player, true, plugin);
		contentsList.add(oldPocketedItem);

		// DIAMOND HELM
		ItemStack diamondHelm = new ItemStack(Material.DIAMOND_HELMET);
		ItemMeta diamondHelmMeta = diamondHelm.getItemMeta();
		List<String> diamondHelmLore = new ArrayList<>();
		diamondHelmMeta.setDisplayName("Diamond Helm");
		diamondHelmLore.add("Diamond Lore");
		diamondHelmMeta.setLore(diamondHelmLore);
		diamondHelmMeta.addEnchant(Enchantment.THORNS, 2, true);
		diamondHelm.setItemMeta(diamondHelmMeta);
		diamondHelm = plugin.getInventoryClickListener().setPocketNbt(diamondHelm, null, player, true);
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
		goldBootsMeta.setDisplayName(
				"Foreign Invisible Text In Name" + InvisibleLoreHelper.convertToInvisibleString("Invisible Name"));
		goldBoots.setItemMeta(goldBootsMeta);
		contentsList.add(goldBoots);

		// FOREIGN INVIS IN LORE DIAMOND_BOOTS
		ItemStack diamondBoots = new ItemStack(Material.DIAMOND_BOOTS);
		ItemMeta diamondBootsMeta = diamondBoots.getItemMeta();
		List<String> diamondBootsLore = new ArrayList<>();
		diamondBootsMeta.setDisplayName("Foreign Invisible Lore");
		diamondBootsLore.add(InvisibleLoreHelper.convertToInvisibleString("Invisible Lore"));
		diamondBootsMeta.setLore(diamondBootsLore);
		diamondBoots.setItemMeta(diamondBootsMeta);
		contentsList.add(diamondBoots);

		// IRON HELM
		ItemStack ironHelm = new ItemStack(Material.IRON_HELMET);
		ironHelm = plugin.getInventoryClickListener().setPocketNbt(ironHelm, null, player, true);
		contentsList.add(ironHelm);

		// PUMPKINS
		String pumpkinKey = getPocketWithPocketRecipeKey(Material.PUMPKIN);
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
		pocket.setAmount(3);
		contentsList.add(pocket);

		// hidden lore Pocket
		IRecipe pocketRecipe = plugin.recipeRegistrar().getRecipeFromKey(PocketsPlugin.POCKET_RECIPE_KEY);
		ItemStack hidenLorePocket = new ItemStack(pocket.getType(), 2);
		ItemMeta hidenLorePocketMeta = diamondBoots.getItemMeta();
		List<String> hidenLorePocketLore = plugin.recipeRegistrar().getLoreConsiderLocalization(pocketRecipe, player);
		String recipeKeyToBeHidden = BlockTyperRecipe.getRecipeKeyToBeHidden(PocketsPlugin.POCKET_RECIPE_KEY);
		hidenLorePocketLore.add("hidden recipe key below");
		hidenLorePocketLore.add(InvisibleLoreHelper.convertToInvisibleString(recipeKeyToBeHidden));
		hidenLorePocketMeta.setLore(hidenLorePocketLore);
		hidenLorePocketMeta.setDisplayName(plugin.recipeRegistrar().getNameConsiderLocalization(pocketRecipe, player));
		hidenLorePocket.setItemMeta(hidenLorePocketMeta);
		contentsList.add(hidenLorePocket);

		// Original Pocket
		ItemStack flowerPocket = new ItemStack(pocket.getType(), 1);
		ItemMeta flowerPocketMeta = diamondBoots.getItemMeta();
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

		plugin.getInvisibleLoreHelper().setInvisisbleJson(pocket, itemWithPocket,
				PocketsListenerBase.POCKETS_HIDDEN_LORE_KEY, visiblePrefix);
	}
}
