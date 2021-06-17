package com.wixpress.common.specs2

import org.specs2.mutable.Specification

class ByNameArgumentsTest extends Specification with JMockTestSupport {
  "support by name parameters" in withJMock { jmock =>
    import jmock.{Stubbed, mock, allowing, checking}
    val mocked = mock[ByNameArgs]
    checking {
      allowing(mocked).doSomething(2, "foo") willReturn 42
    }

    mocked.doSomething(1 + 1, "foo") === 42
  }
  "support Function0 parameters" in withJMock { jmock =>
    import jmock.{Stubbed, mock, allowing, checking}
    val mocked = mock[ThunkArgs]
    checking {
      allowing(mocked).doSomething(() => 2, "foo") willReturn 42
    }

    mocked.doSomething(() => 1 + 1, "foo") === 42
  }

  "support by name parameter matchers" in withJMock { jmock =>
    import jmock.{Stubbed, mock, checking, having, expect}
    val mocked = mock[ByNameArgs]
    checking {
      expect.atMost(1)(mocked)(_.doSomething(having(beEqualTo(2)), "foo")) willReturn 42
    }

    mocked.doSomething(1 + 1, "foo") === 42
  }
}

trait ByNameArgs {
  def doSomething(i: => Int, s: String): Int
}

trait ThunkArgs {
  def doSomething(i: () => Int, s: String): Int
}
