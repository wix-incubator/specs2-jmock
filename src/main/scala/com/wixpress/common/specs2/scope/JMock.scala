package com.wixpress.common.specs2.scope

import com.wixpress.common.specs2.{AssertionCheckingAround, JMockDsl}
import org.specs2.mutable.{Around, Specification}

trait JMock extends Around with AssertionCheckingAround with JMockDsl {
  if (this.isInstanceOf[Specification]) {
    System.err.println("You shouldn't mixin scope.JMock on a Specification. You probably want to use mutable.JMock")
    throw new IllegalArgumentException("wrong mixin of JMock trait")
  }
}
