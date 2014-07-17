package com.wixpress.common.specs2

import org.jmock.api.Action
import org.jmock.internal.{State, StatePredicate}
import org.jmock.lib.concurrent.Synchroniser
import org.jmock.{Expectations, Mockery}
import org.specs2.execute.{AsResult, Result, ResultExecution, Success}
import org.specs2.matcher.Matcher
import org.specs2.mock.MatcherAdapter
import org.specs2.mutable.Specification
import org.specs2.specification.AroundExample

import scala.reflect.ClassTag

/*      __ __ _____  __                                              *\
**     / // // /_/ |/ /          Wix                                 **
**    / // // / /|   /           (c) 2006-2014, Wix LTD.             **
**   / // // / //   |            http://www.wix.com/                 **
**   \__/|__/_//_/| |                                                **
\*                |/                                                 */

trait JMock extends Specification with AroundExample{
  isolated
  private[this] val context:Mockery = new Mockery{{setThreadingPolicy(new Synchroniser)}}
  val expectations = new Expectations

  protected def around[T : AsResult](t: =>T): Result = {
    AsResult(t) and ResultExecution.execute{ context.assertIsSatisfied(); Success() }
  }

  implicit def anyAsResult[A] : AsResult[A] = new AsResult[A]{
    def asResult(a: =>A) =
      ResultExecution.effectively { a; Success() }
  }


  def allowing[T](t: T): T = expectations.allowing(t)
  def will(action: Action) = expectations.will(action)
  def returnValue[T](t: T): Action = Expectations.returnValue(t)
  def oneOf[T](t: T): T = expectations.oneOf(t)
  def checking(f: => Unit) = {f; context.checking(expectations)}
  def exactly(count: Int) = expectations.exactly(count)
  def atLeast(count: Int) = expectations.atLeast(count)
  def ignoring[T](mockObject: T) = expectations.ignoring(mockObject)
  def then(state: State) = expectations.then(state)
  def when(predicate: StatePredicate) = expectations.when(predicate)
  def any[T](implicit t: ClassTag[T]) = beAnInstanceOf(t)

  def `with`[T](m: Matcher[T]): T = expectations.`with`(new MatcherAdapter(m))

  def mock[T](implicit ct: ClassTag[T]): T = context.mock(ct.runtimeClass.asInstanceOf[Class[T]])
  def states(name: String) = context.states(name)
}

