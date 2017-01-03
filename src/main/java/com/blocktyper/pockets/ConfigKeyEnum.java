package com.blocktyper.pockets;

public enum ConfigKeyEnum {
	
	MATERIALS_WHICH_CAN_HAVE_POCKETS("materials-which-can-have-pockets"),
	MATERIAL_SETTINGS("material-settings"),
	MATERIAL_SETTING_LIMIT("limit"),
	MATERIAL_SETTING_ALLOW_POCKET_IN_POCKET("allow-pocket-in-pocket"),
	POCKET_MATERIAL("recipe.pocket.output"),
	POCKET_NAME("recipe.pocket.name"),
	OPEN_POCKET_CLICK_TYPE("open-pocket-click-type"),
	DEFAULT_POCKET_SIZE_LIMIT("default-pocket-size-limit"),
	DEFAULT_ALLOW_POCKET_IN_POCKET("default-allow-pocket-in-pocket")
	;



	private String key;

	private ConfigKeyEnum(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
}