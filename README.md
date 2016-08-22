#IRIS

IRIS provides a SBE ([Simple Binary Encoding](https://github.com/FIXTradingCommunity/fix-simple-binary-encoding)) message codec. It contains a common data structure for all SBE messages. And hence a generic code logic can be defined in processing different SBE messages. This allows to design a software that can have less dependency on the detail SBE message structure. It reduces the impact on the application upon the change or modification of the underlying SBE message structure. It makes this kind of change on message structure more managable especially for a large organization that operates a highly distributed computer system. 

#Usage

Use following maven dependency to use the codec

```
	<dependency>
	  <groupId>com.github.bwsoft.iris</groupId>
	  <artifactId>message</artifactId>
	  <version>1.1.1</version>
	</dependency>
```

For quick start, take a look at the sample code [Car.java] (https://github.com/bwsoft/iris/blob/master/sample/src/main/java/com/github/bwsoft/iris/sample/Car.java) which is based upon a sample SBE XML template, [example-schema.xml](https://github.com/bwsoft/iris/blob/master/sample/src/main/resources/example-schema.xml). It requires a basic understanding of the SBE and the structure of a SBE XML template. A brief tutorial is [here](https://github.com/bwsoft/iris/wiki/Brief-Introduction-on-SBE).

For more details, refer to the [wiki](https://github.com/bwsoft/iris/wiki) page for the usage reference. The [API doc](http://bwsoft.github.io/iris/doc/index.html) is available online and can be downloaded from maven central repostiory.

#Directory Layout

Main source code: [message/src/main] (https://github.com/bwsoft/iris/tree/master/message/src/main)

Junit test code: [message/src/test] (https://github.com/bwsoft/iris/tree/master/message/src/test)

Sample code: [sample] (https://github.com/bwsoft/iris/tree/master/sample)

#Build

./mvn

#License

Copyright 2016 bwsoft and others

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

 [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)
