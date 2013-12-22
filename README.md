parsii
======

Super fast and simple evaluator for mathematical expressions written in Java. More background information can be found in this blog post: http://andreas.haufler.info/2013/12/how-to-write-one-of-fastest-expression.html

Using it is as simple as:

Scope scope = Scope.create();   
Variable a = scope.getVariable("a");   
Expression expr = Parser.parse("3 + a * 4");   
a.setValue(4);   
System.out.println(expr.evaluate());   
a.setValue(5);   
System.out.println(expr.evaluate());

For your convenience: A pre-built jar can be found in the build directory.

parsii is part of the open source initiative of scireum GmbH (http://www.scireum.de)   
Check out or micro kernel called SIRIS: https://github.com/scireum/sirius
