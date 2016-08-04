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
package com.bwsoft.iris.message;

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
	
	/**
	 * Add a group to the end of the existing group list. 
	 * 
	 * @return
	 */
	public GroupObject addGroupObject();
	
	/**
	 * Delete a group object at nth position. It starts from zero and has to be less than getNumOfGroups.
	 * 
	 * @param n
	 */
	public void deleteGroupObject(int n);
}
