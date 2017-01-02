package com.blocktyper.example;

import java.util.ResourceBundle;

import com.blocktyper.plugin.BlockTyperPlugin;

public class ExamplePlugin extends BlockTyperPlugin {

	

	public static final String RESOURCE_NAME = "com.blocktyper.magicdoors.resources.ExampleMessages";
	
	public ExamplePlugin(){
		super();
	}
	

	public void onEnable() {
		super.onEnable();
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
