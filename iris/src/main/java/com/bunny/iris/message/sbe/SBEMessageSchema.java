package com.bunny.iris.message.sbe;

import java.nio.ByteOrder;

public class SBEMessageSchema {

	private String packageName;
	private int version;
	private String semanticVersion;
	private ByteOrder order;
	
	public SBEMessageSchema(String packageName, int version, String semanticVersion, String order) {
		this.packageName = packageName;
		this.version = version;
		this.semanticVersion = semanticVersion;
		switch(order.toLowerCase()) {
		case "littleendian":
			this.order = ByteOrder.LITTLE_ENDIAN;
			break;
		case "bigendian":
			this.order = ByteOrder.BIG_ENDIAN;
			break;
		default:
			throw new IllegalArgumentException("unrecognized byte order: "+order);
		}
	}
	
	public ByteOrder getOrder() {
		return order;
	}
}
