package com.wixpress.common.specs2

import org.jmock.api.{Invokable, Imposteriser}
import org.jmock.lib.JavaReflectionImposteriser
import org.jmock.lib.legacy.ClassImposteriser

import scala.util.Try

class DelegatingImposteriser(jmock: JMock) extends Imposteriser {

  val reflectionImposteriser = JavaReflectionImposteriser.INSTANCE
  val classImposteriser = ClassImposteriser.INSTANCE

  override def canImposterise(aClass: Class[_]): Boolean =
    if(jmock.usingJavaReflectionImposteriser) reflectionImposteriser.canImposterise(aClass) else classImposteriser.canImposterise(aClass)


  override def imposterise[T](invokable: Invokable, aClass: Class[T], classes: Class[_]*): T = {
    if(jmock.usingJavaReflectionImposteriser){
      Try {
        reflectionImposteriser.imposterise(invokable, aClass, classes: _*)
      }.recover{
        case e: IllegalArgumentException ⇒ {
          if(aClass.isInterface) classImposteriser.imposterise(invokable, aClass, classes: _*) else throw e
        }
      }.get
    }
    else {
      classImposteriser.imposterise(invokable, aClass, classes: _*)
    }
  }
}
