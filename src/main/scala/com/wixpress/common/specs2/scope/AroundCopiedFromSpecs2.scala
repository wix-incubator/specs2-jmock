package com.wixpress.common.specs2.scope

import org.specs2.execute.{AsResult, Result, ResultExecution}

object AroundCopiedFromSpecs2 {

  trait Around1 {
    outer =>

    def around[T: AsResult](t: => T): Result

    def apply[T: AsResult](a: => T) = around(a)

    /** compose the actions of 2 Around traits */
    def compose(a: Around1): Around1 = new Around1 {
      def around[T: AsResult](t: => T): Result = {
        a.around(outer.around(t))
      }
    }

    /** sequence the actions of 2 Around traits */
    def andThen(a: Around1): Around1 = new Around1 {
      def around[T: AsResult](t: => T): Result = {
        outer.around(a.around(t))
      }
    }
  }

  trait Around2 extends Around1 with DelayedInit {
    /** use effectively to re-throw FailureExceptions if x failed */
    override def delayedInit(x: => Unit): Unit = {
      ResultExecution.effectively(around {
        Result.resultOrSuccess(x)
      })
      ()
    }
  }

}
