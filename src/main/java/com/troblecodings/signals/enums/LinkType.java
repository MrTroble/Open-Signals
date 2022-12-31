package com.troblecodings.signals.enums;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.core.interfaces.NamableWrapper;

public enum LinkType implements NamableWrapper {
	SIGNAL("signal"), INPUT("input"), OUTPUT("output");
	
    private static final String LINK_TYPE = "linkType";
	private String name;

	private LinkType(String name) {
		this.name = name;
	}
	
	public void write(NBTWrapper wrapper) {
		wrapper.putString(LINK_TYPE, name);
	}

	@Override
	public String getNameWrapper() {
		return this.name;
	}

	public static LinkType of(String name) {
		for(LinkType type : values())
			if(type.name.equalsIgnoreCase(name))
				return type;
		return null;
	}
	
	public static LinkType of(NBTWrapper wrapper) {
		return of(wrapper.getString(LINK_TYPE));
	}
}
