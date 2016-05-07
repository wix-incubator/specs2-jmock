package com.wixpress.common.specs2

import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class JMockOnContextTest extends Specification {
  "This should fail with a context" >> new Scope with JMock { // TODO THIS IS A FAILING TEST
    val foo  = mock[Boo]
    checking {
      oneOf(foo).far
    }
  }
}

trait Boo {
  def far: Unit
}