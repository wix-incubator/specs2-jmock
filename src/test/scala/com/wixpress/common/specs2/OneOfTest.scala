package com.wixpress.common.specs2

import org.specs2.mutable.Specification
import org.specs2.specification.Scope

/**
 * @author viliusl
 * @since 19/05/15
 */
class OneOfTest extends Specification with JMock {

  trait Dep {
    def func1(arg: String): Boolean
  }

  class Sut(dep: Dep) {
    def func1(arg: String): Boolean = {
      if (arg == "")
        dep.func1("")
      else false
    }
  }

  trait Context extends Scope {
    val dep = mock[Dep]
    val sut = new Sut(dep)
  }

  "oneOf" should {

    "just work 1" in new Context {

      checking {
        oneOf(dep).func1("1") willReturn true
      }

      sut.func1("1") must beFalse
    }

    "just work 2" in new Context {
      checking {
        oneOf(dep).func1("") willReturn true
      }

      sut.func1("") must beTrue
    }
  }
}
