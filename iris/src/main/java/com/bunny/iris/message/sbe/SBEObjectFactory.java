package com.bunny.iris.message.sbe;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import uk.co.real_logic.agrona.concurrent.UnsafeBuffer;

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
public class SBEObjectFactory {
	private int initialCapacity = 100;
	
	private int currentCount;
	private SBEObjectArray[] pool;
	
	private final UnsafeBuffer buffer;
	private final ByteOrder order;
	
	public SBEObjectFactory(UnsafeBuffer buffer, ByteOrder order) {
		this.buffer = buffer;
		this.order = order;
		
		currentCount = 0;
		
		pool = new SBEObjectArray[initialCapacity];
		
		for( int i = 0; i < initialCapacity; i ++ ) {
			pool[i] = new SBEObjectArray(buffer,order);
		}
	}
	
	public SBEObjectArray get() {
		if( currentCount < pool.length ) {
			return pool[currentCount++];
		} else {
			SBEObjectArray[] nPool = new SBEObjectArray[pool.length+initialCapacity];
			System.arraycopy(pool, 0, nPool, 0, pool.length);
			for( int i = pool.length; i < nPool.length; i ++ ) {
				nPool[i] = new SBEObjectArray(buffer, order);
			}
			pool = nPool;
			return get();
		}
	}
	
	/**
	 * Always contain the message object
	 * @return
	 */
	public SBEObjectArray getRoot() {
		return pool[0];
	}
	
	public void returnAll() {
		for( int i = 0; i < currentCount; i ++ ) {
			pool[i].reset();
		}
		currentCount = 0;
	}
}
