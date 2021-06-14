package com.wixpress.common.specs2.impostisers

import com.wixpress.common.specs2.JMockDsl
import org.jmock.api.{Imposteriser, Invocation, Invokable}

import java.lang.reflect.Method
import scala.util.Try

class ScalaAwareImposteriser(delegate: Imposteriser, jmock: JMockDsl) extends Imposteriser {
  override def canImposterise(`type`: Class[_]): Boolean = delegate.canImposterise(`type`)

  override def imposterise[T](mockObject: Invokable, mockedType: Class[T], ancilliaryTypes: Class[_]*): T = {
    val interceptor: Invokable = new Invokable {
      override def invoke(inv: Invocation): AnyRef = {
        if(ScalaAwareImposteriser.isDefaultArgMethod(inv.getInvokedMethod)) {
         tryInvokeDefaultArgMethod(inv, orElse = mockObject.invoke(inv))
        }
        else mockObject.invoke(inv)
      }
    }
    delegate.imposterise(interceptor, mockedType, ancilliaryTypes:_*)
  }

  private def tryInvokeDefaultArgMethod[T](inv: Invocation,
                                           orElse: => AnyRef) = {
    try {
      val value = SpecialMethodInvoker.invoke(inv.getInvokedObject, inv.getInvokedMethod, inv.getParametersAsArray)
      if (jmock.areDefaultArgsMatchers) {
        jmock.having(value)
      }
      value
    } catch { case e: Throwable =>
      new Throwable(s"invoke special failed for ${inv.getInvokedMethod}", e).printStackTrace()
      orElse
    }
  }
}

object ScalaAwareImposteriser {
  private val defaultArgMethodPattern = ".*\\$default\\$\\d+".r.pattern
  def isDefaultArgMethod(method: Method) =
    method.getParameterCount == 0 && defaultArgMethodPattern.matcher(method.getName).matches()
}
