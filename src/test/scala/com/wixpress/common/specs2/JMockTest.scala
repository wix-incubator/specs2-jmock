package com.wixpress.common.specs2

import java.util.concurrent.Executors
import org.jmock.States
import org.specs2.matcher.BeEqualTo
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

import scala.language.implicitConversions
import JMockTestSupport._
import org.jmock.api.ExpectationError

class JMockTest extends Specification with JMock {

  def toRunnable(func: => Any): Runnable = new Runnable() {
    override def run(): Unit = func
  }

  def runLater(func: => Any) = {
    Executors.newSingleThreadExecutor().execute(toRunnable(func))
  }

  trait Context extends Scope

  "JMock trait" should {
    "Provide usage of a checking block with jmock expectations in it" in new Context {
      val mockDummy = mock[Dummy]
      checking {
        allowing(mockDummy).func1.will(returnValue("foo"))
        oneOf(mockDummy).func2()
        // new DSL flavor
        expect.allowing(mockDummy)(_.funcWithOneParameter("bar")) willReturn("42")
      }
      val result = mockDummy.func1
      mockDummy.func2()
      result must be equalTo "foo"
      mockDummy.funcWithOneParameter("bar") === "42"
    }

    "accept specs2 matcher in with " in new Context  {
      val mockDummy = mock[Dummy]
      checking {
        val to: BeEqualTo = equalTo("it works")
        oneOf(mockDummy).func3(`with`[String](to))
        // new DSL flavor
        expect.oneOf(mockDummy)(_.funcWithOneParameter(`with`(to))) willReturn("42")
      }
      mockDummy.func3("it works")
      mockDummy.funcWithOneParameter("it works") === "42"
    }

    "accept specs2 beNull matcher in with " in new Context {
      val mockDummy = mock[Dummy]
      checking {
        oneOf(mockDummy).func3(`with`(beNull).asInstanceOf[String])
        // new DSL flavor
        expect.oneOf(mockDummy)(_.funcWithOneParameter(`with`(beNull[String]))) willReturn("42")
      }
      mockDummy.func3(null)
      mockDummy.funcWithOneParameter(null) === "42"
    }

    "accept specs2 matcher in having (alternative to `with`) " in new Context {
      val mockDummy = mock[Dummy]
      checking {
        oneOf(mockDummy).func3(having[String](equalTo("it works")))
        // new DSL flavor
        expect.oneOf(mockDummy)(_.funcWithOneParameter(having(equalTo("it works")))) willReturn("42")
      }
      mockDummy.func3("it works")
      mockDummy.funcWithOneParameter("it works") === "42"
    }

    "accept the result of any in `with`" in new Context {
      val mockDummy = mock[Dummy]
      checking {
        oneOf(mockDummy).func3(`with`(any[String]))
        // new DSL flavor
        expect.oneOf(mockDummy)(_.funcWithOneParameter(`with`(any[String]))) willReturn("42")
      }
      mockDummy.func3("bla")
      mockDummy.funcWithOneParameter("bla") === "42"
    }

    "accept the result of any of a higher-kind type, in having (alternative to `with`)" in new Context {
      val dummy = mock[Dummy]
      checking {
        oneOf(dummy).higherKinded(having(any[HigherKind[_]]))
        // new DSL flavor
        expect.oneOf(dummy)(_.higherKinded1(having(any[HigherKind[_]])))
      }
      dummy.higherKinded(new HigherKind("123"))
      dummy.higherKinded1(new HigherKind("123"))
    }

    "accept the result of any in having (alternative to `with`)" in new Context {
      val mockDummy = mock[Dummy]
      checking {
        oneOf(mockDummy).func3(having(any[String]))
        // new DSL flavor
        expect.atLeast(1)(mockDummy)(_.funcWithOneParameter(having(any[String]))) willReturn "42"
      }
      mockDummy.func3("bla")
      mockDummy.funcWithOneParameter("bla") === "42"
    }

    "accept a string in `with`" in new Context {
      val mockDummy = mock[Dummy]
      checking {
        oneOf(mockDummy).func3(`with`("bla"))
        // new DSL flavor
        expect.exactly(1)(mockDummy)(_.funcWithOneParameter(`with`("bla"))) willReturn "42"
      }
      mockDummy.func3("bla")
      mockDummy.funcWithOneParameter("bla") === "42"
    }

    "accept an Int in `with`" in new Context {
      val mockDummy = mock[Dummy]
      checking {
        oneOf(mockDummy).func4(`with`(5))
        // new DSL flavor
        expect.oneOf(mockDummy)(_.func4(`with`(6)))
      }
      mockDummy.func4(5)
      mockDummy.func4(6)
    }

    "accept sequence directives" in new Context {
      val seq = sequence("mySequence")
      val mockDummy = mock[Dummy]
      checking {
        oneOf(mockDummy).func1; inSequence(seq)
        // new DSL flavor
        expect.oneOf(mockDummy)(_.func2()); inSequence(seq)
      }
      mockDummy.func1
      mockDummy.func2()
    }

    "accept onConsecutiveCalls in a will" in new Context {
      val mockDummy = mock[Dummy]
      checking {
        atLeast(1).of(mockDummy).increment; will(onConsecutiveCalls(
          returnValue(0),
          returnValue(1),
          returnValue(2)
        ))
        // new DSL flavor
        expect.atLeast(1)(mockDummy)(_.increment1) will(
          onConsecutiveCalls(
            returnValue(0),
            returnValue(1),
            returnValue(2)
          )
        )
      }
      mockDummy.increment must beEqualTo(0)
      mockDummy.increment must beEqualTo(1)
      mockDummy.increment must beEqualTo(2)
      // new DSL flavor
      mockDummy.increment1 must beEqualTo(0)
      mockDummy.increment1 must beEqualTo(1)
      mockDummy.increment1 must beEqualTo(2)
    }

    "accept creating two mocks of the same type different names" in new Context {
      val mockDummy1 = mock[Dummy]("dummy1")
      val mockDummy2 = mock[Dummy]("dummy2")

      checking {
        oneOf(mockDummy1).func1
        oneOf(mockDummy2).func2()
      }

      mockDummy1.func1
      mockDummy2.func2()
    }

    "support atMost" in new Context {
      val mockDummy = mock[Dummy]
      val dummyRepeater = new DummyRepeater(mockDummy)
      checking {
        atMost(2).of(mockDummy).func1
        // new DSL flavor
        expect.atMost(2)(mockDummy)(_.func2())
      }

      dummyRepeater.repeat()
      dummyRepeater.repeat(_.func2())
    }

    "support waitUntil mechanism" in new Context {
      val stateMachine: States = states("start")
      val mockDummy1 = mock[Dummy]

      checking {
        oneOf(mockDummy1).func1.willSet(stateMachine.to("end"))
        // new DSL flavor
        expect.oneOf(mockDummy1)(_.func2()) willSet(stateMachine.to("start"))
      }

      runLater(mockDummy1.func1)

      waitUntil(stateMachine.is("end"), 1000)

      runLater(mockDummy1.func2())

      waitUntil(stateMachine.is("start"), 1000)

    }

    "allow to expect an exception from a mock" in new Context {
      val mockDummy = mock[Dummy]
      checking {
        allowing(mockDummy).func1
        will(throwException(new NullPointerException))
        // New DSL flavor
        expect.allowing(mockDummy)(_.func2()) will(throwException(new IllegalArgumentException))
      }

      mockDummy.func1 must throwA[NullPointerException]
      mockDummy.func2() must throwA[IllegalArgumentException]
    }

    "support never expectation" in withJMock  { jm =>
      val mockDummy = jm.mock[Dummy]
      jm.checking {
        jm.never(mockDummy).funcWithOneParameter("foo")
        // new DSL flavor
        jm.expect.never(mockDummy)(_.funcWithTwoParameters("foo", jm.having(any[Int])))
      }
      mockDummy.funcWithOneParameter("foo") must throwA[ExpectationError]("unexpected invocation")
      mockDummy.funcWithTwoParameters("foo", 42) must throwA[ExpectationError]("unexpected invocation")
    }

    "have `any` return a matcher of the right class" in new Context {
      val mockDummy = mock[Dummy]
      checking {
        oneOf(mockDummy).func3(having(any[String]))
        // new DSL flavor
        expect.oneOf(mockDummy)(_.funcWithOneParameter(having(any[String])))
      }

      mockDummy.func3("bla")
      mockDummy.funcWithOneParameter("bla-bla")
    }

    "support any[Integer]" in new Context {
      val mockDummy = mock[Dummy]
      checking {
        oneOf(mockDummy).func4(having(any[Int]))
        // new DSL flavor
        expect.oneOf(mockDummy)(_.func5(having(any[Int])))
      }

      mockDummy.func4(5)
      mockDummy.func5(6)
    }

    "support any[Integer]" in new Context {
      val mockDummy = mock[Dummy]
      checking {
        oneOf(mockDummy).func4(having(any[Int]))
        // new DSL flavor
        expect.oneOf(mockDummy)(_.func5(having(any[Int])))
      }

      mockDummy.func4(Int.MaxValue)
      mockDummy.func5(Int.MaxValue)
    }

    "support any[Long]" in new Context {
      val mockDummy = mock[Dummy]
      checking {
        oneOf(mockDummy).funcLong(having(any[Long]))
      }

      mockDummy.funcLong(5L)
    }

    "support any[Short]" in new Context {
      val mockDummy = mock[Dummy]
      checking {
        oneOf(mockDummy).funcShort(having(any[Short]))
      }

      mockDummy.funcShort(5.toShort)
    }

    "support any[Float]" in new Context {
      val mockDummy = mock[Dummy]
      checking {
        oneOf(mockDummy).funcFloat(having(any[Float]))
      }

      mockDummy.funcFloat(5.0f)
    }

    "support any[Double]" in new Context {
      val mockDummy = mock[Dummy]
      checking {
        oneOf(mockDummy).funcDouble(having(any[Double]))
      }

      mockDummy.funcDouble(5.0)
    }

    "support any[Boolean] in having" in new Context {
      val mockDummy = mock[Dummy]
      checking {
        oneOf(mockDummy).funcBool(having(any[Boolean]))
      }

      mockDummy.funcBool(arg = false)
    }

    "support any[Boolean] in having" in new Context {
      val mockDummy = mock[Dummy]
      checking {
        oneOf(mockDummy).funcBool(having(any[Boolean]))
      }

      mockDummy.funcBool(Boolean.box(x = true))
    }

    "support Set[String] as return type in mocked object" in new Context {
      val mockDummy = mock[Dummy]
      checking {
        oneOf(mockDummy).getSet
      }

      mockDummy.getSet
    }

    "support Map[String, Int] as return type in mocked object" in new Context {
      val mockDummy = mock[Dummy]
      checking {
        exactly(1).of(mockDummy).getMap
      }

      mockDummy.getMap
    }

    "support immutable Map[String, Int] as return type in mocked object" in new Context {
      val mockDummy = mock[Dummy]
      checking {
        exactly(1).of(mockDummy).getMap
      }

      mockDummy.getMap
    }

    "Throw an error if trying to mock a class without using class imposteriser" in new Context {
      mock[DummyClass] must throwAn[IllegalArgumentException](message = "com.wixpress.common.specs2.DummyClass is not an interface")
    }

    "support any[Byte] in having" in new Context {
      val mockDummy = mock[Dummy]
      checking {
        oneOf(mockDummy).funcByte(having(any[Byte]))
      }

      mockDummy.funcByte(arg = 1.toByte)
    }

    "support any[Char] in having" in new Context {
      val mockDummy = mock[Dummy]
      checking {
        oneOf(mockDummy).funcChar(having(any[Char]))
      }

      mockDummy.funcChar(arg = 'c')
    }
  }

