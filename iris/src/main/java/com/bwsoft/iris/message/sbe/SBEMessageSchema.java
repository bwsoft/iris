/*******************************************************************************
 * Copyright 2016 bwsoft and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.bwsoft.iris.message.sbe;

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
