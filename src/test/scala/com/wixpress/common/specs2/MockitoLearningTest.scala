package com.wixpress.common.specs2

import scala.language.implicitConversions



class JMockTest extends JMock  {

  "My JMock test" should { 
    "Do something" in {
      val mockFoo = mock(classOf[FooTrait])
      checking({
        allowing(mockFoo).bar
        will(returnValue("foo"))
        oneOf(mockFoo).baz()
      })
      val result = mockFoo.bar
      mockFoo.baz
      result must be equalTo "foo"
    }

    "Do somethingElse" in {
      val mockFoo = mock(classOf[FooTrait])

      checking({
        allowing(mockFoo).bar
        will(returnValue("foo"))
        oneOf(mockFoo).baz()
      })

      val result = mockFoo.bar
      mockFoo.baz()
      result must be equalTo "foo"
    }

  }
}




trait FooTrait {
  def bar:String
  def baz(){}

}