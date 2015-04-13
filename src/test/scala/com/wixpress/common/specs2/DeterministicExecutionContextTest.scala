package com.wixpress.common.specs2

import java.lang.System.currentTimeMillis
import java.util.concurrent.TimeUnit.SECONDS

import org.specs2.mutable.Specification

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}

class DeterministicExecutionContextTest extends Specification with JMock with DeterministicExecutionContext {

  "DeterministicExecutionContext trait" should {

    "execute async expectations in background thread before evaluating test result" in {

      val service = mock[Service]
      val component = new Component(service)
      val delay = Duration(1, SECONDS)

      checking {
        oneOf(service).doSideEffect()
      }

      val startOfCallInTestThread = currentTimeMillis

      component.doSideEffectAfter(delay)

      currentTimeMillis - startOfCallInTestThread must be_<=(delay.toMillis)
    }

  }

}

class Component(service: Service)(implicit ctx: ExecutionContext) {
  def doSideEffectAfter(delay: Duration): Unit = Future {
    Thread.sleep(delay.toMillis)

    service.doSideEffect()
  }
}

trait Service {
  def doSideEffect(): Unit
}
