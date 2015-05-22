parsii
======

If you have questions or are just curious, please feel welcome to join the chat room:
[![Join the chat at https://gitter.im/scireum/sirius-kernel](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/scireum/OpenSource?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Super fast and simple evaluator for mathematical expressions written in Java. More background information can be found in this blog post: http://andreas.haufler.info/2013/12/how-to-write-one-of-fastest-expression.html

Using it is as simple as:

```java
Scope scope = Scope.create();   
Variable a = scope.getVariable("a");   
Expression expr = Parser.parse("3 + a * 4", scope);   
a.setValue(4);   
System.out.println(expr.evaluate());   
a.setValue(5);   
System.out.println(expr.evaluate());
```

For your convenience: A pre-built jar can be found in the build directory.

parsii is part of the open source initiative of scireum GmbH (http://www.scireum.de)   
Check out or micro kernel called SIRIUS: https://github.com/scireum/sirius

## Maven

parsii is available under:

    <dependency>
      <groupId>com.scireum</groupId>
      <artifactId>parsii</artifactId>
      <version>1.5</version>
    </dependency>