  "JMock.Stubbed" >> {

    "'willReturn' should work as will(returnValue)" in new Context {
      val mockDummy = mock[Dummy]
      checking {
        oneOf(mockDummy).func1 willReturn "some"
      }

      mockDummy.func1 mustEqual "some"
    }

    "'willThrow' should work as will(throwException)" in new Context {
      val mockDummy = mock[Dummy]
      checking {
        oneOf(mockDummy).func1 willThrow new RuntimeException
      }

      mockDummy.func1 must throwA[RuntimeException]
    }

    "'will' with a single arg should act as JMock.will" in new Context {
      val mockDummy = mock[Dummy]
      checking {
        allowing(mockDummy).func1 will returnValue("some")
      }

      mockDummy.func1 mustEqual "some"
      mockDummy.func1 mustEqual "some"
    }

    "'will' with multiple args should act as JMock.will with onConsecutiveCalls" in new Context {
      val mockDummy = mock[Dummy]
      checking {
        allowing(mockDummy).func1 will (
          returnValue("first"),
          returnValue("second"))
      }

      mockDummy.func1 mustEqual "first"
      mockDummy.func1 mustEqual "second"
    }

    "willSetState should work act the same as setState" in new Context {
      val mockDummy = mock[Dummy]
      val state: States = states("state").startsAs("initial")
      checking {
        allowing(mockDummy).func1 willSet state.to("final")
      }

      mockDummy.func1

      waitUntil(state.is("final"))
    }

    "allows declaring a mock that repeats a given set of actions" in new Context {
      val mockDummy = mock[Dummy]

      checking {
        allowing(mockDummy).func1 will repeatedly(returnValue("1"),returnValue("2"))
      }
      mockDummy.func1 must beEqualTo("1")
      mockDummy.func1 must beEqualTo("2")
      mockDummy.func1 must beEqualTo("1")
      mockDummy.func1 must beEqualTo("2")
      // etc...
    }
  }

