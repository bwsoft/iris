package com.bwsoft.iris.message.sbe;

import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.bwsoft.iris.message.GroupObject;
import com.bwsoft.iris.message.GroupObjectArray;
import com.bwsoft.iris.message.SBEMessageSchema;
import com.bwsoft.iris.util.MessageUtil;

public class SBEMessageCreationTest {
	// a buffer that is populated with SBE message before all tests. 
	private final static ByteBuffer sbeBuffer = ByteBuffer.allocateDirect(4096);
	private final static int bufferOffset = 0;
	private final static ByteBuffer newSbeBuffer = ByteBuffer.allocateDirect(4096);

	// create SBEMessageSchema based upon the schema.
	private final static SBEMessageSchema factory;
	static {
		SBEMessageSchema schema = null;
		try{
			schema = SBEMessageSchema.createSBESchema("./src/test/resources/example-schema.xml");
		} catch( Exception e ) {
			e.printStackTrace();
		}
		factory = schema;
	};

	@Rule
	public TestRule watcher = new TestWatcher() {
		protected void starting(Description description) {
			System.out.format("\nStarting test: %s\n", description.getMethodName());
		}
	};
	
	/**
	 * Use RL SBE encoder to create a SBE message and store it in sbeBuffer
	 */
	@BeforeClass
	public static void createSBEMessage() {
		SBEMessageTestUtil.createSBEMessageUsingRLEncoder(sbeBuffer, bufferOffset);
	}
	
	@Test
	public void alterExistingSbeMessageCase0() {
		// copy the first sbe message to a new buffer
		MessageUtil.messageCopy(sbeBuffer, bufferOffset, 0, newSbeBuffer, 0, factory);
		
		// wrap the copied message
		GroupObject msgObj = factory.wrapForRead(newSbeBuffer, 0);
		System.out.println(msgObj.toString());
		
		// insert a new row for fuel figure
		int sizeBefore = msgObj.getSize();
		GroupObjectArray fuelFigures = msgObj.getGroupArray(msgObj.getField((short) 9));
		GroupObject newFuelFigure = fuelFigures.addGroupObject();
		System.out.println(msgObj.toString());
		
		// verify the size change is correct
		Assert.assertEquals(sizeBefore+newFuelFigure.getSize(), msgObj.getSize());

		String unparsedMsg = msgObj.toString();
		msgObj = factory.wrapForRead(newSbeBuffer, 0);
		System.out.println(msgObj.toString());
		Assert.assertEquals(unparsedMsg, msgObj.toString());
		
		// add a row to the accleration group of the second row of the performanceFigure group
		GroupObjectArray performanceFigures = msgObj.getGroupArray(msgObj.getField((short) 12)); 
		Assert.assertEquals(2, performanceFigures.getNumOfGroups()); // two rows originally
		
		GroupObject performanceFigure2 = performanceFigures.getGroupObject((short) 1);
		GroupObjectArray accleration = performanceFigure2.getGroupArray(performanceFigure2.getField((short) 14));
		int originalDimmension = accleration.getNumOfGroups();
		Assert.assertEquals(3, originalDimmension);
		
		int originalPerformanceFigureSize = performanceFigure2.getSize();
		sizeBefore = msgObj.getSize();
		GroupObject newAccleration = accleration.addGroupObject();
		System.out.println(msgObj.toString());
		Assert.assertEquals(originalPerformanceFigureSize+newAccleration.getSize(), performanceFigure2.getSize());
		Assert.assertEquals(sizeBefore+newAccleration.getSize(), msgObj.getSize());		

		unparsedMsg = msgObj.toString();
		msgObj = factory.wrapForRead(newSbeBuffer, 0);
		System.out.println(msgObj.toString());
		Assert.assertEquals(unparsedMsg, msgObj.toString());
	}	
	
	/**
	 * Add a group row to a group without any row initially
	 */
	@Test
	public void alterExistingSbeMessageCase1() {
		// copy the second new SBE message to the buffer
		MessageUtil.messageCopy(sbeBuffer, bufferOffset, 1, newSbeBuffer, 0, factory);
		
		// wrap the copied message
		GroupObject msgObj = factory.wrapForRead(newSbeBuffer, 0);
		System.out.println(msgObj.toString());
		int sizeBefore = msgObj.getSize();
		
		// add row
		GroupObjectArray fuelFigures = msgObj.getGroupArray(msgObj.getField((short) 9));
		GroupObject newFuelFigure = fuelFigures.addGroupObject();
		System.out.println(msgObj.toString());
		Assert.assertEquals(sizeBefore+newFuelFigure.getSize(), msgObj.getSize());
		Assert.assertEquals(6, newFuelFigure.getSize());
		
		String unparsedMsg = msgObj.toString();
		msgObj = factory.wrapForRead(newSbeBuffer, 0);
		System.out.println(msgObj.toString());
		Assert.assertEquals(unparsedMsg, msgObj.toString());
		
		// add another row
		newFuelFigure = fuelFigures.addGroupObject();
		System.out.println(msgObj.toString());
		Assert.assertEquals(sizeBefore+2*newFuelFigure.getSize(), msgObj.getSize());
		Assert.assertEquals(6, newFuelFigure.getSize());
		
		// add a row to the accleration group of the second row of the performanceFigure group
		// it is empty initially
		GroupObjectArray performanceFigures = msgObj.getGroupArray(msgObj.getField((short) 12)); 
		Assert.assertEquals(2, performanceFigures.getNumOfGroups()); // two rows originally
		
		GroupObject performanceFigure2 = performanceFigures.getGroupObject((short) 1);
		GroupObjectArray accleration = performanceFigure2.getGroupArray(performanceFigure2.getField((short) 14));
		int originalDimmension = accleration.getNumOfGroups();
		Assert.assertEquals(0, originalDimmension);
		sizeBefore = msgObj.getSize();
		int performanceFigure2Size = performanceFigure2.getSize();
		GroupObject newAccleration = accleration.addGroupObject();
		System.out.println(msgObj.toString());
		Assert.assertEquals(sizeBefore+newAccleration.getSize(), msgObj.getSize());
		Assert.assertEquals(performanceFigure2Size+newAccleration.getSize(), performanceFigure2.getSize());
		unparsedMsg = msgObj.toString();
		msgObj = factory.wrapForRead(newSbeBuffer, 0);
		System.out.println(msgObj.toString());
		Assert.assertEquals(unparsedMsg, msgObj.toString());
	}	
	
