package com.github.bwsoft.iris.message.sbe.conformance;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import io.fixprotocol.sbe.conformance.TestException;
import io.fixprotocol.sbe.conformance.rlimpl.RLValidator;
import io.fixprotocol.sbe.conformance.rlimpl.RLinjector;

public class IrisInjectorLoopbackTest {

	  @Parameter
	  public int testNumber = -1;

	  @Parameters
	  public static Collection<Integer[]> data() {
	    return Arrays.asList(new Integer[][] {{0}, {1}, {2}});
	  }

	  static final String[] injectorFiles = {"test1inject.sbe", "test2inject.sbe", "test3inject.sbe"};
	  static final String[] responderFiles = {"test1respond.sbe", "test2respond.sbe", "test3respond.sbe"};
	  static final String[] testFiles = {"test1.json", "test2.json", "test3.json"};


	  @Test
	  public void test() throws IOException, TestException {
		  if( testNumber < 0 )
			  return;
		  
	    RLinjector.main(new String[] {testFiles[testNumber], injectorFiles[testNumber]});
	    IrisUnderTest.main(new String[] {testFiles[testNumber], injectorFiles[testNumber],
	        responderFiles[testNumber]});
	    RLValidator.main(new String[] {testFiles[testNumber], responderFiles[testNumber]});
	  }
}
