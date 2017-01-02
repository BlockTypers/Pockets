package com.blocktyper.pockets;

import java.util.ArrayList;
import java.util.List;

import com.blocktyper.serialization.CardboardBox;

public class Pocket {
	private List<CardboardBox> contents;
	
	public Pocket(){
		contents = new ArrayList<>();
	}

	public List<CardboardBox> getContents() {
		return contents;
	}

	public void setContents(List<CardboardBox> contents) {
		this.contents = contents;
	}

	
	
	
}
