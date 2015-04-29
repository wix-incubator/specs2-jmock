package com.wixpress.common.specs2

import org.jmock.api.Action
import org.jmock.internal.{State, StatePredicate}
import org.jmock.lib.concurrent.Synchroniser
import org.jmock.lib.legacy.ClassImposteriser
import org.jmock.{Expectations, Mockery, Sequence}
import org.specs2.execute.{AsResult, Result, ResultExecution, Success}
import org.specs2.main.{ArgumentsArgs, ArgumentsShortcuts}
import org.specs2.matcher.{Matcher, MustMatchers}
import org.specs2.mock.HamcrestMatcherAdapter
import org.specs2.specification.AroundExample

import scala.reflect.ClassTag

/*      __ __ _____  __                                              *\
**     / // // /_/ |/ /          Wix                                 **
**    / // // / /|   /           (c) 2006-2014, Wix LTD.             **
**   / // // / //   |            http://www.wix.com/                 **
**   \__/|__/_//_/| |                                                **
\*                |/                                                 */

trait JMock extends MustMatchers with AroundExample with ArgumentsShortcuts with ArgumentsArgs{
  isolated

  private val synchroniser: Synchroniser = new Synchroniser
  private[this] val context:Mockery = new Mockery{{setThreadingPolicy(synchroniser)}}
  val expectations = new Expectations

  protected def around[T : AsResult](t: =>T): Result = {
    AsResult(t) and ResultExecution.execute{ context.assertIsSatisfied(); Success() }
  }

  implicit def anyAsResult[A] : AsResult[A] = new AsResult[A]{
    def asResult(a: =>A) =
      ResultExecution.effectively { a; Success() }
  }

  def useClassImposterizer() = context.setImposteriser(ClassImposteriser.INSTANCE)


  def allowing[T](t: T): T = expectations.allowing(t)
  def never[T](t: T): T = expectations.never(t)
  def will(action: Action) = expectations.will(action)
  def onConsecutiveCalls(actions: Action*): Action = Expectations.onConsecutiveCalls(actions:_*)
  def returnValue[T](t: T): Action = Expectations.returnValue(t)
  def throwException(e:Throwable) : Action = Expectations.throwException(e)
  def oneOf[T](t: T): T = expectations.oneOf(t)
  def checking(f: => Unit) = {f; context.checking(expectations)}
  def exactly(count: Int) = expectations.exactly(count)
  def atLeast(count: Int) = expectations.atLeast(count)
  def ignoring[T](mockObject: T) = expectations.ignoring(mockObject)
  @deprecated("then is now a deprecated identifier in scala, use set instead.")
  def then(state: State) = expectations.then(state)
  def set(state: State) = expectations.then(state)
  def when(predicate: StatePredicate) = expectations.when(predicate)
  def any[T](implicit t: ClassTag[T]): Matcher[T] = beAnInstanceOf(t)

  def `with`[T](m: Matcher[T]): T = expectations.`with`(HamcrestMatcherAdapter(m))
  def `with`[T](value: T): T = expectations.`with`(value)
  def having[T](m: Matcher[T]): T = expectations.`with`(HamcrestMatcherAdapter(m))
  def having[T](value: T): T = expectations.`with`(value)

  def mock[T](implicit ct: ClassTag[T]): T = context.mock(ct.runtimeClass.asInstanceOf[Class[T]])
  def mock[T](name: String)(implicit ct: ClassTag[T]): T = context.mock(ct.runtimeClass.asInstanceOf[Class[T]], name)

  def states(name: String) = context.states(name)

  def inSequence(sequence: Sequence) = expectations.inSequence(sequence)
  def sequence(name: String) = context.sequence(name)

  def waitUntil(p : StatePredicate) = synchroniser.waitUntil(p)
  def waitUntil(p : StatePredicate, timeoutMs : Long) = synchroniser.waitUntil(p,timeoutMs)

  implicit class Stubbed [T](c: T) {

    def will(action: Action, consecutive: Action*): Unit = {
      if (consecutive.isEmpty)
        expectations.will(action)
      else
        expectations.will(Expectations.onConsecutiveCalls((action +:consecutive):_*))
    }

    def willReturn[K <: T](t: K): Unit = will(returnValue(t))
    
    def willThrow[K <: Throwable](t: K): Unit = will(throwException(t))
  }
}

