package com.wixpress.common.specs2.impostisers

import com.wixpress.common.specs2.JMockDsl
import com.wixpress.common.specs2.impostisers.ScalaAwareImposteriser.defaultArgMethodPattern
import org.jmock.api.{Imposteriser, Invokable}

import java.lang.reflect.Method

class ScalaAwareImposteriser(delegate: Imposteriser, jmock: JMockDsl) extends Imposteriser {
  override def canImposterise(`type`: Class[_]): Boolean = delegate.canImposterise(`type`)

  override def imposterise[T](mockObject: Invokable, mockedType: Class[T], ancilliaryTypes: Class[_]*): T = {
    val interceptor: Invokable = inv => {
      if(ScalaAwareImposteriser.isDefaultArgMethod(inv.getInvokedMethod)) {
        val value = DefaultMethodInvoker.instance.invoke(inv.getInvokedObject, inv.getInvokedMethod, inv.getParametersAsArray)
        if(jmock.areDefaultArgsMatchers) {
          jmock.having(value)
        }
        value
      }
      else mockObject.invoke(inv)
    }
    delegate.imposterise(interceptor, mockedType, ancilliaryTypes:_*)
  }
}

object ScalaAwareImposteriser {
  private val defaultArgMethodPattern = ".*\\$default\\$\\d+".r.pattern
  def isDefaultArgMethod(method: Method) =
    method.getParameterCount == 0 && defaultArgMethodPattern.matcher(method.getName).matches()
}
