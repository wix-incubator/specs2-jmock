package com.wixpress.common.specs2.scope

import com.wixpress.common.specs2.{AssertionCheckingAround, JMockDsl}
import org.specs2.matcher.Scope
import org.specs2.mutable.Specification

trait JMock extends AroundCopiedFromSpecs2.Around2 with AssertionCheckingAround with JMockDsl {

  this: Scope =>

  if (this.isInstanceOf[Specification]) {
    System.err.println("You shouldn't mixin scope.JMock on a Specification. You probably want to use mutable.JMock")
    throw new IllegalArgumentException("wrong mixin of JMock trait")
  }
}
