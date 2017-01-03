package com.blocktyper.pockets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.blocktyper.serialization.CardboardBox;
import com.google.gson.Gson;

public abstract class PocketsListenerBase implements Listener {

	protected PocketsPlugin plugin;
	private Map<String, ItemStack> openPocketMap = new HashMap<>();

	protected static final int INVENTORY_COLUMNS = 9;
	protected static final Material BLACKOUT_MATERIAL = Material.STAINED_GLASS_PANE;
	protected static final String BLACKOUT_TEXT = "---";
	protected static String POCKETS_KEY = "#PKT";
	protected static int LORE_LINE_LENGTH_LIMIT = 500;
	protected static final Gson JSON_HELPER = new Gson();

	public PocketsListenerBase(PocketsPlugin plugin) {
		this.plugin = plugin;
		this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	public void setPocketJson(ItemStack itemWithPocket, List<ItemStack> itemsInPocket) {

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

		String pocketsJson = JSON_HELPER.toJson(pocket);

		List<String> pocketsTextLines = new ArrayList<>();


		int i = 0;
		while (true) {
			i++;
			String prefix = i + POCKETS_KEY;
			
			if(i == 1){
				prefix = UUID.randomUUID().toString() + ":" + prefix;
			}
			
			String prefixPlusPocketsJson = prefix + pocketsJson;
			
			boolean isBreak = prefixPlusPocketsJson.length() <= LORE_LINE_LENGTH_LIMIT;

			int endIndex = !isBreak ? LORE_LINE_LENGTH_LIMIT : prefixPlusPocketsJson.length();
			
			pocketsTextLines.add(prefixPlusPocketsJson.substring(0, endIndex));

			if (isBreak)
				break;

			pocketsJson = prefixPlusPocketsJson.substring(endIndex);
		}

		ItemMeta meta = itemWithPocket.getItemMeta();

		List<String> lore = null;

		if (meta.getLore() != null) {
			lore = meta.getLore().stream().filter(l -> !loreLineIsPocket(l)).collect(Collectors.toList());
		}

		if (lore == null)
			lore = new ArrayList<>();
		
		List<String> pocketLore = pocketsTextLines.stream().map(l -> convertToInvisibleString(l)).collect(Collectors.toList());
		
		if(pocketLore != null && !pocketLore.isEmpty() && pocketLore.get(0) != null){
			String newFirtLine = plugin.getPocketName() + " ["+itemCount+"]" + pocketLore.get(0);
			pocketLore.set(0, newFirtLine);
		}

		lore.addAll(pocketLore);

		meta.setLore(lore);
		itemWithPocket.setItemMeta(meta);
	}

	protected String getMaterialSettingConfigKey(Material material, String suffix) {
		return ConfigKeyEnum.MATERIAL_SETTINGS.getKey() + "." + material.name() + "." + suffix;
	}

	protected void openInventory(ItemStack clickedItem, Player player, Cancellable event) {
		Pocket pocket = getPocket(clickedItem);
		if (pocket == null) {
			return;
		}

		List<ItemStack> items = getPocketContents(pocket);

		int pocketSizeLimit = plugin.getConfig().getInt(
				getMaterialSettingConfigKey(clickedItem.getType(), ConfigKeyEnum.MATERIAL_SETTING_LIMIT.getKey()));

		plugin.debugInfo("pocketSizeLimit [initial]: " + pocketSizeLimit);
		if (pocketSizeLimit <= 0) {
			pocketSizeLimit = plugin.getConfig().getInt(ConfigKeyEnum.DEFAULT_POCKET_SIZE_LIMIT.getKey(), 6);
			plugin.debugInfo("pocketSizeLimit [secondary]: " + pocketSizeLimit);
		}

		if (items.size() > pocketSizeLimit)
			pocketSizeLimit = items.size();

		plugin.debugInfo("pocketSizeLimit [final]: " + pocketSizeLimit);

		int rows = (pocketSizeLimit / INVENTORY_COLUMNS) + (pocketSizeLimit % INVENTORY_COLUMNS > 0 ? 1 : 0);

		Inventory pocketsInventory = Bukkit.createInventory(null, rows * INVENTORY_COLUMNS, plugin.getPocketName());

		int i = -1;

		for (ItemStack item : items) {
			if (item == null || item.getType().equals(Material.AIR))
				continue;

			i++;
			pocketsInventory.setItem(i, item);
		}

		int availableSlotsOnLastRow = pocketSizeLimit >= INVENTORY_COLUMNS ? pocketSizeLimit % INVENTORY_COLUMNS
				: pocketSizeLimit;

		plugin.debugInfo("availableSlotsOnLastRow: " + availableSlotsOnLastRow);

		if (availableSlotsOnLastRow > 0) {
			// we must add black pane glass slots to the end of the inventory
			// which cannot be moved
			int blackedOutSlotsRequired = INVENTORY_COLUMNS - availableSlotsOnLastRow;

			plugin.debugInfo("blackedOutSlotsRequired: " + blackedOutSlotsRequired);
			for (i = pocketSizeLimit; i < pocketSizeLimit + blackedOutSlotsRequired; i++) {
				ItemStack blackOut = new ItemStack(BLACKOUT_MATERIAL);
				ItemMeta itemMeta = blackOut.getItemMeta();
				itemMeta.setDisplayName(BLACKOUT_TEXT);
				itemMeta.setLore(new ArrayList<>());
				itemMeta.getLore().add(convertToInvisibleString(i + ""));
				blackOut.setItemMeta(itemMeta);
				pocketsInventory.setItem(i, blackOut);
			}
		}

		if (event != null)
			event.setCancelled(true);

		if (openPocketMap == null)
			openPocketMap = new HashMap<>();

		openPocketMap.put(player.getName(), clickedItem);

		PocketDelayOpener pocketDelayOpener = new PocketDelayOpener(plugin, player, pocketsInventory);
		pocketDelayOpener.runTaskLater(plugin, 5L * 1);
	}

	protected String convertToInvisibleString(String s) {
		String hidden = "";
		for (char c : s.toCharArray())
			hidden += ChatColor.COLOR_CHAR + "" + c;
		return hidden;
	}

	protected String convertToVisibleString(String s) {
		if (s != null && !s.isEmpty()) {
			s = s.replace(ChatColor.COLOR_CHAR + "", "");
		}

		return s;
	}

	protected boolean loreLineIsPocket(String loreLine) {
		if (loreLine == null || loreLine.isEmpty())
			return false;

		loreLine = convertToVisibleString(loreLine);

		plugin.debugInfo("Lore line: ");
		plugin.debugInfo(loreLine);
		plugin.debugInfo("-------------------------------");

		return loreLine.contains(POCKETS_KEY);
	}

	protected Pocket getPocket(ItemStack item) {
		return getPocket(item, true);
	}

	protected Pocket getPocket(ItemStack item, boolean hideChildPockets) {

		if (item.getItemMeta() == null || item.getItemMeta().getLore() == null) {
			plugin.debugInfo("No lore");
			return null;
		}

		List<String> pocketLines = item.getItemMeta().getLore().stream().filter(l -> loreLineIsPocket(l))
				.collect(Collectors.toList());

		if (pocketLines == null || pocketLines.isEmpty()) {
			plugin.debugInfo("No pocket");
			return null;
		}

		List<String> pocketRawTexts = pocketLines.stream().map(p -> convertToVisibleString(p))
				.collect(Collectors.toList());

		if (pocketRawTexts == null || pocketRawTexts.isEmpty()) {
			plugin.debugInfo("pocketRawTexts null or empty");
			return null;
		}

		List<String> pocketJsonParts = pocketRawTexts.stream()
				.map(p -> p.substring(p.indexOf(POCKETS_KEY) + POCKETS_KEY.length())).collect(Collectors.toList());

		if (pocketJsonParts == null || pocketJsonParts.isEmpty()) {
			plugin.debugInfo("pocketJsonParts null or empty");
			return null;
		}

		String pocketJson = pocketJsonParts.stream().reduce("", (a, b) -> a + b);

		Pocket pocket = plugin.deserializeJsonSafe(pocketJson, Pocket.class);

		if (pocket != null) {
			if (hideChildPockets && pocket.getContents() != null && !pocket.getContents().isEmpty()) {
				List<ItemStack> items = pocket.getContents().stream().map(c -> getPocketItemsHidden(c))
						.collect(Collectors.toList());
				List<CardboardBox> newContents = items == null ? null
						: items.stream().filter(i -> i != null).map(i -> new CardboardBox(i))
								.collect(Collectors.toList());
				pocket.setContents(newContents);
			}
		} else {
			plugin.warning("There was an unexpectedissue deserialing the pocket.");
			plugin.warning("------");
			plugin.warning("Parts[" + pocketJsonParts.size() + "]: ");
			pocketJsonParts.forEach(p -> plugin.debugWarning("  -PART: " + p));
			plugin.warning("------");
			plugin.warning("------");
			plugin.warning("LORE ITEMS[" + item.getItemMeta().getLore().size() + "]");
			item.getItemMeta().getLore().forEach(l -> plugin.debugWarning(" -Lore: " + convertToVisibleString(l)));
			plugin.warning("------");
		}

		return pocket;
	}

	protected ItemStack getPocketItemsHidden(CardboardBox box) {
		ItemStack item = box != null ? box.unbox() : null;
		if (item != null) {
			Pocket pocket = getPocket(item, false);
			if (pocket != null) {
				List<ItemStack> itemsInPocket = pocket.getContents() == null ? null
						: pocket.getContents().stream().map(c -> c.unbox()).collect(Collectors.toList());
				setPocketJson(item, itemsInPocket);
			}
		}
		return item;
	}

	protected List<ItemStack> getPocketContents(ItemStack item, boolean hideChildPockets) {
		return getPocketContents(getPocket(item, hideChildPockets));
	}

	protected List<ItemStack> getPocketContents(Pocket pocket) {
		return pocket == null || pocket.getContents() == null ? null
				: pocket.getContents().stream().filter(c -> c != null).map(c -> c.unbox()).collect(Collectors.toList());
	}

	protected ItemStack getActivePocketItem(HumanEntity player) {
		if (openPocketMap == null)
			openPocketMap = new HashMap<>();

		return openPocketMap.containsKey(player.getName()) ? openPocketMap.get(player.getName()) : null;
	}

}
