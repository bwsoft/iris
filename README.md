#IRIS

IRIS provides a SBE ([Simple Binary Encoding](https://github.com/FIXTradingCommunity/fix-simple-binary-encoding)) message parser. It contains a common data structure for all SBE messages. And hence a generic code logic can be defined in processing different SBE messages. This allows to design a software that can have less dependency on the detail SBE message structure. It reduces the impact on the application upon the change or modification of the underlying SBE message structure. It makes this kind of change on message structure more managable especially for a large organization that operates a highly distributed computer system. As an example, the following piece of code that prints out the SBE message in JSON format is always valid for all SBE messages, new or old, as long as the provided XML based SBE message schema is up-to-date.  

```

  byte[] sbeBuffer = ... ;// a buffer contains SBE message
  int offset = 0; // the start position of SBE message
  String xmlSchemaFilename = ...; // the filename of SBE XML schema. Program will search both classpath and file structure.
  
  // create SBE schema that parses all messages defined in the XML schema
  SBEMessageSchema schema = SBEMessageSchema.createSBESchema(xmlSchemaFilename);
  
  // parse the sbe message buffer
  GroupObject obj = schema.wrapForRead(sbeBuffer, bufferOffset);
  
  // dump the whole message in the json string representation
  System.out.println(obj.toString());
  
```

Refer to usage guide for additional details.

parse all CMEG (Chicago Mercantile Exchange Group) market data. 
