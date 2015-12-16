package com.wixpress.common.specs2

import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class ContextTest extends Specification with JMock{

  sequential


  trait Context extends Scope {
    val mockFoo = mock[Foo]
  }

  "A bar" should {
    "invoke f on foo" in new Context {
      checking {
        ignoring(mockFoo).g()
        oneOf(mockFoo).f()
      }

      new Bar(mockFoo)
    }
    "invoke g on foo" in new Context {
      checking {
        allowing(mockFoo).f()
        oneOf(mockFoo).g()
      }

      new Bar(mockFoo)
    }
  }
}

trait Foo {
  def f()
  def g()
}
class Bar(foo: Foo){
  foo.f()
  foo.g()
}