package com.wixpress.common.specs2

import scala.language.implicitConversions



class JMockTest extends JMock  {

  "My JMock test" should { 
    "Do something" in {
      val mockFoo = mock[FooTrait]
      checking {
        allowing(mockFoo).bar
        will(returnValue("foo"))
        oneOf(mockFoo).baz()
      }
      val result = mockFoo.bar
      mockFoo.baz
      result must be equalTo "foo"
    }

    "Do somethingElse" in {
      val mockFoo = mock[FooTrait]

      checking {
        allowing(mockFoo).bar
        will(returnValue("foo"))
        oneOf(mockFoo).baz()
      }

      val result = mockFoo.bar
      mockFoo.baz()
      result must be equalTo "foo"
    }

    "accept specs2 matcher in with " in {
      val mockFoo = mock[FooTrait]
      checking {
        oneOf(mockFoo).bla(`with`(equalTo("it works")))
      }
      mockFoo.bla("it works")
    }

    "accept the result of any in `with`" in {
      val mockFoo = mock[FooTrait]
      checking {
        oneOf(mockFoo).bla(`with`(beAnInstanceOf[String]))
      }
      mockFoo.bla("bla")
    }

    "accept a string in `with`" in {
      val mockFoo = mock[FooTrait]
      checking(
        oneOf(mockFoo).bla(`with`("bla"))
      )
      mockFoo.bla("bla")
    }
  }
}




trait FooTrait {
  def bar:String
  def baz(){}
  def bla(blu: String)
}