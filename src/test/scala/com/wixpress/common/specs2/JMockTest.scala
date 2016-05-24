package com.wixpress.common.specs2

import java.util.concurrent.Executors

import org.jmock.States
import org.specs2.matcher.BeEqualTo
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

import scala.language.implicitConversions
import scala.util.{Success, Try}


class JMockTest extends Specification with JMock {

  def toRunnable(func: => String): Runnable = new Runnable() {
    override def run(): Unit = func
  }

  trait Context extends Scope

  "JMock trait" should {
    "Provide usage of a checking block with jmock expectations in it" in new Context {
      val mockDummy = mock[Dummy]
      checking {
        allowing(mockDummy).func1
        will(returnValue("foo"))
        oneOf(mockDummy).func2()
      }
      val result = mockDummy.func1
      mockDummy.func2()
      result must be equalTo "foo"
    }

    "accept specs2 matcher in with " in new Context  {
      val mockDummy = mock[Dummy]
      checking {
        val to: BeEqualTo = equalTo("it works")
        oneOf(mockDummy).func3(`with`[String](to))
      }
      mockDummy.func3("it works")
    }

    "accept specs2 beNull matcher in with " in new Context {
      val mockDummy = mock[Dummy]
      checking {
        oneOf(mockDummy).func3(`with`(beNull).asInstanceOf[String])
      }
      mockDummy.func3(null)
    }

    "accept specs2 matcher in having (alternative to `with`) " in new Context {
      val mockDummy = mock[Dummy]
      checking {
        oneOf(mockDummy).func3(having[String](equalTo("it works")))
      }
      mockDummy.func3("it works")
    }

    "accept the result of any in `with`" in new Context {
      val mockDummy = mock[Dummy]
      checking {
        oneOf(mockDummy).func3(`with`(any[String]))
      }
      mockDummy.func3("bla")
    }

    "accept the result of any in having (alternative to `with`)" in new Context {
      val dummy = mock[Dummy]
      checking {
        oneOf(dummy).higherKinded(having(any[HigherKind[_]]))
      }
      dummy.higherKinded(new HigherKind("123"))
    }

    "accept the result of any of a higher-kind type, in having (alternative to `with`)" in new Context {
      val mockDummy = mock[Dummy]
      checking {
        oneOf(mockDummy).func3(having(any[String]))
      }
      mockDummy.func3("bla")
    }

    "accept a string in `with`" in new Context {
      val mockDummy = mock[Dummy]
      checking(
        oneOf(mockDummy).func3(`with`("bla"))
      )
      mockDummy.func3("bla")
    }

    "accept an Int in `with`" in new Context {
      val mockDummy = mock[Dummy]
      checking(
        oneOf(mockDummy).func4(`with`(5))
      )
      mockDummy.func4(5)
    }

    "accept sequence directives" in new Context {
      val seq = sequence("mySequence")
      val mockDummy = mock[Dummy]
      checking {
        oneOf(mockDummy).func1; inSequence(seq)
        oneOf(mockDummy).func2(); inSequence(seq)
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
      }
      mockDummy.increment must beEqualTo(0)
      mockDummy.increment must beEqualTo(1)
      mockDummy.increment must beEqualTo(2)
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
      }

      dummyRepeater.repeat()
    }

    "support waitUntil mechanism" in new Context {
      val stateMachine: States = states("start")
      val mockDummy1 = mock[Dummy]

      checking {
        oneOf(mockDummy1).func1
        set(stateMachine.is("end"))
      }

      Executors.newSingleThreadExecutor().execute(toRunnable(mockDummy1.func1))

      waitUntil(stateMachine.is("end"), 1000)
    }

    "allow to expect an exception from a mock" in new Context {
      val mockDummy = mock[Dummy]
      checking {
        allowing(mockDummy).func1
        will(throwException(new NullPointerException))
      }

      mockDummy.func1 must throwA[NullPointerException]
    }

    "support never expectation" in new Context {
      val mockDummy = mock[Dummy]
      checking {
        never(mockDummy).func1
      }
    }

    "have `any` return a matcher of the right class" in new Context {
      val mockDummy = mock[Dummy]
      checking {
        oneOf(mockDummy).func3(having(any[String]))
      }

      mockDummy.func3("bla")
    }

    "support any[Integer]" in new Context {
      val mockDummy = mock[Dummy]
      checking {
        oneOf(mockDummy).func4(having(any[Int]))
      }

      mockDummy.func4(5)
    }

    "support any[Integer]" in new Context {
      val mockDummy = mock[Dummy]
      checking {
        oneOf(mockDummy).func4(having(any[Int]))
      }

      mockDummy.func4(Int.MaxValue)
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
  }
}

trait Dummy {
  def func1: String
  def func2() {}
  def func3(arg: String)
  def func3(arg: Boolean)
  def funcBool(arg: Boolean)
  def func4(arg: Int)
  def funcLong(arg: Long)
  def funcShort(arg: Short)
  def funcFloat(arg: Float)
  def funcDouble(arg: Double)
  def funcByte(arg: Byte)
  def funcChar(arg: Char)
  def increment: Int
  def getSet: Set[String]
  def getMap: Map[String, Int]
  def getImmutableMap: collection.immutable.Map[String, Int]
  def get(): Unit
  def higherKinded(arg: HigherKind[_])
}

class DummyClass {
  def foo = "bar"
}

class DummyRepeater(dummy: Dummy) {
  def repeat(): Unit = (1 to 2).foreach(_ ⇒ dummy.func1)
}

class HigherKind[T](kind: T)
