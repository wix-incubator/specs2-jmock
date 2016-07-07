specs2-jmock [![Build Status](https://travis-ci.org/wix/specs2-jmock.svg?branch=master)](https://travis-ci.org/wix/specs2-jmock) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.wix/specs2-jmock_2.11/badge.svg?style=flat)](http://mvnrepository.com/artifact/com.wix/specs2-jmock_2.11)

============

[![Join the chat at https://gitter.im/wix/specs2-jmock](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/wix/specs2-jmock?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

This is a specs2 adapter + DSL for using the popular mocking framework JMock

#Installation
###Maven
```xml
<dependency>
    <groupId>com.wix</groupId>
    <artifactId>specs2-jmock_${scala.version}</artifactId>
    <version>x.y.z</version>
    <scope>test</scope>
</dependency>
```
###SBT
```sbt
libraryDependencies += "com.wixpress" %% "specs2-jmock" % "x.y.z"
```

* for latest version check [releases](https://github.com/wix/specs2-jmock/releases)

#Usage
Mixin the `JMock` trait to your Specification class, and use the DSL.

#Example
```Scala
import com.wixpress.common.specs2.JMock

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