	/**
	 * Add a row to a group that contains another group
	 */
	@Test
	public void alterExistingSbeMessageCase2() {
		// copy the second new SBE message to the buffer
		MessageUtil.messageCopy(sbeBuffer, bufferOffset, 1, newSbeBuffer, 0, factory);

		// wrap the copied message
		GroupObject msgObj = factory.wrapForRead(newSbeBuffer, 0);
		System.out.println(msgObj.toString());
		int sizeBefore = msgObj.getSize();
		
		// add a row to the performance figures
		GroupObjectArray performanceFigures = msgObj.getGroupArray(msgObj.getField((short) 12)); 
		Assert.assertEquals(2, performanceFigures.getNumOfGroups()); // two rows originally
		GroupObject newPerformanceFigure = performanceFigures.addGroupObject();
		System.out.println(msgObj.toString());
		Assert.assertEquals(sizeBefore+newPerformanceFigure.getSize(), msgObj.getSize());
		
		GroupObjectArray acclerations = newPerformanceFigure.getGroupArray(newPerformanceFigure.getField((short) 14));
		GroupObject accleration = acclerations.addGroupObject();
		System.out.println(msgObj.toString());
		String originalMsg = msgObj.toString();
		Assert.assertEquals(originalMsg, factory.wrapForRead(newSbeBuffer, 0).toString());
	}	
	
	@Test
	public void deleteExistingSbeGroupCase0() {
		// copy the second new SBE message to the buffer
		MessageUtil.messageCopy(sbeBuffer, bufferOffset, 0, newSbeBuffer, 0, factory);

		// wrap the copied message
		GroupObject msgObj = factory.wrapForRead(newSbeBuffer, 0);
		System.out.println(msgObj.toString());
		int sizeBefore = msgObj.getSize();
		
		GroupObjectArray fuelFigures = msgObj.getGroupArray(msgObj.getField((short) 9));
		Assert.assertEquals(3, fuelFigures.getNumOfGroups());
		fuelFigures.deleteGroupObject(1);
		System.out.println(msgObj.toString());
		String beforeWrap = msgObj.toString();
		msgObj = factory.wrapForRead(newSbeBuffer, 0);
		System.out.println(msgObj.toString());
		Assert.assertEquals(beforeWrap, msgObj.toString());
		Assert.assertEquals(sizeBefore-6, msgObj.getSize());
		
		// delete all
		fuelFigures.deleteGroupObject(1);
		fuelFigures.deleteGroupObject(0);
		System.out.println(msgObj.toString());
		
		// delete the nested repeating group
		GroupObjectArray performanceFigures = msgObj.getGroupArray(msgObj.getField((short) 12)); 
		Assert.assertEquals(2, performanceFigures.getNumOfGroups()); // two rows originally
		GroupObject performanceFigure = performanceFigures.getGroupObject(0);
		GroupObjectArray acclerations = performanceFigure.getGroupArray(performanceFigure.getField((short) 14));
		acclerations.deleteGroupObject(1);
		System.out.println(msgObj.toString());
		GroupObject accleration = acclerations.getGroupObject(1);
		Assert.assertEquals(100, accleration.getNumber(accleration.getField((short) 15)).intValue());
		Assert.assertEquals(12.2, accleration.getNumber(accleration.getField((short) 16)).floatValue(), 0.01);
		acclerations.deleteGroupObject(0);
		acclerations.deleteGroupObject(0);
		System.out.println(msgObj.toString());
		beforeWrap = msgObj.toString();
		msgObj = factory.wrapForRead(newSbeBuffer, 0);
		Assert.assertEquals(beforeWrap, msgObj.toString());
		
		// delete the performance figure 
		performanceFigures.deleteGroupObject(0);
		System.out.println(msgObj.toString());
		
		// add a new performance figure
		performanceFigures.addGroupObject();
		System.out.println(msgObj.toString());
		beforeWrap = msgObj.toString();
		msgObj = factory.wrapForRead(newSbeBuffer, 0);
		Assert.assertEquals(beforeWrap, msgObj.toString());
	}
}
