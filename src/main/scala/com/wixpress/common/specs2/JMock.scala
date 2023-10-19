package com.wixpress.common.specs2

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
import org.specs2.specification.AroundEach
import org.specs2.specification.core.SpecificationStructure
import scala.language.experimental.macros

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

trait JMockDsl extends MustThrownMatchers with ArgumentsShortcuts with ArgumentsArgs { outer =>
  isolated

  private val synchroniser: Synchroniser = new Synchroniser
  private[this] val context: Mockery = new Mockery{{setThreadingPolicy(synchroniser)}}
  val expectations = new Expectations
  private var defaultArgsCompatabilityInitial = true

  private lazy val defaultArgsCompatabilityMode = new ThreadLocal[Boolean] {
    override def initialValue(): Boolean = defaultArgsCompatabilityInitial
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

  def disableDefaultArgsCompatibility() = {
    defaultArgsCompatabilityInitial = false
  }

  def withoutDefaultArgsCompatibility[T](t: => T) =
    withDefaultArgsCompatibilityMode(compatibility = false)(t)

  def withDefaultArgsCompatibilityMode[T](compatibility: Boolean)(t: => T) = {
    val before = defaultArgsCompatabilityMode.get
    defaultArgsCompatabilityMode.set(compatibility)
    try t
    finally defaultArgsCompatabilityMode.set(before)
  }

  private [specs2] def isDefaultArgsCompatibilityEnabled = defaultArgsCompatabilityMode.get

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

  object expect {
    val jmockDsl: JMockDsl = outer
    def allowing[T, R](mock: T)(expr: T => R): R = macro Macros.allowingImpl
    def never[T, R](mock: T)(expr: T => R): R = macro Macros.neverImpl
    def ignoring[T, R](mock: T)(expr: T => R): R = macro Macros.ignoringImpl
    def oneOf[T, R](mock: T)(expr: T => R): R = macro Macros.oneOfImpl
    def atLeast[T, R](n: Int)(mock: T)(expr: T => R): R = macro Macros.atLeastImpl
    def atMost[T, R](n: Int)(mock: T)(expr: T => R): R = macro Macros.atMostImpl
    def exactly[T, R](n: Int)(mock: T)(expr: T => R): R = macro Macros.exactlyImpl
  }

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

  def addDefaultReturnValueFor[T: Manifest](value: T) = {
    context.setDefaultResultForType(manifest[T].runtimeClass, value)
  }

  def addDefaultReturnValues(vals: (Class[_], Any)*) = {
    vals.foreach { case (clazz, value) => context.setDefaultResultForType(clazz, value) }
  }

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

object Macros {
  import scala.reflect.macros.blackbox

  def allowingImpl(c: blackbox.Context)(mock: c.Tree)(expr: c.Tree): c.Tree = {
    import c.universe._
    expectation(c)(mock, expr, q"${c.prefix}.jmockDsl.allowing($mock)")
  }

  def neverImpl(c: blackbox.Context)(mock: c.Tree)(expr: c.Tree): c.Tree = {
    import c.universe._
    expectation(c)(mock, expr, q"${c.prefix}.jmockDsl.never($mock)")
  }

  def ignoringImpl(c: blackbox.Context)(mock: c.Tree)(expr: c.Tree): c.Tree = {
    import c.universe._
    expectation(c)(mock, expr, q"${c.prefix}.jmockDsl.ignoring($mock)")
  }

  def oneOfImpl(c: blackbox.Context)(mock: c.Tree)(expr: c.Tree): c.Tree = {
    import c.universe._
    expectation(c)(mock, expr, q"${c.prefix}.jmockDsl.oneOf($mock)")
  }

  def atLeastImpl(c: blackbox.Context)(n: c.Tree)(mock: c.Tree)(expr: c.Tree): c.Tree = {
    import c.universe._
    expectation(c)(mock, expr, q"${c.prefix}.jmockDsl.atLeast($n).of($mock)")
  }

  def atMostImpl(c: blackbox.Context)(n: c.Tree)(mock: c.Tree)(expr: c.Tree): c.Tree = {
    import c.universe._
    expectation(c)(mock, expr, q"${c.prefix}.jmockDsl.atMost($n).of($mock)")
  }

  def exactlyImpl(c: blackbox.Context)(n: c.Tree)(mock: c.Tree)(expr: c.Tree): c.Tree = {
    import c.universe._
    expectation(c)(mock, expr, q"${c.prefix}.jmockDsl.exactly($n).of($mock)")
  }

  private def expectation(c: blackbox.Context)(mock: c.Tree, expr: c.Tree, start:  c.Tree): c.Tree = {
    import c.universe._
    implicit val cc = c
//    println(s"expr  raw: ${showRaw(expr)}")
//    println(s"expr code: ${showCode(expr)}")
    def abortInvalidLambda(code: String) = c.abort(c.enclosingPosition, s"[$code] expected lambda of form `_.mockMethod(having(...), having(...))`, received: ${showCode(expr)}")
    expr match {
      // a simple call with arguments in order
      case Function(
              ValDef(_, TermName(lambdaParam), _, _) :: _, MethodInvocation(receiver, method, methodParams)
           ) if lambdaParam == receiver  =>
        generate(c)(start, receiver, method, methodParams.map(_.map(_.asInstanceOf[c.Tree])))
      // a call with named args out of order
      case Function(
          ValDef(_, TermName(lambdaParam), _, _) :: _,
          Block(statements, Apply(Select(Ident(TermName(receiver)), TermName(method)), methodParams))
         ) if statements.size == methodParams.size &&  lambdaParam == receiver =>

        var paramMap = statements.collect {
          case ValDef(_, name, _, p) => name -> p
        }.toMap
        if(paramMap.size != statements.size) {
          abortInvalidLambda("vals-count")
        }
        val args = methodParams.collect {
          case Ident(t: TermName) => paramMap.get(t).toSeq
          case t => Seq(t)
        }.flatten

        if(args.size != methodParams.size) {
          abortInvalidLambda(s"params-count ${args.size} != ${methodParams.size}")
        }
        generate(c)(start, receiver, method, Some(args))

      case _ => abortInvalidLambda("no-match")
    }
  }

  private object MethodInvocation {
    def unapply[CTX <: blackbox.Context](tree: CTX#Tree)(implicit c: CTX): Option[(String, String, Option[List[c.Tree]])] = {
      import c.universe._
      tree match {
        case  Apply(Select(Ident(TermName(receiver)), TermName(method)), methodParams) => Some(receiver, method, Some(methodParams))
        case  Select(Ident(TermName(receiver)), TermName(method)) => Some(receiver, method, None)
        case _ => None
      }
    }

    def apply(c: blackbox.Context)(receiver: String, method: String, params: Option[List[c.Tree]]) = {
      import c.universe._
      val select = Select(Ident(TermName(receiver)), TermName(method))
      params.fold(select: c.Tree)(ps => Apply(select, ps))
    }
  }

  private def generate[CTX <: blackbox.Context](c: CTX)
                      (start: c.Tree,
                       receiver: String,
                       method: String,
                       methodParams: Option[List[c.Tree]]) = {
    import c.universe._
    val capturingVal = "capturing$val"
    // if one of the parameters is a matcher this will be the prefix of the `.having` method
    val firstMatcherContext = methodParams.toList.flatten.flatMap(p => matcherParamContext(c)(p)).headOption
    val processedParams = methodParams.map(_.map(processParam(c)(_, receiver, capturingVal, firstMatcherContext)))
    val result =
      q"""{
              val ${ TermName(capturingVal) } = ${c.prefix}.jmockDsl.withoutDefaultArgsCompatibility($start)
              ..${ processedParams.toList.flatten.zipWithIndex.map { case (p, i) => q"val ${ TermName(s"pp$$$i") } = $p"}}
              ${ MethodInvocation(c)(capturingVal, method, methodParams.map(_.indices.toList.map(i => Ident(TermName(s"pp$$$i"))))) }
           }"""
//    println(s"generated: ${ showCode(result) }")
    result
  }

  /**
   * Convert code like `x$1.doSomething$default$2` to `capturing$val.doSomething$default$2` as `$x$1` is not available inside the generated code.
   * Wraps non matcher values in `having(beEqualTo(..))` if `hasMatcherParams` is true
   */
  private def processParam[CTX <: blackbox.Context](c: CTX)(p: c.Tree, receiver: String, newReceiver: String, matcherContext: Option[c.Tree]) = {
    import c.universe._
    if(matcherParamContext(c)(p).isDefined) p
    else {
      val handledDefault = p match {
        case Select(Ident(TermName(`receiver`)), TermName(methodName)) => Select(Ident(TermName(newReceiver)), TermName(methodName))
        case _ => p
      }
      matcherContext.fold(handledDefault) { context =>
        val pType = c.Expr[Any](c.typecheck(p)).actualType.widen // have to widen otherwise we get types like Boolean(false)
        q"$context.having[$pType](org.specs2.matcher.Matchers.beEqualTo[$pType]($handledDefault))"
      }
    }
  }

  private def matcherParamContext[CTX <: blackbox.Context](c: CTX)(p: c.Tree) = {
    import c.universe._
    p match {
      case Apply(TypeApply(Select(context, TermName(method)), _), _) if havingMethodNames(method) =>
        val calledOnType = c.Expr[Any](c.typecheck(context)).actualType
        if(calledOnType.baseType(c.symbolOf[JMockDsl]) == typeOf[JMockDsl]) Some(context)
        else None
      case _ => None
    }
  }

  private val havingMethodNames = Set("having", "with")
}

object JMockDsl {
  object DefaultReturnValues {
    val basicScalaCollections = Map[Class[_], Any](
      classOf[scala.Option[_]] -> None,
      classOf[scala.collection.Seq[_]] -> Seq.empty,
      classOf[scala.collection.Set[_]] -> Set.empty,
      classOf[scala.collection.Map[_, _]] -> Map.empty,
      classOf[scala.collection.immutable.Map[_, _]] -> Map.empty,
      classOf[scala.collection.immutable.Seq[_]] -> Seq.empty,
      classOf[scala.collection.immutable.Set[_]] -> Set.empty
    )
  }
}
