package com.github.bwsoft.iris.message.sbe.conformance;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;

import com.github.bwsoft.iris.message.Group;
import com.github.bwsoft.iris.message.GroupObject;
import com.github.bwsoft.iris.message.GroupObjectArray;
import com.github.bwsoft.iris.message.SBEMessageSchema;
import com.github.bwsoft.iris.util.MessageUtil;

import io.fixprotocol.sbe.conformance.MessageValues;
import io.fixprotocol.sbe.conformance.Responder;
import io.fixprotocol.sbe.conformance.TestException;
import io.fixprotocol.sbe.conformance.json.JsonMessageSource;
import io.fixprotocol.sbe.conformance.json.JsonMessageSource.Message;

public class IrisUnderTest implements Responder {
	  private final String[] args;
	  private int testNumber;

	  public IrisUnderTest(String[] args) {
	    this.args = args;
	  }

	  /**
	   * Invokes the Iris implementation of SBE to produce test results
	   * 
	   * @param args files names
	   *        <ol>
	   *        <li>File name of test plan</li>
	   *        <li>File name of injected message file to read</li>
	   *        <li>File name of message file to produce</li>
	   *        </ol>
	   * @throws IOException if an IO error occurs
	   * @throws TestException if one or more encoding or decoding errors occur
	   */
	  public static void main(String[] args) throws IOException, TestException {
	    if (args.length < 3) {
	      usage();
	      System.exit(1);
	    }
	    IrisUnderTest tester = new IrisUnderTest(args);
	    tester.respondAll();
	  }

	  private void respondAll()
	      throws IllegalArgumentException, IOException, TestException, FileNotFoundException {
	    final ClassLoader classLoader = getClass().getClassLoader();
	    final File in = new File(args[1]);
	    final File out = new File(args[2]);
	    try (final InputStream inputStream = new FileInputStream(in)) {
	      try (final InputStream planInputStream = classLoader.getResourceAsStream(args[0])) {
	        final JsonMessageSource jsonMessageSource = new JsonMessageSource(planInputStream);
	        if (!jsonMessageSource.getTestVersion().equals("2016.1")) {
	          throw new IllegalArgumentException("Unexpected test version");
	        }
	        this.testNumber = jsonMessageSource.getTestNumber();

	        try (final FileOutputStream outputStream = new FileOutputStream(out)) {
	          for (int index = 0; index < jsonMessageSource.getResponseMessageCount(); index++) {
	            final Message message = jsonMessageSource.getResponseMessage(index);
	            respond(inputStream, message, outputStream);
	          }
	        }
	      }
	    }
	  }

	  public static void usage() {
	    System.out.println(
	        "Usage: io.fixprotocol.sbe.conformance.rlimpl.RLUnderTest <input-sbe-file> <input-test-file> <output-sbe-file>");
	  }

	  @Override
	  public void respond(InputStream inputStream, MessageValues values, OutputStream outputStream)
	      throws IOException, TestException {
	    switch (testNumber) {
	    case 1:
	    	doTest1(inputStream, values, outputStream);
	    	break;
	    case 2:
	    	doTest2(inputStream, values, outputStream);
	    	break;
	    case 3:
	    	doTest3(inputStream, values, outputStream);
	    	break;        
	      default:
	        throw new IllegalArgumentException("Unexpected test number " + testNumber);
	    }
	  }

