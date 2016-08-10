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
package com.bwsoft.iris.message;

import java.nio.ByteBuffer;

/**
 * A message is a special Group without a parent.  It contains additional methods to wrap/create a messaqe buffer 
 * to return a GroupObject. The returned GroupObject can be used to get/set values of 
 * fields in this message.
 * 
 * @see com.bwsoft.iris.message.GroupObject
 * 
 * @author yzhou
 *
 */
public interface Message extends Group {
	/**
	 * Wrap a buffer with SBE message and return a GroupObject to get or modify the 
	 * values of fields in the message. Typically this is not directly called. Instead 
	 * one should use a factory method such as {@link SBEMessageSchema#wrapSbeBuffer(ByteBuffer, int)} to wrap 
	 * a buffer containing an unknown message.   
	 * 
	 * @param buffer a buffer containing the SBE message
	 * @param offset the starting position of the SBE message in the buffer
	 * @return a GroupObject or null if this buffer does not contain the current SBEMessage
	 */
	public GroupObject wrapSbeBuffer(ByteBuffer buffer, int offset);
	/**
	 * Wrap a buffer with SBE message and return a GroupObject to get or modify the 
	 * values of fields in the message. Typically this is not directly called. Instead 
	 * one should use a factory method such as {@link SBEMessageSchema#wrapSbeBuffer(ByteBuffer, int, int)} to wrap 
	 * a buffer containing an unknown message.   
	 * 
	 * @param buffer a buffer containing the SBE message
	 * @param offset the starting position of the SBE message in the buffer
	 * @param length buffer boundary
	 * @return a GroupObject or null if this buffer does not contain the current SBEMessage
	 */
	public GroupObject wrapSbeBuffer(ByteBuffer buffer, int offset, int length);
	/**
	 * Wrap a buffer with SBE message and return a GroupObject to get or modify the 
	 * values of fields in the message. Typically this is not directly called. Instead 
	 * one should use a factory method such as {@link SBEMessageSchema#wrapSbeBuffer(byte[], int)} to wrap 
	 * a buffer containing an unknown message.   
	 * 
	 * @param buffer a buffer containing the SBE message
	 * @param offset the starting position of the SBE message in the buffer
	 * @return a GroupObject or null if this buffer does not contain the current SBEMessage
	 */
	public GroupObject wrapSbeBuffer(byte[] buffer, int offset);
	/**
	 * Wrap a buffer with SBE message and return a GroupObject to get or modify the 
	 * values of fields in the message. Typically this is not directly called. Instead 
	 * one should use a factory method such as {@link SBEMessageSchema#wrapSbeBuffer(byte[], int, int)} to wrap 
	 * a buffer containing an unknown message.   
	 * 
	 * @param buffer a buffer containing the SBE message
	 * @param offset the starting position of the SBE message in the buffer
	 * @param length buffer boundary
	 * @return a GroupObject or null if this buffer does not contain the current SBEMessage
	 */
	public GroupObject wrapSbeBuffer(byte[] buffer, int offset, int length);
	/**
	 * Create a SBE buffer using the provided buffer. It returns a GroupObject for setting the 
	 * field values. Typically this is not directly called. Instead 
	 * one should use a factory method such as {@link SBEMessageSchema#createSbeBuffer(int, ByteBuffer, int)} to wrap 
	 * a buffer. 
	 * 
	 * @param buffer a buffer to hold the SBE message
	 * @param offset the starting position to create the SBE message in the buffer
	 * @return a GroupObject to set fields in the message
	 */
	public GroupObject createSbeBuffer(ByteBuffer buffer, int offset);
	/**
	 * Create a SBE buffer using the provided buffer. It returns a GroupObject for setting the 
	 * field values. Typically this is not directly called. Instead 
	 * one should use a factory method such as {@link SBEMessageSchema#createSbeBuffer(int, ByteBuffer, int)} to wrap 
	 * a buffer. 
	 * 
	 * @param buffer a buffer to hold the SBE message
	 * @param offset the starting position to create the SBE message in the buffer
	 * @return a GroupObject to set fields in the message
	 */
	public GroupObject createSbeBuffer(byte[] buffer, int offset);
}
