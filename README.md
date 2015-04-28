specs2-jmock
============

This is a specs2 adapter + DSL for using the popular mocking framework JMock

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
mocking Laboratory? see [jmocking-petri-laboratory](https://github.com/wix/specs2-jmock/wiki/jmocking-petri-laboratory#jmocking-petri-laboratory)

  
  
