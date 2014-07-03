specs2-jmock
============

This is a specs2 adapter + DSL for using the popular mocking framework JMock

#Usage
Mixin the `JMock` trait to your Specification class, and use the DSL.

#Example
```Scala
class JMockTest extends JMock  {

  "My JMock test" should { 
    "Do something" in {
      val mockFoo = mock(classOf[FooTrait])
      checking({
        allowing(mockFoo).bar; will(returnValue("foo"))
        oneOf(mockFoo).baz()
      })
      val result = mockFoo.bar
      mockFoo.baz
      result must be equalTo "foo"
    }
    ...
    
  }
```
  
  
