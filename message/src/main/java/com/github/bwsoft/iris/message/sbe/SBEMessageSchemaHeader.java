/*******************************************************************************
 * Copyright 2016 bwsoft and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *******************************************************************************/
package com.github.bwsoft.iris.message.sbe;

import java.nio.ByteOrder;

/**
 * Header for the SBE message schema
 * 
 * @author yzhou
 *
 */
public class SBEMessageSchemaHeader {

	private String packageName;
	private final int id;
	private final int version;
	private String semanticVersion;
	private ByteOrder order;
	
	SBEMessageSchemaHeader(String packageName, int id, int version, String semanticVersion, String order) {
		this.packageName = packageName;
		this.id = id;
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
	
	/**
	 * The package name of this schema.
	 * 
	 * @return package name
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * The schema ID. This schema can only process messages whose schema ID equals to this 
	 * value.
	 * 
	 * @return the schema ID
	 */
	public int getId() {
		return id;
	}

	/**
	 * Get the version of the schema.
	 * 
	 * @return the version
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * Get the semantic version
	 * 
	 * @return the semantic version
	 */
	public String getSemanticVersion() {
		return semanticVersion;
	}

	/**
	 * Get the ByteOrderfor fields in the schema.
	 *  
	 * @return the ByteOrder
	 */
	public ByteOrder getOrder() {
		return order;
	}
}
