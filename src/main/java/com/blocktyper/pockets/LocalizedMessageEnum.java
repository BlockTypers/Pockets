package com.blocktyper.pockets;

public enum LocalizedMessageEnum {

	PLACED_A_POCKET_DOWN("pockets-placed-a-pocket-down"),
	SERVER_RESTARTED_WHILE_POCKET_WAS_OPEN("pockets-server-restarted-while-pocket-was-open"),
	PERMISSION_DENIED("pockets-permission-denied"),
	POCKETS_IN_POCKETS_NOT_ALLOWED("pockets-pockets-in-pockets-not-allowed"),
	OBJECT_NOT_COMPATIBLE("pockets-object-not-compatible"),
	YOUR_POCKETS_INVENTORY_NAME("pockets-your-pockets-inventory-name");

	private String key;

	private LocalizedMessageEnum(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