	  private void doTest1(InputStream in, MessageValues values, OutputStream outFile)
	      throws IOException, TestException {
	    TestException testException = new TestException();
	    int inOffset = 0;
	    byte[] inBytes = new byte[4096];
	    in.read(inBytes, inOffset, inBytes.length);
	    ByteBuffer buffer = ByteBuffer.wrap(inBytes, inOffset, inBytes.length);
	    SBEMessageSchema schema1 = null;
		try {
			schema1 = SBEMessageSchema.createSBESchema("schema1.xml");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		GroupObject newOrder = schema1.wrapSbeBuffer(buffer, inOffset);
	    int templateId = newOrder.getDefinition().getID();
	    if (templateId != io.fixprotocol.sbe.conformance.schema1.NewOrderSingleDecoder.TEMPLATE_ID) {
	      testException.addDetail("Unexpected message type",
	          Integer
	              .toString(io.fixprotocol.sbe.conformance.schema1.NewOrderSingleDecoder.TEMPLATE_ID),
	          Integer.toString(templateId));
	      throw testException;
	    }

	    int outOffset = 0;
	    ByteBuffer outBuffer = ByteBuffer.allocate(4096);
	    GroupObject execution = schema1.createSbeBuffer(98, outBuffer, outOffset);
	    byte[] value = values.getString("37").getBytes();
	    execution.setBytes(execution.getField("OrderID"),value,0,value.length);
	    value = values.getString("17").getBytes();
	    execution.setBytes(execution.getField("ExecID"),value,0,value.length);
	    execution.setByte(execution.getField("ExecType"), values.getChar("150", Byte.MAX_VALUE));
	    execution.setByte(execution.getField("OrdStatus"), values.getChar("39", Byte.MAX_VALUE));
	    value = newOrder.getString(newOrder.getField("Symbol")).getBytes();
	    execution.setBytes(execution.getField("Symbol"),value,0,value.length);
	    Group monthYear = (Group) execution.getField("MaturityMonthYear");
	    execution.setByte(monthYear.getField("month"), (byte) 255);
	    execution.setChar(execution.getField("Side"), newOrder.getChar(newOrder.getField("Side")));
	    Group leavesQty = (Group) execution.getField("LeavesQty");
	    execution.setNumber(leavesQty.getField("mantissa"), values.getDecimal("151", BigDecimal.valueOf(Integer.MAX_VALUE)));
	    Group cumQty = (Group) execution.getField("CumQty");
	    execution.setNumber(cumQty.getField("mantissa"), values.getDecimal("14", BigDecimal.valueOf(Integer.MAX_VALUE)));
	    execution.setNumber(execution.getField("TradeDate"), values.getInt("75", Short.MAX_VALUE));

	    GroupObjectArray fillsGrp = execution.getGroupArray(execution.getField("FillsGrp"));
	    int fillsGrpCount = values.getGroupCount("FillsGrp");
	    for( int i = 0; i < fillsGrpCount; i ++ ) {
	    	GroupObject aFillGrp = fillsGrp.addGroupObject();
		    MessageValues fillGrpValues = values.getGroup("FillsGrp", i);
	    	Group fillPx = (Group) aFillGrp.getField("FillPx");
	    	aFillGrp.setNumber(fillPx.getField("mantissa"), fillGrpValues.getDecimal("1364", BigDecimal.valueOf(Long.MAX_VALUE)).movePointRight(3));
	    	Group fillQty = (Group) aFillGrp.getField("FillQty");
	    	aFillGrp.setNumber(fillQty.getField("mantissa"), fillGrpValues.getDecimal("1365", BigDecimal.valueOf(Integer.MAX_VALUE)));
	    }

	    System.out.println(MessageUtil.toJsonString(execution));
	    outFile.write(outBuffer.array(), 0, 
	    		outOffset + execution.getSize()+execution.getDefinition().getHeader().getSize());
	  }

	  private void doTest2(InputStream in, MessageValues values, OutputStream outFile)
	      throws IOException, TestException {
		  
	    TestException testException = new TestException();

	    int inOffset = 0;
	    byte[] inBytes = new byte[4096];
	    in.read(inBytes, inOffset, inBytes.length);
	    ByteBuffer inBuffer = ByteBuffer.wrap(inBytes, inOffset, inBytes.length);
	    
	    SBEMessageSchema schema2 =  null;
	    try {
			schema2 = SBEMessageSchema.createSBESchema("schema2.xml");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    GroupObject newOrder = schema2.wrapSbeBuffer(inBuffer, inOffset);
	    if (99 != newOrder.getDefinition().getID() ) {
	      testException.addDetail("Unexpected message type",
	          Integer.toString(99),
	          Integer.toString(newOrder.getDefinition().getID()));
	      throw testException;
	    }

	    try {
			schema2 = SBEMessageSchema.createSBESchema("schema1.xml");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	    
	    int outOffset = 0;
	    ByteBuffer outBuffer = ByteBuffer.allocate(4096);
	    GroupObject execution = schema2.createSbeBuffer(98, outBuffer, outOffset);
	    byte[] value = values.getString("37").getBytes();
	    execution.setBytes(execution.getField("OrderID"), value, 0, value.length);
	    value = values.getString("17").getBytes();
	    execution.setBytes(execution.getField("ExecID"), value, 0, value.length);
	    execution.setByte(execution.getField("ExecType"), values.getChar("150", Byte.MAX_VALUE));
	    execution.setByte(execution.getField("OrdStatus"), values.getChar("39", Byte.MAX_VALUE));
	    value = newOrder.getString(newOrder.getField("Symbol")).getBytes();
	    execution.setBytes(execution.getField("Symbol"), value, 0, value.length);
	    Group monthYear = (Group) execution.getField("MaturityMonthYear");
	    execution.setByte(monthYear.getField("month"), (byte) 255);
	    execution.setChar(execution.getField("Side"), newOrder.getChar(newOrder.getField("Side")));
	    Group leavesQty = (Group) execution.getField("LeavesQty");
	    execution.setNumber(leavesQty.getField("mantissa"), values.getDecimal("151", BigDecimal.valueOf(Integer.MAX_VALUE)));
	    Group cumQty = (Group) execution.getField("CumQty");
	    execution.setNumber(cumQty.getField("mantissa"), values.getDecimal("14", BigDecimal.valueOf(Integer.MAX_VALUE)));
	    execution.setNumber(execution.getField("TradeDate"), values.getInt("75", Short.MAX_VALUE));
	    
	    GroupObjectArray fillsGrp = execution.getGroupArray(execution.getField("FillsGrp"));
	    int fillsGrpCount = values.getGroupCount("FillsGrp");
	    for( int i = 0; i < fillsGrpCount; i ++ ) {
	    	GroupObject aFillGrp = fillsGrp.addGroupObject();
		    MessageValues fillGrpValues = values.getGroup("FillsGrp", i);
	    	Group fillPx = (Group) aFillGrp.getField("FillPx");
	    	aFillGrp.setNumber(fillPx.getField("mantissa"), fillGrpValues.getDecimal("1364", BigDecimal.valueOf(Long.MAX_VALUE)).movePointRight(3));
	    	Group fillQty = (Group) aFillGrp.getField("FillQty");
	    	aFillGrp.setNumber(fillQty.getField("mantissa"), fillGrpValues.getDecimal("1365", BigDecimal.valueOf(Integer.MAX_VALUE)));
	    }

	    System.out.println(MessageUtil.toJsonString(execution));
	    outFile.write(outBuffer.array(), 0, 
	    		outOffset + execution.getSize()+execution.getDefinition().getHeader().getSize());
	  }
	  
	  private void doTest3(InputStream in, MessageValues values, OutputStream outFile)
			  throws IOException, TestException {
		  TestException testException = new TestException();
		  int inOffset = 0;
		  byte[] inBytes = new byte[4096];
		  in.read(inBytes, inOffset, inBytes.length);
		  ByteBuffer inBuffer = ByteBuffer.wrap(inBytes, inOffset, inBytes.length);

		  SBEMessageSchema schema3 =  null;
		  try {
			  schema3 = SBEMessageSchema.createSBESchema("schema3.xml");
		  } catch (Exception e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
		  }
		  
		  GroupObject newOrder = schema3.wrapSbeBuffer(inBuffer, inOffset);
		  if (99 != newOrder.getDefinition().getID() ) {
			  testException.addDetail("Unexpected message type",
					  Integer.toString(99),
					  Integer.toString(newOrder.getDefinition().getID()));
			  throw testException;
		  }

		  int outOffset = 0;
		  ByteBuffer outBuffer = ByteBuffer.allocate(4096);
		  GroupObject execution = schema3.createSbeBuffer(98, outBuffer, outOffset);
		  byte[] value = values.getString("37").getBytes();
		  execution.setBytes(execution.getField("OrderID"), value, 0, value.length);
		  value = values.getString("17").getBytes();
		  execution.setBytes(execution.getField("ExecID"), value, 0, value.length);
		  execution.setByte(execution.getField("ExecType"), values.getChar("150", Byte.MAX_VALUE));
		  execution.setByte(execution.getField("OrdStatus"), values.getChar("39", Byte.MAX_VALUE));
		  value = newOrder.getString(newOrder.getField("Symbol")).getBytes();
		  execution.setBytes(execution.getField("Symbol"), value, 0, value.length);
		  Group monthYear = (Group) execution.getField("MaturityMonthYear");
		  execution.setByte(monthYear.getField("month"), (byte) 255);
		  execution.setChar(execution.getField("Side"), newOrder.getChar(newOrder.getField("Side")));
		  Group leavesQty = (Group) execution.getField("LeavesQty");
		  execution.setNumber(leavesQty.getField("mantissa"), values.getDecimal("151", BigDecimal.valueOf(Integer.MAX_VALUE)));
		  Group cumQty = (Group) execution.getField("CumQty");
		  execution.setNumber(cumQty.getField("mantissa"), values.getDecimal("14", BigDecimal.valueOf(Integer.MAX_VALUE)));
		  execution.setNumber(execution.getField("TradeDate"), values.getInt("75", Short.MAX_VALUE));

		  value = values.getString("48").getBytes();
		  execution.setBytes(execution.getField("SecurityID"), value, 0, value.length);
		  
		  GroupObjectArray fillsGrp = execution.getGroupArray(execution.getField("FillsGrp"));
		  int fillsGrpCount = values.getGroupCount("FillsGrp");
		  for( int i = 0; i < fillsGrpCount; i ++ ) {
			  GroupObject aFillGrp = fillsGrp.addGroupObject();
			  MessageValues fillGrpValues = values.getGroup("FillsGrp", i);
			  Group fillPx = (Group) aFillGrp.getField("FillPx");
			  aFillGrp.setNumber(fillPx.getField("mantissa"), fillGrpValues.getDecimal("1364", BigDecimal.valueOf(Long.MAX_VALUE)).movePointRight(3));
			  Group fillQty = (Group) aFillGrp.getField("FillQty");
			  aFillGrp.setNumber(fillQty.getField("mantissa"), fillGrpValues.getDecimal("1365", BigDecimal.valueOf(Integer.MAX_VALUE)));
		  }
		  
		  value = values.getString("1328").getBytes();
		  execution.setBytes(execution.getField("RejectText"), value, 0, value.length);
		  System.out.println(MessageUtil.toJsonString(execution));
		  outFile.write(outBuffer.array(), 0, 
				  outOffset + execution.getSize()+execution.getDefinition().getHeader().getSize());
	  }
}
