package com.bwsoft.iris.util;

import java.nio.ByteBuffer;

import com.bwsoft.iris.message.GroupObject;
import com.bwsoft.iris.message.SBEMessageSchema;
import com.bwsoft.iris.message.sbe.SBEMessage;

public class MessageUtil {

	/**
	 * Copy a message to a byte buffer
	 * 
	 * @param original
	 * @param startOffset
	 * @param nth
	 * @param dest
	 * @param destOffset
	 * @param schema
	 */
	public static void messageCopy(ByteBuffer original, int startOffset, int nth,
			ByteBuffer dest, int destOffset, SBEMessageSchema schema) {
		if( dest.hasArray() ) {
			messageCopy(original, startOffset, nth, dest.array(), destOffset, schema);
		}
		
		for( int i = 0; i < nth; i ++ ) {
			GroupObject msgObj = schema.wrapForRead(original, startOffset);
			startOffset += msgObj.getSize();
			startOffset += ((SBEMessage) msgObj.getDefinition()).getHeader().getHeaderSize();
		}
		
		GroupObject msgObj = schema.wrapForRead(original, startOffset);
		int msgLength = msgObj.getSize() 
				+ ((SBEMessage) msgObj.getDefinition()).getHeader().getHeaderSize();

		original.position(startOffset);
		
		byte[] array = new byte[msgLength];
		original.slice().get(array, 0, msgLength);
		
		dest.position(destOffset);
		dest.slice().put(array,0,msgLength);
	}
	
	/**
	 * Copy a SBE message to a byte array
	 * 
	 * @param original a buffer containing one or more SBE messages
	 * @param startOffset the start offset of the first message
	 * @param nth  nth message in this byte array. Starts from zero for the first message.
	 * @param dest
	 * @param destOffset
	 * @param schema schema to parse this message
	 */
	public static void messageCopy(ByteBuffer original, int startOffset, int nth,
			byte[] dest, int destOffset, SBEMessageSchema schema) {
		for( int i = 0; i < nth; i ++ ) {
			GroupObject msgObj = schema.wrapForRead(original, startOffset);
			startOffset += msgObj.getSize();
			startOffset += ((SBEMessage) msgObj.getDefinition()).getHeader().getHeaderSize();
		}
		
		GroupObject msgObj = schema.wrapForRead(original, startOffset);
		int msgLength = msgObj.getSize() 
				+ ((SBEMessage) msgObj.getDefinition()).getHeader().getHeaderSize();

		original.position(startOffset);
		original.slice().get(dest, destOffset, msgLength);
	}
	
	/**
	 * Copy a SBE message to a byte array
	 * 
	 * @param original a buffer containing one or more SBE messages
	 * @param startOffset the start offset of the first message
	 * @param nth  nth message in this byte array. Starts from zero for the first message.
	 * @param dest
	 * @param destOffset
	 * @param schema schema to parse this message
	 */
	public static void messageCopy(byte[] original, int startOffset, int nth,
			byte[] dest, int destOffset, SBEMessageSchema schema) {
		for( int i = 0; i < nth; i ++ ) {
			GroupObject msgObj = schema.wrapForRead(original, startOffset);
			startOffset += msgObj.getSize();
			startOffset += ((SBEMessage) msgObj.getDefinition()).getHeader().getHeaderSize();
		}

		GroupObject msgObj = schema.wrapForRead(original, startOffset);
		int msgLength = msgObj.getSize() 
				+ ((SBEMessage) msgObj.getDefinition()).getHeader().getHeaderSize();

		System.arraycopy(original, startOffset, dest, destOffset, msgLength);
	}
}
