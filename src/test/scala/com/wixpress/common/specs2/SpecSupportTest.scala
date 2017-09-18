package com.wixpress.common.specs2

import org.specs2.mutable.Spec
import org.specs2.specification.Scope

class SpecSupportTest extends Spec with JMock {
  "compiles when Spec is extended" in new Scope {
  }
}
