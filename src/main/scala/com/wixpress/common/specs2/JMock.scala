package com.wixpress.common.specs2

import com.wixpress.common.specs2.DefaultArgsMode.{ArgsAreMatchers, ArgsAreValues}
import org.jmock.api.{Action, Imposteriser, Invocation}
import org.jmock.internal.{State, StatePredicate}
import org.jmock.lib.action.CustomAction
import org.jmock.lib.concurrent.Synchroniser
import org.jmock.syntax.ReceiverClause
import org.jmock.{Expectations, _}
import org.specs2.execute._
import org.specs2.main.{ArgumentsArgs, ArgumentsShortcuts}
import org.specs2.matcher.{Expectations => _, _}
import org.specs2.mock.HamcrestMatcherAdapter
import org.specs2.mutable.Specification
import org.specs2.specification.AroundEach
import org.specs2.specification.core.SpecificationStructure

import scala.reflect.ClassTag
import scala.util.Try

/*      __ __ _____  __                                              *\
**     / // // /_/ |/ /          Wix                                 **
**    / // // / /|   /           (c) Wix LTD.                        **
**   / // // / //   |            http://www.wix.com/                 **
**   \__/|__/_//_/| |                                                **
\*                |/                                                 */


trait JMock extends JMockDsl with JMockAroundEach {
  this: SpecificationStructure =>
}

trait JMockAroundEach extends AroundEach with AssertionCheckingAround with JMockDsl

trait AssertionCheckingAround { this: JMockDsl ⇒
  def around[T : AsResult](t: =>T): Result = {
    AsResult(t) and ResultExecution.execute(assertIsSatisfied)
  }
}

trait JMockDsl extends MustThrownMatchers with ArgumentsShortcuts with ArgumentsArgs {
  isolated

  private val synchroniser: Synchroniser = new Synchroniser
  private[this] val context: Mockery = new Mockery{{setThreadingPolicy(synchroniser)}}
  val expectations = new Expectations

  private val defaultMethodArgsModeRef = new ThreadLocal[DefaultArgsMode] {
    override def initialValue(): DefaultArgsMode = DefaultArgsMode.ArgsAreValues
  }

  protected def assertIsSatisfied[T: AsResult]: Result with Product with Serializable = {
    Try(context.assertIsSatisfied()).map(_ ⇒ Success()).recover {
      case t: Throwable ⇒ Error(t)
    }.get
  }

  private [specs2] def throwIfAssertUnsatisfied(): Unit = context.assertIsSatisfied()

  implicit def anyAsResult[A] : AsResult[A] = new AsResult[A]{
    def asResult(a: =>A) =
      ResultExecution.effectively { a; Success() }
  }

  def useImposteriser(imposteriser: Imposteriser): Unit = context.setImposteriser(imposteriser)

  useImposteriser(new DelegatingImposteriser(this))

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
  def exactly(count: Int): ReceiverClause = expectations.exactly(count)
  def atLeast(count: Int): ReceiverClause = expectations.atLeast(count)
  def atMost(count: Int): ReceiverClause = expectations.atMost(count)
  def ignoring[T](mockObject: T) = expectations.ignoring(mockObject)
  @deprecated("then is now a deprecated identifier in scala, use set instead.")
  def `then`(state: State) = expectations.`then`(state)
  def set(state: State) = expectations.`then`(state)
  def when(predicate: StatePredicate) = expectations.when(predicate)

