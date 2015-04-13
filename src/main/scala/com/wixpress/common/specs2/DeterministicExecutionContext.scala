package com.wixpress.common.specs2

import org.jmock.lib.concurrent.DeterministicExecutor
import org.specs2.execute.{AsResult, Result}
import org.specs2.matcher.ConcurrentExecutionContext
import org.specs2.specification.AroundExample

import scala.concurrent.ExecutionContext

/**
 * Sets up an implicit [[scala.concurrent.ExecutionContext]] backed with [[org.jmock.lib.concurrent.DeterministicExecutor]].
 *
 * All submitted tasks are executed in background thread before evaluating test result.
 *
 * Needed to override [[org.specs2.matcher.ConcurrentExecutionContext]] here to avoid conflicting implicit [[scala.concurrent.ExecutionContext]].
 *
 * NOTE: Mixin this trait after [[com.wixpress.common.specs2.JMock]] trait.
 */
trait DeterministicExecutionContext extends ConcurrentExecutionContext with AroundExample {

  private val executor = new DeterministicExecutor

  override implicit def concurrentExecutionContext: ExecutionContext = new ExecutionContext {
    override def execute(runnable: Runnable): Unit = executor.execute(runnable)

    override def reportFailure(cause: Throwable): Unit = throw cause
  }

  abstract override protected def around[T: AsResult](t: => T): Result = {
    val example = t
    executor.runUntilIdle()
    super.around(example)
  }
}
