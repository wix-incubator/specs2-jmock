package com.wixpress.common.specs2

import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class ReusableExpectationsTest extends Specification {

  trait MyContext extends CollaboratorExpectations

  "Something should break" >> new MyContext {
    success
  }.pendingUntilFixed("This test should fail")
}

trait CollaboratorExpectations extends scope.JMock with Scope {
  val collaborator = mock[Collaborator]

  checking {
    oneOf(collaborator).doIt()
  }
}


trait Collaborator {
  def doIt(): Unit
}