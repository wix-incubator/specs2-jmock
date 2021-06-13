package com.wixpress.common.specs2

import org.jmock.api.{Imposteriser, Invokable}
import org.jmock.imposters.ByteBuddyClassImposteriser

import scala.util.Try

class DelegatingImposteriser(jmock: JMockDsl) extends Imposteriser {

  val reflectionImposteriser = new impostisers.JavaReflectionImposteriser(jmock)
  val classImposteriser = ByteBuddyClassImposteriser.INSTANCE

  override def canImposterise(aClass: Class[_]): Boolean =
    if(jmock.usingJavaReflectionImposteriser) reflectionImposteriser.canImposterise(aClass) else classImposteriser.canImposterise(aClass)


  override def imposterise[T](invokable: Invokable, aClass: Class[T], classes: Class[_]*): T = {
    if(jmock.usingJavaReflectionImposteriser){
      Try {
        reflectionImposteriser.imposterise(invokable, aClass, classes: _*)
      }.recover{
        case e: IllegalArgumentException ⇒
          if(aClass.isInterface) classImposteriser.imposterise(invokable, aClass, classes: _*) else throw e
      }.get
    }
    else {
      classImposteriser.imposterise(invokable, aClass, classes: _*)
    }
  }
}
