specs2-jmock
============

This is a specs2 adapter + DSL for using the popular mocking framework JMockit 

#Installation
###Maven
```xml
<dependency>
    <groupId>com.wixpress</groupId>
    <artifactId>specs2-jmock_${scala.version}</artifactId>
    <version>x.y.z</version>
    <scope>test</scope>
</dependency>
```
###SBT
```sbt
libraryDependencies += "com.wixpress" %% "specs2-jmock" % "x.y.z"
```
#Usage
Mixin the `JMock` trait to your Specification class, and use the DSL.

#Example
```Scala
class JMockTest extends Specification with JMock  {

  "My JMock test" should { 
    "Do something" in {
      val mockFoo = mock[FooTrait]
      checking {
        allowing(mockFoo).bar.willReturn("foo")
        oneOf(mockFoo).baz()
      }
      val result = mockFoo.bar
      mockFoo.baz
      result must be equalTo "foo"
    }
    ...
    
  }
```

You can see more examples in the [tests](/src/test/scala/com/wixpress/common/specs2).

#License
Use of this source code is governed by a BSD-style license which you can find [here](/LICENSE.md).