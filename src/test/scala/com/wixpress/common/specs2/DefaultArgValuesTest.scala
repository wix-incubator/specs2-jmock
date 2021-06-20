package com.wixpress.common.specs2

import org.jmock.api.ExpectationError
import org.specs2.mutable.Specification
import org.specs2.specification.core.Fragments

import scala.reflect.ClassTag

//noinspection RedundantDefaultArgument
class DefaultArgValuesTest extends Specification with JMockTestSupport {
  sequential
  val variants = Seq(Variant[ClassToMock](isClass = true)) ++
    // the handling of default arg on traits only works for scala 2.12 (and higher?)
    (if(scalaVersionAtLeast("2.12")) Seq( Variant[ToMock](isClass = false)) else Nil)

  Fragments.foreach(variants) { test =>
    s"when mocking a ${ test.what }" >> {
      s"[${ test.what }] method expectation with non-default param - as value" in withJMock { jmock =>
        import jmock.{Stubbed, allowing, checking}
        val mocked = test.mock(jmock)
        checking {
          allowing(mocked).doSomething(3) willReturn 42
        }
        mocked.doSomething(3) must_=== 42
        mocked.doSomething(3, "foo") must_=== 42
        mocked.doSomething(3, "bar") must throwA[ExpectationError]("unexpected invocation")
      }

      s"[${ test.what }] method expectation with non default and default params - as values" in withJMock { jmock =>
        import jmock.{Stubbed, allowing, checking}
        val mocked = test.mock(jmock)
        checking {
          allowing(mocked).doSomething(3, "bar") willReturn 42
        }
        mocked.doSomething(3, "bar") must_=== 42
        mocked.doSomething(3, "foo") must throwA[ExpectationError]("unexpected invocation")
      }

      s"[${ test.what }] method expectation with non-default param - as matcher" in withJMock { jmock =>
        import jmock.{Stubbed, allowing, checking, expect, having}
        val mocked = test.mock(jmock)
        checking {
           expect.allowing(mocked)(_.doSomething(having(beEqualTo(3)))) willReturn 42
        }
        mocked.doSomething(3) must_=== 42
        mocked.doSomething(3, b = false) must throwA[ExpectationError]("unexpected invocation")
      }

      s"[${ test.what }] method expectation with non-default and overridden params - as matchers" in
        withJMock { jmock =>
          import jmock.{Stubbed, checking, expect, having}
          val mocked = test.mock(jmock)
          checking {
            expect.allowing(mocked)(_.doSomething(having(beEqualTo(3)), b = false)) willReturn 42
          }
          mocked.doSomething(3, "foo", b = false) must_=== 42
          mocked.doSomething(3, "foo") must throwA[ExpectationError]("unexpected invocation")
          mocked.doSomething(3, "bar", b = false) must throwA[ExpectationError]("unexpected invocation")
        }
    }
  }

  if(!scalaVersionAtLeast("2.12")) {
    "[Scala Ver < 2.12] When mocking a trait - fails will unexpected invocation" in withJMock { jmock =>
      import jmock.{Stubbed, allowing, checking, mock}
      val mocked = mock[ToMock]
      checking {
        allowing(mocked).doSomething(3) willReturn 42
      }
      mocked.doSomething(3) must throwA[ExpectationError]("unexpected invocation")
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
