package com.bunny.iris.message.sbe;

import java.util.ArrayList;
import java.util.List;

import com.bunny.iris.message.Field;
import com.bunny.iris.message.FieldType;
import com.bunny.iris.message.FieldValue;
import com.bunny.iris.message.Message;

public abstract class AbstractSBECompositeField extends SBEField {
	// child field definition
	private List<SBEField> children = new ArrayList<>();
	
	// child value node
	private short numChildren;
	private short[] childValueNodeIds;
	
	public AbstractSBECompositeField(Message message) {
		super(message);
		numChildren = 0;
		childValueNodeIds = new short[128];
	}
	
	protected void reset() {
		super.reset();
		numChildren = 0;
	}
	
	/**
	 * Claim a section of arrays to contain children value node Ids
	 * @param num number of array elements to be claimed.
	 * @return array ID of the first claim array element.
	 */
	protected short claimChildren(short num) {
		short start = numChildren;
		numChildren += num;
		return start;
	}
	
	protected short updateChildNode(short index, FieldValue child) {
		short nodeId = child.getNodeId();
		childValueNodeIds[index] = nodeId;
		return index;
	}
	
	protected short addChildNode(FieldValue child) {
		short nodeId = child.getNodeId();
		childValueNodeIds[numChildren] = nodeId;
		numChildren ++;
		return numChildren;
	}
	
	protected short getChildNodeId(short index) {
		return childValueNodeIds[index];
	}

	@Override
	public SBEField getChild(short id) {
		return children.get(id);
	};

	protected SBEField addChild(Field child) {
		this.children.add((SBEField) child);
		return this;
	}
	
	@Override
	public abstract SBEField addChild(FieldType type);

	protected final List<SBEField> getChildren() {
		return children;
	}
}
