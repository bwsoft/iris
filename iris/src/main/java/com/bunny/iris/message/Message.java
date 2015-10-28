package com.bunny.iris.message;

public interface Message {
	public FieldValue allocate(boolean isHeaderNode);
	public FieldValue getValueNode(short nodeId);
	
	public int getSize();
	public int getTotalFields();
	
	public FieldOp getFieldOp();
}
