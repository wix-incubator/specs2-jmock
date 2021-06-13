package com.wixpress.common.specs2

import org.jmock.api.ExpectationError
import org.specs2.execute.AsResult
import org.specs2.mutable.Specification
import org.specs2.specification.core.Fragments

import scala.reflect.ClassTag

//noinspection RedundantDefaultArgument
class DefaultArgValuesInTraitsTest extends Specification {
  Fragments.foreach(Seq(
    Variant[ToMock](isClass = false),
    Variant[ClassToMock](isClass = true)
  )) { test =>
    s"when mocking a ${test.what}" >> {
      s"[${test.what}] method expectation with non-default param - as value" in withJMock { jmock =>
        import jmock.{Stubbed, allowing, checking}
        val mocked = test.mock(jmock)
        checking {
          allowing(mocked).doSomething(3) willReturn 42
        }
        mocked.doSomething(3) must_=== 42
        mocked.doSomething(3, "bar") must throwA[ExpectationError]("unexpected invocation")
      }

      s"[${test.what}] method expectation with non default and default params - as values" in withJMock { jmock =>
        import jmock.{Stubbed, allowing, checking}
        val mocked = test.mock(jmock)
        checking {
          allowing(mocked).doSomething(3, "bar") willReturn 42
        }
        mocked.doSomething(3, "bar") must_=== 42
        mocked.doSomething(3, "foo") must throwA[ExpectationError]("unexpected invocation")
      }

     s"[${test.what}] method expectation with non-default param - as matcher" in withJMock { jmock =>
        import jmock.{Stubbed, allowing, checking, defaultArgsAreMatchers, having}
        val mocked = test.mock(jmock)
        checking {
          defaultArgsAreMatchers {
            allowing(mocked).doSomething(having(beEqualTo(3))) willReturn 42
          }
        }
        mocked.doSomething(3) must_=== 42
        mocked.doSomething(3, b = false) must throwA[ExpectationError]("unexpected invocation")
      }

      s"[${test.what}] method expectation with non-default and overridden params - as matchers" in withJMock { jmock =>
        import jmock.{Stubbed, allowing, checking, defaultArgsAreMatchers, having}
        val mocked = test.mock(jmock)
        checking {
          defaultArgsAreMatchers {
            allowing(mocked).doSomething(having(beEqualTo(3)), having(equalTo("bar"))) willReturn 42
          }
        }
        mocked.doSomething(3, "bar") must_=== 42
        mocked.doSomething(3, "foo") must throwA[ExpectationError]("unexpected invocation")
      }
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

  case class Variant[T <: ToMock](isClass: Boolean)(implicit val classTag: ClassTag[T]) {
    def what = if(isClass) "class" else "trait"
    def mock(jmock: JMockDsl): ToMock = {
      if(isClass) {
        jmock.useClassImposterizer()
      }
      jmock.mock[T](classTag)
    }
  }
}

trait ToMock {
  def doSomething(i: Int, s: String = "foo", b: Boolean = true): Int
}

class ClassToMock extends ToMock {
  def doSomething(i: Int, s: String = "foo", b: Boolean = true): Int = 123
}
