package com.wixpress.common.specs2

import org.jmock.api.Action
import org.jmock.internal.{State, StatePredicate}
import org.jmock.lib.concurrent.Synchroniser
import org.jmock.{AbstractExpectations, Expectations, Mockery, Sequence}
import org.specs2.execute._
import org.specs2.main.{ArgumentsArgs, ArgumentsShortcuts}
import org.specs2.matcher.{Expectable, MatchResult, Matcher, MustMatchers}
import org.specs2.mock.HamcrestMatcherAdapter
import org.specs2.specification.AroundEach

import scala.reflect.ClassTag
import scala.util.Try

/*      __ __ _____  __                                              *\
**     / // // /_/ |/ /          Wix                                 **
**    / // // / /|   /           (c) Wix LTD.                        **
**   / // // / //   |            http://www.wix.com/                 **
**   \__/|__/_//_/| |                                                **
\*                |/                                                 */

trait JMock extends JMockDsl with JMockAround

trait JMockAround extends AroundEach { this: JMockDsl ⇒
  protected def around[T : AsResult](t: =>T): Result = {
    AsResult(t) and ResultExecution.execute(assertIsSatisfied)
  }
}

trait JMockDsl extends MustMatchers with ArgumentsShortcuts with ArgumentsArgs {
  isolated

  private val synchroniser: Synchroniser = new Synchroniser
  private[this] val context:Mockery = new Mockery{{setThreadingPolicy(synchroniser)}}
  val expectations = new Expectations

  protected def assertIsSatisfied[T: AsResult]: Result with Product with Serializable = {
    Try(context.assertIsSatisfied()).map(_ ⇒ Success()).recover {
      case t: Throwable ⇒ Error(t)
    }.get
  }

  implicit def anyAsResult[A] : AsResult[A] = new AsResult[A]{
    def asResult(a: =>A) =
      ResultExecution.effectively { a; Success() }
  }
  private val delagatingImposteriser = new DelegatingImposteriser(this)
  context.setImposteriser(delagatingImposteriser)

  var usingJavaReflectionImposteriser = true
  def useClassImposterizer() = {
    usingJavaReflectionImposteriser = false
  }
  def useJavaReflectionImposterizer() = {
    usingJavaReflectionImposteriser = true
  }

  def allowing[T](t: T): T = expectations.allowing(t)
  def never[T](t: T): T = expectations.never(t)
  def will(action: Action) = expectations.will(action)
  def onConsecutiveCalls(actions: Action*): Action = AbstractExpectations.onConsecutiveCalls(actions:_*)
  def returnValue[T](t: T): Action = AbstractExpectations.returnValue(t)
  def throwException(e:Throwable) : Action = AbstractExpectations.throwException(e)
  def oneOf[T](t: T): T = expectations.oneOf(t)
  def checking(f: => Unit) = {f; context.checking(expectations)}
  def exactly(count: Int) = expectations.exactly(count)
  def atLeast(count: Int) = expectations.atLeast(count)
  def ignoring[T](mockObject: T) = expectations.ignoring(mockObject)
  @deprecated("then is now a deprecated identifier in scala, use set instead.")
  def then(state: State) = expectations.then(state)
  def set(state: State) = expectations.then(state)
  def when(predicate: StatePredicate) = expectations.when(predicate)

  def any[T](implicit ct: ClassTag[T]): Matcher[T] = new Matcher[T] {
    override def apply[S <: T](t: Expectable[S]): MatchResult[S] = {
      val superClass = ct.runtimeClass
      val expectedValue: S = t.value
      val expectableClass = expectedValue.getClass
      val IntClass = classOf[Int]
      val ShortClass = classOf[Short]
      val LongClass = classOf[Long]
      val FloatClass = classOf[Float]
      val DoubleClass = classOf[Double]
      val BooleanClass = classOf[Boolean]
      val isMatching = superClass match {
        case IntClass ⇒ IntClass.isAssignableFrom(expectableClass) || expectedValue.isInstanceOf[java.lang.Integer]
        case DoubleClass ⇒ DoubleClass.isAssignableFrom(expectableClass) || expectedValue.isInstanceOf[java.lang.Double]
        case ShortClass ⇒ ShortClass.isAssignableFrom(expectableClass) || expectedValue.isInstanceOf[java.lang.Short]
        case FloatClass ⇒ FloatClass.isAssignableFrom(expectableClass) || expectedValue.isInstanceOf[java.lang.Float]
        case LongClass ⇒ LongClass.isAssignableFrom(expectableClass) || expectedValue.isInstanceOf[java.lang.Long]
        case BooleanClass ⇒ BooleanClass.isAssignableFrom(expectableClass) || expectedValue.isInstanceOf[java.lang.Boolean]
        case _ ⇒ superClass.isAssignableFrom(expectableClass)
      }

      if (isMatching) {
        success(s"is a ${ct.runtimeClass.getCanonicalName}", t)
      }
      else {
        failure(s"is not a ${ct.runtimeClass.getCanonicalName}", t)
      }
    }
  }

  def `with`[T](m: Matcher[T]): T = expectations.`with`(HamcrestMatcherAdapter(m))
  def `with`[T](value: T): T = expectations.`with`(value)
  def having[T](m: Matcher[T]): T = `with`(m)
  def having[T](value: T): T = `with`(value)

  private def mockWithFallback[T](mock: ⇒T)(implicit ct: ClassTag[T]): T = {
    Try(mock).recover({
      case e: IllegalArgumentException if usingJavaReflectionImposteriser && !ct.runtimeClass.isInterface ⇒ throw e
      case _: Exception if usingJavaReflectionImposteriser ⇒ {
        useClassImposterizer()
        val secondTry = Try(mock)
        useJavaReflectionImposterizer()
        secondTry.get
      }
      case e: Exception ⇒ e.printStackTrace(); throw e
    }).get
  }
  def mock[T](implicit ct: ClassTag[T]): T = mockWithFallback(context.mock(ct.runtimeClass.asInstanceOf[Class[T]]))
  def mock[T](name: String)(implicit ct: ClassTag[T]): T = mockWithFallback(context.mock(ct.runtimeClass.asInstanceOf[Class[T]], name))

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
        expectations.will(AbstractExpectations.onConsecutiveCalls((action +: consecutive): _*))
    }

    def willReturn[K <: T](t: K): Unit = will(returnValue(t))

    def willThrow[K <: Throwable](t: K): Unit = will(throwException(t))
  }
}

