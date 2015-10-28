package com.bunny.iris.message.sbe;

import com.bunny.iris.message.Field;
import com.bunny.iris.message.FieldType;
import com.bunny.iris.message.FieldValue;

public class SBEValueNode implements FieldValue {

	private Field definition;

	private short arrayId;
	private short parentArrayId;
	private short claimedChildren;
	private short[] childNodes;
	
	private int offset;
	private int size;
	
	private boolean isHeaderNode = false;
	private int blockLength;
	
	public SBEValueNode() {
		claimedChildren = 0;
		childNodes = new short[2];
		childNodes[0] = childNodes[1] = 0;
	}
	
	protected void reset() {
		claimedChildren = 0;
		childNodes[0] = childNodes[1] = 0;		
	}
	
	@Override
	public short getNodeId() {
		return arrayId;
	}

	@Override
	public void setNodeId(short id) {
		this.arrayId = id;
	}

	@Override
	public Field getField() {
		return definition;
	}

	@Override
	public void setField(Field field) {
		this.definition = field;
	}

	@Override
	public int getSize() {
		return this.size;
	}

	@Override
	public void setSize(int size) {
		this.size = size;
	}

	@Override
	public FieldValue getParentNode() {
		SBEField field = (SBEField) getField();
		return field.getMessage().getValueNode(parentArrayId);
	}

	@Override
	public void setParentNode(FieldValue parentNode) {
		this.parentArrayId = parentNode.getNodeId();
	}

	@Override
	public short getNumChildNodes() {
		return (short) (childNodes[1]-childNodes[0]);
	}

	@Override
	public FieldValue getChildNode(short index) {
		AbstractSBECompositeField field = (AbstractSBECompositeField) getField();
		short childNodeIdx = field.getChildNodeId((short) (index+childNodes[0]));
		return field.getMessage().getValueNode(childNodeIdx);
	}

	protected void claimChildren(short num) {
		claimedChildren = num;
		childNodes[0] = childNodes[1] = ((AbstractSBECompositeField) getField()).claimChildren(num);
	}
	
	@Override
	public void addChildNode(FieldValue child) {
		if( claimedChildren > 0 ) {
			childNodes[1] = (short) (((AbstractSBECompositeField) getField()).updateChildNode(childNodes[1], child) + 1);
		} else {
			short numChildren = ((AbstractSBECompositeField) getField()).addChildNode(child);
			if( 0 == (childNodes[1]-childNodes[0]) ) {
				childNodes[0] = (short) (numChildren - 1);
			}
			childNodes[1] = numChildren;
		}
	}

	@Override
	public boolean isGroupHeadNode() {
		return isHeaderNode;
	}

	@Override
	public void setGroupHeadNode(boolean isNode) {
		this.isHeaderNode = isNode;
	}
	
	public int getBlockLength() {
		return blockLength;
	}
	
	public void setBlockLength(int length) {
		this.blockLength = length;
	}
	
	public int getOffset() {
		return offset;
	}
	
	public void setOffset(int offset) {
		this.offset = offset;
	}

	@Override
	public String formattedString(String prefix) {
		StringBuilder sb = new StringBuilder();
		sb.append(prefix).append(getField().getName()).append("={")
			.append("id=").append(getField().getID())
			.append(",parentId=").append(getField().getParent().getID())
			.append(",nodeId=").append(getNodeId())
			.append(",parentNodeId=").append(getParentNode().getNodeId())
			.append(",numChildren=").append(getNumChildNodes())
			.append(",offset=").append(getOffset())
			.append(",totalSize=").append(getSize());
		if( getField().getType() != FieldType.GROUP || isHeaderNode ) {
			sb.append(",totalOccurrence=").append(getField().getTotalOccurrence())
				.append(",blockLength=").append(getBlockLength());
		}
		sb.append("}\n");
		
		if( getField().getType() != FieldType.GROUP ) {
			for( short i = 0; i < getField().getArraySize(); i ++ ) {
				SBEField field = (SBEField) getField();
				sb.append(prefix+"  value=").append(field.getMessage().getFieldOp().bind(this).getString(i)).append("\n");
			}
		}
		
		for( short i = 0; i < getNumChildNodes(); i ++ ) {
			FieldValue value = getChildNode(i);
			sb.append(value.formattedString("  "+prefix));
		}
		
		return sb.toString();
	}
}