  "Jmock will answer" should {

    "'willAnswer' will run the method and return answer appropriately" in new Context {
      val mockDummy = mock[Dummy]
      checking {
        allowing(mockDummy).funcWithOneParameter(having(any[String])) willAnswer((_:String).toUpperCase)
        // new DSL flavor
        expect.allowing(mockDummy)(_.funcWithOneParameter1(having(any[String]))) willAnswer((_:String).toUpperCase)
      }

      mockDummy.funcWithOneParameter("hello") must be_===("HELLO")
      mockDummy.funcWithOneParameter1("HeLlO") must be_===("HELLO")
    }

    "'willAnswer' will run the method and return answer appropriately with 2 parameters" in new Context {
      val mockDummy = mock[Dummy]
      checking {
        allowing(mockDummy).funcWithTwoParameters(having(any[String]), having(any[Int])) willAnswer((s:String, i:Int) => s+i)
      }

      mockDummy.funcWithTwoParameters("hello", 1) must be_===("hello1")
    }

    "'willAnswer' will run the method and return answer appropriately with 3 parameters" in new Context {
      val mockDummy = mock[Dummy]
      checking {
        allowing(mockDummy).funcWithThreeParameters(having(any[String]), having(any[Int]), having(any[Char])) willAnswer((s:String, i:Int, c: Char) => s+i+c)
      }

      mockDummy.funcWithThreeParameters("hello", 1, 'k') must be_===("hello1k")
    }

    "'willAnswer' will run the method and return answer appropriately with 4 parameters" in new Context {
      val mockDummy = mock[Dummy]
      checking {
        allowing(mockDummy).funcWithFourParameters(having(any[String]), having(any[Int]), having(any[Char]), having(any[Double])) willAnswer((s:String, i:Int, c: Char, d:Double) => s+i+c+d)
      }

      mockDummy.funcWithFourParameters("hello", 1, 'k', 3.0) must be_===("hello1k3.0")
    }
  }
}

