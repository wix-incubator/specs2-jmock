package com.wixpress.common.specs2.impostisers

import com.wixpress.common.specs2.JMockDsl
import com.wixpress.common.specs2.impostisers.JavaReflectionImposteriser.defaultArgMethodPattern
import org.jmock.api.Invocation.ExpectationMode
import org.jmock.api.{Imposteriser, Invocation, Invokable}
import org.jmock.internal.SearchingClassLoader
import org.specs2.matcher.Matchers

import java.lang.reflect.{InvocationHandler, Method, Proxy}




/**
 * Modified version of [[org.jmock.lib.JavaReflectionImposteriser]] with support for Scala default parameter values
 */
class JavaReflectionImposteriser(jmock: JMockDsl) extends Imposteriser {
  override def canImposterise(`type`: Class[_]): Boolean = `type`.isInterface

  @SuppressWarnings(Array("unchecked"))
  override def imposterise[T](mockObject: Invokable,
                              mockedType: Class[T],
                              ancilliaryTypes: Class[_]*): T = {
    val proxiedClasses = prepend(mockedType, ancilliaryTypes.toArray)
    val classLoader = SearchingClassLoader.combineLoadersOf(proxiedClasses:_*)
    Proxy.newProxyInstance(
      classLoader, proxiedClasses, new InvocationHandler() {
        @throws[Throwable]
        override def invoke(proxy: AnyRef, method: Method, args: Array[AnyRef]): AnyRef = {
          if(method.isDefault && defaultArgMethodPattern.matcher(method.getName).matches()) {
            def value = DefaultMethodInvoker.instance.invoke(proxy, method, args)
            if(jmock.areDefaultArgsMatchers) {
              jmock.having(Matchers.beEqualTo(value))
            }
            value
          }
          else {
            mockObject.invoke(new Invocation(ExpectationMode.LEGACY, proxy, method, args:_*))
          }
        }
      }).asInstanceOf[T]
  }

  private def prepend(first: Class[_], rest: Array[Class[_]]) = {
    val proxiedClasses = new Array[Class[_]](rest.length + 1)
    proxiedClasses(0) = first
    System.arraycopy(rest, 0, proxiedClasses, 1, rest.length)
    proxiedClasses
  }
}

object JavaReflectionImposteriser {
  private val defaultArgMethodPattern = ".*\\$default\\$\\d+".r.pattern
}
