package com.wixpress.common.specs2.impostisers

import java.lang.invoke.{MethodHandles, MethodType}
import java.lang.reflect.{Constructor, Method}

sealed trait DefaultMethodInvoker {
  def invoke(proxy: AnyRef, method: Method, args: Array[AnyRef]): AnyRef
}

/**
 * Invokes default java interface methods, bypassing any overrides (such as dynamic proxy invocation handler)
 * Based on https://stackoverflow.com/a/58800339/10035812
 */
object DefaultMethodInvoker {
  private val java8ClassVersion = 52
  private val javaClassVersion = System.getProperty("java.class.version").toFloat
  val instance = if(javaClassVersion <= java8ClassVersion) java8Invoker else java9AndLaterInvoker

  private lazy val java8Invoker = new DefaultMethodInvoker {
    private val constructor: Constructor[MethodHandles.Lookup] = classOf[MethodHandles.Lookup].getDeclaredConstructor(classOf[Class[_]])
    constructor.setAccessible(true)

    override def invoke(impl: AnyRef, method: Method, args: Array[AnyRef]): AnyRef = {
      val clazz = method.getDeclaringClass
      constructor.newInstance(clazz)
        .in(clazz)
        .unreflectSpecial(method, clazz)
        .bindTo(impl)
        .invokeWithArguments(args:_*)
    }
  }

  private lazy val java9AndLaterInvoker = new DefaultMethodInvoker {
    override def invoke(proxy: AnyRef,
                        method: Method,
                        args: Array[AnyRef]): AnyRef = {

      MethodHandles.lookup
        .findSpecial(
          method.getDeclaringClass,
          method.getName,
          MethodType.methodType(method.getReturnType, method.getParameterTypes),
          method.getDeclaringClass)
        .bindTo(proxy)
        .invokeWithArguments(args:_*)
    }
  }
}
