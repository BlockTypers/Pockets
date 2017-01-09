package com.blocktyper.pockets;

public enum LocalizedMessageEnum {

	PLACED_A_POCKET_DOWN("pockets.placed.a.pocket.down"),
	SERVER_RESTARTED_WHILE_POCKET_WAS_OPEN("pockets.server.restarted.while.pocket.was.open"),
	PERMISSION_DENIED("pockets.permission.denied"),
	POCKETS_IN_POCKETS_NOT_ALLOWED("pockets.pockets.in.pockets.not.allowed"),
	FOREIGN_INVIS_NOT_ALLOWED("pockets.foreign.invis.not.allowed");

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
