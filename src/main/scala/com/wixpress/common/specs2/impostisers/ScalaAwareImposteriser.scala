package com.wixpress.common.specs2.impostisers

import com.wixpress.common.specs2.JMockDsl
import org.jmock.api.{Imposteriser, Invocation, Invokable}

import java.lang.reflect.Method

class ScalaAwareImposteriser(delegate: Imposteriser, jmock: JMockDsl) extends Imposteriser {
  override def canImposterise(`type`: Class[_]): Boolean = delegate.canImposterise(`type`)

  override def imposterise[T](mockObject: Invokable, mockedType: Class[T], ancilliaryTypes: Class[_]*): T = {
    val interceptor: Invokable = new Invokable {
      override def invoke(invocation: Invocation): AnyRef = {
        val inv: Invocation = preProcessParams(invocation)
        if(ScalaAwareImposteriser.isDefaultArgMethod(inv.getInvokedMethod)) {
         tryInvokeDefaultArgMethod(inv, orElse = mockObject.invoke(inv))
        } else mockObject.invoke(inv)
      }
    }
    delegate.imposterise(interceptor, mockedType, ancilliaryTypes:_*)
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
    new Invocation(mode, that.getInvokedObject, that.getInvokedMethod, newParams)
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
}
