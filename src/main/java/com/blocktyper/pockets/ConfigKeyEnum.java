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
	DEFAULT_ALLOW_POCKET_IN_POCKET("default-allow-pocket-in-pocket"),
	OPEN_POCKET_ACTION("open-pocket-action"),
	REQUIRE_PERMISSIONS_FOR_GENERAL_USE("require-permissions-general-pocket-use"),
	GENERAL_USE_PERMISSIONS("permissions-for-general-pocket-use"),
	REQUIRE_PERMISSIONS_FOR_POCKET_IN_POCKET_USE("require-permissions-pocket-in-pocket-use"),
	POCKET_IN_POCKET_USE_USE_PERMISSIONS("permissions-for-pocket-in-pocket-use"),
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
