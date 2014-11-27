package com.wixpress.common.specs2

import java.util.concurrent.Executors

import org.jmock.States
import org.specs2.mutable.Specification

import scala.language.implicitConversions


class JMockTest extends Specification with JMock {

  def toRunnable(func: String): Runnable with Object {def run(): Unit} = {
    new Runnable() {
      override def run(): Unit = func
    }
  }

  "JMock trait" should {
    "Provide usage of a checking block with jmock expectations in it" in {
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

    "accept specs2 matcher in with " in {
      val mockDummy = mock[Dummy]
      checking {
        oneOf(mockDummy).func3(`with`(equalTo("it works")))
      }
      mockDummy.func3("it works")
    }

    "accept the result of any in `with`" in {
      val mockDummy = mock[Dummy]
      checking {
        oneOf(mockDummy).func3(`with`(any[String]))
      }
      mockDummy.func3("bla")
    }

    "accept a string in `with`" in {
      val mockDummy = mock[Dummy]
      checking(
        oneOf(mockDummy).func3(`with`("bla"))
      )
      mockDummy.func3("bla")
    }

    "accept an Int in `with`" in {
      val mockDummy = mock[Dummy]
      checking(
        oneOf(mockDummy).func4(`with`(5))
      )
      mockDummy.func4(5)
    }

    "accept sequence directives" in {
      val seq = sequence("mySequence")
      val mockDummy = mock[Dummy]
      checking {
        oneOf(mockDummy).func1; inSequence(seq)
        oneOf(mockDummy).func2(); inSequence(seq)
      }
      mockDummy.func1
      mockDummy.func2()
    }

    "accept onConsecutiveCalls in a will" in {
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

    "accept creating two mocks of the same type different names" in {
      val mockDummy1 = mock[Dummy]("dummy1")
      val mockDummy2 = mock[Dummy]("dummy2")

      checking {
        oneOf(mockDummy1).func1
        oneOf(mockDummy2).func2
      }

      mockDummy1.func1
      mockDummy2.func2
    }

    "support waitUntil mechanism" in {
      val stateMachine: States = states("start")
      val mockDummy1 = mock[Dummy]

      checking {
        oneOf(mockDummy1).func1
        then(stateMachine.is("end"))
      }

      Executors.newSingleThreadExecutor().execute(toRunnable(mockDummy1.func1))

      waitUntil(stateMachine.is("end"), 1)
    }
  }
}

trait Dummy {
  def func1: String
  def func2() {}
  def func3(arg: String)
  def func4(arg: Int)
  def increment: Int
}