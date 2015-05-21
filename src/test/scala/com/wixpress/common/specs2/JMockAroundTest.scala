package com.wixpress.common.specs2

import org.specs2.execute.Error
import org.specs2.mutable.Specification

class JMockAroundTest extends Specification with JMockDsl {
  trait Test{
    def doSomething(): String
  }

  val mocked = mock[Test]

  "A test should fail on unsatisfied expectation when" >> {
    "oneOf declared but function is not called" >> {
      checking {
        oneOf(mocked).doSomething().willReturn("")
      }
      assertIsSatisfied must beAnInstanceOf[Error]
    }


  }
}
