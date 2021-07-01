package com.wixpress.common.specs2.impostisers

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.wixpress.common.specs2.JMockDsl
import com.wixpress.common.specs2.impostisers.ScalaAwareImposteriser.{JacksonSafe, MockInCapturingState}
import org.jmock.api.{Imposteriser, Invocation, Invokable}
import org.jmock.internal.{CaptureControl, ExpectationCapture, InvocationToExpectationTranslator, ObjectMethodExpectationBouncer, ReturnDefaultValueAction}

import java.lang.reflect.Method

class ScalaAwareImposteriser(delegate: Imposteriser, jmock: JMockDsl) extends Imposteriser {
  private val defaultAction = new ReturnDefaultValueAction(delegate)

  override def canImposterise(`type`: Class[_]): Boolean = delegate.canImposterise(`type`)

  override def imposterise[T](mockObject: Invokable, mockedType: Class[T], ancilliaryTypes: Class[_]*): T = {
    imposteriseInternal(inCaptureMode = false)(mockObject, mockedType, ancilliaryTypes:_*)
  }

  private def imposteriseInternal[T](inCaptureMode: Boolean, compatMode: Boolean = false)(mockObject: Invokable, mockedType: Class[T], ancilliaryTypes: Class[_]*) = {
    val interceptor: Invokable = new Invokable {
      override def invoke(invocation: Invocation): AnyRef = {
        captureExpectationToInvocation(mockedType, jmock.isDefaultArgsCompatibilityEnabled)(invocation) getOrElse {
          val inv: Invocation = preProcessParams(invocation)
          if (ScalaAwareImposteriser.isDefaultArgMethod(inv.getInvokedMethod)) {
            // before default args were supported this was the behavior during expectation capture
            if (compatMode && inCaptureMode) defaultAction.invoke(invocation)
            else tryInvokeDefaultArgMethod(inv, orElse = mockObject.invoke(inv))
          } else mockObject.invoke(inv)
        }
      }.asInstanceOf[AnyRef]
    }
    delegate.imposterise(interceptor, mockedType, ancilliaryTypes :+ classOf[JacksonSafe] :_*)
  }

  /**
   * if what is being invoked is [[CaptureControl.captureExpectationTo]], tag the returned mock
   */
  private def captureExpectationToInvocation[T](mockedType: Class[T], compatMode: Boolean)(invocation: Invocation): Option[T] = {
    val CaptureControlClass = classOf[CaptureControl]
    (invocation.getInvokedMethod.getName,  invocation.getInvokedMethod.getDeclaringClass, invocation.getParametersAsArray.headOption) match {
      case ("captureExpectationTo", CaptureControlClass, Some(capture: ExpectationCapture)) =>
        Some(makeCapturingMock(capture, mockedType, compatMode))
      case _ => None
    }
  }

  /**
   * same as [[org.jmock.Mockery.MockObject.captureExpectationTo]] but adds a marker trait to the generated mock + calls imposteriseInternal(true)(...)
   */
  private def makeCapturingMock[T](capture: ExpectationCapture, mockedType: Class[_], compatMode: Boolean): T = {
    imposteriseInternal[T](inCaptureMode = true, compatMode)(
      new ObjectMethodExpectationBouncer(
        new InvocationToExpectationTranslator(
          capture,
          defaultAction)),
      mockedType.asInstanceOf[Class[T]],
      classOf[MockInCapturingState])
  }

  private def preProcessParams[T](inv: Invocation) = {
    val hasThunkParams = inv.getParametersAsArray.exists {
      case _: Function0[_] => true
      case _ => false
    }
    if(hasThunkParams) {
      val params = inv.getParametersAsArray.map {
        case p: Function0[AnyRef] => p.apply()
        case p => p
      }
      withModifiedParams(inv, params)
    } else inv
  }

  private def withModifiedParams(that: Invocation, newParams: Array[AnyRef]) = {
    val mode = if (that.isBuildingExpectation) Invocation.ExpectationMode.BUILDING
               else Invocation.ExpectationMode.ASSERTING
    new Invocation(mode, that.getInvokedObject, that.getInvokedMethod, newParams:_ *)
  }

  private def tryInvokeDefaultArgMethod[T](inv: Invocation,
                                           orElse: => AnyRef) = {
    try {
      SpecialMethodInvoker.invoke(inv.getInvokedObject, inv.getInvokedMethod, inv.getParametersAsArray)
    } catch { case _: Throwable => orElse }
  }
}

object ScalaAwareImposteriser {
  private val defaultArgMethodPattern = ".*\\$default\\$\\d+".r.pattern
  def isDefaultArgMethod(method: Method) =
    method.getParameterCount == 0 && defaultArgMethodPattern.matcher(method.getName).matches()

  @JsonSerialize(using = classOf[EmptySerializer[_]])
  trait JacksonSafe

  class EmptySerializer[T] extends JsonSerializer[T] {
    override def serialize(value: T,
                           gen: JsonGenerator,
                           serializers: SerializerProvider): Unit = gen.writeRaw("{}")
  }

  trait MockInCapturingState

}