trait Dummy {
  def func1: String
  def func2() {}
  def func3(arg: String)
  def func3(arg: Boolean)
  def funcBool(arg: Boolean)
  def func4(arg: Int)
  def func5(arg: Int)
  def funcLong(arg: Long)
  def funcShort(arg: Short)
  def funcFloat(arg: Float)
  def funcDouble(arg: Double)
  def funcByte(arg: Byte)
  def funcChar(arg: Char)
  def increment: Int
  def increment1: Int
  def getSet: Set[String]
  def getMap: Map[String, Int]
  def getImmutableMap: collection.immutable.Map[String, Int]
  def get(): Unit
  def higherKinded(arg: HigherKind[_])
  def higherKinded1(arg: HigherKind[_])

  def funcWithOneParameter(arg1: String): String
  def funcWithOneParameter1(arg1: String): String
  def funcWithTwoParameters(arg1: String, arg2: Int): String
  def funcWithThreeParameters(arg1: String, arg2: Int, arg3: Char): String
  def funcWithFourParameters(arg1: String, arg2: Int, arg3: Char, arg4: Double): String
}

class DummyClass {
  def foo = "bar"
}

class DummyRepeater(dummy: Dummy) {
  def repeat(f: Dummy => Any = _.func1): Unit = (1 to 2).foreach(_ => f(dummy))
}

class HigherKind[T](kind: T)
