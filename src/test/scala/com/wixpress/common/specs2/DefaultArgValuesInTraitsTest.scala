package com.wixpress.common.specs2

import org.jmock.api.ExpectationError
import org.specs2.execute.AsResult
import org.specs2.mutable.Specification

//noinspection RedundantDefaultArgument
class DefaultArgValuesInTraitsTest extends Specification {
  "when mocking traits" >> {
    "method expectation with non-default param - as value" in withJMock { jmock =>
      import jmock.Stubbed
      val mocked = jmock.mock[ToMock]
      jmock.checking {
        jmock.allowing(mocked).doSomething(3) willReturn 42
      }
      mocked.doSomething(3) must_=== 42
      mocked.doSomething(3, "bar") must throwA[ExpectationError]("unexpected invocation")
    }

    "method expectation with non default and default params - as values" in withJMock { jmock =>
      import jmock.{Stubbed, allowing, checking, mock}
      val mocked = mock[ToMock]
      checking {
        allowing(mocked).doSomething(3, "bar") willReturn 42
      }
      mocked.doSomething(3, "bar") must_=== 42
      mocked.doSomething(3, "foo") must throwA[ExpectationError]("unexpected invocation")
    }

    "method expectation with non-default param - as matcher" in withJMock { jmock =>
      import jmock.{Stubbed, allowing, checking, defaultArgsAreMatchers, having, mock}
      val mocked = mock[ToMock]
      checking {
        defaultArgsAreMatchers {
          allowing(mocked).doSomething(having(beEqualTo(3))) willReturn 42
        }
      }
      mocked.doSomething(3) must_=== 42
      mocked.doSomething(3, b = false) must throwA[ExpectationError]("unexpected invocation")
    }

    "method expectation with non-default and overridden params - as matchers" in withJMock { jmock =>
      import jmock.{Stubbed, allowing, checking, defaultArgsAreMatchers, having, mock}
      val mocked = mock[ToMock]
      checking {
        defaultArgsAreMatchers {
          allowing(mocked).doSomething(having(beEqualTo(3)), having(equalTo("bar"))) willReturn 42
        }
      }
      mocked.doSomething(3, "bar") must_=== 42
      mocked.doSomething(3, "foo") must throwA[ExpectationError]("unexpected invocation")
    }
  }

  def withJMock[R: AsResult](f: JMockDsl => R): R = {
    val jmock = new JMockDsl {
      var successful: Boolean = false
    }
    try {
      val res = f(jmock)
      jmock.successful = implicitly[AsResult[R]].asResult(res).isSuccess
      res
    }
    finally {
      if(!jmock.successful) {
        try
          jmock.throwIfAssertUnsatisfied()
        catch {
          case e: Throwable => e.printStackTrace()
        }
      }
    }
  }
}

trait ToMock {
  def doSomething(i: Int, s: String = "foo", b: Boolean = true): Int
}
