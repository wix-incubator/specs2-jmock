package com.wixpress.common.specs2.scope

import com.wixpress.common.specs2.{AssertionCheckingAround, JMockDsl}
import org.specs2.matcher.Scope

trait JMock extends AroundCopiedFromSpecs2.Around2 with AssertionCheckingAround with JMockDsl { this: Scope => }
