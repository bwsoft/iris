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
 * This factory class is to recycle all SBEObject created to reduce the chance of GC collection.
 * It assumes that objects are leased one by one. And all leased objects are returned at the 
 * same time.
 * 
 * This class is not thread safe.
 * 
 * @author yzhou
 *
 */
class SBEObjectFactory {
	private int initialCapacity;
	
	private int currentCount;
	private SBEObjectArray[] pool;
	
	SBEObjectFactory() {
		this.initialCapacity = Integer.parseInt(SBESchemaLoader.properties.getProperty(SBESchemaLoader.OPTIMIZED_NUM_OF_GROUPS));
		
		currentCount = 0;
		
		pool = new SBEObjectArray[initialCapacity];
		
		for( int i = 0; i < initialCapacity; i ++ ) {
			pool[i] = new SBEObjectArray();
		}
	}
	
	SBEObjectArray get() {
		if( currentCount < pool.length ) {
			SBEObjectArray array = pool[currentCount++];
			array.reset();
			return array;
		} else {
			SBEObjectArray[] nPool = new SBEObjectArray[pool.length+initialCapacity];
			System.arraycopy(pool, 0, nPool, 0, pool.length);
			for( int i = pool.length; i < nPool.length; i ++ ) {
				nPool[i] = new SBEObjectArray();
			}
			pool = nPool;
			return get();
		}
	}
	
	/**
	 * Always contain the message object
	 * @return
	 */
	SBEObjectArray getRoot() {
		return pool[0];
	}
	
	void returnAll() {
		currentCount = 0;
	}
}
