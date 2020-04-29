package com.wixpress.common.specs2

import org.jmock.api.{Imposteriser, Invokable}
import org.jmock.lib.JavaReflectionImposteriser
import org.specs2.mutable.Spec

class CustomImposteriserTest extends Spec with JMock {
  useImposteriser(CustomImposteriser)

  "use custom imposteriser" >> {
    mock[Collaborator] must (beAnInstanceOf[Collaborator] and beAnInstanceOf[CustomImposteriser.Marker])
  }
}

object CustomImposteriser extends Imposteriser {

  trait Marker

  override def canImposterise(`type`: Class[_]): Boolean =
    JavaReflectionImposteriser.INSTANCE.canImposterise(`type`)

  override def imposterise[T](mockObject: Invokable, mockedType: Class[T], ancilliaryTypes: Class[_]*): T =
    JavaReflectionImposteriser.INSTANCE.imposterise(mockObject, mockedType, classOf[Marker])
}
