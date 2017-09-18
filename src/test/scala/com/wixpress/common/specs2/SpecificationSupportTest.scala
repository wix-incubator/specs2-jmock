package com.wixpress.common.specs2

import org.specs2.mutable.{Spec, Specification}
import org.specs2.specification.Scope

class SpecificationSupportTest extends Specification with JMock {
  "compiles when Specification is extended" in new Scope {
  }
}
