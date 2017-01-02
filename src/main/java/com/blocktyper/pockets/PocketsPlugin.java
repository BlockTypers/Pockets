package com.blocktyper.pockets;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.blocktyper.plugin.BlockTyperPlugin;
import com.blocktyper.serialization.CardboardBox;
import com.google.gson.Gson;

public class PocketsPlugin extends BlockTyperPlugin {

	public static final String RESOURCE_NAME = "com.blocktyper.pockets.resources.PocketsMessages";

	public static String POCKETS_KEY = "#PKT";
	public static int LORE_LINE_LENGTH_LIMIT = 500;

	protected static Gson JSON_HELPER = new Gson();

	public PocketsPlugin() {
		super();
	}

	public void onEnable() {
		super.onEnable();
		new PocketsCommand(this);
		new InventoryClickListener(this);
	}

	public String convertToInvisibleString(String s) {
		String hidden = "";
		for (char c : s.toCharArray())
			hidden += ChatColor.COLOR_CHAR + "" + c;
		return hidden;
	}

	public String convertToVisibleString(String s) {
		if (s != null && !s.isEmpty()) {
			s = s.replace(ChatColor.COLOR_CHAR + "", "");
		}

		return s;
	}

	public boolean loreLineIsPocket(String loreLine) {
		if (loreLine == null || loreLine.isEmpty())
			return false;

		loreLine = convertToVisibleString(loreLine);
		
		debugInfo("Lore line: " + loreLine);

		return loreLine.contains(POCKETS_KEY);
	}

	public Pocket getPocket(ItemStack item) {
		return getPocket(item, true);
	}
	public Pocket getPocket(ItemStack item, boolean hideChildPockets) {

		if (item.getItemMeta() == null || item.getItemMeta().getLore() == null) {
			debugInfo("No lore");
			return null;
		}

		List<String> pocketLines = item.getItemMeta().getLore().stream().filter(l -> loreLineIsPocket(l))
				.collect(Collectors.toList());

		if (pocketLines == null || pocketLines.isEmpty()) {
			debugInfo("No pocket");
			return null;
		}

		List<String> pocketRawTexts = pocketLines.stream().map(p -> convertToVisibleString(p))
				.collect(Collectors.toList());

		if (pocketRawTexts == null || pocketRawTexts.isEmpty()) {
			debugInfo("pocketRawTexts null or empty");
			return null;
		}

		List<String> pocketJsonParts = null;// pocketRawText.substring(pocketRawText.indexOf(POCKETS_KEY)
											// + POCKETS_KEY.length());

		pocketJsonParts = pocketRawTexts.stream().map(p -> p.substring(p.indexOf(POCKETS_KEY) + POCKETS_KEY.length()))
				.collect(Collectors.toList());

		if (pocketJsonParts == null || pocketJsonParts.isEmpty()) {
			debugInfo("pocketJsonParts null or empty");
			return null;
		}

		String pocketJson = pocketJsonParts.stream().reduce("", String::concat);

		Pocket pocket = deserializeJsonSafe(pocketJson, Pocket.class);
		
		if(hideChildPockets && pocket.getContents() != null && !pocket.getContents().isEmpty()){
			List<ItemStack> items = pocket.getContents().stream().map(c -> getPocketItemsHidden(c)).collect(Collectors.toList());
			List<CardboardBox> newContents = items == null ? null : items.stream().filter(i -> i != null).map(i -> new CardboardBox(i)).collect(Collectors.toList());
			pocket.setContents(newContents);
		}

		return pocket;
	}
	
	public ItemStack getPocketItemsHidden(CardboardBox box){
		ItemStack item = box != null ? box.unbox() : null;
		if(item != null){
			Pocket pocket = getPocket(item, false);
			if(pocket != null){
				List<ItemStack> itemsInPocket = pocket.getContents() == null ? null : pocket.getContents().stream().map(c -> c.unbox()).collect(Collectors.toList());
				setPocketJson(item, itemsInPocket);
			}
		}
		return item;
	}

	public List<ItemStack> getPocketContents(ItemStack item, boolean hideChildPockets) {
		return getPocketContents(getPocket(item, hideChildPockets));
	}

	public List<ItemStack> getPocketContents(Pocket pocket) {
		return pocket == null || pocket.getContents() == null ? null
				: pocket.getContents().stream().filter(c -> c != null).map(c -> c.unbox()).collect(Collectors.toList());
	}

	protected void setPocketJson(ItemStack itemWithPocket, List<ItemStack> itemsInPocket) {

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

		String pocketsJson = JSON_HELPER.toJson(pocket);

		List<String> pocketsTextLines = new ArrayList<>();

		int i = 0;
		while (true) {
			i++;
			boolean isBreak = pocketsJson.length() <= LORE_LINE_LENGTH_LIMIT;

			int endIndex = !isBreak ? LORE_LINE_LENGTH_LIMIT : pocketsJson.length();

			pocketsTextLines.add(i + POCKETS_KEY + pocketsJson.substring(0, endIndex));

			if (isBreak)
				break;

			pocketsJson = pocketsJson.substring(endIndex);
		}

		ItemMeta meta = itemWithPocket.getItemMeta();

		List<String> lore = null;

		if (meta.getLore() != null) {
			lore = meta.getLore().stream().filter(l -> !loreLineIsPocket(l)).collect(Collectors.toList());
		}

		if (lore == null)
			lore = new ArrayList<>();

		lore.addAll(pocketsTextLines.stream().map(l -> convertToInvisibleString(l)).collect(Collectors.toList()));

		meta.setLore(lore);
		itemWithPocket.setItemMeta(meta);
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
