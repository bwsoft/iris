package com.bunny.iris.message;

public interface FieldValue {
	public short getNodeId();
	public void setNodeId(short id);
	
	public Field getField();
	public void setField(Field field);
	
	public int getSize();
	public void setSize(int size);
	
	public FieldValue getParentNode();
	public void setParentNode(FieldValue parentNode);
	
	public short getNumChildNodes();
	public FieldValue getChildNode(short index);
	public void addChildNode(FieldValue child);
	
	public boolean isGroupHeadNode();
	public void setGroupHeadNode(boolean isNode);
	
	public String formattedString(String prefix);
}
