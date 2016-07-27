This is a parser for the [simple binary encoding] (https://github.com/FIXTradingCommunity/fix-simple-binary-encoding) (SBE) message.

This SBE parser provides a generic procedure to process all types of SBE messages in the same logic way. Below is a simple example on how to use it to print out the SBE message:

```

  byte[] sbeBuffer = ... ;// a buffer contains SBE message
  int offset = 0; // the start position of SBE message
  String xmlSchemaFilename = ...; the filename of SBE XML schema. Program will search both classpath and file structure.
  
  // create SBE schema that can parse all messages defined in the XML schema
  SBEMessageSchema schema = SBEMessageSchema.createSBESchema(xmlSchemaFilename);
  
  // parse the sbe message buffer
  GroupObject obj = schema.wrapForRead(sbeBuffer, bufferOffset);
  
  // dump the whole message in the json string representation
  System.out.println(obj.toString());
  
```

Refer to usage guide for additional details.

TODO: provide the utility to parse all CMEG (Chicago Mercantile Exchange Group) market data. 
