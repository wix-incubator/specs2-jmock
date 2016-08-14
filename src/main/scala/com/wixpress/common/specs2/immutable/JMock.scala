package com.wixpress.common.specs2.immutable

import com.wixpress.common.specs2.{JMockAroundEach, JMockDsl}
import org.specs2.Specification

trait JMock extends JMockDsl with JMockAroundEach {this: Specification ⇒ }