  def any[T](implicit ct: ClassTag[T]): Matcher[T] = new Matcher[T] {
    override def apply[S <: T](t: Expectable[S]): MatchResult[S] = {
      val superClass = ct.runtimeClass
      val expectedValue: S = t.value
      val expectableClass = expectedValue.getClass
      val IntClass = classOf[Int]
      val ShortClass = classOf[Short]
      val ByteClass = classOf[Byte]
      val LongClass = classOf[Long]
      val FloatClass = classOf[Float]
      val DoubleClass = classOf[Double]
      val BooleanClass = classOf[Boolean]
      val CharClass = classOf[Char]
      val isMatching = superClass match {
        case IntClass ⇒ IntClass.isAssignableFrom(expectableClass) || expectedValue.isInstanceOf[java.lang.Integer]
        case ByteClass ⇒ ByteClass.isAssignableFrom(expectableClass) || expectedValue.isInstanceOf[java.lang.Byte]
        case DoubleClass ⇒ DoubleClass.isAssignableFrom(expectableClass) || expectedValue.isInstanceOf[java.lang.Double]
        case ShortClass ⇒ ShortClass.isAssignableFrom(expectableClass) || expectedValue.isInstanceOf[java.lang.Short]
        case FloatClass ⇒ FloatClass.isAssignableFrom(expectableClass) || expectedValue.isInstanceOf[java.lang.Float]
        case LongClass ⇒ LongClass.isAssignableFrom(expectableClass) || expectedValue.isInstanceOf[java.lang.Long]
        case BooleanClass ⇒ BooleanClass.isAssignableFrom(expectableClass) || expectedValue.isInstanceOf[java.lang.Boolean]
        case CharClass ⇒ CharClass.isAssignableFrom(expectableClass) || expectedValue.isInstanceOf[java.lang.Character]
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
  def `with`[T](value: T)(implicit ct: ClassTag[T]): T = expectations.`with`(value)
  def having[T](m: Matcher[T]): T = `with`(m)
  def having[T](value: T)(implicit ct: ClassTag[T]): T = `with`(value)

  private def mockWithFallback[T](mock: ⇒T)(implicit ct: ClassTag[T]): T = {
    Try(mock).recover({
      case e: IllegalArgumentException if usingJavaReflectionImposteriser && !ct.runtimeClass.isInterface ⇒
        throw e

      case _: Exception if usingJavaReflectionImposteriser ⇒
        useClassImposterizer()
        val secondTry = Try(mock)
        useJavaReflectionImposterizer()
        secondTry.get

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

  def repeatedly(actions:Action*): Action = {
    new CustomAction("repeatedly") {
      var iterator = actions.iterator
      override def invoke(invocation: Invocation): AnyRef = {
        if (!iterator.hasNext)
          iterator = actions.iterator
        iterator.next().invoke(invocation)
      }
    }
  }

  def defaultArgsAreMatchers[T](t : => T): T = {
    try {
      defaultMethodArgsModeRef.set(ArgsAreMatchers)
      t
    } finally defaultMethodArgsModeRef.set(ArgsAreValues)
  }

  private [specs2] def areDefaultArgsMatchers = defaultMethodArgsModeRef.get == ArgsAreMatchers

  implicit class Stubbed[T](c: T) {

    def will(action: Action, consecutive: Action*): Unit = {
      if (consecutive.isEmpty)
        expectations.will(action)
      else
        expectations.will(AbstractExpectations.onConsecutiveCalls(action +: consecutive: _*))
    }

    def willReturn[K <: T](t: K): Unit = will(returnValue(t))

    def willThrow[K <: Throwable](t: K): Unit = will(throwException(t))

    def willSet(state: State): Unit = set(state)
  }

  implicit class StatesOps(states: States) {
    def to = states.is _
  }

  implicit class StubbedAnyRef[T <: AnyRef](c:T) {

    private def msg = s"Answer for value ${c.getClass.getSimpleName}"

    def willAnswer[I1, K <: T](function1: I1 => K): Unit =
      will(new AnswerAction1(msg, function1))

    def willAnswer[I1, I2, K <: T](function2: (I1, I2) => K): Unit =
      will(new AnswerAction2(msg, function2))

    def willAnswer[I1, I2, I3, K <: T](function3: (I1, I2, I3) => K): Unit =
      will(new AnswerAction3(msg, function3))

    def willAnswer[I1, I2, I3, I4, K <: T](function4: (I1, I2, I3, I4) => K): Unit =
      will(new AnswerAction4(msg, function4))
  }

}


abstract class AnswerAction(msg: String) extends CustomAction(msg) {
  implicit class `Invocation with param`(invocation: Invocation) {
    def apply[P](index: Int): P = invocation.getParameter(index).asInstanceOf[P]
  }

}

class AnswerAction1[I1, K <: AnyRef](msg: String, f: I1 => K) extends AnswerAction(msg) {
  override def invoke(invocation: Invocation): AnyRef = f(invocation(0))
}

class AnswerAction2[I1, I2, K <: AnyRef](msg: String, f: (I1, I2) => K) extends AnswerAction(msg) {
  override def invoke(invocation: Invocation): AnyRef = f(invocation(0), invocation(1))
}

class AnswerAction3[I1, I2, I3, K <: AnyRef](msg: String, f: (I1, I2, I3) => K) extends AnswerAction(msg) {
  override def invoke(invocation: Invocation): AnyRef = f(invocation(0), invocation(1), invocation(2))
}

class AnswerAction4[I1, I2, I3, I4, K <: AnyRef](msg: String, f: (I1, I2, I3, I4) => K) extends AnswerAction(msg) {
  override def invoke(invocation: Invocation): AnyRef = f(invocation(0), invocation(1), invocation(2), invocation(3))
}

sealed trait DefaultArgsMode
object DefaultArgsMode {
  case object ArgsAreValues extends DefaultArgsMode
  case object ArgsAreMatchers extends DefaultArgsMode
}
