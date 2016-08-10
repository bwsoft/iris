#IRIS

IRIS provides a SBE ([Simple Binary Encoding](https://github.com/FIXTradingCommunity/fix-simple-binary-encoding)) message codec. It contains a common data structure for all SBE messages. And hence a generic code logic can be defined in processing different SBE messages. This allows to design a software that can have less dependency on the detail SBE message structure. It reduces the impact on the application upon the change or modification of the underlying SBE message structure. It makes this kind of change on message structure more managable especially for a large organization that operates a highly distributed computer system. 

#Usage

Refer to [wiki](https://github.com/bwsoft/iris/wiki) page for the usage reference. View [API doc](http://bwsoft.github.io/iris/doc/index.html) online.

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
