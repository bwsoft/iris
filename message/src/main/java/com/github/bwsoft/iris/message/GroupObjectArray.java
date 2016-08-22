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
package com.github.bwsoft.iris.message;

/**
 * A GroupObjectArray is a section in a message that contains several repeated identical structures.
 * Use {@link #getDefinition()} to obtain the definition of each identical structure.
 * The GroupObjectArray is the array of this structure, the size of which can be obtained 
 * via {@link #getNumOfGroups()}. 
 * 
 * GroupObjectArray is also used to add/remove a row to the array. 
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
	 * the existence of the nth object. The behavior is undefined for retrieving a 
	 * GroupObject out of bound. 
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
	 * Add a new row to the end of the existing group list. 
	 * 
	 * @return the newly added GroupObject to set values
	 */
	public GroupObject addGroupObject();

	/**
	 * Add a new row to the specific position of the existing group list. 
	 * 
	 * @param n the nth row to be inserted. It starts with zero and has to be less than the number of rows.
	 * @return the newly added GroupObject to set values
	 */
	public GroupObject addGroupObject(int n);

	/**
	 * Delete a group object at nth position. It starts from zero and has to be less than getNumOfGroups.
	 * 
	 * @param n the row to be deleted
	 */
	public void deleteGroupObject(int n);
}
