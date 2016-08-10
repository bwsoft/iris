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
package com.bwsoft.iris.message.sbe;

import com.bwsoft.iris.message.FieldHeader;
import com.bwsoft.iris.message.FieldType;
import com.bwsoft.iris.message.Group;

class SBEVarLengthField extends SBEField {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2032780172159012674L;
	private final FieldHeader header;
	
	SBEVarLengthField(Group parent, FieldHeader header) {
		super(parent, FieldType.RAW, (short) 1);
		this.header = header;
	}
	
	@Override
	public FieldHeader getHeader() {
		return header;
	}
}
