package com.bunny.iris.message;

/**
 * A group object array is a continuous section in the message. The section contains several 
 * repeated structures, each of which is structured based upon a group definition. getNumOfGroups
 * returns the number of repeated structures.
 * 
 *  A GroupObjectArray for a message will only contain one repeat of the structure. 
 *  Hence its getNumOfGroups returns 1. 
 *  
 * @author yzhou
 *
 */
public interface GroupObjectArray {
	/**
	 * @return the Field definition.
	 */
	public Field getDefinition(); 
	/**
	 * Get nth group object from the array. There is no obligation for the code to verify
	 * the existence of the nth object. And hence an ArrayOutofBound exception may throw 
	 * if n is greater than the available objects.
	 * 
	 * @param n index of a group object
	 * @return nth group object
	 */
	public GroupObject getGroupObject(int n); 

	/**
	 * @return total number of group objects in this array.
	 */
	public short getNumOfGroups(); 
}
